/*
 * Array.java
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
import edu.clarkson.serl.critic.factory.DefaultSymbolFactory;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.SymbolicKey;
import edu.clarkson.serl.critic.interpreter.Symbol;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class Array<T extends Value> extends Symbol<T> {
	/**
	 * @param sootValue
	 * @param open
	 */
	public Array(T sootValue, boolean open, ISymbol<? extends Value> length) {
		super(sootValue, open, true);
		super.put(SymbolicKey.LENGTH, length);
	}
	
	public ISymbol<? extends Value> length() {
		return super.get(SymbolicKey.LENGTH);
	}

	@Override
	public ISymbol<? extends Value> get(ISymbol<? extends Value> attribute) {
		ISymbol<? extends Value> value = super.get(attribute);
		if(value != null)
			return value;
		
		// Value is null
		if(!this.isOpen()) {
			if(!attribute.isOpen()) {
				int length = (Integer)this.length().getValue();
				int index = (Integer)attribute.getValue();
				if(index >= length) 
					return Interpreter.ARRAY_INDEX_OUT_OF_BOUNDS;
			}
			value = DefaultSymbolFactory.getDefault(this.getType());
		}
		else {
			// At this point we know that the array is open
			// Create a symbolic object for that index
			value = DefaultSymbolFactory.getSymbol(this.getType().toString(), AbstractValue.fromObject(this.getType()), true, true);
		}
		value = value.execute();
		this.put(attribute, value);
		return value;
	}

	@Override
	public ISymbol<? extends Value> put(ISymbol<? extends Value> attribute, ISymbol<? extends Value> symbol) {
		if(!this.isOpen() && !attribute.isOpen()) {
			int length = (Integer)this.length().getValue();
			int index = (Integer)attribute.getValue();
			if(index >= length) 
				return Interpreter.ARRAY_INDEX_OUT_OF_BOUNDS;
		}
		return super.put(attribute, symbol);
	}
}
