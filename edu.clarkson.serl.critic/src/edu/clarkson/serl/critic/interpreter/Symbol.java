/*
 * Symbol.java
 * Jul 18, 2011
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
 
package edu.clarkson.serl.critic.interpreter;

import java.util.Collections;
import java.util.Set;

import soot.Value;

/**
 * 
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class Symbol<T extends Value> extends AbstractSymbol<T> {
	private boolean open;
	private boolean mutable;
	/**
	 * @param sootValue
	 */
	public Symbol(T sootValue, boolean open, boolean mutable) {
		super(sootValue);
		this.open = open;
		this.mutable = mutable;
		
		if(mutable && Interpreter.instance().peekStack().isEmpty()) {
			throw new UnsupportedOperationException("A mutable symbolic object cannot not be declared static!");
		}
	}

	public boolean isOpen() {
		return this.open;
	}

	public boolean isMutable() {
		return this.mutable;
	}

	public Object getValue() {
		return this;
	}

	public ISymbol<? extends Value> execute() {
		return this;
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
