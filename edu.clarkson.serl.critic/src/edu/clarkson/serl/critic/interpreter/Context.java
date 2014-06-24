/*
 * Context.java
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

import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;

/**
 * This class represents the entry point for starting symbolic execution.
 * Since a method in a concrete class may be inherited from a super class,
 * providing a class as a context parameter mean that the method should be
 * processed in the context of the provided class even if the method is not
 * physically present in the provided class.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class Context {
	public static final int ENTRY = 0;
	public static final int NON_ENTRY = 1;
	public static final int CALLBACK = 2;
	
	private SootMethod method;
	private SootClass clazz;
	private Stmt callSite;
	private int type;
	
	/**
	 * Initializes entry context for entry and non-entry methods.
	 * @param method
	 * @param clazz
	 */
	public Context(Stmt callSite, SootMethod method, SootClass clazz) {
		this.callSite = callSite;
		this.method = method;
		this.clazz = clazz;
		if(this.callSite == null)
			this.type = Context.ENTRY;
		else
			this.type = Context.NON_ENTRY;
	}
	
	/**
	 * Initializes context for callback methods such as listener.
	 * 
	 * @param method
	 */
	public Context(SootMethod method) {
		this.type = Context.CALLBACK;
		this.method = method;
		this.clazz = method.getDeclaringClass();
	}
	
	/**
	 * @return the callSite
	 */
	public Stmt getCallSite() {
		return callSite;
	}

	/**
	 * Return ENTRY, NON_ENTRY, or CALLBACK.
	 * @return
	 */
	public int getType() {
		return this.type;
	}

	/**
	 * Gets the entry method.
	 * @return the method
	 */
	public SootMethod getSootMethod() {
		return method;
	}

	/**
	 * Gets the context class for the entry method.
	 * 
	 * @return the clazz
	 */
	public SootClass getSootClass() {
		return clazz;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callSite == null) ? 0 : callSite.hashCode());
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + type;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Context other = (Context) obj;
		if (callSite == null) {
			if (other.callSite != null)
				return false;
		} else if (!callSite.equals(other.callSite))
			return false;
		if (clazz == null) {
			if (other.clazz != null)
				return false;
		} else if (!clazz.equals(other.clazz))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Context [method=" + method + ", clazz=" + clazz + ", callSite="
				+ callSite + "]";
	}
}
