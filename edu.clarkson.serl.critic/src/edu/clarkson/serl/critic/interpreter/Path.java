/*
 * Path.java
 * Jul 7, 2011
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
 
package edu.clarkson.serl.critic.interpreter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import soot.jimple.Stmt;

/**
 * Represents an {@link ArrayList} of jimple {@link Stmt}s in execution order.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class Path extends ArrayList<Stmt> {
	private static final long serialVersionUID = 8313667240952092269L;

	public static final int EXCEPTION = 4;
	public static final int SATISFIABLE = 3;
	public static final int UNSATISFIABLE = 2;
	public static final int FEASIBLE = 1;
	public static final int UNKNOWN = 0;
	public static final int INFEASIBLE = -1;
	
	private HashMap<Stmt, Integer> loopExitToRepeatitionMap;
	
	/**
	 * Initializes {@link Path} with default settings.
	 */
	public Path() {
		loopExitToRepeatitionMap = new HashMap<Stmt, Integer>();
	}

	/**
	 * Initializes {@link Path} with supplied initial capacity.
	 * 
	 * @param initialCapacity
	 */
	public Path(int initialCapacity) {
		super(initialCapacity);
		loopExitToRepeatitionMap = new HashMap<Stmt, Integer>();
	}

	/**
	 * Initializes {@link Path} with shallow copy of supplied collection.
	 * 
	 * @param c
	 */
	public Path(Collection<? extends Stmt> c) {
		super(c);
		loopExitToRepeatitionMap = new HashMap<Stmt, Integer>();
	}

	/**
	 * Gets the repetition of the supplied loop exit statement in the path.
	 * 
	 * @param stmt
	 * @return 
	 */
	protected int getLoopExitRepeatition(Stmt stmt) {
		Integer value = this.loopExitToRepeatitionMap.get(stmt);
		if(value != null)
			return value;
		return 0;
	}
	
	/**
	 * Increment the repetition of the supplied loop exit statement in the path.
	 * 
	 * @param stmt
	 * @return The new incremented value
	 */
	protected int incrementLoopExitRepeatition(Stmt stmt) {
		int value = this.getLoopExitRepeatition(stmt);
		this.loopExitToRepeatitionMap.put(stmt, ++value);
		return value;
	}

	/**
	 * Performs shallow cloning of the path.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		Path cloned = (Path)super.clone();
		
		// Perform cloning of the mutable reference field also
		cloned.loopExitToRepeatitionMap = (HashMap<Stmt, Integer>)this.loopExitToRepeatitionMap.clone();
		return cloned;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for(Stmt s : this) {
			buffer.append(s);
			buffer.append("\n");
		}
		return buffer.toString();
	}	
}
