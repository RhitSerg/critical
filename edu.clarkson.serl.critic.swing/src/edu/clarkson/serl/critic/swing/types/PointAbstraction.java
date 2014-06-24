/*
 * PointAbstraction.java
 * Aug 16, 2011
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
import edu.clarkson.serl.critic.interpreter.model.ConstInteger;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class PointAbstraction extends SymbolicApi<Value> {
	public static final String TYPE = "java.awt.Point";
	public static final Value DEFAULT_VALUE = AbstractValue.fromObject(TYPE);
	
	public static final SymbolicKey X = SymbolicKey.fromObject("x");
	public static final SymbolicKey Y = SymbolicKey.fromObject("y");
	
	
	public static PointAbstraction getDefaultLocation() {
		PointAbstraction p = new PointAbstraction();
		p.setLocation(ConstInteger.fromInteger(-1), ConstInteger.fromInteger(-1));
		return p;
	}
	
	public PointAbstraction() {
		super(DEFAULT_VALUE, false, true);
	}
	
	public PointAbstraction(Value sootValue) {
		super(sootValue, false, true);
	}

	public PointAbstraction(Value sootValue, boolean open, boolean mutable) {
		super(sootValue, open, mutable);
	}
	
	public boolean isInitialized() {
		return this.getX() != null && this.getY() != null;
	}
	
	public boolean isComputed() {
		ConstInteger negOne = ConstInteger.fromInteger(-1);
		return negOne.equals(this.getX()) && negOne.equals(this.getY());
	}
	
	public boolean isUserDefined() {
		return this.isInitialized() && !this.isComputed();
	}
	
	
	@Override
	public IResult init(List<ISymbol<? extends Value>> arguments) {
		if(arguments.isEmpty()) {
			this.put(X, ConstInteger.fromInteger(0));
			this.put(Y, ConstInteger.fromInteger(0));
		}
		else if(arguments.size() == 1) {
			PointAbstraction p = (PointAbstraction)arguments.get(0);
			this.put(X, p.getX());
			this.put(Y, p.getY());
		}
		else if(arguments.size() == 2) {
			this.put(X, arguments.get(0));
			this.put(Y, arguments.get(1));
		}
		return new Result(Interpreter.VOID);
	}
	

	public ISymbol<? extends Value> getX() {
		return this.get(X);
	}
	
	public ISymbol<? extends Value> getY() {
		return this.get(Y);
	}
	
	public IResult setLocation(ISymbol<? extends Value> x, ISymbol<? extends Value> y) {
		this.put(X, x);
		this.put(Y, y);
		return new Result(Interpreter.VOID);
	}
	
	@Override
	public IResult execute(InvokeExpr invokeExpr, List<ISymbol<? extends Value>> arguments) {
		String method = invokeExpr.getMethod().getName();
		// Only process constructor with two arguments
		if(method.equals("<init>")) {
			return this.init(arguments);
		}
		
		if(method.equals("getX"))
			return new Result(this.getX());
		
		if(method.equals("getY"))
			return new Result(this.getY());
		
		if(method.equals("setLocation")) {
			if(arguments.size() == 2) {
				return this.setLocation(arguments.get(0), arguments.get(1));
			}
			else {
				PointAbstraction point = (PointAbstraction)arguments.get(0);
				return this.setLocation(point.getX(), point.getY());
			}
		}
		
		// We do not care about others
		return new Result(DefaultSymbolFactory.getOpenMutableSymbol(invokeExpr));
	}
}
