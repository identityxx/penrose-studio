package org.safehaus.penrose.studio.server.editor;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.ServerConfig;
import org.safehaus.penrose.studio.server.ServerNode;
import org.safehaus.penrose.studio.PenroseStudio;

/**
 * @author Endi S. Dewata
 */
public class ServerPropertiesPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Label typeLabel;
    Label hostnameLabel;
    Label portLabel;
    Label usernameLabel;
    Label passwordLabel;

    ServerEditor editor;
    Server server;
    ServerConfig serverConfig;

    public ServerPropertiesPage(ServerEditor editor) {
        super(editor, "PROPERTIES", "  Properties  ");

        this.editor = editor;
        this.server = editor.getServer();
        this.serverConfig = editor.getServerConfig();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText(serverConfig.getName());

        Composite body = form.getBody();
        body.setLayout(new GridLayout(2, false));

        Section propertiesSection = createPropertiesSection(body);
        propertiesSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Section actionsSection = createActionsSection(body);
        actionsSection.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    public Section createPropertiesSection(final Composite parent) {

        Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Properties");

        Composite composite = toolkit.createComposite(section);
        section.setClient(composite);
        composite.setLayout(new GridLayout(2, false));

        Label typeLabel = toolkit.createLabel(composite, "Type:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        typeLabel.setLayoutData(gd);

        this.typeLabel = toolkit.createLabel(composite, "", SWT.NONE);
        this.typeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        String s = serverConfig.getType();
        if (s != null) this.typeLabel.setText(s);

        toolkit.createLabel(composite, "Hostname:");

        hostnameLabel = toolkit.createLabel(composite, "", SWT.NONE);
        hostnameLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        s = serverConfig.getHostname();
        if (s != null) hostnameLabel.setText(s);

        toolkit.createLabel(composite, "Port:");

        portLabel = toolkit.createLabel(composite, "", SWT.NONE);
        portLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        int port = serverConfig.getPort();
        portLabel.setText(port+"");

        toolkit.createLabel(composite, "Username:");

        usernameLabel = toolkit.createLabel(composite, "", SWT.NONE);
        usernameLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        s = serverConfig.getUsername();
        if (s != null) usernameLabel.setText(s);

        toolkit.createLabel(composite, "Password:");

        passwordLabel = toolkit.createLabel(composite, "", SWT.PASSWORD);
        passwordLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        passwordLabel.setText("********");

        return section;
    }

    public Section createActionsSection(final Composite parent) {

        Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Actions");

        Composite composite = toolkit.createComposite(section);
        section.setClient(composite);
        composite.setLayout(new GridLayout());

        Hyperlink openLink = toolkit.createHyperlink(composite, "Connect", SWT.NONE);

        openLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                try {
                    PenroseStudio penroseStudio = PenroseStudio.getInstance();

                    ServerNode serverNode = penroseStudio.getServerNode(server.getName());
                    serverNode.connect();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        Hyperlink closeLink = toolkit.createHyperlink(composite, "Disconnect", SWT.NONE);

        closeLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                try {
                    PenroseStudio penroseStudio = PenroseStudio.getInstance();

                    ServerNode serverNode = penroseStudio.getServerNode(server.getName());
                    serverNode.disconnect();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        Hyperlink editLink = toolkit.createHyperlink(composite, "Properties", SWT.NONE);

        editLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                try {
                    String oldProjectName = serverConfig.getName();

                    ServerEditorDialog dialog = new ServerEditorDialog(parent.getShell(), SWT.NONE);
                    dialog.setServerConfig(serverConfig);
                    dialog.open();

                    if (dialog.getAction() == ServerEditorDialog.CANCEL) return;

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();

                    if (!oldProjectName.equals(serverConfig.getName())) {
                        penroseStudio.removeServer(oldProjectName);
                        penroseStudio.addServer(serverConfig);
                    }

                    penroseStudio.save();

                    penroseStudio.fireChangeEvent();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        return section;
    }

    public void checkDirty() {
        editor.checkDirty();
    }
}
