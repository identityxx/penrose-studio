package org.safehaus.penrose.studio.federation.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.connection.wizard.ConnectionDriverPage;
import org.safehaus.penrose.studio.jdbc.connection.JDBCConnectionWizardPage;
import org.safehaus.penrose.studio.driver.Driver;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Endi S. Dewata
 */
public class FederationWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;

    public ConnectionDriverPage dbDriverPage;
    public JDBCConnectionWizardPage jdbcPage;

    private Map<String,String> allParameters = new HashMap<String,String>();
    private Map<String,String> parameters = new HashMap<String,String>();

    public FederationWizard() {
        setWindowTitle("Setup Wizard");
    }

    public void init(Project project) throws Exception {
        this.project = project;
    }

    public boolean canFinish() {
        if (!dbDriverPage.isPageComplete()) return false;
        if (!jdbcPage.isPageComplete()) return false;

        return true;
    }

    public void addPages() {

        dbDriverPage = new ConnectionDriverPage();
        dbDriverPage.setDescription("Select the type of the database.");
        dbDriverPage.removeDriver("LDAP");
        addPage(dbDriverPage);

        jdbcPage = new JDBCConnectionWizardPage();
        jdbcPage.setDescription("Enter database connection parameters.");
        jdbcPage.setShowDatabase(false);
        addPage(jdbcPage);
    }

    public boolean performFinish() {
        try {
            allParameters = jdbcPage.getAllParameters();
            parameters = jdbcPage.getParameters();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public IWizardPage getNextPage(IWizardPage page) {
        try {
            if (page == dbDriverPage) {
                Driver driver = (Driver) dbDriverPage.getDriver().clone();
                driver.removeParameter("database");
                jdbcPage.setDriver(driver);
            }
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return super.getNextPage(page);
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

    public Map<String, String> getAllParameters() {
        return allParameters;
    }

    public void setAllParameters(Map<String, String> allParameters) {
        this.allParameters = allParameters;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
