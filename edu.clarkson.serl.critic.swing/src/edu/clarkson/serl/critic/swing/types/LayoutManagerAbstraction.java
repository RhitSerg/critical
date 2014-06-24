/*
 * LayoutManagerAbstraction.java
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Value;
import soot.jimple.InvokeExpr;
import edu.clarkson.serl.critic.adt.ClosedList;
import edu.clarkson.serl.critic.extension.Critic;
import edu.clarkson.serl.critic.extension.ICritic;
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
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class LayoutManagerAbstraction extends SymbolicApi<Value> {
	public static final String TYPE = "java.awt.LayoutManager"; 
	public static final AbstractValue DEFAULT_VALUE = AbstractValue.fromObject(TYPE);
	public static final String ADD_COMPONENT = "addLayoutComponent";
	public static final String REMOVE_COMPONENT = "removeLayoutComponent";
	public static final SymbolicKey DEFAULT = SymbolicKey.fromObject("default");
	
	public LayoutManagerAbstraction() {
		super(DEFAULT_VALUE, false, true);
	}
	
	public LayoutManagerAbstraction(Value sootValue) {
		super(sootValue, false, true);
	}

	public LayoutManagerAbstraction(Value sootValue, boolean open, boolean mutable) {
		super(sootValue, open, mutable);
	}
	
	public void setDefault(boolean d) {
		if(d)
			this.put(DEFAULT, Interpreter.TRUE);
		else
			this.put(DEFAULT, Interpreter.FALSE);
	}
	
	public boolean isDefault() {
		ISymbol<? extends Value> d = this.get(DEFAULT);
		return Interpreter.TRUE.equals(d);
	}
	

	public IResult addLayoutComponent(List<ISymbol<? extends Value>> arguments) {
		return new Result(Interpreter.VOID);
	}

	public IResult removeLayoutComponent(List<ISymbol<? extends Value>> arguments) {
		ISymbol<? extends Value> c = arguments.get(0);
		this.removeAllEntries(c);
//		Iterator<Map.Entry<ISymbol<? extends Value>, ISymbol<? extends Value>>> iterator = this.propertyToSymbolMap.entrySet().iterator();
//		while(iterator.hasNext()) {
//			ISymbol<? extends Value> component = iterator.next().getValue();
//			if(component.equals(c)) {
//				iterator.remove();
//				this.notifyRemoved(component);
//			}	
//		}
		return new Result(Interpreter.VOID);
	}
	
	public IResult layoutContainer(JComponentAbstraction target) {
		return new Result(Interpreter.VOID);
	}

	@Override
	public IResult init(List<ISymbol<? extends Value>> arguments) {
		return new Result(Interpreter.VOID);
	}

	@Override
	public IResult execute(InvokeExpr invokeExpr, List<ISymbol<? extends Value>> arguments) {
		// We do not handle other methods
		return new Result(DefaultSymbolFactory.getOpenMutableSymbol(invokeExpr));
	}
	
	protected JComponentAbstraction getContainer() {
		for(ISymbol<? extends Value> c : this.getAllContainers()) {
			if(c instanceof JPanelAbstraction) {
				JPanelAbstraction panel = (JPanelAbstraction)c;
				ISymbol<? extends Value> layout = panel.get(JComponentAbstraction.LAYOUT);
				if(layout == this)
					return panel;
			}
		}
		return null;
	}
	
	protected ClosedList<Value> getComponents() {
		ClosedList<Value> list = new ClosedList<Value>(AbstractValue.fromObject("java.util.List"));
		for(ISymbol<? extends Value> e : this.getAllContained()) {
			if(e instanceof JComponentAbstraction) {
				list.add(e);
			}
		}
		return list;
	}
	
	protected void addNullLayoutRelatedCritic(IResult result, JComponentAbstraction comp) {
		ICritic critic = new Critic(this.getType() + "-Preferred-Size-Expected:Not-Assigned",
				comp.getOutermostClass(),
				comp.getLineNumber(),
				ICritic.Type.Criticism,
				ICritic.Priority.High,
				"Preferred Size Not Defined",
				"The component initialized at " + comp.getLocationString() + " does not have a preferred size \n" +
				"defined that is required by " + this.getType() + ". This usually happens when the component is a container with \n" +
				"null layout. For such container you should explicitly call setPreferredSize() method to make it work \n" +
				"with the " + this.getType() + " layout manager. \n" 
				);
		critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/components/jcomponent.html#layoutapi");
		result.add(critic);
	}
	
	protected boolean checkOrphan(IResult result) {
		Set<ISymbol<? extends Value>> containers = this.getAllContainers();
		boolean hasAContainer = false;
		for(ISymbol<? extends Value> container : containers) {
			if(container instanceof JComponentAbstraction) {
				hasAContainer = true;
				break;
			}
		}
		
		if(!hasAContainer && !this.isDefault()) {
			ICritic critic = new Critic(this.getType() + "-Orphan-GUI-Object",
					this.getOutermostClass(),
					this.getLineNumber(),
					ICritic.Type.Criticism,
					ICritic.Priority.Medium,
					"Orphan GUI Object",
					"The layout manager initialized at " + this.getLocationString() + " is not used." +
					"Is this really what you want to do with the layout manager since it has no effect?"
					);
			critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/using.html#set");
			result.add(critic);
		}
		return !hasAContainer;
	}
}
