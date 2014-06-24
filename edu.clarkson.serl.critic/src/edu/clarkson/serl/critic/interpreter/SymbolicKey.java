/*
 * SymbolicKey.java
 * Jul 15, 2011
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
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.model.ExprAbstract;

/**
 * Represents an immutable symbolic representation of a Key. It is an immutable object inherited
 * from {@link ExprAbstract}. See the documentation of the super class for detials.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class SymbolicKey extends ExprAbstract<AbstractValue> {
	public static final SymbolicKey LENGTH = SymbolicKey.fromObject("length"); 
	public static final SymbolicKey REF = SymbolicKey.fromObject("reference");
	public static final SymbolicKey VALUE = SymbolicKey.fromObject("value");
	public static final SymbolicKey TYPES_IN = SymbolicKey.fromObject("inclusive-types");
	public static final SymbolicKey TYPES_OUT = SymbolicKey.fromObject("exclusive-types");
	public static final SymbolicKey SIZE = SymbolicKey.fromObject("size");
	public static final SymbolicKey INDICES = SymbolicKey.fromObject("indices");
	
	
	public static SymbolicKey fromObject(Object key) {
		return new SymbolicKey(AbstractValue.fromObject(key));
	}
	
	/**
	 * @param sootValue
	 */
	public SymbolicKey(AbstractValue sootValue) {
		super(sootValue);
	}

	public boolean isOpen() {
		return false;
	}
	
	public boolean isMutable() {
		return false;
	}

	public Object getValue() {
		return this.getSootValue().getValue();
	}
	
	public ISymbol<? extends Value> execute() {
		return this;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.getSootValue() == null) ? 0 : this.getSootValue().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SymbolicKey other = (SymbolicKey) obj;
		if (this.getSootValue() == null) {
			if (other.getSootValue() != null)
				return false;
		} else if (!this.getSootValue().equals(other.getSootValue()))
			return false;
		return true;
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
