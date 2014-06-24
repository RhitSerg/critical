/*
 * ExprCondition.java
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

import edu.clarkson.serl.critic.interpreter.ISymbol;
import soot.Value;
import soot.jimple.ConditionExpr;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public abstract class ExprCondition<T extends ConditionExpr> extends ExprBinary<T> {
	/**
	 * @param sootValue
	 */
	public ExprCondition(T sootValue, ISymbol<? extends Value> left, ISymbol<? extends Value> right) {
		super(sootValue, left, right);
	}

	public ISymbol<? extends Value> execute() {
		// If open, then we will just return the tree
		if(this.isOpen()) {
			this.result = this;
			return this;
		}
		
		// Execute both branches first
		ISymbol<? extends Value> lTree = this.getLeft().execute();
		ISymbol<? extends Value> rTree = this.getRight().execute();
		
		Object lValue = lTree.getValue();
		Object rValue = rTree.getValue();
		
		ISymbol<? extends Value> result = this.execute(lValue, rValue);
		this.result = result;
		return result;
	}
	
	/**
	 * Subtypes of {@link ExprCondition} will evaluate the supplied values and
	 * return an {@link ExprAbstract} object that corresponds to the truth value
	 * of the conditional expression. 
	 * 
	 * @param lValue
	 * @param rValue
	 * @return
	 */
	public abstract ISymbol<? extends Value> execute(Object lValue, Object rValue);
}
