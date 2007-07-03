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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.safehaus.penrose.partition.SourceConfig;

public class FieldEditorWindow {

	Shell shell;
	FormToolkit toolkit;
	ScrolledForm form;
	
	Text name;
	Text description;
	Button jdbcButton;
	Button jndiButton;
	
	Text jdbcDriver;
	Text jdbcUrl;
	Text jdbcUsername;
	Text jdbcPassword;
	
	Text jndiProviderUrl;
	Text jndiInitialContext;
	Text jndiSecurityPrincipal;
	Text jndiSecurityCredentials;

	Button editOnCopy;

	Section jdbcSection;
	Section jndiSection;
	
	Button saveButton;
	
	FieldEditorWindow(Shell parent, SourceConfig source) {
		// Shell
		shell = new Shell(parent);
		shell.setSize(400, 500);
	    shell.setLayout(new FillLayout());
	    // Toolkit and Form
		toolkit = new FormToolkit(shell.getDisplay());
		form = toolkit.createScrolledForm(shell);
		//form.setLayoutData(new gridl TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));
		TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);
		// Title
		shell.setText("Source Editor");
		form.setText("Source Editor");
		// Sections
		createHeadSection(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
		jdbcSection = createJdbcSourceSection(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
		jndiSection = createJndiSourceSection(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
		createFootSection(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
		// Open shell
	    shell.open();
	    // Prepopulate
	    if (source != null) {
	    	load(source);
	    }
		jdbcSection.setExpanded(jdbcButton.getSelection());
		jndiSection.setExpanded(jndiButton.getSelection());
	}
	
	FieldEditorWindow(Shell parent) {
		this(parent, null);
	}
	
	public void load(SourceConfig source) {
		// TODO
	}
	
	public void store(SourceConfig Source) {
		// TODO
	}

	/**
	 * "Head" Section
	 */
	public Section createHeadSection(TableWrapData layoutData) {
		Section section = toolkit.createSection(form.getBody(),
				Section.DESCRIPTION | Section.TITLE_BAR | Section.EXPANDED);
		section.setLayoutData(layoutData);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section.setText("Source Editor");
		section.setDescription("Choose a Source type. ");
		Composite sectionClient = toolkit.createComposite(section);
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 2;
		sectionClient.setLayout(sectionLayout);
		// Source Name: (text)
		toolkit.createLabel(sectionClient, "Source Name:");
		name = toolkit.createText(sectionClient, "", SWT.BORDER);
		name.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// Description: (text)
		toolkit.createLabel(sectionClient, "Description:");
		description = toolkit.createText(sectionClient, "", SWT.BORDER);
		description.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// Source Type: [x] Database Source 
		toolkit.createLabel(sectionClient, "Source Type:");
		jdbcButton = toolkit.createButton(sectionClient, "Database Source (JDBC): SQL or databases", SWT.RADIO);
		jdbcButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				jdbcSection.setEnabled(jdbcButton.getSelection());
				jdbcSection.setVisible(jdbcButton.getSelection());
				jdbcSection.setExpanded(jdbcButton.getSelection());
			}
		});
		// "" [x] Directory Source
		toolkit.createLabel(sectionClient, "");
		jndiButton = toolkit.createButton(sectionClient, "Directory Source (JNDI): LDAP or directories", SWT.RADIO);
		jndiButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				jndiSection.setEnabled(jndiButton.getSelection());
				jndiSection.setVisible(jndiButton.getSelection());
				jndiSection.setExpanded(jndiButton.getSelection());
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
		sectionLayout.numColumns = 3;
		sectionClient.setLayout(sectionLayout);
		// [Test Source] 
		Button testButton = toolkit.createButton(sectionClient, "Test Source", SWT.PUSH);
		testButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				// TODO
			}
		});
		// [Save]
		saveButton = toolkit.createButton(sectionClient, "Save", SWT.PUSH);
		saveButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				// TODO 
				shell.close();
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

	/**
	 * "Jdbc Source" Section
	 */
	public Section createJdbcSourceSection(TableWrapData layoutData) {
		Section section = toolkit.createSection(form.getBody(), 
				Section.DESCRIPTION | Section.EXPANDED);
		section.setLayoutData(layoutData);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section.setDescription("Edit JDBC Source parameters below.");
		Composite sectionClient = toolkit.createComposite(section);
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 2;
		sectionClient.setLayout(sectionLayout);
		// JDBC Driver: (text)
		toolkit.createLabel(sectionClient, "JDBC URL:");
		jdbcDriver = toolkit.createText(sectionClient, "", SWT.BORDER);
		jdbcDriver.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// JDBC URL: (text)
		toolkit.createLabel(sectionClient, "JDBC URL:");
		jdbcUrl = toolkit.createText(sectionClient, "", SWT.BORDER);
		jdbcUrl.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// Username: (text)
		toolkit.createLabel(sectionClient, "Username:");
		jdbcUsername = toolkit.createText(sectionClient, "", SWT.BORDER);
		jdbcUsername.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// Password: (text)
		toolkit.createLabel(sectionClient, "Password:");
		jdbcPassword = toolkit.createText(sectionClient, "", SWT.BORDER | SWT.PASSWORD);
		
		jdbcPassword.setLayoutData(new TableWrapData(TableWrapData.LEFT));
		// end
		section.setClient(sectionClient);
		return section;
	}

	/**
	 * "Jndi Source" Section
	 */
	public Section createJndiSourceSection(TableWrapData layoutData) {
		Section section = toolkit.createSection(form.getBody(), 
				Section.DESCRIPTION | Section.EXPANDED);
		section.setLayoutData(layoutData);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section.setDescription("Edit JNDI Source parameters below.");
		Composite sectionClient = toolkit.createComposite(section);
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 2;
		sectionClient.setLayout(sectionLayout);
		// JNDI Initial: (text)
		toolkit.createLabel(sectionClient, "JNDI Initial Context:");
		jndiInitialContext = toolkit.createText(sectionClient, "", SWT.BORDER);
		jndiInitialContext.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// JNDI Provider: (text)
		toolkit.createLabel(sectionClient, "JNDI Provider URL:");
		jndiProviderUrl = toolkit.createText(sectionClient, "", SWT.BORDER);
		jndiProviderUrl.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// Principal: (text)
		toolkit.createLabel(sectionClient, "Principal:");
		jndiSecurityPrincipal = toolkit.createText(sectionClient, "", SWT.BORDER);
		jndiSecurityPrincipal.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// Credentials: (text)
		toolkit.createLabel(sectionClient, "Credentials:");
		jndiSecurityCredentials = toolkit.createText(sectionClient, "", SWT.BORDER
				| SWT.PASSWORD);
		jndiSecurityCredentials.setLayoutData(new TableWrapData(TableWrapData.LEFT));
		// end
		section.setClient(sectionClient);
		return section;
	}

}
