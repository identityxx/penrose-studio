package org.safehaus.penrose.studio.config;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.safehaus.penrose.studio.project.ProjectConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class PenroseStudioConfigWriter {

    Writer writer;

    public PenroseStudioConfigWriter(Writer writer) throws Exception {
        this.writer = writer;
    }

    public PenroseStudioConfigWriter(File file) throws Exception {
        writer = new FileWriter(file);
    }

    public void write(PenroseStudioConfig config) throws Exception {
        write(toElement(config));
    }

    public void write(ProjectConfig projectConfig) throws Exception {
        write(toElement(projectConfig));
    }
    
    public void write(Element element) throws Exception {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setTrimText(false);

        XMLWriter xmlWriter = new XMLWriter(writer, format);
        xmlWriter.startDocument();
        xmlWriter.write(element);
        xmlWriter.close();
    }

    public void close() throws Exception {
        writer.close();
    }

    public Element toElement(PenroseStudioConfig config) {

        Element element = new DefaultElement("config");

        Collection projects = config.getProjectConfigs();
        for (Iterator i=projects.iterator(); i.hasNext(); ) {
            ProjectConfig projectConfig = (ProjectConfig)i.next();
            element.add(toElement(projectConfig));
        }

        return element;
    }

    public Element toElement(ProjectConfig projectConfig) {

        Element element = new DefaultElement("project");

        element.addAttribute("name", projectConfig.getName());
        element.addAttribute("type", projectConfig.getType());
        element.addAttribute("host", projectConfig.getHost());
        if (projectConfig.getPort() > 0) element.addAttribute("port", ""+projectConfig.getPort());
        element.addAttribute("username", projectConfig.getUsername());
        element.addAttribute("password", projectConfig.getPassword());

        return element;
    }
}
