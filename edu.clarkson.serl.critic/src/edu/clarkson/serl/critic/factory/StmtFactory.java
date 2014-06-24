/*
 * StmtFactory.java
 * Jul 11, 2011
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
 
package edu.clarkson.serl.critic.factory;


import org.eclipse.core.runtime.Status;

import edu.clarkson.serl.critic.CriticPlugin;
import edu.clarkson.serl.critic.interpreter.model.StmtAbstract;
import edu.clarkson.serl.critic.interpreter.model.StmtAssign;
import edu.clarkson.serl.critic.interpreter.model.StmtBreakpoint;
import edu.clarkson.serl.critic.interpreter.model.StmtEnterMonitor;
import edu.clarkson.serl.critic.interpreter.model.StmtExitMonitor;
import edu.clarkson.serl.critic.interpreter.model.StmtGoto;
import edu.clarkson.serl.critic.interpreter.model.StmtIdentity;
import edu.clarkson.serl.critic.interpreter.model.StmtIf;
import edu.clarkson.serl.critic.interpreter.model.StmtInvoke;
import edu.clarkson.serl.critic.interpreter.model.StmtLookupSwitch;
import edu.clarkson.serl.critic.interpreter.model.StmtNop;
import edu.clarkson.serl.critic.interpreter.model.StmtRet;
import edu.clarkson.serl.critic.interpreter.model.StmtReturn;
import edu.clarkson.serl.critic.interpreter.model.StmtReturnVoid;
import edu.clarkson.serl.critic.interpreter.model.StmtTableSwitch;
import edu.clarkson.serl.critic.interpreter.model.StmtThrow;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NopStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.StmtSwitch;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class StmtFactory implements StmtSwitch {
	private StmtAbstract<? extends Stmt> symStmt;
	
	public StmtAbstract<? extends Stmt> newSymbol(Stmt s) {
		s.apply(this);
		StmtAbstract<? extends Stmt> stmt = this.symStmt;
		this.symStmt = null;
		return stmt;
	}
	
	public void caseBreakpointStmt(BreakpointStmt stmt) {
		this.symStmt = new StmtBreakpoint(stmt);
	}

	public void caseInvokeStmt(InvokeStmt stmt) {
		this.symStmt = new StmtInvoke(stmt);
	}

	public void caseAssignStmt(AssignStmt stmt) {
		this.symStmt = new StmtAssign(stmt);
	}

	public void caseIdentityStmt(IdentityStmt stmt) {
		this.symStmt = new StmtIdentity(stmt);
	}

	public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
		this.symStmt = new StmtEnterMonitor(stmt);
	}

	public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
		this.symStmt = new StmtExitMonitor(stmt);
	}

	public void caseGotoStmt(GotoStmt stmt) {
		this.symStmt = new StmtGoto(stmt);
	}

	public void caseIfStmt(IfStmt stmt) {
		this.symStmt = new StmtIf(stmt);
	}

	public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
		this.symStmt = new StmtLookupSwitch(stmt);
	}

	public void caseNopStmt(NopStmt stmt) {
		this.symStmt = new StmtNop(stmt);
	}

	public void caseRetStmt(RetStmt stmt) {
		this.symStmt = new StmtRet(stmt);
	}

	public void caseReturnStmt(ReturnStmt stmt) {
		this.symStmt = new StmtReturn(stmt);
	}

	public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
		this.symStmt = new StmtReturnVoid(stmt);
	}

	public void caseTableSwitchStmt(TableSwitchStmt stmt) {
		this.symStmt = new StmtTableSwitch(stmt);
	}

	public void caseThrowStmt(ThrowStmt stmt) {
		this.symStmt = new StmtThrow(stmt);
	}

	public void defaultCase(Object obj) {
		CriticPlugin.log(Status.WARNING, "Unexpected Jimple statement encoutered.", new UnsupportedOperationException());
	}
}
