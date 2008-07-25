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
package org.safehaus.penrose.studio.mapping.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.safehaus.penrose.mapping.Expression;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class FieldValueWizardPage extends WizardPage {

    public final static String NAME = "Field Value";

    Logger log = Logger.getLogger(getClass());

    public final static int TEXT       = 0;
    public final static int BINARY     = 1;
    public final static int VARIABLE   = 2;
    public final static int EXPRESSION = 3;

    Font font;

    Composite blankPanel;
    Composite currentPanel;

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

    int type = TEXT;

    Object constant;
    String variable;
    Expression expression;

    Collection<String> variables = new ArrayList<String>();

    public FieldValueWizardPage() {
        super(NAME);
    }

    public void createControl(final Composite parent) {

        font = new Font(parent.getDisplay(), "Courier New", 8, SWT.NONE);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        setControl(composite);

        Label typeLabel = new Label(composite, SWT.NONE);
        typeLabel.setText("Type:");
        GridData gd = new GridData();
        gd.widthHint = 80;
        typeLabel.setLayoutData(gd);

        Composite typeButtons = new Composite(composite, SWT.NONE);
        typeButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        typeButtons.setLayout(new RowLayout());

        textRadio = new Button(typeButtons, SWT.RADIO);
        textRadio.setText("text");
        textRadio.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                type = TEXT;
                refresh();
            }
        });

        binaryRadio = new Button(typeButtons, SWT.RADIO);
        binaryRadio.setText("binary");
        binaryRadio.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                type = BINARY;
                refresh();
            }
        });

        variableRadio = new Button(typeButtons, SWT.RADIO);
        variableRadio.setText("variable");
        variableRadio.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                type = VARIABLE;
                refresh();
            }
        });

        expressionRadio = new Button(typeButtons, SWT.RADIO);
        expressionRadio.setText("expression");
        expressionRadio.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                type = EXPRESSION;
                refresh();
            }
        });

        Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        separator.setLayoutData(gd);

        blankPanel = new Composite(composite, SWT.NONE);
        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        blankPanel.setLayoutData(gd);
        blankPanel.setLayout(new FillLayout());

        refresh();
    }

    public void dispose() {
         font.dispose();
    }

    public void setCurrentPanel(Composite panel) {
        if (currentPanel != null) currentPanel.dispose();
        currentPanel = panel;
        blankPanel.layout();
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) refresh();
    }

    public void refresh() {

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

    public Composite createTextPanel(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label label = new Label(composite, SWT.NONE);
        label.setText("Text:");
        GridData gd = new GridData();
        gd.widthHint = 80;
        label.setLayoutData(gd);

        textText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        textText.setText(constant == null ? "" : constant.toString());
        gd = new GridData(GridData.FILL_BOTH);
        gd.verticalSpan = 2;
        textText.setLayoutData(gd);

        return composite;
    }

    public Composite createBinaryPanel(final Composite parent) {

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

                FileDialog dialog = new FileDialog(parent.getShell(), SWT.OPEN);
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
                    ErrorDialog.open(e);
                }
            }
        });

        Hyperlink exportData = new Hyperlink(actions, SWT.NONE);
        exportData.setText("Export");
        exportData.setUnderlined(true);

        exportData.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {

                FileDialog dialog = new FileDialog(parent.getShell(), SWT.SAVE);
                dialog.setText("Export Binary Data");

                String filename = dialog.open();
                if (filename == null) return;

                try {
                    FileOutputStream out = new FileOutputStream(filename);
                    out.write(getBinary());
                    out.close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        if (constant instanceof byte[]) {

            //log.debug("Writing "+bytes.length+" bytes.");

            StringBuilder sb = new StringBuilder();

            byte[] bytes = (byte[])constant;

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

        variableCombo = new Combo(composite, SWT.BORDER);
        variableCombo.add("");
        for (String variable : variables) {
            variableCombo.add(variable);
        }
        variableCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        variableCombo.setText(variable == null ? "" : variable);

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
        for (String variable : variables) {
            foreachCombo.add(variable);
        }
        foreachCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label varLabel = new Label(composite, SWT.NONE);
        varLabel.setText("Variable name:");
        varLabel.setLayoutData(new GridData());

        varText = new Text(composite, SWT.BORDER);
        varText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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

        if (expression != null) {
            String foreach = expression.getForeach();
            foreachCombo.setText(foreach == null ? "" : foreach);

            String var = expression.getVar();
            varText.setText(var == null ? "" : var);

            String script = expression.getScript();
            scriptText.setText(script == null ? "" : script);
        }

        return composite;
    }

    public void addVariable(String variable) {
        variables.add(variable);
    }

    public void setConstant(Object constant) {
        if (constant instanceof byte[]) {
            setBinary((byte[])constant);
        } else {
            setText((String)constant);
        }
    }

    public int getType() {
        return type;
    }

    public void setText(String text) {
        this.constant = text;
        type = TEXT;
    }

    public String getText() {
        return "".equals(textText.getText()) ? null : textText.getText();
    }

    public void setBinary(byte[] bytes) {
        this.constant = bytes;
        type = BINARY;
    }

    public byte[] getBinary() {
        byte[] bytes = null;
        try {
            BufferedReader in = new BufferedReader(new StringReader(binaryText.getText()));
            Collection<Byte> c = new ArrayList<Byte>();

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
                    c.add(b);
                    //log.debug(" - "+h+" -> "+b);
                }
            }

            bytes = new byte[c.size()];
            int pos = 0;
            for (Byte b : c) {
                bytes[pos++] = b;
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        //log.debug("Reading "+bytes.length+" bytes.");

        return bytes;
    }

    public void setVariable(String variable) {
        this.variable = variable;
        type = VARIABLE;
    }

    public String getVariable() {
        return "".equals(variableCombo.getText()) ? null : variableCombo.getText();
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
        type = EXPRESSION;
    }

    public String getForeach() {
        return "".equals(foreachCombo.getText()) ? null : foreachCombo.getText();
    }

    public String getVar() {
        return "".equals(varText.getText()) ? null : varText.getText();
    }

    public String getScript() {
        return "".equals(scriptText.getText()) ? null : scriptText.getText();
    }

    public Expression getExpression() {
        Expression expression = new Expression();
        expression.setForeach(getForeach());
        expression.setVar(getVar());
        expression.setScript(getScript());
        return expression;
    }
}