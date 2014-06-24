/*
 * Pair.java
 * Mar 2, 2011
 *
 * Serl's Path-sensitive Program Analysis Framework
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
 * Chandan Raj Rupakheti (rupakhcr@clarkson.edu)
 * Daqing Hou (dhou@clarkson.edu)
 * Clarkson University
 * PO Box 5722
 * Potsdam
 * NY 13699-5722
 * http://serl.clarkson.edu
*/
 
package edu.clarkson.serl.critic.preferences;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
@XStreamAlias("option")
public class Pair implements Comparable<Pair> {
	@XStreamAsAttribute
	private String key;
	
	@XStreamAsAttribute
	private String value;

	/**
	 * Constructor for creating a key value pair.
	 * 
	 * @param key
	 * @param value
	 */
	public Pair(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	
	public String getKey() {
		return key;
	}


	public String getValue() {
		return value;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Pair other = (Pair) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}


	public int compareTo(Pair that) {
		int cmp = this.key.compareTo(that.key);
		if(cmp != 0)
			return cmp;
		return this.value.compareTo(that.value);
	}
}
