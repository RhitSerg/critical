/*
 * SwingFactory.java
 * Mar 17, 2011
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
 
package edu.clarkson.serl.critic.swing;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Status;

import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;
import edu.clarkson.serl.critic.CriticPlugin;
import edu.clarkson.serl.critic.extension.ICheckPoint;
import edu.clarkson.serl.critic.factory.IFactory;
import edu.clarkson.serl.critic.interpreter.ISymbol;
import edu.clarkson.serl.critic.swing.poi.CheckMethodEnd;
import edu.clarkson.serl.critic.swing.poi.CheckPathEnd;
import edu.clarkson.serl.critic.swing.types.ActionEventAbstraction;
import edu.clarkson.serl.critic.swing.types.BorderLayoutAbstraction;
import edu.clarkson.serl.critic.swing.types.BoxAbstraction;
import edu.clarkson.serl.critic.swing.types.BoxLayoutAbstraction;
import edu.clarkson.serl.critic.swing.types.DimensionAbstraction;
import edu.clarkson.serl.critic.swing.types.FlowLayoutAbstraction;
import edu.clarkson.serl.critic.swing.types.GraphicsAbstraction;
import edu.clarkson.serl.critic.swing.types.JButtonAbstraction;
import edu.clarkson.serl.critic.swing.types.JComboBoxAbstraction;
import edu.clarkson.serl.critic.swing.types.JComponentAbstraction;
import edu.clarkson.serl.critic.swing.types.JDesktopIconAbstraction;
import edu.clarkson.serl.critic.swing.types.JEditorPaneAbstraction;
import edu.clarkson.serl.critic.swing.types.JFileChooserAbstraction;
import edu.clarkson.serl.critic.swing.types.JFrameAbstraction;
import edu.clarkson.serl.critic.swing.types.JInternalFrameAbstraction;
import edu.clarkson.serl.critic.swing.types.JLabelAbstraction;
import edu.clarkson.serl.critic.swing.types.JLayeredPaneAbstraction;
import edu.clarkson.serl.critic.swing.types.JListAbstraction;
import edu.clarkson.serl.critic.swing.types.JMenuBarAbstraction;
import edu.clarkson.serl.critic.swing.types.JOptionPaneAbstraction;
import edu.clarkson.serl.critic.swing.types.JPanelAbstraction;
import edu.clarkson.serl.critic.swing.types.JPopupMenuAbstraction;
import edu.clarkson.serl.critic.swing.types.JProgressBarAbstraction;
import edu.clarkson.serl.critic.swing.types.JRadioButtonAbstraction;
import edu.clarkson.serl.critic.swing.types.JRootPaneAbstraction;
import edu.clarkson.serl.critic.swing.types.JScrollBarAbstraction;
import edu.clarkson.serl.critic.swing.types.JScrollPaneAbstraction;
import edu.clarkson.serl.critic.swing.types.JSeperatorAbstraction;
import edu.clarkson.serl.critic.swing.types.JSliderAbstraction;
import edu.clarkson.serl.critic.swing.types.JSpinnerAbstraction;
import edu.clarkson.serl.critic.swing.types.JSplitPaneAbstraction;
import edu.clarkson.serl.critic.swing.types.JTabbedPaneAbstraction;
import edu.clarkson.serl.critic.swing.types.JTableAbstraction;
import edu.clarkson.serl.critic.swing.types.JTableHeaderAbstraction;
import edu.clarkson.serl.critic.swing.types.JTextAreaAbstraction;
import edu.clarkson.serl.critic.swing.types.JTextFieldAbstraction;
import edu.clarkson.serl.critic.swing.types.JToolBarAbstraction;
import edu.clarkson.serl.critic.swing.types.JToolTipAbstraction;
import edu.clarkson.serl.critic.swing.types.JTreeAbstraction;
import edu.clarkson.serl.critic.swing.types.JViewportAbstraction;
import edu.clarkson.serl.critic.swing.types.PointAbstraction;
import edu.clarkson.serl.critic.swing.types.RectangleAbstraction;
import edu.clarkson.serl.critic.util.EntryFinder;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class SwingFactory implements IFactory {
	public static final String[] TYPES = {
		ActionEventAbstraction.TYPE,
		BorderLayoutAbstraction.TYPE,
		BoxAbstraction.TYPE,
		BoxLayoutAbstraction.TYPE,
		DimensionAbstraction.TYPE,
		FlowLayoutAbstraction.TYPE,
		GraphicsAbstraction.TYPE,
		JButtonAbstraction.TYPE,
		JComboBoxAbstraction.TYPE,
		JComponentAbstraction.TYPE,
		JDesktopIconAbstraction.TYPE,
		JEditorPaneAbstraction.TYPE,
		JFileChooserAbstraction.TYPE,
		JFrameAbstraction.TYPE,
		JInternalFrameAbstraction.TYPE,
		JLabelAbstraction.TYPE,
		JLayeredPaneAbstraction.TYPE,
		JListAbstraction.TYPE,
		JMenuBarAbstraction.TYPE,
		JOptionPaneAbstraction.TYPE,
		JPanelAbstraction.TYPE,
		JPopupMenuAbstraction.TYPE,
		JProgressBarAbstraction.TYPE,
		JRadioButtonAbstraction.TYPE,
		JRootPaneAbstraction.TYPE,
		JScrollBarAbstraction.TYPE,
		JScrollPaneAbstraction.TYPE,
		JSeperatorAbstraction.TYPE,
		JSliderAbstraction.TYPE,
		JSpinnerAbstraction.TYPE,
		JSplitPaneAbstraction.TYPE,
		JTabbedPaneAbstraction.TYPE,
		JTableAbstraction.TYPE,
		JTableHeaderAbstraction.TYPE,
		JTextAreaAbstraction.TYPE,
		JTextFieldAbstraction.TYPE,
		JToolBarAbstraction.TYPE,
		JToolTipAbstraction.TYPE,
		JTreeAbstraction.TYPE,
		JViewportAbstraction.TYPE,
		PointAbstraction.TYPE,
		RectangleAbstraction.TYPE
	};
	
	Set<ICheckPoint> checkPoints;
	Set<SootMethod> entryMethods;
	public SwingFactory() {
		this.checkPoints = new HashSet<ICheckPoint>();
		this.checkPoints.add(new CheckMethodEnd());
		this.checkPoints.add(new CheckPathEnd());
		
		this.entryMethods = new HashSet<SootMethod>();
	}
	
	public String getId() {
		return "javax.swing";
	}

	private Set<String> types = null;
	public Set<String> getSupportedTypes() {
		if(types == null) {
			types = new HashSet<String>();
			types.addAll(Arrays.asList(TYPES));
		}
		return types;
	}

	public ISymbol<? extends Value> newSymbol(String type, Value value, boolean open, boolean mutable) {
		try {
			Class<?> clazz = this.getClassFor(type);
			Constructor<?> constructor = clazz.getConstructor(Value.class, Boolean.TYPE, Boolean.TYPE);
			@SuppressWarnings("unchecked")
			ISymbol<? extends Value> symbol = (ISymbol<? extends Value>)constructor.newInstance(value, open, mutable);
			return symbol;
		}
		catch(Exception e) {
			CriticPlugin.log(Status.ERROR, e.getMessage(), e);
		}
		return null;
	}

	public Set<ICheckPoint> getCheckPoints() {
		return this.checkPoints;
	}
	
	public Set<SootMethod> getEntryMethods() {
		return this.entryMethods;
	}

	public void checkEntry(SootMethod method) {
		if(!method.isConcrete())
			return;
		
		try {
			if(EntryFinder.isIntializedTypeOrSuperType(JFrameAbstraction.TYPE, method))
				this.entryMethods.add(method);
		}
		catch(Exception e) {
			CriticPlugin.log(Status.ERROR, "Error evaluating an entry point for " + method + ".", e);
		}
	}
	
	@Override
	public boolean shouldInline(SootMethod method, ISymbol<? extends Value> receiver, List<ISymbol<? extends Value>> arguments, Stmt callSite) {
		if(!method.isConcrete())
			return false;
		
		try {
			if(EntryFinder.isUsedTypeOrSuperType(JComponentAbstraction.TYPE, method))
				return true;
			if(EntryFinder.isUsedTypeOrSuperType(JFrameAbstraction.TYPE, method))
				return true;
		}
		catch(Exception e) {
			CriticPlugin.log(Status.ERROR, "Error evaluating whether " + method + " should be inlined.", e);
		}
		
		return false;
	}
	

	@Override
	public Class<?> getClassFor(String type) {
		if(type.equals(ActionEventAbstraction.TYPE))
			return ActionEventAbstraction.class;
		if(type.equals(BorderLayoutAbstraction.TYPE))
			return BorderLayoutAbstraction.class;
		if(type.equals(BoxAbstraction.TYPE))
			return BoxAbstraction.class;
		if(type.equals(BoxLayoutAbstraction.TYPE))
			return BoxLayoutAbstraction.class;
		if(type.equals(DimensionAbstraction.TYPE))
			return DimensionAbstraction.class;
		if(type.equals(FlowLayoutAbstraction.TYPE))
			return FlowLayoutAbstraction.class;
		if(type.equals(GraphicsAbstraction.TYPE))
			return GraphicsAbstraction.class;
		if(type.equals(JButtonAbstraction.TYPE))
			return JButtonAbstraction.class;
		if(type.equals(JComboBoxAbstraction.TYPE))
			return JComboBoxAbstraction.class;
		if(type.equals(JComponentAbstraction.TYPE))
			return JComponentAbstraction.class;
		if(type.equals(JDesktopIconAbstraction.TYPE))
			return JDesktopIconAbstraction.class;
		if(type.equals(JEditorPaneAbstraction.TYPE))
			return JEditorPaneAbstraction.class;
		if(type.equals(JFileChooserAbstraction.TYPE))
			return JFileChooserAbstraction.class;
		if(type.equals(JFrameAbstraction.TYPE))
			return JFrameAbstraction.class;
		if(type.equals(JInternalFrameAbstraction.TYPE))
			return JInternalFrameAbstraction.class;
		if(type.equals(JLabelAbstraction.TYPE))
			return JLabelAbstraction.class;
		if(type.equals(JLayeredPaneAbstraction.TYPE))
			return JLayeredPaneAbstraction.class;
		if(type.equals(JListAbstraction.TYPE))
			return JListAbstraction.class;
		if(type.equals(JMenuBarAbstraction.TYPE))
			return JMenuBarAbstraction.class;
		if(type.equals(JOptionPaneAbstraction.TYPE))
			return JOptionPaneAbstraction.class;
		if(type.equals(JPanelAbstraction.TYPE))
			return JPanelAbstraction.class;
		if(type.equals(JPopupMenuAbstraction.TYPE))
			return JPopupMenuAbstraction.class;
		if(type.equals(JProgressBarAbstraction.TYPE))
			return JProgressBarAbstraction.class;
		if(type.equals(JRadioButtonAbstraction.TYPE))
			return JRadioButtonAbstraction.class;
		if(type.equals(JRootPaneAbstraction.TYPE))
			return JRootPaneAbstraction.class;
		if(type.equals(JScrollBarAbstraction.TYPE))
			return JScrollBarAbstraction.class;
		if(type.equals(JScrollPaneAbstraction.TYPE))
			return JScrollPaneAbstraction.class;
		if(type.equals(JSeperatorAbstraction.TYPE))
			return JSeperatorAbstraction.class;
		if(type.equals(JSliderAbstraction.TYPE))
			return JSliderAbstraction.class;
		if(type.equals(JSpinnerAbstraction.TYPE))
			return JSpinnerAbstraction.class;
		if(type.equals(JSplitPaneAbstraction.TYPE))
			return JSplitPaneAbstraction.class;
		if(type.equals(JTabbedPaneAbstraction.TYPE))
			return JTabbedPaneAbstraction.class;
		if(type.equals(JTableAbstraction.TYPE))
			return JTableAbstraction.class;
		if(type.equals(JTableHeaderAbstraction.TYPE))
			return JTableHeaderAbstraction.class;
		if(type.equals(JTextAreaAbstraction.TYPE))
			return JTextAreaAbstraction.class;
		if(type.equals(JTextFieldAbstraction.TYPE))
			return JTextFieldAbstraction.class;
		if(type.equals(JToolBarAbstraction.TYPE))
			return JToolBarAbstraction.class;
		if(type.equals(JToolTipAbstraction.TYPE))
			return JToolTipAbstraction.class;
		if(type.equals(JTreeAbstraction.TYPE))
			return JTreeAbstraction.class;
		if(type.equals(JViewportAbstraction.TYPE))
			return JViewportAbstraction.class;
		if(type.equals(PointAbstraction.TYPE))
			return PointAbstraction.class;
		if(type.equals(RectangleAbstraction.TYPE))
			return RectangleAbstraction.class;
		return null;
	}
}
