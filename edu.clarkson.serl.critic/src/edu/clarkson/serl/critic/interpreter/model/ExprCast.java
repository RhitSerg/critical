/*
 * ExprCast.java
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
import edu.clarkson.serl.critic.exceptions.IThrowable;
import edu.clarkson.serl.critic.extension.ExtensionManager;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.SymbolicKey;
import edu.clarkson.serl.critic.interpreter.internal.StackFrame;
import soot.Local;
import soot.Type;
import soot.Value;
import soot.jimple.CastExpr;
import soot.jimple.Constant;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class ExprCast extends ExprAbstract<CastExpr> implements IThrowable<CastExpr> {
	private ISymbol<? extends Value> exception;
	/**
	 * @param sootValue
	 */
	public ExprCast(CastExpr sootValue) {
		super(sootValue);
		exception = null;
	}

	/**
	 * Adds the type information in the casted object.
	 * Also checks if casting throws an exception, in which case
	 * exception will be reported and the path will be marked infeasible.
	 */
	public ISymbol<? extends Value> execute() {
		Value op = this.getSootValue().getOp();
		StackFrame stackFrame = (StackFrame)Interpreter.instance().peek();
		ISymbol<? extends Value> result = null; 
		Type castType = this.getSootValue().getCastType();
		if(op instanceof Local) {
			result = stackFrame.lookup(op);
			if(!(result instanceof Constant)) {
				if(!result.isOpen()) {
					try{
						IJavaProject project = CriticPlugin.getIJavaProject();
						IType opIType = project.findType(result.getType().toString());
						IType castIType = project.findType(castType.toString());
						ITypeHierarchy typeHierarchy = opIType.newSupertypeHierarchy(null);
						if(!typeHierarchy.contains(castIType)) {
							this.exception = Interpreter.CLASS_CAST;
						}
					}
					catch(Exception e) {
					}
				}
				else if(result.isMutable()) {
					ClosedSet<? extends Value> inclusiveTypes = (ClosedSet<? extends Value>)result.get(SymbolicKey.TYPES_IN);
					if(inclusiveTypes == null) {
						inclusiveTypes = new ClosedSet<Value>(AbstractValue.fromObject("edu.clarkson.serl.critic.adt.ClosedSet"));
						result.put(SymbolicKey.TYPES_IN, inclusiveTypes);
					}
					inclusiveTypes.add(SymbolicKey.fromObject(castType));
				}
			}
		}
		else if(op instanceof Constant) {
			// Do nothing related to constant
			result = ExtensionManager.instance().getSymbolicObject(op, false, false);
			result = result.execute();
		}
		else {
			throw new UnsupportedOperationException("Operand of a cast expression must be a local or a constant");
		}
		
		this.result = result;
		return result;
	}

	public boolean isThrowing() {
		return this.exception != null;
	}

	public ISymbol<? extends Value> getException() {
		return this.exception;
	}
}
