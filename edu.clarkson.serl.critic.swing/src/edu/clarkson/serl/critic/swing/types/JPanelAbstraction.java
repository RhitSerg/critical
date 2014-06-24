/*
 * JPanelAbstraction.java
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

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import edu.clarkson.serl.critic.CriticPlugin;
import edu.clarkson.serl.critic.adt.ClosedList;
import edu.clarkson.serl.critic.adt.ClosedString;
import edu.clarkson.serl.critic.extension.Critic;
import edu.clarkson.serl.critic.extension.ICritic;
import edu.clarkson.serl.critic.extension.IResult;
import edu.clarkson.serl.critic.extension.Result;
import edu.clarkson.serl.critic.interpreter.AbstractValue;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.interpreter.Interpreter;
import edu.clarkson.serl.critic.interpreter.SymbolicKey;
import edu.clarkson.serl.critic.interpreter.model.ConstNull;
import edu.clarkson.serl.critic.swing.SwingCriticPlugin;
import soot.BooleanType;
import soot.IntType;
import soot.PrimType;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;

/**
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 *
 */
public class JPanelAbstraction extends JComponentAbstraction {
	public static final String TYPE = "javax.swing.JPanel";
	public static final Value DEFAULT_VALUE = AbstractValue.fromObject(TYPE);
	public static final SymbolicKey DOUBLE_BUFFERED = SymbolicKey.fromObject("double-buffered");

	public JPanelAbstraction() {
		super(DEFAULT_VALUE);
	}

	public JPanelAbstraction(Value sootValue) {
		super(sootValue);
	}

	public JPanelAbstraction(Value sootValue, boolean open, boolean mutable) {
		super(sootValue, open, mutable);
	}

	@Override
	public IResult execute(InvokeExpr invokeExpr, List<ISymbol<? extends Value>> arguments) {
		String method = invokeExpr.getMethod().getName();
		// Call super constructors first
		if(method.equals("<init>")) {
			return this.init(arguments);
		}

		// Let JComponentAbstraction handle all other operations
		return super.execute(invokeExpr, arguments);
	}

	public IResult init(List<ISymbol<? extends Value>> arguments) {
		IResult result = super.init(arguments);

		if(arguments.isEmpty()) {
			FlowLayoutAbstraction layout = new FlowLayoutAbstraction(AbstractValue.fromObject("java.awt.FlowLayout"));
			layout.setDefault(true);
			this.put(LAYOUT, layout);
		}
		else {
			if(arguments.size() == 1) {
				ISymbol<? extends Value> arg = arguments.get(0);
				if(arg.getType() instanceof IntType) {
					this.put(DOUBLE_BUFFERED, arg);
					FlowLayoutAbstraction layout = new FlowLayoutAbstraction(AbstractValue.fromObject("java.awt.FlowLayout"));
					layout.setDefault(true);
					this.put(LAYOUT, layout);
				}
				else { // arg is the layout manager
					if(arg instanceof ConstNull) { // null layout
						// It does not have a preferred size
						this.put(JComponentAbstraction.PREFERRED_SIZE, new DimensionAbstraction());
					}
					this.put(DOUBLE_BUFFERED, Interpreter.TRUE);

					result.add(this.setLayout(arguments));
				}
			}
			else { // must be 2
				ISymbol<? extends Value> arg = arguments.get(0);
				if(arg instanceof ConstNull) { // null layout
					// It does not have a preferred size
					this.put(JComponentAbstraction.PREFERRED_SIZE, new DimensionAbstraction());
				}
				// Set the double buffered property
				this.put(DOUBLE_BUFFERED, arguments.get(1));
				
				ArrayList<ISymbol<? extends Value>> layoutArgs = new ArrayList<ISymbol<? extends Value>>();
				layoutArgs.add(arg);
				result.add(this.setLayout(layoutArgs));
			}
		}
		return result;
	}

	@Override
	public IResult getPreferredSize() {
		IResult result = super.getPreferredSize();
		DimensionAbstraction prefSize = (DimensionAbstraction)result.getValue();
		if(!prefSize.isInitialized()) {
			ISymbol<? extends Value> layout = this.get(LAYOUT);
			if(layout != null && layout.getValue() != null) {
				prefSize = DimensionAbstraction.getDefaultSize();
				this.put(PREFERRED_SIZE, prefSize); // Set up proper preferred size
				return new Result(prefSize);
			}
		}
		return result;
	}

	@Override
	public IResult checkAtEnd() {
		// Get the critics from JComponent
		IResult result = super.checkAtEnd();

		// Add more critics from JPanel
		ISymbol<? extends Value> layout = this.get(JComponentAbstraction.LAYOUT);
		if(layout == null || layout.getValue() == null) {
			String message = "The JPanel created in " + this.getLocationString() + " uses a null layout. \n" +
					"When this is done, the widgets inside it will not resize themselves automaticaly.\n" +
					"If resizing is required then you should use a layout manager.";

			ICritic critic = new Critic(
					JComponentAbstraction.TYPE + "-Null-Layout",
					this.getOutermostClass(),
					this.getLineNumber(),
					ICritic.Type.Explanation,
					ICritic.Priority.High,
					"JPanel Uses Null Layout",
					message
					);
			String url = CriticPlugin.getDocumentURL(SwingCriticPlugin.PLUGIN_ID, "RE-Null-Layout.html");
			critic.setAttribute(ICritic.URL, url);
			critic.setAttribute("-CLASS-", this.getOutermostClass());
			critic.setAttribute("-LINE-", this.getLineNumber());
			result.add(critic);
		}
		else if(layout instanceof LayoutManagerAbstraction) {
			LayoutManagerAbstraction l = (LayoutManagerAbstraction)layout;
			if(!l.isDefault() && this.getAllChildren().isEmpty()) {
				ICritic critic = new Critic(
						JComponentAbstraction.TYPE + "-Layout-Set:Empty-Components",
						this.getOutermostClass(),
						this.getLineNumber(),
						ICritic.Type.Criticism,
						ICritic.Priority.Medium,
						"Layout Set But Container Empty",
						"You have manually set a layout manager to the container " + this.getLocationString() + 
						" \nbut did not add any widget to it. If you are not adding any widget to the \n" +
						"container then you do not need to manually set the layout. \n"
						);
				result.add(critic);
			}
		}
		
		checkImproperTables(this, result);
		
		return result;
	}

	private static void checkImproperTables(JComponentAbstraction comp, IResult result) {
		// Check if the component contains more than one child
		@SuppressWarnings("unchecked")
		ClosedList<Value> children = (ClosedList<Value>)comp.get(JComponentAbstraction.COMPONENTS);
		if(children == null || children.size() < 2)
			return;

		// The layout of the component must be FlowLayout or BoxLayout
		ISymbol<? extends Value> layout = comp.get(JComponentAbstraction.LAYOUT);
		if(!(layout instanceof FlowLayoutAbstraction) && !(layout instanceof BoxLayoutAbstraction))
			return;
		
		// Get hold of parent
		ISymbol<? extends Value> parent = comp.getParent().getValue();
		if(!(parent instanceof JComponentAbstraction))
			return;

		// Get hold of siblings
		@SuppressWarnings("unchecked")
		ClosedList<Value> siblings = (ClosedList<Value>)parent.get(JComponentAbstraction.COMPONENTS);
		if(siblings == null || siblings.size() < 2)
			return;
		
		// Get the layout of parent container
		layout = parent.get(JComponentAbstraction.LAYOUT);
		if(layout instanceof FlowLayoutAbstraction || layout instanceof BoxLayoutAbstraction)  {
			checkTableGeneral(comp, children, siblings, result);
		}
		else if(layout instanceof BorderLayoutAbstraction) {
			checkTableBorder((BorderLayoutAbstraction)layout, result);
		}
		
		// Else don't care
	}
	
	
	private static void checkTableGeneral(JComponentAbstraction comp, ClosedList<Value> children, ClosedList<Value> siblings, IResult result) {
		// Now get the adjacent component from parent container
		int index = siblings.indexOf(comp) + 1;
		if(index < siblings.size()) {
			ISymbol<? extends Value> adjComp = siblings.get(index);
			@SuppressWarnings("unchecked")
			ClosedList<Value> adjChildren = (ClosedList<Value>)adjComp.get(JComponentAbstraction.COMPONENTS);

			// If they have same number of components, we complain
			if(isTablePair(children, adjChildren)) {
				ICritic critic = new Critic(
					JComponentAbstraction.TYPE + "-Local-Container-JLabel",
					ICritic.Type.Criticism,
					ICritic.Priority.High,
					"Use of Mulitple Container for a Table Design",
					"It seems you are using two containers for creating a table-like design.\n" +
					"The first container is created due to line number " + comp.getLocationString() + ".\n" +
					"The second container is created due to line number " + adjComp.getLocationString() +".\n" +
					"Instead of using multiple containers for creating one table, you may use \n" +
					"layout managers such as GridLayout, SpringLayout, or GridbagLayout to achieve \n" +
					"a better result.\n"
				);
				critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html#gridbag");
				result.add(critic);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void checkTableBorder(BorderLayoutAbstraction borderLayout, IResult result) {
		List<JComponentAbstraction[]> list = getAllPairs(borderLayout);
		for(JComponentAbstraction[] array : list) {
			ClosedList<Value> firstChildren = (ClosedList<Value>)array[0].get(JComponentAbstraction.COMPONENTS);
			ClosedList<Value> secondChildren = (ClosedList<Value>)array[1].get(JComponentAbstraction.COMPONENTS);
			if(isTablePair(firstChildren, secondChildren)) {
				ICritic critic = new Critic(
						JComponentAbstraction.TYPE + "-Local-Container-Table",
						ICritic.Type.Criticism,
						ICritic.Priority.High,
						"Use of Mulitple Container for a Table Design",
						"It seems you are using two containers for creating a table-like design.\n" +
						"The first container is created due to line number " + array[0].getLocationString() + ".\n" +
						"The second container is created due to line number " + array[1].getLocationString() +".\n" +
						"Instead of using multiple containers for creating one table, you may use \n" +
						"layout managers such as GridLayout, SpringLayout, or GridbagLayout to achieve \n" +
						"a better result.\n"
						);
				critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html#gridbag");
				result.add(critic);
			}
		}
	}
	
	private static boolean isAllLabels(ClosedList<Value> children) {
		for(int i = 0; i < children.size(); ++i) {
			if(!(children.get(0) instanceof JLabelAbstraction)) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean isTablePair(ClosedList<Value> children, ClosedList<Value> adjChildren) {
		if(children == null || adjChildren == null)
			return false;
		
		if(children.size() != adjChildren.size())
			return false;
		
		
		if(isAllLabels(children) || isAllLabels(adjChildren))
				return true;
		
		if(children.size() != 2)
			return false;

		boolean match = false;
		for(int i = 0; i < children.size(); ++i) {
			ISymbol<? extends Value> comp = children.get(i);
			if(comp instanceof JLabelAbstraction) {
				ISymbol<? extends Value> adjComp = children.get(i);
				if(adjComp instanceof JLabelAbstraction) {
					match = true;
					break;
				}
			}
		}
		
		return match;
	}
		
	private static List<JComponentAbstraction[]> getAllPairs(BorderLayoutAbstraction abstraction) {
		JComponentAbstraction center = (JComponentAbstraction)abstraction.get(new ClosedString(BorderLayout.CENTER));
		JComponentAbstraction north = (JComponentAbstraction)abstraction.get(new ClosedString(BorderLayout.NORTH));
		JComponentAbstraction south = (JComponentAbstraction)abstraction.get(new ClosedString(BorderLayout.SOUTH));
		JComponentAbstraction east = (JComponentAbstraction)abstraction.get(new ClosedString(BorderLayout.EAST));
		JComponentAbstraction west = (JComponentAbstraction)abstraction.get(new ClosedString(BorderLayout.WEST));

		JComponentAbstraction pageStart = (JComponentAbstraction)abstraction.get(new ClosedString(BorderLayout.PAGE_START));
		JComponentAbstraction pageEnd = (JComponentAbstraction)abstraction.get(new ClosedString(BorderLayout.PAGE_END));
		JComponentAbstraction lineStart = (JComponentAbstraction)abstraction.get(new ClosedString(BorderLayout.LINE_START));
		JComponentAbstraction lineEnd = (JComponentAbstraction)abstraction.get(new ClosedString(BorderLayout.LINE_END));
		

		List<JComponentAbstraction[]> list = new ArrayList<JComponentAbstraction[]>();
		
		if(west == null && east == null) {
			if(north != null && center != null) {
				list.add(new JComponentAbstraction[]{north, center});
			}
			if(center != null && south != null) {
				list.add(new JComponentAbstraction[]{center, south});
			}
		}
		
		if(west != null && center != null) {
			list.add(new JComponentAbstraction[]{west, center});
		}
		if(center != null && east != null) {
			list.add(new JComponentAbstraction[]{center, east});
		}
		
		if(west == null && center == null) {
			if(north != null && east != null) {
				list.add(new JComponentAbstraction[]{north, east});
			}
			if(east != null && south != null) {
				list.add(new JComponentAbstraction[]{east, south});
			}
		}

		if(center == null && east == null) {
			if(north != null && west != null) {
				list.add(new JComponentAbstraction[]{north, west});
			}
			if(west != null && south != null) {
				list.add(new JComponentAbstraction[]{west, south});
			}
		}
		
		if(west == null && center == null && east == null) {
			if(north != null && south != null) {
				list.add(new JComponentAbstraction[]{north, south});
			}
		}
		
		if(center == null) {
			if(west != null && east != null) {
				list.add(new JComponentAbstraction[]{west, east});
			}
		}
		
		if(lineStart == null && lineEnd == null) {
			if(pageStart != null && center != null) {
				list.add(new JComponentAbstraction[]{pageStart, center});
			}
			if(center != null && pageEnd != null) {
				list.add(new JComponentAbstraction[]{center, pageEnd});
			}
		}
		
		
		if(lineStart != null && center != null) {
			list.add(new JComponentAbstraction[]{lineStart, center});
		}
		if(center != null && lineEnd != null) {
			list.add(new JComponentAbstraction[]{center, lineEnd});
		}
		
		if(lineStart == null && center == null) {
			if(pageStart != null && lineEnd != null) {
				list.add(new JComponentAbstraction[]{pageStart, lineEnd});
			}
			if(lineEnd != null && pageEnd != null) {
				list.add(new JComponentAbstraction[]{lineEnd, pageEnd});
			}
		}

		if(center == null && lineEnd == null) {
			if(pageStart != null && lineStart != null) {
				list.add(new JComponentAbstraction[]{pageStart, lineStart});
			}
			if(lineStart != null && pageEnd != null) {
				list.add(new JComponentAbstraction[]{lineStart, pageEnd});
			}
		}
		
		if(lineStart == null && center == null && lineEnd == null) {
			if(pageStart != null && pageEnd != null) {
				list.add(new JComponentAbstraction[]{pageStart, pageEnd});
			}
		}
		
		if(center == null) {
			if(lineStart != null && lineEnd != null) {
				list.add(new JComponentAbstraction[]{lineStart, lineEnd});
			}
		}
		
		return list;
	}	
}
