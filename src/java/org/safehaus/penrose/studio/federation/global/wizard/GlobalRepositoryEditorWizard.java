package org.safehaus.penrose.studio.federation.global.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.federation.repository.GlobalRepository;
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.studio.ldap.connection.LDAPConnectionWizardPage;

import javax.naming.Context;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Endi S. Dewata
 */
public class GlobalRepositoryEditorWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    LDAPConnectionWizardPage connectionPage;

    private Map<String,String> parameters = new LinkedHashMap<String,String>();
    private String suffix;

    GlobalRepository repository;

    public GlobalRepositoryEditorWizard(GlobalRepository repository) {
        this.repository = repository;
        setWindowTitle("Global Repository");
    }

    public void addPages() {

        connectionPage = new LDAPConnectionWizardPage();

        if (repository != null) {
            connectionPage.setSuffix(repository.getSuffix());

            connectionPage.setProviderUrl(repository.getUrl());
            connectionPage.setBindDn(repository.getUser());
            connectionPage.setBindPassword(repository.getPassword());
        }

        addPage(connectionPage);
    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {
            setSuffix(connectionPage.getSuffix());
            setParameters(connectionPage.getParameters());

            if (repository == null) {
                repository = new GlobalRepository();
                repository.setName(Federation.GLOBAL);
                repository.setType("GLOBAL");
            }

            repository.setUrl(connectionPage.getProviderUrl());
            repository.setUser(connectionPage.getBindDn());
            repository.setPassword(connectionPage.getBindPassword());
            repository.setSuffix(connectionPage.getSuffix());

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        if (this.parameters == parameters) return;
        this.parameters.clear();
        this.parameters.putAll(parameters);
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public GlobalRepository getRepository() {
        return repository;
    }
}