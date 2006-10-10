package org.safehaus.penrose.studio.util;

import org.safehaus.penrose.partition.SourceConfig;
import org.safehaus.penrose.partition.FieldConfig;
import org.safehaus.penrose.partition.ConnectionConfig;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.mapping.AttributeMapping;
import org.safehaus.penrose.mapping.Expression;
import org.safehaus.penrose.mapping.SourceMapping;

/**
 * @author Endi S. Dewata
 */
public class ADUtil {
    
    public EntryMapping createSchemaProxy(
            Partition partition,
            ConnectionConfig connectionConfig,
            String sourceSchemaDn,
            String destSchemaDn
            ) throws Exception {
        
        SourceConfig sourceConfig = new SourceConfig();
        sourceConfig.setName(connectionConfig.getName()+" Schema");
        sourceConfig.setConnectionName(connectionConfig.getName());

        sourceConfig.addFieldConfig(new FieldConfig("lDAPDisplayName", "true"));
        sourceConfig.addFieldConfig(new FieldConfig("objectClass"));
        sourceConfig.addFieldConfig(new FieldConfig("attributeID"));
        sourceConfig.addFieldConfig(new FieldConfig("adminDescription"));
        sourceConfig.addFieldConfig(new FieldConfig("attributeSyntax"));
        sourceConfig.addFieldConfig(new FieldConfig("isSingleValued"));
        sourceConfig.addFieldConfig(new FieldConfig("governsID"));
        sourceConfig.addFieldConfig(new FieldConfig("mustContain"));
        sourceConfig.addFieldConfig(new FieldConfig("systemMustContain"));
        sourceConfig.addFieldConfig(new FieldConfig("mayContain"));
        sourceConfig.addFieldConfig(new FieldConfig("systemMayContain"));

        sourceConfig.setParameter("baseDn", sourceSchemaDn);
        sourceConfig.setParameter("scope", "ONELEVEL");
        sourceConfig.setParameter("filter", "(objectClass=*)");

        partition.addSourceConfig(sourceConfig);

        EntryMapping entryMapping = new EntryMapping();
        entryMapping.setDn(destSchemaDn);
        entryMapping.addObjectClass("top");
        entryMapping.addObjectClass("subschema");

        entryMapping.addAttributeMapping(new AttributeMapping("cn", AttributeMapping.CONSTANT, "schema", true));

        Expression atExpression = new Expression(
                "schema", "s",
                "import org.safehaus.penrose.schema.*;\n" +
                "\n" +
                "if (!s.objectClass.contains(\"attributeSchema\")) return null;\n" +
                "\n" +
                "AttributeType at = new AttributeType();\n" +
                "at.setOid(s.attributeID);\n" +
                "at.setName(s.lDAPDisplayName);\n" +
                "if (s.adminDescription != void) at.setDescription(s.adminDescription);\n" +
                "if (s.attributeSyntax != void) at.setSyntax(s.attributeSyntax);\n" +
                "if (s.isSingleValued != void) at.setSingleValued(Boolean.valueOf(s.isSingleValued).booleanValue());\n" +
                "return \"( \"+at+\" )\";"
        );

        entryMapping.addAttributeMapping(
                new AttributeMapping(
                        "attributeTypes",
                        AttributeMapping.EXPRESSION,
                        atExpression
                )
        );

        Expression ocExpression = new Expression(
                "schema", "s",
                "import java.util.*;\n" +
                "import org.safehaus.penrose.schema.*;\n" +
                "\n" +
                "if (!s.objectClass.contains(\"classSchema\")) return null;\n" +
                "\n" +
                "ObjectClass oc = new ObjectClass();\n" +
                "oc.setOid(s.governsID);\n" +
                "oc.setName(s.lDAPDisplayName);\n" +
                "if (s.adminDescription != void) oc.setDescription(s.adminDescription);\n" +
                "if (s.mustContain != void) {\n" +
                "   if (s.mustContain instanceof Collection) {\n" +
                "       oc.addRequiredAttributes(s.mustContain);\n" +
                "   } else {\n" +
                "       oc.addRequiredAttribute(s.mustContain);\n" +
                "   }\n" +
                "}\n" +
                "if (s.systemMustContain != void) {\n" +
                "    if (s.systemMustContain instanceof Collection) {\n" +
                "        oc.addRequiredAttributes(s.systemMustContain);\n" +
                "    } else {\n" +
                "        oc.addRequiredAttribute(s.systemMustContain);\n" +
                "    }\n" +
                "}\n" +
                "if (s.mayContain != void) {\n" +
                "    if (s.mayContain instanceof Collection) {\n" +
                "        oc.addOptionalAttributes(s.mayContain);\n" +
                "    } else {\n" +
                "        oc.addOptionalAttribute(s.mayContain);\n" +
                "    }\n" +
                "}\n" +
                "if (s.systemMayContain != void) {\n" +
                "    if (s.systemMayContain instanceof Collection) {\n" +
                "        oc.addOptionalAttributes(s.systemMayContain);\n" +
                "    } else {\n" +
                "        oc.addOptionalAttribute(s.systemMayContain);\n" +
                "    }\n" +
                "}\n" +
                "return \"( \"+oc+\" )\";"
        );

        entryMapping.addAttributeMapping(
                new AttributeMapping(
                        "objectClasses",
                        AttributeMapping.EXPRESSION,
                        ocExpression
                )
        );

        SourceMapping sourceMapping = new SourceMapping("schema", sourceConfig.getName());
        entryMapping.addSourceMapping(sourceMapping);

        partition.addEntryMapping(entryMapping);

        return entryMapping;
    }
}
