/*
 * JButtonAbstraction.java
 * Mar 17, 2011
 *
 * Serl's Software Expert (SE) System
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 * 
 * Contact Us:
 * Chandan Raj Rupakheti (rupakhcr@clarkson.edu)
 * Daqing Hou (dhou@clarkson.edu)
 * Clarkson University
 * PO Box 5722
 * Potsdam
 * NY 13699-5722
 * http://serl.clarkson.edu
*/
 
package edu.clarkson.serl.critic.swing.types;

import java.util.ArrayList;
import java.util.List;

import edu.clarkson.serl.critic.extension.ExtensionManager;
import edu.clarkson.serl.critic.extension.IResult;
import edu.clarkson.serl.critic.extension.Result;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.CallbackPoint;
import edu.clarkson.serl.critic.interpreter.ICallbackPoint;
import edu.clarkson.serl.critic.interpreter.IStack;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.InvokeExpr;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class JButtonAbstraction extends JComponentAbstraction {
	public static final String TYPE = "javax.swing.JButton";
	public static final Value DEFAULT_VALUE = AbstractValue.fromObject(TYPE);
	
	public JButtonAbstraction() {
		super(DEFAULT_VALUE);
	}

	public JButtonAbstraction(Value sootValue) {
		super(sootValue);
	}

	public JButtonAbstraction(Value sootValue, boolean open, boolean mutable) {
		super(sootValue, open, mutable);
	}

	@Override
	public IResult init(List<ISymbol<? extends Value>> arguments) {
		IResult result = super.init(arguments);
		this.put(PREFERRED_SIZE, DimensionAbstraction.getDefaultSize());
		return result;
	}
	
	@Override
	public IResult execute(InvokeExpr invokeExpr, List<ISymbol<? extends Value>> arguments) {
		String method  = invokeExpr.getMethod().getName();
		if(method.equals("addActionListener")) {
			return this.addActionListener(arguments);
		}

		// Do whatever JComponent does with other methods
		return super.execute(invokeExpr, arguments);
	}

	public IResult addActionListener(List<ISymbol<? extends Value>> arguments) {
		ICallbackPoint callBack = new CallbackPoint(arguments.get(0), this, "java.awt.event.ActionListener") {
			@Override
			public List<ISymbol<? extends Value>> getArguments(SootMethod method) {
				ArrayList<ISymbol<? extends Value>> arguments = new ArrayList<ISymbol<? extends Value>>();
				ActionEventAbstraction event = new ActionEventAbstraction();
				event.put(ActionEventAbstraction.SOURCE, JButtonAbstraction.this);
				arguments.add(event);
				return arguments;
			}
		};
		
		IStack stack = Interpreter.instance().peekStack();
		stack.addCallback(callBack);
		return new Result(Interpreter.VOID);
	}
}
