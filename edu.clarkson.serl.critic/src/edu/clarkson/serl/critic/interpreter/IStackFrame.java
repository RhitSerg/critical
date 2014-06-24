/*
 * IStackFrame.java
 * Jul 7, 2011
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import soot.Value;
import soot.jimple.JimpleBody;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.ExceptionalUnitGraph;

/**
 * Represents an element of the program stack. One stack frame will be created per
 * method in the call hierarchy.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public interface IStackFrame {
	/**
	 * Gets the {@link IStack} where this stack frame is located.
	 * @return {@link IStack}
	 */
	public IStack getStack();
	
	/**
	 * Gets the context (class and method pair) of the frame.
	 * @return {@link Context}
	 */
	public Context getContext();
	
	/**
	 * Gets the graph corresponding to the current method being processed.
	 * 
	 * @return {@link ExceptionalUnitGraph}
	 */
	public ExceptionalUnitGraph getGraph();
	
	/**
	 * Gets the jimple body of the current method being processed.
	 * @return
	 */
	public JimpleBody getBody();
	
	/**
	 * Gets the loops present in the graph.
	 * 
	 * @return A {@link Collection} of {@link Loop} in the graph.
	 */
	public Collection<Loop> getLoops();
	
	/**
	 * Gets the {@link ISymbol} corresponding to the supplied value.
	 * 
	 * @param value The value to be looked up.
	 * @return
	 */
	public ISymbol<? extends Value> lookup(Value value);

	/**
	 * Gets the receiver of the method call corresponding to this stack frame.
	 * 
	 * @return The receiver of the method call corresponding to this stack frame and <tt>null</tt> for static method.
	 */
	public ISymbol<? extends Value> getReceiver();
	
	/**
	 * Returns the list of arguments in the order they were supplied to the corresponding method call.
	 * @return
	 */
	public List<ISymbol<? extends Value>> getArguments();
	
	/**
	 * Returns the return value of the method call. Returns <tt>null</tt> if the method does not return anything
	 * or if the call of the method has not reached the end of its body.
	 * @return
	 */
	public ISymbol<? extends Value> getReturnValue();
	
	/**
	 * Returns an unmodifaible map of the internal mapping of {@link Value} to {@link ISymbol}.
	 * @return
	 */
	public Map<Value, ISymbol<? extends Value>> getVariableToSymbolMap();
	
	/**
	 * Returns all of the {@link ISymbol} that was assigned to the local variables
	 * of this stack frame. The modification of the returned set will not effect
	 * the internal state of this frame.
	 * 
	 * @return A midifiable set of {@link ISymbol}
	 */
	public Set<ISymbol<? extends Value>> getSymbols();

	/**
	 * <p>
	 * Computes a set of {@link DefinitionPoint} sorted in ascending order according to 
	 * the line number in the source code. A definition point is the location in the source
	 * code where the supplied <tt>symbol</tt> is assigned to a local variable.
	 * </p>
	 * <p>
	 * The first element of this set would represent the first assignment of this symbol to
	 * a local variable in the current method. Hence, if the supplied object was created
	 * in this method, then the first element of the set represents the location where
	 * the supplied <tt>symbol</tt> was initialized.
	 * </p>
	 * @param symbol
	 * @return
	 */
	public SortedSet<DefinitionPoint> getDefinitionPoint(ISymbol<? extends Value> symbol);
	
	
	/**
	 * Returns an unmodifiable set of newly created objects in this stack frame.
	 * 
	 * @return An unmodifiable set of {@link ISymbol}.
	 */
	public Set<ISymbol<? extends Value>> getNewObjects();
	
	/**
	 * Returns a set of newly created mutable objects in this stack frame.
	 * @return
	 */
	public Set<ISymbol<? extends Value>> getMutableNewObjects();	
	
	/**
	 * Checks wheter the supplied <tt>symbol</tt> escapes the method boundary.
	 * An object escapes a method boundary in four ways:
	 * <ol>
	 * <li>If the object is a part of the heap of the receiver of the current method.</li>
	 * <li>If the object is a part of the heap of parameters of the current method.</li>
	 * <li>If the object is a part of the heap of the return value of the current method.</li>
	 * <li>If the object is a part of the heap of a static field.</li>
	 * </ol> 
	 * @param symbol The {@link ISymbol to be checked}
	 * @return <tt>true</tt> if the supplied object escapes the method boundary and <tt>false</tt> otherwise.
	 */
	public boolean isEscapingObject(ISymbol<? extends Value> symbol);

	/**
	 * Computes a {@link Set} of newly created {@link ISymbol} within this method that escape
	 * the method boundary.
	 * An object escapes a method boundary in four ways:
	 * <ol>
	 * <li>If the object is a part of the heap of the receiver of the current method.</li>
	 * <li>If the object is a part of the heap of parameters of the current method.</li>
	 * <li>If the object is a part of the heap of the return value of the current method.</li>
	 * <li>If the object is a part of the heap of a static field.</li>
	 * </ol> 
	 * @return {@link Set} of newly created {@link ISymbol} escaping the method boundary.
	 */
	public Set<ISymbol<? extends Value>> getEscapingNewObjects();
	
	/**
	 * Computes a {@link Set} of newly created {@link ISymbol} within this method that does not escape
	 * the method boundary.
	 * An object escapes a method boundary in four ways:
	 * <ol>
	 * <li>If the object is a part of the heap of the receiver of the current method.</li>
	 * <li>If the object is a part of the heap of parameters of the current method.</li>
	 * <li>If the object is a part of the heap of the return value of the current method.</li>
	 * <li>If the object is a part of the heap of a static field.</li>
	 * </ol> 
	 * @return {@link Set} of newly created {@link ISymbol} that does not escape the method boundary.
	 */
	public Set<ISymbol<? extends Value>> getNonEscapingNewObjects();

	/**
	 * Computes a {@link Set} of newly created {@link ISymbol} within this method, 
	 * that are part of the supplied <tt>symbol</tt>, and that escape the method boundary.
	 * An object escapes a method boundary in four ways:
	 * <ol>
	 * <li>If the object is a part of the heap of the receiver of the current method.</li>
	 * <li>If the object is a part of the heap of parameters of the current method.</li>
	 * <li>If the object is a part of the heap of the return value of the current method.</li>
	 * <li>If the object is a part of the heap of a static field.</li>
	 * </ol> 
	 * @param symbol
	 * @return
	 */
	public Set<ISymbol<? extends Value>> getEscapingNewObjects(ISymbol<? extends Value> symbol);
	
	/**
	 * Computes a {@link Set} of newly created {@link ISymbol} within this method, 
	 * that are part of the supplied <tt>symbol</tt>, and that does not escape the 
	 * method boundary.
	 * An object escapes a method boundary in four ways:
	 * <ol>
	 * <li>If the object is a part of the heap of the receiver of the current method.</li>
	 * <li>If the object is a part of the heap of parameters of the current method.</li>
	 * <li>If the object is a part of the heap of the return value of the current method.</li>
	 * <li>If the object is a part of the heap of a static field.</li>
	 * </ol> 
	 * @param symbol
	 * @return
	 */
	public Set<ISymbol<? extends Value>> getNonEscapingNewObjects(ISymbol<? extends Value> symbol);
}
