/*
 * SymbolicApi.java
 * Jul 26, 2011
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
 
package edu.clarkson.serl.critic.extension;

import java.util.List;

import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Symbol;

import soot.SootMethod;
import soot.Value;
import soot.jimple.InvokeExpr;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public abstract class SymbolicApi<T extends Value> extends Symbol<T> implements IApi<T> {
	/**
	 * @param sootValue
	 * @param open
	 */
	public SymbolicApi(T sootValue, boolean open, boolean mutable) {
		super(sootValue, open, mutable);
	}
	
	/**
	 * This method is used as a constructor for a local object mostly within the 
	 * library plugin. Note that {@link #execute(SootMethod, List)} to avoid 
	 * repetition, {@link #execute(SootMethod, List)} should call this method when
	 * <tt>method.getName().equals("&lt;init&gt;")</tt> holds inside the <tt>execute</tt>
	 * method.
	 * 
	 * @param arguments
	 * @return
	 */
	public abstract IResult init(List<ISymbol<? extends Value>> arguments);

	public abstract IResult execute(InvokeExpr invokeExpr, List<ISymbol<? extends Value>> arguments);
}
