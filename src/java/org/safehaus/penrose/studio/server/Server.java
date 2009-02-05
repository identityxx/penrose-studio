package org.safehaus.penrose.studio.server;

import org.safehaus.penrose.util.FileUtil;
import org.safehaus.penrose.client.PenroseClient;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import java.io.File;

/**
 * @author Endi Sukma Dewata
 */
public class Server {

    public Logger log = Logger.getLogger(getClass());
    public boolean debug = log.isDebugEnabled();

    protected ServerConfig serverConfig;

    protected PenroseClient client;
    protected File workDir;

    boolean connected;

    public Server(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public String getName() {
        return serverConfig.getName();
    }

    public void connect(IProgressMonitor monitor) throws Exception {
        try {
            monitor.beginTask("Opening " + serverConfig.getName() + "...", IProgressMonitor.UNKNOWN);
    
            log.debug("-------------------------------------------------------------------------------------");

            log.debug("Opening "+ serverConfig.getName()+"...");

            client = new PenroseClient(
                    serverConfig.getType(),
                    serverConfig.getHost(),
                    serverConfig.getPort(),
                    serverConfig.getUsername(),
                    serverConfig.getPassword()
            );

            client.connect();

            File userHome = new File(System.getProperty("user.home"));
            File homeDir = new File(userHome, ".penrose");
            homeDir.mkdirs();

            workDir = new File(homeDir, "Servers"+File.separator+ serverConfig.getName());
            FileUtil.delete(workDir);

            monitor.worked(1);

            connected = true;

        } finally {
            monitor.done();
        }
    }

    public void close() throws Exception {

        log.debug("-------------------------------------------------------------------------------------");
        log.debug("Closing "+ serverConfig.getName()+".");

        connected = false;
        client.close();
    }

    public boolean isConnected() {
        return connected;
    }
    
    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public PenroseClient getClient() {
        return client;
    }

    public void setClient(PenroseClient client) {
        this.client = client;
    }

    public File getWorkDir() {
        return workDir;
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    public void upload() throws Exception {
        client.upload(workDir);
    }

    public void upload(String path) throws Exception {
        log.debug("----------------------------------------------------------------------------------------------");
        client.upload(workDir, path);
    }

    public void download(String path) throws Exception {
        log.debug("----------------------------------------------------------------------------------------------");
        client.download(workDir, path);
    }

    public void removeDirectory(String path) throws Exception {
        client.removeDirectory(path);
    }

    public int hashCode() {
        return serverConfig == null ? 0 : serverConfig.hashCode();
    }

    boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 != null) return o1.equals(o2);
        return o2.equals(o1);
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null) return false;
        if (object.getClass() != this.getClass()) return false;

        Server ei = (Server)object;
        if (!equals(serverConfig, ei.serverConfig)) return false;

        return true;
    }
}
