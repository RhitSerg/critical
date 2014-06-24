/*
 * JComponentAbstraction.java
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
public class JComponentAbstraction  extends SymbolicApi<Value> implements ICheckable<Value> {
	public static final String TYPE = "javax.swing.JComponent";
	public static final Value DEFAULT_VALUE = AbstractValue.fromObject(TYPE);
	
	public static final SymbolicKey LOCATION = SymbolicKey.fromObject("location");
	public static final SymbolicKey SIZE = SymbolicKey.fromObject("size");
	public static final SymbolicKey PREFERRED_SIZE = SymbolicKey.fromObject("preferredSize");
	public static final SymbolicKey MIN_SIZE = SymbolicKey.fromObject("minSize");
	public static final SymbolicKey MAX_SIZE = SymbolicKey.fromObject("maxSize");
	
	public static final SymbolicKey LAYOUT = SymbolicKey.fromObject("layout");
	public static final SymbolicKey COMPONENTS = SymbolicKey.fromObject("components");
	public static final SymbolicKey VALID = SymbolicKey.fromObject("valid");
	public static final SymbolicKey PARENT = SymbolicKey.fromObject("parent");
	
	public JComponentAbstraction() {
		super(DEFAULT_VALUE, false, true);
	}
	
	public JComponentAbstraction(Value value) {
		super(value, false, true);
	}
	
	public JComponentAbstraction(Value value, boolean open, boolean mutable) {
		super(value, open, mutable);
	}

	@Override
	public IResult execute(InvokeExpr invokeExpr, List<ISymbol<? extends Value>> arguments) {
		String method  = invokeExpr.getMethod().getName();
		// We only simulate size properties for the constructor
		if(method.equals("<init>")) {
			return this.init(arguments);
		}
		else if(method.equals("setBounds")) {
			return this.setBounds(arguments);
		}
		else if(method.equals("getSize")) {
			return this.getSize();
		}
		else if(method.equals("setSize")) {
			return this.setSize(arguments);
		}
		else if(method.equals("getPreferredSize")) {
			return this.getPreferredSize();
		}
		else if(method.equals("setPreferredSize")) {
			return this.setPreferredSize(arguments);
		}
		else if(method.equals("getMinimumSize")) {
			return this.getMinimumSize();
		}
		else if(method.equals("setMinimumSize")) {
			return this.setMinimumSize(arguments);
		}
		else if(method.equals("getMaximumSize")) {
			return this.getMaximumSize();
		}
		else if(method.equals("setMaximumSize")) {
			return this.setMaximumSize(arguments);
		}
		else if(method.equals("setLocation")) {
			return this.setLocation(arguments);
		}
		else if(method.equals("getLocation")) {
			return this.getLocation();
		}
		else if(method.equals("setLayout")) {
			return this.setLayout(arguments);
		}
		else if(method.equals("getLayout")) {
			return new Result(this.get(LAYOUT));
		}
		else if(method.equals("repaint")) {
			return this.repaint();
		}
		else if(method.equals("getParent")) {
			return this.getParent();
		}
		else if(method.equals("add")) {
			return this.add(arguments);
		}
		else if(method.equals("remove")) {
			return this.remove(arguments);
		}
		else if(method.equals("invalidate")) {
			this.put(VALID, Interpreter.FALSE);
			return new Result(Interpreter.VOID);
		}
		else if(method.equals("validate") || method.equals("revalidate")) {
			return this.revalidate();
		}
		else if(method.equals("setVisible")) {
			return this.setVisible(arguments);
		}
		else if(method.equals("paint")) {
			return this.paint(arguments);
		}
		else if(method.equals("setAlignmentX") ||
				method.equals("setAlignmentY") ||
				method.equals("setHorizontalAlignment") ||
				method.equals("setVerticalAlignment") ||
				method.equals("setHorizontalTextPosition") ||
				method.equals("setVerticalTextPoisition")) {
			if(this instanceof JButtonAbstraction || 
				this instanceof JLabelAbstraction || 
				this instanceof JRadioButtonAbstraction ||
				this instanceof JComboBoxAbstraction) {
				ICritic critic = new Critic(
						TYPE + "-Alignment-Recommendation",
						ICritic.Type.Recommendation,
						ICritic.Priority.Medium,
						"Use of Confusing APIs",
						"You are using an API that controls alignment for a GUI widget. " +
						"setAlignmentX() and setAlignmentY() is designed to be used with BoxLayout " +
						"with parameters such as TOP_ALIGNMENT, LEFT_ALIGNMENT, etc. " +
						"setHorizontalAlignment() and setVerticalAlignment() is designed to be used " +
						"with other layouts such as GridLayout with parameters such as TOP, LEFT, etc. " +
						"setHorizontalTextPosition() and setVerticalTextPoisition() are used in JButtons " +
						"and JLabels to align text relative to the icon image with parameters such as TOP, LEFT, etc."
				);
				String url = CriticPlugin.getDocumentURL(SwingCriticPlugin.PLUGIN_ID, "RE-Alignment.html");
				critic.setAttribute(ICritic.URL, url);
				critic.setAttribute("-CLASS-", critic.getOutermostClass());
				critic.setAttribute("-LINE-", critic.getLineNumber());
				
				IResult result = new Result(Interpreter.VOID);
				result.add(critic);
				return result;
			}
		}
		
		// We do not handle other methods
		return new Result(DefaultSymbolFactory.getOpenMutableSymbol(invokeExpr));
	}
	
	public IResult init(List<ISymbol<? extends Value>> arguments) {
		this.put(SIZE, DimensionAbstraction.getDefaultSize());
		this.put(PREFERRED_SIZE, DimensionAbstraction.getDefaultSize());
		this.put(MIN_SIZE, DimensionAbstraction.getDefaultSize());
		this.put(MAX_SIZE, DimensionAbstraction.getDefaultSize());
		ISymbol<? extends Value> symFalse = Interpreter.FALSE;
		symFalse = ConstInteger.fromInteger(0);
		this.put(VALID, symFalse);
		return new Result(Interpreter.VOID);
	}
	
	public IResult paint(List<ISymbol<? extends Value>> arguments) {
		if(!arguments.isEmpty()) {
			ISymbol<? extends Value> graphics = arguments.get(0);
			if(graphics instanceof GraphicsAbstraction) {
				((GraphicsAbstraction) graphics).setSuperPaint(true);
			}
		}
		return new Result(Interpreter.VOID);
	}

	
	public IResult revalidate() {
		IResult myResult = new Result(Interpreter.VOID);

		// Recursively enter the child and start layout from the child
		@SuppressWarnings("unchecked")
		ClosedList<Value> children = (ClosedList<Value>)this.get(COMPONENTS);
		if(children != null) {
			Iterator<ISymbol<? extends Value>> iterator = children.iterator();
			while(iterator.hasNext()) {
				ISymbol<? extends Value> child = iterator.next();
				if(child instanceof JComponentAbstraction) {
					myResult.add(((JComponentAbstraction)child).revalidate());
				}
			}
		}

		
		// Set the child component to be in the valid state
		this.put(VALID, Interpreter.TRUE);
		
		// Perform the layout job in the child
		ISymbol<? extends Value> layout = this.get(LAYOUT);
		if(layout instanceof LayoutManagerAbstraction) {
			myResult.add(((LayoutManagerAbstraction)layout).layoutContainer(this));
		}
		
		// If parent is a JComponent, then do layout and size related checking
		ISymbol<? extends Value> parent = this.get(PARENT);
		if(parent instanceof JComponentAbstraction) {
			layout = parent.get(LAYOUT);
			if(layout == null || layout.getValue() == null) {
				// Using absolute layout but size set through preferred size
				DimensionAbstraction prefSize = (DimensionAbstraction)this.get(JComponentAbstraction.PREFERRED_SIZE); 
				if(prefSize != null && prefSize.isUserDefined()) {
					ICritic critic = new Critic(
							TYPE + "-Absolutelayout-Component",
							this.getLineNumber(),
							ICritic.Type.Criticism,
							ICritic.Priority.Medium,
							"Use of Absolute Layout (null layout) and JComponent.setPreferredSize() Together",
							"You are adding a component that is laid out by absolute positioning (null layout). " +
							"However, there is a path till this point that also sets the preferred size of " +
							"the  component using setPreferredSize() method. Either use a non-null layout by calling " +
							"Container.setLayout(new FlowLayout()) for the container or use " +
							"component.setSize(new Dimension(w,h)) for the individual component to " +
							"assign an absolute size. The setPreferredSize() method performs relative positioning that " +
							"will be ignored in the absolute positioning."
							);
					critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/none.html");
					myResult.add(critic);
				}
				
				DimensionAbstraction size = (DimensionAbstraction)this.get(JComponentAbstraction.SIZE);
				if(size == null || !size.isUserDefined()) {
					ICritic critic = new Critic(
							TYPE + "-Absolutelayout:Size-Unknown",
							this.getLineNumber(),
							ICritic.Type.Criticism,
							ICritic.Priority.High,
							"Use of Absolute Layout (null layout) and Unknown Size",
							"You are adding a component that is laid out by absolute positioning (null layout). " +
							"However, this component do not have a size allocated through component.setSize() method. "
							);
					critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/none.html");
					myResult.add(critic);
				}
				PointAbstraction location = (PointAbstraction)this.get(JComponentAbstraction.LOCATION);
				if(location == null || !location.isUserDefined()) {
					ICritic critic = new Critic(
							TYPE + "-Absolutelayout:Location-Unknown",
							this.getLineNumber(),
							ICritic.Type.Criticism,
							ICritic.Priority.High,
							"Use of Absolute Layout (null layout) and Unknown Location",
							"You are adding a component that is laid out by absolute positioning (null layout). " +
							"However, this component do not have a location allocated through component.setLocation() method. "
							);
					critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/none.html");
					myResult.add(critic);
				}			
			}
		}
		
		return myResult;
	}
	
	
	public IResult repaint() {
		IResult result = new Result(Interpreter.VOID);
		
		// Recursively enter the child and start layout from the child
		@SuppressWarnings("unchecked")
		ClosedList<Value> children = (ClosedList<Value>)this.get(COMPONENTS);
		if(children != null) {
			for(ISymbol<? extends Value> child : children) {
				if(child instanceof JComponentAbstraction) {
					((JComponentAbstraction) child).repaint();
				}
			}
		}
		
		ISymbol<? extends Value> visibility = this.get(JFrameAbstraction.VISIBLE);
		if(visibility == null || visibility.equals(Interpreter.FALSE)) {
			return result;
		}
		
		// Check validity
		ISymbol<? extends Value> validObj = this.get(JComponentAbstraction.VALID);
		if(validObj == null || validObj.equals(Interpreter.FALSE)) {
			// We have a problem
			ICritic critic = new Critic(
					TYPE + "-Repaint-Component-Validity",
					ICritic.Type.Criticism,
					ICritic.Priority.High,
					"Painting and Validity Inconsistency",
					"When painting a container through method such as repaint(), it must follow after all of " +
					"its children are in a valid state. A container is in invalid state when its structure " +
					"changes after an addition or deletion of an element. When this happens a pack() method " +
					"on the top level window (e.g. JFrame) can be called to re-validate the container. This is " +
					"also achieved by calling JComponent.validate() when a new component is added or removed " +
					"from the container."
					);
			critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/howLayoutWorks.html");
			result.add(critic);
		}
		
		
		PointAbstraction location = (PointAbstraction)this.getLocation().getValue();
		DimensionAbstraction size = (DimensionAbstraction)this.getSize().getValue();
				
		if(!location.isInitialized() || !size.isInitialized()) {
			// Unknown size and location
			ICritic critic = new Critic(
					TYPE + "Repaint-Called:Unknown-Size-Unknown-Location",
					this.getOutermostClass(),
					this.getLineNumber(),
					ICritic.Type.Criticism,
					ICritic.Priority.High,
					"Painting a Component Without Known Size and Location",
					"When a component is painted, it must have a known size and location. " +
					"Oterwise, a GUI widget may not be visible to the user or may sometimes be out of place. " +
					"The size and location to a component can be explicitly set using setSize(Dimension) and " +
					"setLocation(Point) method or by using setBounds(x,y,width,height) method in the absence of " +
					"a layout manager. Note that when using a layout manager on a container call to revalidate() or " +
					"validate() forces computation of size and location for the container. Similarly, call to " +
					"JFrame.pack() or JFrame.setVisible(true) will force the layout manager of all of the container " +
					"in the GUI hierarchy to compute the size and location. Also note that layout manager may overwrite " +
					"the size and location set through setSize(), setLocation(), and setBounds() method if the layout " +
					"is done after the call to these methods. "
					);
			critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/problems.html");
			result.add(critic);
		}
		return result;
	}	
	
	public IResult setBounds(List<ISymbol<? extends Value>> arguments) {
		IResult result = new Result(Interpreter.VOID);
		if(arguments.size() == 4) {
			PointAbstraction location = new PointAbstraction();
			location.put(PointAbstraction.X, arguments.get(0));
			location.put(PointAbstraction.Y, arguments.get(1));
			
			DimensionAbstraction dimension = new DimensionAbstraction();
			dimension.put(DimensionAbstraction.WIDTH, arguments.get(2));
			dimension.put(DimensionAbstraction.HEIGHT, arguments.get(3));
			
			ArrayList<ISymbol<? extends Value>> args = new ArrayList<ISymbol<? extends Value>>(1);
			args.add(location);
			result.add(this.setLocation(args));
			args = new ArrayList<ISymbol<? extends Value>>(1);
			args.add(dimension);
			result.add(this.setSize(args));
		}
		else if(arguments.size() == 1) {
			RectangleAbstraction rectangle = (RectangleAbstraction)arguments.get(0);
			
			ArrayList<ISymbol<? extends Value>> args = new ArrayList<ISymbol<? extends Value>>(1);
			args.add(rectangle.getLocation());
			result.add(this.setLocation(args));
			args = new ArrayList<ISymbol<? extends Value>>(1);
			args.add(rectangle.getSize());
			result.add(this.setSize(args));
		}
		return result;
	}
	
	public IResult getParent() {
		return new Result(this.get(PARENT));
	}
	
	public IResult setLayout(List<ISymbol<? extends Value>> arguments) {
		IResult result = new Result(Interpreter.VOID);
		ISymbol<? extends Value> layout = arguments.get(0);
		ISymbol<? extends Value> prevLayout = this.get(LAYOUT);
		
		if(prevLayout != null && layout.getType().equals(prevLayout.getType())) {
			// Repeated action
			ICritic critic = new Critic(
					TYPE + "-Repeated-Action:Same-Layout",
					ICritic.Type.Criticism,
					ICritic.Priority.Medium,
					"Idempotent Layout Action",
					"You are setting the same kind of layout to the container twice. This action is redundant." 
					);
			result.add(critic);
		}
		
		// Check if this layout has already been used in another container
		if(layout.getValue() != null && !layout.getAllContainers().isEmpty()) {
			for(ISymbol<? extends Value> container : layout.getAllContainers()) {
				ISymbol<? extends Value> tempLayout = container.get(JComponentAbstraction.LAYOUT);
				if(layout.equals(tempLayout)) {
					// Layout has been used
					ICritic critic = new Critic(
							TYPE + "-Reused-Layout",
							ICritic.Type.Criticism,
							ICritic.Priority.High,
							"Reused Layout",
							"You are reusing a layout that has already been used in another container. " +
							"This may result in unintended behaviors of the corresponding GUI widget " +
							"and should be avoided." 
							);
					result.add(critic);
					break;
				}
			}
		}
		
		this.put(LAYOUT, layout);
		return result;
	}
	
	public IResult setVisible(List<ISymbol<? extends Value>> arguments) {
		IResult result = new Result(Interpreter.VOID); 
		if(arguments.size() != 1)
			return result;
		ISymbol<? extends Value> visibility = arguments.get(0);
		this.put(JFrameAbstraction.VISIBLE, visibility);
		for(ISymbol<? extends Value> child : this.getAllChildren()) {
			child.put(JFrameAbstraction.VISIBLE, visibility);
		}
		return result;
	}
	
	
	public IResult getLocation() {
		PointAbstraction location = (PointAbstraction)this.get(LOCATION);
		if(location == null || location.getValue() == null) {
			location = new PointAbstraction();
			this.put(LOCATION, location);
		}
		return new Result(location);
	}

	public IResult setLocation(List<ISymbol<? extends Value>> arguments) {
		IResult result = new Result(Interpreter.VOID);
		
		ISymbol<? extends Value> parent = this.get(PARENT);
		if(parent instanceof JComponentAbstraction) {
			ISymbol<? extends Value> layout = parent.get(LAYOUT);
			if(layout != null && layout.getValue() != null) {
				// Use of layout and set location together
				ICritic critic = new Critic(
						TYPE + "LayoutManager-Used:SetLocation-Called",
						ICritic.Type.Criticism,
						ICritic.Priority.High,
						"Use of Layout Manager with Absolute Location",
						"When using a layout manager, methods such as setPreferredSize(), " + 
						"setMinimumSize(), and setMaximumSize() should be used for setting sizes. " +
						"Location is determined by the layout manager itself. Use of setLocation() " + 
						"has no effect when using a layout manager."
						);
				critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/problems.html");
				result.add(critic);
			}
		}
		
		DimensionAbstraction preferredSize = (DimensionAbstraction)this.get(PREFERRED_SIZE);
		if(preferredSize != null && preferredSize.isUserDefined()) {
			// We have a problem
			ICritic critic = new Critic(
					TYPE + "PreferredSize-Set:SetLocation-Called",
					ICritic.Type.Criticism,
					ICritic.Priority.Medium,
					"Location and Preferred Size Problem",
					"Your code has a path that has already set the preferred size for this JComponent at this point. " +
					"Instead of setting location call pack() method on the frame that will lay out " +
					"all of the components with the preferred size and automatically calculated location. "
					);
			critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/problems.html");
			result.add(critic);
		}

		if(arguments.size() == 1) {
			// setLocation(new Point(x,y))
			this.put(LOCATION, arguments.get(0));
		}
		else {
			// setLocation(x,y)
			PointAbstraction point = new PointAbstraction();
			point.init(arguments);
			this.put(LOCATION, point);
		}
		return result;
	}
	
	public IResult getSize() {
		DimensionAbstraction size = (DimensionAbstraction)this.get(SIZE);
		if(size == null || size.getValue() == null) {
			size = new DimensionAbstraction();
			this.put(SIZE, size);
		}
		return new Result(size);
	}

	public IResult setSize(List<ISymbol<? extends Value>> arguments) {
		IResult result = new Result(Interpreter.VOID);
		
		ISymbol<? extends Value> parent = this.get(PARENT);
		if(parent instanceof JComponentAbstraction) {
			ISymbol<? extends Value> layout = parent.get(LAYOUT);
			if(layout != null && layout.getValue() != null) {
				// Use of layout and set size together
				ICritic critic = new Critic(
						TYPE + "Layout-Manager:SetSize-Called",
						ICritic.Type.Criticism,
						ICritic.Priority.High,
						"Use of Layout Manager with Absolute Size",
						"When using layout manager, methods such as setPreferredSize(), " + 
						"setMinimumSize(), and setMaximumSize() should be used instead of setSize(). " +
						"Use of setSize() has no effect when using a layout manager."
						);
				critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/problems.html");
				result.add(critic);
			}
		}
		
		DimensionAbstraction preferredSize = (DimensionAbstraction)this.get(PREFERRED_SIZE);
		if(preferredSize != null && preferredSize.isUserDefined()) {
			// We have a problem
			ICritic critic = new Critic(
					TYPE + "PreferredSize-Set:SetSize-Called",
					ICritic.Type.Criticism,
					ICritic.Priority.Medium,
					"Size and Preferred Size Problem",
					"Your code has a path that has already been set the preferred size for this JComponent " +
					"at this point. Instead of setting size again call pack() method on the container of this " +
					"object that will lay out all of the components with the preferred size."
					);
			critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/problems.html");
			result.add(critic);
		}
		if(arguments.size() == 1) {
			// setLocation(new Dimension(width,height))
			this.put(SIZE, arguments.get(0));
		}
		else {
			// setLocation(width,height)
			DimensionAbstraction dimension = new DimensionAbstraction();
			dimension.init(arguments);
			this.put(SIZE, dimension);
		}
		return result;
	}
	
	public IResult getPreferredSize() {
		DimensionAbstraction pSize = (DimensionAbstraction)this.get(PREFERRED_SIZE);
		if(pSize == null || pSize.getValue() == null) {
			pSize = new DimensionAbstraction();
			this.put(PREFERRED_SIZE, pSize);
		}
		return new Result(pSize);
	}
	
	public IResult setPreferredSize(List<ISymbol<? extends Value>> arguments) {
		IResult result = new Result(Interpreter.VOID);
		
		ISymbol<? extends Value> parent = this.get(PARENT);
		if(parent instanceof JComponentAbstraction) {
			ISymbol<? extends Value> layout = parent.get(LAYOUT);
			if(layout == null || layout.getValue() == null) {
				// Use of absolute position and set preferred size together
				ICritic critic = new Critic(
						TYPE + "Null-Layout:SetPreferredSize-Called",
						ICritic.Type.Criticism,
						ICritic.Priority.High,
						"Use of Null Layout with Preferred Size",
						"When using absolute positioning (null layout), setPreferredSize() has no effect. " +
						"Instead, use (setSize() and setLocation()) or setBounds() method for setting size and location. "
						);
				critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/problems.html");
				result.add(critic);
			}
		}
		
		DimensionAbstraction size = (DimensionAbstraction)this.get(SIZE);
		if(size != null && size.isUserDefined()) {
			// We have a problem
			ICritic critic = new Critic(
					TYPE + "Size-Set:SetPreferredSize-Called",
					ICritic.Type.Criticism,
					ICritic.Priority.Medium,
					"Size and Preferred Size Problem",
					"Your code has a path that has already set the size for this JComponent at this point. " +
					"Instead of setting preferred size again use null layout and call repaint() method of the " +
					" container to diplay sub-components. Note that this not the best way to lay out components " +
					"on a window instead use preferred size for all of the sub-components and call the pack() " +
					"method of the container."
					);
			critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/problems.html");
			result.add(critic);
		}
		
		if(arguments.size() == 1) {
			// setLocation(new Dimension(width,height))
			this.put(PREFERRED_SIZE, arguments.get(0));
		}
		else {
			// setLocation(width,height)
			DimensionAbstraction dimension = new DimensionAbstraction();
			dimension.init(arguments);
			this.put(PREFERRED_SIZE, dimension);
		}
		
		return result;
	}
	
	public IResult getMinimumSize() {
		DimensionAbstraction pSize = (DimensionAbstraction)this.get(MIN_SIZE);
		if(pSize == null || pSize.getValue() == null) {
			pSize = new DimensionAbstraction();
			this.put(MIN_SIZE, pSize);
		}
		return new Result(pSize);
	}
	
	public IResult setMinimumSize(List<ISymbol<? extends Value>> arguments) {
		if(arguments.size() == 1) {
			// setLocation(new Dimension(width,height))
			this.put(MIN_SIZE, arguments.get(0));
		}
		else {
			// setLocation(width,height)
			DimensionAbstraction dimension = new DimensionAbstraction();
			dimension.init(arguments);
			this.put(MIN_SIZE, dimension);
		}
		
		return new Result(Interpreter.VOID);
	}
	
	public IResult getMaximumSize() {
		DimensionAbstraction size = (DimensionAbstraction)this.get(MAX_SIZE);
		if(size == null || size.getValue() == null) {
			size = new DimensionAbstraction();
			this.put(MAX_SIZE, size);
		}
		return new Result(size);
	}
	
	public IResult setMaximumSize(List<ISymbol<? extends Value>> arguments) {
		if(arguments.size() == 1) {
			// setLocation(new Dimension(width,height))
			this.put(MAX_SIZE, arguments.get(0));
		}
		else {
			// setLocation(width,height)
			DimensionAbstraction dimension = new DimensionAbstraction();
			dimension.init(arguments);
			this.put(MAX_SIZE, dimension);
		}
		return new Result(Interpreter.VOID);
	}
	
	public IResult remove(List<ISymbol<? extends Value>> arguments) {
		IResult result = new Result(Interpreter.VOID);

		// Whenever a component is remove from a container, it is in invalid state
		this.put(VALID, Interpreter.FALSE);
		
		// Remove from component list
		@SuppressWarnings("unchecked")
		ClosedList<Value> list = (ClosedList<Value>)this.get(COMPONENTS);
		if(list != null) {
			ISymbol<? extends Value> component = arguments.get(0);
			if(component instanceof ConstInteger) {
				int index = (Integer)((ConstInteger)component).getValue();
				component = list.remove(index);
				component.put(PARENT, Interpreter.NULL);
			}
			else if(list.remove(component)) {
				component.put(PARENT, Interpreter.NULL);
			}
		}

		// Remove from layout manager
		ISymbol<? extends Value> layoutObj = this.get(LAYOUT);
		if(layoutObj instanceof LayoutManagerAbstraction) {
			result.add(((LayoutManagerAbstraction)layoutObj).removeLayoutComponent(arguments));
		}
		
		return result;
	}
	
	public IResult add(List<ISymbol<? extends Value>> arguments) {
		// Whenever a component is added the container is in invalid state
		this.put(VALID, Interpreter.FALSE);
		
		JComponentAbstraction component = null;
		ClosedString constraint = null;
		
		for(ISymbol<? extends Value> o : arguments) {
			if(o instanceof JComponentAbstraction) {
				component = (JComponentAbstraction)o;
			}
			else if(o instanceof ClosedString) {
				constraint = (ClosedString)o;
			}
		}
		
		// Add to the list first
		@SuppressWarnings("unchecked")
		ClosedList<Value> list = (ClosedList<Value>)this.get(COMPONENTS);
		if(list == null) {
			list = new ClosedList<Value>(AbstractValue.fromObject("java.util.List"));
			this.put(COMPONENTS, list);
		}
		
		boolean added = false;
		
		if(!list.contains(component)) {
			added = list.add(component);
		}

		IResult myResult = new Result(component);
		if(!added) {
			ICritic critic = new Critic(
					TYPE + "-Adding-Duplicate",
					ICritic.Type.Criticism,
					ICritic.Priority.Medium,
					"Trying to Add Duplicate Elements",
					"You are adding a component that is already present in the containter. " +
					"If you want a common behavior for two different widgets then you should " +
					"share their model, not their view. The component is created at " + component.getLocationString() + "."
					);
			critic.setAttribute(ICritic.URL, "http://www.java2s.com/Code/Java/Swing-JFC/TextAreaShareModel.htm");
			myResult.add(critic);
		}
		
		// Check if the element already has a parent which is invisible
		ISymbol<? extends Value> parent = component.get(PARENT);
		if(parent != null && parent.getValue() != null) {
			ISymbol<? extends Value> visibility = parent.get(JFrameAbstraction.VISIBLE);
			if(visibility == null || visibility.equals(Interpreter.FALSE)) {
				if(parent != this.getWorkingCopy()) {
					// Found a problem
					ICritic critic = new Critic(
							TYPE + "-Shared-GUI-Hierarchy",
							ICritic.Type.Criticism,
							ICritic.Priority.High,
							"GUI Widgets Switching Parent Widget",
							"You are swithching a GUI widget in a container before the container is made visible. " +
							"The previous GUI hierarcy of the container will not be visible at runtime."
					);
					critic.setAttribute(ICritic.URL, "http://stackoverflow.com/questions/4620601/cant-a-swing-component-be-added-to-multiple-containers");
					myResult.add(critic);
				}
			}
			if(parent instanceof JComponentAbstraction) {
				List<ISymbol<? extends Value>> args = new ArrayList<ISymbol<? extends Value>>();
				args.add(component);
				((JComponentAbstraction)parent).remove(args);
			}
		}

		// Element is added to the list, so set up parent
		component.put(PARENT, this);
		
		// Add to the layout manager
		// We know component is not null but constraints may be null
		ISymbol<? extends Value> layout = this.get(LAYOUT);
		
		IResult layoutResult = null;		
		if(layout instanceof LayoutManagerAbstraction) {
			LayoutManagerAbstraction manager = (LayoutManagerAbstraction)layout;
			
			// Trying to lay out component but size already defined
			DimensionAbstraction size = (DimensionAbstraction)component.get(JComponentAbstraction.SIZE);
			if(size != null && size.isUserDefined()) {
				ICritic critic = new Critic(
						TYPE + "-Layoutmanager-Component",
						ICritic.Type.Criticism,
						ICritic.Priority.Medium,
						"Use of Layout Manager and JComponent.setSize() Together",
						"You are adding a component that is laid out by a layout manager. " +
						"However, there is a path till this point that also sets the size of " +
						"the  component using setSize() method. Either use a null layout by calling " +
						"Container.setLayout(null) for the container or use " +
						"component.setPreferredSize(new Dimension(w,h)) for the individual component to " +
						"assign a preferred size. The setSize() method performs absolute positioning that " +
						"will be ignored by the layout manager."
						);
				critic.setAttribute(ICritic.URL, "http://stackoverflow.com/questions/1783793/java-difference-between-the-setpreferredsize-and-setsize-methods-in-compone");
				myResult.add(critic);
			}
				
			ArrayList<ISymbol<? extends Value>> args = new ArrayList<ISymbol<? extends Value>>();
			args.add(component);
			args.add(constraint);
			layoutResult = manager.addLayoutComponent(args);
		}
		
		if(layoutResult != null) {
			// Append the result from layout manager to the result in the component list 
			myResult.add(layoutResult);
		}
		
		return myResult;
	}
	
	public Set<ISymbol<? extends Value>> getAllChildren() {
		HashSet<ISymbol<? extends Value>> children = new HashSet<ISymbol<? extends Value>>();
		this.dfsGetChildren(this, children);
		return children;
	}
	
	private void dfsGetChildren(ISymbol<? extends Value> root, HashSet<ISymbol<? extends Value>> list) {
		@SuppressWarnings("unchecked")
		ClosedList<Value> children = (ClosedList<Value>)this.get(COMPONENTS);
		if(children != null) {
			list.addAll(children);
			for(ISymbol<? extends Value> child : children) {
				if(!list.contains(child))
					dfsGetChildren(child, list);
			}
		}
	}

	@Override
	public IResult checkAtEnd() {
		Result result = new Result(Interpreter.VOID);
		ISymbol<? extends Value> parent = this.get(JComponentAbstraction.PARENT);
		if(parent == null || parent.getValue() == null) {
			ICritic critic = new Critic(
					JFrameAbstraction.TYPE + "-Object:Parent-Null",
					this.getOutermostClass(),
					this.getLineNumber(),
					ICritic.Type.Criticism,
					ICritic.Priority.High,
					"Widget Not Contained in GUI Hierarchy",
					"You have created a GUI widget at " + this.getLocationString() + " that is not assigned to any " +
					"top-level GUI widget and is not used in your source code. This is probably not what you want! "
					);
			critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/components/toplevel.html");
			result.add(critic);
		}
		
		// If a local component is visible but invalid
		ISymbol<? extends Value> visible = this.get(JFrameAbstraction.VISIBLE);
		if(visible != null && visible.equals(Interpreter.TRUE)) {
			ISymbol<? extends Value> validity = this.get(JComponentAbstraction.VALID);
			if(validity == null || validity.equals(Interpreter.FALSE)) {
				ICritic critic = new Critic(
						JComponentAbstraction.TYPE + "-Object:Visible-InValid",
						this.getOutermostClass(),
						this.getLineNumber(),
						ICritic.Type.Criticism,
						ICritic.Priority.High,
						"Local GUI Widget Visible But Invalid",
						"You have created a GUI widget " + this.getLocationString() + " that has been changed after it " +
						"has been made visible. The effect of such change is not visible until " +
						"widget.revalidate() followed by widget.repaint() is called or the " +
						"pack() method of the JFrame/JDialog that contains it is called. "
						);
				critic.setAttribute(ICritic.URL, "http://docs.oracle.com/javase/tutorial/uiswing/layout/problems.html");
				result.add(critic);
			}
		}
		
		return result;
	}
}
