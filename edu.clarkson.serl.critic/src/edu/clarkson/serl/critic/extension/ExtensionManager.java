/*
 * ExtensionManager.java
 * Jul 7, 2011
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
 
package edu.clarkson.serl.critic.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import edu.clarkson.serl.critic.CriticPlugin;
import edu.clarkson.serl.critic.factory.ConstantFactory;
import edu.clarkson.serl.critic.factory.DefaultSymbolFactory;
import edu.clarkson.serl.critic.factory.ExprFactory;
import edu.clarkson.serl.critic.factory.IFactory;
import edu.clarkson.serl.critic.factory.RefFactory;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.Context;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.loader.SootClassLoader;
import edu.clarkson.serl.critic.preferences.Preferences;

import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.Constant;
import soot.jimple.Expr;
import soot.jimple.Ref;
import soot.jimple.Stmt;

/**
 * This class manages all of the registered extensions and callbacks to those extension.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class ExtensionManager {
	private static ExtensionManager instance;
	public static ExtensionManager instance() {
		if(instance == null)
			instance = new ExtensionManager(); 
		return instance;
	}
	
	public static void reset() {
		instance = null;
	}
	
	private IFactory defaultFactory;
	private Set<IFactory> factories;
	private HashMap<ICheckPoint.Interest, HashSet<ICheckPoint>> interestToCheckPointMap;
	private ExtensionManager(){
		this.defaultFactory = new DefaultSymbolFactory();
		this.factories = new HashSet<IFactory>();
		this.interestToCheckPointMap = new HashMap<ICheckPoint.Interest, HashSet<ICheckPoint>>();
		
		// Update the added factories from extension point
		this.updateFromExtension();
	}
	
	
	private void updateFromExtension() {
		Preferences preferences = CriticPlugin.getPreferences();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CriticPlugin.EXTENSION_ID);
		IExtension[] extensions = point.getExtensions();
		for(IExtension extension : extensions) {
			String id = extension.getUniqueIdentifier();
			// Do not process disabled extensions
			if(preferences.containsDisabledExtension(id)) {
				continue;
			}
			
			IConfigurationElement[] factories = extension.getConfigurationElements();
			// Every plugin must have exactly one factory class
			if(factories.length != 1) {
				String message = extension.getLabel() + " [" + id + "]" + " does not have exactly one factory.";
				CriticPlugin.log(Status.ERROR, message, new UnsupportedOperationException());
				continue;
			}
			
			// All Good
			try {
				Object o = factories[0].createExecutableExtension("class");
				if(o instanceof IFactory) {
					this.add((IFactory) o);
				}
			}
			catch(Exception e) {
				String message = extension.getLabel() + " [" + id + "]" + " error initializing factory.";
				CriticPlugin.log(Status.ERROR, message, new UnsupportedOperationException());
			}
		}
	}
	
	public boolean add(IFactory factory) {
		if(!this.factories.add(factory))
			return false;
		
		for(ICheckPoint p : factory.getCheckPoints()) {
			this.addCheckPoint(p);
		}
		return true;
	}
	
	private void addCheckPoint(ICheckPoint checkPoint) {
		HashSet<ICheckPoint> checkPoints = this.interestToCheckPointMap.get(checkPoint.getInterest());
		if(checkPoints == null) {
			checkPoints = new HashSet<ICheckPoint>();
			this.interestToCheckPointMap.put(checkPoint.getInterest(), checkPoints);
		}
		checkPoints.add(checkPoint);
	}
	
	public Set<ICheckPoint> getCheckPoints(ICheckPoint.Interest interest) {
		HashSet<ICheckPoint> checkPoints = this.interestToCheckPointMap.get(interest);
		if(checkPoints == null) {
			checkPoints = new HashSet<ICheckPoint>();
			this.interestToCheckPointMap.put(interest, checkPoints);
		}
		return checkPoints;
	}

	public List<Context> getEntryMethods() {
		ArrayList<Context> list = new ArrayList<Context>();
		HashSet<SootMethod> methods = new HashSet<SootMethod>();
		for(IFactory factory : this.factories) {
			methods.addAll(factory.getEntryMethods());
		}
		
		for(SootMethod m : methods) {
			list.add(new Context(null, m, m.getDeclaringClass()));
		}
		return list;
	}
	
	public void checkEntryMethod(SootMethod method, IProgressMonitor monitor) {
		for(IFactory factory : this.factories) {
			if(monitor.isCanceled())
				return;
			factory.checkEntry(method);
		}
	}
	
	public boolean isSupportedType(String type) {
		for(IFactory factory : this.factories) {
			if(factory.getSupportedTypes().contains(type))
				return true;
		}
		return false;
	}
	
	public Class<?> getClassFor(String type) {
		for(IFactory factory : this.factories) {
			Class<?> clazz = factory.getClassFor(type);
			if(clazz != null)
				return clazz;
		}
		return null;
	}
	
	public ISymbol<? extends Value> getSymbolicObject(Value value, boolean open, boolean mutable) {
		String type = value.getType().toString();
		if(value instanceof Constant) {
			ConstantFactory factory = new ConstantFactory();
			value.apply(factory);
			return factory.newSymbol(type, value, open, mutable);
		}
		else if(value instanceof Expr) {
			ExprFactory factory = new ExprFactory();
			value.apply(factory);
			return factory.newSymbol(type, value, open, mutable);
		}
		else if(value instanceof Ref) {
			RefFactory factory = new RefFactory();
			value.apply(factory);
			return factory.newSymbol(type, value, open, mutable);
		}
		else if(value instanceof AbstractValue) {
			return this.getSymbol(value, open, mutable);
		}
		else {
			throw new UnsupportedOperationException("Unknown value encountered");
		}
	}
	
	private ISymbol<? extends Value> getSymbol(Value value, boolean open, boolean mutable) {
		if(value.getType() instanceof RefType) {
			RefType refType = (RefType)value.getType();
			SootClass clazz = refType.getSootClass();
			while(true){
				// Swing factory will be plugged in here
				for(IFactory f : this.factories) {
					if(f.getSupportedTypes().contains(clazz.getName())) {
						return f.newSymbol(clazz.getName(), value, open, mutable);
					}
				}
				
				// If the current class is not supported then check if a super class is supported
				if(clazz.hasSuperclass()) {
					clazz = clazz.getSuperclass();
				}
				else
					break;
			}
		}
		return this.defaultFactory.newSymbol(value.getType().toString(), value, open, mutable);
	}
	
	public boolean shouldInline(SootMethod resolvedMethod, ISymbol<? extends Value> receiver, List<ISymbol<? extends Value>> arguments, Stmt callSite) {
		// Note for a static method: receiver will be null.

		// If the receiver class is an application class, then inline the method
		if(!SootClassLoader.instance().contains(resolvedMethod.getDeclaringClass()))
			return false;
		
		// Check if we are still within the given
		Preferences preferences = CriticPlugin.getPreferences();
		int methodDepth = preferences.getMethodDepth();
		if(Interpreter.instance().peekStack().size() >= methodDepth)
			return false;
		
		// If one of the factory says inline, then inline the resolved method
		for(IFactory factory : this.factories) {
			if(factory.shouldInline(resolvedMethod, receiver, arguments, callSite))
				return true;
		}
		return false;
	}
}
