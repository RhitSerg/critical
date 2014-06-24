/*
 * BoxLayoutAbstraction.java
 * Sep 13, 2011
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

import edu.clarkson.serl.critic.adt.ClosedList;
import edu.clarkson.serl.critic.extension.Critic;
import edu.clarkson.serl.critic.extension.ICritic;
import edu.clarkson.serl.critic.extension.IResult;
import edu.clarkson.serl.critic.extension.Result;
import edu.clarkson.serl.critic.factory.DefaultSymbolFactory;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import soot.Value;
import soot.jimple.InvokeExpr;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class BoxLayoutAbstraction extends LayoutManagerAbstraction {
	public static final String TYPE = "javax.swing.BoxLayout";
	public static final Value DEFAULT_VALUE = AbstractValue.fromObject(TYPE);
	
	public BoxLayoutAbstraction() {
		super(DEFAULT_VALUE);
	}

	public BoxLayoutAbstraction(Value sootValue) {
		super(sootValue);
	}

	public BoxLayoutAbstraction(Value sootValue, boolean open, boolean mutable) {
		super(sootValue, open, mutable);
	}

	@Override
	public IResult init(List<ISymbol<? extends Value>> arguments) {
		return new Result(Interpreter.VOID);
	}

	@Override
	public IResult execute(InvokeExpr invokeExpr, List<ISymbol<? extends Value>> arguments) {
		return new Result(DefaultSymbolFactory.getOpenMutableSymbol(invokeExpr));
	}

	@Override
	public IResult addLayoutComponent(List<ISymbol<? extends Value>> arguments) {
		return new Result(Interpreter.VOID);
	}

	@Override
	public IResult layoutContainer(JComponentAbstraction target) {
		IResult result = new Result(Interpreter.VOID);
		@SuppressWarnings("unchecked")
		ClosedList<Value> list = (ClosedList<Value>)target.get(JComponentAbstraction.COMPONENTS);
		
		if(list == null)
			return result;
		
		for(ISymbol<? extends Value> s : list) {
			JComponentAbstraction comp = (JComponentAbstraction)s;
			DimensionAbstraction preferredSize = (DimensionAbstraction)comp.getPreferredSize().getValue();
			DimensionAbstraction size = null;
			PointAbstraction location = null;
			if(!preferredSize.isInitialized()) {
				size = new DimensionAbstraction(); // Unknown size
				location = new PointAbstraction(); // Unknown location
				
				this.addNullLayoutRelatedCritic(result, comp);
			}
			else  {
				size = DimensionAbstraction.getDefaultSize();
				location = PointAbstraction.getDefaultLocation();
			}
			comp.put(JComponentAbstraction.SIZE, size);
			comp.put(JComponentAbstraction.LOCATION, location);
		}
		return result;
	}	
}
