/*
 * StmtAssign.java
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

import edu.clarkson.serl.critic.exceptions.IThrowable;
import edu.clarkson.serl.critic.exceptions.SymThrowable;
import edu.clarkson.serl.critic.extension.ExtensionManager;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.Path;
import edu.clarkson.serl.critic.interpreter.SymbolicKey;
import edu.clarkson.serl.critic.interpreter.internal.Stack;
import edu.clarkson.serl.critic.interpreter.internal.StackFrame;
import soot.Local;
import soot.SootField;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.InstanceFieldRef;
import soot.jimple.Ref;
import soot.jimple.StaticFieldRef;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class StmtAssign extends StmtAbstract<AssignStmt> {

	/**
	 * @param stmt
	 */
	public StmtAssign(AssignStmt stmt) {
		super(stmt);
	}

	@Override
	protected int executeStmt(StackFrame stackFrame) {
		Value leftOp = this.stmt.getLeftOp();
		Value rightOp = this.stmt.getRightOp();
		
		ISymbol<? extends Value> right = null;
		if(rightOp instanceof Local) {
			right = stackFrame.lookup(rightOp);
		}
		else {
			right = ExtensionManager.instance().getSymbolicObject(rightOp, true, true);
			right = right.execute();
		}
		
		if(right instanceof IThrowable) {
			IThrowable<? extends Value> throwable = (IThrowable<? extends Value>)right;
			if(throwable.isThrowing()) {
				Interpreter.instance().setException(throwable.getException());
				this.children = stackFrame.getExceptionalSuccOf(this.stmt);
				this.result = Path.EXCEPTION;
				return this.result;
			}
		}
		
		if(leftOp instanceof Local) {
			stackFrame.put(leftOp, right);
		}
		else if(leftOp instanceof Ref) {
			this.handleRef((Ref)leftOp, right, stackFrame);
		}
		else {
			throw new UnsupportedOperationException("The left operand of an assignment must be either a local variable or a ref.");
		}
		
		if(exception) {
			this.result = Path.EXCEPTION;
			this.children = stackFrame.getExceptionalSuccOf(this.stmt);
		}
		else {
			this.result = Path.FEASIBLE;
			this.children = stackFrame.getUnExceptionalSuccOf(this.stmt);
		}
		return this.result;
	}
	
	boolean exception = false;
	private void handleRef(Ref leftOp, ISymbol<? extends Value> right, StackFrame stackFrame) {
		if(leftOp instanceof StaticFieldRef) {
			// Static fields are managed by interpreter directly
			SootField field = ((StaticFieldRef)leftOp).getField();
			((Stack)stackFrame.getStack()).put(field, right);
		}
		else if(leftOp instanceof InstanceFieldRef) {
			InstanceFieldRef fieldRef = (InstanceFieldRef)leftOp;
			Value base = fieldRef.getBase();
			ISymbol<? extends Value> leftSymbol = stackFrame.lookup(base);
			if(leftSymbol == null || leftSymbol.getValue() == null) {
				// Tell interpreter about the null pointer exception
				Interpreter.instance().setException(Interpreter.NULL_POINTER);
				exception = true;
			}
			else {
				// Maps the field to the corresponding symbolic object
				SymbolicKey key = SymbolicKey.fromObject(fieldRef.getField());
				leftSymbol.put(key, right);
			}
		}
		else if(leftOp instanceof ArrayRef) {
			ArrayRef arrayRef = (ArrayRef)leftOp;
			Value base = arrayRef.getBase();
			ISymbol<? extends Value> symBase = stackFrame.lookup(base);
			Interpreter interpreter = Interpreter.instance();
			if(symBase == null || symBase.getValue() == null) {
				// Tell interpreter about the null pointer exception
				interpreter.setException(Interpreter.NULL_POINTER);
				exception = true;
			}
			else {
				Value index = arrayRef.getIndex();
				ISymbol<? extends Value> symIndex = null;
				
				if(index instanceof Constant)
					symIndex = ExtensionManager.instance().getSymbolicObject(index, false, false);
				else
					symIndex = interpreter.peek().lookup(index);
				ISymbol<? extends Value> result = symBase.put(symIndex, right);
				if(result instanceof SymThrowable) {
					// Tell interpreter about the exception
					interpreter.setException(result);
					exception = true;
				}
			}
		}
	}
}
