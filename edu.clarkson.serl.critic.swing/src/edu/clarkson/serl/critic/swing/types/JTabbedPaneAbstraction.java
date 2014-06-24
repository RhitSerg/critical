/*
 * JTabbedPaneAbstraction.java
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

import java.util.List;

import edu.clarkson.serl.critic.extension.IResult;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import soot.Value;
import soot.jimple.InvokeExpr;

/**
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 *
 */
public class JTabbedPaneAbstraction extends JComponentAbstraction {
	public static final String TYPE = "javax.swing.JTabbedPane";
	public static final Value DEFAULT_VALUE = AbstractValue.fromObject(TYPE);

	public JTabbedPaneAbstraction() {
		super(DEFAULT_VALUE);
	}

	public JTabbedPaneAbstraction(Value sootValue) {
		super(sootValue);
	}

	public JTabbedPaneAbstraction(Value sootValue, boolean open, boolean mutable) {
		super(sootValue, open, mutable);
	}
	
	@Override
	public IResult execute(InvokeExpr invokeExpr, List<ISymbol<? extends Value>> arguments) {
		String method  = invokeExpr.getMethod().getName();
		// We are only interested in handling addTab()
		if(method.equals("addTab")) {
			return this.add(arguments);
		}

		// Do whatever JComponent does with other methods
		return super.execute(invokeExpr, arguments);
	}
	
	@Override
	public IResult init(List<ISymbol<? extends Value>> arguments) {
		IResult result = super.init(arguments);

		// We are abstracting the layout with a FlowLayout.
		this.put(LAYOUT, DefaultLayoutManager.newDefaultLayout());
		
		return result;
	}
}
