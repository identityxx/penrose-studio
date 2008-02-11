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
package org.safehaus.penrose.studio.federation;

import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.ldap.DNBuilder;
import org.safehaus.penrose.ldap.RDNBuilder;
import org.safehaus.penrose.studio.federation.ldap.LDAPRepository;
import org.safehaus.penrose.studio.federation.nis.NISDomain;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Endi S. Dewata
 */
public class FederationConfig implements Serializable, Cloneable {

    protected Map<String, RepositoryConfig> repositoryConfigs = new LinkedHashMap<String, RepositoryConfig>();

    public FederationConfig() {
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

        FederationConfig federationConfig = (FederationConfig)object;

        if (!equals(repositoryConfigs, federationConfig.repositoryConfigs)) return false;

        return true;
    }

    public Object clone() throws CloneNotSupportedException {
        FederationConfig federationConfig = (FederationConfig)super.clone();

        federationConfig.repositoryConfigs = new LinkedHashMap<String, RepositoryConfig>();
        for (RepositoryConfig repository : repositoryConfigs.values()) {
            federationConfig.addRepositoryConfig((RepositoryConfig) repository.clone());
        }

        return federationConfig;
    }

    public Collection<RepositoryConfig> getRepositoryConfigs() {
        return repositoryConfigs.values();
    }

    public RepositoryConfig getRepositoryConfig(String name) {
        return repositoryConfigs.get(name);
    }

    public RepositoryConfig removeRepositoryConfig(String name) {
        return repositoryConfigs.remove(name);
    }

    public void addRepositoryConfig(RepositoryConfig repository) {
        String name = repository.getName();
        String type = repository.getType();

        if ("GLOBAL".equals(type)) {
            repository = new GlobalRepository(repository);

        } else if ("LDAP".equals(type)) {
            repository = new LDAPRepository(repository);

        } else if ("NIS".equals(type)) {
            repository = new NISDomain(repository);

            Map<String,String> parameters = repository.getParameters();

            String ypSuffix = parameters.get("ypSuffix");
            if (ypSuffix == null) {

                DN suffix = new DN(parameters.get("suffix"));

                DNBuilder db = new DNBuilder();
                db.append(suffix.get(0));
                db.append("ou=yp");
                db.append(suffix.getSuffix(2));
                ypSuffix = db.toString();

                parameters.put("ypSuffix", ypSuffix);
            }
        }

        repositoryConfigs.put(name, repository);
    }

    public Collection<String> getRepositoryNames() {
        return repositoryConfigs.keySet();
    }
}