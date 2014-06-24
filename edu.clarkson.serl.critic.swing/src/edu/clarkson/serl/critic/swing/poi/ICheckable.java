/*
 * ICheckable.java
 * Jan 25, 2012
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

import edu.clarkson.serl.critic.extension.IApi;
import edu.clarkson.serl.critic.extension.ICheckPoint;
import edu.clarkson.serl.critic.extension.ICritic;
import edu.clarkson.serl.critic.extension.IResult;
import soot.Value;

/**
 * This interface should be implemented to produce critics at the end of a method call or a path.
 * The {@link #checkAtEnd()} method gets called either at the <tt>MethodEnd</tt> or at the <tt>PathEnd</tt>
 * {@link ICheckPoint} depending on when <tt>this</tt> object can no more escape the expanded method boundary.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public interface ICheckable<T extends Value> extends IApi<T> {
	/**
	 * This method gets called either at the <tt>MethodEnd</tt> or at the <tt>PathEnd</tt>
	 * {@link ICheckPoint} depending on when <tt>this</tt> object can no more escape the expanded method boundary.
	 * 
	 * @return Returns {@link IResult} containing a set of {@link ICritic} recommendations.
	 */
	public IResult checkAtEnd();
}
