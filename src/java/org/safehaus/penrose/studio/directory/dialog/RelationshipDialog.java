/**
 * Copyright 2009 Red Hat, Inc.
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
package org.safehaus.penrose.studio.directory.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.mapping.*;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.apache.log4j.Logger;

public class RelationshipDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

	Button expressionButton;
	Button simpleJoinButton;
	Text expressionText;
	
	Table leftTable;
    Combo operatorCombo;
    Table rightTable;

    private int action;
    Relationship relationship;

	String[] comparisons = new String[] { "=" }; //, ">", ">=", "<", "<=" };
	
	public RelationshipDialog(Shell parent, int style) {
        super(parent, style);

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl(shell);
    }

    public void open () {

        Point size = new Point(400, 300);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenroseStudio.getImage(PenroseImage.LOGO));
        shell.open();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void createControl(final Shell parent) {
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        parent.setLayout(layout);

/*
		// (x) Expression
		expressionButton = new Button(sectionClient, SWT.RADIO);
		expressionButton.setText("Expression");

		expressionButton.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB, 1, 3));
		expressionButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectionChanged();
			}
		});

        // (expression text)
		expressionText = new Text(parent, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
		expressionText.setLayoutData(gd);
        expressionText.setEditable(false);
        expressionText.setEnabled(false);

		expressionText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				guessSimpleExpression(false);
			}
		});

		// (x) Simple Join
		simpleJoinButton = new Button(sectionClient, SWT.RADIO);
		simpleJoinButton.setText("Simple Join");

		simpleJoinButton.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB, 1, 3));
		simpleJoinButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectionChanged();
			}
		});
*/

		leftTable = new Table(parent, SWT.BORDER | SWT.READ_ONLY);
		leftTable.setLayoutData(new GridData(GridData.FILL_BOTH));
/*
		leftTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateSimpleJoin();
			}
		});
*/

		operatorCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		operatorCombo.setLayoutData(new GridData(GridData.FILL));

        for (String comparison : comparisons) {
            operatorCombo.add(comparison);
        }
		operatorCombo.select(0);
/*
		operatorCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateSimpleJoin();
			}
		});
*/

		rightTable = new Table(parent, SWT.BORDER | SWT.READ_ONLY);
		rightTable.setLayoutData(new GridData(GridData.FILL_BOTH));
/*
		rightTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateSimpleJoin();
			}
		});
*/
        Composite buttons = new Composite(parent, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        gd.horizontalAlignment = GridData.END;
        buttons.setLayoutData(gd);
        buttons.setLayout(new RowLayout());

		Button saveButton = new Button(buttons, SWT.PUSH);
        saveButton.setText("Save");
		saveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
                try {
                    if (leftTable.getSelectionCount() == 0) return;
                    if (rightTable.getSelectionCount() == 0) return;

                    TableItem leftItem = leftTable.getSelection()[0];
                    TableItem rightItem = rightTable.getSelection()[0];

                    String expression = leftItem.getText()+" "+operatorCombo.getText()+" "+rightItem.getText();
                    System.out.println("Expression: "+expression);

                    relationship.setLhs(leftItem.getText());
                    relationship.setRhs(rightItem.getText());
                    System.out.println("Relationship: "+relationship.getExpression());

                    action = OK;

                    shell.close();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
			}
		});

		Button cancelButton = new Button(buttons, SWT.PUSH);
        cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
                shell.close();
			}
		});
	}
	
	public void selectionChanged() {
        /*
		if (expressionButton.getSelection()) {
			expression.setEditable(true);
			expression.setEnabled(true);
			leftTable.setEnabled(false);
			rightTable.setEnabled(false);
			operatorCombo.setEnabled(false);
		} else if (simpleJoinButton.getSelection()) {
			expressionText.setEditable(false);
			expressionText.setEnabled(false);
			leftTable.setEnabled(true);
			rightTable.setEnabled(true);
			operatorCombo.setEnabled(true);
		//}
        */
	}
/*
	public void updateSimpleJoin() {
		try {
			String left = leftTable.getSelection()[0].getText();
			String right = rightTable.getSelection()[0].getText();
			String comp = operatorCombo.getText();
			if (left == null || left.equals("")) return;
			if (right == null || right.equals("")) return;
			if (comp == null || comp.equals("")) return;
			String exp = left + " " + comp + " " + right;
			expressionText.setText(exp);
		} catch (Exception ex) {
            System.out.println("Error: "+ex.getMessage());
		}
	}

	public void guessSimpleExpression(boolean initial) {
        System.out.println("[RelationshipDialog] guessSimpleExpression");
        if (expressionText.getText() == null || expressionText.getText().equals("")) return;

		try {
			for (int i=comparisons.length-1; i>=0; i--) {
				String comp = comparisons[i];
				int f1 = expressionText.getText().indexOf(comp);
				int f2 = -1;
				try {
					expressionText.getText().indexOf(comp, f1+1);
				} catch (Exception ex) {
				}
				if (f1 >= 0 && f2 < 0) {
					// exactly one occurence of comparison
					String l = expressionText.getText().substring(0, f1-1).trim();
					String r = expressionText.getText().substring(f1+comp.length()).trim();
					int lj = -1;
					int rj = -1;
					for (int j=0; j<leftTable.getItemCount(); j++) {
						TableItem item = leftTable.getItem(j);
						if (item.getText().equals(l)) {
							lj = j;
						}
					}
					for (int j=0; j<rightTable.getItemCount(); j++) {
						TableItem item = rightTable.getItem(j);
						if (item.getText().equals(r)) {
							rj = j;
						}
					}
					if (lj > -1 && rj > -1) {
						leftTable.select(lj);
						rightTable.select(rj);
						operatorCombo.select(i);
						if (initial) {
							//expressionButton.setSelection(false);
							//simpleJoinButton.setSelection(true);
						}
					} else {
						if (initial) {
							//expressionButton.setSelection(true);
							//simpleJoinButton.setSelection(false);
						}
					}
					selectionChanged();
					break;
				}
			}
		} catch (Exception ex) {
			// don't worry if we fail
            System.out.println("Error: "+ex.getMessage());
		}
	}
*/
    public void addField(String name, boolean primaryKey) {
        TableItem item = new TableItem(leftTable, SWT.NONE);
        item.setText(name);
        item.setImage(PenroseStudio.getImage(primaryKey ? PenroseImage.KEY : PenroseImage.NOKEY));

        item = new TableItem(rightTable, SWT.NONE);
        item.setText(name);
        item.setImage(PenroseStudio.getImage(primaryKey ? PenroseImage.KEY : PenroseImage.NOKEY));
    }

    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;

        String lhs = relationship.getLhs();
        String rhs = relationship.getRhs();

        TableItem items[] = leftTable.getItems();
        for (int i=0; i<items.length; i++) {
            if (items[i].getText().equals(lhs)) leftTable.select(i);
            if (items[i].getText().equals(rhs)) rightTable.select(i);
        }

        operatorCombo.setText(relationship.getOperator());
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
