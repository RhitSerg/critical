/*
 * ClosedSet.java
 * Jul 19, 2011
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
 
package edu.clarkson.serl.critic.adt;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.Value;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.Symbol;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class ClosedSet<T extends Value> extends Symbol<T> implements Set<ISymbol<? extends Value>>{
	/**
	 * @param sootValue
	 * @param known
	 */
	public ClosedSet(T sootValue) {
		super(sootValue, false, true);
	}

	public int size() {
		ClosedSet<T > wCopy = (ClosedSet<T>)this.getWorkingCopy();
		return wCopy.propertyToSymbolMap.size();
	}

	public boolean isEmpty() {
		ClosedSet<T > wCopy = (ClosedSet<T>)this.getWorkingCopy();
		return wCopy.propertyToSymbolMap.isEmpty();
	}

	@SuppressWarnings("unchecked")
	public boolean contains(Object o) {
		if(o instanceof ISymbol) {
			ClosedSet<T > wCopy = (ClosedSet<T>)this.getWorkingCopy();
			return wCopy.propertyToSymbolMap.containsKey(((ISymbol<? extends Value>)o).getWorkingCopy());
		}
		return false;
	}

	public Iterator<ISymbol<? extends Value>> iterator() {
		ClosedSet<T > wCopy = (ClosedSet<T>)this.getWorkingCopy();
		// We need to create clone of the backing set and return an iterator to the clone.
		// Otherwise the original set may itself be modified through cloning of its elements and
		// a ConcurrentModificationException will be thrown.
		HashSet<ISymbol<? extends Value>> clone = new HashSet<ISymbol<? extends Value>>(wCopy.propertyToSymbolMap.keySet());
		return Collections.unmodifiableSet(clone).iterator();
	}

	public Object[] toArray() {
		ClosedSet<T > wCopy = (ClosedSet<T>)this.getWorkingCopy();
		return wCopy.propertyToSymbolMap.keySet().toArray();
	}

	public <C> C[] toArray(C[] a) {
		ClosedSet<T > wCopy = (ClosedSet<T>)this.getWorkingCopy();
		return wCopy.propertyToSymbolMap.keySet().toArray(a);
	}

	public boolean add(ISymbol<? extends Value> o) {
		ISymbol<? extends Value> wObj = o.getWorkingCopy();
		if(this.contains(wObj))
			return false;
		this.put(wObj, Interpreter.VOID);
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean remove(Object o) {
		if(o instanceof ISymbol) {
			ISymbol<? extends Value> wObj = ((ISymbol<? extends Value>)o).getWorkingCopy();
			if(!this.contains(wObj))
				return false;
			ClosedSet<T> mutable = (ClosedSet<T>)this.getMutableCopy();
			mutable.propertyToSymbolMap.remove(wObj);
			mutable.notifyRemoved((ISymbol<? extends Value>)o);
		}
		return true;
	}

	public boolean containsAll(Collection<?> c) {
		return this.propertyToSymbolMap.keySet().containsAll(c);
	}

	public boolean addAll(Collection<? extends ISymbol<? extends Value>> c) {
		ClosedSet<T> mutable = (ClosedSet<T>)this.getMutableCopy();
		int size = mutable.propertyToSymbolMap.size();
		for(ISymbol<? extends Value> s : c) {
			ISymbol<? extends Value> wSym = s.getWorkingCopy();
			mutable.propertyToSymbolMap.put(wSym, Interpreter.VOID);
			mutable.notifyAdded(wSym);
		}
		return size != mutable.propertyToSymbolMap.size();
	}

	public boolean retainAll(Collection<?> c) {
		ClosedSet<T> mutable = (ClosedSet<T>)this.getMutableCopy();
		int size = mutable.propertyToSymbolMap.size();
		Iterator<ISymbol<? extends Value>> iterator = mutable.propertyToSymbolMap.keySet().iterator();
		while(iterator.hasNext()) {
			ISymbol<? extends Value> s = iterator.next();
			if(!c.contains(s)) {
				iterator.remove();
				mutable.notifyRemoved(s);
			}
		}
		return size != mutable.propertyToSymbolMap.size();
	}

	@SuppressWarnings("unchecked")
	public boolean removeAll(Collection<?> c) {
		ClosedSet<T> mutable = (ClosedSet<T>)this.getMutableCopy();
		int size = mutable.propertyToSymbolMap.size();
		for(Object o : c) {
			if(o instanceof ISymbol) {
				ISymbol<? extends Value> wObj = ((ISymbol<? extends Value>) o).getWorkingCopy();
				if(mutable.propertyToSymbolMap.containsKey(wObj)) {
					mutable.propertyToSymbolMap.remove(wObj);
					mutable.notifyRemoved(wObj);
				}
			}
		}
		return size != mutable.propertyToSymbolMap.size();
	}

	public void clear() {
		ClosedSet<T> mutable = (ClosedSet<T>)this.getMutableCopy();
		Iterator<ISymbol<? extends Value>> iterator = mutable.propertyToSymbolMap.keySet().iterator();
		while(iterator.hasNext()) {
			ISymbol<? extends Value> s = iterator.next();
			iterator.remove();
			mutable.notifyRemoved(s);
		}
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		for(ISymbol<? extends Value> e : this.propertyToSymbolMap.keySet()) {
			buffer.append(e.toString());
			buffer.append(", ");
		}
		buffer.append("]");
		return buffer.toString();
	}
}
