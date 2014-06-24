/*
 * AbstractValue.java
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

import java.util.List;

import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.UnitPrinter;
import soot.Value;
import soot.jimple.Ref;
import soot.util.Switch;

/**
 * Represents an immutable abstract value as a Jimple {@link Value} that can wrap any object.
 * This class is typically intended to be used in a {@link SymbolicKey} object.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class AbstractValue implements Value {
	private static final long serialVersionUID = 829485906420787058L;
	
	public static AbstractValue fromObject(Object value) {
		return new AbstractValue(value);
	}
	
	private Object value;
	public AbstractValue(Object value) {
		this.value = value;
	}
	
	public Object getValue() {
		return value;
	}

	public void apply(Switch sw) {
		throw new UnsupportedOperationException("apply() operation on abstract value is not supported");
	}

	public boolean equivTo(Object o) {
		throw new UnsupportedOperationException("equivTo() operation on abstract value is not supported");
	}

	public int equivHashCode() {
		throw new UnsupportedOperationException("equivHashCode() operation on abstract value is not supported");
	}

	public List getUseBoxes() {
		throw new UnsupportedOperationException("getUseBoxes() operation on abstract value is not supported");
	}

	public Type getType() {
		if(value instanceof Type) {
			return (Type)value;
		}
		else if(value instanceof SootField) {
			return ((SootField)value).getType();
		}
		else if(value instanceof SootMethod) {
			return ((SootMethod)value).getReturnType();
		}
		else if(value instanceof SootClass) {
			return ((SootClass)value).getType();
		}
		else if(value instanceof Value) {
			return ((Value)value).getType();
		}
		else if(value instanceof String) {
			return RefType.v((String)value);
		}
		else {
			return Scene.v().getObjectType();
		}
	}

	public void toString(UnitPrinter up) {
		throw new UnsupportedOperationException("toString(UnitPrinter) operation on abstract value is not supported");
	}

	public Object clone() {
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		AbstractValue other = (AbstractValue) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
