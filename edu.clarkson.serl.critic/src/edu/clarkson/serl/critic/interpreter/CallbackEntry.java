/*
 * CallbackEntry.java
 * Nov 10, 2011
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

import java.util.List;

import soot.SootMethod;
import soot.Value;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class CallbackEntry implements ICallbackEntry {
	private ICallbackPoint callPoint;
	private SootMethod method;
	
	public CallbackEntry(ICallbackPoint callPoint, SootMethod method) {
		this.callPoint = callPoint;
		this.method = method;
	}
	
	@Override
	public ISymbol<? extends Value> getCallbackSymbol() {
		return this.callPoint.getCallbackSymbol();
	}

	@Override
	public ISymbol<? extends Value> getSource() {
		return this.callPoint.getSource();
	}

	@Override
	public String getCallbackType() {
		return this.callPoint.getCallbackType();
	}

	@Override
	public SootMethod getCallbackMethod() {
		return this.method;
	}
	
	public List<ISymbol<? extends Value>> getArguments() {
		return this.callPoint.getArguments(this.method);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callPoint == null) ? 0 : callPoint.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
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
		CallbackEntry other = (CallbackEntry) obj;
		if (callPoint == null) {
			if (other.callPoint != null)
				return false;
		} else if (!callPoint.equals(other.callPoint))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}
}
