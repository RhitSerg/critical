/*
 * RefFactory.java
 * Jul 11, 2011
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
 
package edu.clarkson.serl.critic.factory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.clarkson.serl.critic.extension.ICheckPoint;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.model.RefArray;
import edu.clarkson.serl.critic.interpreter.model.RefCaughtException;
import edu.clarkson.serl.critic.interpreter.model.RefInstanceField;
import edu.clarkson.serl.critic.interpreter.model.RefParameter;
import edu.clarkson.serl.critic.interpreter.model.RefStaticField;
import edu.clarkson.serl.critic.interpreter.model.RefThis;
import soot.SootMethod;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.ParameterRef;
import soot.jimple.RefSwitch;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class RefFactory implements RefSwitch, IFactory {
	private ISymbol<? extends Value> symbol;

	public Set<String> getSupportedTypes() {
		return Collections.emptySet();
	}

	public ISymbol<? extends Value> newSymbol(String type, Value value, boolean open, boolean mutable) {
		value.apply(this);
		ISymbol<? extends Value> result = symbol;
		symbol = null;
		return result;
	}

	public void caseArrayRef(ArrayRef v) {
		symbol = new RefArray(v);
	}

	public void caseStaticFieldRef(StaticFieldRef v) {
		symbol = new RefStaticField(v);
	}

	public void caseInstanceFieldRef(InstanceFieldRef v) {
		symbol = new RefInstanceField(v);
	}

	public void caseParameterRef(ParameterRef v) {
		symbol = new RefParameter(v);
	}

	public void caseCaughtExceptionRef(CaughtExceptionRef v) {
		symbol = new RefCaughtException(v);
	}

	public void caseThisRef(ThisRef v) {
		symbol = new RefThis(v);
	}

	public void defaultCase(Object obj) {
		throw new UnsupportedOperationException("Unknown ref type encountered.");
	}
	
	public Set<ICheckPoint> getCheckPoints() {
		return Collections.emptySet();
	}
	
	public Set<SootMethod> getEntryMethods() {
		return Collections.emptySet();
	}

	public void checkEntry(SootMethod method) {
	}

	@Override
	public Class<?> getClassFor(String type) {
		throw new UnsupportedOperationException("RefFactory does not support getClassFor(type) operation.");
	}	
	
	@Override
	public boolean shouldInline(SootMethod method, ISymbol<? extends Value> receiver, List<ISymbol<? extends Value>> arguments, Stmt callSite) {
		return false;
	}
}
