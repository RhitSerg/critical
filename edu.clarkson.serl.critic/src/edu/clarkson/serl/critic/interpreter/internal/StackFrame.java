/*
 * StackFrame.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.clarkson.serl.critic.interpreter.AbstractSymbol;
import edu.clarkson.serl.critic.interpreter.Context;
import edu.clarkson.serl.critic.interpreter.DefinitionPoint;
import edu.clarkson.serl.critic.interpreter.IStack;
import edu.clarkson.serl.critic.interpreter.IStackFrame;
import edu.clarkson.serl.critic.interpreter.IStackPointer;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.util.EvaluationLog;
import edu.clarkson.serl.critic.util.Util;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.JimpleBody;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.annotation.logic.LoopFinder;
import soot.toolkits.graph.ExceptionalUnitGraph;

/**
 * Represents the implementation of {@link IStackFrame}.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class StackFrame implements IStackFrame, Cloneable {
	private static int count = 0;
	/**
	 * Resets the object counter of StackFrame back to 0.
	 */
	public static void reset() {
		count = 0;
	}
	
	private int id;
	
	private Context context;
	private JimpleBody body;
	private ExceptionalUnitGraph graph;
	private Collection<Loop> loops;
	private HashMap<Stmt, ArrayList<Stmt>> loopExitToTargetMap;

	private IStack stack;
	private int branch;
	private boolean forced;
	private List<Stmt> branches;
	
	private ISymbol<? extends Value> returnValue;
	
	private ISymbol<? extends Value> receiver;
	private ThisRef thisRef;
	
	private ArrayList<ParameterRef> parameterRefs;
	List<ISymbol<? extends Value>> arguments;
	
	private HashMap<Value, ISymbol<? extends Value>> valueToSymbolMap;
	
	private HashSet<ISymbol<? extends Value>> newObjects;
	
	/**
	 * Creates a stack frame with supplied context object.
	 * @param context The {@link Context} object for this frame.
	 */
	public StackFrame(IStack stack, Context context, ISymbol<? extends Value> receiver, List<ISymbol<? extends Value>> arguments) {
		this.id = ++count;
		this.stack = stack;
		
		this.context = context;
		this.body = (JimpleBody)context.getSootMethod().retrieveActiveBody();
		this.graph = new ExceptionalUnitGraph(this.body);
		LoopFinder loopFinder = new LoopFinder();
		loopFinder.transform(this.body);
		this.loops = loopFinder.loops();
		this.loopExitToTargetMap = new HashMap<Stmt, ArrayList<Stmt>>();
		
		// Add loops to the log
		EvaluationLog.put(this.context.getSootMethod(), this.loops.size());
		
		this.branch = 0;
		this.forced = false;
		this.branches = Collections.emptyList();
		
		this.returnValue = null;
		
		this.receiver = receiver;
		this.thisRef = null;
		
		this.arguments = arguments;
		this.parameterRefs = new ArrayList<ParameterRef>(context.getSootMethod().getParameterCount());
		
		this.valueToSymbolMap = new HashMap<Value, ISymbol<? extends Value>>();
		this.newObjects = new HashSet<ISymbol<? extends Value>>();
	}
	
	@SuppressWarnings("unchecked")
	public StackFrame clone(IStack stack) {
		StackFrame clone = null;
		try {
			clone = (StackFrame)super.clone();
		}
		catch(CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		clone.stack = stack;

		// context, graph, loops, and parameterRefs are immutable and are fine
		// Handle mutable fields
		clone.loopExitToTargetMap = new HashMap<Stmt, ArrayList<Stmt>>(this.loopExitToTargetMap.size());
		for(Map.Entry<Stmt, ArrayList<Stmt>> e : this.loopExitToTargetMap.entrySet()) {
			clone.loopExitToTargetMap.put(e.getKey(), (ArrayList<Stmt>)e.getValue().clone());
		}
		
		clone.newObjects = (HashSet<ISymbol<? extends Value>>)this.newObjects.clone(); 		
		
		// We need to reset the stack pointers in the cloned stack and in the shared 
		// symbolic objects between this and cloned stack frame
		clone.valueToSymbolMap = (HashMap<Value, ISymbol<? extends Value>>)this.valueToSymbolMap.clone();
		for(Map.Entry<Value, ISymbol<? extends Value>> e : clone.valueToSymbolMap.entrySet()) {
			Value local = e.getKey();
			ISymbol<? extends Value> symbol = e.getValue();
			if(symbol.isMutable()) {
				IStackPointer pointer = new StackPointer(clone, local);
				((AbstractSymbol<? extends Value>)symbol).addLink(pointer);
			}
		}
		
		return clone;
	}
	
	/**
	 * Gets the previous branch number.
	 * 
	 * @return the branch
	 */
	public int getBranch() {
		return branch;
	}
	
	public void setBranch(int branch) {
		this.branch = branch;
	}
	
	/**
	 * @return the branches
	 */
	public List<Stmt> getBranches() {
		return this.branches;
	}
	
	public void setBranches(List<Stmt> branches) {
		this.branches = branches;
	}
	
	/**
	 * Gets whether the previous branch was forced.
	 * 
	 * @return the forced
	 */
	public boolean isForced() {
		return forced;
	}
	
	public void setForced(boolean forced) {
		this.forced = forced;
	}

	
	@Override
	public String toString() {
		return "[" + this.id + " - " + this.context.getSootMethod().getName() + "() - " + this.context.getCallSite() + "]";
	}
	
	public Context getContext() {
		return context;
	}

	public JimpleBody getBody() {
		return body;
	}

	public ExceptionalUnitGraph getGraph() {
		return graph;
	}

	public Collection<Loop> getLoops() {
		return loops;
	}
	
	public ISymbol<? extends Value> lookup(Value value) {
		return this.valueToSymbolMap.get(value);
	}
	
	public ISymbol<? extends Value> getReceiver() {
		if(this.thisRef == null)
			return this.receiver;
		return this.lookup(this.thisRef);
	}
	
	public ISymbol<? extends Value> getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(ISymbol<? extends Value> returnValue) {
		this.returnValue = returnValue;
	}
	
	public void setThisRef(ThisRef thisRef) {
		this.thisRef = thisRef;
	}
	
	public void addParameterRef(ParameterRef param) {
		this.parameterRefs.add(param);
	}

	public ISymbol<? extends Value> getArgument(int index) {
		return this.lookup(this.parameterRefs.get(index));
	}
	
	public List<ISymbol<? extends Value>> getArguments() {
		if(this.parameterRefs.isEmpty() || this.arguments == null || this.arguments.size() != this.parameterRefs.size()) // Arguments is still not configured
			return this.arguments;
		
		List<ISymbol<? extends Value>> list = new ArrayList<ISymbol<? extends Value>>(this.parameterRefs.size());
		for(ParameterRef p : this.parameterRefs) {
			ISymbol<? extends Value> symbol = this.lookup(p);
			list.add(symbol);
		}
		return list;
	}
	
	
	/**
	 * Maps the jimple value to the symbol.
	 * 
	 * @param value The jimple {@link Value} key.
	 * @param symbol The {@link ISymbol} object to be mapped.
	 * @return Previously mapped {@link ISymbol} to the supplied value if any.
	 */
	public ISymbol<? extends Value> put(Value value, ISymbol<? extends Value> symbol) {
		IStackPointer p = new StackPointer(this, value);
		((AbstractSymbol<? extends Value>) symbol).addLink(p);
		return this.valueToSymbolMap.put(value, symbol);
	}
	
	/**
	 * Adds new symbolic object to the set of newly created objects for this stack frame. 
	 * 
	 * @param newObject The new symbolic object.
	 * @return Returns <tt>true</tt> if successful else returns <tt>false</tt>.
	 */
	public boolean add(ISymbol<? extends Value> newObject) {
		return this.newObjects.add(newObject);
	}
	
	/**
	 * Removes the supplied object from the set of newly created objects for this stack frame. 
	 * @param symbol The symbol to be removed.
	 * @return Returns <tt>true</tt> if successful else returns <tt>false</tt>.
	 */
	public boolean remove(ISymbol<? extends Value> symbol) {
		return this.newObjects.remove(symbol);
	}
	
	/**
	 * Replaces the original symbolic object with the supplied clone in the set set of newly 
	 * created object for this stack frame.
	 * 
	 * @param original The original symbolic object.
	 * @param clone The clone of the oringinal symbolic object.
	 */
	public void replace(ISymbol<? extends Value> original, ISymbol<? extends Value> clone) {
		this.newObjects.remove(original);
		this.newObjects.add(clone);
	}
	
	
	Map<Local, Integer> localToLineNumber = null;
	public SortedSet<DefinitionPoint> getDefinitionPoint(ISymbol<? extends Value> symbol) {
		if(this.localToLineNumber == null)
			this.localToLineNumber = Util.getLocalToLineNumberMap(this.graph);

		TreeSet<DefinitionPoint> set = new TreeSet<DefinitionPoint>();
		Set<Value> vars = this.getDefiningVariables(symbol);
		for(Value v : vars) {
			if(v instanceof Local) {
				Integer line = this.localToLineNumber.get(v);
				if(line != null) {
					set.add(new DefinitionPoint(v, line));
				}
			}
		}
		return set;
	}
	private Set<Value> getDefiningVariables(ISymbol<? extends Value> symbol) {
		HashSet<Value> set = new HashSet<Value>();
		for(Map.Entry<Value, ISymbol<? extends Value>> entry : this.valueToSymbolMap.entrySet()) {
			if(symbol.equals(entry.getValue()))
				set.add(entry.getKey());
		}
		return set;
	}

	
	/**
	 * <p>
	 * Soot <strong>does not detect a loop containing a single statement</strong> with its
	 * {@link LoopFinder} API. An example:
	 * </p>
	 *
	 * <pre>
	 * 	public synchronized int test(SimpleFlow that) {
	 * 		synchronized(this) {
	 * 			int i = 1 + 2;
	 * 			return i;
	 * 		}
	 * 	}
	 * </pre>
	 * 
	 * <p>
	 * This produces a jimple statement for exception handling that has edge to itself shown below.
	 * This function will ignore such edge while returning children. i.e. A statement
	 * who is a child of itself will be ignored as a child:
	 * </p>
	 * <pre>
	 * label2:
	 * 	$r1 := @caughtexception;
	 * </pre>
	 * 
	 * @param stmt
	 * @return List of all successors both exceptional as well as un-exceptional.
	 * @see {@link #toListOfStmt(Stmt, List)}
	 */
	public List<Stmt> getAllSuccsOf(Stmt stmt) {
		List<Unit> succ = this.graph.getSuccsOf(stmt);
		return this.toListOfStmt(stmt, succ);
	}
	
	private List<Stmt> toListOfStmt(Stmt stmt, List<Unit> succ) {
		ArrayList<Stmt> children = new ArrayList<Stmt>(succ.size());
		for(Unit child : succ) {
			if(child != stmt)
				children.add((Stmt)child);
		}
		return children;
	}
	
	/**
	 * Return un-exceptional successors of the supplied statement
	 * @param stmt
	 * @return
	 */
	public List<Stmt> getUnExceptionalSuccOf(Stmt stmt) {
		List<Unit> succ = this.graph.getUnexceptionalSuccsOf(stmt);
		return this.toListOfStmt(stmt, succ);
	}

	/**
	 * Returns the exceptional successors of the supplied statement.
	 * @param stmt
	 * @return
	 */
	public List<Stmt> getExceptionalSuccOf(Stmt stmt) {
		List<Unit> succ = this.graph.getExceptionalSuccsOf(stmt);
		return this.toListOfStmt(stmt, succ);
	}
	
	/**
	 * Returns a successor wrapped by a list that falls through the supplied statement.
	 * @param stmt
	 * @return
	 */
	public List<Stmt> getFallThroughSuccOf(Stmt stmt) {
		ArrayList<Stmt> list = new ArrayList<Stmt>(1);
		List<Stmt> succ = this.getUnExceptionalSuccOf(stmt);
		if(!succ.isEmpty())
			list.add(succ.get(0));
		return list;
	}
	
	/**
	 * Returns a successor wrapped by a list that branches from the supplied statement.
	 * @param stmt
	 * @return
	 */
	public List<Stmt> getBranchingSuccOf(Stmt stmt) {
		ArrayList<Stmt> list = new ArrayList<Stmt>(1);
		List<Stmt> succ = this.getUnExceptionalSuccOf(stmt);
		if(!succ.isEmpty()) {
			if(succ.size() == 1)
				list.add(succ.get(0));
			else
				list.add(succ.get(1));
		}
		return list;
	}

	/**
	 * Returns a list of successors that takes the supplied loopExit statement
	 * out of the loop rather than going inside the body of the loop.
	 * 
	 * @param loopExit The loop exit statement of a loop.
	 * @return A {@link List} of the forced successor {@link Stmt}s.
	 */
	public List<Stmt> getForcedSuccOf(Stmt loopExit) {
		ArrayList<Stmt> exitPoints = this.loopExitToTargetMap.get(loopExit);
		if(exitPoints != null)
			return exitPoints;
		
		exitPoints = new ArrayList<Stmt>();
		for(Loop l : loops) {
			if(l.getLoopExits().contains(loopExit)) {
				exitPoints.addAll(l.targetsOfLoopExit(loopExit));
			}
		}
		this.loopExitToTargetMap.put(loopExit, exitPoints);
		return exitPoints;
	}
	
	
	/**
	 * Checks if the supplied statement is a loop exit or not. Note that, 
	 * for an infinite loop, a back jump statement is considered as a loop exit.
	 * 
	 * @param s The {@link Stmt} to be checked.
	 * @return True is the supplied statement is a loop exit otherwise false.
	 */
	public boolean isALoopExit(Stmt s) {
		for(Loop l : loops) {
			if(l.getLoopExits().contains(s))
				return true;
			else if(l.loopsForever() && s.equals(l.getBackJumpStmt())) 
				// For an infinite loop, we count the repetition of back jump statement.
				// Otherwise, the DFS will go forever!
				return true;
		}
		
		return false;
	}

	/**
	 * This method tells all of the symbol to release the pointer to this as it 
	 * has been popped out of the current {@link IStack}.
	 */
	public void firePoped() {
		for(Map.Entry<Value,ISymbol<? extends Value>> e : this.valueToSymbolMap.entrySet()) {
			AbstractSymbol<? extends Value> symbol = (AbstractSymbol<? extends Value>)e.getValue();
			symbol.releaseLink(new StackPointer(this, e.getKey()));
		}
		if(this.stack.isEmpty())
			return;
		StackFrame newTop = (StackFrame)this.stack.peek();
		newTop.newObjects.addAll(this.getEscapingNewObjects());
	}

	public IStack getStack() {
		return this.stack;
	}

	public Map<Value, ISymbol<? extends Value>> getVariableToSymbolMap() {
		return Collections.unmodifiableMap(this.valueToSymbolMap);
	}

	public Set<ISymbol<? extends Value>> getSymbols() {
		HashSet<ISymbol<? extends Value>> set = new HashSet<ISymbol<? extends Value>>();
		set.addAll(this.valueToSymbolMap.values());
		return set;
	}

	public Set<ISymbol<? extends Value>> getNewObjects() {
		return Collections.unmodifiableSet(this.newObjects);
	}
	
	public Set<ISymbol<? extends Value>> getMutableNewObjects() {
		HashSet<ISymbol<? extends Value>> mutableSet = new HashSet<ISymbol<? extends Value>>();
		for(ISymbol<? extends Value> s : this.newObjects) {
			if(s.isMutable())
				mutableSet.add(s);
		}
		return mutableSet;
	}
	
	public Set<ISymbol<? extends Value>> getEscapingNewObjects() {
		HashSet<ISymbol<? extends Value>> escaping = new HashSet<ISymbol<? extends Value>>();
		Set<ISymbol<? extends Value>> escapeRoots = this.getEscapeRoots();
		for(ISymbol<? extends Value> symbol : this.getMutableNewObjects()) {
			if(this.isEscapingObject(symbol, escapeRoots)) {
				escaping.add(symbol);
			}
		}
		return escaping;
	}
	
	public Set<ISymbol<? extends Value>> getNonEscapingNewObjects() {
		HashSet<ISymbol<? extends Value>> nonEscaping = new HashSet<ISymbol<? extends Value>>();
		Set<ISymbol<? extends Value>> escapeRoots = this.getEscapeRoots();
		for(ISymbol<? extends Value> symbol : this.getMutableNewObjects()) {
			if(!this.isEscapingObject(symbol, escapeRoots)) {
				nonEscaping.add(symbol);
			}
		}
		return nonEscaping;
	}
	
	public Set<ISymbol<? extends Value>> getEscapingNewObjects(ISymbol<? extends Value> symbol) {
		Set<ISymbol<? extends Value>> closure = symbol.getContainedReflexiveTransitiveClosure();
		closure.retainAll(this.getMutableNewObjects());
		
		Set<ISymbol<? extends Value>> escapeRoots = this.getEscapeRoots();
		Iterator<ISymbol<? extends Value>> iterator = closure.iterator();
		while(iterator.hasNext()) {
			ISymbol<? extends Value> child = iterator.next();
			if(!this.isEscapingObject(child, escapeRoots)) {
				iterator.remove();
			}
		}
		return closure;
	}
	
	public Set<ISymbol<? extends Value>> getNonEscapingNewObjects(ISymbol<? extends Value> symbol) {
		Set<ISymbol<? extends Value>> closure = symbol.getContainedReflexiveTransitiveClosure();
		closure.retainAll(this.getMutableNewObjects());
		
		Set<ISymbol<? extends Value>> escapeRoots = this.getEscapeRoots();
		Iterator<ISymbol<? extends Value>> iterator = closure.iterator();
		while(iterator.hasNext()) {
			ISymbol<? extends Value> child = iterator.next();
			if(this.isEscapingObject(child, escapeRoots)) {
				iterator.remove();
			}
		}
		return closure;
	}
	
	
	public boolean isEscapingObject(ISymbol<? extends Value> symbol) {
		if(!this.newObjects.contains(symbol))
			return true;
		return this.isEscapingObject(symbol, this.getEscapeRoots());
	}
	
	private boolean isEscapingObject(ISymbol<? extends Value> symbol, Set<ISymbol<? extends Value>> escapeRoots) {
		if(!symbol.getStaticFields().isEmpty() || escapeRoots.contains(symbol))
			return true;
		
		Set<ISymbol<? extends Value>> closure = symbol.getContainerTransitiveClosure();
		for(ISymbol<? extends Value> parent : closure) {
			if(!parent.getStaticFields().isEmpty() || escapeRoots.contains(parent))
				return true;
		}
		return false;
	}

	private HashSet<ISymbol<? extends Value>> getEscapeRoots() {
		HashSet<ISymbol<? extends Value>> escapingRoots = new HashSet<ISymbol<? extends Value>>();
		
		// Receiver object will escape method boundary
		ISymbol<? extends Value> symbol = this.getReceiver();
		if(symbol != null) {
			escapingRoots.add(symbol);
		}
		
		// Return value will escape method boundary
		symbol = this.getReturnValue();
		if(symbol != null) {
			escapingRoots.add(symbol);
		}
		
		// Method arguments will escape method boundary
		List<ISymbol<? extends Value>> arguments = this.getArguments();
		if(arguments != null) {
			escapingRoots.addAll(arguments);
		}
		
		// Note static fields are also escaping roots whose objects are not
		// confined to this method
		return escapingRoots;
	}
}
