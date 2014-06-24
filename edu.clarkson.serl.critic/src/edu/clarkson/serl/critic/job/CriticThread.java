/*
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
package edu.clarkson.serl.critic.job;

import org.eclipse.core.runtime.Status;

import edu.clarkson.serl.critic.CriticPlugin;




/**
 * Use this to add threading support and instead of implementing
 * {@link #run()} implement {@link #execute()}. Errors are automatically
 * logged in the CriticAL plugin log. 
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public abstract class CriticThread implements Runnable {
	private String task;

	/**
	 * Creates a new instance of the class CriticThread
	 * 
	 */
	public CriticThread(String task) {
		this.task = task;
	}
	
	public void setTask(String task) {
		this.task = task;
	}

	/**
	 * 
	 */
	public void run() {
		try {
			execute();
		} catch(Throwable t) {
			CriticPlugin.stopAnalysis();
			
			String msg = "An error occured while performing [" + task + "]";
			Exception e = new Exception(msg, t);
			CriticPlugin.log(Status.ERROR, msg, e);
			e.printStackTrace();

			throw new Error(t);
		}
	}
	
	public abstract void execute() throws Throwable;
}
