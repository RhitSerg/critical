/*
 * FlowLayoutAbstraction.java
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

import java.awt.FlowLayout;
import java.util.List;
import java.util.Set;

import soot.Value;
import soot.jimple.InvokeExpr;
import edu.clarkson.serl.critic.CriticPlugin;
import edu.clarkson.serl.critic.adt.ClosedList;
import edu.clarkson.serl.critic.extension.Critic;
import edu.clarkson.serl.critic.extension.ICritic;
import edu.clarkson.serl.critic.extension.IResult;
import edu.clarkson.serl.critic.extension.Result;
import edu.clarkson.serl.critic.factory.DefaultSymbolFactory;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.SymbolicKey;
import edu.clarkson.serl.critic.interpreter.model.ConstInteger;
import edu.clarkson.serl.critic.swing.SwingCriticPlugin;
import edu.clarkson.serl.critic.swing.poi.ICheckable;

/**
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 *
 */
public class DefaultLayoutManager extends LayoutManagerAbstraction implements ICheckable<Value> {
	public static final String TYPE = "edu.clarkson.serl.critic.DefaultLayout";

	public static DefaultLayoutManager newDefaultLayout() {
		DefaultLayoutManager layout = new DefaultLayoutManager(AbstractValue.fromObject(TYPE));
		layout.setDefault(true);
		return layout;
	}

	public DefaultLayoutManager(Value sootValue) {
		super(sootValue);
	}

	public DefaultLayoutManager(Value sootValue, boolean open, boolean mutable) {
		super(sootValue, open, mutable);
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

	@Override
	public IResult checkAtEnd() {
		return new Result(Interpreter.VOID);
	}
}
