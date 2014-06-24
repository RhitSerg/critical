/*
 * Equals Checker (EQ)
 * 
 * Copyright (C) 2010 Chandan Raj Rupakheti & Daqing Hou, Clarkson University
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contact Us:
 * Chandan Raj Rupakheti (rupakhcr@clarkson.edu)
 * Daqing Hou (dhou@clarkson.edu)
 * Clarkson University
 * PO Box 5722
 * Potsdam
 * NY 13699-5722
 * http://serl.clarkson.edu
*/
 
package edu.clarkson.serl.critic.views;

import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import edu.clarkson.serl.critic.CriticPlugin;


/**
 *
 *
 * @author Chandan Raj Rupakheti (rupakhcr@clarkson.edu)
 *
 */
public class ProjectNameDialog {
	private Shell shell;
	private Shell dialog;
	private Label label;
	private Table table;
	private Button ok;
	private Button cancel;
	private IJavaProject javaProject;
	
	
	public ProjectNameDialog(Shell shell) {
		this.shell = shell;
	}
	
	public synchronized IJavaProject getProject() throws Exception {
		createDialog();
		addLabel();
		addTable();
		addButtons();
		addListeners();
		
		dialog.pack ();
		dialog.open ();
		while (!dialog.isDisposed ()) {
			if (!shell.getDisplay().readAndDispatch ()) shell.getDisplay().sleep();
		}
		return javaProject;
	}
	
	public synchronized String getProjectName() throws Exception {
		IJavaProject javaProject = getProject();
		if(javaProject != null)
			return javaProject.getElementName();
		return "";
	}
	
	private void createDialog() {
		Rectangle dim = shell.getClientArea();
		dialog = new Shell (shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialog.setText("CriticAL - Project Selection Dialog");

		int x = shell.getLocation().x + dim.width / 3;
		int y = shell.getLocation().y + dim.height / 3;
		dialog.setLocation(x, y);
		
		dialog.setLocation( x , y);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		dialog.setLayout (gridLayout);
		// Handle Escape: Close the dialog
		dialog.addListener (SWT.Traverse, new Listener () {
			public void handleEvent (Event event) {
				switch (event.detail) {
					case SWT.TRAVERSE_ESCAPE:
						dialog.close ();
						event.detail = SWT.TRAVERSE_NONE;
						event.doit = false;
						break;
				}
			}
		});
	}
	
	private void addLabel() {
		// Add label to the dialog
		label = new Label(dialog, SWT.PUSH);
		label.setText("Please select a project from the list below:");
		GridData labelGridData = new GridData();
		labelGridData.horizontalAlignment = GridData.FILL;
		labelGridData.horizontalSpan = 2;
		label.setLayoutData(labelGridData);
	}
	
	private void addTable() throws Exception {
		// Add table to the dialog
		table = new Table (dialog, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
//		GridData data = new GridData (SWT.FILL, SWT.FILL, true, true);
		GridData data = new GridData ();
		data.heightHint = 200;
		data.horizontalSpan = 2;
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.FILL;
		table.setLayoutData (data);
		table.setLinesVisible (true);
		table.setHeaderVisible (true);
		
		TableColumn column = new TableColumn (table, SWT.LEAD);
		column.setText("Java Project List");
		
		JavaElementLabelProvider labelProvider = new JavaElementLabelProvider();
		IJavaModel javaModel = CriticPlugin.getJavaModel();
		IJavaProject[] javaProjects = javaModel.getJavaProjects();
		TreeSet<IJavaProject> set = new TreeSet<IJavaProject>(new Comparator<IJavaProject>() {
			public int compare(IJavaProject thisP, IJavaProject thatP) {
				return thisP.getElementName().compareTo(thatP.getElementName());
			}
		});
		set.addAll(Arrays.asList(javaProjects));
		
		for(IJavaProject javaProject : set) {
			TableItem item = new TableItem (table, SWT.NULL);
			item.setText(javaProject.getElementName());
			item.setImage(labelProvider.getImage(javaProject));
			item.setData(javaProject);
		}
		column.pack();
		column.setWidth(300);
	}
	
	private void addButtons() {
		// Add ok button to the form
		ok = new Button (dialog, SWT.PUSH);
		ok.setText ("OK");
		ok.setLayoutData(new GridData());
		
		// Add cancel button to the form
		cancel = new Button (dialog, SWT.PUSH);
		cancel.setText ("Cancel");
		cancel.setLayoutData(new GridData());
	}
	
	private void addListeners() {
		Listener listener =new Listener () {
			public void handleEvent (Event event) {
				if(event.widget == ok || event.widget == table) {
					TableItem[] selection = table.getSelection();
					if(selection == null || selection.length == 0) {
						MessageDialog.openError(dialog, "Invalid Project Selection", "There was no selection of project. Please select again");
						return;
					}
					if(selection[0] == null) {
						MessageDialog.openError(dialog, "Invalid Project Selection", "There was no selection of project. Please select again");
						return;
					}
					javaProject = (IJavaProject)selection[0].getData();
				}
				dialog.close ();
			}
		};
		ok.addListener (SWT.Selection, listener);
		cancel.addListener (SWT.Selection, listener);
		table.addListener(SWT.MouseDoubleClick, listener);
	}
}
