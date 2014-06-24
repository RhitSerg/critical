/*
 * ShowDetailsHandler.java
 * Sep 20, 2011
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

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.clarkson.serl.critic.CriticPlugin;
import edu.clarkson.serl.critic.extension.ICritic;
import edu.clarkson.serl.critic.views.HtmlViewer;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class ShowDetailsHandler extends AbstractHandler {

	/**
	 * 
	 */
	public ShowDetailsHandler() {
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelection(event);
		
		// We are only interested in one selection and assuming here that to be the first one
		Object entry = selection.getFirstElement();
		if(entry instanceof IAdaptable) {
			try {
				IAdaptable adaptable = (IAdaptable)entry;
				IMarker marker = (IMarker)adaptable.getAdapter(IMarker.class);
				if(marker != null) {
					showDetails(marker);
				}
			}
			catch(Exception e) {
				CriticPlugin.log(Status.WARNING, e.getMessage(), e);
			}
		}
		return null;
	}
	
	public static void showDetails(IMarker marker) throws Exception {
		IWorkbenchWindow workWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage workPage = workWindow.getActivePage();
		HtmlViewer htmlView = (HtmlViewer)workPage.showView(HtmlViewer.ID);
		htmlView.displayMarker(marker);
	}
}
