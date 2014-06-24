/*
 * RefInstanceField.java
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
 
package edu.clarkson.serl.critic.interpreter.model;

import edu.clarkson.serl.critic.exceptions.IThrowable;
import edu.clarkson.serl.critic.exceptions.SymThrowable;
import edu.clarkson.serl.critic.factory.DefaultSymbolFactory;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.SymbolicKey;
import soot.SootField;
import soot.Value;
import soot.jimple.InstanceFieldRef;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class RefInstanceField extends ExprAbstract<InstanceFieldRef> implements IThrowable<InstanceFieldRef> {

	/**
	 * @param sootValue
	 */
	public RefInstanceField(InstanceFieldRef sootValue) {
		super(sootValue);
	}

	public ISymbol<? extends Value> execute() {
		Value base = this.sootValue.getBase();
		
		Interpreter interpreter = Interpreter.instance();
		ISymbol<? extends Value> symBase = interpreter.peek().lookup(base);
		ISymbol<? extends Value> result = null;
		if(symBase == null || symBase.getValue() == null) {
			result = Interpreter.NULL_POINTER;
		}
		else  {
			SootField field = this.sootValue.getField();
			ISymbol<? extends Value> symField = SymbolicKey.fromObject(field);
			result = symBase.get(symField);
			if(result == null) {
				result = DefaultSymbolFactory.getSymbol(field.getType().toString(), AbstractValue.fromObject(field), true, true);
			}
		}
		this.result =  result;
		return result;
	}

	public boolean isThrowing() {
		return this.result instanceof SymThrowable;
	}

	@SuppressWarnings("unchecked")
	public ISymbol<? extends Value> getException() {
		if(this.result instanceof SymThrowable)
			return (ISymbol<? extends Value>)this.result;
		return null;
	}
}
