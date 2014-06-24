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
package edu.clarkson.serl.critic.jobs.internal;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.progress.IProgressConstants;

import edu.clarkson.serl.critic.CriticPlugin;



/**
 * Use this to run a {@link WorkspaceJob} as a part of PSF framework.
 *
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public abstract class CriticWorkspaceJob extends WorkspaceJob {
	/**
	 * Creates a new instance of the class CriticWorkspaceJob
	 * 
	 * @param name
	 */
	public CriticWorkspaceJob(String name) {
		super(name);
		this.setProperty(IProgressConstants.ICON_PROPERTY, CriticPlugin.getImageDescriptor("icons/big_bug.png"));
	}

	@Override
	public abstract IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException;

	@Override
	public boolean belongsTo(Object family) {
		return CriticPlugin.JOB_ID.equals(family);
	}
	
	@Override
	protected void canceling() {
		super.canceling();
		CriticPlugin.stopAnalysis();
	}
}
