/*
 * RectangleAbstraction.java
 * Aug 22, 2011
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

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class RectangleAbstraction extends SymbolicApi<Value> {
	public static final String TYPE = "java.awt.Rectangle";
	public static final Value DEFAULT_VALUE = AbstractValue.fromObject(TYPE);
	
	public RectangleAbstraction() {
		super(DEFAULT_VALUE, false, true);
	}
	
	public RectangleAbstraction(Value sootValue) {
		super(sootValue, false, true);
	}

	public RectangleAbstraction(Value sootValue, boolean open, boolean mutable) {
		super(sootValue, open, mutable);
	}
	
	@Override
	public IResult init(List<ISymbol<? extends Value>> arguments) {
		if(arguments.size() == 4)
			return this.setBounds(arguments.get(0), arguments.get(1), arguments.get(2), arguments.get(3));
		return new Result(Interpreter.VOID);
	}

	@Override
	public IResult execute(InvokeExpr invokeExpr, List<ISymbol<? extends Value>> arguments) {
		String method = invokeExpr.getMethod().getName();
		if(method.equals("setBounds") && arguments.size() == 4)
			return this.setBounds(arguments.get(0), arguments.get(1), arguments.get(2), arguments.get(3));
		if(method.equals("getSize"))
			return new Result(this.getSize());
		if(method.equals("getLocation"))
			return new Result(this.getLocation());
		if(method.equals("getBounds"))
			return new Result(this.getBounds());
		
		return new Result(DefaultSymbolFactory.getOpenMutableSymbol(invokeExpr));
	}

	public IResult setBounds(ISymbol<? extends Value> x, ISymbol<? extends Value> y, 
			ISymbol<? extends Value> width, ISymbol<? extends Value> height) {
		this.put(PointAbstraction.X, x);
		this.put(PointAbstraction.Y, y);
		this.put(DimensionAbstraction.WIDTH, width);
		this.put(DimensionAbstraction.HEIGHT, height);
		
		return new Result(Interpreter.VOID);
	}
	
	public PointAbstraction getLocation() {
		PointAbstraction point = new PointAbstraction();
		point.put(PointAbstraction.X, this.get(PointAbstraction.X));
		point.put(PointAbstraction.Y, this.get(PointAbstraction.Y));
		return point;
	}
	
	public DimensionAbstraction getSize() {
		DimensionAbstraction size = new DimensionAbstraction();
		size.put(DimensionAbstraction.WIDTH, this.get(DimensionAbstraction.WIDTH));
		size.put(DimensionAbstraction.HEIGHT, this.get(DimensionAbstraction.HEIGHT));
		return size;
	}
	
	public RectangleAbstraction getBounds() {
		RectangleAbstraction abstraction = new RectangleAbstraction();
		abstraction.put(PointAbstraction.X, this.get(PointAbstraction.X));
		abstraction.put(PointAbstraction.Y, this.get(PointAbstraction.Y));
		abstraction.put(DimensionAbstraction.WIDTH, this.get(DimensionAbstraction.WIDTH));
		abstraction.put(DimensionAbstraction.HEIGHT, this.get(DimensionAbstraction.HEIGHT));
		return abstraction;
	}
}
