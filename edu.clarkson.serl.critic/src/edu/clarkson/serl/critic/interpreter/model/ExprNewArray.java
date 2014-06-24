/*
 * ExprNewArray.java
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

import edu.clarkson.serl.critic.adt.Array;
import edu.clarkson.serl.critic.extension.ExtensionManager;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import soot.Type;
import soot.Value;
import soot.jimple.NewArrayExpr;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class ExprNewArray extends ExprAbstract<NewArrayExpr> {

	/**
	 * @param sootValue
	 */
	public ExprNewArray(NewArrayExpr sootValue) {
		super(sootValue);
	}

	public ISymbol<? extends Value> execute() {
		Type type = this.sootValue.getBaseType();
		AbstractValue value = AbstractValue.fromObject(type);
		Value size = this.sootValue.getSize();
		
		ISymbol<? extends Value> length = ExtensionManager.instance().getSymbolicObject(size, true, true);
		length = length.execute();
		
		ISymbol<? extends Value> result = new Array<AbstractValue>(value, length.isOpen(), length);
		result = result.execute();
		
		this.result = result;
		return result;
	}
}
