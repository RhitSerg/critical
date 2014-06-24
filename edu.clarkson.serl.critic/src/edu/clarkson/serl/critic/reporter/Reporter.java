/*
 * Reporter.java
 * Jul 26, 2011
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

import java.util.Map;
import java.util.SortedSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.TextSelection;

import soot.SootClass;
import soot.SootMethod;

import edu.clarkson.serl.critic.CriticPlugin;
import edu.clarkson.serl.critic.extension.ICritic;
import edu.clarkson.serl.critic.extension.IResult;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.util.EvaluationLog;
import edu.clarkson.serl.critic.util.Util;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class Reporter {
	private static Reporter instance = null;
	private Reporter() {}
	
	public static Reporter instance() {
		if(instance == null)
			instance = new Reporter();
		return instance;
	}
	
	public static void reset() {
		instance = null;
	}
	
	public boolean report(IResult result) {
		SortedSet<ICritic> critics = result.getCritics();
		if(critics.isEmpty())
			return false;
		
		Interpreter interpreter = Interpreter.instance();
		for(ICritic c : critics) {
			// Add to the evaluation log
			EvaluationLog.add(c);
			
			try {
				IType type = this.getCurrentType(interpreter.getCurrentMethod());
				IResource file = (IResource)type.getAdapter(IResource.class);
				IMarker marker = CriticMarkerFactory.createMarker(file, c);
				if(marker == null)
					continue;
	            marker.setAttribute(ICritic.JAVA_TYPE, type.getFullyQualifiedName());
	            
	            for(Map.Entry<String, Object> e : c.getAttributeMap().entrySet()) {
	            	marker.setAttribute(e.getKey(), e.getValue());
	            }
			}
			catch(Exception e) {
				e.printStackTrace();
				CriticPlugin.log(Status.ERROR, e.getMessage(), e);
			}
			
//			System.out.println("----------------------------------------");
//			System.out.println("Line Number: " + interpreter.getLineNumber());
//			System.out.println("Current Class: " + interpreter.getCurrentClass());
//			System.out.println("Current Method: " + interpreter.getCurrentMethod());
//			System.out.println("Invoked Method: " + interpreter.getInvokedMethod());
//			System.out.println("----------------------------------------");
//			System.out.println(c);
//			System.out.println("========================================\n");
		}
		return true;
	}
	
	public IType getCurrentType(SootMethod currentMethod) throws Exception {
		SootClass clazz = currentMethod.getDeclaringClass();
		String name = Util.getOuterMostClass(clazz);
		
		IJavaProject project = CriticPlugin.getIJavaProject();
		IType iType = project.findType(name);
		return iType;
	}
}
