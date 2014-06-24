/*
 * InvokeStatic.java
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

import java.lang.reflect.Method;
import java.util.List;

import edu.clarkson.serl.critic.extension.ExtensionManager;
import edu.clarkson.serl.critic.extension.IApi;
import edu.clarkson.serl.critic.extension.IResult;
import edu.clarkson.serl.critic.factory.DefaultSymbolFactory;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.reporter.Reporter;
import soot.PrimType;
import soot.Type;
import soot.Value;
import soot.jimple.StaticInvokeExpr;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class InvokeStatic extends Invoke<StaticInvokeExpr> {

	/**
	 * @param sootValue
	 */
	public InvokeStatic(StaticInvokeExpr sootValue) {
		super(sootValue);
	}

	public ISymbol<? extends Value> execute() {
		String type = this.sootValue.getMethod().getDeclaringClass().getName();

		ExtensionManager manager = ExtensionManager.instance();
		Class<?> clazz = manager.getClassFor(type);
		
		if(clazz != null) {
			IResult result = null;
			try {
				String name = this.sootValue.getMethod().getName();
				Method method = clazz.getMethod(name, List.class);
				result = (IResult)method.invoke(null, this.getArguments());
			}
			catch(Exception e) {
				// Something went wrong. We assume that there is no proper support for this static call
			}
			
			if(result != null) {
				this.result = result.getValue();
				Reporter.instance().report(result);
				return result.getValue();
			}
		}
		
		// We do not have support for this static call
		ISymbol<? extends Value> res = DefaultSymbolFactory.getOpenMutableSymbol(this.sootValue);
		this.result = res;
		return res;
		
	}
	
//	public ISymbol<? extends Value> execute() {
//		Type type = this.sootValue.getMethod().getDeclaringClass().getType();
//		ISymbol<? extends Value> symbol = ExtensionManager.instance().getSymbolicObject(AbstractValue.fromObject(type), true, true);
//		if(!(symbol instanceof IApi)) {
//			this.result = this;
//			return this;
//		}
//		
//		IApi<? extends Value> api = (IApi<? extends Value>)symbol;
//		IResult result = api.execute(this.sootValue, this.getArguments());
//		this.result = result.getValue();
//		Reporter.instance().report(result);
//		return result.getValue();
//	}
}
