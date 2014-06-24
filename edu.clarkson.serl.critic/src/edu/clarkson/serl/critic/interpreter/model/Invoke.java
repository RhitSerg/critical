/*
 * Invoke.java
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.clarkson.serl.critic.exceptions.IThrowable;
import edu.clarkson.serl.critic.extension.ExtensionManager;
import edu.clarkson.serl.critic.interpreter.IStackFrame;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import soot.Local;
import soot.Value;
import soot.jimple.InvokeExpr;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public abstract class Invoke<T extends InvokeExpr> extends ExprAbstract<T> implements IThrowable<T> {

	/**
	 * @param sootValue
	 */
	public Invoke(T sootValue) {
		super(sootValue);
	}
	
	/**
	 * Returns an unmodifiable {@link List} of arguments supplied to this invoke expression.
	 *  
	 * @param sootValue {@link InvokeExpr} from which a list of arguments is supplied.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<ISymbol<? extends Value>> getArguments(InvokeExpr sootValue) {
		List<Value> args = sootValue.getArgs();
		Interpreter interpreter = Interpreter.instance();
		IStackFrame stackFrame = interpreter.peek();
		ExtensionManager manager = ExtensionManager.instance();
		ArrayList<ISymbol<? extends Value>> symArgs = new ArrayList<ISymbol<? extends Value>>(args.size());
		for(Value arg : args) {
			ISymbol<? extends Value> symArg = null;
			if(arg instanceof Local)
				symArg = stackFrame.lookup(arg);
			else
				symArg = manager.getSymbolicObject(arg, true, true);
			
			symArgs.add(symArg);
		}
		return symArgs;
	}
	
	/**
	 * Returns an unmodifiable {@link List} of arguments supplied to this invoke expression. 
	 * @return
	 */
	public List<ISymbol<? extends Value>> getArguments() {
		return Collections.unmodifiableList(getArguments(this.sootValue));
	}
	
	public boolean isThrowing() {
		return this != this.result && this.result instanceof IThrowable;
	}

	@SuppressWarnings("unchecked")
	public ISymbol<? extends Value> getException() {
		if(this.result instanceof IThrowable)
			return (ISymbol<? extends Value>)this.result;
		return null;
	}
}
