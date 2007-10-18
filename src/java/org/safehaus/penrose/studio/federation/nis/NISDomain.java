package org.safehaus.penrose.studio.federation.nis;

import org.safehaus.penrose.studio.federation.Repository;

import java.util.Map;

/**
 * @author Endi Sukma Dewata
 */
public class NISDomain extends Repository {
    
    protected String fullName;
    protected String server;

    public NISDomain() {
        setType("NIS");
    }

    public String getUrl() {
        return parameters.get("url");
    }

    public void setUrl(String url) {
        parameters.put("url", url);
        parseUrl();
    }

    public void parseUrl() {
        String url = parameters.get("url");

        int i = url.indexOf("://");
        if (i < 0) throw new RuntimeException("Invalid URL: "+url);

        String protocol = url.substring(0, i);
        if (!"nis".equals(protocol)) throw new RuntimeException("Unknown protocol: "+protocol);

        int j = url.indexOf("/", i+3);
        if (j < 0) throw new RuntimeException("Missing NIS domain name.");

        server = url.substring(i+3, j);
        fullName = url.substring(j+1);
    }

    public void updateUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append("nis://");
        sb.append(server);
        sb.append("/");
        sb.append(fullName);

        parameters.put("url", sb.toString());
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        updateUrl();
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
        updateUrl();
    }

    public String getSuffix() {
        return parameters.get("suffix");
    }

    public void setSuffix(String suffix) {
        parameters.put("suffix", suffix);
    }

    public String getNssSuffix() {
        return parameters.get("nssSuffix");
    }

    public void setNssSuffix(String nssSuffix) {
        parameters.put("nssSuffix", nssSuffix);
    }

    public void setParameters(Map<String,String> parameters) {
        super.setParameters(parameters);
        parseUrl();
    }
}
