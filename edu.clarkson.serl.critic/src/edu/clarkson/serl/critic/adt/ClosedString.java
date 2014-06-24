/*
 * ClosedString.java
 * Jul 19, 2011
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
 
package edu.clarkson.serl.critic.adt;

import soot.Value;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.SymbolicKey;
import edu.clarkson.serl.critic.interpreter.Symbol;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class ClosedString extends Symbol<Value> {
	public static SymbolicKey LENGTH = SymbolicKey.fromObject("length"); 
	public static ClosedString EMPTY = new ClosedString("");
	
	public ClosedString(String value) {
		super(AbstractValue.fromObject(value), false, false);
	}

	public ISymbol<? extends Value> length() {
		return this.get(LENGTH);
	}
	
	@Override
	public Object getValue() {
		return ((AbstractValue)this.sootValue).getValue();
	}

	@Override
	public int hashCode() {
		return this.sootValue.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this)
			return true;
		if(o == null)
			return false;
		if(o.getClass() != this.getClass())
			return false;
		ClosedString that = (ClosedString)o;
		return this.sootValue.equals(that.sootValue); // sooValue is always not null
	}
}
