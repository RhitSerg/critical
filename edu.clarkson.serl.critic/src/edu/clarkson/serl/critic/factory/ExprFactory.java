/*
 * ExprFactory.java
 * Jul 11, 2011
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

import edu.clarkson.serl.critic.extension.ICheckPoint;
import edu.clarkson.serl.critic.interpreter.IStackFrame;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.model.ExprAdd;
import edu.clarkson.serl.critic.interpreter.model.ExprAnd;
import edu.clarkson.serl.critic.interpreter.model.ExprCast;
import edu.clarkson.serl.critic.interpreter.model.ExprCmp;
import edu.clarkson.serl.critic.interpreter.model.ExprCmpg;
import edu.clarkson.serl.critic.interpreter.model.ExprCmpl;
import edu.clarkson.serl.critic.interpreter.model.ExprDiv;
import edu.clarkson.serl.critic.interpreter.model.ExprEq;
import edu.clarkson.serl.critic.interpreter.model.ExprGe;
import edu.clarkson.serl.critic.interpreter.model.ExprGt;
import edu.clarkson.serl.critic.interpreter.model.ExprInstanceOf;
import edu.clarkson.serl.critic.interpreter.model.ExprLe;
import edu.clarkson.serl.critic.interpreter.model.ExprLength;
import edu.clarkson.serl.critic.interpreter.model.ExprLt;
import edu.clarkson.serl.critic.interpreter.model.ExprMul;
import edu.clarkson.serl.critic.interpreter.model.ExprNe;
import edu.clarkson.serl.critic.interpreter.model.ExprNeg;
import edu.clarkson.serl.critic.interpreter.model.ExprNew;
import edu.clarkson.serl.critic.interpreter.model.ExprNewArray;
import edu.clarkson.serl.critic.interpreter.model.ExprNewMulitArray;
import edu.clarkson.serl.critic.interpreter.model.ExprOr;
import edu.clarkson.serl.critic.interpreter.model.ExprRem;
import edu.clarkson.serl.critic.interpreter.model.ExprShl;
import edu.clarkson.serl.critic.interpreter.model.ExprShr;
import edu.clarkson.serl.critic.interpreter.model.ExprSub;
import edu.clarkson.serl.critic.interpreter.model.ExprUshr;
import edu.clarkson.serl.critic.interpreter.model.ExprXor;
import edu.clarkson.serl.critic.interpreter.model.InvokeDynamic;
import edu.clarkson.serl.critic.interpreter.model.InvokeInstance;
import edu.clarkson.serl.critic.interpreter.model.InvokeStatic;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.CmpExpr;
import soot.jimple.CmpgExpr;
import soot.jimple.CmplExpr;
import soot.jimple.Constant;
import soot.jimple.DivExpr;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.EqExpr;
import soot.jimple.ExprSwitch;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.LeExpr;
import soot.jimple.LengthExpr;
import soot.jimple.LtExpr;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.NegExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.OrExpr;
import soot.jimple.RemExpr;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.SubExpr;
import soot.jimple.UnopExpr;
import soot.jimple.UshrExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.XorExpr;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class ExprFactory implements ExprSwitch, IFactory {
	private Set<String> types;
	private ISymbol<? extends Value> expr;
	private ISymbol<? extends Value> left;
	private ISymbol<? extends Value> right;
	
	public ExprFactory() {
		// For expression types are not important
		types = Collections.emptySet();
	}
	
	public Set<String> getSupportedTypes() {
		return types;
	}

	public ISymbol<? extends Value> newSymbol(String type, Value value, boolean open, boolean mutable) {
		value.apply(this);
		ISymbol<? extends Value> ret = expr;
		
		// Releasing references to the computed fields		
		expr = null;
		left = null;
		right = null;
		
		return ret;
	}

	public void evaluateBinary(BinopExpr e) {
		Value leftOp = e.getOp1();
		Value rightOp = e.getOp2();
		
		IStackFrame stackFrame = Interpreter.instance().peek();
		ConstantFactory constFactory = new ConstantFactory();
		// For left
		if(leftOp instanceof Local) {
			left = stackFrame.lookup(leftOp);
		}
		else if(leftOp instanceof Constant) {
			left = constFactory.newSymbol(leftOp.getType().toString(), leftOp, false, false);
		}
		else {
			throw new UnsupportedOperationException("Operand of any expression must be either a local variable or a constant.");
		}
		
		// For right
		if(rightOp instanceof Local) {
			right = stackFrame.lookup(rightOp);
		}
		else if(rightOp instanceof Constant) {
			right = constFactory.newSymbol(rightOp.getType().toString(), rightOp, false, false);
		}
		else {
			throw new UnsupportedOperationException("Operand of any expression must be either a local variable or a constant.");
		}
	}
	
	public void evaluateUnary(UnopExpr e) {
		Value op = e.getOp();
		if(op instanceof Local) {
			IStackFrame stackFrame = Interpreter.instance().peek();
			left = stackFrame.lookup(op);
		}
		else if(op instanceof Constant) {
			ConstantFactory constFactory = new ConstantFactory();
			left = constFactory.newSymbol(op.getType().toString(), op, false, false);
		}
		else {
			throw new UnsupportedOperationException("Operand of any expression must be either a local variable or a constant.");
		}
	}

	public void caseAddExpr(AddExpr v) {
		this.evaluateBinary(v);
		expr = new ExprAdd(v, left, right);
	}

	public void caseAndExpr(AndExpr v) {
		this.evaluateBinary(v);
		expr = new ExprAnd(v, left, right);
	}

	public void caseCmpExpr(CmpExpr v) {
		this.evaluateBinary(v);
		expr = new ExprCmp(v, left, right);
	}

	public void caseCmpgExpr(CmpgExpr v) {
		this.evaluateBinary(v);
		expr = new ExprCmpg(v, left, right);
	}

	public void caseCmplExpr(CmplExpr v) {
		this.evaluateBinary(v);
		expr = new ExprCmpl(v, left, right);
	}

	public void caseDivExpr(DivExpr v) {
		this.evaluateBinary(v);
		expr = new ExprDiv(v, left, right);
	}

	public void caseNeExpr(NeExpr v) {
		this.evaluateBinary(v);
		expr = new ExprNe(v, left, right);
	}

	public void caseMulExpr(MulExpr v) {
		this.evaluateBinary(v);
		expr = new ExprMul(v, left, right);
	}

	public void caseOrExpr(OrExpr v) {
		this.evaluateBinary(v);
		expr = new ExprOr(v, left, right);
	}

	public void caseRemExpr(RemExpr v) {
		this.evaluateBinary(v);
		expr = new ExprRem(v, left, right);
	}

	public void caseShlExpr(ShlExpr v) {
		this.evaluateBinary(v);
		expr = new ExprShl(v, left, right);
	}

	public void caseShrExpr(ShrExpr v) {
		this.evaluateBinary(v);
		expr = new ExprShr(v, left, right);
	}

	public void caseUshrExpr(UshrExpr v) {
		this.evaluateBinary(v);
		expr = new ExprUshr(v, left, right);
	}

	public void caseSubExpr(SubExpr v) {
		this.evaluateBinary(v);
		expr = new ExprSub(v, left, right);
	}

	public void caseXorExpr(XorExpr v) {
		this.evaluateBinary(v);
		expr = new ExprXor(v, left, right);
	}

	public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
		expr = new InvokeInstance<SpecialInvokeExpr>(v);
	}

	public void caseStaticInvokeExpr(StaticInvokeExpr v) {
		expr = new InvokeStatic(v);
	}

	public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
		expr = new InvokeInstance<VirtualInvokeExpr>(v);
	}

	public void caseCastExpr(CastExpr v) {
		expr = new ExprCast(v);
	}

	public void caseDynamicInvokeExpr(DynamicInvokeExpr v) {
		expr = new InvokeDynamic(v);
	}

	public void caseEqExpr(EqExpr v) {
		this.evaluateBinary(v);
		expr = new ExprEq(v, left, right);
	}

	public void caseGeExpr(GeExpr v) {
		this.evaluateBinary(v);
		expr = new ExprGe(v, left, right);
	}

	public void caseGtExpr(GtExpr v) {
		this.evaluateBinary(v);
		expr = new ExprGt(v, left, right);
	}

	public void caseLeExpr(LeExpr v) {
		this.evaluateBinary(v);
		expr = new ExprLe(v, left, right);
	}

	public void caseLtExpr(LtExpr v) {
		this.evaluateBinary(v);
		expr = new ExprLt(v, left, right);
	}

	public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
		expr = new InvokeInstance<InterfaceInvokeExpr>(v);
	}

	public void caseInstanceOfExpr(InstanceOfExpr v) {
		expr = new ExprInstanceOf(v);
	}

	public void caseNewArrayExpr(NewArrayExpr v) {
		expr = new ExprNewArray(v);
	}

	public void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
		expr = new ExprNewMulitArray(v);
	}

	public void caseNewExpr(NewExpr v) {
		expr = new ExprNew(v);
	}

	public void caseLengthExpr(LengthExpr v) {
		this.evaluateUnary(v);
		expr = new ExprLength(v, left);
	}

	public void caseNegExpr(NegExpr v) {
		this.evaluateUnary(v);
		expr = new ExprNeg(v, left);
	}

	public void defaultCase(Object obj) {
		throw new UnsupportedOperationException("The supplied value is not supported expression in the library.");
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
		throw new UnsupportedOperationException("ExprFactory does not support getClassFor(type) operation.");
	}
	
	@Override
	public boolean shouldInline(SootMethod method, ISymbol<? extends Value> receiver, List<ISymbol<? extends Value>> arguments, Stmt callSite) {
		return false;
	}	
}
