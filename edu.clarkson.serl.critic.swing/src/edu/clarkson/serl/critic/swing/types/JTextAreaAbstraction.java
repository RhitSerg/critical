/*
 * JTextAreaAbstraction.java
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

/**
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 *
 */
public class JTextAreaAbstraction extends JComponentAbstraction {
	public static final String TYPE = "javax.swing.JTextArea";
	public static final Value DEFAULT_VALUE = AbstractValue.fromObject(TYPE);

	public JTextAreaAbstraction() {
		super(DEFAULT_VALUE);
	}

	public JTextAreaAbstraction(Value sootValue) {
		super(sootValue);
	}

	public JTextAreaAbstraction(Value sootValue, boolean open, boolean mutable) {
		super(sootValue, open, mutable);
	}

	@Override
	public IResult init(List<ISymbol<? extends Value>> arguments) {
		IResult result = super.init(arguments);
		this.put(PREFERRED_SIZE, DimensionAbstraction.getDefaultSize());
		return result;
	}
}
