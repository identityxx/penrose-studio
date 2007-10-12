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
package org.safehaus.penrose.studio.mapping;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.safehaus.penrose.studio.views.CodeAssist;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.mapping.Expression;
import org.safehaus.penrose.directory.AttributeMapping;
import org.safehaus.penrose.directory.FieldMapping;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.io.*;

/**
 * @author Endi S. Dewata
 */
public class ExpressionDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    public final static int TEXT       = 0;
    public final static int BINARY     = 1;
    public final static int VARIABLE   = 2;
    public final static int EXPRESSION = 3;

    Shell shell;
    Font font;

    Composite blankPanel;
    Composite currentPanel;
    int currentType;

    //Text nameText;

    Button binaryRadio;
    Text binaryText;

    Button textRadio;
    Text textText;

    Button variableRadio;
    Combo variableCombo;

    Button expressionRadio;
	Text scriptText;
    Combo foreachCombo;
    Text varText;

    private AttributeMapping attributeMapping;
    private FieldMapping fieldMapping;

    public Collection variables = new ArrayList();

    private int action = CANCEL;

	public ExpressionDialog(Shell parent, int style) {
		super(parent, style);

        font = new Font(parent.getDisplay(), "Courier New", 8, SWT.NONE);

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        shell.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent event) {
                font.dispose();
            }
        });

        createControl(shell);
    }

    public void open() {

        Point size = new Point(600, 400);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenroseStudioPlugin.getImage(PenroseImage.LOGO16));
        shell.open();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void createControl(final Shell parent) {
        parent.setLayout(new GridLayout(1, false));
/*
        Composite names = new Composite(parent, SWT.NONE);
        names.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        names.setLayout(new GridLayout(2, false));

        Label nameLabel = new Label(names, SWT.NONE);
        nameLabel.setText("Name:");
        GridData gd = new GridData();
        gd.widthHint = 80;
        nameLabel.setLayoutData(gd);

        nameText = new Text(names, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        nameText.setLayoutData(gd);
*/
        Composite types = new Composite(parent, SWT.NONE);
        types.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        types.setLayout(new GridLayout(2, false));

        Label typeLabel = new Label(types, SWT.NONE);
        typeLabel.setText("Type:");
        GridData gd = new GridData();
        gd.widthHint = 80;
        typeLabel.setLayoutData(gd);

        Composite typeButtons = new Composite(types, SWT.NONE);
        typeButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        typeButtons.setLayout(new RowLayout());

        textRadio = new Button(typeButtons, SWT.RADIO);
        textRadio.setText("text");
        textRadio.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setType(TEXT);
            }
        });

        binaryRadio = new Button(typeButtons, SWT.RADIO);
        binaryRadio.setText("binary");
        binaryRadio.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setType(BINARY);
            }
        });

        variableRadio = new Button(typeButtons, SWT.RADIO);
        variableRadio.setText("variable");
        variableRadio.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setType(VARIABLE);
            }
        });

        expressionRadio = new Button(typeButtons, SWT.RADIO);
        expressionRadio.setText("expression");
        expressionRadio.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setType(EXPRESSION);
            }
        });

        Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        blankPanel = new Composite(parent, SWT.NONE);
        blankPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
        blankPanel.setLayout(new FillLayout());

        Composite controlButtons = createControlPanel(parent);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
		controlButtons.setLayoutData(gd);

        //setType(TEXT);
	}

    public Composite createControlPanel(Composite parent) {

        Composite buttons = new Composite(parent, SWT.NONE);
        buttons.setLayout(new RowLayout());

        Button saveButton = new Button(buttons, SWT.PUSH);
        saveButton.setText("Save");

        saveButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Object constant = null;
                String variable = null;
                Expression expression = null;

                if (TEXT == currentType) {
                    constant = getConstant();

                } else if (BINARY == currentType) {
                    constant = getBinary();

                } else if (VARIABLE == currentType) {
                    variable = getVariable();

                } else if (EXPRESSION == currentType) {
                    expression = new Expression();
                    expression.setScript(getScript());
                    expression.setForeach(getForeach());
                    expression.setVar(getVar());
                }

                if (attributeMapping != null) {
                    //attributeMapping.setName(getName());
                    attributeMapping.setConstant(constant);
                    attributeMapping.setVariable(variable);
                    attributeMapping.setExpression(expression);
                }

                if (fieldMapping != null) {
                    //fieldMapping.setName(getName());
                    fieldMapping.setConstant(constant);
                    fieldMapping.setVariable(variable);
                    fieldMapping.setExpression(expression);
                }

                action = OK;
                shell.close();
            }
        });

        Button cancelButton = new Button(buttons, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            }
        });

        return buttons;
    }

    public Composite createTextPanel(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label label = new Label(composite, SWT.NONE);
        label.setText("Text:");
        GridData gd = new GridData();
        gd.widthHint = 80;
        label.setLayoutData(gd);

        textText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        gd = new GridData(GridData.FILL_BOTH);
        gd.verticalSpan = 2;
        textText.setLayoutData(gd);

        if (attributeMapping != null) {
            Object value = attributeMapping.getConstant();
            if (value != null && value instanceof String) {
                setConstant((String)value);
            }
        }

        if (fieldMapping != null) {
            Object value = fieldMapping.getConstant();
            if (value != null && value instanceof String) {
                setConstant((String)value);
            }
        }

        textText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
            }
        });

        textText.setFocus();

        return composite;
    }

    public Composite createBinaryPanel(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label label = new Label(composite, SWT.NONE);
        label.setText("Binary:");
        GridData gd = new GridData();
        gd.widthHint = 80;
        label.setLayoutData(gd);

        binaryText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
        binaryText.setFont(font);
        gd = new GridData(GridData.FILL_BOTH);
        gd.verticalSpan = 2;
        binaryText.setLayoutData(gd);

        new Label(composite, SWT.NONE);

        new Label(composite, SWT.NONE);

        Composite actions = new Composite(composite, SWT.NONE);
        actions.setLayout(new RowLayout());
        actions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Hyperlink importData = new Hyperlink(actions, SWT.NONE);
        importData.setText("Import");
        importData.setUnderlined(true);

        importData.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {

                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setText("Import Binary Data");

                String filename = dialog.open();
                if (filename == null) return;

                try {
                    File file = new File(filename);
                    byte bytes[] = new byte[(int)file.length()];

                    FileInputStream out = new FileInputStream(file);
                    out.read(bytes);
                    out.close();

                    setBinary(bytes);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        Hyperlink exportData = new Hyperlink(actions, SWT.NONE);
        exportData.setText("Export");
        exportData.setUnderlined(true);

        exportData.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {

                FileDialog dialog = new FileDialog(shell, SWT.SAVE);
                dialog.setText("Export Binary Data");

                String filename = dialog.open();
                if (filename == null) return;

                try {
                    FileOutputStream out = new FileOutputStream(filename);
                    out.write(getBinary());
                    out.close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        if (attributeMapping != null) {
            Object value = attributeMapping.getConstant();
            if (value != null && value instanceof byte[]) {
                setBinary((byte[])value);
            }
        }

        if (fieldMapping != null) {
            Object value = fieldMapping.getConstant();
            if (value != null && value instanceof byte[]) {
                setBinary((byte[])value);
            }
        }

        return composite;
    }

    public Composite createVariablePanel(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label label = new Label(composite, SWT.NONE);
        label.setText("Variable:");
        GridData gd = new GridData();
        gd.widthHint = 80;
        label.setLayoutData(gd);

        variableCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        variableCombo.add("");
        for (Iterator i=variables.iterator(); i.hasNext(); ) {
            String variable = (String)i.next();
            variableCombo.add(variable);
        }
        variableCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        if (attributeMapping != null) {
            String variable = attributeMapping.getVariable();
            if (variable != null) {
                setVariable(variable);
            }
        }

        if (fieldMapping != null) {
            String variable = fieldMapping.getVariable();
            if (variable != null) {
                setVariable(variable);
            }
        }

        variableCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            }
        });

        variableCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
            }
        });

        variableCombo.setFocus();

        return composite;
    }

    public Composite createExpressionPanel(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label expressionLabel = new Label(composite, SWT.NONE);
        expressionLabel.setText("Expression:");
        GridData gd = new GridData();
        gd.widthHint = 80;
        expressionLabel.setLayoutData(gd);

        scriptText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        gd = new GridData(GridData.FILL_BOTH);
        gd.verticalSpan = 2;
		scriptText.setLayoutData(gd);

        new Label(composite, SWT.NONE);

        Label foreachLabel = new Label(composite, SWT.NONE);
        foreachLabel.setText("Repeat for each:");
        foreachLabel.setLayoutData(new GridData());

        foreachCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        foreachCombo.add("");
        for (Iterator i=variables.iterator(); i.hasNext(); ) {
            String variable = (String)i.next();
            foreachCombo.add(variable);
        }
        foreachCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label varLabel = new Label(composite, SWT.NONE);
        varLabel.setText("Variable name:");
        varLabel.setLayoutData(new GridData());

        varText = new Text(composite, SWT.BORDER);
        varText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        if (attributeMapping != null) {
            Expression expression = attributeMapping.getExpression();
            if (expression != null) {
                setForeach(expression.getForeach());
                setVar(expression.getVar());
                setScript(expression.getScript());
            }
        }

        if (fieldMapping != null) {
            Expression expression = fieldMapping.getExpression();
            if (expression != null) {
                setForeach(expression.getForeach());
                setVar(expression.getVar());
                setScript(expression.getScript());
            }
        }

        scriptText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
            }
        });

		scriptText.addKeyListener(new KeyListener() {
            boolean ctrlPressed;
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
					case SWT.CTRL:
						ctrlPressed = true;
						break;
					case ' ':
						if (ctrlPressed) {
							e.doit = false;
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

        scriptText.setFocus();

        return composite;
    }

    public void setCurrentPanel(Composite panel) {
        if (currentPanel != null) currentPanel.dispose();
        currentPanel = panel;
        blankPanel.layout();
    }

    public void setType(int type) {
        currentType = type;

        boolean isText = type == TEXT;
        textRadio.setSelection(isText);
        if (isText) {
            setCurrentPanel(createTextPanel(blankPanel));
        }

        boolean isBinary = type == BINARY;
        binaryRadio.setSelection(isBinary);
        if (isBinary) {
            setCurrentPanel(createBinaryPanel(blankPanel));
        }

        boolean isVariable = type == VARIABLE;
        variableRadio.setSelection(isVariable);
        if (isVariable) {
            setCurrentPanel(createVariablePanel(blankPanel));
        }

        boolean isExpression = type == EXPRESSION;
        expressionRadio.setSelection(isExpression);
        if (isExpression) {
            setCurrentPanel(createExpressionPanel(blankPanel));
        }
    }

    void showHints() {
        CodeAssist codeAssist = new CodeAssist(getParent(), scriptText);
        codeAssist.open();
    }

    public void addVariable(String variable) {
        variables.add(variable);
    }

    public void setScript(String script) {
        scriptText.setText(script == null ? "" : script);
    }

    public String getScript() {
        return "".equals(scriptText.getText()) ? null : scriptText.getText();
    }

    public String getForeach() {
        return "".equals(foreachCombo.getText()) ? null : foreachCombo.getText();
    }

    public void setForeach(String foreach) {
        foreachCombo.setText(foreach == null ? "" : foreach);
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public AttributeMapping getAttributeMapping() {
        return attributeMapping;
    }

    public void setConstant(String constant) {
        textText.setText(constant == null ? "" : constant);
    }

    public String getConstant() {
        return "".equals(textText.getText()) ? null : textText.getText();
    }

    public void setBinary(byte[] bytes) {

        //log.debug("Writing "+bytes.length+" bytes.");

        StringBuilder sb = new StringBuilder();

        int lines = bytes.length / 16 + 1;
        int pos = 0;

        for (int i=0; i<lines && pos < bytes.length; i++) {

            String index = Integer.toHexString(pos);
            for (int j=0; j<8-index.length(); j++) sb.append("0");
            sb.append(index);
            sb.append(": ");

            StringBuilder text = new StringBuilder();

            for (int j=0; j<16; j++) {

                if (pos < bytes.length) {
                    int b = bytes[pos++] & 0xff;
                    String hex = Integer.toHexString(b).toUpperCase();

                    if (hex.length() == 1) sb.append("0");
                    sb.append(hex);

                    if (b >= '!' && b <= '~') {
                        text.append((char)b);
                    } else {
                        text.append(".");
                    }

                } else {
                    sb.append("  ");
                }

                if (j % 2 == 1) sb.append(" ");
            }

            sb.append(text);
            sb.append("\n");
        }

        binaryText.setText(sb.toString());
    }

    public byte[] getBinary() {
        byte[] bytes = null;
        try {
            BufferedReader in = new BufferedReader(new StringReader(binaryText.getText()));
            Collection c = new ArrayList();

            String line;
            while ((line = in.readLine()) != null) {
                String index = line.substring(0, 10);
                String values = line.substring(10, 50);
                String text = line.substring(50);

                for (int i=0; i<16; i++) {
                    int pos = (i/2) * 5 + (i%2) * 2;
                    String h = values.substring(pos, pos+2).trim();
                    if ("".equals(h)) break;

                    byte b = Byte.parseByte(h, 16);
                    c.add(new Byte(b));
                    //log.debug(" - "+h+" -> "+b);
                }
            }

            bytes = new byte[c.size()];
            int pos = 0;
            for (Iterator i=c.iterator(); i.hasNext(); ) {
                Byte b = (Byte)i.next();
                bytes[pos++] = b.byteValue();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        //log.debug("Reading "+bytes.length+" bytes.");

        return bytes;
    }

    public void setVariable(String variable) {
        variableCombo.setText(variable == null ? "" : variable);
    }

    public String getVariable() {
        return "".equals(variableCombo.getText()) ? null : variableCombo.getText();
    }
/*
    public void setName(String name) {
        nameText.setText(name == null ? "" : name);
    }

    public String getName() {
        return "".equals(nameText.getText()) ? null : nameText.getText();
    }
*/
    public void setAttributeMapping(AttributeMapping attributeMapping) {
        this.attributeMapping = attributeMapping;

        //setName(attributeMapping.getName());

        Object constant = attributeMapping.getConstant();
        if (constant != null) {
            if (constant instanceof byte[]) {
                setType(BINARY);
                setBinary((byte[])constant);
            } else {
                setType(TEXT);
                setConstant((String)constant);
            }
            return;
        }

        String variable = attributeMapping.getVariable();
        if (variable != null) {
            setType(VARIABLE);
            setVariable(variable);
            return;
        }

        Expression expression = attributeMapping.getExpression();
        if (expression != null) {
            setType(EXPRESSION);
            setForeach(expression.getForeach());
            setVar(expression.getVar());
            setScript(expression.getScript());
            return;
        }

        setType(TEXT);
    }

    public void setFieldMapping(FieldMapping fieldMapping) {
        this.fieldMapping = fieldMapping;

        //setName(fieldMapping.getName());

        Object constant = fieldMapping.getConstant();
        if (constant != null) {
            if (constant instanceof byte[]) {
                setType(BINARY);
                setBinary((byte[])constant);
            } else {
                setType(TEXT);
                setConstant((String)constant);
            }
            return;
        }

        String variable = fieldMapping.getVariable();
        if (variable != null) {
            setType(VARIABLE);
            setVariable(variable);
            return;
        }

        Expression expression = fieldMapping.getExpression();
        if (expression != null) {
            setType(EXPRESSION);
            setForeach(expression.getForeach());
            setVar(expression.getVar());
            setScript(expression.getScript());
            return;
        }

        setType(TEXT);
    }

    public FieldMapping getFieldMapping() {
        return fieldMapping;
    }

    public interface NameChecker {
		public String checkName(String name);
	}

    public String getVar() {
        return "".equals(varText.getText()) ? null : varText.getText();
    }

    public void setVar(String var) {
        varText.setText(var == null ? "" : var);
    }
}
