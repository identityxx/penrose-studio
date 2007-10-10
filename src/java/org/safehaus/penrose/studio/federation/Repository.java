package org.safehaus.penrose.studio.federation;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collection;

/**
 * @author Endi Sukma Dewata
 */
public class Repository {

    protected String name;
    protected String type;

    protected Map<String,String> parameters = new LinkedHashMap<String,String>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Collection<String> getParameterNames() {
        return parameters.keySet();
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        if (parameters == this.parameters) return;
        this.parameters.clear();
        this.parameters.putAll(parameters);
    }

    public int hashCode() {
        return (name == null ? 0 : name.hashCode()) +
                (type == null ? 0 : type.hashCode()) +
                (parameters == null ? 0 : parameters.hashCode());
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

        Repository domain = (Repository)object;
        if (!equals(name, domain.name)) return false;
        if (!equals(type, domain.type)) return false;
        if (!equals(parameters, domain.parameters)) return false;

        return true;
    }
}
