/*
 * JFrameAbstraction.java
 * Mar 13, 2011
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
 
package edu.clarkson.serl.critic.swing.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import soot.Value;
import soot.jimple.InvokeExpr;

import edu.clarkson.serl.critic.CriticPlugin;
import edu.clarkson.serl.critic.adt.ClosedList;
import edu.clarkson.serl.critic.adt.ClosedString;
import edu.clarkson.serl.critic.extension.Critic;
import edu.clarkson.serl.critic.extension.ICritic;
import edu.clarkson.serl.critic.extension.IResult;
import edu.clarkson.serl.critic.extension.Result;
import edu.clarkson.serl.critic.extension.SymbolicApi;
import edu.clarkson.serl.critic.factory.DefaultSymbolFactory;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.SymbolicKey;
import edu.clarkson.serl.critic.interpreter.model.ConstInteger;
import edu.clarkson.serl.critic.swing.SwingCriticPlugin;
import edu.clarkson.serl.critic.swing.poi.ICheckable;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class JFrameAbstraction extends SymbolicApi<Value> implements ICheckable<Value> {
	public static final String TYPE = "javax.swing.JFrame";
	public static final Value DEFAULT_VALUE = AbstractValue.fromObject(TYPE);
	
	public static final SymbolicKey TITLE = SymbolicKey.fromObject("title");
	public static final SymbolicKey CONTENT_PANE = SymbolicKey.fromObject("contentPane");
	public static final SymbolicKey DEFAULT_CLOSE_OPERATION = SymbolicKey.fromObject("defaultCloseOperation");
	public static final SymbolicKey LAID_OUT = SymbolicKey.fromObject("laidout");
	public static final SymbolicKey VISIBLE = SymbolicKey.fromObject("visible");
	public static final SymbolicKey MENU_BAR = SymbolicKey.fromObject("menu");
	
	public JFrameAbstraction() {
		super(DEFAULT_VALUE, false, true);
	}

	public JFrameAbstraction(Value sootValue) {
		super(sootValue, false, true);
	}

	public JFrameAbstraction(Value sootValue, boolean open, boolean mutable) {
		super(sootValue, open, mutable);
	}
	
	@Override
	public IResult execute(InvokeExpr invokeExpr, List<ISymbol<? extends Value>> arguments) {
		String method = invokeExpr.getMethod().getName();
		if(method.equals("<init>"))
			return this.init(arguments);
		if(method.equals("getContentPane"))
			return this.getContentPane();
		if(method.equals("setContentPane"))
			return this.setContentPane(arguments);
		if(method.equals("repaint"))
			return this.repaint();
		if(method.equals("pack"))
			return this.pack();
		if(method.equals("setVisible"))
			return this.setVisible(arguments);
		if(method.equals("setSize"))
			return this.setSize(arguments);
		if(method.equals("setPreferredSize"))
			return this.setPreferredSize(arguments);
		if(method.equals("getPreferredSize"))
			return this.getPreferredSize();
		if(method.equals("getSize"))
			return this.getSize();
		if(method.equals("add"))
			return this.add(arguments);
		if(method.equals("setLayout"))
			return this.setLayout(arguments);
		if(method.equals("setJMenuBar"))
			return this.setJMenuBar(arguments);
		if(method.equals("getJMenuBar"))
			return this.getJMenuBar();
		if(method.equals("setDefaultCloseOperation"))
			return this.setDefaultCloseOperation(arguments);
		if(method.equals("getDefaultCloseOperation"))
			return this.getDefaultCloseOperation();
		if(method.equals("show"))
			return this.show();
		
		// We do not handle other methods
		return new Result(DefaultSymbolFactory.getOpenMutableSymbol(invokeExpr));
	}
	
	public IResult setDefaultCloseOperation(List<ISymbol<? extends Value>> arguments) {
		ISymbol<? extends Value> op = arguments.get(0);
		this.put(JFrameAbstraction.DEFAULT_CLOSE_OPERATION, op);
		return new Result(Interpreter.VOID);
	}
	
	public IResult getDefaultCloseOperation() {
		return new Result(this.get(DEFAULT_CLOSE_OPERATION));
	}
	
	
	public IResult setJMenuBar(List<ISymbol<? extends Value>> arguments) {
		ISymbol<? extends Value> menu = arguments.get(0);
		this.put(MENU_BAR, menu);
		if(menu.getValue() != null) 
			menu.put(JComponentAbstraction.PARENT, this);
		return new Result(Interpreter.VOID);
	}
	
	public IResult getJMenuBar() {
		ISymbol<? extends Value> menu = this.get(JFrameAbstraction.MENU_BAR);
		if(menu == null)
			menu = Interpreter.NULL;
		return new Result(menu);
	}
	
	public IResult setLayout(List<ISymbol<? extends Value>> arguments) {
		ISymbol<? extends Value> contentPane = this.get(CONTENT_PANE);
		if(contentPane instanceof JPanelAbstraction) {
			return ((JPanelAbstraction)contentPane).setLayout(arguments);
		}
		return new Result(Interpreter.NULL);
	}
	
	public IResult show() {
		ArrayList<ISymbol<? extends Value>> args = new ArrayList<ISymbol<? extends Value>>();
		args.add(Interpreter.TRUE);
		return this.setVisible(args);
	}
	
	public IResult setVisible(List<ISymbol<? extends Value>> arguments) {
		IResult result = new Result(Interpreter.VOID); 
		if(arguments.size() != 1)
			return result;
		ISymbol<? extends Value> visibility = arguments.get(0);
		this.put(VISIBLE, visibility);
		for(ISymbol<? extends Value> child : this.getAllChildren()) {
			child.put(VISIBLE, visibility);
		}
		
		if(visibility == null || visibility.equals(Interpreter.FALSE))
			return result;
		
		JComponentAbstraction contentPane = (JComponentAbstraction)this.get(CONTENT_PANE);
		if(contentPane != null) {
			ISymbol<? extends Value> laidOut = this.get(LAID_OUT);
			if(laidOut == null || laidOut.equals(Interpreter.FALSE))
				this.pack();
		}
		return contentPane.repaint();
	}
	
	public IResult repaint() {
		IResult result = new Result(Interpreter.VOID);
		ISymbol<? extends Value> contentPane = this.get(CONTENT_PANE);
		if(contentPane instanceof JComponentAbstraction) {
			return ((JComponentAbstraction)contentPane).repaint();
		}
		return result;
	}
	
	public IResult pack() {
		IResult result = new Result(Interpreter.VOID);
		ISymbol<? extends Value> contentPane = this.get(CONTENT_PANE);
		this.put(LAID_OUT, Interpreter.TRUE);
		
		if(!(contentPane instanceof JComponentAbstraction))
			return result;
		
		DimensionAbstraction size = (DimensionAbstraction)this.getSize().getValue();
		if(size != null && size.isUserDefined()) {
			// Size will get overwritten by pack
			ICritic critic = new Critic(
					TYPE + "-SetSize-Pack-Inconsistency",
					ICritic.Type.Criticism,
					ICritic.Priority.High,
					"Set Size and Pack Inconsistency",
					"Use of JFrame.pack() after JFrame.setSize() will overwrite the effect of size. " +
					"Intead use JFrame.setPreferredSize() when followed by pack()."
					);
			critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/components/frame.html");
			result.add(critic);
		}
		
		JComponentAbstraction panel = (JComponentAbstraction)contentPane;
		result.add(panel.revalidate());
		
		JComponentAbstraction menu = (JComponentAbstraction)this.get(MENU_BAR);
		if(menu != null) {
			result.add(menu.revalidate());
		}
		
		// JFrame should give size and location to content pane
		panel.put(JComponentAbstraction.SIZE, DimensionAbstraction.getDefaultSize());
		panel.put(JComponentAbstraction.LOCATION, PointAbstraction.getDefaultLocation());
		
		return result;
	}
	
	public IResult getSize() {
		ISymbol<? extends Value> size = this.get(JComponentAbstraction.SIZE);
		if(size == null || size.getValue() == null) {
			size = new DimensionAbstraction();
			this.put(JComponentAbstraction.SIZE, size);
		}
		return new Result(size);
	}
	
	public IResult setSize(List<ISymbol<? extends Value>> arguments) {
		if(arguments.size() == 1) {
			// setLocation(new Dimension(width,height))
			this.put(JComponentAbstraction.SIZE, arguments.get(0));
		}
		else {
			// setLocation(width,height)
			DimensionAbstraction dimension = new DimensionAbstraction();
			dimension.init(arguments);
			this.put(JComponentAbstraction.SIZE, dimension);
		}
		
		Result result = new Result(Interpreter.VOID);
		ISymbol<? extends Value> laidOut = this.get(LAID_OUT);
		if(Interpreter.TRUE.equals(laidOut)) {
			// Call to setSize() following pack()
			ICritic critic = new Critic(
					TYPE + "-Pack-SetSize-Inconsistency",
					ICritic.Type.Criticism,
					ICritic.Priority.High,
					"Pack and Set Size Inconsistency",
					"Use of JFrame.setSize() after JFrame.pack() will overwrite the effect of the pack() method. " +
					"Consider using only one of them."
					);
			critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/components/frame.html");
			result.add(critic);
		}
		return result;
	}

	public IResult getPreferredSize() {
		ISymbol<? extends Value> size = this.get(JComponentAbstraction.PREFERRED_SIZE);
		if(size == null || size.getValue() == null) {
			size = new DimensionAbstraction();
			this.put(JComponentAbstraction.PREFERRED_SIZE, size);
		}
		return new Result(size);
	}
	
	public IResult setPreferredSize(List<ISymbol<? extends Value>> arguments) {
		if(arguments.size() == 1) {
			// setLocation(new Dimension(width,height))
			this.put(JComponentAbstraction.PREFERRED_SIZE, arguments.get(0));
		}
		else {
			// setLocation(width,height)
			DimensionAbstraction dimension = new DimensionAbstraction();
			dimension.init(arguments);
			this.put(JComponentAbstraction.PREFERRED_SIZE, dimension);
		}
		return new Result(Interpreter.VOID);
	}
	
	public IResult getContentPane() {
		ISymbol<? extends Value> contentPane = this.get(CONTENT_PANE);
		return new Result(contentPane);
	}
	
	public IResult add(List<ISymbol<? extends Value>> arguments) {
		ISymbol<? extends Value> contentPane = this.get(CONTENT_PANE);
		if(contentPane instanceof JPanelAbstraction) {
			return ((JPanelAbstraction)contentPane).add(arguments);
		}
		return new Result(Interpreter.NULL);
	}
	
	public IResult setContentPane(List<ISymbol<? extends Value>> arguments) {
		this.put(CONTENT_PANE, arguments.get(0));
		arguments.get(0).put(JComponentAbstraction.PARENT, this);
		return new Result(Interpreter.VOID);
	}
	
	
	public IResult init(List<ISymbol<? extends Value>> arguments) {
		if(arguments.isEmpty()) {
			// Empty title
			this.put(JFrameAbstraction.TITLE, ClosedString.EMPTY);
		}
		else {
			ClosedString title = null;
			for(ISymbol<? extends Value> o : arguments) {
				if(o instanceof ClosedString)
					title = (ClosedString)o; 
			}
			if(title == null)
				this.put(JFrameAbstraction.TITLE, ClosedString.EMPTY);
			else
				this.put(JFrameAbstraction.TITLE, title);
		}
		
		// Initialize and set JPanel as default content pane
		JPanelAbstraction panel = new JPanelAbstraction();

		// Set BorderLayout as default layout manager
		List<ISymbol<? extends Value>> args = new ArrayList<ISymbol<? extends Value>>(1);
		BorderLayoutAbstraction borderLayout = new BorderLayoutAbstraction();
		borderLayout.setDefault(true);
		args.add(borderLayout);
		panel.init(args);

		panel.put(JComponentAbstraction.PARENT, this);
		this.put(JFrameAbstraction.CONTENT_PANE, panel);
		
		// Set default close operation
		this.put(JFrameAbstraction.DEFAULT_CLOSE_OPERATION, ConstInteger.fromInteger(JFrame.HIDE_ON_CLOSE));
		
		return new Result(Interpreter.VOID);
	}
	
	public Set<ISymbol<? extends Value>> getAllChildren() {
		ISymbol<? extends Value> object = this.get(CONTENT_PANE);
		ISymbol<? extends Value> menu = this.get(MENU_BAR);
		if(object instanceof JComponentAbstraction) {
			Set<ISymbol<? extends Value>> children = ((JComponentAbstraction)object).getAllChildren();
			Set<ISymbol<? extends Value>> set = new HashSet<ISymbol<? extends Value>>(children.size() + 2);
			set.add(object);
			set.addAll(children);
			if(menu != null && menu.getValue() != null)
				set.add(menu);
			return set;
		}
		
		Set<ISymbol<? extends Value>> set = new HashSet<ISymbol<? extends Value>>(2);
		if(object != null && object.getValue() != null)
			set.add(object);
		if(menu != null && menu.getValue() != null)
			set.add(menu);
		
		return set;
	}

	@Override
	public IResult checkAtEnd() {
		Result result = new Result(Interpreter.VOID);
		// Check if setVisible has been called
		ISymbol<? extends Value> visible = this.get(JFrameAbstraction.VISIBLE);
		if(visible == null || visible.getValue().equals(Boolean.FALSE)) {
			ICritic critic = new Critic(
					JFrameAbstraction.TYPE + "-Visibility",
					this.getOutermostClass(),
					this.getLineNumber(),
					ICritic.Type.Criticism,
					ICritic.Priority.High,
					"JFrame Invisible",
					"The JFrame you have created at " + this.getLocationString() + " has not been properly " +
					"configured for display. A JFrame can be displayed by calling the setVisible(true) " +
					"method on the frame object. Please click on this menu to see details."
					);
			String url = CriticPlugin.getDocumentURL(SwingCriticPlugin.PLUGIN_ID, "RE-Using-JFrame.html");
			critic.setAttribute(ICritic.URL, url);
			critic.setAttribute("-CLASS-", critic.getOutermostClass());
			critic.setAttribute("-LINE-", critic.getLineNumber());			
			result.add(critic);
		}

		JPanelAbstraction contentPane = (JPanelAbstraction)this.get(CONTENT_PANE);
		if(contentPane != null) {
			ISymbol<? extends Value> layout = contentPane.get(JComponentAbstraction.LAYOUT);
			if(layout instanceof BorderLayoutAbstraction && ((BorderLayoutAbstraction) layout).isDefault()) {
				ICritic critic = new Critic(
						JFrameAbstraction.TYPE + "-BorderLayout-Explanation",
						this.getOutermostClass(),
						this.getLineNumber(),
						ICritic.Type.Explanation,
						ICritic.Priority.High,
						"Content Pane has a BorderLayout",
						"Did you know that the content pane of the JFrame you have created at "
						+ this.getLocationString() + " has a BorderLayout as its layout manager? " +
						"A layout manager provides a systematic way to arrange your components on your window. " +
						"Please click on this menu to see details about how to use layout managers."
						);
				critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/using.html");
				result.add(critic);
			}
		}
		
		if(contentPane == null || contentPane.getAllChildren().isEmpty()) {
			ICritic critic = new Critic(
					JFrameAbstraction.TYPE + "-Empty-Content",
					this.getOutermostClass(),
					this.getLineNumber(),
					ICritic.Type.Recommendation,
					ICritic.Priority.High,
					"Empty Content Pane in JFrame",
					"The JFrame created at " + this.getLocationString() + " does not contain any user-defined " +
					"components. Please click on this menu to learn about adding new components " +
					"to your JFrame."
					);
			String url = CriticPlugin.getDocumentURL(SwingCriticPlugin.PLUGIN_ID, "RE-Using-JFrame.html");
			critic.setAttribute(ICritic.URL, url);
			critic.setAttribute("-CLASS-", critic.getOutermostClass());
			critic.setAttribute("-LINE-", critic.getLineNumber());			
			result.add(critic);
		}
				
		
		// Recommendation on default close operation
		ISymbol<? extends Value> closeOp = this.get(JFrameAbstraction.DEFAULT_CLOSE_OPERATION);
		int value = (Integer)closeOp.getValue();
		ICritic critic = new Critic(
				JFrameAbstraction.TYPE + "-Default-Close-Operation",
				this.getOutermostClass(),
				this.getLineNumber(),
				ICritic.Type.Recommendation,
				ICritic.Priority.High,
				"JFrame Close Operation",
				"You are using the " + toCloseOperationString(value) + " property for the default closing behavior of the JFrame. " +
				"There are multiple options available for closing a frame. Please click to see the details."
				);
		critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/components/frame.html#windowevents");
		result.add(critic);
		return result;
	}
	
	public static String toCloseOperationString(int value) {
		if(value == JFrame.EXIT_ON_CLOSE)
			return "EXIT_ON_CLOSE";
		if(value == JFrame.HIDE_ON_CLOSE)
			return "HIDE_ON_CLOSE";
		if(value == JFrame.DO_NOTHING_ON_CLOSE)
			return "DO_NOTHING_ON_CLOSE";
		if(value == JFrame.DISPOSE_ON_CLOSE)
			return "DISPOSE_ON_CLOSE";
		return "UNKNOWN_VALUE";
	}
}
