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
public class FlowLayoutAbstraction extends LayoutManagerAbstraction implements ICheckable<Value> {
	public static final String TYPE = "java.awt.FlowLayout";
	public static final SymbolicKey ALIGNMENT = SymbolicKey.fromObject("alignment");
	public static final SymbolicKey H_GAP = SymbolicKey.fromObject("hGap");
	public static final SymbolicKey V_GAP = SymbolicKey.fromObject("vGap");

	public FlowLayoutAbstraction(Value sootValue) {
		super(sootValue);
	}

	public FlowLayoutAbstraction(Value sootValue, boolean open, boolean mutable) {
		super(sootValue, open, mutable);
	}
	
	@Override
	public IResult init(List<ISymbol<? extends Value>> arguments) {
		if(arguments.size() >= 1) { // 1 argument with alignment
			this.put(ALIGNMENT, arguments.get(0));
		}
		if(arguments.size() == 3) { // 3 arguments with alignment, hgap, and vgap
			this.put(H_GAP, arguments.get(1));
			this.put(V_GAP, arguments.get(2));
		}
		return new Result(Interpreter.VOID);
	}

	@Override
	public IResult execute(InvokeExpr invokeExpr, List<ISymbol<? extends Value>> arguments) {
		String method = invokeExpr.getMethod().getName();
		if(method.equals("<init>")) {
			return this.init(arguments);
		}
		if(method.equals("setAlignment")) {
			this.put(ALIGNMENT, arguments.get(0));
		}
		else if(method.equals("setHgap")) {
			this.put(H_GAP, arguments.get(0));
		}
		else if(method.equals("setVgap")) {
			this.put(V_GAP, arguments.get(0));
		}
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

	@Override
	public IResult checkAtEnd() {
		Result result = new Result(Interpreter.VOID);
		
		// Check if this is orphan 
		if(!this.checkOrphan(result)) {
			String url = CriticPlugin.getDocumentURL(SwingCriticPlugin.PLUGIN_ID, "RE-FlowLayout.html");
			if(this.get(ALIGNMENT) == null) {
				ICritic critic = new Critic(TYPE + "-Alignment",
						this.getOutermostClass(),
						this.getLineNumber(),
						ICritic.Type.Recommendation,
						ICritic.Priority.Medium,
						"Recommendation for Alignment",
						"You are using a FlowLayout for the container. By default FlowLayout aligns its " +
						"components at center. If you want to align components at any other location, say left, " +
						"then you can use setAlignment(FlowLayout.LEFT)."
						);
				critic.setAttribute(ICritic.URL, url);
				result.add(critic);				
			}
			if(this.get(H_GAP) == null && this.get(V_GAP) == null) {
				ICritic critic = new Critic(TYPE + "-Gap",
						this.getOutermostClass(),
						this.getLineNumber(),
						ICritic.Type.Recommendation,
						ICritic.Priority.Medium,
						"Recommendation for Horizontal and Vertical Gap",
						"You are using a FlowLayout for the container. By default FlowLayout allocate " +
						"a 5-pixel horizontal and vertical gap between components and between the border " +
						"of the container and its components. You can change these values by calling " +
						"setHgap(int) and setVgap(int) methods of FlowLayout."
						);
				critic.setAttribute(ICritic.URL, url);
				result.add(critic);				
			}
		}
		return result;
	}
}
