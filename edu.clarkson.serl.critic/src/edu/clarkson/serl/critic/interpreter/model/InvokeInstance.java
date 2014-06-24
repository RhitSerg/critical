/*
 * InvokeInstance.java
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

import edu.clarkson.serl.critic.extension.IApi;
import edu.clarkson.serl.critic.extension.IResult;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.reporter.Reporter;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class InvokeInstance<T extends InstanceInvokeExpr> extends Invoke<T> {

	/**
	 * @param sootValue
	 */
	public InvokeInstance(T sootValue) {
		super(sootValue);
	}

	public ISymbol<? extends Value> execute() {
		ISymbol<? extends Value> symBase = this.getBase();
		if(!(symBase instanceof IApi)) { // Not an API object
			this.result = this;
			return this;
		}

		// Is an API object, or at least subtype of an API object
		IApi<? extends Value> api = (IApi<? extends Value>)symBase;
		IResult result = api.execute(this.sootValue, this.getArguments());
		Reporter.instance().report(result);
		this.result = result.getValue();
		return result.getValue();
	}

	/**
	 * Returns the base symbol on which {@link InstanceInvokeExpr} has been invoked.
	 * 
	 * @param sootValue The supplied {@link InstanceInvokeExpr}.
	 * @return
	 */
	public static ISymbol<? extends Value> getBase(InstanceInvokeExpr sootValue) {
		return Interpreter.instance().lookup(sootValue.getBase());
	}

	/**
	 * Returns the base symbol on which {@link InstanceInvokeExpr} has been invoked.
	 * @return
	 */
	public ISymbol<? extends Value> getBase() {
		return Interpreter.instance().lookup(sootValue.getBase());
	}
}
