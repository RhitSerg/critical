/*
 * Interpreter.java
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
 
package edu.clarkson.serl.critic.interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;

import edu.clarkson.serl.critic.CriticPlugin;
import edu.clarkson.serl.critic.exceptions.SymThrowable;
import edu.clarkson.serl.critic.extension.ExtensionManager;
import edu.clarkson.serl.critic.extension.ICheckPoint;
import edu.clarkson.serl.critic.factory.StmtFactory;
import edu.clarkson.serl.critic.interpreter.internal.Stack;
import edu.clarkson.serl.critic.interpreter.internal.StackFrame;
import edu.clarkson.serl.critic.interpreter.internal.WeakHashSet;
import edu.clarkson.serl.critic.interpreter.model.ConstInteger;
import edu.clarkson.serl.critic.interpreter.model.ConstNull;
import edu.clarkson.serl.critic.interpreter.model.Invoke;
import edu.clarkson.serl.critic.interpreter.model.StmtAbstract;
import edu.clarkson.serl.critic.preferences.Preferences;
import edu.clarkson.serl.critic.reporter.Reporter;
import edu.clarkson.serl.critic.util.EvaluationLog;
import edu.clarkson.serl.critic.util.Util;

import soot.FastHierarchy;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Ref;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;

/**
 * The interpreter for CriticAL runs on top of Soot's Jimple representation. 
 * It is designed as a singleton class and can be configured using preference and extension
 * mechanism of Eclipse.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class Interpreter {
	public static final String INIT = "<init>";
	public static final String OBJECT = "java.lang.Object";
	public static final AbstractValue VALUE = AbstractValue.fromObject(OBJECT);
	
	public static final ConstNull NULL = ConstNull.NULL;
	public static final ConstInteger TRUE = new ConstInteger(IntConstant.v(1)); //ConstInteger.TRUE;
	public static final ConstInteger FALSE = new ConstInteger(IntConstant.v(0)); //ConstInteger.FALSE;
	public static final ISymbol<Value> VOID = new Symbol<Value>(AbstractValue.fromObject("java.lang.Object"), false, false);
	
	public static final ISymbol<Value> RUNTIME = null; // "java.lang.RuntimeException";
	public static final ISymbol<Value> ARITHMETIC = null; // "java.lang.ArithmeticException";
	public static final ISymbol<Value> ARRAY_STORE = null; // "java.lang.ArrayStoreException";
	public static final ISymbol<Value> CLASS_CAST = null; // "java.lang.ClassCastException";
	public static final ISymbol<Value> ILLEGAL_MONITOR_STATE = null; // "java.lang.IllegalMonitorStateException";
	public static final ISymbol<Value> INDEX_OUT_OF_BOUNDS = null; // "java.lang.IndexOutOfBoundsException";
	public static final ISymbol<Value> ARRAY_INDEX_OUT_OF_BOUNDS = new SymThrowable<Value>(AbstractValue.fromObject("java.lang.ArrayIndexOutOfBoundsException"), false, false); 
	public static final ISymbol<Value> NEGATIVE_ARRAY_SIZE = null; // "java.lang.NegativeArraySizeException";
	public static final ISymbol<Value> NULL_POINTER = null; // "java.lang.NullPointerException";
	public static final ISymbol<Value> INSTANTIATION_ERROR = null; // "java.lang.InstantiationError";
	public static final ISymbol<Value> EXCEPTION = null; // "java.lang.Throwable";
	public static final ISymbol<Value> THROWABLE = null; // "java.lang.Exception";
	public static final ISymbol<Value> ERROR = null; // "java.lang.Error";

	private static Interpreter instance = null;
	
	public static Interpreter instance() {
		if(instance == null)
			instance = new Interpreter();
		return instance;
	}
	
	public static void reset() {
		instance = null;
	}

	public static enum Result {
		Unknown,
		Feasible,
		Infeasible,
		Satisfiable,
		Unsatisfiable,
		Exception;
	}
	
//	private LinkedList<IStackFrame> stack;
	private WeakHashSet<ISymbol<? extends Value>> heap;
	private ISymbol<? extends Value> exception;
	
	private LinkedList<IStack> stackOfStack;
	
	
	private Interpreter() {
		this.init();
	}
	
	private void init() {
		heap = new WeakHashSet<ISymbol<? extends Value>>();
		exception = null;
		stackOfStack = new LinkedList<IStack>();
		
		// Interpreter will have at least one stack to start with
		stackOfStack.addLast(new Stack());
	}
	
	public IStack peekStack() {
		return this.stackOfStack.getLast();
	}
	
	public boolean contains(IStack stack) {
		return this.stackOfStack.contains(stack);
	}
	
	// Stack of stack operations
	public IStackFrame peek() {
		return stackOfStack.getLast().peek();
	}
	
	private StackFrame pop() {
		Stack lastStack = (Stack)stackOfStack.getLast();
		StackFrame frame = (StackFrame)lastStack.pop();
		if(lastStack.isEmpty()) {
			stackOfStack.removeLast();
			
			// We are removing lastStack, let us tell the stack about this operation
			lastStack.poped();
		}
		return frame;
	}
	
	private void push(IStackFrame frame) {
		Stack stack;
		if(this.stackOfStack.isEmpty()) {
			stack = new Stack();
			this.stackOfStack.addLast(stack);
		}
		else {
			stack = (Stack)stackOfStack.getLast();
		}
		stack.push(frame);
	}
	
	private void push(IStack stack) {
		stackOfStack.addLast(stack);
	}
	
	private boolean isEmpty() {
		return stackOfStack.isEmpty();
	}
	
//	@Override
//	public String toString() {
//		StringBuffer buffer = new StringBuffer();
//		buffer.append("================= Interpreter =================\n");
//		buffer.append("Stack of Stack Size: " + this.stackOfStack.size() + "\n");
//		ListIterator<LinkedList<IStackFrame>> i = this.stackOfStack.listIterator(this.stackOfStack.size());
//		while(i.hasPrevious()) {
//			LinkedList<IStackFrame> stack = i.previous();
//			buffer.append("Stack Size: " + stack.size() + "\n");
//			buffer.append("--------------------------------------\n");
//			ListIterator<IStackFrame> iterator = stack.listIterator(stack.size());
//			while(iterator.hasPrevious()) {
//				buffer.append(iterator.previous());
//				buffer.append("\n");
//			}
//			buffer.append("--------------------------------------\n");
//		}
//		buffer.append("============= End of Interpreter ==============\n");
//		return buffer.toString();
//	}
	
	public void setException(ISymbol<? extends Value> exception) {
		this.exception = exception;
	}
	
	/**
	 * @param value
	 * @return
	 */
	public ISymbol<? extends Value> lookup(Value value) {
		return this.peek().lookup(value);
	}
	
	/**
	 * Adds a non-stack related symbol to the heap.
	 * @param symbol
	 * @return
	 */
	public boolean addToHeap(ISymbol<? extends Value> symbol) {
		return heap.add(symbol);
	}
	
	/**
	 * Gets the unmodifiable view of the heap as a {@link Set}. Note
	 * that the set is a {@link WeakHashSet} and the elements may change
	 * due to garbage collection non-deterministically.
	 * 
	 * @return The unmodifaible view of the set.
	 * @see {@link WeakHastSet}
	 * @see {@link java.util.WeakHashMap}
	 * @see {@link java.lang.ref.WeakReference}
	 */
	public Set<ISymbol<? extends Value>> getHeap() {
		return Collections.unmodifiableSet(this.heap);
	}
	
	public int getLineNumber() {
		return Util.getLineNumber(this.currentStmt);
	}
	
	public SootMethod getCurrentMethod() {
		return this.currentMethod;
	}
	
	public SootMethod getInvokedMethod() {
		return this.invokedMethod;
	}
	
	public Stmt getCurrentStmt() {
		return this.currentStmt;
	}
	
	public SootClass getCurrentClass() {
		if(this.currentMethod != null)
				return this.currentMethod.getDeclaringClass();
		return null;
	}
	
	private int pathCount = 1;
	// We will use this to check the result of the executeStatement() method
	public void execute(IProgressMonitor monitor) {
		this.monitor = monitor;
		ExtensionManager extManager = ExtensionManager.instance();
		List<Context> entryList = extManager.getEntryMethods();
		monitor.beginTask("Performing symbolic execution ...", entryList.size());
		
		// Start of critic
		this.processCheckPoint(ICheckPoint.Interest.CriticStart);
		
		for(Context context : entryList) {
			if(monitor.isCanceled())
				return;
			
			EvaluationLog.startProgram(context.getSootMethod());
			
			this.init();
			// Lets reset the clone history object
			CloneHistory.reset();
			try {
				// Process the context
				this.processContext(context);
			}
			catch(Exception e) {
				String message = "CriticAL framework encountered a problem while analyzing: \n" +
						"\t[Class: " + this.getCurrentClass() + "]\n" +
						"\t[Method: " + this.getCurrentMethod() + "]\n" +
						"\t[Line Number: " + this.getLineNumber() + "]";
				Exception newE = new Exception(message, e);
				newE.printStackTrace();
				CriticPlugin.log(Status.ERROR, message, e);
			}
			
			EvaluationLog.endProgram();
			monitor.worked(1);
		}
		
		// End of critic
		this.processCheckPoint(ICheckPoint.Interest.CriticEnd);
		monitor.done();
	}
	
	
	
	// These are the interpreter states
	private Stmt currentStmt;
	private List<Stmt> children;
	private SootMethod currentMethod;
	private SootMethod invokedMethod;
	private StackFrame sFrame;
	private IProgressMonitor monitor;
	
	public void processContext(Context context) {
		EvaluationLog.newPath();
		
		CloneHistory.instance();
		sFrame = new StackFrame(this.peekStack(), context, null, null);
		this.push(sFrame);
		this.currentStmt = (Stmt)sFrame.getGraph().getHeads().get(0);

		// Get hold of the current method
		this.currentMethod = sFrame.getContext().getSootMethod();
		String msg = "Processing " + this.currentMethod.getDeclaringClass() + " ...";
		monitor.setTaskName(msg);
		monitor.subTask("Processing " + this.currentMethod + " ...");
//		System.out.println(msg);
		
		// Create a dummy starting point settings
		this.children = Collections.emptyList();
		sFrame.setBranch(0);
		sFrame.setBranches(children);
		
		// This is the starting point of the execution of a path
		this.processCheckPoint(ICheckPoint.Interest.PathStart);
		
		// Start processing the method
		while(this.currentStmt != null) {
			this.currentMethod = sFrame.getContext().getSootMethod();

			// TODO: Get rid of this println()
//			System.out.println(this.currentStmt);
			EvaluationLog.newStmt();
			
			if(monitor.isCanceled())
				return;
			
			// This is the starting point of a statement
			this.processCheckPoint(ICheckPoint.Interest.StatementStart);
			
			// Check if this statement has invoke expression that needs to be processed first
			// If this is true then the sFrame will be replaced as well as currentStmt will point to
			// the first statement of the invoked method
			if(this.currentStmt.containsInvokeExpr()) {
				processInlining();
				String message = "Processing " + this.peek().getContext().getSootMethod() + " ...";
				monitor.subTask(message);
			}
			
			// Execute the statement and update states
			StmtFactory factory = new StmtFactory();
			StmtAbstract<? extends Stmt> symStmt = factory.newSymbol(this.currentStmt);
			int result = symStmt.execute();
			children = symStmt.getChildren();
			
			// We have finished executing the statement
			this.processCheckPoint(ICheckPoint.Interest.StatementEnd);
			
			// Check if the current statement is a loop exit statement and 
			// has reached its unrolling threshold. If so we need to rewrite 
			// its next child
			if(this.shouldForceLoopExit()) {
				this.children = this.sFrame.getForcedSuccOf(this.currentStmt);
			}

			// Add current statement to the path and set the next set of branches 
			this.peekStack().getPath().add(this.currentStmt);
			sFrame.setBranches(children);
			
			// Method to take actions according to the execution result of currentStmt
			this.processResult(result);
			
			// Now configure next statement to be executed
			// Check if there are more than one child, we need to clone stack for this
			if(children.size() > 1) {
				// Before stack cloning
				this.processCheckPoint(ICheckPoint.Interest.BeforeStackCloned);
				
				// This method performs satisfiability test and clones stack for a feasible path
				this.processMultiChild();

				// After stack is cloned
				this.processCheckPoint(ICheckPoint.Interest.AfterStackCloned);
			}
			else if(children.size() == 1) {
				// Only one successor, so, we are walking in the same branch
				this.currentStmt = children.get(0);
				sFrame.setBranch(0); // Next branch to be processed
			}
			else {
				// TODO: Remove this print statement
//				this.printPath(this.peekStack().getPath(), this.pathCount);
				
				// Method execution end
				this.processCheckPoint(ICheckPoint.Interest.MethodEnd);

				// No successor statement in current method, must be a return statement
				if(sFrame.getContext().getType() == Context.NON_ENTRY) {
					children = this.processNonEntryPathEnd();
				}
				else {
					this.processCheckPoint(ICheckPoint.Interest.PathEnd);
					this.processNextStack();
					++this.pathCount;	
				}
			}
			this.invokedMethod = null;
		}
	}
	
	private boolean shouldForceLoopExit() {
		Path path = this.peekStack().getPath();
		int repeatition = path.getLoopExitRepeatition(this.currentStmt);

		// Repetition is cached, so optimizing lookup
		if(repeatition < 1) {
			// Check if the current statement is a loop exit
			if(!this.sFrame.isALoopExit(this.currentStmt))
				return false;
		
			// Check that the current loop exit already exist
			if(!this.peekStack().getPath().contains(this.currentStmt))
				return false;
		}
		
		// Current statement is a loop exit at this point
		// Lets check if it has been repeated for the given threshold
		Preferences preferences = CriticPlugin.getPreferences();
		repeatition = path.incrementLoopExitRepeatition(this.currentStmt);
		if(repeatition >= preferences.getLoopUnrollLimit())
			return true;
		return false;
	}
	
	private void processNextStack() {
		if(sFrame.getContext().getType() == Context.CALLBACK) {
			this.pop(); // Current callback method
			this.pop(); // The main method
			
			CloneHistory.instance().pop();
		}

		// We need to process a callback point here if there are any in current stack
		if(!this.isEmpty()) {
			Stack oldStack = (Stack)this.peekStack();
			
			ICallbackEntry callBack = oldStack.popCallbackEntry();
			if(callBack != null) {
				this.processCallBack(callBack);
				return;
			}
			else {
				this.pop();
				CloneHistory.instance().pop();
			}
		}
		
		if(!this.isEmpty()) {
			// Configure to process next branch in next stack if one exists 
			StackFrame nextFrame = (StackFrame)this.peek();
			
			// Next branch to be processed
			int branch = nextFrame.getBranch();
			children = nextFrame.getBranches();
			
			if(branch + 1 < children.size()) {
				// TODO: Need to perform satisfiability test here
				
				// More than one successor left, so, we need to clone the stack
				Stack oldStack = (Stack)this.peekStack();
				Stack stack = oldStack.clone();
				this.push(stack);
				sFrame = (StackFrame)this.peek();

				// Now Configure the next branch (0th branch) to be processed
//				sFrame.setForced(false);
				this.currentStmt = children.get(branch);

				// Update the backtrack-point to reflect the next to next branch to be processed
				((StackFrame)oldStack.peek()).setBranch(branch+1);
				
				// Also start a new branch for recording clone history
				CloneHistory.instance().branch();
				
				// TODO: We need to update the state according to the fall and branch behavior
				// or if its a switch statement then according to the value associated to the target
				// 0 - Always falls through
			}
			else {
				// nextFrame has the last branch to be processed
				sFrame = nextFrame;
				this.currentStmt = children.get(branch);
			}
			
			// Start of another backtrack point
			this.processCheckPoint(ICheckPoint.Interest.BackTrackPointStart);
			EvaluationLog.newPath();
		}
		else {
			this.currentStmt = null;
		}
	}
	
	private void processCallBack(ICallbackEntry callBack) {
		this.processCheckPoint(ICheckPoint.Interest.BeforeStackCloned);
		
		// TODO: Use source for Event type
//		ISymbol<? extends Value> source = callBack.getCallbackSymbol();
		SootMethod method = callBack.getCallbackMethod();
		ISymbol<? extends Value> receiver = callBack.getCallbackSymbol();
		
		// We need to clone the stack so that objects are shared
		Stack oldStack = (Stack)this.peekStack();
		Stack stack = oldStack.clone();
		this.push(stack);
		
		CloneHistory.instance().branch();

		// We need to create a stack frame now, if user did not configure arguments
		// then lets do it here
		List<ISymbol<? extends Value>> arguments = callBack.getArguments();
		if(arguments == null) {
			arguments = new ArrayList<ISymbol<? extends Value>>();
			ExtensionManager extManager = ExtensionManager.instance();
			for(int i = 0; i < method.getParameterCount(); ++i) {
				Type t = method.getParameterType(i);
				ISymbol<? extends Value> arg = extManager.getSymbolicObject(AbstractValue.fromObject(t), true, true);
				arguments.add(arg);
			}
		}
		
		Context newContext = new Context(method);
		this.sFrame = new StackFrame(this.peekStack(), newContext, receiver, arguments);
		this.push(sFrame);

		// Next statement to be processed
		this.currentStmt = (Stmt)sFrame.getGraph().getHeads().get(0);
		
		// Configure stack frame
		List<Stmt> children = Collections.emptyList();
		sFrame.setBranch(0);
		sFrame.setBranches(children);
		
		this.processCheckPoint(ICheckPoint.Interest.AfterStackCloned);
		
		// This is the starting point of method entry
		this.processCheckPoint(ICheckPoint.Interest.MethodStart);
	}

	
	/**
	 * Returns the children of the call site after restoring caller stack frame.
	 * @return
	 */
	private List<Stmt> processNonEntryPathEnd() {
		// Now Add the last statement
		this.currentStmt = sFrame.getContext().getCallSite();
		this.peekStack().getPath().add(this.currentStmt);
		
		// Pop the sFrame out, we are done with this method
		this.pop();
		
		// Now restore the stack frame for the return point
		StackFrame restoredFrame = (StackFrame)this.peek();
		this.restoreStackFrameForReturn(restoredFrame, this.sFrame);
		List<Stmt> children = restoredFrame.getUnExceptionalSuccOf(this.currentStmt);
		if(children.size() > 1) 
			throw new UnsupportedOperationException("A statement with invoke expression cannot have more than one un-exceptional successor.");
		this.currentStmt = children.get(0);
		this.sFrame = restoredFrame;
		return children;
	}
	
	private void processMultiChild() {
		// TODO: Need to perform satisfiability test here before cloning
		
		Stack oldStack = (Stack)this.peekStack();
		// More than one successor, so, we need to clone the stack
		Stack stack = oldStack.clone();
		this.push(stack);
		sFrame = (StackFrame)this.peek();

		// Now Configure the next branch (0th branch) to be processed
		sFrame.setBranch(0);
		this.currentStmt = children.get(0);

		// Update the backtrack-point to reflect the next to next branch to be processed
		((StackFrame)oldStack.peek()).setBranch(1); 
		
		// Also start a new branch for recording clone history
		CloneHistory.instance().branch();
		
		// TODO: We need to update the state according to the fall and branch behavior
		// or if its a switch statement then according to the value associated to the target
		// 0 - Always falls through
	}
	
	// TODO: Responsible for handling the result
	private void processResult(int result) {
		if(result == Path.EXCEPTION) {
			// TODO: Algorithm for resolving correct catch block goes here
			// Right now just treating it as infeasible
		}
		else if(result == Path.FEASIBLE) {
		}
		else if(result == Path.UNKNOWN) {
			// TODO: Actions for unknown condition
		}
		else {
			throw new UnsupportedOperationException("StmtAbstract.execute() should either report feasible, infeasible, exception, or unknown constant.");
		}
		// Infeasible is impossible for this method
	}
	
	private void processInlining() {
		// Assuming that the statement contains invoke expression at this point
		this.invokedMethod = this.currentStmt.getInvokeExpr().getMethod();
		Object[] data = new Object[3];
		if(this.shouldInline(this.currentStmt, data)) {
			Path path = this.peekStack().getPath();
			path.add(this.currentStmt);
			
			// We need to create a stack frame now
			SootMethod method = (SootMethod)data[0];
			@SuppressWarnings("unchecked")
			ArrayList<ISymbol<? extends Value>> arguments = (ArrayList<ISymbol<? extends Value>>)data[2];
			@SuppressWarnings("unchecked")
			ISymbol<? extends Value> receiver = (ISymbol<? extends Value>)data[1];
			Context newContext = new Context(this.currentStmt, method, method.getDeclaringClass());
			this.sFrame = new StackFrame(this.peekStack(), newContext, receiver, arguments);
			this.push(sFrame);

			// Next statement to be processed
			this.currentStmt = (Stmt)sFrame.getGraph().getHeads().get(0);
			
			// Configure stack frame
			List<Stmt> children = Collections.emptyList();
			sFrame.setBranch(0);
			sFrame.setBranches(children);
			
			// This is the starting point of method entry
			this.processCheckPoint(ICheckPoint.Interest.MethodStart);
		}
	}
	
	
	private void processCheckPoint(ICheckPoint.Interest interest) {
		Set<ISymbol<? extends Value>> heap = this.getHeap();
		IStack stack = null;
		IStackFrame frame = null;
		if(!this.isEmpty())
			stack = this.peekStack();
		if(stack != null && !stack.isEmpty())
			frame = stack.peek();
		Set<ICheckPoint> checkPoints = ExtensionManager.instance().getCheckPoints(interest);
		Reporter reporter = Reporter.instance();
		for(ICheckPoint checkPoint : checkPoints) {
			if(monitor.isCanceled())
				return;
			reporter.report(checkPoint.check(this.currentStmt, frame, stack, heap));
		}
	}
	
	
	private void restoreStackFrameForReturn(StackFrame restoredFrame, StackFrame calledFrame) {
		Stmt callSite = calledFrame.getContext().getCallSite();
		if(callSite instanceof AssignStmt) {
			// If call site is a assignment statement, then restore the return value
			Value value = ((AssignStmt) callSite).getLeftOp();
			if(!(value instanceof Local))
				throw new UnsupportedOperationException("Invoke expression must be assigned to a local variable.");
			restoredFrame.put(value, calledFrame.getReturnValue());
		}
		
		// Restore argument values possibly changed due to the cloning effect during branching
		InvokeExpr invokeExpr = callSite.getInvokeExpr();
		List<ISymbol<? extends Value>> arguments = calledFrame.getArguments();
		for(int i = 0; i < invokeExpr.getArgCount(); ++i) {
			Value arg = invokeExpr.getArg(i);
			if(arg instanceof Local) {
				restoredFrame.put(arg, arguments.get(i));
			}
		}
		
		// Restore the receiver value possibly changed due to the cloning effect during branching
		if(invokeExpr instanceof InstanceInvokeExpr) {
			Value base = ((InstanceInvokeExpr) invokeExpr).getBase();
			restoredFrame.put(base, calledFrame.getReceiver());
		}
	}
	
	
	// For communication between execute() and shouldInline() method
	private boolean shouldInline(Stmt stmt, Object[] data) {
		ExtensionManager extManager = ExtensionManager.instance();
		InvokeExpr invokeExpr = stmt.getInvokeExpr();

		// For DynamicInvokeExpr, we do not in-line
		if(invokeExpr instanceof DynamicInvokeExpr)
			return false;
		
		SootMethod method = invokeExpr.getMethod();
		if(method.isNative() || method.isPhantom())
			return false;
		
		List<ISymbol<? extends Value>> arguments = Invoke.getArguments(invokeExpr);
		data[2] = arguments;
		if(invokeExpr instanceof StaticInvokeExpr) {
			data[0] = method;
			return extManager.shouldInline(method, null, arguments, stmt);
		}
		
		// <java.lang.Object.<init>() will not be expanded
		// And constructor that calls itself will not be expanded as well
		if(method.getName().equals(INIT) && method.getDeclaringClass().getName().equals(OBJECT))
			return false;
		
		// Must be InstanceInvokeExpr
		InstanceInvokeExpr insInvokeExpr = (InstanceInvokeExpr)stmt.getInvokeExpr();
		Value base = insInvokeExpr.getBase();
		ISymbol<? extends Value> symReceiver = Interpreter.instance().lookup(base);
		data[1] = symReceiver;
		RefType type = (RefType)symReceiver.getType();
 
		SootClass baseClass = type.getSootClass();
		
		// The receiver should not be of an abstract type and the method should not be phantom or native
		if(baseClass.isAbstract())
			return false;
		
		// Resolve using CHA
		FastHierarchy fastHierarchy = Scene.v().getOrMakeFastHierarchy();
		
		SootMethod resolvedMethod = null;
		if(insInvokeExpr instanceof SpecialInvokeExpr) {
			resolvedMethod = fastHierarchy.resolveSpecialDispatch((SpecialInvokeExpr)insInvokeExpr, method);			
		}
		else { // VirtualInvokeExpr
			resolvedMethod = fastHierarchy.resolveConcreteDispatch(baseClass, method);
		}
		SootMethod currentMethod = this.peek().getContext().getSootMethod();
		
		// We do not want to recurse on constructor
		if(method.getName().equals(INIT) && currentMethod.equals(resolvedMethod))
			return false;
		
		data[0] = resolvedMethod;
		return extManager.shouldInline(resolvedMethod, symReceiver, arguments, stmt);
	}
	
	public void printPath(Path path, int count) {
		System.out.println("Path [" + count + "]");
		if(this.exception != null) {
			System.out.println(this.exception);
		}
		System.out.println("------------------------------------");
		System.out.println(path);
		
		Stmt last = path.get(path.size() - 1);
		if(last instanceof ReturnStmt) {
			Value value = ((ReturnStmt) last).getOp();
			if(value instanceof Constant) {
				System.out.println("Returned: " + value);
			}
			else if(value instanceof Local) {
				System.out.println("Returned: " + this.lookup(value));
			}
			else if(value instanceof Ref) {
				System.out.println("Returned: " + this.lookup(AbstractValue.fromObject(value)));
			}			
		}
		System.out.println("------------------------------------");
	}	
}
