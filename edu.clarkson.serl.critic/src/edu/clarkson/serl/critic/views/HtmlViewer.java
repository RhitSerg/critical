/*
 * HtmlViewer.java
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
 
package edu.clarkson.serl.critic.views;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.clarkson.serl.critic.CriticPlugin;
import edu.clarkson.serl.critic.extension.ICritic;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class HtmlViewer extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.clarkson.serl.critic.htmlView";
	
	public static class EditorUtility {
		private EditorUtility() {
			super();
		}

		public static IEditorPart getActiveEditor() {
			IWorkbenchWindow window = CriticPlugin.getActiveWorkbenchWindow(); 
			if (window != null) {
				IWorkbenchPage page= window.getActivePage();
				if (page != null) {
					return page.getActiveEditor();
				}
			}
			return null;
		}
		
		public static ITypeRoot getJavaInput(IEditorPart part) {
			IEditorInput editorInput= part.getEditorInput();
			if (editorInput != null) {
				IJavaElement input= JavaUI.getEditorInputJavaElement(editorInput);
				if (input instanceof ITypeRoot) {
					return (ITypeRoot) input;
				}
			}
			return null;	
		}

		public static void selectInEditor(ITextEditor editor, int offset, int length) {
			IEditorPart active = getActiveEditor();
			if (active != editor) {
				editor.getSite().getPage().activate(editor);
			}
			editor.selectAndReveal(offset, length);
		}
		
		public static int[] getIJavaElementFor(IType aType, int lineNumber) throws Exception {
			ICompilationUnit cu = aType.getCompilationUnit();
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(cu);
			CompilationUnit root =  (CompilationUnit)parser.createAST(null);
			
			if(lineNumber < 1) {
				return new int[]{root.getStartPosition(), root.getLength()};
			}
			
			int startPos = root.getPosition(lineNumber, 0);
			int endPos = root.getPosition(lineNumber + 1, 0);
			int length = endPos - startPos;
			
			return new int[]{startPos, length};
		}	
	}	

	private IMarker marker;
	private Browser browser;
	private Action forwardAction;
	private Action backAction;

	/**
	 * The constructor.
	 */
	public HtmlViewer() {
	}
	
	public void displayMarker(IMarker marker) {
		String uri = null;
		try {
			uri = (String)marker.getAttribute(ICritic.URL);
		}
		catch(Exception e) {}
		
		if(uri == null) {
			MessageDialog.openError(this.getSite().getShell(), "Document Unavailable", "The detialed documentation for the message is unavailable at the moment.");
		}
		else {
			this.marker = marker;
			this.browser.setUrl(uri);
		}
	}
	
	public void clear() {
		this.marker = null;
		this.browser.setText("Sorry nothing to show!");
	}
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		// Fill the parent window with the buttons and sash
		parent.setLayout(new FillLayout());

		// Create the SashForm and the buttons
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		try {
			browser = new Browser(sashForm, SWT.NONE);
			browser.addProgressListener(new ProgressListener() {
				public void changed(ProgressEvent event) {
				}

				public void completed(ProgressEvent event) {
					// When browser has completely loaded the page
				}
			});
			
			browser.addLocationListener(new LocationListener() {
				public void changed(LocationEvent event) {
				}

				public void changing(LocationEvent event) {
					Browser brow = (Browser)event.widget;
					String location = event.location;
					
					if(!location.startsWith("code")) {
						event.doit = true;
						return;
					}
					
					event.doit = false;
					
					MessageBox messageBox = null;
					try {
						String[] pair = location.split(":");
						
						String className = (String)marker.getAttribute(pair[1]);
						IType aType = CriticPlugin.getIType(className);
						int lineNumber = (Integer)marker.getAttribute(pair[2]);

						if(aType != null && lineNumber > 0) {
							IEditorPart editorPart = JavaUI.openInEditor(aType);
							if(lineNumber != 0) {
								int[] range = EditorUtility.getIJavaElementFor(aType, lineNumber);
								if (editorPart instanceof ITextEditor) {
									EditorUtility.selectInEditor((ITextEditor)editorPart, range[0], range[1]);
								}
							}
							return;
						}
						
						messageBox = new MessageBox(brow.getShell(), SWT.ICON_ERROR | SWT.OK);
						messageBox.setText("Error Opening Code");
						messageBox.setMessage("Null Java Element Found");
						messageBox.open();
					}
					catch(Exception e) {
						CriticPlugin.log(Status.ERROR, e.getMessage(), e);
						
						messageBox = new MessageBox(brow.getShell(), SWT.ICON_ERROR | SWT.OK);
						messageBox.setText("Error Opening Code");
						messageBox.setMessage(e.toString());
						messageBox.open();
					}
				}
			});
		} catch (SWTError e) {
			e.printStackTrace();
			MessageBox messageBox = new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setMessage("Closing application. The Browser could not be initialized.");
			messageBox.setText("Fatal error - browser cannot be initialized");
			messageBox.open();
			return;
		}
		
		// Add forward and backward listener
		forwardAction = new Action() {
			public void run() {
				browser.forward();
			}
		};
		forwardAction.setText("Forward");;
		forwardAction.setToolTipText("Browses forward page.");
		forwardAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
		
		backAction = new Action() {
			public void run() {
				browser.back();
			}
		};
		backAction.setText("Back");
		backAction.setToolTipText("Browses back page.");
		backAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
		
		// Contribute to the action bars
		IActionBars bars = getViewSite().getActionBars();
		IMenuManager menuManager = bars.getMenuManager();		
		menuManager.add(backAction);
		menuManager.add(forwardAction);
		menuManager.add(new Separator());
		
		IToolBarManager toolBarmanager = bars.getToolBarManager();
		toolBarmanager.add(backAction);
		toolBarmanager.add(forwardAction);
		toolBarmanager.add(new Separator());
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
	}
}
