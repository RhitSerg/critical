/**
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
package edu.clarkson.serl.critic;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.thoughtworks.xstream.XStream;

import edu.clarkson.serl.critic.extension.ExtensionManager;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.internal.StackFrame;
import edu.clarkson.serl.critic.jobs.internal.CriticMainJob;
import edu.clarkson.serl.critic.jobs.internal.CriticWorkspaceJob;
import edu.clarkson.serl.critic.jobs.internal.FileWriterJob;
import edu.clarkson.serl.critic.loader.SootClassLoader;
import edu.clarkson.serl.critic.preferences.Pair;
import edu.clarkson.serl.critic.preferences.Preferences;
import edu.clarkson.serl.critic.reporter.Reporter;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class CriticPlugin extends AbstractUIPlugin {
	// Preferences file
	private static final String PREFERENCE_FILE= ".edu.clarkson.serl.critic.preferences.xml";//$NON-NLS-1$
	
	// The plug-in ID
	public static final String PLUGIN_ID = "edu.clarkson.serl.critical"; //$NON-NLS-1$
	// Useful IDs
	public static final String JOB_ID = "edu.clarkson.serl.critic.jobs"; //$NON-NLS-1$
	public static final String EXTENSION_ID = "edu.clarkson.serl.critical.factory"; //$NON-NLS-1$

	// The shared instance
	private static CriticPlugin plugin;
	private static boolean stop = false;
	
	// Shared Image KEYS
	public static final String IMG_CRITICAL_MAIN = "CriticAL";
	public static final String IMG_CRITICAL_SMALL = "CriticALSmall";
	public static final String IMG_CRITICISM = "Criticsm";
	public static final String IMG_EXPLANATION = "Explanation";
	public static final String IMG_RECOMMENDATION = "Recommendation";
	
	/**
	 * The constructor
	 */
	public CriticPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		stop = false;
		
//		System.out.println("CriticPlugin Loader: " + CriticPlugin.class.getClassLoader());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		stop = true;
		stopAnalysis();
		plugin = null;
		super.stop(context);
	}

	
	public static Shell getShell() {
		return getActiveWorkbenchWindow().getShell();
	}
	
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}	
	
	/**
	 * Always good to have this static method as when dealing with IResources
	 * having a interface to get the editor is very handy
	 * @return
	 */
	public static ITextEditor getEditor() {
		return (ITextEditor)getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CriticPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		super.initializeImageRegistry(registry);
		Bundle bundle = Platform.getBundle(CriticPlugin.PLUGIN_ID);

		// Add the main image to registry
		ImageDescriptor myImage = ImageDescriptor.createFromURL(
				FileLocator.find(bundle,new Path("icons/CriticAL.gif"), null));
		registry.put(CriticPlugin.IMG_CRITICAL_MAIN, myImage);
		
		// Add the small image to registry
		myImage = ImageDescriptor.createFromURL(FileLocator.find(bundle,
				new Path("icons/CriticALSmall.gif"), null));
		registry.put(CriticPlugin.IMG_CRITICAL_SMALL, myImage);
		
		// Add the Criticism image to registry
		myImage = ImageDescriptor.createFromURL(FileLocator.find(bundle,
				new Path("icons/Criticism.gif"), null));
		registry.put(CriticPlugin.IMG_CRITICISM, myImage);

		// Add the Explanation image to registry
		myImage = ImageDescriptor.createFromURL(FileLocator.find(bundle,
				new Path("icons/Explanation.gif"), null));
		registry.put(CriticPlugin.IMG_EXPLANATION, myImage);

		// Add the Recommendation image to registry
		myImage = ImageDescriptor.createFromURL(FileLocator.find(bundle,
				new Path("icons/Recommendation.gif"), null));
		registry.put(CriticPlugin.IMG_RECOMMENDATION, myImage);
	}	
	
	/**
	 * Logs the message in the Error log view.
	 * 
	 * @param status The status code from {@link IStatus}.
	 * Can be one of the following:
	 * <ul> 
	 * <li>{@link IStatus#INFO}</li>
	 * <li>{@link IStatus#CANCEL}</li>
	 * <li>{@link IStatus#WARNING}</li>
	 * <li>{@link IStatus#ERROR}</li>
	 * <li>{@link IStatus#OK}</li>
	 * </ul>
	 * @param message The message to be logged.
	 * @param t The exception that caused this message.
	 */
	public static void log(int status, String message, Throwable t) {
		ILog aLog = plugin.getLog();
		IStatus aStatus = new Status(status, PLUGIN_ID, message, t);
 		aLog.log(aStatus);
	}
	
	/**
	 * Logs the information (not error or warnings) in the log view.
	 * 
	 * @param message The message to be logged.
	 */
	public static void log(String message) {
		ILog aLog = plugin.getLog();
		IStatus aStatus = new Status(Status.WARNING, PLUGIN_ID, message);
 		aLog.log(aStatus);
	}

	static {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot wsRoot = ws.getRoot();
		workspaceRoot = wsRoot;
		javaModel = JavaCore.create(wsRoot);
	}
	
	private static IWorkspaceRoot workspaceRoot;
	private static IJavaModel javaModel;
	private static IJavaProject javaProject;
	private static IFolder srcFolder;
	private static IFolder criticFolder;
	private static HashMap<String, IFolder> idToFolderMap;

	/**
	 * Constructs a new super type hierarchy for the supplied fully qualified name.
	 * 
	 * @param name The fully qualified name of a type.
	 * @return {@link ITypeHierarchy} representing the super type hierarchy of the supplied type.
	 */
	public static ITypeHierarchy getSuperTypeHierarchy(String name) {
		try {
			IType type = javaProject.findType(name);
			return type.newSupertypeHierarchy(null);
		}
		catch(JavaModelException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Constructs a new type hierarchy rooted at the supplied type in the context of the project being analyzed.
	 * 
	 * @param name The fully qualified name of a type.
	 * @return {@link ITypeHierarchy} representing the type hierarchy rooted at the supplied type.
	 */
	public static ITypeHierarchy getTypeHierarchy(String name) {
		try {
			IType type = javaProject.findType(name);
			return type.newTypeHierarchy(javaProject, null);
		}
		catch(JavaModelException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets the {@link IType} from the supplied qualified name.
	 * 
	 * @param qualifiedName The qualified name of a class.
	 * @return The {@link IType} of the supplied qualified name.
	 */
	public static IType getIType(String qualifiedName) {
		try {
			return javaProject.findType(qualifiedName);
		}
		catch(JavaModelException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets the current {@link IProject} under analysis.
	 * 
	 * @return
	 */
	public static IProject getIProject() {
		return javaProject.getProject();
	}
	
	/**
	 * Gets the current {@link IJavaProject} under analysis.
	 * @return
	 */
	public static IJavaProject getIJavaProject() {
		return javaProject;
	}

	/**
	 * Gets the workspace root ({@link IResource}). 
	 * @return
	 */
	public static IResource getWorkspaceRoot() {
		return workspaceRoot;
	}

	/**
	 * Gets the Java model ({@link IJavaModel}).
	 * @return
	 */
	public static IJavaModel getJavaModel() {
		return javaModel;
	}
	
	/**
	 * Gets the src folder of the current project under analysis.
	 * 
	 * @return
	 */
	public static IFolder getSrcFolder() {
		return srcFolder;
	}

	/**
	 * Gets the output critic folder for the current project under analysis.
	 * @return
	 */
	public static IFolder getCriticFolder() {
		return criticFolder;
	}
	
	/**
	 * Gets the output folder under critic folder corresponding to the supplied extension.
	 * @param id Id of the extension.
	 * @return
	 */
	public static IFolder getExtensionFolder(String id) {
		return idToFolderMap.get(id);
	}
	
	public synchronized static void reset() {
		SootClassLoader.reset();
		ExtensionManager.reset();
		Interpreter.reset();
		StackFrame.reset();
		Reporter.reset();
	}
	
	/**
	 * Stops the analysis of the CriticAL.
	 */
	public synchronized static void stopAnalysis() {
		// Cancel all job
		CriticMainJob.getJobManager().cancel(CriticPlugin.JOB_ID);
		
		if(!stop) {
			stop = true;
			final Display display = Display.getDefault(); 
			display.syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(display.getActiveShell(), "CriticAL Analysis Framework", "CriticAL has been canceled before completion! Please check error log for details.");
				}
			});
		}
	}

	/**
	 * Starts the analysis of the PSF for the given project.
	 * @param projectName The name of the project.
	 * @throws Exception Any exception during the processing of the project.
	 */
	public static void startAnalysis(String projectName) throws Exception {
		SootClassLoader.reset();

		javaProject = javaModel.getJavaProject(projectName);

		PackageCreatorJob aJob = new PackageCreatorJob();
		aJob.setPriority(Job.SHORT);
		aJob.schedule();
		aJob.join(); // Wait for this job to be done
		
		BuildJob buildJob = new BuildJob();
		buildJob.setPriority(Job.SHORT);
		buildJob.schedule();
		buildJob.join(); // Wait for this job to be done before returning
		
		CriticMainJob mainJob = new CriticMainJob(projectName);
		mainJob.schedule();
	}
	
	/**
	 * Creates necessary packages and folders for the analysis by CriticAL.
	 * 
	 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
	 */
	public static class PackageCreatorJob extends CriticWorkspaceJob {
		public PackageCreatorJob() {
			super("Creating dummy packages for whole-program mode");
		}
		
		public IStatus runInWorkspace(IProgressMonitor monitor) {
			try {
				IProject project = javaProject.getProject();
				String projName = project.getName();
				
				monitor.beginTask("Adding dummy entry class serl.dummy.Main to " + projName, 6);
				srcFolder = project.getFolder("src");
				if(!srcFolder.exists()) {
					srcFolder.create(true, true, null);
				}
				monitor.worked(1);

				IFolder serlFolder = srcFolder.getFolder("serl");
				if(!serlFolder.exists()) {
					serlFolder.create(true, true, null);
				}

				monitor.worked(1);
				IFolder dummyFolder = serlFolder.getFolder("dummy");
				if(!dummyFolder.exists()) {
					dummyFolder.create(true, true, null);
				}
				monitor.worked(1);
				
				IFile mainFile = dummyFolder.getFile("Main.java");
				if(mainFile.exists()) {
					mainFile.delete(true, null);
				}
				String source = "\n" +
				"package serl.dummy;\n\n" +
				"public class Main {\n\n" +
				"\tpublic static void main(String[] args) {\n\n" +
				"\t}\n\n" +
				"}\n\n";
				ByteArrayInputStream stream = new ByteArrayInputStream(source.getBytes());
				mainFile.create(stream, true, null);
				monitor.worked(1);
				

				criticFolder = project.getFolder("critic");
				if(criticFolder.exists()) {
					criticFolder.delete(true, new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
				}
				criticFolder.create(true, true, null);
				monitor.worked(1);
				
				// Create output folder for each extension
				idToFolderMap = new HashMap<String, IFolder>();
				IConfigurationElement[] configElems = Platform.getExtensionRegistry().getConfigurationElementsFor(CriticPlugin.EXTENSION_ID);
				for(IConfigurationElement elem : configElems) {
					IExtension extension = elem.getDeclaringExtension();
					String id = extension.getUniqueIdentifier();
					
					IFolder idFolder = criticFolder.getFolder(id);
					if(idFolder.exists()) {
						idFolder.delete(true, new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
					}
					idFolder.create(true, true, new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
					idToFolderMap.put(id, idFolder);
				}
				idToFolderMap.put(CriticPlugin.PLUGIN_ID, criticFolder);
				monitor.worked(1);

				return Status.OK_STATUS;
			}
			catch(Exception e) {
				return new Status(Status.ERROR, CriticPlugin.PLUGIN_ID, Status.CANCEL, 
						e.getMessage(), new Exception(e));
			}
		}
	}
	
	/**
	 * Refreshes and builds the project before stating analysis.
	 * 
	 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
	 */
	public static class BuildJob extends CriticWorkspaceJob {
		public BuildJob() {
			super("Build project for equals method analysis");
		}
		
		public IStatus runInWorkspace(IProgressMonitor monitor) {
			try {
				IProject project = javaProject.getProject();
				String projName = project.getName();
				
				monitor.beginTask("Building " + projName + " for newly added dummy serl.dummy.Main class", IProgressMonitor.UNKNOWN);
				project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new SubProgressMonitor(monitor, SubProgressMonitor.UNKNOWN));
				
				return Status.OK_STATUS;
			}
			catch(Exception e) {
				return new Status(Status.ERROR, CriticPlugin.PLUGIN_ID, Status.CANCEL, 
						e.getMessage(), new Exception(e));
			}
		}
	}
	
	private static Preferences preferences = null;
	
	/**
	 * Returns the cached preference for the current project.
	 * 
	 * @return
	 */
	public static Preferences getPreferences() {
		if(preferences == null)
			return new Preferences();
		return preferences;
	}

	/**
	 * Reads the preference file from the project.
	 * 
	 * @param project
	 * @return
	 */
	public static Preferences readPreferencesFromStore(IProject project) {
		try {
			IFile prefFile = project.getFile(PREFERENCE_FILE);
			if(!prefFile.exists()) {
				preferences = new Preferences();
				return preferences;
			}

			InputStream inStream = prefFile.getContents();
			XStream xstream = new XStream();
			xstream.setClassLoader(Preferences.class.getClassLoader());

			xstream.processAnnotations(Preferences.class);
			xstream.processAnnotations(Pair.class);
			
			preferences = (Preferences)xstream.fromXML(inStream);
			
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_ID);
			IExtension[] extensions = point.getExtensions();
			for(String key : preferences.getDisabledExtensions()) {
				boolean found = false;;
				for(IExtension extension : extensions) {
					String eid = extension.getUniqueIdentifier();
					if(key.equals(eid)) {
						found = true;
						break;
					}
				}
				
				// Make sure our record is up to date
				if(!found)
					preferences.removeDisabledExtension(key);
			}
			
			return preferences;
		}
		catch(Exception e) {
			CriticPlugin.log(Status.WARNING, "Cannot read stored preferences", e);
			return new Preferences();
		}
	}
	
	/**
	 * Writes the preferences for the given project in the physical storage of the project.
	 * 
	 * @param project {@link IProject} the project whose preference needs to be stored.
	 * @param preferences The {@link Preferences} object related to the given project.
	 */
	public static void writePreferencesToStore(IProject project, Preferences preferences) {
		CriticPlugin.preferences = preferences;
		try {
			IFile prefFile = project.getFile(PREFERENCE_FILE);
			if(prefFile.exists())
				prefFile.delete(true, null);

			XStream xstream = new XStream();
			xstream.setClassLoader(Preferences.class.getClassLoader());
			
			xstream.processAnnotations(Preferences.class);
			xstream.processAnnotations(Pair.class);
			
			String xml = xstream.toXML(preferences);
			ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
			prefFile.create(stream, true, null);
		}
		catch(Exception e) {
			CriticPlugin.log(Status.WARNING, "Cannot read stored preferences", e);
		}
	}
	
	/**
	 * Gets the URL for the document file with the supplied name in the <tt>/doc</tt> directory of the extension plugin. 
	 * 
	 * @param pluginId The extension id of the plugin.
	 * @param fileNameOnly The name of the file in the <tt>/doc</tt> directory. Note: only filename not path. eg. <tt>file.txt</tt>.
	 * @return The complete URL to the supplied file.
	 */
	public static String getDocumentURL(String pluginId, String fileNameOnly) {
		try {
			Bundle bundle = Platform.getBundle(pluginId);
			URL locationUrl = FileLocator.find(bundle, new Path("/"), null);
			URL fileUrl = FileLocator.toFileURL(locationUrl);
			return fileUrl.getFile() + "doc" + File.separatorChar + fileNameOnly;		
		}
		catch(Exception e){}
		return null;
	}
}
