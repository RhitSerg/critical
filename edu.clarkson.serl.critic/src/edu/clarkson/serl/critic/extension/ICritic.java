/*
 * ICritic.java
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

import java.util.Map;

import org.eclipse.core.resources.IMarker;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public interface ICritic extends Comparable<ICritic> {
	public static final String JAVA_TYPE = "javaType";
	public static final String LINE_NUMBER = IMarker.LINE_NUMBER;
	public static final String MESSAGE = IMarker.MESSAGE;
	public static final String PRIORITY = IMarker.PRIORITY;
	public static final String URL = "url";

	
	public static enum Type {
		Recommendation, 
		Explanation,
		Criticism
	}
	
	public static enum Priority {
		Low,
		Medium,
		High
	}

	public String getId();
	public String getOutermostClass();
	public int getLineNumber();
	public Type getType();
	public Priority getPriority();
	public String getTitle();
	public String getDescription();
	
	public Object getAttribute(String key);
	public Object setAttribute(String key, Object value);
	public Map<String, Object> getAttributeMap();
}
