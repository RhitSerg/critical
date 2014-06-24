/*
 * MarkerMenuContribution.java
 * Feb 5, 2012
 *
 * CriticAL: A Critic for APIs and Libraries
 * 
 * Copyright (C) 2011 Chandan Raj Rupakheti & Daqing Hou, Clarkson University
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contact Us:
 * Chandan Raj Rupakheti <rupakhcr@clarkson.edu> 
 * Daqing Hou <dhou@clarkson.edu>
 * Clarkson University
 * Potsdam
 * NY 13699-5722
 * USA
 * http://critical.sf.net
 */

package edu.clarkson.serl.critic.reporter;

import java.util.List;
import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.part.FileEditorInput;

import edu.clarkson.serl.critic.CriticPlugin;
/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class MarkerMenuContribution extends ContributionItem {
	private ITextEditor editor;
	private IVerticalRulerInfo rulerInfo;
	private List<IMarker> markers;

	public MarkerMenuContribution(ITextEditor editor){
		this.editor = editor;
		this.rulerInfo = getRulerInfo();
		this.markers = getMarkers();
	}

	private IVerticalRulerInfo getRulerInfo() {
		return (IVerticalRulerInfo) editor.getAdapter(IVerticalRulerInfo.class);
	}

	private List<IMarker> getMarkers(){
		List<IMarker> clickedOnMarkers = new ArrayList<IMarker>();
		List<IMarker> markers = CriticMarkerFactory.findMarkers(((FileEditorInput) editor.getEditorInput()).getFile());
		for (IMarker marker : markers){
			if (markerHasBeenClicked(marker)){
				clickedOnMarkers.add(marker);
			}
		}
		return clickedOnMarkers;
	}

	//Determine whether the marker has been clicked using the ruler's mouse listener
	private boolean markerHasBeenClicked(IMarker marker){
		return (marker.getAttribute(IMarker.LINE_NUMBER, 0)) == (rulerInfo.getLineOfLastMouseButtonActivity() + 1);
	}

	@Override
	//Create a menu item for each marker on the line clicked on
	public void fill(Menu menu, int index){
		if(markers == null || markers.isEmpty())
			return;

		// Get shared image registry of CriticAL
		ImageRegistry imgRegistry = CriticPlugin.getDefault().getImageRegistry();

		// Add Critiques Menu Item
		MenuItem criticsMenuItem = new MenuItem(menu, SWT.CASCADE, index);
		criticsMenuItem.setText("Critiques");
		criticsMenuItem.setImage(imgRegistry.get(CriticPlugin.IMG_CRITICAL_MAIN));


		// Add Separator
		new MenuItem(menu, SWT.SEPARATOR, 1);

		// Add Critiques sub-menu
		Menu critiquesMenu = new Menu(menu);
		criticsMenuItem.setMenu(critiquesMenu);
		
		// Add tooltip
		final ToolTip tip = new ToolTip(critiquesMenu.getShell(), SWT.ICON_INFORMATION);
		tip.setAutoHide(false);
		critiquesMenu.addMenuListener(this.createDynamicMenuAdapter(tip));

		// Now add the menu item
	    String prevType = "";
	    boolean seperator = false;
		int c = 0, r = 0, e = 0;
		for (final IMarker marker : markers) {	
			Image image = null;
			String title = null;
			try {
				if(marker.isSubtypeOf(CriticMarkerFactory.MARKER_CRITICISM)) {
					++c;
					image = imgRegistry.get(CriticPlugin.IMG_CRITICISM);
					title = "Criticism #" + c + "       ";
					
					if(prevType.equals("") || prevType.equals(CriticMarkerFactory.MARKER_CRITICISM))
						seperator = false;
					else
						seperator = true;
					prevType = CriticMarkerFactory.MARKER_CRITICISM;
				}
				else if(marker.isSubtypeOf(CriticMarkerFactory.MARKER_EXPLANATION)) {
					++e;
					image = imgRegistry.get(CriticPlugin.IMG_EXPLANATION);
					title = "Explanation #" + e + "     ";

					if(prevType.equals("") || prevType.equals(CriticMarkerFactory.MARKER_EXPLANATION))
						seperator = false;
					else 
						seperator = true;
					prevType = CriticMarkerFactory.MARKER_EXPLANATION;
				}
				else if(marker.isSubtypeOf(CriticMarkerFactory.MARKER_RECOMMENDATION)) {
					++r;
					image = imgRegistry.get(CriticPlugin.IMG_RECOMMENDATION);
					title = "Recommendation #" + r;
					
					if(prevType.equals("") || prevType.equals(CriticMarkerFactory.MARKER_RECOMMENDATION))
						seperator = false;
					else 
						seperator = true;
					prevType = CriticMarkerFactory.MARKER_RECOMMENDATION;
				}
			}
			catch(Exception ex){}

			// Add separators
			if(seperator)
				new MenuItem(critiquesMenu, SWT.SEPARATOR);

			MenuItem menuItem = new MenuItem(critiquesMenu, SWT.PUSH);
			menuItem.setImage(image);
			menuItem.setText(title);

			// We will pass this to arm listener for display
			menuItem.addSelectionListener(createDynamicSelectionListener(marker));
			menuItem.addArmListener(this.createDynamicArmListener(marker, tip));
		}
	}

	//Action to be performed when clicking on the menu item is defined here
	private SelectionAdapter createDynamicSelectionListener(final IMarker marker){
		return new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				try {
					ShowDetailsHandler.showDetails(marker);
				}
				catch(Exception ex) {
					CriticPlugin.log(Status.WARNING, ex.getMessage(), ex);
				}
			}
		};
	}

	private ArmListener createDynamicArmListener(final IMarker marker, final ToolTip tip){
		return new ArmListener() {
			@Override
			public void widgetArmed(ArmEvent e) {
				if(tip.isVisible())
					tip.setVisible(false);
				
				// Add tool tip to the menu item
				MenuItem source = (MenuItem)e.widget;
				
				tip.setMessage(marker.getAttribute(IMarker.MESSAGE, ""));
				Point location = source.getDisplay().getCursorLocation();

				// Use graphics control to estimate the end location of the MenuItem so that 
				// we can start the tool tip from that point
				GC gc = new GC(source.getDisplay());
				Point size = gc.textExtent(source.getText()); 
				int newX = location.x + size.x;				// Add width of text
				Image icon = source.getImage();
				if(icon != null)
					newX += icon.getBounds().width;			// Add width of image
				
				tip.setLocation(newX + 80, location.y);		// Add small width more
				gc.dispose();

				tip.setVisible(true);
			}
		};
	}
	
	private MenuAdapter createDynamicMenuAdapter(final ToolTip tip) {
		return new MenuAdapter() {
			@Override
			public void menuHidden(MenuEvent e) {
				if(tip.isVisible())
					tip.setVisible(false);
			}
		};
	}
}
