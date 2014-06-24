/*
 * Stack.java
 * Aug 10, 2011
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.clarkson.serl.critic.interpreter.AbstractSymbol;
import edu.clarkson.serl.critic.interpreter.CallbackPoint;
import edu.clarkson.serl.critic.interpreter.ICallbackEntry;
import edu.clarkson.serl.critic.interpreter.ICallbackPoint;
import edu.clarkson.serl.critic.interpreter.IStack;
import edu.clarkson.serl.critic.interpreter.IStackFrame;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.Path;

import soot.SootField;
import soot.Value;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class Stack implements IStack, Cloneable {
	private HashMap<SootField, ISymbol<? extends Value>> staticFieldToSymbolMap;
	private LinkedList<IStackFrame> list;
	private Path path;
	
	private HashSet<ICallbackPoint> callBackSet;
	private ArrayList<ICallbackEntry> callBackEntries;
	
	private HashSet<ISymbol<? extends Value>> symbols;
	
	public Stack() {
		list = new LinkedList<IStackFrame>();
		this.path = new Path();
		this.staticFieldToSymbolMap = new HashMap<SootField, ISymbol<? extends Value>>();
		this.callBackSet = new HashSet<ICallbackPoint>();
		this.symbols = new HashSet<ISymbol<? extends Value>>();
	}
	
	public boolean isEmpty() {
		return this.list.isEmpty();
	}

	public IStackFrame peek() {
		return this.list.getLast();
	}
	
	public int size() {
		return this.list.size();
	}

	public IStackFrame pop() {
		StackFrame frame = (StackFrame)this.list.removeLast();
		frame.firePoped();
		return frame;
	}

	public void push(IStackFrame frame) {
		this.list.addLast(frame);
	}
	
	public boolean add(ISymbol<? extends Value> symbol) {
		return this.symbols.add(symbol);
	}
	
	public boolean remove(ISymbol<? extends Value> symbol) {
		return this.symbols.remove(symbol);
	}

	public Path getPath() {
		return this.path;
	}
	
	public List<IStackFrame> getList() {
		return Collections.unmodifiableList(this.list);
	}
	
	public LinkedList<IStackFrame> getLinkedList() {
		return this.list;
	}


	public ISymbol<? extends Value> lookup(Value local) {
		return this.list.getLast().lookup(local);
	}
	
	public ISymbol<? extends Value> lookup(SootField field) {
		return this.staticFieldToSymbolMap.get(field);
	}
	
	public ISymbol<? extends Value> put(SootField field, ISymbol<? extends Value> symbol) {
		((AbstractSymbol<? extends Value>)symbol).addStaticField(field);
		return this.staticFieldToSymbolMap.put(field, symbol);
	}

	public boolean addCallback(ICallbackPoint e) {
		return this.callBackSet.add(e);
	}
	
	public boolean removeCallback(ICallbackPoint e) {
		return this.callBackSet.remove(e);
	}
	
	public Set<ICallbackPoint> getCallbackSet() {
		return Collections.unmodifiableSet(this.callBackSet);
	}
	
	public void computeCallbackEntries() {
		this.callBackEntries = new ArrayList<ICallbackEntry>(this.callBackSet.size() * 3);
		for(ICallbackPoint p : this.callBackSet) {
			CallbackPoint cPoint = (CallbackPoint)p;
			this.callBackEntries.addAll(cPoint.computeCallbackEntries());
		}
	}
	
	public ICallbackEntry popCallbackEntry() {
		if(this.callBackEntries == null)
			this.computeCallbackEntries();
		
		if(this.callBackEntries.isEmpty())
			return null;
		
		return this.callBackEntries.remove(0);
	}
	
//	private static int clones = 0;
	@SuppressWarnings("unchecked")
	public Stack clone() {
//		++clones;
//		System.out.println("Cloned: " + clones);
		Stack clone = null;
		try {
			clone = (Stack)super.clone();
		}
		catch(CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		clone.list = new LinkedList<IStackFrame>();
		for(IStackFrame s : this.list) {
			IStackFrame clonedFrame = ((StackFrame)s).clone(clone);
			clone.list.add(clonedFrame);
		}

		// Clone field to symbol map and let all symbolic objects know that they are shared by this and clone
		clone.staticFieldToSymbolMap = (HashMap<SootField, ISymbol<? extends Value>>)this.staticFieldToSymbolMap.clone();
		for(ISymbol<? extends Value> symbol : clone.staticFieldToSymbolMap.values()) {
			((AbstractSymbol<? extends Value>)symbol).addStack(clone);
		}
		
		// Clone symbols set
		clone.symbols = new HashSet<ISymbol<? extends Value>>(this.symbols.size());
		for(ISymbol<? extends Value> symbol : this.symbols) {
			// Automatically adds to the clone.symbols object by calling the following method
			((AbstractSymbol<? extends Value>)symbol).addStack(clone); 
		}
		
		// Call back set cloning
		clone.callBackSet = (HashSet<ICallbackPoint>)this.callBackSet.clone();

		clone.path = (Path)this.path.clone();
		return clone;
	}
	
	/**
	 * <p>
	 * The heap uses weak references (<tt>WeakHashSet</tt>) to maintain a collection
	 * of symbolic object. A symbolic object in the heap which do not have a pointer 
	 * from outside the heap gets garbage collected and deleted from the heap
	 * automatically. Hence, it is not wise to have a dangling pointer from an 
	 * obsolete stack to the symbolic object and effect the garbage collection
	 * behavior of the heap.
	 * </p>
	 */
	public void poped() {
		@SuppressWarnings("unchecked")
		HashSet<ISymbol<? extends Value>> symbolsClone = (HashSet<ISymbol<? extends Value>>)this.symbols.clone();
		for(ISymbol<? extends Value> symbol : symbolsClone) {
			// Automatically removes the symbol object from this.symbols.
			((AbstractSymbol<? extends Value>) symbol).removeStack(this);
		}
	}

	public Collection<ISymbol<? extends Value>> getStaticFields() {
		return Collections.unmodifiableCollection(this.staticFieldToSymbolMap.values());
	}
}
