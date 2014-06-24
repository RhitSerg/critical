/*
 * CheckPathEnd.java
 * Aug 23, 2011
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
 
package edu.clarkson.serl.critic.swing.poi;

import java.util.Set;

import soot.Value;
import soot.jimple.Stmt;
import edu.clarkson.serl.critic.extension.Critic;
import edu.clarkson.serl.critic.extension.ICheckPoint;
import edu.clarkson.serl.critic.extension.ICritic;
import edu.clarkson.serl.critic.extension.IResult;
import edu.clarkson.serl.critic.extension.Result;
import edu.clarkson.serl.critic.interpreter.IStack;
import edu.clarkson.serl.critic.interpreter.IStackFrame;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.swing.types.JComponentAbstraction;
import edu.clarkson.serl.critic.swing.types.JFrameAbstraction;
import edu.clarkson.serl.critic.swing.types.JLabelAbstraction;
import edu.clarkson.serl.critic.swing.types.JPanelAbstraction;
import edu.clarkson.serl.critic.swing.types.LayoutManagerAbstraction;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class CheckPathEnd implements ICheckPoint {

	public Interest getInterest() {
		return ICheckPoint.Interest.PathEnd;
	}

	public IResult check(Stmt programCounter, IStackFrame frame, IStack stack, Set<ISymbol<? extends Value>> heap) {
		Set<ISymbol<? extends Value>> escapingSet = frame.getEscapingNewObjects();
		escapingSet.addAll(stack.getStaticFields());

		Result result = new Result(Interpreter.VOID);
		CheckMethodEnd.check(programCounter, frame, stack, heap, result, escapingSet);
		return result;
	}
}
