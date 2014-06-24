/*
 * Result.java
 * Mar 12, 2011
 *
 * Serl's Software Expert (SE) System
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 * 
 * Contact Us:
 * Chandan Raj Rupakheti (rupakhcr@clarkson.edu)
 * Daqing Hou (dhou@clarkson.edu)
 * Clarkson University
 * PO Box 5722
 * Potsdam
 * NY 13699-5722
 * http://serl.clarkson.edu
*/
 
package edu.clarkson.serl.critic.extension;

import java.util.SortedSet;
import java.util.TreeSet;

import soot.Value;

import edu.clarkson.serl.critic.interpreter.ISymbol;


/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class Result implements IResult {
	private ISymbol<? extends Value> result;
	private SortedSet<ICritic> ciritics;
	
	public Result(ISymbol<? extends Value> result) {
		this.result = result;
		this.ciritics = new TreeSet<ICritic>();
	}
	
	public Result(ISymbol<? extends Value> result, SortedSet<ICritic> critics) {
		this.result = result;
		this.ciritics = critics;
	}

	public ISymbol<? extends Value> getValue() {
		return result;
	}
	
	public SortedSet<ICritic> getCritics() {
		return this.ciritics;
	}

	public boolean add(ICritic critic) {
		return this.ciritics.add(critic);
	}

	public boolean add(IResult result) {
		return this.ciritics.addAll(result.getCritics());
	}

	public boolean remove(ICritic critic) {
		return this.ciritics.remove(critic);
	}

	public boolean remove(IResult result) {
		return this.ciritics.removeAll(result.getCritics());
	}
}
