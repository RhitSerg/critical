/*
 * ISymbol.java
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.SootField;
import soot.Type;
import soot.Value;

/**
 * <p>
 * Represents a symbolic object. For storing any property of a symbolic object, it is critical
 * that implementors use the {@link #put(ISymbol, ISymbol)} method. Rather then implementing
 * this interface directly, it is strongly recommended to subclass a new symbolic object from
 * the {@link AbstractSymbol} class that provides a skeleton for these objects to work with 
 * the CriticAL framework.
 * </p>
 * <p>
 * The {@link AbstractSymbol} class provides a means for lazy initialization of 
 * mutable symbolic objects through {@link #isMutable()}, {@link #getMutableCopy()}, 
 * {@link #getWorkingCopy()}, and {@link #clone(IStack)} methods.
 * A symbolic object is mutable if it can be modified ({@link #isMutable()}) and it is open
 * if we do not know its concrete value ({@link #isOpen()}).  
 * </p>
 * <p>
 * Note that all of the interface methods provided by {@link ISymbol} uses the working copy
 * version of the <tt>this</tt> object for read-only operations (see {@link #getWorkingCopy()})
 * and the mutable copy version of the <tt>this</tt> object for write operations 
 * (see {@link #getMutableCopy()}). A subclass of {@link AbstractSymbol} must also follow the 
 * same strategy in order to work with the CriticAL framework. We need to use working or mutable 
 * copies because the <tt>this</tt> pointer may still point to the previous copy of the symbolic 
 * object which may have been recently cloned for the current execution stack due to the 
 * implementation logic used in extension classes.
 * </p>
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public interface ISymbol<T extends Value> extends Cloneable {
	/**
	 * Returns the {@link Type} of this symbol.
	 * @return
	 */
	public Type getType();
	
	/**
	 * Returns the value this objects backs up.
	 * @return 
	 */
	public T getSootValue();

	/**
	 * Returns whether the value for this symbolic object was created using the new operator or 
	 * was retrieved as an open object, e.g., parameter of an entry method.
	 * 
	 * @return
	 */
	public boolean isOpen();
	
	/**
	 * Returns whether the state of this symbolic object can be modified.
	 * 
	 * @return <tt>true</tt> if modifiable and <tt>false</tt> otherwise.
	 */
	public boolean isMutable();
	
	
	/**
	 * Returns the line number where this symbolic object was created.
	 * @return
	 */
	public int getLineNumber();
	
	/**
	 * Returns the outermost class of the class that creates this symbolic object.
	 * This can be used to locate the object creation site in the source code.
	 * 
	 * @return The qualified name of the outer most class of the class that creates this object.
	 */
	public String getOutermostClass();
	
	/**
	 * @return Returns a location string that combines outermost class and line number. e.g [className:lineNumber]
	 */
	public String getLocationString();
		
	/**
	 * Returns the concrete java object representation for this object if any.
	 * @return
	 */
	public Object getValue();
	
	/**
	 * Returns the symbol corresponding to the supplied attributes if stored previously, 
	 * otherwise returns null.
	 * 
	 * @param attribute The attribute for look up. 
	 * @return
	 */
	public ISymbol<? extends Value> get(ISymbol<? extends Value> attribute);
	
	/**
	 * Gets a list of attribute (or key) that maps to the supplied value in this
	 * symbolic object.
	 * 
	 * @param value The value to be searched.
	 * @return A list of attributes.
	 */
	public List<ISymbol<? extends Value>> getAttributes(ISymbol<? extends Value> value);
	
	/**
	 * Stores a mapping between an attribute and a symbol for this symbolic object.
	 * If this object is a proxy to another object then the backing map must be cloned
	 * in order for the proxy object to morph into a new real object.
	 * See {@link AbstractSymbol#put(ISymbol, ISymbol)} method for a sample implementation.
	 * 
	 * @param attribute The attribute as a key to be stored
	 * @param symbol The corresponding symbol to be associated with the attribute.
	 * @return Previously associated symbol if any for the given attribute before overwrite.
	 * @see #isProxy()
	 * @see #getProxy()
	 * @see #makeItReal()
	 */
	public ISymbol<? extends Value> put(ISymbol<? extends Value> attribute, ISymbol<? extends Value> symbol);
	
	/**
	 * Returns the backing {@link Map} that stores mapping between attributes and symbols.
	 * @return
	 */
	public Map<ISymbol<? extends Value>, ISymbol<? extends Value>> getPropertyValueMap();
	
	/**
	 * Executes the expression to reduce it to a simple symbolic form if possible else
	 * returns itself. This method tries to perform eager concretization whenever possible.
	 * 
	 * @return
	 */
	public ISymbol<? extends Value> execute();
	
	/**
	 * Returns all of the symbolic object directly contained within itself.
	 * e.g. <tt>a->b, a->c->d => {b,c}</tt>
	 * @return
	 */
	public Set<ISymbol<? extends Value>> getAllContained();
	
	/**
	 * Returns the transitive closure of the objects contained within itself.
	 * e.g. <tt>a->b, a->c->d => {b,c,d}</tt>
	 * @return
	 */
	public Set<ISymbol<? extends Value>> getContainedTransitiveClosure();
	
	/**
	 * Returns the reflexive transitive closure of the objects contained within itself.
	 * e.g. <tt>a->b, a->c->d => {a,b,c,d}</tt>
	 * @return
	 */
	public Set<ISymbol<? extends Value>> getContainedReflexiveTransitiveClosure();

	/**
	 * Returns all of the symbolic object directly containing the <tt>this</tt> object.
	 * e.g. <tt>b<-a, d<-c<-a => {b,c}</tt>
	 * @return
	 */
	public Set<ISymbol<? extends Value>> getAllContainers();

	/**
	 * Returns the transitive closure of all of the containers of the <tt>this</tt> object.
	 * e.g. <tt>b<-a, d<-c<-a => {b,c,d}</tt>
	 * @return
	 */
	public Set<ISymbol<? extends Value>> getContainerTransitiveClosure();

	/**
	 * Returns the reflexive transitive closure of all of the containers of the <tt>this</tt> object.
	 * e.g. <tt>b<-a, d<-c<-a => {a,b,c,d}</tt>
	 * @return
	 */
	public Set<ISymbol<? extends Value>> getContainerReflexiveTransitiveClosure();
	

	/**
	 * Gets the set of all of the stack pointers pointing to the supplied stack.
	 * @param stack The supplied stack.
	 * @return The set of stack pointers.
	 */
	public Set<IStackPointer> getStackPointers(IStack stack);
	
	/**
	 * Returns all of the stack and heap pointers that the <tt>this</tt> object points to. 
	 * @return A set of stack and heap pointers.
	 */
	public Set<IPointer> getAllPointers();
	
	/**
	 * Returns a set of stack pointers.
	 * @return A set of {@link IStackPointer}s.
	 */
	public Set<IStackPointer> getStackPointers();

	/**
	 * Returns a set of heap pointers.
	 * @return A set of {@link IHeapPointer}s.
	 */
	public Set<IHeapPointer> getHeapPointers();
	
	/**
	 * Gets a set of execution stacks that the <tt>this</tt> object is shared in.
	 * Note that this method does not use the working copy for returning the stacks.
	 * @return A set of {@link IStack}.
	 */
	public Set<IStack> getStacks();
	
	/**
	 * Returns a set of static fields that points to the <tt>this</tt> object.
	 * @return A set of static fields.
	 */
	public Set<SootField> getStaticFields();

	/**
	 * <p>
	 * This method clones the current object for the current execution stack.
	 * <b>NOTE:</b> If you need a mutable version of the <tt>this</tt> object, 
	 * then use {@link #getMutableCopy()} rather than {@link #clone(IStack)} or {@link #clone()}.
	 * </p>
	 * <p>
	 * For a subclass implementing a custom data structure other than the map data structure provided
	 * by {@link AbstractSymbol}, user can perform the custom data structure specific cloning by using
	 * {@link AbstractSymbol#cloneInSub()}, {@link AbstractSymbol#replaceInSub(ISymbol, ISymbol)}, and
	 * {@link AbstractSymbol#getAllContainedInSub()} methods.
	 * </p>
	 * 
	 * 
	 * @param stack The stack for which the <tt>this</tt> object is being cloned for.
	 * @return A cloned version of the <tt>this</tt> object if it is mutable otherwise 
	 * the <tt>this</tt> object is retuned.
	 * 
	 * @see #getMutableCopy()
	 * @see #getWorkingCopy()
	 * @see AbstractSymbol#cloneInSub()
	 * @see AbstractSymbol#replaceInSub(ISymbol, ISymbol)
	 */
	public ISymbol<T> clone(IStack stack);
	
	/**
	 * Returns the mutable copy of <tt>this</tt> object for the current execution stack.
	 * A mutable copy of the current object must be used when the <tt>this</tt> object is going to be
	 * mutated or changed.
	 * 
	 * @return The mutable copy of the <tt>this</tt> object.
	 * 
	 * @see #isMutable()
	 * @see #getWorkingCopy()
	 */
	public ISymbol<T> getMutableCopy();
	
	/**
	 * Returns the current working copy of the <tt>this</tt> object. The working copy of the <tt>this</tt>
	 * object must be used when a read-only operation is being carried out. Use of working copy ensures
	 * that the read-only operation is being carried out on the correct version of the <tt>this</tt> object
	 * created for the current execution stack. It is possible that the <tt>this</tt> object is pointing
	 * to the previous stale copy rather than a cloned copy of the object created for the current stack. 
	 * 
	 * @return The read-only working copy of the <tt>this</tt> object.
	 * 
	 * @see #isMutable()
	 * @see #getMutableCopy()
	 */
	public ISymbol<T> getWorkingCopy();
	
	/**
	 * First tries to retrieve working copy through {@link #getWorkingCopy()}. If the working copy is null
	 * then retrieves a mutable copy through {@link #getMutableCopy()}.
	 * 
	 * @return Returns either a working copy or a mutable copy for this object.
	 */
	public ISymbol<T> getWorkingOrMutableCopy();	
}
