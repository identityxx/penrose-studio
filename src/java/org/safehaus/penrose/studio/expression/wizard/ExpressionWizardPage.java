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
package org.safehaus.penrose.studio.expression.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.safehaus.penrose.studio.views.CodeAssist;
import org.safehaus.penrose.mapping.Expression;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.io.*;

/**
 * @author Endi S. Dewata
 */
public class ExpressionWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Expression";

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

    int type;

    String text;
    byte[] binary;
    String variable;
    Expression expression = new Expression();

    public Collection<String> variables = new ArrayList<String>();

	public ExpressionWizardPage() {
        super(NAME);
        setDescription("Enter attribute value.");
    }

    public void createControl(final Composite parent) {

        font = new Font(parent.getDisplay(), "Courier New", 8, SWT.NONE);

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout());

        Composite types = new Composite(composite, SWT.NONE);
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
                try {
                    if (!textRadio.getSelection()) return;
                    setType(TEXT);
                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        });

        binaryRadio = new Button(typeButtons, SWT.RADIO);
        binaryRadio.setText("binary");
        binaryRadio.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (!binaryRadio.getSelection()) return;
                    setType(BINARY);
                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        });

        variableRadio = new Button(typeButtons, SWT.RADIO);
        variableRadio.setText("variable");
        variableRadio.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (!variableRadio.getSelection()) return;
                    setType(VARIABLE);
                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        });

        expressionRadio = new Button(typeButtons, SWT.RADIO);
        expressionRadio.setText("expression");
        expressionRadio.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (!expressionRadio.getSelection()) return;
                    setType(EXPRESSION);
                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        });

        Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        blankPanel = new Composite(composite, SWT.NONE);
        blankPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
        blankPanel.setLayout(new FillLayout());

        refresh();
    }

    public Composite createTextPanel(Composite parent) {

        log.debug("Creating text panel.");

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

        textText.setText(text == null ? "" : text);

        textText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                text = textText.getText().trim();
                text = "".equals(text) ? null : text;
            }
        });

        textText.setFocus();

        return composite;
    }

    public Composite createBinaryPanel(Composite parent) {

        log.debug("Creating binary panel.");

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
                try {
                    FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
                    dialog.setText("Import Binary Data");

                    String filename = dialog.open();
                    if (filename == null) return;

                    File file = new File(filename);
                    binary = new byte[(int)file.length()];

                    FileInputStream out = new FileInputStream(file);
                    int length = out.read(binary);

                    if (length != file.length()) {
                        throw new Exception("Error reading file.");
                    }

                    out.close();

                    String s = getBinaryText();
                    binaryText.setText(s);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        });

        Hyperlink exportData = new Hyperlink(actions, SWT.NONE);
        exportData.setText("Export");
        exportData.setUnderlined(true);

        exportData.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                try {
                    FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
                    dialog.setText("Export Binary Data");

                    String filename = dialog.open();
                    if (filename == null) return;

                    FileOutputStream out = new FileOutputStream(filename);
                    out.write(binary);
                    out.close();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        });

        String s = getBinaryText();
        binaryText.setText(s);

        return composite;
    }

    public String getBinaryText() {

        if (binary == null) return "";
        
        StringBuilder sb = new StringBuilder();

        int lines = binary.length / 16 + 1;
        int pos = 0;

        for (int i=0; i<lines && pos < binary.length; i++) {

            String index = Integer.toHexString(pos);
            for (int j=0; j<8-index.length(); j++) sb.append("0");
            sb.append(index);
            sb.append(": ");

            StringBuilder text = new StringBuilder();

            for (int j=0; j<16; j++) {

                if (pos < binary.length) {
                    int b = binary[pos++] & 0xff;
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

        return sb.toString();
    }

    public Composite createVariablePanel(Composite parent) {

        log.debug("Creating variable panel.");

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label label = new Label(composite, SWT.NONE);
        label.setText("Variable:");
        GridData gd = new GridData();
        gd.widthHint = 80;
        label.setLayoutData(gd);

        variableCombo = new Combo(composite, SWT.BORDER);
        variableCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        variableCombo.add("");
        for (String variable : variables) variableCombo.add(variable);

        variableCombo.setText(variable == null ? "" : variable);

        variableCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                variable = variableCombo.getText().trim();
                variable = "".equals(variable) ? null : variable;
            }
        });

        variableCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                variable = variableCombo.getText().trim();
                variable = "".equals(variable) ? null : variable;
            }
        });

        variableCombo.setFocus();

        return composite;
    }

    public Composite createExpressionPanel(Composite parent) {

        log.debug("Creating expression panel.");

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

        String script = expression.getScript();
        scriptText.setText(script == null ? "" : script);

        scriptText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String script = scriptText.getText().trim();
                expression.setScript("".equals(script) ? null : script);
            }
        });

        new Label(composite, SWT.NONE);

        Label foreachLabel = new Label(composite, SWT.NONE);
        foreachLabel.setText("Repeat for each:");
        foreachLabel.setLayoutData(new GridData());

        foreachCombo = new Combo(composite, SWT.BORDER);
        foreachCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        foreachCombo.add("");
        for (String variable : variables) foreachCombo.add(variable);

        String foreach = expression.getForeach();
        foreachCombo.setText(foreach == null ? "" : foreach);

        foreachCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                String foreach = foreachCombo.getText().trim();
                expression.setForeach("".equals(foreach) ? null : foreach);
            }
        });

        foreachCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String foreach = foreachCombo.getText().trim();
                expression.setForeach("".equals(foreach) ? null : foreach);
            }
        });

        Label varLabel = new Label(composite, SWT.NONE);
        varLabel.setText("Variable name:");
        varLabel.setLayoutData(new GridData());

        varText = new Text(composite, SWT.BORDER);
        varText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        String var = expression.getVar();
        varText.setText(var == null ? "" : var);

        varText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String var = varText.getText().trim();
                expression.setVar("".equals(var) ? null : var);
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Collection<String> getVariables() {
        return variables;
    }
    
    public void setVariables(Collection<String> variables) {
        if (this.variables == variables) return;
        this.variables.clear();
        if (variables == null) return;
        this.variables.addAll(variables);
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression.copy(expression);
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setBinary(byte[] binary) {
        this.binary = binary;
    }

    public byte[] getBinary() {
        return binary;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getVariable() {
        return variable;
    }

    void showHints() {
        CodeAssist codeAssist = new CodeAssist(getShell(), scriptText);
        codeAssist.open();
    }

    public interface NameChecker {
		public String checkName(String name);
	}
}