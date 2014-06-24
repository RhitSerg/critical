/*
 * StmtIdentity.java
 * Jul 11, 2011
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

import java.util.List;

import edu.clarkson.serl.critic.extension.ExtensionManager;
import edu.clarkson.serl.critic.interpreter.Context;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.Path;
import edu.clarkson.serl.critic.interpreter.internal.StackFrame;
import soot.Local;
import soot.Value;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.IdentityStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;

/**
 * This statement represents {@link IdentityStmt} in Jimple.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class StmtIdentity extends StmtAbstract<IdentityStmt> {
	/**
	 * @param stmt
	 */
	public StmtIdentity(IdentityStmt stmt) {
		super(stmt);
	}

	@Override
	protected int executeStmt(StackFrame stackFrame) {
		Value leftOp = this.stmt.getLeftOp();
		Value rightOp = this.stmt.getRightOp();
		
		if(!(leftOp instanceof Local))
			throw new UnsupportedOperationException("The left operand of of an identity statement must be a local.");

		// For a non-entry method
		if(stackFrame.getContext().getType() != Context.ENTRY) {
			List<ISymbol<? extends Value>> arguments = stackFrame.getArguments();
			if(rightOp instanceof ThisRef) {
				ISymbol<? extends Value> receiver = stackFrame.getReceiver(); 

				stackFrame.put(leftOp, receiver);
				stackFrame.put(rightOp, receiver);
				stackFrame.setThisRef((ThisRef)rightOp);
			}
			else if(rightOp instanceof ParameterRef) {
				ParameterRef pRef = (ParameterRef)rightOp;
				ISymbol<? extends Value> arg = arguments.get(pRef.getIndex()); 

				stackFrame.put(leftOp, arg);
				stackFrame.put(pRef, arg);
				stackFrame.addParameterRef(pRef);
			}
			else if(rightOp instanceof CaughtExceptionRef) {
				// This is not useful for analysis
				stackFrame.put(leftOp, Interpreter.VOID);
			}
			else {
				throw new UnsupportedOperationException("The right operand of an identity statement must be a this, parameter, or exception ref.");
			}
		}
		else { // For an entry method
			ExtensionManager extManager = ExtensionManager.instance();
			if(rightOp instanceof ThisRef || rightOp instanceof ParameterRef) {
				// We do not know the actual object so we will create a symbolic object,
				// save it on the heap, and link the variable in the stack to the symbol in the heap
				ISymbol<? extends Value> symbol = extManager.getSymbolicObject(rightOp, true, true);
				symbol = symbol.execute(); // Execute the symbol first
				
				stackFrame.put(leftOp, symbol);
				stackFrame.put(rightOp, symbol);
				
				if(rightOp instanceof ThisRef)
					stackFrame.setThisRef((ThisRef)rightOp);
				else
					stackFrame.addParameterRef((ParameterRef)rightOp);
			}
			else if(rightOp instanceof CaughtExceptionRef) {
				// This is not useful for analysis
				stackFrame.put(leftOp, Interpreter.VOID);
			}
			else {
				throw new UnsupportedOperationException("The right operand of an identity statement must be a this, parameter, or exception ref.");
			}
		}
		
		this.children = stackFrame.getUnExceptionalSuccOf(this.stmt);
		this.result = Path.FEASIBLE;
		return this.result;
	}
}
