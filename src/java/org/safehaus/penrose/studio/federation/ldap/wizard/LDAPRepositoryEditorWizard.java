package org.safehaus.penrose.studio.federation.ldap.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.jndi.connection.JNDIConnectionInfoWizardPage;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * @author Endi S. Dewata
 */
public class LDAPRepositoryEditorWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());


    private Project project;

    public JNDIConnectionInfoWizardPage ldapPage;

    private Map<String,String> parameters = new LinkedHashMap<String,String>();
    private String suffix;

    public LDAPRepositoryEditorWizard() {
    }

    public boolean canFinish() {
        if (!ldapPage.isPageComplete()) return false;

        return true;
    }

    public void addPages() {

        ldapPage = new JNDIConnectionInfoWizardPage();
        ldapPage.setDescription("Enter LDAP connection parameters.");
        ldapPage.setParameters(parameters);
        ldapPage.setSuffix(suffix);
        addPage(ldapPage);
    }

    public boolean performFinish() {
        try {
            setSuffix(ldapPage.getSuffix());
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
}
