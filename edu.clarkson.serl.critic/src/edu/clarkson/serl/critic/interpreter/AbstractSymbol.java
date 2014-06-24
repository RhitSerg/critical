/*
 * AbstractSymbol.java
 * Jul 1, 2011
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.clarkson.serl.critic.interpreter.internal.HeapPointer;
import edu.clarkson.serl.critic.interpreter.internal.Stack;
import edu.clarkson.serl.critic.interpreter.internal.StackFrame;
import edu.clarkson.serl.critic.util.Util;




import soot.SootField;
import soot.Type;
import soot.Value;

/**
 * <p>
 * An implementation of {@link ISymbol}. It allows for mapping of an attribute to a symbol. 
 * Note that for storing any property of a symbolic object, it is critical that implementors 
 * use the {@link #put(ISymbol, ISymbol)} method rather then inventing the wheel. If the needs 
 * of a subclass cannot be fulfilled by the map data structure provided in this class, then
 * a read-only operation on those subclass specific data-structures must be done on the working 
 * copy of the <tt>this</tt> object (see {@link #getWorkingCopy()}) and a write operation must 
 * be done on the mutable copy (see {@link #getMutableCopy()}).
 * </p>
 * <p>
 * The subclass must also implement {@link #cloneInSub()}, {@link #replaceInSub(ISymbol, ISymbol)},
 * and {@link #getAllContainedInSub()} methods to work with the CriticAL framework. Whenever an 
 * object is added to the data structure specific to the subclass, the {@link #notifyAdded(ISymbol)}
 * method must be called on the working copy. Whenever an object is removed from the data structure
 * specific to the subclass, the {@link #notifyRemoved(ISymbol)} method must be called on the
 * working copy.
 * </p>
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 * @see {@link ISymbol}
 */
public abstract class AbstractSymbol<T extends Value> implements ISymbol<T> {
	protected T sootValue;
	protected int lineNumber;
	protected String outermostClass;
	protected HashMap<ISymbol<? extends Value>, ISymbol<? extends Value>> propertyToSymbolMap;
	private HashSet<IStackPointer> stackPointers;
	private HashSet<IHeapPointer> heapPointers;
	private HashSet<IStack> ownerStacks;
	private HashSet<SootField> staticFields;
	
	private static int count = 0;
	private int id;
	public static void reset() {
		count = 0;
	}
	
	public AbstractSymbol(T sootValue) {
		this.id = ++count;
		this.sootValue = sootValue;
		this.propertyToSymbolMap = new HashMap<ISymbol<? extends Value>, ISymbol<? extends Value>>(5);
		this.stackPointers = new HashSet<IStackPointer>();
		this.heapPointers = new HashSet<IHeapPointer>();
		this.ownerStacks = new HashSet<IStack>();
		this.staticFields = new HashSet<SootField>();
		
		Interpreter interpreter = Interpreter.instance();
		this.lineNumber = interpreter.getLineNumber();
		this.outermostClass = Util.getOuterMostClass(interpreter.getCurrentClass());
		
		// Make current stack owner of this object
		IStack stack = interpreter.peekStack(); 
		this.addStack(stack);

		
		// Some of the static objects will be initialized before execution
		// at which point a stack frame is not pushed in a stack.
		// It is IMPORTANT that such objects must be immutable.
		if(!stack.isEmpty()) {
			((StackFrame)stack.peek()).add(this);
		}
		
		// Add this symbolic object to the heap
		interpreter.addToHeap(this);
	}
	
	public int getLineNumber() {
		return this.lineNumber;
	}
	
	public String getOutermostClass() {
		return this.outermostClass;
	}
	
	public String getLocationString() {
		return "[" + this.outermostClass + ":" + this.lineNumber + "]";
	}
	
	public Type getType() {
		return this.sootValue.getType();
	}

	public T getSootValue() {
		return this.sootValue;
	}
	
	public ISymbol<T> getMutableCopy() {
		ISymbol<T> symbol = this.clone(Interpreter.instance().peekStack());
		return symbol;
	}
	
	@SuppressWarnings("unchecked")
	public ISymbol<T> getWorkingCopy() {
		// If this object cannot be modified then all of the stack will share the same copy
		// of this object.
		if(!this.isMutable())
			return this;
		
		// Lets check whether the this object belongs to the current execution stack, if so
		// then the this object is the working copy for the current stack.
		IStack stack = Interpreter.instance().peekStack();
		if(this.ownerStacks.contains(stack))
			return this;
		
		// The this object does not belong to the current execution stack, so it must have been
		// cloned for the current execution stack previously. Let's get the working copy from
		// the clone history. 
		CloneHistory history = CloneHistory.instance();
		HashSet<IStack> set = new HashSet<IStack>(1);
		set.add(stack);
		ISymbol<T> workingCopy = (ISymbol<T>)history.getInStacksSymbol(this, set);
		return workingCopy;
	}
	
	public ISymbol<T> getWorkingOrMutableCopy() {
		ISymbol<T> copy = this.getWorkingCopy();
		if(copy == null)
			copy = this.getMutableCopy();
		return copy;
	}
	
	public List<ISymbol<? extends Value>> getAttributes(ISymbol<? extends Value> value) {
		AbstractSymbol<? extends Value> wValue = (AbstractSymbol<? extends Value>)value.getWorkingCopy();
		AbstractSymbol<? extends Value> wCopy = (AbstractSymbol<? extends Value>)this.getWorkingCopy();
		ArrayList<ISymbol<? extends Value>> list = new ArrayList<ISymbol<? extends Value>>();
		for(Map.Entry<ISymbol<? extends Value>, ISymbol<? extends Value>> e : wCopy.propertyToSymbolMap.entrySet()) {
			if(e.getValue().equals(wValue))
				list.add(e.getKey());
		}
		return list;
	}

	public ISymbol<? extends Value> get(ISymbol<? extends Value> attribute) {
		AbstractSymbol<? extends Value> wAttribute = (AbstractSymbol<? extends Value>)attribute.getWorkingCopy();
		AbstractSymbol<? extends Value> wCopy = (AbstractSymbol<? extends Value>)this.getWorkingCopy();
		return wCopy.propertyToSymbolMap.get(wAttribute);
	}
	
	public ISymbol<? extends Value> removeEntry(ISymbol<? extends Value> attribute) {
		if(!this.isMutable())
			throw new UnsupportedOperationException("State of immutable object cannot be changed.");

		AbstractSymbol<? extends Value> wAttribute = (AbstractSymbol<? extends Value>)attribute.getWorkingCopy();
		AbstractSymbol<? extends Value> mutableCopy = (AbstractSymbol<? extends Value>)this.getMutableCopy();
		AbstractSymbol<? extends Value> removed = (AbstractSymbol<? extends Value>)mutableCopy.propertyToSymbolMap.remove(wAttribute);
		if(removed != null) {
			wAttribute.releaseLink(new HeapPointer(IHeapPointer.Type.Key, mutableCopy));
			removed.releaseLink(new HeapPointer(IHeapPointer.Type.Value, mutableCopy));
		}
		return removed;
	}
	
	public void removeAllEntries(ISymbol<? extends Value> value) {
		if(!this.isMutable())
			throw new UnsupportedOperationException("State of immutable object cannot be changed.");

		AbstractSymbol<? extends Value> mutableCopy = (AbstractSymbol<? extends Value>)this.getMutableCopy();
		AbstractSymbol<? extends Value> wValue = (AbstractSymbol<? extends Value>)value.getWorkingCopy();
		Iterator<Map.Entry<ISymbol<? extends Value>, ISymbol<? extends Value>>> itr = mutableCopy.propertyToSymbolMap.entrySet().iterator();
		while(itr.hasNext()) {
			Map.Entry<ISymbol<? extends Value>, ISymbol<? extends Value>> entry = itr.next();
			AbstractSymbol<? extends Value> k = (AbstractSymbol<? extends Value>)entry.getKey().getWorkingCopy();
			AbstractSymbol<? extends Value> v = (AbstractSymbol<? extends Value>)entry.getValue().getWorkingCopy();
			if(v.equals(wValue)) {
				itr.remove();
				k.releaseLink(new HeapPointer(IHeapPointer.Type.Key, mutableCopy));
				v.releaseLink(new HeapPointer(IHeapPointer.Type.Value, mutableCopy));
			}
		}
	}	
	
	public ISymbol<? extends Value> put(ISymbol<? extends Value> attribute, ISymbol<? extends Value> symbol) {
		if(!this.isMutable())
			throw new UnsupportedOperationException("State of immutable object cannot be changed.");
		
		AbstractSymbol<T> mutableCopy = (AbstractSymbol<T>)this.getMutableCopy();
		IHeapPointer keyPointer = new HeapPointer(IHeapPointer.Type.Key, mutableCopy);
		IHeapPointer valuePointer = new HeapPointer(IHeapPointer.Type.Value, mutableCopy);
		
		// Lets also get the working copy of attribute and symbol
		AbstractSymbol<? extends Value> wAttribute = (AbstractSymbol<? extends Value>)attribute.getWorkingOrMutableCopy();
		AbstractSymbol<? extends Value> wSymbol = (AbstractSymbol<? extends Value>)symbol.getWorkingOrMutableCopy();
		
		AbstractSymbol<? extends Value> removed =  (AbstractSymbol<? extends Value>)mutableCopy.propertyToSymbolMap.put(wAttribute, wSymbol);
		wAttribute.addLink(keyPointer);
		wSymbol.addLink(valuePointer);
		
		if(removed != null) {
			removed.releaseLink(new HeapPointer(IHeapPointer.Type.Value, mutableCopy));
		}
		return removed;
	}

	public Map<ISymbol<? extends Value>, ISymbol<? extends Value>> getPropertyValueMap() {
		AbstractSymbol<? extends Value> wCopy = (AbstractSymbol<? extends Value>)this.getWorkingCopy();
		return Collections.unmodifiableMap(wCopy.propertyToSymbolMap); 
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.toStringSimple() + "\n");
		if(this.propertyToSymbolMap != null && !this.propertyToSymbolMap.isEmpty()) {
			buffer.append("Key => Value:\n");
			buffer.append("-----------------------------\n");
			for(Map.Entry<ISymbol<? extends Value>, ISymbol<? extends Value>> e : this.propertyToSymbolMap.entrySet()) {
				if(e.getKey() instanceof AbstractSymbol)
					buffer.append(((AbstractSymbol<? extends Value>)e.getKey()).toStringSimple() + " => \n");
				else
					buffer.append(e.getKey() + " => \n");

				if(e.getValue() instanceof AbstractSymbol)
					buffer.append(((AbstractSymbol<? extends Value>)e.getValue()).toStringSimple() + "\n");
				else
					buffer.append(e.getValue() + "\n");
				buffer.append("----\n");
			}
		}
		return buffer.toString();
	}
	
	/**
	 * A simplified {@link #toString()} method.
	 * @return A simplified string for the <tt>this</tt> object.
	 */
	public String toStringSimple() {
		Object value = this.getValue();
		if(value != null && value != this)
			return "[Type: " + this.getType() + ", Value: " + value.toString() + "]";
		else
			return "[Type: " + this.getType() + ", Value: " + super.toString() + "]\n";
	}

	
	// IHeapObject related operation
	public Set<IPointer> getAllPointers() {
		AbstractSymbol<? extends Value> wCopy = (AbstractSymbol<? extends Value>)this.getWorkingCopy();
		HashSet<IPointer> pointers = new HashSet<IPointer>(wCopy.stackPointers.size() + wCopy.heapPointers.size());
		pointers.addAll(wCopy.stackPointers);
		pointers.addAll(wCopy.heapPointers);
		return Collections.unmodifiableSet(pointers);
	}

	public Set<IStackPointer> getStackPointers() {
		AbstractSymbol<? extends Value> wCopy = (AbstractSymbol<? extends Value>)this.getWorkingCopy();
		return Collections.unmodifiableSet(wCopy.stackPointers);
	}

	public Set<IHeapPointer> getHeapPointers() {
		AbstractSymbol<? extends Value> wCopy = (AbstractSymbol<? extends Value>)this.getWorkingCopy();
		return Collections.unmodifiableSet(wCopy.heapPointers);
	}

	public Set<IStackPointer> getStackPointers(IStack stack) {
		AbstractSymbol<? extends Value> wCopy = (AbstractSymbol<? extends Value>)this.getWorkingCopy();
		if(wCopy == null) {
			return Collections.emptySet();
		}
		
		HashSet<IStackPointer> pointers = new HashSet<IStackPointer>();
		for(IStackPointer p : wCopy.stackPointers) {
			if(p.getStack().equals(stack))
				pointers.add(p);
		}
		return Collections.unmodifiableSet(pointers);
	}

	public Set<IStack> getStacks() {
		return Collections.unmodifiableSet(this.ownerStacks);
	}
	
	/**
	 * Adds the supplied stack to the raw <tt>this</tt> object. Note that this method
	 * does not use the working copy. Furthermore, it also notifies the stack that
	 * an object want to be shared in the stack. 
	 * 
	 * @param stack The supplied execution stack.
	 * @return Return <tt>true</tt> if the stack was successfully added and <tt>false</tt> otherwise.
	 */
	public boolean addStack(IStack stack) {
		((Stack)stack).add(this);
		return this.ownerStacks.add(stack);
	}
	
	/**
	 * Removes the supplied stack from the set of the execution stacks that shares the <tt>this</tt> object.
	 * Note that this method does not use the working copy. Furthermore, it also notifies the stack that,
	 * the <tt>this</tt> object does not want to be shared by the stack anymore.
	 * 
	 * @param stack The supplied execution stack. 
	 * @return Return <tt>true</tt> if the stack was successfully removed and <tt>false</tt> otherwise.
	 */
	public boolean removeStack(IStack stack) {
		((Stack)stack).remove(this);
		boolean removed = this.ownerStacks.remove(stack);

		// This object is no more needed, so lets tell all of its children to 
		// remove the dangling pointer to it 
		if(this.ownerStacks.isEmpty()) {
			for(Map.Entry<ISymbol<? extends Value>, ISymbol<? extends Value>> e : this.propertyToSymbolMap.entrySet()) {
				AbstractSymbol<? extends Value> key = (AbstractSymbol<? extends Value>)e.getKey();
				AbstractSymbol<? extends Value> value = (AbstractSymbol<? extends Value>)e.getValue();
				key.releaseAllHeapPointers(this);
				value.releaseAllHeapPointers(this);
			}
			
			for(ISymbol<? extends Value> child : this.getAllContainedInSub()) {
				((AbstractSymbol<? extends Value>)child).releaseAllHeapPointers(this);
			}
		}
		
		return removed;
	}
	
	public Set<SootField> getStaticFields() {
		AbstractSymbol<? extends Value> wCopy = (AbstractSymbol<? extends Value>)this.getWorkingCopy();
		return Collections.unmodifiableSet(wCopy.staticFields);
	}
	
	/**
	 * Adds a static field that points to the <tt>this</tt> object.
	 * 
	 * @param field
	 * @return
	 */
	public boolean addStaticField(SootField sootField) {
		AbstractSymbol<? extends Value> wCopy = (AbstractSymbol<? extends Value>)this.getWorkingCopy();
		return wCopy.staticFields.add(sootField);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ISymbol<T> clone() {
		if(!this.isMutable())
			return this;
		
		try {
			AbstractSymbol<T> clone = (AbstractSymbol<T>)super.clone();
			Interpreter.instance().addToHeap(clone);
			return clone;
		}
		catch(CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public ISymbol<T> clone(IStack iStack) {
		// Note that immutable objects will not carry stack pointer because
		// they cannot be changed in any program stack, so no need to clone them.
		if(!this.isMutable())
			return this;
		
		// The this object is immutable. Lets check if the this object belongs to the 
		// supplied stack only. If so, we do not need to do cloning.
		if(this.ownerStacks.size() == 1 && this.ownerStacks.contains(iStack))
			return this;

		// Before we proceed with cloning, let us check if we have already cloned this object
		// for the current iStack. We will need the clone history object that maintains
		// the history of cloning for the current execution stack for lookup.
		final CloneHistory history = CloneHistory.instance(); 

		// Now lets check in the clone history object if we have already cloned the this object
		// for the supplied iStack object. If so, we will return the clone.
		HashSet<IStack> set = new HashSet<IStack>(1);
		set.add(iStack);
		AbstractSymbol<T> prevClone = (AbstractSymbol<T>)history.getInStacksSymbol(this, set);
		if(prevClone != null && prevClone.ownerStacks.size() == 1)
			return (ISymbol<T>)prevClone;
		
		// We need to perform cloning
		Stack stack = (Stack)iStack;
		AbstractSymbol<T> cloned = (AbstractSymbol<T>)this.clone();
		Set<IStackPointer> pointers = this.getStackPointers(stack);
		
		// Add the pair to the clone history to be used later
		history.add(this, cloned);
		
		// If this object is pointed by static field then stack should update it
		cloned.staticFields = (HashSet<SootField>)this.staticFields.clone();
		for(SootField f : cloned.staticFields) {
			stack.put(f, cloned);
		}

		// Initialize fresh container for stack pointers 
		cloned.stackPointers = new HashSet<IStackPointer>(pointers.size());
		
		// Lets release the stack pointer associated to stack from the this object as stack owns cloned.
		// Lets make the top stack frame of stack know that the this object has been cloned.
		// The Stack frame will take the responsibility of linking necessary stack pointers
		// to the cloned object
		StackFrame frame = (StackFrame)stack.peek();
		for(IStackPointer pointer : pointers) {
			if(pointer.getStackFrame().equals(frame)) {
				this.releaseLink(pointer);
			}
			Value local = pointer.getLocal();
			frame.put(local, cloned);
		}
		frame.replace(this, cloned);
		this.removeStack(stack);
		cloned.ownerStacks = new HashSet<IStack>(5);
		cloned.addStack(stack);
		
		// Symbolic objects are shared as much as possible. This cloning is shallow, i.e. the elements are not cloned
		cloned.propertyToSymbolMap = (HashMap<ISymbol<? extends Value>, ISymbol<? extends Value>>)this.propertyToSymbolMap.clone();
		
		// Tell the children that they need to add one more heap pointer to the cloned parent beside the this object
		for(Map.Entry<ISymbol<? extends Value>, ISymbol<? extends Value>> e : this.propertyToSymbolMap.entrySet()) {
			ISymbol<? extends Value> key = e.getKey();
			ISymbol<? extends Value> value = e.getValue();
			
			IHeapPointer keyPointer = new HeapPointer(IHeapPointer.Type.Key, cloned);
			IHeapPointer valuePointer = new HeapPointer(IHeapPointer.Type.Value, cloned);
			
			((AbstractSymbol<? extends Value>)key).addLink(keyPointer);
			((AbstractSymbol<? extends Value>)value).addLink(valuePointer);
		}
		
		// Lets perform the cloning of the custom data structure provided in subtypes
		cloned.cloneInSub();
		for(ISymbol<? extends Value> e : cloned.getAllContainedInSub()) {
			IHeapPointer pointer = new HeapPointer(IHeapPointer.Type.Custom, cloned);
			((AbstractSymbol<? extends Value>)e).addLink(pointer);
		}
		
		// Clone will share the same heap pointers 
		cloned.heapPointers = (HashSet<IHeapPointer>)this.heapPointers.clone();

		// We need to propagate the cloning effect to all of the parent containers as well.
		// But lets ensure that we do so once for each pair of clones rather than repeating
		// the same operation twice. In order to isolate a pointer for each pair, let us 
		// use the following tree set with the supplied comparator.
		TreeSet<IHeapPointer> uniquePairPointers = new TreeSet<IHeapPointer>(new Comparator<IHeapPointer>() {
			@Override
			public int compare(IHeapPointer p1, IHeapPointer p2) {
				if(history.contains(p1.getContainer(), p2.getContainer()))
					return 0;
				return -1;
			}
		});
		uniquePairPointers.addAll(cloned.heapPointers);
		for(IHeapPointer p : uniquePairPointers) {
			AbstractSymbol<? extends Value> container = (AbstractSymbol<? extends Value>)p.getContainer();
			
			// We need to clone parent as well
			container.cloned(stack, p, this, cloned);
		}
		
		return cloned;
	}
	
	/**
	 * This method is responsible for cloning the container of a recently cloned symbolic object.
	 * 
	 * @param stack The execution stack that has cloned the child of the <tt>this</tt> object.
	 * @param pointerToMe The heap pointer that points to the <tt>this</tt> object from the recently cloned child.
	 * @param originalChild The original child.
	 * @param clonedChild The clone of the original child.
	 */
	private void cloned(IStack stack, IHeapPointer pointerToMe, AbstractSymbol<? extends Value> originalChild, AbstractSymbol<? extends Value> clonedChild) {
		// Get the clone history object for cloning that has been done so far starting at a child symbol
		CloneHistory history = CloneHistory.instance();
		
		// The this object is the container or parent of a cloned child
		// Let's make sure that the this object is shared by more than one stacks before cloning it
		if(this.ownerStacks.size() > 1 && this.ownerStacks.contains(stack)) {
			// Now clone the parent i.e. the this object
			this.clone(stack);
		}

		boolean shared = true;
		AbstractSymbol<? extends Value> myClone = (AbstractSymbol<? extends Value>)history.getOutOfStacksSymbol(this, this.getStacks());
		if(myClone == null)
			shared = false;
		
		// Lets establish proper internal structure and pointers based on the clone history now 
		AbstractSymbol<? extends Value> wrongContainer;
		HeapPointer tempPointer;
		if(shared) {
			// The this object is shared.
			// The originalChild object will have at least two heap pointers:
			// one to the this object and another to the myClone object
			// Let us fix this situation first
			wrongContainer = (AbstractSymbol<? extends Value>)history.getOutOfStacksSymbol(this, originalChild.getStacks());
			tempPointer = new HeapPointer(pointerToMe.getType(), wrongContainer);
			originalChild.releaseLink(tempPointer);

			// The cloned child still contains a heap pointer to the original parent.
			// Let us delete that pointer and establish the pointer to the cloned child
			wrongContainer = (AbstractSymbol<? extends Value>)history.getOutOfStacksSymbol(this, clonedChild.getStacks());
			tempPointer = new HeapPointer(pointerToMe.getType(), wrongContainer);
			clonedChild.releaseLink(tempPointer);
			AbstractSymbol<? extends Value> rightContainer = (AbstractSymbol<? extends Value>)history.getInStacksSymbol(this, clonedChild.getStacks());
			tempPointer = new HeapPointer(pointerToMe.getType(), rightContainer);
			clonedChild.addLink(tempPointer);
		}
		else {
			// If the this object is not shared by stack, then only clonedChild should maintain a pointer to this object
			tempPointer = new HeapPointer(pointerToMe.getType(), this);
			originalChild.releaseLink(tempPointer);

			// Cloned child already has a heap pointer to the this object, so we do not need to release anything
		}
		
		// Update the property for the this object to include the correct child
		// that was created due to cloning
		AbstractSymbol<? extends Value> keep, remove;
		if(this.getStacks().equals(originalChild.getStacks())) {
			keep = (AbstractSymbol<? extends Value>)originalChild;
			remove = (AbstractSymbol<? extends Value>)clonedChild;
		}
		else {
			remove = (AbstractSymbol<? extends Value>)originalChild;
			keep = (AbstractSymbol<? extends Value>)clonedChild;
		}
		tempPointer = new HeapPointer(pointerToMe.getType(), this);
		replace(this, keep, remove, tempPointer);
		
		// Now lets also update the property for the clone of the this object to include the correct child if one exist
		if(shared) {
			tempPointer = new HeapPointer(pointerToMe.getType(), myClone);
			replace(myClone, remove, keep, tempPointer);
		}
	}
	
	private static void replace(AbstractSymbol<? extends Value> container, AbstractSymbol<? extends Value> keep, 
			AbstractSymbol<? extends Value> remove, IHeapPointer pointer) {
		// Get the pointer type
		IHeapPointer.Type type = pointer.getType();
		
		if(type == IHeapPointer.Type.Key) {
			ISymbol<? extends Value> value = container.propertyToSymbolMap.remove(remove);
			container.propertyToSymbolMap.put(keep, value);
			keep.addLink(pointer);
		}
		else if (type == IHeapPointer.Type.Value) {
			Iterator<Map.Entry<ISymbol<? extends Value>, ISymbol<? extends Value>>> mIterator = container.propertyToSymbolMap.entrySet().iterator();
			while(mIterator.hasNext()) {
				Map.Entry<ISymbol<? extends Value>, ISymbol<? extends Value>> entry = mIterator.next();
				if(remove.equals(entry.getValue())) {
					entry.setValue(keep);
					keep.addLink(pointer);
				}
			}
		}
		else { // Must be IHeapPointer.Type.Custom
			container.replaceInSub(remove, keep);
			keep.addLink(pointer);
		}		
	}
	
	public Set<ISymbol<? extends Value>> getAllContained() {
		AbstractSymbol<? extends Value> wCopy = (AbstractSymbol<? extends Value>)this.getWorkingCopy();
		Set<ISymbol<? extends Value>> keySet = wCopy.propertyToSymbolMap.keySet();
		Collection<ISymbol<? extends Value>> values = wCopy.propertyToSymbolMap.values();
		HashSet<ISymbol<? extends Value>> contained = new HashSet<ISymbol<? extends Value>>(keySet.size() + values.size());
		contained.addAll(keySet);
		contained.addAll(values);
		contained.addAll(wCopy.getAllContainedInSub());
		return contained;
	}

	public Set<ISymbol<? extends Value>> getContainedReflexiveTransitiveClosure() {
		HashSet<ISymbol<? extends Value>> closure = new HashSet<ISymbol<? extends Value>>();
		dfsContainedClosure(this, closure);
		return closure;
	}
	private static void dfsContainedClosure(ISymbol<? extends Value> s, HashSet<ISymbol<? extends Value>> closure) {
		AbstractSymbol<? extends Value> symbol = (AbstractSymbol<? extends Value>)s.getWorkingCopy();
		if(closure.contains(symbol))
			return;
		closure.add(symbol);
		for(ISymbol<? extends Value> child : symbol.getAllContained()) {
			dfsContainedClosure(child, closure);
		}
	}
	
	public Set<ISymbol<? extends Value>> getContainedTransitiveClosure() {
		Set<ISymbol<? extends Value>> closure = this.getContainedReflexiveTransitiveClosure();
		AbstractSymbol<? extends Value> wCopy = (AbstractSymbol<? extends Value>)this.getWorkingCopy();
		closure.remove(wCopy);
		return closure;
	}
	
	
	public Set<ISymbol<? extends Value>> getAllContainers() {
		AbstractSymbol<? extends Value> wCopy = (AbstractSymbol<? extends Value>)this.getWorkingCopy();
		// A null value may be possible when getAllContainer() is called recursively through reverse heap pointers.
		// A scenario: Say current stack is s1, child is shared by two stack say s1 and s2, but its heap 
		// pointer points to a parent container p, which only belongs to s2. In this case working copy is null.
		if(wCopy == null) 
			return Collections.emptySet();
		
		HashSet<ISymbol<? extends Value>> containers = new HashSet<ISymbol<? extends Value>>(wCopy.heapPointers.size());
		for(IHeapPointer p : wCopy.heapPointers) {
			// Lets only add parent that is shared by the current stack
			AbstractSymbol<? extends Value> container = (AbstractSymbol<? extends Value>)p.getContainer().getWorkingCopy();
			if(container != null)
				containers.add(container);
		}
		return containers;
	}
	
	public Set<ISymbol<? extends Value>> getContainerReflexiveTransitiveClosure() {
		HashSet<ISymbol<? extends Value>> closure = new HashSet<ISymbol<? extends Value>>();
		AbstractSymbol<? extends Value> wCopy = (AbstractSymbol<? extends Value>)this.getWorkingCopy();
		dfsContainerClosure(wCopy, closure);
		return closure;
	}
	private static void dfsContainerClosure(ISymbol<? extends Value> symbol, HashSet<ISymbol<? extends Value>> closure) {
		if(closure.contains(symbol))
			return;
		closure.add(symbol);
		for(ISymbol<? extends Value> parent : symbol.getAllContainers()) {
			dfsContainerClosure(parent, closure);
		}
	}

	public Set<ISymbol<? extends Value>> getContainerTransitiveClosure() {
		Set<ISymbol<? extends Value>> closure = this.getContainerReflexiveTransitiveClosure();
		AbstractSymbol<? extends Value> wCopy = (AbstractSymbol<? extends Value>)this.getWorkingCopy();
		closure.remove(wCopy);
		return closure;
	}
	
	
	/**
	 * Adds the pointer to the stack or the container of the <tt>this</tt> object. Note that if it is a 
	 * stack pointer then all of the heap subtree will also be shared by the stack pointed by the supplied
	 * pointer.
	 *  
	 * @param pointer A heap or a stack pointer.
	 * @return Returns <tt>true</tt> if successfully added else returns <tt>false</tt>.
	 */
	public boolean addLink(IPointer pointer) {
		if(pointer instanceof IHeapPointer) {
			return this.heapPointers.add((IHeapPointer)pointer);
		}
		else if(pointer instanceof IStackPointer) {
			IStackPointer sPointer = (IStackPointer)pointer;
			dfsAddStackOwners(this, sPointer.getStack());
			return this.stackPointers.add(sPointer);
		}
		return false;
	}
	
	private static void dfsAddStackOwners(AbstractSymbol<? extends Value> symbol, IStack stack) {
		if(symbol.getStacks().contains(stack))
			return;
		
		symbol.addStack(stack);
		
		for(ISymbol<? extends Value> child : symbol.getAllContained()) {
			dfsAddStackOwners((AbstractSymbol<? extends Value>)child, stack);
		}
	}

	/**
	 * Removes the supplied pointer from the set of heap or stack pointers.
	 * 
	 * @param pointer The pointer to be removed.
	 * @return Returns <tt>true</tt> if successfully removed else returns <tt>false</tt>.
	 */
	public boolean releaseLink(IPointer pointer) {
		if(pointer instanceof IHeapPointer)
			return this.heapPointers.remove(pointer);
		else if(pointer instanceof IStackPointer)
			return this.stackPointers.remove(pointer);
		return false;
	}
	
	/**
	 * Releases all of the heap pointers pointing to the container from <tt>this</tt> object.
	 * @param container The supplied container.
	 */
	public void releaseAllHeapPointers(ISymbol<? extends Value> container) {
		Iterator<IHeapPointer> iterator = this.heapPointers.iterator();
		while(iterator.hasNext()) {
			IHeapPointer pointer = iterator.next();
			if(pointer.getContainer().equals(container))
				iterator.remove();
		}
	}
	
	/**
	 * Notifies the {@link AbstractSymbol} that an element has been added in the
	 * data structure specific to the subclass.
	 * 
	 * @param symbol The element that is added.
	 */
	protected void notifyAdded(ISymbol<? extends Value> symbol) {
		IHeapPointer pointer = new HeapPointer(IHeapPointer.Type.Custom, this);
		((AbstractSymbol<? extends Value>)symbol).addLink(pointer);
	}
	
	/**
	 * Notifies the {@link AbstractSymbol} that an element has been removed from the
	 * data structure specific to the subclass.
	 * 
	 * @param symbol The element that is removed.
	 */
	protected void notifyRemoved(ISymbol<? extends Value> symbol) {
		Iterator<IHeapPointer> iterator = ((AbstractSymbol<? extends Value>)symbol).heapPointers.iterator();
		while(iterator.hasNext()) {
			IHeapPointer p = iterator.next();
			if(this.equals(p.getContainer())) {
				iterator.remove();
			}
		}
	}

	// Subclasses must override the following three methods in order to make the framework work properly
	/**
	 * Clone data structures specific to a subclass which has been shallow copied so far.
	 */
	protected abstract void cloneInSub();
	
	/**
	 * Replace the original object with the cloned object in the subclass if these objects are
	 * held in data structures specific to the subclass. Note that after replacing the elements,
	 * this function must call <tt>this.notifyAdded(cloned); this.notifyRemoved(original)</tt> 
	 * to notify {@link AbstractSymbol} that a new element has been added in a container specific 
	 * to the subclass.
	 * 
	 * @param original The original object
	 * @param cloned The cloned version of the original object
	 * @see #notifyAdded(ISymbol)
	 * @see #notifyRemoved(ISymbol)
	 */
	protected abstract void replaceInSub(ISymbol<? extends Value> original, ISymbol<? extends Value> cloned);
	
	/**
	 * Subclasses must return all of the elements contained in data structures specific to the subclass
	 * through this method. If a subclass do not use any extra data structure than available from
	 * {@link AbstractSymbol}, then it must return an empty set.
	 * 
	 * @return A set of symbolic elements contained in the data structure specific to the subclass.
	 */
	protected abstract Set<ISymbol<? extends Value>> getAllContainedInSub();

}
