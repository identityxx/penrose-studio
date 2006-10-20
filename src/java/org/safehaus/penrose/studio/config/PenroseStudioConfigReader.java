package org.safehaus.penrose.studio.config;

import org.apache.commons.digester.Digester;
import org.safehaus.penrose.studio.server.ServerConfig;

import java.io.File;

/**
 * @author Endi S. Dewata
 */
public class PenroseStudioConfigReader {

    File file;

    public PenroseStudioConfigReader(File file) {
        this.file = file;
    }

    public PenroseStudioConfig read() throws Exception {

        PenroseStudioConfig config = new PenroseStudioConfig();

        Digester digester = new Digester();

        digester.addObjectCreate("config/project", ServerConfig.class);
        digester.addSetProperties("config/project");
        digester.addSetNext("config/project", "addServerConfig");

        digester.addObjectCreate("config/server", ServerConfig.class);
        digester.addSetProperties("config/server");
        digester.addSetNext("config/server", "addServerConfig");

        digester.setValidating(false);
        digester.setClassLoader(getClass().getClassLoader());
        digester.push(config);
        digester.parse(file);

        return config;
    }
}
