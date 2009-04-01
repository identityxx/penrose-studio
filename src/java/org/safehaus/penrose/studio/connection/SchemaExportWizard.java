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
package org.safehaus.penrose.studio.connection;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.SchemaWriter;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * @author Endi S. Dewata
 */
public class SchemaExportWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server server;
    private Schema schema;

    public SchemaExportPage page = new SchemaExportPage();
    public SchemaSyntaxMappingPage syntaxMappingPage;

    public SchemaExportWizard(Server server, Schema schema) {
        this.server = server;
        this.schema = schema;

        syntaxMappingPage = new SchemaSyntaxMappingPage();

        setWindowTitle("Export Partition");
        page.setDescription("Enter the location to which the schema will be exported.");
    }

    public boolean canFinish() {
        if (!page.isPageComplete()) return false;
        if (!syntaxMappingPage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {
            File path = new File(page.getPath());

            if (page.getExcludeDuplicate()) {
                removeDuplicates(schema);
            }

            mapSyntaxes(schema);

            SchemaWriter writer = new SchemaWriter();
            writer.write(path, schema);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }

    public void removeDuplicates(Schema schema) throws Exception {

        PenroseClient client = server.getClient();
        SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();

        Collection<AttributeType> attributeTypes = schemaManagerClient.getAttributeTypes();
        for (AttributeType at : attributeTypes) {
            String oid = at.getOid();
            String name = at.getName();

            AttributeType newAt = schema.getAttributeType(oid);
            if (newAt != null) {
                log.debug("Removing attribute type " + oid);
                schema.removeAttributeType(oid);
            }

            newAt = schema.getAttributeType(name);
            if (newAt != null) {
                log.debug("Removing attribute type " + name);
                schema.removeAttributeType(name);
            }
        }

        Collection<ObjectClass> objectClasses = schemaManagerClient.getObjectClasses();
        for (ObjectClass oc : objectClasses) {
            String oid = oc.getOid();
            String name = oc.getName();

            ObjectClass newOc = schema.getObjectClass(oid);
            if (newOc != null) {
                log.debug("Removing object class " + oid);
                schema.removeObjectClass(oid);
            }

            newOc = schema.getObjectClass(name);
            if (newOc == null) {
                log.debug("Removing object class " + name);
                schema.removeObjectClass(name);
            }
        }
    }

    public void mapSyntaxes(Schema schema) {
        Map syntaxMapping = syntaxMappingPage.getSyntaxMapping();
        Collection<AttributeType> attributeTypes = schema.getAttributeTypes();
        for (AttributeType at : attributeTypes) {
            String oldSyntax = at.getSyntax();
            String newSyntax = (String) syntaxMapping.get(oldSyntax);

            if (newSyntax == null) {
                log.debug("Can't find mapping for " + oldSyntax);
            } else {
                at.setSyntax(newSyntax);
                //log.debug("Mapping "+oldSyntax+" to "+newSyntax);
            }
        }
    }

    public void addPages() {
        addPage(page);
        addPage(syntaxMappingPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }
}
