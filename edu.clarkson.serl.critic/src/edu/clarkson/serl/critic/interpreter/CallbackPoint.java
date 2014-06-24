/*
 * CallbackPoint.java
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Status;

import edu.clarkson.serl.critic.CriticPlugin;

import soot.FastHierarchy;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.JimpleBody;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;

/**
 * Represents a call back point that would be executed at the end of a regular code block.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public abstract class CallbackPoint implements ICallbackPoint {
	private ISymbol<? extends Value> callBack;
	private ISymbol<? extends Value> source;
	private String type;
	
	/**
	 * Constructs a call back entry object with supplied fields.
	 * 
	 * @param callBack
	 * @param source
	 * @param type
	 */
	public CallbackPoint(ISymbol<? extends Value> callBack, ISymbol<? extends Value> source, String type) {
		this.callBack = callBack;
		this.source = source;
		this.type = type;
	}

	@Override
	public ISymbol<? extends Value> getCallbackSymbol() {
		return this.callBack;
	}

	@Override
	public ISymbol<? extends Value> getSource() {
		return this.source;
	}

	@Override
	public String getCallbackType() {
		return this.type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callBack == null) ? 0 : callBack.hashCode());
		result = prime * result
				+ ((source == null) ? 0 : source.hashCode());
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
		CallbackPoint other = (CallbackPoint) obj;
		if (callBack == null) {
			if (other.callBack != null)
				return false;
		} else if (!callBack.equals(other.callBack))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	public List<ICallbackEntry> computeCallbackEntries() {
		ArrayList<ICallbackEntry> list = new ArrayList<ICallbackEntry>();
		Scene scene = Scene.v();
		SootClass callbackType = scene.getSootClass(this.type);
		
		// TODO: Need to process the case related to static method e.g. SwingUtilities.invokeLater()
		RefType refType = (RefType)callBack.getType();
		SootClass concreteClass = refType.getSootClass();
		
		// Resolve using CHA
		FastHierarchy fastHierarchy = Scene.v().getOrMakeFastHierarchy();
		for(SootMethod callbackMethod : callbackType.getMethods()) {
			try {
				SootMethod concreteMethod = fastHierarchy.resolveConcreteDispatch(concreteClass, callbackMethod);
				JimpleBody body = (JimpleBody)concreteMethod.retrieveActiveBody();
				
				// No point executing an empty callback function
				Stmt firstStmt = body.getFirstNonIdentityStmt();
				if(firstStmt instanceof ReturnStmt || firstStmt instanceof ReturnVoidStmt)
					continue;

				CallbackEntry entry = new CallbackEntry(this, concreteMethod);
				list.add(entry);
			}
			catch(Exception e) {
				e.printStackTrace();
				CriticPlugin.log(Status.ERROR, e.getMessage(), e);
			}
		}
		return list;
	}
}
