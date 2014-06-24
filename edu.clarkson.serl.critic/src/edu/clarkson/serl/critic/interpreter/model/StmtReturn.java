/*
 * StmtReturn.java
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

import java.util.Collections;

import edu.clarkson.serl.critic.extension.ExtensionManager;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.Path;
import edu.clarkson.serl.critic.interpreter.internal.StackFrame;
import soot.Local;
import soot.Value;
import soot.jimple.ReturnStmt;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class StmtReturn extends StmtAbstract<ReturnStmt> {

	/**
	 * @param stmt
	 */
	public StmtReturn(ReturnStmt stmt) {
		super(stmt);
	}

	@Override
	protected int executeStmt(StackFrame stackFrame) {
		Value retValue = this.stmt.getOp();
		ISymbol<? extends Value> result = null; 
		if(retValue instanceof Local) {
			result = stackFrame.lookup(retValue);
		}
		else {
			result = ExtensionManager.instance().getSymbolicObject(retValue, true, true);
			result = result.execute();
		}
		stackFrame.setReturnValue(result);
		this.result = Path.FEASIBLE;
		this.children = Collections.emptyList();
		return this.result;
	}
}
