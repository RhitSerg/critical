/*
 * DefaultSymbolFactory.java
 * Jul 22, 2011
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

import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.LongType;
import soot.PrimType;
import soot.ShortType;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.Stmt;
import edu.clarkson.serl.critic.extension.DefaultSymbolicApi;
import edu.clarkson.serl.critic.extension.ICheckPoint;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Symbol;
import edu.clarkson.serl.critic.interpreter.model.ConstDouble;
import edu.clarkson.serl.critic.interpreter.model.ConstFloat;
import edu.clarkson.serl.critic.interpreter.model.ConstInteger;
import edu.clarkson.serl.critic.interpreter.model.ConstLong;
import edu.clarkson.serl.critic.interpreter.model.ConstNull;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class DefaultSymbolFactory implements IFactory {
	public static final ConstInteger INT_0 = ConstInteger.fromInteger(0);
	public static final ConstLong LONG_0 = ConstLong.fromLong(0);
	public static final ConstFloat FLOAT_0 = ConstFloat.fromFloat(0);
	public static final ConstDouble DOUBLE_0 = ConstDouble.fromDouble(0);
	public static final ConstNull NULL = ConstNull.NULL;
	
	public static ISymbol<? extends Value> getDefault(Type type) {
		if(type instanceof BooleanType)
			return INT_0;
		if(type instanceof ByteType)
			return INT_0;
		if(type instanceof CharType) 
			return INT_0;
		if(type instanceof ShortType)
			return INT_0;
		if(type instanceof IntType)
			return INT_0;
		if(type instanceof LongType)
			return LONG_0;
		if(type instanceof FloatType)
			return FLOAT_0;
		if(type instanceof DoubleType) 
			return DOUBLE_0;
		return NULL;
	}
	
	public static <T extends Value> ISymbol<T> getOpenMutableSymbol(T value) {
		return getSymbol(value.getType().toString(), value, true, true);
	}
	
	public static <T extends Value> ISymbol<T> getSymbol(String type, T value, boolean open, boolean mutable) {
		if(value.getType() instanceof PrimType) {
			return new Symbol<T>(value, open, mutable);
		}
		return new DefaultSymbolicApi<T>(value, open, mutable);
	}

	public Set<String> getSupportedTypes() {
		return Collections.emptySet();
	}

	public ISymbol<? extends Value> newSymbol(String type, Value value, boolean open, boolean mutable) {
		return getSymbol(type, value, open, mutable);
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
		throw new UnsupportedOperationException("DefaultSymbolFactory does not support getClassFor(type) operation.");
	}
	
	@Override
	public boolean shouldInline(SootMethod method, ISymbol<? extends Value> receiver, List<ISymbol<? extends Value>> arguments, Stmt callSite) {
		return false;
	}	
}
