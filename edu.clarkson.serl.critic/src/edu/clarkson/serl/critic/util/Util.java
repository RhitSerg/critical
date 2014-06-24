/*
 * Util.java
 * Aug 19, 2011
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
 
package edu.clarkson.serl.critic.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;

import edu.clarkson.serl.critic.CriticPlugin;
import edu.clarkson.serl.critic.loader.SootClassLoader;

import soot.Local;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.ExceptionalBlockGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class Util {
	/**
	 * Computes the union of the supplied collections.
	 * 
	 * @param c1 First collection.
	 * @param c2 Second collection.
	 * @return Returns the union of the supplied collections as a {@link Set}.
	 */
	public static <T> Set<T> union(Collection<T> c1, Collection<T> c2) {
		Set<T> set = new HashSet<T>();
		set.addAll(c1);
		set.addAll(c2);
		return set;
	}
	
	/**
	 * Computes the intersection of the supplied collections.
	 * 
	 * @param c1 First collection.
	 * @param c2 Second collection.
	 * @return Returns the intersection of the supplied collections as a {@link Set}.
	 */
	public static <T> Set<T> intersection(Collection<T> c1, Collection<T> c2) {
		Set<T> set = new HashSet<T>();
		for(T t : c1) {
			if(c2.contains(t)) {
				set.add(t);
			}
		}
		return set;
	}
	
	/**
	 * Computes the set difference between the supplied collections. i.e. c1-c2
	 * 
	 * @param c1 First collection.
	 * @param c2 Second collection.
	 * @return Returns the set difference between the supplied collection as a {@link Set}.
	 */
	public static <T> Set<T> difference(Collection<T> c1, Collection<T> c2) {
		Set<T> set = new HashSet<T>();
		for(T t : c1) {
			if(!c2.contains(t)) {
				set.add(t);
			}
		}
		return set;
	}
	
	/**
	 * Computes the xor of the supplied collections. i.e. {c1-c2} + {c2-c1}
	 * 
	 * @param c1 First collection.
	 * @param c2 Second collection.
	 * @return Returns the xor of the supplied collection as a {@link Set}.
	 */
	public static<T> Set<T> xor(Collection<T> c1, Collection<T> c2) {
		Set<T> diff1 = difference(c1, c2);
		Set<T> diff2 = difference(c2, c1);
		return union(diff1, diff2);
	}
	
	/**
	 * Provides topological ordering of the units in the given unit graph.
	 * This ordering is such that all the units executed first will appear
	 * before the units that is executed later.
	 * 
	 * @param unitGraph UnitGraph of the analyzed method
	 * @return List of unit where the order of execution is preserved from top to bottom.
	 */
	public static List<Unit> getTopologicalOrdering(UnitGraph unitGraph) {
		if(unitGraph == null)
			return new ArrayList<Unit>();
		
		List<Unit> orderedList = new ArrayList<Unit>();
		List<Unit> visitedList = new ArrayList<Unit>();
		List<Unit> entries = unitGraph.getHeads();
		for(Unit entry : entries) {
			depthFirstTopologicalTraversal(entry, unitGraph, orderedList, visitedList);
		}
		return orderedList;
	}
	
	private static void depthFirstTopologicalTraversal(Unit unit, UnitGraph unitGraph, List<Unit> orderedList, List<Unit> visitedList) {
		if(!visitedList.contains(unit))
			visitedList.add(unit);
		for(Unit childUnit : unitGraph.getSuccsOf(unit)) {
			if(!visitedList.contains(childUnit)) // Stops recursion for "for, while and do while" statements
				depthFirstTopologicalTraversal(childUnit, unitGraph, orderedList, visitedList);
		}
		if(!orderedList.contains(unit)) {
			orderedList.add(0, unit);
		}
	}
	
	/**
	 * Gets the set of local involved in the supplied unit graph.
	 * @param unitGraph
	 * @return 
	 */
	public static Set<Local> getLocalsSet(UnitGraph unitGraph) {
		Map<Local, Integer> map = getLocalToLineNumberMap(unitGraph);
		return map.keySet();
	}
	
	public static Map<Local, Integer> getLocalToLineNumberMap(UnitGraph unitGraph) {
		Map<Local, Integer> newMap = new HashMap<Local, Integer>();
		PatchingChain<Unit> unitsChain = unitGraph.getBody().getUnits();
		for(Unit unit : unitsChain) {
			int lineNumber = getLineNumber(unit);
			List<ValueBox> valueBoxList = unit.getDefBoxes();
			for(ValueBox valueBox : valueBoxList) {
				Value value = valueBox.getValue();
				if(value instanceof Local) {
					Local local = (Local)value;
					newMap.put(local, new Integer(lineNumber));
				}
			}
		}
		return newMap;
	}

	
	public static Map<Integer,Local> getLineNumberToLocalMap(UnitGraph unitGraph) {
		Map<Integer,Local> newMap = new HashMap<Integer,Local>();
		PatchingChain<Unit> unitsChain = unitGraph.getBody().getUnits();
		for(Unit unit : unitsChain) {
			int lineNumber = getLineNumber(unit);
			List<ValueBox> valueBoxList = unit.getDefBoxes();
			for(ValueBox valueBox : valueBoxList) {
				Value value = valueBox.getValue();
				if(value instanceof Local) {
					Local local = (Local)value;
					newMap.put(new Integer(lineNumber),local);
				}
			}
		}
		return newMap;
	}
	
	public static synchronized ExceptionalUnitGraph getUnitGraph(SootMethod method) {
			JimpleBody jimpleBody = (JimpleBody)method.retrieveActiveBody();
			ExceptionalUnitGraph unitGraph = new ExceptionalUnitGraph(jimpleBody);
			return unitGraph;
	}
	
	public static int getLineNumber(Unit unit) {
		if(unit == null)
			return -1;
		List<Tag> tagList = unit.getTags();
		for(Tag tag : tagList) {
			if(tag instanceof LineNumberTag) {
				LineNumberTag lineNumberTag = (LineNumberTag)tag;
				return lineNumberTag.getLineNumber();
			}	
		}
		return -1;
	}
	
	public static boolean fallsFrom(Unit parentUnit, Unit childUnit, ExceptionalUnitGraph unitGraph) {
		try {
			List<Unit> successors = unitGraph.getUnexceptionalSuccsOf(parentUnit);
			if(parentUnit.fallsThrough()) 
				return successors.get(0).equals(childUnit);
		}
		catch(Exception e) {
		}
		return false;
	}
	
	public static boolean fallsFrom(Block parentBlock, Block childBlock, ExceptionalBlockGraph blockGraph) {
		try {
			List<Block> successors = blockGraph.getUnexceptionalSuccsOf(parentBlock);
			if(parentBlock.getTail().fallsThrough())
				return successors.get(0).equals(childBlock);
		}
		catch(Exception e) {
		}
		return false;
	}	
	
	public static boolean branchesFrom(Unit parentUnit, Unit childUnit, ExceptionalUnitGraph unitGraph) {
		try {
			List<Unit> successors = unitGraph.getUnexceptionalSuccsOf(parentUnit);
			if(parentUnit.branches()) {
				if(successors.size() == 2)
					return successors.get(1).equals(childUnit);
				return successors.get(0).equals(childUnit);
			}
		}
		catch(Exception e) {
		}
		return false;
	}
	
	public static boolean branchesFrom(Block parentBlock, Block childBlock, ExceptionalBlockGraph blockGraph) {
		try {
			List<Block> successors = blockGraph.getUnexceptionalSuccsOf(parentBlock);
			if(parentBlock.getTail().branches()) {
				if(successors.size() == 2)
					return successors.get(1).equals(childBlock);
				return successors.get(0).equals(childBlock);
			}
		}
		catch(Exception e) {
		}
		return false;
	}
	
	public static boolean exceptionFrom(Unit parentUnit, Unit childUnit, ExceptionalUnitGraph unitGraph) {
		try {
			List<Unit> successors = unitGraph.getExceptionalSuccsOf(parentUnit);
			if(successors.size() > 0)
				return successors.contains(childUnit);
		}
		catch(Exception e) {
		}
		return false;
	}

	public static boolean exceptionFrom(Block parentBlock, Block childBlock, ExceptionalBlockGraph blockGraph) {
		try {
			List<Block> successors = blockGraph.getExceptionalSuccsOf(parentBlock);
			if(successors.size() > 0)
				return successors.contains(childBlock);
		}
		catch(Exception e) {
		}
		return false;
	}
	
	public static String getOuterMostClass(SootClass clazz) {
		if(clazz == null)
			return "";
		
		while(clazz.hasOuterClass()) {
			clazz = clazz.getOuterClass();
		}
		
		String name = clazz.getName();

		int index = name.indexOf('$');
		if(index > 0)
			name = name.substring(0, index);
		
		return name;
	}
	
	public static void printJimpleBody(SootMethod method, OutputStream outStream) {
		PrintStream out = new PrintStream(outStream);
		JimpleBody jimpleBody = (JimpleBody)method.retrieveActiveBody();
		out.println("############################################################");
		out.println("Jimple Body for: " + method);
		out.println("############################################################");
		for(Unit aUnit : jimpleBody.getUnits()) {
			out.println(aUnit);
		}
		out.println();
		out.flush();
	}
	
	public static void printTopologicalOrdering(SootMethod method, OutputStream outStream) {
		PrintStream out = new PrintStream(outStream);
		List<Unit> ordering = Util.getTopologicalOrdering(Util.getUnitGraph(method));
		out.println("############################################################");
		out.println("Topological Ordering for: " + method);
		out.println("############################################################");
		for(Unit aUnit : ordering) {
			out.println(aUnit);
		}
		out.println();
		out.flush();
	}
	
	
	public static void printUnitGraph(SootMethod method, OutputStream outStream) {
		PrintStream out = new PrintStream(outStream);
		ExceptionalUnitGraph unitGraph = Util.getUnitGraph(method);
		List<Unit> heads = unitGraph.getHeads();
		out.println("############################################################");
		out.println("ExceptionUnitGraph for: " + method);
		out.println("############################################################");
		for(Unit aUnit : heads) {
			List<Unit> processed = new ArrayList<Unit>();
			dfsPrintUnitGraph(aUnit, unitGraph, processed, 0, out);
		}
		out.println();
		out.flush();
	}
	
	private static void dfsPrintUnitGraph(Unit aUnit, ExceptionalUnitGraph unitGraph, List<Unit> processed, int tab, PrintStream out) {
		List<Unit> children = unitGraph.getSuccsOf(aUnit);

		if(processed.contains(aUnit)) {
			Util.printTab(tab, out);
			out.print("[LOOP DETECTED] " + aUnit + " --> [");
			for(Unit aChild : children) {
				out.print(aChild + ", ");
			}
			out.println("]");

			return;
		}

		Util.printTab(tab, out);
		out.print(aUnit + " --> [");
		for(Unit aChild : children) {
			out.print(aChild + ", ");
		}
		out.println("]");
		processed.add(aUnit);
		
		if(children.size() == 1) {
			dfsPrintUnitGraph(children.get(0), unitGraph, processed, tab, out);
		}
		else {
			for(Unit childUnit : children) {
				dfsPrintUnitGraph(childUnit, unitGraph, processed, tab + 1, out);
			}
		}
	}
	
	private static void printTab(int tab, PrintStream out) {
		for(int i = 0; i < tab; ++i) {
			out.print("=");
		}
		out.print(" ");
	}	
}
