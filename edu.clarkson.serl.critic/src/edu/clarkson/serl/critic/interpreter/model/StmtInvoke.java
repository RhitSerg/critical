/*
 * StmtInvoke.java
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
import edu.clarkson.serl.critic.extension.ExtensionManager;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.Path;
import edu.clarkson.serl.critic.interpreter.internal.StackFrame;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class StmtInvoke extends StmtAbstract<InvokeStmt> {

	/**
	 * @param stmt
	 */
	public StmtInvoke(InvokeStmt stmt) {
		super(stmt);
	}

	@Override
	protected int executeStmt(StackFrame stackFrame) {
		InvokeExpr invokeExpr = (InvokeExpr)this.stmt.getInvokeExpr();
		ISymbol<? extends Value> result = ExtensionManager.instance().getSymbolicObject(invokeExpr, true, true);
		result = result.execute();
		Interpreter interpreter = Interpreter.instance();
		
		// Check for an exception
		if(result instanceof IThrowable) {
			IThrowable<? extends Value> throwable = (IThrowable<? extends Value>)result;
			if(throwable.isThrowing()) {
				interpreter.setException(throwable.getException());
				this.children = stackFrame.getExceptionalSuccOf(this.stmt);
				this.result = Path.EXCEPTION;
				return this.result;
			}
		}
		this.children = stackFrame.getUnExceptionalSuccOf(this.stmt);
		this.result = Path.FEASIBLE;
		return this.result;
	}
}
