/*
 * SootClassLoader.java
 * Jun 30, 2011
 *
 * CriticAL : A Critic for APIs and Libraries
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
 * Chandan Raj Rupakheti (rupakhcr@clarkson.edu)
 * Daqing Hou (dhou@clarkson.edu)
 * Clarkson University
 * PO Box 5722
 * Potsdam
 * NY 13699-5722
 * http://critical.sourceforge.net
 */
 
package edu.clarkson.serl.critic.loader;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;

import edu.clarkson.serl.critic.CriticPlugin;
import edu.clarkson.serl.critic.extension.ExtensionManager;
import edu.clarkson.serl.critic.interpreter.Interpreter;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;

/**
 * This class uses JDT API to traverse through all of the project types 
 * including the inner types and loads them in Soot. It is designed as a
 * singleton class.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class SootClassLoader {
	private final static String DUMMY_MAIN_CLASS = "serl.dummy.Main";
	private final static String DUMMY_ENTRY_METHOD = "main";

	private static SootClassLoader instance;

	/**
	 * Gets the instance of class loader.
	 * @return
	 */
	public static synchronized SootClassLoader instance() {
		if(instance == null)
			instance = new SootClassLoader();
		return instance;
	}

	/**
	 * Resets the instance to null, releasing all of the occupied resources.
	 */
	public static synchronized void reset() {
		instance = null;
	}
	
	private Map<String, SootClass> nameToClassMap;
	
	private SootClassLoader() {
		nameToClassMap = Collections.synchronizedMap(new HashMap<String, SootClass>());
		configureSoot();
	}
	
	private synchronized void configureSoot() {
		String classPath = SootClasspath.urlsToString(SootClasspath.projectClassPath2(CriticPlugin.getIJavaProject()));
		
		// Set up all the options for soot
		soot.G.reset();
		Options options = soot.options.Options.v();
		
		options.set_soot_classpath(classPath);
		options.set_prepend_classpath(true);
		options.allow_phantom_refs();

		options.set_keep_line_number(true);
		options.set_whole_program(true);
		options.setPhaseOption("jb","use-original-names:true");
		options.setPhaseOption("cg","verbose:false");

		// Let soot parse user option and set it
		options.parse(CriticPlugin.getPreferences().getSootOptionArray());
		
		// Load dummy main class. This is requirement for all the project being analyzed
		SootClass mainClass = this.load(DUMMY_MAIN_CLASS, true);
		List<SootMethod> entryPointList = new ArrayList<SootMethod>();
		entryPointList.add(mainClass.getMethodByName(DUMMY_ENTRY_METHOD));
		soot.Scene.v().setEntryPoints(entryPointList);
	}
	
	/**
	 * Loads a class to the soot.
	 * 
	 * @param name The qualified name of the class to be loaded in Soot.
	 * @return {@link SootClass} after loading.
	 */
	public synchronized SootClass load(String name) {
		SootClass sootClass = load(name, false);
		if(sootClass != null) {
			this.nameToClassMap.put(name, sootClass);
		}
		return sootClass;
	}
	
	// Load class here returns null so we might expect a null pointer exception somewhere
	private synchronized SootClass load(String name, boolean main) {
		
		try {
			SootClass c = Scene.v().loadClassAndSupport(name);
			c.setApplicationClass();
			if (main) 
				Scene.v().setMainClass(c);
			return c;
		}
		catch(Exception e) {
			CriticPlugin.log(Status.ERROR, "Unable to load " + name + " in Soot.", e);
			return null;
		}
	}
	
	/**
	 * Returns the class loaded in Soot corresponding to the supplied name.
	 * Null is returned if the class with the given name is not present.
	 * 
	 * @param name
	 * @return
	 */
	public SootClass getSootClass(String name) {
		return this.nameToClassMap.get(name);
	}
	
	/**
	 * Gets all of the classes loaded in soot.
	 * Note modifying this collection will change the internal structure of the backing map.
	 * 
	 * @return
	 */
	public Collection<SootClass> getAllSootClasses() {
		return this.nameToClassMap.values();
	}
	
	/**
	 * Checks if the supplied name of the class is an application class that was loaded in soot.
	 * 
	 * @param className The name of the class to be checked.
	 * @return <tt>true</tt> if loaded as an application class and <tt>false</tt> otherwise.
	 */
	public boolean contains(String className) {
		return this.nameToClassMap.containsKey(className);
	}
	
	
	/**
	 * Checks if the supplied class is an application class that was loaded in soot.
	 * 
	 * @param clazz The class to be checked.
	 * @return <tt>true</tt> if loaded as an application class and <tt>false</tt> otherwise.
	 */
	public boolean contains(SootClass clazz) {
		return this.nameToClassMap.containsKey(clazz.getName());
	}
	
	/**
	 * The method traverses all of the project types in depth-first order 
	 * including inner and anonymous types and loads them in Soot. 
	 * 
	 * 
	 * @param monitor The progress monitor.
	 * @throws Exception Propagated from JDT APIs.
	 */
	public void process(IProgressMonitor subMonitor) throws Exception {
		IJavaProject project = CriticPlugin.getIJavaProject();
		IPackageFragmentRoot[] packageFragmentRoots = project.getPackageFragmentRoots();
		subMonitor.beginTask("Loading " + project.getElementName() + " ...", 2);
		
		SubProgressMonitor monitor = new SubProgressMonitor(subMonitor, 1);
		monitor.beginTask("Loading packages ... ", packageFragmentRoots.length + 1);

		for(IPackageFragmentRoot pkgFragRoot : packageFragmentRoots) {
			if(pkgFragRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
				IJavaElement[] pkgFrags = (IJavaElement[])pkgFragRoot.getChildren();
				for(IJavaElement pkgFrag : pkgFrags) {
					if(monitor.isCanceled())
						return;

					monitor.subTask("Loading classes in " + pkgFrag.getElementName());
					
					if(pkgFrag.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
						IPackageFragment pkgFragment = (IPackageFragment)pkgFrag;
						IJavaElement[] children = pkgFragment.getChildren();
						for(IJavaElement anElement : children) {
							if(monitor.isCanceled())
								return;
							
							// Make sure its a java file
							if(anElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
								this.dfsDomTree(anElement, monitor);
							}
						}
					}
				}
			}
			monitor.worked(1);
		}
		
		// Load the necessary classes after all of the classes have been loaded.
		Scene.v().loadNecessaryClasses();
		monitor.done();
		
		// Create an instance of Interpreter before we process anything further
		Interpreter.instance();
		
		monitor = new SubProgressMonitor(subMonitor, 1);
		monitor.beginTask("Configuring entry points ... ", this.getAllSootClasses().size());
		
		for(SootClass c : this.getAllSootClasses()) {
			ExtensionManager manager = ExtensionManager.instance();
			// Configure entry methods for extension plugin
			for(SootMethod method : c.getMethods()) {
				if(monitor.isCanceled())
					return;
				
				manager.checkEntryMethod(method, monitor);
				monitor.worked(1);
			}
		}
		monitor.done();
	}

	protected void dfsDomTree(IJavaElement element, IProgressMonitor monitor) throws Exception {
		if(monitor.isCanceled()) {
			return;
		}

		if(element.isReadOnly())
			return;

		int elementType = element.getElementType();
		if(elementType == IJavaElement.COMPILATION_UNIT) {
			ICompilationUnit cu = (ICompilationUnit)element;
			IType[] allTypes = cu.getTypes();
			for(IType aType : allTypes) {
				dfsDomTree(aType, monitor);
			}
		}
		else if(elementType == IJavaElement.TYPE) {
			IType aType = (IType)element;

			if(aType.isClass()){
				// Load a type in Soot
				load(aType.getFullyQualifiedName());
			}

			// Go inside the methods to look for Anonymous Inner Class
			for(IMethod m : aType.getMethods()) {
				IJavaElement[] elems = m.getChildren();
				for(IJavaElement elem : elems) {
					if(elem.getElementType() == IJavaElement.TYPE) {
						dfsDomTree(elem, monitor);
					}
				}
			}
			
			// For inner classes
			IType[] allTypes = aType.getTypes();
			for(IType childType : allTypes) {
				dfsDomTree(childType, monitor);
			}
		}
		
	}
	
	/**
	 * This is a debug method to check all of the classes loaded in Soot through this loader.
	 * @param oStream
	 */
	public void printLoadedClasses(OutputStream oStream) {
		PrintStream out = new PrintStream(oStream);
		out.println("#################################################");
		out.println("Loaded Classes in Soot:");
		out.println("#################################################");
		for(SootClass aClass: this.nameToClassMap.values()) {
			String superClassString = "NONE";
			
			if(aClass.hasSuperclass()) {
				superClassString = aClass.getSuperclass().getName();
			}
			
			out.println(aClass.getName() + " extends " + superClassString);
		}
		out.println("#################################################");
		out.println();
		out.flush();
	}
}