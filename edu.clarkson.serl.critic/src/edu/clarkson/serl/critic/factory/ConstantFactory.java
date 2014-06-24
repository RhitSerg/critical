/*
 * ConstantFactory.java
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.clarkson.serl.critic.adt.ClosedString;
import edu.clarkson.serl.critic.extension.ICheckPoint;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.model.ConstClass;
import edu.clarkson.serl.critic.interpreter.model.ConstDouble;
import edu.clarkson.serl.critic.interpreter.model.ConstFloat;
import edu.clarkson.serl.critic.interpreter.model.ConstInteger;
import edu.clarkson.serl.critic.interpreter.model.ConstLong;
import edu.clarkson.serl.critic.interpreter.model.ConstNull;
import soot.SootMethod;
import soot.Value;
import soot.jimple.ClassConstant;
import soot.jimple.Constant;
import soot.jimple.ConstantSwitch;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.LongConstant;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class ConstantFactory implements ConstantSwitch, IFactory {
	private HashSet<String> types;
	private ISymbol<? extends Value> object;
	public ConstantFactory() {
		types = new HashSet<String>();
		types.add("boolean");
		types.add("char");
		types.add("byte");
		types.add("short");
		types.add("int");
		types.add("long");
		types.add("float");
		types.add("double");
	}
	
	public Set<String> getSupportedTypes() {
		return Collections.unmodifiableSet(types);
	}

	public ISymbol<? extends Value> newSymbol(String type, Value value, boolean open, boolean mutable) {
		if(!(value instanceof Constant)) // Sanity check
			throw new UnsupportedOperationException("ConstantFactory can only produce symbols for soot.jimple.Constant type hierarchy.");
		value.apply(this);
		ISymbol<? extends Value> object = this.object;
		this.object = null;
		return object;
	}
	
	public void caseDoubleConstant(DoubleConstant v) {
		this.object = new ConstDouble(v);
	}

	public void caseFloatConstant(FloatConstant v) {
		this.object = new ConstFloat(v);
	}

	public void caseIntConstant(IntConstant v) {
		this.object = new ConstInteger(v);
	}

	public void caseLongConstant(LongConstant v) {
		this.object = new ConstLong(v);
	}

	public void caseNullConstant(NullConstant v) {
		this.object = ConstNull.NULL;
	}

	public void caseStringConstant(StringConstant v) {
		this.object = new ClosedString(v.value);
	}

	public void caseClassConstant(ClassConstant v) {
		this.object = new ConstClass(v);
	}

	public void defaultCase(Object object) {
		throw new UnsupportedOperationException("The supplied value for constant is not syported by the library.");
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
		throw new UnsupportedOperationException("ConstantFactory does not support getClassFor(type) operation.");
	}

	@Override
	public boolean shouldInline(SootMethod method, ISymbol<? extends Value> receiver, List<ISymbol<? extends Value>> arguments, Stmt callSite) {
		return false;
	}
}
