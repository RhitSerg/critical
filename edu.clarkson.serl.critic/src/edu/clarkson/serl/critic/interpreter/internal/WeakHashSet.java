/*
 * WeakHashSet.java
 * Jul 6, 2011
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
 
package edu.clarkson.serl.critic.interpreter.internal;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * WeakHashSet is implemented as a subtype of {@link AbstractSet}. It is backed-up by a 
 * {@link WeakHashMap} in the same way as a {@link java.util.HashSet} is backed-up by a 
 * {@link java.util.HashMap}. Whenever an element in the set loses all of its strong 
 * references and only a weak reference from this set to the element exists, then it 
 * will be automatically garbage collected in the next garbage collection cycle. 
 * Hence, the behavior of the set such as {@link #size()}, {@link #contains(Object)}, 
 * and so on may not be deterministic.
 * 
 * @param <E> The element type of this set.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 * @see {@link WeakHashMap}
 * @see {@link java.lang.ref.WeakReference}
 */
public class WeakHashSet<E> extends AbstractSet<E> {
	// The dummy static object that will be used as value in all key-value pair
    private static final Object DUMMY = new Object();
    
    // The backing WeakHashMap
    private WeakHashMap<E,Object> map;

    /**
     * Constructs a <tt>WeakHashSet</tt> object using the default load factor of <tt>0.75</tt> and
     * initial capacity of <tt>16</tt>.
     */
    public WeakHashSet() {
    	map = new WeakHashMap<E,Object>();
    }

    /**
     * Constructs a <tt>WeakHashSet</tt> object by adding the elements in the supplied collection.
     * 
     * @param c The initial collection.
     */
    public WeakHashSet(Collection<? extends E> c) {
    	map = new WeakHashMap<E,Object>();
    	addAll(c);
    }

    /**
     * Constructs a <tt>WeakHashSet</tt> object with the supplied initial capacity and
     * using the default load factor of <tt>0.75</tt>.
     * 
     * @param initialCapacity The initial capacity of the heap.
     */
    public WeakHashSet(int initialCapacity) {
    	map = new WeakHashMap<E,Object>(initialCapacity);
    }	

    /**
     * Constructs a <tt>WeakHashSet</tt> object using the supplied initial capacity and load factor.
     * 
     * @param initialCapacity Initial capacity.
     * @param loadFactor Load factor.
     */
    public WeakHashSet(int initialCapacity, float loadFactor) {
    	map = new WeakHashMap<E,Object>(initialCapacity, loadFactor);
    }

	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	public boolean add(E o) {
		return map.put(o, DUMMY) == null;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean remove(Object o) {
		return map.remove(o) == DUMMY;
	}
}
