/*
 * DimensionAbstraction.java
 * Mar 13, 2011
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
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class DimensionAbstraction extends SymbolicApi<Value> {
	public static final String TYPE = "java.awt.Dimension";
	public static final Value DEFAULT_VALUE = AbstractValue.fromObject("java.awt.Dimension");
	
	public static final SymbolicKey WIDTH = SymbolicKey.fromObject("width");
	public static final SymbolicKey HEIGHT = SymbolicKey.fromObject("height");
	
	public static DimensionAbstraction getDefaultSize() {
		DimensionAbstraction d = new DimensionAbstraction();
		d.setSize(ConstInteger.fromInteger(-1), ConstInteger.fromInteger(-1));
		return d;
	}
	
	public DimensionAbstraction() {
		super(DEFAULT_VALUE, false, true);
	}
	
	public DimensionAbstraction(Value sootValue) {
		super(sootValue, false, true);
	}
	
	public DimensionAbstraction(Value sootValue, boolean open, boolean mutable) {
		super(sootValue, open, mutable);
	}
	
	
	public boolean isInitialized() {
		return this.getHeight() != null && this.getWidth() != null;
	}
	
	public boolean isComputed() {
		ConstInteger negOne = ConstInteger.fromInteger(-1);
		return negOne.equals(this.getHeight()) && negOne.equals(this.getWidth());
	}
	
	public boolean isUserDefined() {
		boolean inited = this.isInitialized();
		boolean computed = this.isComputed();
		return inited && !computed;
	}
	
	@Override
	public IResult init(List<ISymbol<? extends Value>> arguments) {
		if(arguments.isEmpty()) {
			this.setSize(ConstInteger.fromInteger(0), ConstInteger.fromInteger(0));
		}
		else if(arguments.size() == 1) {
			DimensionAbstraction d = (DimensionAbstraction)arguments.get(0);
			this.setSize(d.getWidth(), d.getHeight());
		}
		else if(arguments.size() == 2) {
			this.setSize(arguments.get(0), arguments.get(1));
		}
		return new Result(Interpreter.VOID);
	}
	

	public ISymbol<? extends Value> getWidth() {
		return this.get(WIDTH);
	}
	
	public ISymbol<? extends Value> getHeight() {
		return this.get(HEIGHT);
	}
	
	
	public void setSize(ISymbol<? extends Value> width, ISymbol<? extends Value> height) {
		this.put(WIDTH, width);
		this.put(HEIGHT, height);
		
	}

	@Override
	public IResult execute(InvokeExpr invokeExpr, List<ISymbol<? extends Value>> arguments) {
		String method = invokeExpr.getMethod().getName();
		// Only process constructor with two arguments
		if(method.equals("<init>")) {
			return this.init(arguments);
		}
		
		if(method.equals("getWidth"))
			return new Result(this.getWidth());
		
		if(method.equals("getHeight"))
			return new Result(this.getHeight());
		
		if(method.equals("setSize")) {
			if(arguments.size() == 1)
				this.setSize(arguments.get(0).get(WIDTH), arguments.get(0).get(HEIGHT));
			else
				this.setSize(arguments.get(0), arguments.get(1));
			return new Result(Interpreter.VOID);
		}
		
		// We do not care about others
		return new Result(DefaultSymbolFactory.getOpenMutableSymbol(invokeExpr));
	}
}
