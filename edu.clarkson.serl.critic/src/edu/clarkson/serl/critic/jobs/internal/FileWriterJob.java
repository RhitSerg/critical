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

import java.io.ByteArrayInputStream;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.IProgressConstants;

import edu.clarkson.serl.critic.CriticPlugin;



/**
 * Use this to write data to the output folder using following:
 * 
 * {@link #write(int, String, String)}
 *
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class FileWriterJob extends CriticWorkspaceJob {
	public static final int STOP = 0;
	public static final int DATA = 2;
	
	private static FileWriterJob instance;
	
	/**
	 * Resets the file writer job by stopping and joining the main thread.
	 */
	public static synchronized void reset() {
		if(instance != null) {
			stopAndJoin();
			instance = null;
		}
	}
	
	/**
	 * Starts the file writer job.
	 */
	public static synchronized void start() {
		if(instance == null) {
			instance = new FileWriterJob("Output File Writting Job");
			instance.setProperty(IProgressConstants.ICON_PROPERTY, CriticPlugin.getImageDescriptor("icons/big_bug.png"));
			instance.setPriority(Job.SHORT);
			instance.schedule();
		}
	}
	
	/**
	 * Stops the file writer job and the control waits until the task is finished.
	 */
	public static synchronized void stopAndJoin() {
		write(STOP, null, null, null);
		if(instance != null) {
			try {
				instance.join();
			}
			catch(Exception e) {
				CriticPlugin.log(Status.WARNING, "File Writter Thread Interrupted", e);
			}
		}
	}
	
	/**
	 * Stops the file writer job by sending stop signal but does not wait for it to complete.
	 */
	public static synchronized void stop() {
		write(STOP, null, null, null);
	}
	
	/**
	 * Writes data to the corresponding folder allocated for each extension plugin.
	 * @param id The id of the extension plugin.
	 * @param fileName The name of the file that should be created to write the data.
	 * @param data The data to be written.
	 * @return <tt>true</tt> if the data was successfully scheduled for writing and <tt>false</tt> otherwise.
	 */
	public static synchronized boolean write(String id, String fileName, String data) {
		return write(DATA, id, fileName, data);
	}
	
	private static synchronized boolean write(int type, String id, String fileName, String data) {
		if(instance == null)
			return false;
		
		instance.push(type, id, fileName, data);
		return true;
	}
	
	public static class Entry {
		public int type;
		public String id;
		public String fileName;
		public String data;
		
		public Entry(int type, String id, String fileName, String data) {
			this.id = id;
			this.type = type;
			this.fileName = fileName;
			this.data = data;
		}
		
		public String getId() {
			return id;
		}

		public int getType() {
			return type;
		}

		public String getFileName() {
			return fileName;
		}

		public String getData() {
			return data;
		}
		
		@Override
		public String toString() {
			return "[" + this.type + ", " + this.fileName + "]"; 
		}
	}
	
	private LinkedBlockingQueue<Entry> queue;
	public FileWriterJob(String name) {
		super(name);
		this.queue = new LinkedBlockingQueue<Entry>();
	}

	public void push(int type, String id, String fileName, String data) {
		Entry anEntry = new Entry(type, id, fileName, data);
		try {
			this.queue.put(anEntry);
		}
		catch(Exception e) {
			CriticPlugin.log(Status.ERROR, e.getMessage(), e);
		}
	}
	
	
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Writting output file to anlaysis folder ...", IProgressMonitor.UNKNOWN);
		while(true) {
			if(monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			
			try {
				Entry anEntry = this.queue.take();
				int type = anEntry.getType();
				if(type == STOP) {
					break;
				}
				else if(type == DATA) {
					String id = anEntry.getId();
					String fileName = anEntry.getFileName();
					monitor.setTaskName("Writing " + fileName);
					try {
						IFolder idFolder = CriticPlugin.getExtensionFolder(id);
						if(idFolder != null) {
							IFile outFile = idFolder.getFile(fileName);
							int i = 1;
							while(outFile.exists()) {
								outFile = idFolder.getFile(i + fileName);
								++i;
							}
							String data = anEntry.getData();
							ByteArrayInputStream stream = new ByteArrayInputStream(data.getBytes());
							outFile.create(stream, true, null);

							monitor.setTaskName("Done Writing " + fileName);
						}
					}
					catch(Exception e) {
						CriticPlugin.log(Status.ERROR,"[" + id + "] Cannot write file: " + fileName, e);
					}
				}
			}
			catch(Exception e) {
				CriticPlugin.log(Status.ERROR, e.getMessage(), e);
			}

			monitor.worked(1);
		}
		monitor.done();
		return Status.OK_STATUS;
	}
	
	@Override
	protected void canceling() {
		super.canceling();
		FileWriterJob.stop();
	}
}
