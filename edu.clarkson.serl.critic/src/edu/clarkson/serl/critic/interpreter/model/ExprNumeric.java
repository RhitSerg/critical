/*
 * ExprNumeric.java
 * Jul 5, 2011
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
 
package edu.clarkson.serl.critic.interpreter.model;

import edu.clarkson.serl.critic.interpreter.ISymbol;
import soot.DoubleType;
import soot.FloatType;
import soot.LongType;
import soot.Type;
import soot.Value;
import soot.jimple.BinopExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.LongConstant;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public abstract class ExprNumeric<T extends BinopExpr> extends ExprBinary<T> {

	/**
	 * @param sootValue
	 * @param left
	 * @param right
	 */
	public ExprNumeric(T sootValue, ISymbol<? extends Value> left, ISymbol<? extends Value> right) {
		super(sootValue, left, right);
	}
	
	public ISymbol<? extends Value> execute() {
		// If unknown, then we will just return the tree
		if(this.isOpen()) {
			this.result = this;
			return this;
		}
		
		// Execute both branches first
		ISymbol<? extends Value> lTree = this.getLeft().execute();
		ISymbol<? extends Value> rTree = this.getRight().execute();
		
		// Perform all numeric operations in double precision
		double l = Double.parseDouble(lTree.getValue().toString());;
		double r = Double.parseDouble(rTree.getValue().toString());
		double result = this.execute(l,r);
		
		// Translate the result back to the correct result type
		Type retType = this.getSootValue().getType();
		if(retType instanceof DoubleType) {
			this.result = new ConstDouble(DoubleConstant.v(result)); 
			return (ConstDouble)this.result;
		}
		if(retType instanceof FloatType) {
			this.result = new ConstFloat(FloatConstant.v((float)result)); 
			return (ConstFloat)this.result;
		}
		if(retType instanceof LongType) {
			this.result = new ConstLong(LongConstant.v((long)result));
			return (ConstLong)this.result;
		}
		this.result = new ConstInteger(IntConstant.v((int)result)); 
		return (ConstInteger)this.result;
	}
	
	/**
	 * This method performs exact numeric operations in subtypes of {@link ExprNumeric}.
	 * 
	 * @param l The left value
	 * @param r The right value
	 * @return Thre result of performing concrete numeric operation on the supplied values.
	 */
	protected abstract double execute(double l, double r);	
}
