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

import org.eclipse.swt.SWT;
import org.eclipse.jface.action.Action;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.directory.EntryNode;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.mapping.SourceDialog;
import org.safehaus.penrose.mapping.SourceMapping;
import org.safehaus.penrose.directory.EntryMapping;
import org.safehaus.penrose.directory.AttributeMapping;
import org.safehaus.penrose.directory.FieldMapping;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.filter.*;
import org.safehaus.penrose.ldap.DNBuilder;
import org.safehaus.penrose.ldap.RDNBuilder;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Stack;
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
            ServersView serversView = ServersView.getInstance();

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            //if (!penroseStudio.isCommercial()) return;

            PartitionConfig partitionConfig = node.getPartitionConfig();
            EntryMapping entryMapping = node.getEntryMapping();

            Collection sourceConfigs = partitionConfig.getSourceConfigs().getSourceConfigs();
            if (sourceConfigs.size() == 0) {
                System.out.println("There is no sources defined.");
                return;
            }

            SourceMapping sourceMapping = new SourceMapping();
            SourceDialog dialog = new SourceDialog(serversView.getSite().getShell(), SWT.NONE);
            dialog.setSourceConfigs(sourceConfigs);
            dialog.setSourceMapping(sourceMapping);
            dialog.setText("Select source...");

            dialog.open();

            if (!dialog.isSaved()) return;

            SourceConfig sourceConfig = partitionConfig.getSourceConfigs().getSourceConfig(sourceMapping.getSourceName());

            final EntryMapping entry = new EntryMapping();

            DNBuilder db = new DNBuilder();
            RDNBuilder rb = new RDNBuilder();

            Collection<String> pkNames = sourceConfig.getPrimaryKeyNames();
            for (String pkName : pkNames) {
                rb.set(pkName, "...");
            }

            db.append(rb.toRdn());
            db.append(entryMapping.getDn());

            entry.setDn(db.toDn());
            entry.addObjectClass("top");

            String s = sourceConfig.getParameter("filter");

            if (s != null && !"".equals(s)) {
                FilterParser parser = new FilterParser(new StringReader(s));
                Filter filter = parser.parse();

                FilterProcessor fp = new FilterProcessor() {
                    public void process(Stack<Filter> path, Filter filter) throws Exception {
                        if (!(filter instanceof SimpleFilter)) {
                            super.process(path, filter);
                            return;
                        }

                        SimpleFilter sf = (SimpleFilter)filter;

                        String attribute = sf.getAttribute();
                        if (!attribute.equalsIgnoreCase("objectClass")) return;

                        Object value = sf.getValue();
                        if (value.equals("*")) return;

                        entry.addObjectClass(value.toString());
                    }
                };
                
                fp.process(filter);
            }

            String sourceAlias = sourceMapping.getName();
            for (FieldConfig fieldConfig : sourceConfig.getFieldConfigs()) {
                String fieldName = fieldConfig.getName();

                AttributeMapping attributeMapping = new AttributeMapping(
                        fieldName,
                        AttributeMapping.VARIABLE,
                        sourceAlias + "." + fieldName,
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

            partitionConfig.getDirectoryConfig().addEntryMapping(entry);

            penroseStudio.notifyChangeListeners();

            serversView.open(node);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
	}
	
}