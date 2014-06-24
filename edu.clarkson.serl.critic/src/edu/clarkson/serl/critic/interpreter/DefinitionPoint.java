/*
 * DefinitionPoint.java
 * Aug 19, 2011
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

import soot.Local;
import soot.Value;

/**
 * Represents a point in the source code where a local variable is assigned
 * a symbolic value ({@link ISymbol}.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 * @see 
 */
public class DefinitionPoint implements Comparable<DefinitionPoint> {
	private int lineNumber;
	private Value local;
	
	/**
	 * Creates a definition point with a local variable and a line number
	 * where the local variable is defined (or assigned a symbolic value).
	 * @param local
	 * @param lineNumber
	 */
	public DefinitionPoint(Value local, int lineNumber) {
		this.local = local;
		this.lineNumber = lineNumber;
	}
	
	/**
	 * @return The line number of the definition point.
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	
	/**
	 * @return The local variable that is assigned a symbolic value.
	 */
	public Value getLocal() {
		return local;
	}
	
	/**
	 * Checks whether the variable involved with this definition point is a
	 * user defined variable or system generated. The system defined variable
	 * is a automatically generated variable that is not visible in the actual
	 * source code.
	 * 
	 * @return <tt>true</tt> if user defined and <tt>false</tt> if system defined.
	 */
	public boolean isUserDefinedVariable() {
		if(this.local instanceof Local) {
			return !((Local) this.local).getName().startsWith("$");
		}
		return false;
	}

	public int compareTo(DefinitionPoint o) {
		if(this.equals(o))
			return 0;
		
		if(this.lineNumber == o.lineNumber)
			return -1;
		
		return this.lineNumber - o.lineNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lineNumber;
		result = prime * result + ((local == null) ? 0 : local.hashCode());
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
		DefinitionPoint other = (DefinitionPoint) obj;
		if (lineNumber != other.lineNumber)
			return false;
		if (local == null) {
			if (other.local != null)
				return false;
		} else if (!local.equals(other.local))
			return false;
		return true;
	}
}
