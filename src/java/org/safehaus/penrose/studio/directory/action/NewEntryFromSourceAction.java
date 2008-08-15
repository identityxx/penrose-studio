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

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.safehaus.penrose.directory.EntryAttributeConfig;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.EntryFieldConfig;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.filter.FilterParser;
import org.safehaus.penrose.filter.FilterProcessor;
import org.safehaus.penrose.filter.SimpleFilter;
import org.safehaus.penrose.ldap.DNBuilder;
import org.safehaus.penrose.ldap.RDNBuilder;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.directory.EntryNode;
import org.safehaus.penrose.studio.directory.dialog.SourceDialog;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.server.ServersView;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

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

            ProjectNode projectNode = node.getProjectNode();
            Project project = projectNode.getProject();

            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(node.getPartitionName());

            Collection<String> sourceNames = partitionClient.getSourceNames();
            if (sourceNames.isEmpty()) {
                System.out.println("There is no sources defined.");
                return;
            }

            Map<String,SourceConfig> sourceConfigs = new HashMap<String,SourceConfig>();
            for (String sourceName : sourceNames) {
                SourceClient sourceClient = partitionClient.getSourceClient(sourceName);
                SourceConfig sourceConfig = sourceClient.getSourceConfig();
                sourceConfigs.put(sourceConfig.getName(), sourceConfig);
            }
/*
            Collection<SourceConfig> sourceConfigManager = partitionConfig.getSourceConfigManager().getSourceConfigManager();
            if (sourceConfigManager.isEmpty()) {
                System.out.println("There is no sources defined.");
                return;
            }
*/
            EntrySourceConfig sourceMapping = new EntrySourceConfig();
            SourceDialog dialog = new SourceDialog(serversView.getSite().getShell(), SWT.NONE);
            dialog.setSourceConfigs(sourceConfigs.values());
            dialog.setSourceMapping(sourceMapping);
            dialog.setText("Select source...");

            dialog.open();

            if (!dialog.isSaved()) return;

            SourceConfig sourceConfig = sourceConfigs.get(sourceMapping.getSourceName());

            final EntryConfig newEntryConfig = new EntryConfig();

            DNBuilder db = new DNBuilder();
            RDNBuilder rb = new RDNBuilder();

            Collection<String> pkNames = sourceConfig.getPrimaryKeyNames();
            for (String pkName : pkNames) {
                rb.set(pkName, "...");
            }

            db.append(rb.toRdn());
            db.append(node.getEntryConfig().getDn());

            newEntryConfig.setDn(db.toDn());
            newEntryConfig.addObjectClass("top");

            String s = sourceConfig.getParameter("filter");

            if (s != null && !"".equals(s)) {
                FilterParser parser = new FilterParser(new StringReader(s));
                Filter filter = parser.parse();

                FilterProcessor fp = new FilterProcessor() {
                    public Filter process(Stack<Filter> path, Filter filter) throws Exception {
                        if (!(filter instanceof SimpleFilter)) {
                            return super.process(path, filter);
                        }

                        SimpleFilter sf = (SimpleFilter)filter;

                        String attribute = sf.getAttribute();
                        if (!attribute.equalsIgnoreCase("objectClass")) return filter;

                        Object value = sf.getValue();
                        if (value.equals("*")) return filter;

                        newEntryConfig.addObjectClass(value.toString());
                        
                        return filter;
                    }
                };
                
                fp.process(filter);
            }

            String sourceAlias = sourceMapping.getAlias();
            for (FieldConfig fieldConfig : sourceConfig.getFieldConfigs()) {
                String fieldName = fieldConfig.getName();

                EntryAttributeConfig attributeMapping = new EntryAttributeConfig(
                        fieldName,
                        EntryAttributeConfig.VARIABLE,
                        sourceAlias + "." + fieldName,
                        pkNames.contains(fieldName)
                );

                newEntryConfig.addAttributeConfig(attributeMapping);

                EntryFieldConfig fieldMapping = new EntryFieldConfig(
                        fieldName,
                        EntryFieldConfig.VARIABLE,
                        fieldName
                );

                sourceMapping.addFieldConfig(fieldMapping);
            }

            newEntryConfig.addSourceConfig(sourceMapping);
/*
            DirectoryConfig directoryConfig = partitionConfig.getDirectoryConfig();
            directoryConfig.addEntryConfig(newEntryConfig);
*/
            partitionClient.createEntry(newEntryConfig);
            partitionClient.store();

            penroseStudio.notifyChangeListeners();

            serversView.open(node);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
	}
	
}