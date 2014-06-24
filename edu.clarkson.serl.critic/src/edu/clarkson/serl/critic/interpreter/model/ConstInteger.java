/*
 * ConstInteger.java
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
 
package edu.clarkson.serl.critic.interpreter.model;

import soot.jimple.IntConstant;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class ConstInteger extends ConstArithmetic<IntConstant> {
	/* 
	 * The implementation of cache has been copied from the
	 * java.lang.Integer.IntegerCache class of JDK with minor
	 * modification.
	 * 
	 * @author  Lee Boynton
	 * @author  Arthur van Hoff
	 * @author  Josh Bloch
	 * @version 1.90, 05/11/04
	 */
    private static final ConstInteger cache[] = new ConstInteger[-(-128) + 127 + 1];
    static{
	    for(int i = 0; i < cache.length; i++)
	    	cache[i] = new ConstInteger(IntConstant.v(i - 128));
    }
    
	public static final ConstInteger TRUE = ConstInteger.fromInteger(1);
	public static final ConstInteger FALSE = ConstInteger.fromInteger(0);

	/**
	 * Returns the integer constant for the supplied integer value.
	 * @param i 
	 * @return
	 */
	public static ConstInteger fromInteger(int j) {
		final int offset = 128;
		if (j >= -128 && j <= 127) { // must cache 
		    return cache[j + offset];
		}
		return new ConstInteger(IntConstant.v(j));
	}
	
	public static ConstInteger fromBoolean(boolean b) {
		if(b)
			return TRUE;
		else
			return FALSE;
	}

	/**
	 * @param sootValue
	 */
	public ConstInteger(IntConstant sootValue) {
		super(sootValue);
		this.result = sootValue.value;
	}
}
