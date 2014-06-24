/*
 * Critic.java
 * Aug 16, 2011
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.util.Util;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class Critic implements ICritic {
	private String id;
	private String javaType;
	private int lineNumber;
	private Type type;
	private Priority priority;
	private String title;
	private String description;
	private HashMap<String, Object> attributeMap;
	
	/**
	 * Creates a critic with supplied fields.
	 * 
	 * @param id
	 * @param javaType
	 * @param lineNumber
	 * @param type
	 * @param priority
	 * @param title
	 * @param description
	 */
	public Critic(String id, String javaType, int lineNumber, Type type, Priority priority, String title, String description) {
		this.id = id;
		this.javaType = javaType;
		this.lineNumber = lineNumber;
		this.type = type;
		this.priority = priority;
		this.title = title;
		this.description = description;
		this.attributeMap = new HashMap<String, Object>(5);
	}
	
	/**
	 * Creates a critic with supplied fields.
	 * 
	 * @param id
	 * @param lineNumber
	 * @param type
	 * @param priority
	 * @param title
	 * @param description
	 */
	public Critic(String id, int lineNumber, Type type, Priority priority, String title, String description) {
		this.id = id;
		this.lineNumber = lineNumber;
		this.javaType = Util.getOuterMostClass(Interpreter.instance().getCurrentClass());
		this.type = type;
		this.priority = priority;
		this.title = title;
		this.description = description;
		this.attributeMap = new HashMap<String, Object>(5);
	}
	
	
	/**
	 * Creates a critic with supplied field. Line number is automatically calculated
	 * based on the current location of the program counter in the {@link Interpreter}.
	 * 
	 * @param id
	 * @param type
	 * @param priority
	 * @param title
	 * @param description
	 */
	public Critic(String id, Type type, Priority priority, String title, String description) {
		this.id = id;
		this.type = type;
		this.priority = priority;
		this.title = title;
		this.description = description;
		this.lineNumber = Interpreter.instance().getLineNumber();
		this.javaType = Util.getOuterMostClass(Interpreter.instance().getCurrentClass());
		this.attributeMap = new HashMap<String, Object>(5);
	}
	
	public String getOutermostClass() {
		return this.javaType;
	}
	
	public int getLineNumber() {
		return this.lineNumber;
	}

	public String getId() {
		return this.id;
	}

	public Type getType() {
		return this.type;
	}

	public Priority getPriority() {
		return this.priority;
	}

	public String getTitle() {
		return this.title;
	}

	public String getDescription() {
		return this.description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + lineNumber;
		result = prime * result
				+ ((priority == null) ? 0 : priority.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Critic other = (Critic) obj;
		if (lineNumber != other.lineNumber)
			return false;
		if (priority != other.priority)
			return false;
		if (type != other.type)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		return true;
	}


	public int compareTo(ICritic that) {
		if(this.equals(that))
			return 0;
		int result = this.priority.compareTo(that.getPriority());
		if(result != 0)
			return result;
		result = this.type.compareTo(that.getType());
		if(result != 0)
			return result;
		result  = this.lineNumber - that.getLineNumber();
		if(result != 0)
			return result;
		return 1;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Id: " + this.id + "\n");
		buffer.append("Line: " + this.lineNumber + "\n");
		buffer.append("Type: " + this.type + "\n");
		buffer.append("Priority: " + this.priority + "\n");
		buffer.append("Title: " + this.title + "\n");
		buffer.append("Description: \n");
		buffer.append(this.description);
		buffer.append("\n");
		buffer.append("Other Attributes:\n");
		for(Map.Entry<String, Object> e : this.attributeMap.entrySet()) {
			buffer.append(e.getKey() + ": " + e.getValue() + "\n");
		}
		return buffer.toString();
	}

	@Override
	public Object getAttribute(String key) {
		return this.attributeMap.get(key);
	}


	@Override
	public Object setAttribute(String key, Object value) {
		return this.attributeMap.put(key, value);
	}


	@Override
	public Map<String, Object> getAttributeMap() {
		return Collections.unmodifiableMap(this.attributeMap);
	}
}
