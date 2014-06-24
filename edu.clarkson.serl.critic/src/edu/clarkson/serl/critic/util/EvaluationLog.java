/*
 * EvaluationLog.java
 * Apr 27, 2012
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import edu.clarkson.serl.critic.extension.ICritic;

import soot.SootMethod;

/**
 * Maintains log of CriticAL's performance.
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class EvaluationLog {
	private EvaluationLog(){}
	
	private static ArrayList<Program> list;
	private static long startTime;
	private static long endTime;
	
	private static Program top() {
		return list.get(list.size() - 1);
	}
	
	public static void startCritic() {
		list = new ArrayList<Program>();
		startTime = System.currentTimeMillis();
	}
	
	public static void endCritic() {
		endTime = System.currentTimeMillis();
	}

	public static void startProgram(SootMethod method) {
		list.add(new Program(method));
	}
	
	public static void endProgram() {
		top().endTime = System.currentTimeMillis();
	}
	
	public static void newPath() {
		Program p = top();
		p.paths++;
	}
	
	public static void put(SootMethod method, int loops) {
		Program p = top();
		p.methodToLoops.put(method, loops);
	}
	
	public static void newStmt() {
		Program p = top();
		p.statements++;
	}
	
	public static void add(ICritic critic) {
		Program p = top();
		String key;

		if(critic.getType() == ICritic.Type.Explanation) {
			key = "E | " + critic.getId() + " | " +  critic.getLineNumber();
			p.explanations.add(key);
		}
		else if(critic.getType() == ICritic.Type.Recommendation) {
			key = "R | " + critic.getId() + " | " + critic.getLineNumber();
			p.recommendations.add(key);
		}
		else {
			key = "C | " + critic.getId() + " | " + critic.getLineNumber();
			p.criticisms.add(key);
		}
	}
	
	private static class Program {
		private SootMethod method;
		private int statements;
		private int paths;
		private long startTime;
		private long endTime;
		private HashMap<SootMethod, Integer> methodToLoops;
		private TreeSet<String> explanations;
		private TreeSet<String> recommendations;
		private TreeSet<String> criticisms;
		
		public Program(SootMethod method) {
			this.startTime = System.currentTimeMillis();
			this.method = method;
			this.paths = 0;
			this.statements = 0;
			this.methodToLoops = new HashMap<SootMethod, Integer>();
			this.explanations = new TreeSet<String>();
			this.recommendations = new TreeSet<String>();
			this.criticisms = new TreeSet<String>();
		}
	}
	
	private static final String LS = "----------------------------------------------------------------\n";
	private static final String NL = "\n";
	private static final String NP = "================================================================\n";
	private static final String TB = "\t";
	private static final String SP = " , ";
	
	public static String getString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(LS);
		buffer.append("Evaluation Log" + NL);
		buffer.append("Total Programs | " + list.size() + NL);
		buffer.append("Total Time | " + (endTime-startTime) + NL);
		buffer.append(NP);

		long totalTime = 0;
		for(Program p : list) {
			buffer.append("Method | " + p.method + NL);
			buffer.append("Statements | " + p.statements + NL);
			buffer.append("Paths | " + p.paths + NL);
			long programTime = p.endTime - p.startTime;
			totalTime += programTime;
			buffer.append("Program Time | " + programTime + NL);

			// Method to loops
			buffer.append(LS);
			buffer.append(TB + "Methods | Loops" + NL);
			buffer.append(LS);
			int total = 0;
			for(Map.Entry<SootMethod, Integer> e : p.methodToLoops.entrySet()) {
				SootMethod m = e.getKey();
				int loop = e.getValue();
				total += loop;
				
				buffer.append(TB + m + " | " + loop + NL);
			}
			buffer.append(TB + "Total Loops | " + total + NL);
			buffer.append(LS);
			
			populate(p.criticisms, "Criticisms", buffer);
			populate(p.recommendations, "Recommendation", buffer);
			populate(p.explanations, "Explanations", buffer);
			
			total = p.explanations.size() + p.recommendations.size() + p.criticisms.size();
			buffer.append("Total Critiques | " + total + NL);
			buffer.append(NP);
		}
		buffer.append("Total Program Time | " + totalTime + NL);
		return buffer.toString();
	}
	
	private static void populate(TreeSet<String> set, String type, StringBuffer buffer) {
		buffer.append(TB + type + " | Count" + NL);
		buffer.append(LS);
		for(String s : set) {
			buffer.append(TB + s + NL);
		}
		buffer.append(TB + "Total " + type + " | " + set.size() + NL);
		buffer.append(LS);
	}

	public static String getCSVString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(LS);
		Date d = new Date();
		buffer.append("Evaluation Log | " + d + NL);
		buffer.append(LS);
		buffer.append("Total Programs | " + list.size() + NL);
		buffer.append("Total Time | " + (endTime-startTime) + NL);
		buffer.append(NP);
		
		buffer.append("S.No. , Stmts , Time , Loops , Paths , Criticisms , Recommendations , Explanations , Total Critiques, Entry Point" + NL);
		int sNo = 1;
		for(Program p : list) {
			long programTime = p.endTime - p.startTime;

			// Method to loops
			int totalLoops = 0;
			for(Integer e : p.methodToLoops.values()) {
				totalLoops += e;
			}
			int totalCritics = p.explanations.size() + p.recommendations.size() + p.criticisms.size();
			buffer.append(sNo + SP + p.statements + SP + programTime + SP + totalLoops + SP + p.paths + SP 
					+ p.criticisms.size() + SP + p.recommendations.size() + SP + p.explanations.size() + SP +
					totalCritics + SP + p.method + NL);
			++sNo;
		}
		return buffer.toString();
	}
}
