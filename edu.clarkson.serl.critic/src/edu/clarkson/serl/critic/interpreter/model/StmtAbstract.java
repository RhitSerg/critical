/*
 * StmtAbstract.java
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
import java.util.List;

import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.Path;
import edu.clarkson.serl.critic.interpreter.internal.StackFrame;

import soot.jimple.Stmt;

/**
 * This class wraps a jimple {@link Stmt} to provide functionality for symbolic execution.
 * 
 * @param <T> The subtype of the Jimple {@link Stmt}.
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public abstract class StmtAbstract<T extends Stmt> {
	protected T stmt;
	protected int result;
	protected List<Stmt> children;
	
	/**
	 * Constructs a symbolic statement on top of supplied Jimple {@link Stmt}.
	 * @param stmt The backing Jimple statement.
	 */
	public StmtAbstract(T stmt) {
		this.stmt = stmt;
		this.result = Path.UNKNOWN;
		this.children = new ArrayList<Stmt>(2);
	}

	/**
	 * Gets the underlying Jimple {@link Stmt}.
	 * 
	 * @return the stmt
	 */
	public T getStmt() {
		return stmt;
	}
	
	/**
	 * Gets the saved result constant of the symbolic execution of this statement. Note that
	 * {@link #execute()} method must be invoked before calling this method to get correct result.
	 * 
	 * <ul>
	 * <li>{@link Path#FEASIBLE}: If the execution of this statement is feasible.</li>
	 * <li>{@link Path#UNKNOWN}: If the execution of this statement may be feasible or infeasible.</li>
	 * <li>{@link Path#INFEASIBLE}: If the execution of this statement is infeasible.</li>
	 * </ul>
	 * @return the result
	 */
	public int getResult() {
		return result;
	}

	/**
	 * Gets the next possible children for this statement. Note that {@link #execute()} method must
	 * be invoked before calling this method to get correct result.
	 * 
	 * @return the children
	 */
	public List<Stmt> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return "[stmt=" + stmt + "]";
	}
	
	
	/**
	 * Calls {@link #executeStmt(StackFrame)} method with supplied {@link IStackFrame} parameter.
	 * 
	 * @return The constant for the result of the execution of this statement.
	 * @see {@link #executeStmt(StackFrame)}
	 */
	public int execute() {
		Interpreter interpreter = Interpreter.instance();
		StackFrame stackFrame = (StackFrame)interpreter.peek();
		return executeStmt(stackFrame);
	}

	/**
	 * Executes the current statement symbolically and returns one of the following constants:
	 * <ul>
	 * <li>{@link Path#FEASIBLE}: If the execution of this statement is feasible.</li>
	 * <li>{@link Path#UNKNOWN}: If the execution of this statement may be feasible or infeasible.</li>
	 * <li>{@link Path#INFEASIBLE}: If the execution of this statement is infeasible.</li>
	 * </ul>
	 * Note that for a conditional statement, if the condition is evaluated to false, then taking a false branch
	 * still makes the statement feasible. In-feasibility is caused when none of the branch can be taken.
	 * For instance, in <code>if(p == null){p.m();} ...</code>, the execution of <code>p.m()</code> is infeasible.
	 * 
	 * @param stackFrame The {@link StackFrame} object.
	 * @return The constant for the result of the execution of this statement.
	 */
	protected abstract int executeStmt(StackFrame stackFrame);
}
