/*
 * CloneHistory.java
 * Mar 3, 2012
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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import edu.clarkson.serl.critic.util.Util;

import soot.Value;

/**
 * A singleton class backed by a {@link HashSet} to maintain the history of cloning. The history is 
 * maintained at the granularity of branches. A stack is used that holds a set of {@link ClonePair}
 * for each newly created branches that is currently executed by the {@link Interpreter}.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class CloneHistory {
	/**
	 * A class representing a pair of a symbol and its clone. This class does not distinguish two
	 * pairs based on the order of the original and clone. i.e. 
	 * <tt>(original,clone) = (clone,original)</tt>
	 * 
	 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
	 */
	public static class ClonePair {
		ISymbol<? extends Value> original;
		ISymbol<? extends Value> clone;
		
		public ClonePair(ISymbol<? extends Value> original, ISymbol<? extends Value> clone) {
			this.original = original;
			this.clone = clone;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((clone == null) ? 0 : clone.hashCode() + 
					((original == null) ? 0 : original.hashCode()));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ClonePair other = (ClonePair) obj;
			
			if((eq(this.clone, other.clone) && eq(this.original, other.original)) ||
					(eq(this.clone, other.original) && eq(this.original, other.clone)))
				return true;
			return false;
		}
		
		private static boolean eq(ISymbol<? extends Value> a, ISymbol<? extends Value> b) {
			if(a == null) {
				if (b != null)
					return false;
			} 
			else if(!a.equals(b))
				return false;
			return true;
		}

		/**
		 * Gets the original object.
		 * @return
		 */
		public ISymbol<? extends Value> getOriginal() {
			return original;
		}

		/**
		 * Gets the clone object.
		 * @return
		 */
		public ISymbol<? extends Value> getClone() {
			return clone;
		}
		
		
	}
	
	private static CloneHistory history = null;
	
	/**
	 * Create an singleton instance for maintaining history of cloning.
	 * @return 
	 */
	public static CloneHistory instance() {
		if(history == null)
			history = new CloneHistory();
		return history;
	}
	
	/**
	 * Resets the clone history singleton object.
	 */
	public static void reset() {
		history = null;
	}
	
	private LinkedList<HashSet<ClonePair>> stack;

	/**
	 * Creates CloneHistory.
	 */
	private CloneHistory() {
		this.stack = new LinkedList<HashSet<ClonePair>>();
		this.branch();
	}
	
	/**
	 * <b>Note:</b> Not to be used by a client code. It adds a new frame containing 
	 * a set of clone pairs for the newly encountered branch.
	 */
	void branch() {
		HashSet<ClonePair> set = new HashSet<ClonePair>();
		this.stack.addLast(set);
	}
	
	/**
	 * <b>Note:</b> Not to be used by a client code. It drops the most recent frame containing 
	 * a set of clone pairs after a stack is finished executing.
	 */
	void pop() {
		this.stack.removeLast();
	}
	
	/**
	 * Add the original and its corresponding clone object to the history.  
	 * @param original The original object.
	 * @param clone The cloned object.
	 */
	public void add(ISymbol<? extends Value> original, ISymbol<? extends Value> clone) {
		HashSet<ClonePair> set = this.stack.getLast();
		set.add(new ClonePair(original, clone));
	}
	
	/**
	 * Returns the right {@link ISymbol} that is shared by the supplied stack for its corresponding
	 * <tt>symbol</tt> supplied as a parameter.
	 * @param symbol The symbol to look for in the clone history.
	 * @param stacks The stacks the returned symbol should belong to.
	 * @return The {@link ISymbol} which is a pair of the supplied <tt>symbol</tt> in the supplied stacks
	 * or <tt>null</tt> is the pair is not found. 
	 */
	public ISymbol<? extends Value> getInStacksSymbol(ISymbol<? extends Value> symbol, Set<IStack> stacks) {
		ListIterator<HashSet<ClonePair>> iterator = this.stack.listIterator(this.stack.size());
		while(iterator.hasPrevious()) {
			HashSet<ClonePair> set = iterator.previous();
			for(ClonePair p : set) {
				if(p.original.equals(symbol) || p.clone.equals(symbol)) {
					if(!Util.intersection(p.original.getStacks(), stacks).isEmpty())
						return p.original;
					if(!Util.intersection(p.clone.getStacks(),stacks).isEmpty())
						return p.clone;
				}
			}
		}
		return null;
	}
//	public ISymbol<? extends Value> getInStacksSymbol(ISymbol<? extends Value> symbol, Set<IStack> stacks) {
//		HashSet<ClonePair> set = this.stack.getLast();
//		for(ClonePair p : set) {
//			if(p.original.equals(symbol) || p.clone.equals(symbol)) {
//				if(!Util.intersection(p.original.getStacks(), stacks).isEmpty())
//					return p.original;
//				if(!Util.intersection(p.clone.getStacks(),stacks).isEmpty())
//					return p.clone;
//			}
//		}
//		return null;
//	}	
	
	/**
	 * Returns the right {@link ISymbol} that is shared by the supplied stack for its corresponding
	 * <tt>symbol</tt> supplied as a parameter.
	 * @param symbol The symbol to look for in the clone history.
	 * @param stacks The stacks the returned symbol should belong to.
	 * @return The {@link ISymbol} which is a pair of the supplied <tt>symbol</tt> in the supplied stacks
	 * or <tt>null</tt> is the pair is not found. 
	 */
	public ISymbol<? extends Value> getOutOfStacksSymbol(ISymbol<? extends Value> symbol, Set<IStack> stacks) {
		ListIterator<HashSet<ClonePair>> iterator = this.stack.listIterator(this.stack.size());
		while(iterator.hasPrevious()) {
			HashSet<ClonePair> set = iterator.previous();
			for(ClonePair p : set) {
				if(p.original.equals(symbol) || p.clone.equals(symbol)) {
					if(Util.intersection(p.original.getStacks(), stacks).isEmpty())
						return p.original;
					if(Util.intersection(p.clone.getStacks(),stacks).isEmpty())
						return p.clone;
				}
			}
		}
		return null;
	}
//	public ISymbol<? extends Value> getOutOfStacksSymbol(ISymbol<? extends Value> symbol, Set<IStack> stacks) {
//		HashSet<ClonePair> set = this.stack.getLast();
//		for(ClonePair p : set) {
//			if(p.original.equals(symbol) || p.clone.equals(symbol)) {
//				if(Util.intersection(p.original.getStacks(), stacks).isEmpty())
//					return p.original;
//				if(Util.intersection(p.clone.getStacks(),stacks).isEmpty())
//					return p.clone;
//			}
//		}
//		return null;
//	}
	
	/**
	 * Checks whether the given pair of original and clone symbol are present in the clone history.
	 * The order of parameters does not matter.
	 * 
	 * @param original A symbol in the pair of original and clone.
	 * @param clone A symbol in the pair of original and clone.
	 * @return <tt>true</tt> if the pair of original and clone was found in the clone history and <tt>false</tt> otherwise.
	 */
	public boolean contains(ISymbol<? extends Value> original, ISymbol<? extends Value> clone) {
		ListIterator<HashSet<ClonePair>> iterator = this.stack.listIterator(this.stack.size());
		while(iterator.hasPrevious()) {
			HashSet<ClonePair> set = iterator.previous();
			if(set.contains(new ClonePair(original, clone)))
				return true;
		}
		return false;
	}
//	public boolean contains(ISymbol<? extends Value> original, ISymbol<? extends Value> clone) {
//		HashSet<ClonePair> set = this.stack.getLast();
//		return set.contains(new ClonePair(original, clone));
//	}
}
