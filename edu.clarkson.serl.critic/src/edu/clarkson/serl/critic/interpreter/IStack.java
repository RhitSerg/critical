/*
 * IStack.java
 * Aug 3, 2011
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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.clarkson.serl.critic.extension.ICallbackType;

import soot.SootField;
import soot.Value;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public interface IStack {
	public boolean isEmpty();
	public IStackFrame peek();
	public ISymbol<? extends Value> lookup(SootField fieldRef);
	public ISymbol<? extends Value> lookup(Value local);
	public Collection<ISymbol<? extends Value>> getStaticFields();
	public List<IStackFrame> getList();
	public Path getPath();
	public int size();
	
	public boolean addCallback(ICallbackPoint e);
	public boolean removeCallback(ICallbackPoint e);
	public Set<ICallbackPoint> getCallbackSet();
}
