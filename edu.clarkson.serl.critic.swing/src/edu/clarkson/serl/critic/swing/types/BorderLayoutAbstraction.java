/*
 * BorderLayoutAbstraction.java
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

import java.awt.BorderLayout;
import java.util.List;
import java.util.ListIterator;

import edu.clarkson.serl.critic.adt.ClosedList;
import edu.clarkson.serl.critic.adt.ClosedString;
import edu.clarkson.serl.critic.extension.Critic;
import edu.clarkson.serl.critic.extension.ICritic;
import edu.clarkson.serl.critic.extension.IResult;
import edu.clarkson.serl.critic.extension.Result;
import edu.clarkson.serl.critic.factory.DefaultSymbolFactory;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.swing.poi.ICheckable;

import soot.Value;
import soot.jimple.InvokeExpr;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class BorderLayoutAbstraction extends LayoutManagerAbstraction  implements ICheckable<Value> {
	public static final String TYPE = "java.awt.BorderLayout";
	public static final Value DEFAULT_VALUE = AbstractValue.fromObject(TYPE);

	public BorderLayoutAbstraction() {
		super(DEFAULT_VALUE);
	}

	public BorderLayoutAbstraction(Value sootValue) {
		super(sootValue);
	}

	public BorderLayoutAbstraction(Value sootValue, boolean open, boolean mutable) {
		super(sootValue, open, mutable);
	}
	
	@Override
	public IResult init(List<ISymbol<? extends Value>> arguments) {
		return new Result(Interpreter.VOID);
	}
	
	@Override
	public IResult execute(InvokeExpr invokeExpr, List<ISymbol<? extends Value>> arguments) {
		String method = invokeExpr.getMethod().getName();
		// Only handles add and remove
		if(method.equals(LayoutManagerAbstraction.ADD_COMPONENT)) {
			return this.addLayoutComponent(arguments);
		}
		else if(method.equals(LayoutManagerAbstraction.REMOVE_COMPONENT)) {
			return this.removeLayoutComponent(arguments);
		}
		
		// We do not care about others
		return new Result(DefaultSymbolFactory.getOpenMutableSymbol(invokeExpr));
	}
	
	private String getConstants(ISymbol<? extends Value> c) {
		List<ISymbol<? extends Value>> constants = this.getAttributes(c);
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		ListIterator<ISymbol<? extends Value>> iterator = constants.listIterator();
		int size = constants.size() - 1;
		while(iterator.nextIndex() < size) {
			ISymbol<? extends Value> constraint = iterator.next();
			buffer.append(constraint.getValue());
			buffer.append(",");
		}
		buffer.append(iterator.next());
		buffer.append("]");
		return buffer.toString();
	}
	
	public IResult addLayoutComponent(List<ISymbol<? extends Value>> arguments) {
		IResult result = new Result(Interpreter.VOID);

		ISymbol<? extends Value> c = arguments.get(0);
		if(this.propertyToSymbolMap.values().contains(c)) {
			ICritic critic = new Critic(
					TYPE + "-Shared-Element",
					ICritic.Type.Criticism,
					ICritic.Priority.High,
					"Shared Element",
					"You are sharing the same element in " + this.getConstants(c) + " positions of BorderLayout. \n" +
					"If you want a common behavior for two different widgets then you should share there model not their view.\n" +
					"The element is created at " + c.getLocationString() + ".\n"
					);
			critic.setAttribute(ICritic.URL, "http://www.java2s.com/Code/Java/Swing-JFC/TextAreaShareModel.htm");
			result.add(critic);
		}
		
		ClosedString constraint = new ClosedString(BorderLayout.CENTER);
		if(arguments.get(1) instanceof ClosedString) {
			constraint = (ClosedString)arguments.get(1);
		}
		
		String constant = (String)constraint.getValue();
		
		ISymbol<? extends Value> component = this.get(constraint);
		if(component != null) {
			// We have a problem
			
			int containerSize = 0;
			int layoutSize = 0;
			
			JComponentAbstraction parent = this.getContainer();
			if(parent != null) {
				ClosedList<Value> components = (ClosedList<Value>)parent.get(JComponentAbstraction.COMPONENTS);
				if(components != null)
					containerSize = components.size();
			}
			
			ClosedList<Value> children = this.getComponents();
			if(children != null) {
				layoutSize = children.size();
			}
			
			ICritic critic = new Critic(TYPE + "-Replacing-Element",
					ICritic.Type.Criticism,
					ICritic.Priority.High,
					"Overwritten Element",
					"You are overwritting an existing element at [" + constant + "] position of BorderLayout." +
					"As a result, the contents of the container and its layout manager are not the same." +
					"Specifically, the container has " + containerSize + " child components, but the layout " +
					" has " + layoutSize + " components."
					);
			critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/border.html");
			result.add(critic);
		}
		else {
			// component == null
			ICritic critic = new Critic(TYPE + "-Adding-Element",
					ICritic.Type.Explanation,
					ICritic.Priority.Low,
					"Adding Element",
					"This action has added the component to the [" + constant + "] location of BorderLayout. " +
					"Please refer to the associated documentation for details on other available locations."
					);
			critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/border.html");
			result.add(critic);
		}
		this.put(constraint, c);
		return result;
	}
	
	@Override
	public IResult layoutContainer(JComponentAbstraction target) {
		@SuppressWarnings("unchecked")
		ClosedList<Value> list = (ClosedList<Value>)target.get(JComponentAbstraction.COMPONENTS);

		IResult result = new Result(Interpreter.VOID); 
		if(list == null)
			return result;

		ISymbol<? extends Value> centerComp = this.get(new ClosedString(BorderLayout.CENTER));
		for(ISymbol<? extends Value> s : list) {
			JComponentAbstraction comp = (JComponentAbstraction)s;
			DimensionAbstraction size = null;
			PointAbstraction location = null;
			
			if(centerComp != null && centerComp.equals(comp)) {
				// BorderLayout allocates size and location to center component even if the components
				// do not have a preferred size defined
				size = DimensionAbstraction.getDefaultSize();
				location = PointAbstraction.getDefaultLocation();				
			}
			else {
				// Look for preferred size
				DimensionAbstraction preferredSize = (DimensionAbstraction)comp.getPreferredSize().getValue();
				if(!preferredSize.isInitialized()) {
					size = new DimensionAbstraction(); // Unknown size
					location = new PointAbstraction(); // Unknown location
					
					this.addNullLayoutRelatedCritic(result, comp);					
				}
				else  {
					size = DimensionAbstraction.getDefaultSize();
					location = PointAbstraction.getDefaultLocation();
				}
			}
			comp.put(JComponentAbstraction.SIZE, size);
			comp.put(JComponentAbstraction.LOCATION, location);
		}
		return result;
	}

	@Override
	public IResult checkAtEnd() {
		Result result = new Result(Interpreter.VOID);
		this.checkOrphan(result);
		return result;
	}	
}
