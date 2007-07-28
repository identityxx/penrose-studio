/**
 * Copyright (c) 2000-2006, Identyx Corporation.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.safehaus.penrose.studio.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.safehaus.penrose.mapping.*;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.views.BaseDialog;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.util.Pair;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.FieldConfig;
import org.apache.log4j.Logger;

public class ExpressionEditorDialog extends BaseDialog {

    Logger log = Logger.getLogger(getClass());

	Object obj;
	int type = EXPRESSION;
	Text name;
	Text expression;
	
	Button saveButton;
	
	public final static int NAME_VALUE = 1;
	public final static int EXPRESSION = 2;
	
	NameChecker nameChecker;
	
	Shell codeAssistShell;
	
	boolean ctrlPressed;

    private Partition partition;
	private EntryMapping entry;
	
	public ExpressionEditorDialog(Shell parent, Object obj, String shellTitle, String formTitle) {
		super(parent);
		this.obj = obj;
		//
		if (obj instanceof FieldMapping) {
			type = EXPRESSION;
		} else if (obj instanceof AttributeMapping) {
			type = EXPRESSION;
		} else if (obj instanceof Pair) {
			type = NAME_VALUE;
		}
	    // Layout
		TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);
		// Title
		shell.setText(shellTitle);
		form.setText(formTitle);
		// Sections
		createExpressionSection(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
		createFootSection(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
	    // Prepopulate
	    if (obj != null) {
	    	load(obj);
	    }
	}

	public void load(Object obj) {
		if (obj == null) return;

		if (obj instanceof FieldMapping) {
			FieldMapping field = (FieldMapping) obj;
			if (field.getExpression() != null) expression.setText(field.getExpression().getScript());

		} else if (obj instanceof AttributeMapping) {
			AttributeMapping attr = (AttributeMapping) obj;
			if (attr.getExpression() != null) expression.setText(attr.getExpression().getScript());

		} else if (obj instanceof Pair) {
			Pair pair = (Pair) obj;
			if (pair.getName() != null) name.setText(pair.getName());
			if (pair.getValue() != null) expression.setText(pair.getValue());
		}
	}
	
	public void store(Object obj) {

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        penroseApplication.setDirty(true);

		if (obj instanceof FieldMapping) {
            String s = expression.getText().trim();
            if (s.equals("")) s = null;

			FieldMapping field = (FieldMapping) obj;
			Expression exp = new Expression();
			exp.setScript(s);
			field.setExpression(exp);

		} else if (obj instanceof AttributeMapping) {
			AttributeMapping attr = (AttributeMapping) obj;
			attr.getExpression().setScript(expression.getText());

		} else if (obj instanceof Pair) {
			Pair pair = (Pair) obj;
			pair.setName(name.getText());
			pair.setValue(expression.getText());
		}
	}

	/**
	 * "Modules" Section
	 */
	public Section createExpressionSection(TableWrapData layoutData) {
		Section section = toolkit.createSection(form.getBody(), 
				Section.DESCRIPTION | Section.EXPANDED);
		section.setLayoutData(layoutData);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		if (type == NAME_VALUE) {
			section.setDescription("Edit the value below.");
		} else if (type == EXPRESSION) {
			section.setDescription("Edit the value or BeanShell (Java-like) expression below.");
		} else {
			section.setDescription("");
		}
		// Section Client
		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.FILL));
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 2;
		sectionClient.setLayout(sectionLayout);
		// Name:
		if (type == NAME_VALUE) {
			toolkit.createLabel(sectionClient, "Name:");
			name = toolkit.createText(sectionClient, "", SWT.BORDER);
			name.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB, 1, 1));
			name.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (nameChecker == null) return;
					String errorMessage = nameChecker.checkName(name.getText());
					if (errorMessage != null) {
						name.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_RED));
						name.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
						name.setToolTipText(errorMessage);
						saveButton.setEnabled(false);
					} else {
						name.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
						name.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
						name.setToolTipText(errorMessage);
						saveButton.setEnabled(true);
					}
				}
			});
		}
		// Value/Expression:
		if (type == NAME_VALUE) {
			Label label = toolkit.createLabel(sectionClient, "Value:");
			label.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB, 1, 2));
		} else if (type == EXPRESSION) {
			Label label = toolkit.createLabel(sectionClient, "Value or Expression: (use CTRL-Space for code assist/completion)");
			label.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB, 1, 2));
		}
		// (text)
		TableWrapData twd = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB, 1, 2);
		twd.heightHint = 300;
		expression = toolkit.createText(sectionClient, "", SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		expression.setLayoutData(twd);
		expression.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
					case SWT.CTRL:
						ctrlPressed = true;
						break;
					case ' ':
						if (ctrlPressed) {
							e.doit = false;
							CodeAssist codeAssist = new CodeAssist();
							codeAssist.open();
						}
				}
			}
			public void keyReleased(KeyEvent e) {
				switch (e.keyCode) {
					case SWT.CTRL:
						ctrlPressed = false;
						break;
				}
			}
		});
		// end
		section.setClient(sectionClient);
		return section;
	}
	
	/**
	 * "Foot" Section
	 */
	public Section createFootSection(TableWrapData layoutData) {
		Section section = toolkit.createSection(form.getBody(), Section.EXPANDED);
		section.setLayoutData(layoutData);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		Composite sectionClient = toolkit.createComposite(section);
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 2;
		sectionClient.setLayout(sectionLayout);
		// [Save]
		saveButton = toolkit.createButton(sectionClient, "Save", SWT.PUSH);
		saveButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
                try {
                    store(obj);
                    shell.close();
                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                }
			}
		});
		// [Cancel]
		Button cancelButton = toolkit.createButton(sectionClient, "Cancel", SWT.PUSH);
		cancelButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		// end
		section.setClient(sectionClient);
		return section;
	}
	
	public void setNameChecker(NameChecker nameChecker) {
		this.nameChecker = nameChecker;
	}
	
	public Object getObject() {
		return obj;
	}

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public interface NameChecker {
		public String checkName(String name);
	}

	class CodeAssist {
		Table myTable;
		ArrayList hints;
		
		public CodeAssist() {
			Point location = expression.getCaretLocation();
			for (Control c=expression; c != null; c=c.getParent()) {
				location = add(location, c.getLocation());
				if (c instanceof Shell) break;
			}
			if (codeAssistShell == null || codeAssistShell.isDisposed()) {
				codeAssistShell = new Shell(shell, SWT.RESIZE);
				codeAssistShell.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
				codeAssistShell.setLayout(new FillLayout());
				codeAssistShell.addShellListener(new ShellAdapter() {
					public void shellDeactivated(ShellEvent e) {
						codeAssistShell.dispose();
					}
				});
				myTable = new Table(codeAssistShell, SWT.H_SCROLL | SWT.V_SCROLL);
				myTable.addKeyListener(new KeyListener() {
					public void keyPressed(KeyEvent e) {
						switch (e.keyCode) {
							case SWT.CTRL:
								ctrlPressed = true;
								break;
						}
					}

                    public void keyReleased(KeyEvent e) {
                        switch (e.keyCode) {
                            case SWT.CR:
                                // add selected item to expression at caret location
                                if (myTable.getSelectionCount() > 0) {
                                    String s = myTable.getSelection()[0].getText();
                                    expression.insert(s);
                                    codeAssistShell.dispose();
                                }
                                break;
                            case SWT.CTRL:
                                ctrlPressed = false;
                                break;
                        }
                    }
				});
			}
			location = add(location, new Point(10, 42));
			codeAssistShell.setSize(200, 200);
			codeAssistShell.setLocation(location);
			codeAssistShell.open();
			populateTable();
		}
		
		void populateTable() {
			// populate code assist table
			myTable.removeAll();
			populateHints();
		}
		
		void populateHints() {
			if (entry == null) return;
			
			// add all sources
			Collection sources = entry.getSourceMappings();
			for (Iterator i=sources.iterator(); i.hasNext(); ) {
				SourceMapping source = (SourceMapping)i.next();

                PartitionConfig partitionConfig = partition.getPartitionConfig();
				SourceConfig sourceConfig = partitionConfig.getSourceConfigs().getSourceConfig(source.getSourceName());

				Object[] fields = sourceConfig.getFieldConfigs().toArray();
				Image icon = PenrosePlugin.getImage(PenroseImage.SOURCE);

				for (int j=0; j<fields.length; j++) {
					FieldConfig field = (FieldConfig) fields[j];
					addHint(icon, source.getName()+"."+field.getName());
				}
			}
			// add all attributes
			Object[] attributes = entry.getAttributeMappings().toArray();
			for (int i=0; i<attributes.length; i++) {
				AttributeMapping attribute = (AttributeMapping) attributes[i];
				addHint(PenrosePlugin.getImage(PenroseImage.NOKEY), attribute.getName());
			}
		}
		
		public void open() {
			codeAssistShell.open();
		}
		
		public void addHint(Image icon, String text) {
			TableItem item = new TableItem(myTable, SWT.NONE);
			item.setText(text);
			item.setImage(icon);
		}
	}
	
	Point add(Point a, Point b) {
		return new Point(a.x + b.x, a.y + b.y);
	}
	
	public EntryMapping getEntry() {
		return entry;
	}
	public void setEntry(EntryMapping entry) {
		this.entry = entry;
	}
}
