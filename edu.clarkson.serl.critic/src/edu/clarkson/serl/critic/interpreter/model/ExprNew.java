/*
 * ExprNew.java
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

import edu.clarkson.serl.critic.extension.ExtensionManager;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import soot.RefType;
import soot.Value;
import soot.jimple.NewExpr;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class ExprNew extends ExprAbstract<NewExpr> {

	/**
	 * @param sootValue
	 */
	public ExprNew(NewExpr sootValue) {
		super(sootValue);
	}

	/**
	 * This method will delegate the creation of symbolic object to {@link ExtensionManager#getSymbolicObject(Value)}.
	 * where objects from contributed plugins will be search for the corresponding symbolic object.
	 * If it did not find one, then the default implementation of symbolic object will be returned 
	 * without the domain specific semantics.
	 */
	public ISymbol<? extends Value> execute() {
		RefType baseType = this.getSootValue().getBaseType();
		ISymbol<? extends Value> result = ExtensionManager.instance().getSymbolicObject(AbstractValue.fromObject(baseType), false, true);
		result = result.execute();
		this.result = result;
		return result;
	}
}
