package org.safehaus.penrose.studio.connection.editor;

import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * @author Endi S. Dewata
 */
public class ConnectionStatusPage extends ConnectionEditorPage {

    Text statusText;

    public ConnectionStatusPage(ConnectionEditor editor) {
        super(editor, "STATUS", "  Status  ");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();
        Composite body = form.getBody();
        body.setLayout(new GridLayout(2, false));

        Section propertiesSection = createPropertiesControl(body);
        propertiesSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Section actionsSection = createActionsSection(body);
        actionsSection.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    public void refresh() {
        ConnectionEditor editor = (ConnectionEditor)getEditor();
        Server server = editor.getServer();
        ConnectionConfig connectionConfig = editor.getConnectionConfig();

        PenroseClient client = server.getClient();
        ConnectionManagerClient connectionManagerClient = client.getConnectionManagerClient();

        try {
            String status = connectionManagerClient.getStatus(getPartition().getName(), connectionConfig.getName());
            statusText.setText(status);
        } catch (Exception e) {
            statusText.setText("UNKNOWN");
        }
    }

    public Section createPropertiesControl(final Composite parent) {

        Section section = getToolkit().createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Properties");

        Composite composite = getToolkit().createComposite(section);
        section.setClient(composite);
        composite.setLayout(new GridLayout(2, false));

        Label statusLabel = getToolkit().createLabel(composite, "Status:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        statusLabel.setLayoutData(gd);

        statusText = getToolkit().createText(composite, "", SWT.READ_ONLY);
        statusText.setEnabled(false);
        statusText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return section;
    }

    public Section createActionsSection(final Composite parent) {

        Section section = getToolkit().createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Actions");

        Composite composite = getToolkit().createComposite(section);
        section.setClient(composite);
        composite.setLayout(new GridLayout());

        Hyperlink startLink = getToolkit().createHyperlink(composite, "Start", SWT.NONE);

        startLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                try {
                    ConnectionEditor editor = (ConnectionEditor)getEditor();
                    Server server = editor.getServer();
                    ConnectionConfig connectionConfig = editor.getConnectionConfig();

                    PenroseClient client = server.getClient();
                    ConnectionManagerClient connectionManagerClient = client.getConnectionManagerClient();

                    connectionManagerClient.start(getPartition().getName(), connectionConfig.getName());

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        Hyperlink stopLink = getToolkit().createHyperlink(composite, "Stop", SWT.NONE);

        stopLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                try {
                    ConnectionEditor editor = (ConnectionEditor)getEditor();
                    Server server = editor.getServer();
                    ConnectionConfig connectionConfig = editor.getConnectionConfig();

                    PenroseClient client = server.getClient();
                    ConnectionManagerClient connectionManagerClient = client.getConnectionManagerClient();

                    connectionManagerClient.stop(getPartition().getName(), connectionConfig.getName());

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        Hyperlink refreshLink = getToolkit().createHyperlink(composite, "Refresh", SWT.NONE);

        refreshLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                refresh();
            }
        });

        return section;
    }
}
