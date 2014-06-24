/*
 * StmtTableSwitch.java
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

import java.util.ArrayList;
import java.util.Collections;

import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Path;
import edu.clarkson.serl.critic.interpreter.internal.StackFrame;
import soot.Local;
import soot.Value;
import soot.jimple.Constant;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class StmtTableSwitch extends StmtAbstract<TableSwitchStmt> {

	/**
	 * @param stmt
	 */
	public StmtTableSwitch(TableSwitchStmt stmt) {
		super(stmt);
	}

	@Override
	protected int executeStmt(StackFrame stackFrame) {
		this.result = Path.FEASIBLE;
		Value value = this.stmt.getKey();
		if(!(value instanceof Local || value instanceof Constant))
			throw new UnsupportedOperationException("Key to a lookup switch statement must be a local variable or a constant.");
		ISymbol<? extends Value> key = stackFrame.lookup(value);
		if(!key.isOpen()) {
			int intKey = (Integer)key.getValue();
			int low = this.stmt.getLowIndex();
			int high = this.stmt.getHighIndex();
			int count = 0;
			ArrayList<Stmt> succs = new ArrayList<Stmt>(1);
			for(int i = low; i <= high; ++i) {
				if(intKey == i) {
					succs.add((Stmt)this.stmt.getTarget(count++));
					break;
				}
			}
			// None of the target is taken, so take default target
			if(succs.isEmpty())
				succs.add((Stmt)this.stmt.getDefaultTarget());
			this.result = Path.FEASIBLE;
			this.children = Collections.unmodifiableList(succs);
			return this.result;
		}
		this.result = Path.UNKNOWN;
		this.children = stackFrame.getUnExceptionalSuccOf(this.stmt);
		return this.result;
	}
}
