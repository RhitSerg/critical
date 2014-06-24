/*
 * RefStaticField.java
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

import edu.clarkson.serl.critic.factory.DefaultSymbolFactory;
import edu.clarkson.serl.critic.interpreter.IStack;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.internal.Stack;
import soot.SootField;
import soot.Value;
import soot.jimple.StaticFieldRef;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class RefStaticField extends ExprAbstract<StaticFieldRef> {

	/**
	 * @param sootValue
	 */
	public RefStaticField(StaticFieldRef sootValue) {
		super(sootValue);
	}

	public ISymbol<? extends Value> execute() {
		ISymbol<? extends Value> result = null;
		SootField field = sootValue.getField();
		Stack stack = (Stack)Interpreter.instance().peekStack();
		result = stack.lookup(field);
		if(result == null) {
			DefaultSymbolFactory symbolFactory = new DefaultSymbolFactory();
			result = symbolFactory.newSymbol(field.getType().toString(), sootValue, true, true);
			stack.put(field, result);
		}
		this.result = result;
		return result;
	}
}
