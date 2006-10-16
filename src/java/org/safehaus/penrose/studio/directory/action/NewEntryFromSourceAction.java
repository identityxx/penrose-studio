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
package org.safehaus.penrose.studio.directory.action;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.jface.action.Action;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.directory.EntryNode;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.mapping.SourceDialog;
import org.safehaus.penrose.mapping.SourceMapping;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.mapping.AttributeMapping;
import org.safehaus.penrose.mapping.FieldMapping;
import org.safehaus.penrose.partition.SourceConfig;
import org.safehaus.penrose.partition.FieldConfig;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.filter.*;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.io.StringReader;

public class NewEntryFromSourceAction extends Action {

    Logger log = Logger.getLogger(getClass());

    EntryNode node;

	public NewEntryFromSourceAction(EntryNode node) {
        this.node = node;

        setText("New Dynamic Entry from Source...");
        setId(getClass().getName());
	}
	
	public void run() {
        try {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

            Shell shell = window.getShell();

            PenroseApplication penroseApplication = PenroseApplication.getInstance();
            //if (!penroseApplication.checkCommercial()) return;

            Partition partition = node.getPartition();
            EntryMapping entryMapping = node.getEntryMapping();

            Collection sourceConfigs = partition.getSourceConfigs();
            if (sourceConfigs.size() == 0) {
                System.out.println("There is no sources defined.");
                return;
            }

            SourceMapping sourceMapping = new SourceMapping();
            SourceDialog dialog = new SourceDialog(shell, SWT.NONE);
            dialog.setSourceConfigs(sourceConfigs);
            dialog.setSourceMapping(sourceMapping);
            dialog.setText("Select source...");

            dialog.open();

            if (!dialog.isSaved()) return;

            SourceConfig sourceConfig = partition.getSourceConfig(sourceMapping.getSourceName());

            final EntryMapping entry = new EntryMapping();

            StringBuffer rdn = new StringBuffer();
            Collection pkNames = sourceConfig.getPrimaryKeyNames();
            for (Iterator i=pkNames.iterator(); i.hasNext(); ) {
                String pkName = (String)i.next();
                if (rdn.length() > 0) rdn.append("+");
                rdn.append(pkName);
                rdn.append("=...");
            }

            entry.setRdn(rdn.toString());
            entry.setParentDn(entryMapping.getDn());

            entry.addObjectClass("top");

            String s = sourceConfig.getParameter("filter");

            if (s != null && !"".equals(s)) {
                FilterParser parser = new FilterParser(new StringReader(s));
                Filter filter = parser.parse();

                FilterIterator fi = new FilterIterator(filter, new FilterVisitor() {
                    public void preVisit(Collection parents, Filter filter) {
                        log.debug("Filter: "+filter.getClass().getName());

                        if (!(filter instanceof SimpleFilter)) return;

                        SimpleFilter sf = (SimpleFilter)filter;

                        String attribute = sf.getAttribute();
                        if (!attribute.equalsIgnoreCase("objectClass")) return;

                        String value = sf.getValue();
                        if (value.equals("*")) return;

                        entry.addObjectClass(value);
                    }
                });
                fi.run();
            }

            String sourceAlias = sourceMapping.getName();
            for (Iterator i=sourceConfig.getFieldConfigs().iterator(); i.hasNext(); ) {
                FieldConfig fieldConfig = (FieldConfig)i.next();
                String fieldName = fieldConfig.getName();

                AttributeMapping attributeMapping = new AttributeMapping(
                        fieldName,
                        AttributeMapping.VARIABLE,
                        sourceAlias+"."+fieldName,
                        pkNames.contains(fieldName)
                );

                entry.addAttributeMapping(attributeMapping);

                FieldMapping fieldMapping = new FieldMapping(
                        fieldName,
                        FieldMapping.VARIABLE,
                        fieldName
                );

                sourceMapping.addFieldMapping(fieldMapping);
            }

            entry.addSourceMapping(sourceMapping);

            partition.addEntryMapping(entry);

            penroseApplication.notifyChangeListeners();

            objectsView.show(node);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
	}
	
}