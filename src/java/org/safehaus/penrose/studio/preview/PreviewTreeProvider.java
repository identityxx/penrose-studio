/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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
package org.safehaus.penrose.studio.preview;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.session.PenroseSearchResults;
import org.safehaus.penrose.session.PenroseSession;
import org.safehaus.penrose.session.PenroseSearchControls;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.user.UserConfig;
import org.safehaus.penrose.Penrose;
import org.safehaus.penrose.util.EntryUtil;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.partition.PartitionManager;
import org.safehaus.penrose.partition.Partition;
import org.apache.log4j.Logger;
import org.ietf.ldap.LDAPEntry;
import org.ietf.ldap.LDAPAttribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

public class PreviewTreeProvider extends LabelProvider implements ITableLabelProvider, ITreeContentProvider, ISelectionChangedListener {

    private Logger log = Logger.getLogger(getClass());

    private PreviewEditor previewEditor;

    public PreviewTreeProvider(PreviewEditor previewEditor) {
        this.previewEditor = previewEditor;
    }

    public Image getColumnImage(Object element, int columnIndex) {
        return getImage(element);
    }

    public String getColumnText(Object element, int columnIndex) {
        return getText(element);
    }

    public Image getImage(Object element) {
        PreviewNode previewNode = (PreviewNode) element;
        if (previewNode.getParent() == null) {
            return PenrosePlugin.getImage(PenroseImage.HOME_NODE);
        } else {
            return PenrosePlugin.getImage(PenroseImage.NODE);
        }
    }

    public String getText(Object element) {
        PreviewNode previewNode = (PreviewNode) element;
        return previewNode.getTitle();
/*
        LDAPEntry entry = (LDAPEntry)previewNode.getObject();
        String dn = entry.getDN();
        if (previewNode.getParent() == null) {
            return "".equals(dn) ? "Root DSE" : dn;
        } else {
            int i = dn.indexOf(",");
            return dn.substring(0, i);
        }
*/
    }

    public Object[] getElements(Object inputElement) {

		Object[] result = new PreviewNode[0];
		try {
            PenroseApplication penroseApplication = PenroseApplication.getInstance();
            PenroseConfig penroseConfig = penroseApplication.getPenroseConfig();

            UserConfig rootUserConfig = penroseConfig.getRootUserConfig();

            Penrose penrose = previewEditor.penrose;
            PenroseSession session = penrose.newSession();
            session.bind(rootUserConfig.getDn(), rootUserConfig.getPassword());

            Collection list = new ArrayList();

            PenroseSearchControls sc = new PenroseSearchControls();
            sc.setScope(PenroseSearchControls.SCOPE_BASE);
            PenroseSearchResults sr = session.search("", "(objectClass=*)", sc);

            LDAPEntry rootDseEntry = (LDAPEntry)sr.next();
            PreviewNode rootDseNode = new PreviewNode("Root DSE", rootDseEntry);
            list.add(rootDseNode);
/*
            PartitionManager partitionManager = penrose.getPartitionManager();
            Collection partitions = partitionManager.getPartitions();
            for (Iterator i=partitions.iterator(); i.hasNext(); ) {
                Partition partition = (Partition)i.next();
                Collection entryMappings = partition.getRootEntryMappings();
                for (Iterator j=entryMappings.iterator(); j.hasNext(); ) {
                    EntryMapping entryMapping = (EntryMapping)j.next();
                    String dn = entryMapping.getDn();
                    if ("".equals(dn)) continue;

                    sc = new PenroseSearchControls();
                    sc.setScope(PenroseSearchControls.SCOPE_BASE);
                    sr = session.search(dn, "(objectClass=*)", sc);

                    log.debug("Returned from searching "+dn);
                    log.debug(dn+" has next: "+sr.hasNext());
                    if (!sr.hasNext()) continue;

                    log.debug("Got result from searching "+dn);
                    LDAPEntry entry = (LDAPEntry)sr.next();
                    PreviewNode previewNode = new PreviewNode(entry);
                    previewNode.parent = rootDseNode;
                    list.add(previewNode);
                }
            }
*/
/*
            log.debug("Searching Root DSE"+"...");
            PenroseSearchControls sc = new PenroseSearchControls();
            sc.setScope(PenroseSearchControls.SCOPE_BASE);

            PenroseSearchResults sr = session.search("", "(objectClass=*)", sc);

            LDAPEntry ldapEntry = (LDAPEntry)sr.next();

            LDAPAttribute attribute = ldapEntry.getAttribute("namingContexts");
            for (Enumeration e=attribute.getStringValues(); e.hasMoreElements(); ) {
                String suffix = (String)e.nextElement();

                log.debug("Searching "+suffix+"...");

                PenroseSearchControls sc2 = new PenroseSearchControls();
                sc2.setScope(PenroseSearchControls.SCOPE_BASE);
                PenroseSearchResults sr2 = session.search(suffix, "(objectClass=*)", sc2);

                log.debug("Returned from searching "+suffix);
                log.debug(suffix+" has next: "+sr2.hasNext());
                if (!sr2.hasNext()) continue;

                log.debug("Got result from searching "+suffix);
                LDAPEntry entry = (LDAPEntry)sr2.next();
                PreviewNode previewNode = new PreviewNode(entry);
                list.add(previewNode);
            }
*/
            session.close();

            result = list.toArray();

		} catch (Exception ex) {
			log.debug(ex.toString(), ex);
		}
		return result;
	}

    public Object[] getChildren(Object object) {
        log.debug("getChildren:"+object);
        PreviewNode previewNode = (PreviewNode) object;

        Object[] children = new Object[0];

        try {
            LDAPEntry parentEntry = (LDAPEntry)previewNode.getObject();
            String baseDn = parentEntry.getDN();
            String filter = "(objectClass=*)";

            PenroseApplication penroseApplication = PenroseApplication.getInstance();
            PenroseConfig penroseConfig = penroseApplication.getPenroseConfig();
            UserConfig rootUserConfig = penroseConfig.getRootUserConfig();

            PenroseSession session = previewEditor.penrose.newSession();
            session.bind(rootUserConfig.getDn(), rootUserConfig.getPassword());

            ArrayList list = new ArrayList();

            if ("".equals(baseDn)) {

                PartitionManager partitionManager = previewEditor.penrose.getPartitionManager();
                Collection partitions = partitionManager.getPartitions();

                for (Iterator i=partitions.iterator(); i.hasNext(); ) {
                    Partition partition = (Partition)i.next();
                    Collection entryMappings = partition.getRootEntryMappings();

                    for (Iterator j=entryMappings.iterator(); j.hasNext(); ) {
                        EntryMapping entryMapping = (EntryMapping)j.next();
                        String dn = entryMapping.getDn();
                        if ("".equals(dn)) continue;

                        PenroseSearchControls sc = new PenroseSearchControls();
                        sc.setScope(PenroseSearchControls.SCOPE_BASE);
                        PenroseSearchResults sr = session.search(dn, "(objectClass=*)", sc);

                        log.debug("Returned from searching "+dn);
                        log.debug(dn+" has next: "+sr.hasNext());
                        if (!sr.hasNext()) continue;

                        log.debug("Got result from searching "+dn);
                        LDAPEntry entry = (LDAPEntry)sr.next();
                        PreviewNode childPreviewNode = new PreviewNode(dn, entry);
                        childPreviewNode.setParent(previewNode);
                        list.add(childPreviewNode);
                    }
                }

            } else {

                PenroseSearchControls sc = new PenroseSearchControls();
                sc.setScope(PenroseSearchControls.SCOPE_ONE);

                PenroseSearchResults sr = session.search(baseDn, filter, sc);

                while (sr.hasNext()) {
                    LDAPEntry child = (LDAPEntry) sr.next();
                    String rdn = EntryUtil.getRdn(child.getDN()).toString();

                    PreviewNode childPreviewNode = new PreviewNode(rdn, child);
                    childPreviewNode.setParent(previewNode);
                    list.add(childPreviewNode);
                }
            }

            children = list.toArray();
            session.close();

        } catch (Exception ex) {
            log.debug(ex.toString(), ex);
        }

        return children;
    }

    public Object getParent(Object element) {
        PreviewNode previewNode = (PreviewNode) element;
        return previewNode.getParent();
    }

    public boolean hasChildren(Object element) {
        return true;
    }

    public void selectionChanged(SelectionChangedEvent event) {
        Object o = ((IStructuredSelection) previewEditor.treeViewer.getSelection()).getFirstElement();
        if (o != null) {
            previewEditor.tableProvider.previewNode = (PreviewNode) o;
        }
        previewEditor.tableViewer.refresh();
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
}
