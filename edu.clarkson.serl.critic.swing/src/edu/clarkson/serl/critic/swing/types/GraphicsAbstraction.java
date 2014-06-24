/*
 * GraphicsAbstraction.java
 * Aug 30, 2011
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
import edu.clarkson.serl.critic.extension.Critic;
import edu.clarkson.serl.critic.extension.ICritic;
import edu.clarkson.serl.critic.extension.IResult;
import edu.clarkson.serl.critic.extension.Result;
import edu.clarkson.serl.critic.extension.SymbolicApi;
import edu.clarkson.serl.critic.factory.DefaultSymbolFactory;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.SymbolicKey;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class GraphicsAbstraction extends SymbolicApi<Value> {
	public static final String TYPE = "java.awt.Graphics";
	public static final SymbolicKey SUPER_PAINTED = SymbolicKey.fromObject("SuperPainted");
	
	public GraphicsAbstraction(Value sootValue) {
		super(sootValue, false, true);
	}

	public GraphicsAbstraction(Value sootValue, boolean open, boolean mutable) {
		super(sootValue, open, mutable);
	}
	
	@Override
	public IResult init(List<ISymbol<? extends Value>> arguments) {
		return new Result(Interpreter.VOID);
	}
	
	public boolean isSuperPainted() {
		return Interpreter.TRUE.equals(this.get(SUPER_PAINTED));
	}
	
	public void setSuperPaint(boolean painted) {
		if(painted)
			this.put(SUPER_PAINTED, Interpreter.TRUE);
		else
			this.put(SUPER_PAINTED, Interpreter.FALSE);
	}
	
	@Override
	public IResult execute(InvokeExpr invokeExpr, List<ISymbol<? extends Value>> arguments) {
//		String method = invokeExpr.getMethod().getName();
//		if(method.startsWith("draw") || method.startsWith("fill")) {
//			if(!this.isSuperPainted()) {
//				ICritic critic = new Critic(
//						TYPE + "-Paint:Without-Super-Call",
//						this.getLineNumber(),
//						ICritic.Type.Criticism,
//						ICritic.Priority.Medium,
//						"Overriding Jcomponent.paint() Without super.paint() Call",
//						"You are overriding the paint method of JComponent without calling\n" +
//						"super.paint(Graphics). If the component contains any layout manager\n" +
//						"then the effect of layout manager will not be visible."
//						);
//				IResult result = new Result(Interpreter.VOID);
//				result.add(critic);
//			}
//		}
		
		// We do not handle other methods
		return new Result(DefaultSymbolFactory.getOpenMutableSymbol(invokeExpr));
	}
}
