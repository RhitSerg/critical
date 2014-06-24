/*
 * ActionEventAbstraction.java
 * Nov 15, 2011
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
 
package edu.clarkson.serl.critic.swing.types;

import java.util.List;

import soot.Value;
import soot.jimple.InvokeExpr;
import edu.clarkson.serl.critic.extension.IResult;
import edu.clarkson.serl.critic.extension.Result;
import edu.clarkson.serl.critic.extension.SymbolicApi;
import edu.clarkson.serl.critic.factory.DefaultSymbolFactory;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.SymbolicKey;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class ActionEventAbstraction extends SymbolicApi<Value> {
	public static final String TYPE = "java.awt.event.ActionEvent";
	public static final Value DEFAULT_VALUE = AbstractValue.fromObject(TYPE);
	public static final SymbolicKey SOURCE = SymbolicKey.fromObject("source");
	
	public ActionEventAbstraction() {
		super(DEFAULT_VALUE, false, true);
	}

	/**
	 * Constructs action event object.
	 * 
	 * @param sootValue
	 * @param open
	 * @param mutable
	 */
	public ActionEventAbstraction(Value sootValue, boolean open, boolean mutable) {
		super(sootValue, open, mutable);
	}

	@Override
	public IResult init(List<ISymbol<? extends Value>> arguments) {
		return new Result(Interpreter.VOID);
	}

	@Override
	public IResult execute(InvokeExpr invokeExpr, List<ISymbol<? extends Value>> arguments) {
		String method = invokeExpr.getMethod().getName();
		if(method.equals("getSource")) {
			ISymbol<? extends Value> src = this.get(SOURCE);
			if(src != null)
				return new Result(this.get(SOURCE));
		}
		return new Result(DefaultSymbolFactory.getOpenMutableSymbol(invokeExpr));
	}
}
