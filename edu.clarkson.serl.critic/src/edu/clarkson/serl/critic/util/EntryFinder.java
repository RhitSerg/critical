/*
 * EntryFinder.java
 * Sep 15, 2011
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
 
package edu.clarkson.serl.critic.util;

import java.util.HashSet;
import java.util.Set;

import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.toolkits.graph.UnitGraph;

/**
 * This class provides some basic utility for finding entry methods based on some
 * patterns supplied by user.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class EntryFinder {
	public static Set<Type> getUsedTypes(SootMethod method) {
		UnitGraph graph = Util.getUnitGraph(method);
		Set<Local> locals = Util.getLocalsSet(graph);
		Set<Type> types = new HashSet<Type>();
		for(Local l : locals) {
			types.add(l.getType());
		}
		return types;
	}
	
	public static Set<Type> getInitializedTypes(SootMethod method) {
		Set<Type> types = new HashSet<Type>();
		JimpleBody jimpleBody = (JimpleBody)method.retrieveActiveBody();
		for(Unit u : jimpleBody.getUnits()) {
			if(u instanceof AssignStmt) {
				Value rightOp = ((AssignStmt)u).getRightOp();
				if(rightOp instanceof NewExpr) {
					NewExpr newExpr = (NewExpr)rightOp;
					RefType type = newExpr.getBaseType();
					types.add(type);
				}
			}
		}
		return types;
	}

	public static boolean isUsedType(String qualifiedName, SootMethod method) {
		Type t = Scene.v().getRefType(qualifiedName);
		if(EntryFinder.getUsedTypes(method).contains(t))
			return true;
		return false;
	}
	
	public static boolean isIntializedType(String qualifiedName, SootMethod method) {
		Type t = Scene.v().getRefType(qualifiedName);
		JimpleBody jimpleBody = (JimpleBody)method.retrieveActiveBody();
		for(Unit u : jimpleBody.getUnits()) {
			if(u instanceof AssignStmt) {
				Value rightOp = ((AssignStmt)u).getRightOp();
				if(rightOp instanceof NewExpr) {
					NewExpr newExpr = (NewExpr)rightOp;
					RefType type = newExpr.getBaseType();
					if(type.equals(t))
						return true;
				}
			}
		}
		return false;
	}

	public static boolean isUsedTypeOrSuperType(String qualifiedName, SootMethod method) {
		RefType parentType = Scene.v().getRefType(qualifiedName);
		SootClass parentClass = parentType.getSootClass();
		
		for(Type usedType : EntryFinder.getUsedTypes(method)) {
			if(usedType instanceof RefType) {
				SootClass usedClass = ((RefType)usedType).getSootClass();
				
				if(usedClass.equals(parentClass))
					return true;
				
				while(usedClass.hasSuperclass()) {
					usedClass = usedClass.getSuperclass();
					if(usedClass.equals(parentClass))
						return true;
				}
			}
		}
		return false;
	}

	public static boolean isIntializedTypeOrSuperType(String qualifiedName, SootMethod method) {
		RefType parentType = Scene.v().getRefType(qualifiedName);
		SootClass parentClass = parentType.getSootClass();
		
		JimpleBody jimpleBody = (JimpleBody)method.retrieveActiveBody();
		for(Unit u : jimpleBody.getUnits()) {
			if(u instanceof AssignStmt) {
				Value rightOp = ((AssignStmt)u).getRightOp();
				if(rightOp instanceof NewExpr) {
					NewExpr newExpr = (NewExpr)rightOp;
					RefType initializedType = newExpr.getBaseType();
					SootClass initializedClass = ((RefType)initializedType).getSootClass();
					
					if(initializedClass.equals(parentClass))
						return true;
					
					while(initializedClass.hasSuperclass()) {
						initializedClass = initializedClass.getSuperclass();
						if(initializedClass.equals(parentClass))
							return true;
					}
				}
			}
		}
		return false;
	}
}
