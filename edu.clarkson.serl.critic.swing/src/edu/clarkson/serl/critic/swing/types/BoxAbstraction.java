/*
 * BoxAbstraction.java
 * Mar 25, 2012
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

import edu.clarkson.serl.critic.extension.Critic;
import edu.clarkson.serl.critic.extension.ICritic;
import edu.clarkson.serl.critic.extension.IResult;
import edu.clarkson.serl.critic.extension.Result;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import soot.Value;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class BoxAbstraction extends JComponentAbstraction {
	public static final String TYPE = "javax.swing.Box";
	public static final Value DEFAULT_VALUE = AbstractValue.fromObject(TYPE);
	
	public BoxAbstraction() {
		super(DEFAULT_VALUE);
	}

	public BoxAbstraction(Value value) {
		super(value);
	}

	public BoxAbstraction(Value value, boolean open, boolean mutable) {
		super(value, open, mutable);
	}
	
	@Override
	public IResult init(List<ISymbol<? extends Value>> arguments) {
		IResult result = super.init(arguments);
		BoxLayoutAbstraction layout = new BoxLayoutAbstraction();
		layout.setDefault(true);
		this.put(LAYOUT, layout);
		return result;
	}
	
	public IResult setLayout(List<ISymbol<? extends Value>> arguments) {
		IResult result = new Result(Interpreter.VOID);
		ICritic critic = new Critic(
				TYPE + "-Setting-Layout",
				ICritic.Type.Criticism,
				ICritic.Priority.High,
				"Setting Layout on a Box",
				"You cannot set a new layout to a Box. It will throw an AWTError." 
				);
		result.add(critic);
		return result;
	}
	

	public static IResult createGlue(List<ISymbol<? extends Value>> arguments) {
		BoxAbstraction box = new BoxAbstraction();
		// The arguments has no effect so lets reuse it
		box.init(arguments);
		return new Result(box);
	}

	public static IResult createHorizontalBox(List<ISymbol<? extends Value>> arguments) {
		BoxAbstraction box = new BoxAbstraction();
		// The arguments has no effect so lets reuse it
		box.init(arguments);
		return new Result(box);
	}

	public static IResult createHorizontalGlue(List<ISymbol<? extends Value>> arguments) {
		BoxAbstraction box = new BoxAbstraction();
		// The arguments has no effect so lets reuse it
		box.init(arguments);
		return new Result(box);
	}
	
	public static IResult createHorizontalStrut(List<ISymbol<? extends Value>> arguments) {
		BoxAbstraction box = new BoxAbstraction();
		// The arguments has no effect so lets reuse it
		box.init(arguments);
		return new Result(box);
	}
	
	public static IResult createRigidArea(List<ISymbol<? extends Value>> arguments) {
		BoxAbstraction box = new BoxAbstraction();
		// The arguments has no effect so lets reuse it
		box.init(arguments);
		return new Result(box);
	}

	public static IResult createVerticalBox(List<ISymbol<? extends Value>> arguments) {
		BoxAbstraction box = new BoxAbstraction();
		// The arguments has no effect so lets reuse it
		box.init(arguments);
		return new Result(box);
	}

	public static IResult createVerticalGlue(List<ISymbol<? extends Value>> arguments) {
		BoxAbstraction box = new BoxAbstraction();
		// The arguments has no effect so lets reuse it
		box.init(arguments);
		return new Result(box);
	}

	public static IResult createVerticalStrut(List<ISymbol<? extends Value>> arguments) {
		BoxAbstraction box = new BoxAbstraction();
		// The arguments has no effect so lets reuse it
		box.init(arguments);
		return new Result(box);
	}
}
