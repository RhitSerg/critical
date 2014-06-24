/*
 * Preferences.java
 * Sept 3, 2011
 *
 * CriticAL : A Critic for APIs and Libraries
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
 * Chandan Raj Rupakheti (rupakhcr@clarkson.edu)
 * Daqing Hou (dhou@clarkson.edu)
 * Clarkson University
 * PO Box 5722
 * Potsdam
 * NY 13699-5722
 * http://critical.sourceforge.net
 */
package edu.clarkson.serl.critic.preferences;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("preferences")
public class Preferences {
	@XStreamAsAttribute
	private String type;
	
	@XStreamOmitField
	private long freeMemorySize;
	@XStreamOmitField
	private long availableMemorySize;
	@XStreamOmitField
	private long maxMemorySize;
	@XStreamOmitField
	private int threadPoolSize;

	@XStreamAlias("loop-unroll-limit")
	private int loopUnrollLimit;

	@XStreamAlias("method-depth")
	private int methodDepth;
	
	@XStreamAlias("path-cuttoff-limit")
	private int pathCutOffSize;
	
	@XStreamAlias("thread-delay")
	private long delay;

	@XStreamAlias("thread-pool-wait-time")
	private long poolWaitTime;
	
	@XStreamAlias("soot-options")
	private TreeSet<Pair> sootOptions;
	
	// XStream Bug: Just having @XStreamImplicit(itemFieldName="extension-disabled") without
	// @XStreamAlias("disabled-extensions") apparently does not work!
	@XStreamAlias("disabled-extensions")
	@XStreamImplicit(itemFieldName="extension-disabled")
	private TreeSet<String> disabledExtensions;
	
	private void initRuntime() {
		freeMemorySize = Runtime.getRuntime().freeMemory();
		availableMemorySize = Runtime.getRuntime().totalMemory();
		maxMemorySize = Runtime.getRuntime().maxMemory();
		threadPoolSize = Runtime.getRuntime().availableProcessors();
	}
	
	public Preferences() {
		this.initRuntime();

		type = "SERL CriticAL Preferences. (Note: Edit using Eclipse project properties)";
		
		loopUnrollLimit = 2;
		methodDepth = 3;
		pathCutOffSize = 8192; // 10 = 1024, 11 = 2048, 12 = 4096, 13 = 8192, 14 = 16384, 15 = 32768
		delay = 100;
		poolWaitTime = 300; // 300 secs = 5 mins before pool termination is checked
		
		// This will be used to set soot
		this.sootOptions = new TreeSet<Pair>();
		
		// By default we ignore four JDK packages from analysis for efficiency
		// But user can delete this to include them in analysis from preference page
//		this.addSootOption("exclude", "java.");
//		this.addSootOption("exclude", "javax.");
//		this.addSootOption("exclude", "sun.");
//		this.addSootOption("exclude", "com.sun.");
		
		// This is for disabling extensions that adds analyzer to this plugin.
		this.disabledExtensions = new TreeSet<String>();
	}

	public String getType() {
		return type;
	}

	public long getFreeMemorySize() {
		if(this.freeMemorySize <= 0)
			this.initRuntime();
		return freeMemorySize;
	}

	public long getAvailableMemorySize() {
		if(this.availableMemorySize <= 0)
			this.initRuntime();
		return availableMemorySize;
	}

	public long getMaxMemorySize() {
		if(this.maxMemorySize <= 0)
			this.initRuntime();
		return maxMemorySize;
	}

	public int getLoopUnrollLimit() {
		return loopUnrollLimit;
	}

	public void setLoopUnrollLimit(int loopUnrollLimit) {
		this.loopUnrollLimit = loopUnrollLimit;
	}

	public int getMethodDepth() {
		return methodDepth;
	}

	public void setMethodDepth(int methodDepth) {
		this.methodDepth = methodDepth;
	}

	public int getPathCutOffSize() {
		return pathCutOffSize;
	}

	public void setPathCutOffSize(int pathCutOffSize) {
		this.pathCutOffSize = pathCutOffSize;
	}

	public int getThreadPoolSize() {
		if(this.threadPoolSize <= 0)
			this.initRuntime();
		return threadPoolSize;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public long getPoolWaitTime() {
		return poolWaitTime;
	}

	public void setPoolWaitTime(long poolWaitTime) {
		this.poolWaitTime = poolWaitTime;
	}

	public Set<Pair> getSootOptions() {
		if(this.sootOptions == null)
			this.sootOptions = new TreeSet<Pair>();
		return this.sootOptions;
	}

	public boolean addSootOption(String key, String value) {
		if(this.sootOptions == null)
			this.sootOptions = new TreeSet<Pair>();
		return this.sootOptions.add(new Pair(key, value));
	}
	
	public String[] getSootOptionArray() {
		ArrayList<String> list = new ArrayList<String>(this.sootOptions.size());
		if(this.sootOptions == null)
			return list.toArray(new String[0]);
		
		for(Pair option : this.sootOptions) {
			String key = option.getKey().trim();
			String value = option.getValue().trim();
			if(value.length() == 0)
				list.add(key);
			else
				list.add(key + " " + value);
		}
		return list.toArray(new String[0]);
	}
	
	public boolean addDisabledExtension(String id) {
		if(this.disabledExtensions == null)
			this.disabledExtensions = new TreeSet<String>();
		return this.disabledExtensions.add(id);
	}
	
	public boolean removeDisabledExtension(String id) {
		if(this.disabledExtensions == null)
			this.disabledExtensions = new TreeSet<String>();
		return this.disabledExtensions.remove(id);
	}
	
	public boolean containsDisabledExtension(String id) {
		if(this.disabledExtensions == null)
			this.disabledExtensions = new TreeSet<String>();
		return this.disabledExtensions.contains(id);
	}
	
	public Set<String> getDisabledExtensions() {
		if(this.disabledExtensions == null)
			this.disabledExtensions = new TreeSet<String>();
		return this.disabledExtensions;
	}
}
