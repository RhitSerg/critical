package edu.clarkson.serl.critic.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import edu.clarkson.serl.critic.CriticPlugin;
import edu.clarkson.serl.critic.extension.ExtensionManager;

public class CriticalPropertyPage extends PropertyPage {
	Text loopUnrollLimit;
	Text methodDepth;
	Text pathCutOffSize;
	Text delay;
	Text poolWaitTime;
	
	Button addNewRule;
	Button deleteRule;
	Table table;
	
	Preferences preferences;
	
	public CriticalPropertyPage() {
	}
	

	@Override
	public boolean performOk() {
		this.updateFromFields();
		CriticPlugin.writePreferencesToStore(this.getProject(), this.preferences);

		return super.performOk();
	}


	@Override
	protected void performApply() {
		this.updateFromFields();
		CriticPlugin.writePreferencesToStore(this.getProject(), this.preferences);
		
		super.performApply();
	}

	@Override
	protected void performDefaults() {
		boolean ans = MessageDialog.openQuestion(this.getShell(), "Confirm Restore Defaults", "Your previous settings will be overwritten. Are you sure?");
		if(!ans)
			return;
		
		super.performDefaults();

		
		this.preferences = new Preferences();
		this.updateToFields();
		
		CriticPlugin.writePreferencesToStore(this.getProject(), this.preferences);
	}

	@Override
	protected Control createContents(Composite parent) {
		if(this.preferences == null)
			this.preferences = CriticPlugin.readPreferencesFromStore(this.getProject());
		
		Font font = parent.getFont();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(font);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace  = true;
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);
		
		this.createUnEditableProperties(composite);
		this.createEditableProperties(composite);
		this.createSootOptionProperties(composite);
		this.createExtensionPluginGroup(composite);
		
		return composite;
	}
	

	public IProject getProject() {
		IAdaptable elem = this.getElement();
		if(elem instanceof IProject)
			return (IProject)elem;
		else if(elem instanceof IJavaProject)
			return ((IJavaProject)elem).getProject();
		return null;
	}
	
	
	private void updateFromFields() {
		try {
			preferences.setDelay(Long.parseLong(this.delay.getText()));
			preferences.setLoopUnrollLimit(Integer.parseInt(this.loopUnrollLimit.getText()));
			preferences.setMethodDepth(Integer.parseInt(this.methodDepth.getText()));
			preferences.setPathCutOffSize(Integer.parseInt(this.pathCutOffSize.getText()));
			preferences.setPoolWaitTime(Long.parseLong(this.poolWaitTime.getText()));
			
			// Abstractions have already been added
		}
		catch(Exception e){
			CriticPlugin.log(Status.WARNING, "Could not store new preferences.", e);
		}
	}
	
	private void updateToFields() {
		this.delay.setText("" + preferences.getDelay());
		this.loopUnrollLimit.setText("" + preferences.getLoopUnrollLimit());
		this.methodDepth.setText("" + preferences.getMethodDepth());
		this.pathCutOffSize.setText("" + preferences.getPathCutOffSize());
		this.poolWaitTime.setText("" + preferences.getPoolWaitTime());
		table.removeAll();
		for(Pair option : preferences.getSootOptions()) {
			addTableItem(table, option.getKey(), option.getValue());
		}
	}
	
	
	private Composite createExtensionPluginGroup(final Composite parent) {
		Font font = parent.getFont();
		Group group = new Group(parent, SWT.BORDER);
		group.setText("Intalled Extension Plugins for CriticAL (UnCheck to disable the listed extensions).");
		group.setToolTipText("These are the plugins that are installed in IDE that extend CriticAL.");
		group.setFont(font);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = true;
		group.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace  = true;
		data.grabExcessVerticalSpace = true;
		group.setLayoutData(data);

		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CriticPlugin.EXTENSION_ID);
		IExtension[] extensions = point.getExtensions();
		if(extensions.length == 0) {
			String toolTip = "No extension has been added to CriticAL.";
			newLabel(group, "No extension has been added to CriticAL yet!", toolTip, true);
		}
		for(IExtension extension : extensions) {
			String id = extension.getUniqueIdentifier();
			String name = extension.getLabel();
			
			String toolTip = name + " extension point.";
			boolean enabled = !preferences.containsDisabledExtension(id);
			final Button button = newButton(group, name + " [" + id + "]", enabled, toolTip, SWT.CHECK, null, false);
			button.setData("id", id);
			button.setData("name", name);
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					String butId = (String)button.getData("id");
					
					if(button.getSelection()) {
						preferences.removeDisabledExtension(butId);
					}
					else {
						preferences.addDisabledExtension(butId);
					}
					
					// Whenever there is a change is list of available plugin
					// we reset the extension manager
					ExtensionManager.reset();
				}
			});
		}
		return group;		
	}
	
	private Composite createSootOptionProperties(final Composite parent) {
		Font font = parent.getFont();
		final Group group = new Group(parent, SWT.BORDER);
		group.setText("Soot Options");
		group.setToolTipText("Add options for Soot here. Visit http://www.sable.mcgill.ca/soot/tutorial/usage/ for detials.");
		group.setFont(font);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace  = true;
		data.grabExcessVerticalSpace = true;
		group.setLayoutData(data);
		
		FillLayout fillLayout = new FillLayout();
		Composite buttonGroup = new Composite(group, SWT.NONE);
		buttonGroup.setLayout(fillLayout);
		buttonGroup.setFont(font);
		
		String toolTip = "Press this button to add new option";
		this.addNewRule = newButton(buttonGroup, "Add Option", false, toolTip, SWT.PUSH, null, false);
		this.addNewRule.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newOption(parent);
			}
		});
		
		toolTip = "Press this button to delete selected option";
		this.deleteRule = newButton(buttonGroup, "Delete Option", false, toolTip, SWT.PUSH, null, false);
		this.deleteRule.setEnabled(false);
		this.deleteRule.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = table.getSelection();
				for(TableItem item : items) {
					String key = item.getText(0);
					String value = item.getText(1);
					preferences.getSootOptions().remove(new Pair(key, value));
					table.remove(table.indexOf(item));
				}
			}
		});
		
		this.table = this.createOptionTable(group);
		
		return group;
	}
	
	private Table createOptionTable(Composite parent) {
		Table table = new Table (parent, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setLinesVisible (true);
		table.setHeaderVisible(true);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace  = true;
		data.grabExcessVerticalSpace = true;
		table.setLayoutData(data);
		
		TableColumn column = new TableColumn(table, SWT.LEAD); // Key
		column.setWidth (200);
		column.setText("Key");
		column = new TableColumn(table, SWT.LEAD);	// Value
		column.setWidth (200);
		column.setText("Value");
		
		for (Pair option : this.preferences.getSootOptions()) {
			this.addTableItem(table, option.getKey(), option.getValue());
		}
		
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Table table = (Table)e.getSource();
				int index = table.getSelectionIndex();
				if(index >= 0) {
					deleteRule.setEnabled(true);
				}
				else {
					deleteRule.setEnabled(false);
				}
			}
		});
		
		return table;
	}
	
	private void addTableItem(Table table, String key, String value) {
		TableItem item = new TableItem (table, SWT.NONE);
		item.setText(0, key);
		item.setText(1, value);
	}
	
	private void newOption(Composite parent) {
		Font font = parent.getFont();
		final Shell dialog = new Shell (getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		dialog.setText("Soot Option Addition Dialog ");
		dialog.setFont(font);

		Rectangle dim = this.getShell().getBounds();
		int x = this.getShell().getLocation().x + dim.width / 3;
		int y = this.getShell().getLocation().y + dim.height / 3;
		dialog.setLocation(x, y);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		dialog.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace  = true;
		data.grabExcessVerticalSpace = true;
		dialog.setLayoutData(data);
		
		this.newLabel(dialog, "Enter the option name", "See http://www.sable.mcgill.ca/soot/tutorial/usage/ for detials", true);
		final Text optionName = this.newText(dialog, false, "", "See http://www.sable.mcgill.ca/soot/tutorial/usage/ for detials", true);
		optionName.addListener(SWT.Verify, new Listener () {
			public void handleEvent(Event event) {
				String string = event.text;
				if(string.contains(" "))
					event.doit = false;
			}
		});

		this.newLabel(dialog, "Enter the value (if any)", "Leave it blank if this option does not have a value", true);
		final Text optionValue = this.newText(dialog, false, "", "Leave it blank if this option does not have a value", true);
		
		final Button ok = this.newButton(dialog, "Done", false, "Press to store data", SWT.PUSH, null, true);
		ok.addSelectionListener (new SelectionAdapter () {
			public void widgetSelected (SelectionEvent e) {
				if(optionName.getText().trim().length() == 0) {
					e.doit = false;
					return;
				}
				if(preferences.addSootOption(optionName.getText(), optionValue.getText()))
						addTableItem(table, optionName.getText(), optionValue.getText());
				dialog.close ();
			}
		});

		final Button cancel = this.newButton(dialog, "Cancel", false, "Press to cancel", SWT.PUSH, null, true);
		cancel.addSelectionListener (new SelectionAdapter () {
			public void widgetSelected (SelectionEvent e) {
				dialog.close ();
			}
		});
		
		dialog.setTabList(new Control[] {optionName, optionValue});
		dialog.setDefaultButton(ok);
		dialog.pack();
		dialog.open();
	}
	
	private Composite createUnEditableProperties(Composite parent) {
		Font font = parent.getFont();
		Group group = new Group(parent, SWT.BORDER);
		group.setText("Settings from Virtual Machine (Edit in eclipse.ini)");
		group.setToolTipText("Please change these settings from eclipse.ini file");
		group.setFont(font);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		group.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace  = true;
		data.grabExcessVerticalSpace = true;
		group.setLayoutData(data);
		
		
		String toolTip = "This field reflects the starting memory allocated through VM's -xms parameter in eclipse.ini file";
		newLabel(group, "Starting total memory (MB)", toolTip, true);
		long temp = preferences.getAvailableMemorySize() / (long)(1024 * 1024);
		newText(group, true, temp + "", toolTip, true).setEditable(false);		

		toolTip = "This field reflects the maximum memory allocated through VM's -xmx parameter in eclipse.ini file";
		newLabel(group, "Maximum available memory (MB)", toolTip, true);
		temp = preferences.getMaxMemorySize() / (long)(1024 * 1024);
		newText(group, true, temp + "", toolTip, true).setEditable(false);
		
		toolTip = "Total initial free memory available for the PSF to use";
		newLabel(group, "Initial free memory (MB)", toolTip, true);
		temp = preferences.getAvailableMemorySize() / (long)(1024 * 1024);
		newText(group, true, temp + "", toolTip, true).setEditable(false);		
		
		toolTip = "The number of available processors to use for parallel processing during analysis";
		newLabel(group, "Available processors", toolTip, true);
		temp = preferences.getThreadPoolSize();
		newText(group, true, temp + "", toolTip, true).setEditable(false);
		
		return group;
	}
	
	private Composite createEditableProperties(Composite parent) {
		Font font = parent.getFont();
		Group group = new Group(parent, SWT.BORDER);
		group.setText("Serl's CriticAL settings");
		group.setToolTipText("You can edit these settings with your own");
		group.setFont(font);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		group.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace  = true;
		data.grabExcessVerticalSpace = true;
		group.setLayoutData(data);
		
		
		String toolTip = "This is the depth of method that should be expanded during analysis. Default is 3, a() -> b() -> c()";
		newLabel(group, "Call stack depth for expansion", toolTip, true);
		this.methodDepth = newText(group, true, preferences.getMethodDepth() + "", toolTip, true);
		
		toolTip = "This is the threshold that will be used to stop a loop from unrolling infinitely. Default is 2, i.e. two iterations.";
		newLabel(group, "Loop unrolling limit", toolTip, true);
		this.loopUnrollLimit = newText(group, true, preferences.getLoopUnrollLimit() + "", toolTip, true);

		toolTip = "This is the threshold that will be used to stop further analysis of a class. " +
					"This is used to counter path explosion by giving up";
		newLabel(group, "Path cutt-off limit", toolTip, true);
		this.pathCutOffSize = newText(group, true, preferences.getPathCutOffSize() + "", toolTip, true);
		
		toolTip = "This determines the sleep time for a thread during analysis to facilitate fair context switching. Default is 100 ms";
		newLabel(group, "Thread sleep time (ms)", toolTip, true);
		this.delay = newText(group, true, preferences.getDelay() + "", toolTip, true);
		
		toolTip = "This will determine the sleep time for the thread pool to await termination in secs. Default is 300 secs or 5 mins.";
		newLabel(group, "Thread pool sleep time (secs)", toolTip, true);
		this.poolWaitTime = newText(group, true, preferences.getPoolWaitTime() + "", toolTip, true);
		
		return group;
	}
	
	private Label newLabel(Composite parent, String text, String toolTip, boolean grid) {
		Font font = parent.getFont();
		Label label = new Label(parent, SWT.NONE);
		label.setText(text);
		label.setFont(font);
		label.setToolTipText(toolTip);
		
		if(grid) {
			GridData data = new GridData();
			data.verticalAlignment = GridData.FILL;
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace  = true;
			data.grabExcessVerticalSpace = true;
			label.setLayoutData(data);
		}
		return label;
	}
	
	private Button newButton(Composite parent, String text, boolean checked, String toolTip, int constant, Listener listener, boolean grid) {
		Font font = parent.getFont();
		Button button = new Button(parent, constant);
		button.setText(text);
		button.setSelection(checked);
		button.setToolTipText(toolTip);
		button.setFont(font);
		if(listener != null)
			button.addListener(SWT.Selection, listener);

		if(grid) {
			GridData data = new GridData();
			data.verticalAlignment = GridData.FILL;
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace  = true;
			data.grabExcessVerticalSpace = true;
			button.setLayoutData(data);
		}
		
		button.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				switch (e.detail) {
				/* Do tab group traversal */
				case SWT.TRAVERSE_ESCAPE:
				case SWT.TRAVERSE_RETURN:
				case SWT.TRAVERSE_TAB_NEXT:	
				case SWT.TRAVERSE_TAB_PREVIOUS:
				case SWT.TRAVERSE_PAGE_NEXT:	
				case SWT.TRAVERSE_PAGE_PREVIOUS:
					e.doit = true;
					break;
				}
			}
		});
		
		return button;
	}
	
	@SuppressWarnings("unused")
	private Combo newCombo(Composite parent, String[] contents, String toolTip, boolean grid) {
		Font font = parent.getFont();
		Combo combo = new Combo(parent, SWT.BORDER | SWT.WRAP);
		combo.setItems(contents);
		combo.setFont(font);
		combo.setToolTipText(toolTip);
		combo.select(0);

		if(grid) {
			GridData data = new GridData();
			data.verticalAlignment = GridData.FILL;
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace  = true;
			data.grabExcessVerticalSpace = true;
			combo.setLayoutData(data);
		}
		
		return combo;
	}
	
	private Text newText(Composite parent, boolean number, String txt, String toolTip, boolean grid) {
		Font font = parent.getFont();
		Text text = new Text(parent, SWT.BORDER | SWT.WRAP);
		if(number) {
			text.addListener (SWT.Verify, new Listener () {
				public void handleEvent (Event e) {
					String string = e.text;
					char [] chars = new char [string.length ()];
					string.getChars (0, chars.length, chars, 0);
					for (int i=0; i<chars.length; i++) {
						if (!('0' <= chars [i] && chars [i] <= '9') && chars[i] != '.') {
							e.doit = false;
							return;
						}
					}
				}
			});
		}
		if(txt != null)
			text.setText(txt);
		text.setToolTipText(toolTip);
		text.setFont(font);

		if(grid) {
			GridData data = new GridData();
			data.verticalAlignment = GridData.FILL;
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace  = true;
			data.grabExcessVerticalSpace = true;
			text.setLayoutData(data);
		}
		
		text.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				switch (e.detail) {
				/* Do tab group traversal */
				case SWT.TRAVERSE_ESCAPE:
				case SWT.TRAVERSE_RETURN:
				case SWT.TRAVERSE_TAB_NEXT:	
				case SWT.TRAVERSE_TAB_PREVIOUS:
				case SWT.TRAVERSE_PAGE_NEXT:	
				case SWT.TRAVERSE_PAGE_PREVIOUS:
					e.doit = true;
					break;
				}
			}
		});
		return text;
	}

}