/*
 * ExprBinary.java
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
import soot.jimple.BinopExpr;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public abstract class ExprBinary<T extends BinopExpr> extends ExprAbstract<T> {
	protected ISymbol<? extends Value> left;
	protected ISymbol<? extends Value> right;
	
	/**
	 * @param sootValue
	 */
	public ExprBinary(T sootValue, ISymbol<? extends Value> left, ISymbol<? extends Value> right) {
		super(sootValue);
		this.left = left;
		this.right = right;
	}

	/**
	 * @return the left subtree.
	 */
	public ISymbol<? extends Value> getLeft() {
		return this.left;
	}

	/**
	 * @return the right subtree.
	 */
	public ISymbol<? extends Value> getRight() {
		return this.right;
	}

	@Override
	public boolean isOpen() {
		return this.left.isOpen() || this.right.isOpen();
	}
}
