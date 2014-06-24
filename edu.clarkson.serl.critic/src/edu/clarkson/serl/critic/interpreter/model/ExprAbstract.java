/*
 * ExprAbstract.java
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.AbstractSymbol;
import soot.Value;

/**
 * Represents a Jimple expression, more precisely all of the Jimple {@link Value} object.
 * This object is immutable, and hence, do not have a proxy i.e. the {@link #isProxy()}
 * method returns false, the {@link #clone()} method returns pointer to itself instead
 * of creating a new object, the {@link #makeItReal()} method does nothing, and the
 * {@link #put(ISymbol, ISymbol)} method will throw an {@link UnsupportedOperationException}.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public abstract class ExprAbstract<T extends Value> extends AbstractSymbol<T> {
	/**
	 * Subtypes must assign a value to this field in order to make the 
	 * {@link #getValue()} method work.
	 */
	protected Object result;

	/**
	 * @param sootValue
	 */
	public ExprAbstract(T sootValue) {
		super(sootValue);
		this.result = null;
		this.propertyToSymbolMap = new HashMap<ISymbol<? extends Value>, ISymbol<? extends Value>>(0);
	}
	
	public Object getValue() {
		return result;
	}

	public boolean isOpen() {
		return true;
	}
	
	public boolean isMutable() {
		return false;
	}

	@Override
	public ISymbol<T> clone() {
		return this;
	}

	@Override
	public ISymbol<? extends Value> put(ISymbol<? extends Value> attribute,
			ISymbol<? extends Value> symbol) {
		throw new UnsupportedOperationException("put(ISymbol, ISymbol) operation on expressions are not supported.");
	}
	
	@Override
	protected void cloneInSub() {
		// Nothing to be done
	}

	@Override
	protected void replaceInSub(ISymbol<? extends Value> original, ISymbol<? extends Value> cloned) {
		// Nothing to be done
	}

	@Override
	protected Set<ISymbol<? extends Value>> getAllContainedInSub() {
		return Collections.emptySet();
	}	
}
