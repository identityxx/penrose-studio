package org.safehaus.penrose.studio.federation.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.jndi.connection.JNDIConnectionInfoWizardPage;
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.connection.ConnectionConfig;

import java.io.File;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * @author Endi S. Dewata
 */
public class GlobalRepositoryWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());


    private Federation federation;
    private Project project;

    public JNDIConnectionInfoWizardPage ldapPage;

    private Map<String,String> parameters = new LinkedHashMap<String,String>();

    public GlobalRepositoryWizard() {
        setWindowTitle("Global Repository Setup Wizard");
    }

    public void init(Federation federation) throws Exception {
        this.federation = federation;
        this.project = federation.getProject();
    }

    public boolean canFinish() {
        if (!ldapPage.isPageComplete()) return false;

        return true;
    }

    public void addPages() {

        ldapPage = new JNDIConnectionInfoWizardPage();
        ldapPage.setDescription("Enter LDAP connection parameters.");
        ldapPage.setParameters(parameters);
        addPage(ldapPage);
    }

    public boolean performFinish() {
        try {
            setParameters(ldapPage.getParameters());

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public Federation getFederation() {
        return federation;
    }

    public void setFederation(Federation federation) {
        this.federation = federation;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        if (this.parameters == parameters) return;
        this.parameters.clear();
        this.parameters.putAll(parameters);
    }
}
