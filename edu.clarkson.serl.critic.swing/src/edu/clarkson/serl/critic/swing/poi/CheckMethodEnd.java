/*
 * CheckMethodEnd.java
 * Aug 18, 2011
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
import edu.clarkson.serl.critic.extension.ICheckPoint;
import edu.clarkson.serl.critic.extension.IResult;
import edu.clarkson.serl.critic.extension.Result;
import edu.clarkson.serl.critic.interpreter.IStack;
import edu.clarkson.serl.critic.interpreter.IStackFrame;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class CheckMethodEnd implements ICheckPoint {
	public Interest getInterest() {
		return ICheckPoint.Interest.MethodEnd;
	}
	
	public IResult check(Stmt programCounter, IStackFrame frame, IStack stack, Set<ISymbol<? extends Value>> heap) {
		Set<ISymbol<? extends Value>> set = frame.getNonEscapingNewObjects();
		Result result = new Result(Interpreter.VOID);
		check(programCounter, frame, stack, heap, result, set);
		return result;
	}
	
	
	public static void check(Stmt programCounter, IStackFrame frame, IStack stack, Set<ISymbol<? extends Value>> heap, 
			IResult result, Set<ISymbol<? extends Value>> checkSet) {
		for(ISymbol<? extends Value> s : checkSet) {
			if(s instanceof ICheckable) {
				IResult tempResult = ((ICheckable<?>)s).checkAtEnd(); 
				result.add(tempResult);
			}
		}
	}
}
