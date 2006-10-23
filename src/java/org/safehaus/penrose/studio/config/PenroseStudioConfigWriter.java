package org.safehaus.penrose.studio.config;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.safehaus.penrose.studio.server.ServerConfig;

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

    public void write(ServerConfig serverConfig) throws Exception {
        write(toElement(serverConfig));
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

        Collection projects = config.getServerConfigs();
        for (Iterator i=projects.iterator(); i.hasNext(); ) {
            ServerConfig serverConfig = (ServerConfig)i.next();
            element.add(toElement(serverConfig));
        }

        return element;
    }

    public Element toElement(ServerConfig serverConfig) {

        Element element = new DefaultElement("project");

        element.addAttribute("name", serverConfig.getName());
        element.addAttribute("type", serverConfig.getType());
        element.addAttribute("host", serverConfig.getHostname());
        if (serverConfig.getPort() > 0) element.addAttribute("port", ""+serverConfig.getPort());
        element.addAttribute("username", serverConfig.getUsername());
        element.addAttribute("password", serverConfig.getPassword());

        return element;
    }
}
