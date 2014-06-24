/*
 * ClosedList.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import soot.Value;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Symbol;
import edu.clarkson.serl.critic.interpreter.model.ConstInteger;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class ClosedList<T extends Value> extends Symbol<T> implements List<ISymbol<? extends Value>> {
	private ArrayList<ISymbol<? extends Value>> list;
	
	/**
	 * @param sootValue
	 * @param known
	 */
	public ClosedList(T sootValue) {
		super(sootValue, false, true);
		this.propertyToSymbolMap = new HashMap<ISymbol<? extends Value>, ISymbol<? extends Value>>(0); // Not using it
		list = new ArrayList<ISymbol<? extends Value>>();
	}

	/**
	 * @param sootValue
	 * @param known
	 */
	public ClosedList(T sootValue, int size) {
		super(sootValue, false, true);
		this.propertyToSymbolMap = new HashMap<ISymbol<? extends Value>, ISymbol<? extends Value>>(0); // Not using it
		list = new ArrayList<ISymbol<? extends Value>>(size);
	}
	
	public boolean isEmpty() {
		ClosedList<T> wCopy = (ClosedList<T>)this.getWorkingCopy();
		return wCopy.list.isEmpty();
	}
	
	public boolean contains(ISymbol<? extends Value> elem) {
		ClosedList<T> wCopy = (ClosedList<T>)this.getWorkingCopy();
		ISymbol<?extends Value> wElem = elem.getWorkingCopy();
		return wCopy.list.contains(wElem);
	}
	
	public boolean add(ISymbol<? extends Value> elem) {
		ISymbol<?extends Value> wElem = elem.getWorkingCopy();
		ClosedList<T> mutable = (ClosedList<T>)this.getMutableCopy();
		boolean added = mutable.list.add(wElem);
		if(added) {
			mutable.notifyAdded(wElem);
		}
		return added;
	}
	
	public ISymbol<? extends Value> remove(int index) {
		ClosedList<T> mutable = (ClosedList<T>)this.getMutableCopy();
		ISymbol<? extends Value> removed = mutable.list.remove(index);
		mutable.notifyRemoved(removed);
		return removed;
	}
	
	public void clear() {
		ClosedList<T> mutable = (ClosedList<T>)this.getMutableCopy();
		while(!mutable.list.isEmpty()) {
			ISymbol<? extends Value> e = mutable.list.remove(0);
			mutable.notifyRemoved(e);
		}
	}
	
	public ISymbol<? extends Value> get(int index) {
		ClosedList<T> wCopy = (ClosedList<T>)this.getWorkingCopy();
		return wCopy.list.get(index);
	}
	
	@Override
	public ISymbol<? extends Value> get(ISymbol<? extends Value> index) {
		ClosedList<T> wCopy = (ClosedList<T>)this.getWorkingCopy();
		if(index.isOpen())
			throw new UnsupportedOperationException("ClosedList must be closed.");
		ISymbol<? extends Value> elem = wCopy.list.get((Integer)index.getValue());
		return elem;
	}
	
	@Override
	public ISymbol<? extends Value> put(ISymbol<? extends Value> index, ISymbol<? extends Value> value) {
		if(index.isOpen())
			throw new UnsupportedOperationException("ClosedList must be closed.");

		ISymbol<? extends Value> wValue = value.getWorkingCopy();
		ClosedList<T> mutable = (ClosedList<T>)this.getMutableCopy();
		ISymbol<? extends Value> oldValue = mutable.list.set((Integer)index.getValue(), wValue);
		mutable.notifyRemoved(oldValue);
		mutable.notifyAdded(wValue);
		return oldValue;
	}
	
	@Override
	public Map<ISymbol<? extends Value>, ISymbol<? extends Value>> getPropertyValueMap() {
		ClosedList<T> wCopy = (ClosedList<T>)this.getWorkingCopy();
		TreeMap<ISymbol<? extends Value>, ISymbol<? extends Value>> map = new TreeMap<ISymbol<? extends Value>, ISymbol<? extends Value>>();
		int i = 0;
		for(ISymbol<? extends Value> e : wCopy.list) {
			map.put(ConstInteger.fromInteger(i++), e);
		}
		return Collections.unmodifiableMap(map); 
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		for(ISymbol<? extends Value> e : this.list) {
			buffer.append(e.toString());
			buffer.append(", ");
		}
		buffer.append("]");
		return buffer.toString();
	}

	public int size() {
		ClosedList<T> wCopy = (ClosedList<T>)this.getWorkingCopy();
		return wCopy.list.size();
	}

	public boolean contains(Object o) {
		ClosedList<T> wCopy = (ClosedList<T>)this.getWorkingCopy();
		if(o instanceof ISymbol) {
			return wCopy.list.contains(o);
		}
		return false;
	}

	public Object[] toArray() {
		ClosedList<T> wCopy = (ClosedList<T>)this.getWorkingCopy();
		return wCopy.list.toArray();
	}

	public <C> C[] toArray(C[] a) {
		ClosedList<T> wCopy = (ClosedList<T>)this.getWorkingCopy();
		return wCopy.list.toArray(a);
	}

	@SuppressWarnings("unchecked")
	public boolean remove(Object o) {
		if(o instanceof ISymbol) {
			ClosedList<T> mutable = (ClosedList<T>)this.getMutableCopy();
			ISymbol<? extends Value> wObj = ((ISymbol<? extends Value>)o).getWorkingCopy();
			if(mutable.list.remove(wObj)) {
				mutable.notifyRemoved(wObj);
				return true;
			}
			else if(o instanceof ConstInteger) {
				int index = (Integer)((ConstInteger)o).getValue();
				ISymbol<? extends Value> removed = mutable.list.remove(index);
				if(removed != null) {
					mutable.notifyRemoved(removed);
					return true;
				}
			}
		}
		return false;
	}

	public boolean containsAll(Collection<?> c) {
		ClosedList<T> wCopy = (ClosedList<T>)this.getWorkingCopy();
		return wCopy.list.containsAll(c);
	}

	public boolean addAll(Collection<? extends ISymbol<? extends Value>> c) {
		ClosedList<T> mutable = (ClosedList<T>)this.getMutableCopy();
		int size = mutable.list.size();
		
		for(ISymbol<? extends Value> e : c) {
			ISymbol<? extends Value> wElem = e.getWorkingCopy();
			mutable.list.add(wElem);
			mutable.notifyAdded(e);
		}
		
		if(size != mutable.list.size())
			return true;
		return false;
	}

	public boolean addAll(int index, Collection<? extends ISymbol<? extends Value>> c) {
		ClosedList<T> mutable = (ClosedList<T>)this.getMutableCopy();
		
		// Prepare a working copy for the collection
		ArrayList<ISymbol<? extends Value>> wCol = new ArrayList<ISymbol<? extends Value>>(c.size());
		for(ISymbol<? extends Value> e : c) {
			wCol.add(e.getWorkingCopy());
		}
		
		if(mutable.list.addAll(index, wCol)) {
			for(ISymbol<? extends Value> e : wCol) {
				mutable.notifyAdded(e);
			}
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean removeAll(Collection<?> c) {
		ClosedList<T> mutable = (ClosedList<T>)this.getMutableCopy();
		int size = mutable.list.size();
		for(Object o : c) {
			if(o instanceof ISymbol) {
				ISymbol<? extends Value> removed = ((ISymbol<? extends Value>)o).getWorkingCopy();
				if(mutable.list.remove(removed)) {
					mutable.notifyRemoved(removed);
				}
			}
		}
		return mutable.list.size() != size;
	}

	public boolean retainAll(Collection<?> c) {
		ClosedList<T> mutable = (ClosedList<T>)this.getMutableCopy();
		int size = mutable.list.size();
		
		Iterator<ISymbol<? extends Value>> iterator = mutable.list.iterator();
		while(iterator.hasNext()) {
			ISymbol<? extends Value> removed = iterator.next();
			if(!c.contains(removed)) {
				iterator.remove();
				mutable.notifyRemoved(removed);
			}
		}
		return mutable.list.size() != size;
	}

	public ISymbol<? extends Value> set(int index, ISymbol<? extends Value> element) {
		ClosedList<T> mutable = (ClosedList<T>)this.getMutableCopy();
		ISymbol<? extends Value> wElement = element.getWorkingCopy();
		ISymbol<? extends Value> old = mutable.list.set(index, wElement);
		mutable.notifyAdded(wElement);
		mutable.notifyRemoved(old);
		return old;
	}

	public void add(int index, ISymbol<? extends Value> element) {
		ClosedList<T> mutable = (ClosedList<T>)this.getMutableCopy();
		ISymbol<? extends Value> wElement = element.getWorkingCopy();
		mutable.list.add(index, wElement);
		mutable.notifyAdded(wElement);
	}

	public int indexOf(Object o) {
		if(o instanceof ISymbol) {
			ClosedList<T> wCopy = (ClosedList<T>)this.getWorkingCopy();
			return wCopy.list.indexOf(o);
		}
		return -1;
	}

	public int lastIndexOf(Object o) {
		if(o instanceof ISymbol) {
			ClosedList<T> wCopy = (ClosedList<T>)this.getWorkingCopy();
			return wCopy.list.lastIndexOf(o);
		}
		return -1;
	}

	@SuppressWarnings("unchecked")
	public Iterator<ISymbol<? extends Value>> iterator() {
		ClosedList<T> wCopy = (ClosedList<T>)this.getWorkingCopy();
		
		// We need to create clone of the backing list and return an iterator to the clone.
		// Otherwise the original list may itself be modified through cloning of its elements and
		// a ConcurrentModificationException will be thrown.
		return (Collections.unmodifiableList((ArrayList<ISymbol<? extends Value>>)wCopy.list.clone())).iterator();
	}

	@SuppressWarnings("unchecked")
	public ListIterator<ISymbol<? extends Value>> listIterator() {
		ClosedList<T> wCopy = (ClosedList<T>)this.getWorkingCopy();

		// We need to create clone of the backing list and return an iterator to the clone.
		// Otherwise the original list may itself be modified through cloning of its elements and
		// a ConcurrentModificationException will be thrown.
		return (Collections.unmodifiableList((ArrayList<ISymbol<? extends Value>>)wCopy.list.clone())).listIterator();
	}

	@SuppressWarnings("unchecked")
	public ListIterator<ISymbol<? extends Value>> listIterator(int index) {
		ClosedList<T> wCopy = (ClosedList<T>)this.getWorkingCopy();
		return ((ArrayList<ISymbol<? extends Value>>)wCopy.list.clone()).listIterator(index);
	}

	public List<ISymbol<? extends Value>> subList(int fromIndex, int toIndex) {
		ClosedList<T> wCopy = (ClosedList<T>)this.getWorkingCopy();
		ClosedList<T> newList = new ClosedList<T>(this.sootValue, toIndex - fromIndex);
		for(int i = fromIndex; i < toIndex; ++i) {
			newList.list.add(wCopy.list.get(i));
		}
		return newList;
	}

	/*************** Methods for supporting cloning ***************/
	@SuppressWarnings("unchecked")
	@Override
	protected void cloneInSub() {
		this.list = (ArrayList<ISymbol<? extends Value>>)this.list.clone();
	}

	@Override
	protected void replaceInSub(ISymbol<? extends Value> original, ISymbol<? extends Value> cloned) {
		int index = this.list.indexOf(original);
		if(index >= 0) {
			this.list.remove(index);
			this.list.add(index, cloned);
			this.notifyAdded(cloned);
			this.notifyRemoved(original);
		}
	}

	@Override
	protected Set<ISymbol<? extends Value>> getAllContainedInSub() {
		HashSet<ISymbol<? extends Value>> set = new HashSet<ISymbol<? extends Value>>(this.list.size());
		set.addAll(this.list);
		return set;
	}
}
