/*
 * ExprInstanceOf.java
 * Jul 4, 2011
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
 
package edu.clarkson.serl.critic.interpreter.model;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;

import edu.clarkson.serl.critic.CriticPlugin;
import edu.clarkson.serl.critic.adt.ClosedSet;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.SymbolicKey;
import soot.Local;
import soot.Type;
import soot.Value;
import soot.jimple.InstanceOfExpr;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class ExprInstanceOf extends ExprAbstract<InstanceOfExpr> {

	/**
	 * @param sootValue
	 */
	public ExprInstanceOf(InstanceOfExpr sootValue) {
		super(sootValue);
	}

	public ISymbol<? extends Value> execute() {
		Value op = this.getSootValue().getOp();
		if(!(op instanceof Local)) // Sanity check
			throw new UnsupportedOperationException("The operand of instanceof expression must be a local variable");
		
		ISymbol<? extends Value> operand = Interpreter.instance().peek().lookup(op);
		Type opType = operand.getType();
		Type checkedType = this.getSootValue().getCheckType();
		
// Note: Incomplete implementation using Soot's fast hierarchy for future consideration.
// The FastHierarchy API does not provide required functionalities that ITypeHierarchy provides.		
//		if(operand.isKnown()) {
//			FastHierarchy fastHierarchy = Scene.v().getOrMakeFastHierarchy();
//			if(fastHierarchy.canStoreType(opType, checkedType)) {
//				this.result = Interpreter.TRUE;
//				return Interpreter.TRUE;
//			}
//			this.result = Interpreter.FALSE;
//			return Interpreter.FALSE;
//		}

		IJavaProject project;
		IType opIType;
		IType checkedIType;
		ITypeHierarchy opHierarchy;
		try{
			project = CriticPlugin.getIJavaProject();
			opIType = project.findType(opType.toString());
			checkedIType = project.findType(checkedType.toString());
			opHierarchy = opIType.newSupertypeHierarchy(null);
			if(opHierarchy.contains(checkedIType)) { // For both open and closed this holds
				this.result = Interpreter.TRUE;
				return Interpreter.TRUE;
			}
			else if(!this.isOpen()) { // For only closed object this holds
				this.result = Interpreter.FALSE;
				return Interpreter.FALSE;
			}
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}

		// At this point we are dealing with open object only and we know that 
		// checkedIType is not a supertype of opIType. So, instead it may hold 
		// that the checkedIType is a subtype of the supplied opType.
		try{
			ITypeHierarchy checkedHierarchy = checkedIType.newSupertypeHierarchy(null);
			
			if(!checkedHierarchy.contains(opIType)) {
				// At this point neither checkedIType is a parent of opIType, nor opIType is a parent of checkedIType.  
				// This mean that opType and checkedType are in two different sub-hierarchies from a common parent
				this.result = Interpreter.FALSE;
				return Interpreter.FALSE;
			}
			
			// At this point opIType is a subtype of checkedIType. This may be true because the real type of op is open.
			// Lets check whether any type related information is available.

			// First the inclusive types
			ClosedSet<? extends Value> types = (ClosedSet<? extends Value>)operand.get(SymbolicKey.TYPES_IN);
			if(types != null && !types.isEmpty()) {
				for(ISymbol<? extends Value> t : types) {
					IType tIType = project.findType(t.getType().toString());
					ITypeHierarchy tHierarchy = tIType.newSupertypeHierarchy(null);
					if(tHierarchy.contains(checkedIType)) { // The checked type is a super type of one of the inclusive type
						this.result = Interpreter.TRUE;
						return Interpreter.TRUE;
					}
				}
			}
			
			// Inclusive types could not resolve the instanceof test
			// Now lets try exclusive types
			types = (ClosedSet<? extends Value>)operand.get(SymbolicKey.TYPES_OUT);
			if(types != null && !types.isEmpty()) {
				for(ISymbol<? extends Value> t : types) {
					IType tIType = project.findType(t.getType().toString());
					if(checkedIType.equals(tIType)) { // instaceof test of the excluded type
						this.result = Interpreter.FALSE;
						return Interpreter.FALSE;
					}
					ITypeHierarchy tHierarchy = tIType.newTypeHierarchy(project, null);
					IType[] subtypes = tHierarchy.getAllSubtypes(tIType);
					for(IType sub : subtypes) {
						if(checkedIType.equals(sub)) { // instaceof test of a subtype of the excluded type
							this.result = Interpreter.FALSE;
							return Interpreter.FALSE;
						}
					}
					
				}
			}
			
			// Neither inclusive nor exclusive set resolved the instanceof test.
			// Hence, result is set to be open.
			this.result = this; 
			return this;
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
