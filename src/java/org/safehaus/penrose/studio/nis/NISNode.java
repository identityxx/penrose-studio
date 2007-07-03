package org.safehaus.penrose.studio.nis;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.source.SourceManager;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.nis.NISDomain;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class NISNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;

    public NISNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) throws Exception {

        manager.add(new Action("New Domain...") {
            public void run() {
                try {
                    newDomain();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Refresh") {
            public void run() {
                try {
                    refresh();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
    }

    public boolean hasChildren() throws Exception {

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();
        SourceManager sourceManager = penroseContext.getSourceManager();

        Source domains = sourceManager.getSource("DEFAULT", "penrose.domains");
        if (domains == null) return false;

        SearchRequest request = new SearchRequest();
        SearchResponse<SearchResult> response = new SearchResponse<SearchResult>();

        domains.search(request, response);

        return response.getTotalCount() > 0;
    }

    public Collection getChildren() throws Exception {

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();
        SourceManager sourceManager = penroseContext.getSourceManager();

        Source domains = sourceManager.getSource("DEFAULT", "penrose.domains");
        if (domains == null) return null;

        SearchRequest request = new SearchRequest();
        SearchResponse<SearchResult> response = new SearchResponse<SearchResult>();

        domains.search(request, response);

        Collection children = new ArrayList();

        while (response.hasNext()) {
            SearchResult result = response.next();
            Attributes attributes = result.getAttributes();
            
            String name = (String)attributes.getValue("name");
            String partition = (String)attributes.getValue("partition");
            String server = (String)attributes.getValue("server");
            String suffix = (String)attributes.getValue("suffix");

            NISDomain domain = new NISDomain();
            domain.setName(name);
            domain.setPartition(partition);
            domain.setServer(server);
            domain.setSuffix(suffix);

            NISDomainNode node = new NISDomainNode(
                    view,
                    partition,
                    ObjectsView.ENTRY,
                    PenrosePlugin.getImage(PenroseImage.NODE),
                    domain,
                    this
            );

            children.add(node);
        }

        return children;
    }

    public void newDomain() throws Exception {

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();
        SourceManager sourceManager = penroseContext.getSourceManager();

        Source domains = sourceManager.getSource("DEFAULT", "penrose.domains");

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        NISDomainDialog dialog = new NISDomainDialog(shell, SWT.NONE);

        dialog.open();

        int action = dialog.getAction();
        if (action == NISUserDialog.CANCEL) return;

        RDNBuilder rb = new RDNBuilder();
        rb.set("name", dialog.getName());
        DN dn = new DN(rb.toRdn());

        Attributes attributes = new Attributes();
        attributes.setValue("name", dialog.getName());
        attributes.setValue("partition", dialog.getPartition());
        attributes.setValue("server", dialog.getServer());
        attributes.setValue("suffix", dialog.getSuffix());

        domains.add(dn, attributes);

        penroseApplication.notifyChangeListeners();
    }

    public void refresh() throws Exception {
        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        penroseApplication.notifyChangeListeners();
    }
}
