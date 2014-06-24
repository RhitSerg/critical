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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import soot.SootClass;
import soot.SootMethod;

import edu.clarkson.serl.critic.CriticPlugin;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.loader.SootClassLoader;
import edu.clarkson.serl.critic.reporter.CriticMarkerFactory;
import edu.clarkson.serl.critic.util.EvaluationLog;


/**
 * This is the Checker's entry point where a Job running on a new Non-UI thread created 
 * to carry out analyses.
 *
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class CriticMainJob extends AbstractCriticJob {
	/**
	 * Creates a new instance of the class CriticMainJob
	 * 
	 * @param name
	 */
	public CriticMainJob(String projectName) {
		super("Analyzing " + projectName);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		EvaluationLog.startCritic();
		
		monitor.beginTask("CriticAL analysis in progress ...", 2);
		try {
			final Display display = Display.getDefault(); 
			
			// Load all of the project types in soot
			SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
			SootClassLoader loader = SootClassLoader.instance();
			loader.process(subMonitor);
			
			if(monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			subMonitor.done();
			
			// Open a file writer job for any intermediate file writing
			FileWriterJob.start();
			
			// Hook for adding custom analyzer into project's main analyzer
			subMonitor = new SubProgressMonitor(monitor, 1);

			// Remove all existing markers
			CriticMarkerFactory.removeAllMarkers(CriticPlugin.getSrcFolder());

			// Run interpreter
			Interpreter.instance().execute(subMonitor);
			
			subMonitor.done();

			// Stop log for evaluation
			EvaluationLog.endCritic();
			FileWriterJob.write(CriticPlugin.PLUGIN_ID, "evaluation.txt", EvaluationLog.getString());
			FileWriterJob.write(CriticPlugin.PLUGIN_ID, "evaluation.csv", EvaluationLog.getCSVString());
			
			// Stop file writing job
			FileWriterJob.stopAndJoin();
			
			display.syncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(display.getActiveShell(), "CriticAL Framework", "Analysis completed successfully!");
				}
			});
			monitor.done();
			CriticPlugin.reset();
		}
		catch(Throwable t) {
			CriticPlugin.stopAnalysis();
			EvaluationLog.endCritic();
			
			try {
				Interpreter interpreter = Interpreter.instance();
				SootMethod method = interpreter.getCurrentMethod();
				SootClass clazz = interpreter.getCurrentClass();
				int lineNumber = interpreter.getLineNumber();

				String message = "CriticAL framework encountered a problem while analyzing: \n" +
						"\t[Class: " + clazz + "]\n" +
						"\t[Method: " + method + "]\n" +
						"\t[Line Number: " + lineNumber + "]";
				Exception e = new Exception(message, t);
				e.printStackTrace();
				CriticPlugin.reset();

				if(t instanceof Error)
					throw (Error)t;
				
				return new Status(Status.ERROR, CriticPlugin.PLUGIN_ID, Status.CANCEL, message, e);
			}
			catch(Throwable newT) {
				t.printStackTrace();
			}
			
			if(t instanceof Error)
				throw (Error)t;
			
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}
}
