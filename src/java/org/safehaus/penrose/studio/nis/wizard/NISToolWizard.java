package org.safehaus.penrose.studio.nis.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.connection.wizard.ConnectionDriverPage;
import org.safehaus.penrose.studio.jdbc.connection.JDBCConnectionWizardPage;
import org.safehaus.penrose.studio.driver.Driver;
import org.safehaus.penrose.studio.util.Helper;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.studio.nis.NISTool;
import org.safehaus.penrose.studio.jndi.connection.JNDIConnectionInfoWizardPage;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.source.SourceConfigs;
import org.safehaus.penrose.source.SourceConfig;

import java.util.Map;
import java.io.File;

/**
 * @author Endi S. Dewata
 */
public class NISToolWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;
    private PartitionConfig partitionConfig;
    private ConnectionConfig dbConnectionConfig;
    private ConnectionConfig ldapConnectionConfig;

    public ConnectionDriverPage dbDriverPage;
    public JDBCConnectionWizardPage jdbcPage;
    public JNDIConnectionInfoWizardPage ldapPage;

    public NISToolWizard() {
        setWindowTitle("NIS Tool");
    }

    public boolean canFinish() {
        if (!dbDriverPage.isPageComplete()) return false;
        if (!jdbcPage.isPageComplete()) return false;
        if (!ldapPage.isPageComplete()) return false;

        return true;
    }

    public void addPages() {

        dbDriverPage = new ConnectionDriverPage();
        dbDriverPage.setDescription("Select the type of the database.");
        dbDriverPage.removeDriver("LDAP");

        jdbcPage = new JDBCConnectionWizardPage();
        jdbcPage.setDescription("Enter database connection parameters.");
        jdbcPage.setConnectionConfig(dbConnectionConfig);
        jdbcPage.setShowDatabase(false);

        ldapPage = new JNDIConnectionInfoWizardPage();
        ldapPage.setDescription("Enter LDAP connection parameters.");
        ldapPage.setConnectionConfig(ldapConnectionConfig);
        
        addPage(dbDriverPage);
        addPage(jdbcPage);
        addPage(ldapPage);
    }

    public boolean performFinish() {
        try {
            Map<String,String> allParameters = jdbcPage.getAllParameters();
            String url = allParameters.get(JDBCClient.URL);
            url = Helper.replace(url, allParameters);

            Map<String,String> parameters = jdbcPage.getParameters();
            parameters.put(JDBCClient.URL, url);
            dbConnectionConfig.setParameters(parameters);

            Map<String,String> map = ldapConnectionConfig.getParameters();
            map.putAll(ldapPage.getParameters());
            ldapConnectionConfig.setParameters(map);

            log.debug("Creating partition "+ NISTool.NIS_TOOL +".");

            File sampleDir = new File(project.getWorkDir(), "samples/"+NISTool.NIS_TOOL);
            File partitionDir = new File(project.getWorkDir(), "partitions/"+NISTool.NIS_TOOL);
            FileUtil.copy(sampleDir, partitionDir);

            PartitionConfigs partitionConfigs = project.getPartitionConfigs();
            partitionConfigs.addPartitionConfig(partitionConfig);
            project.save(partitionConfig);

            log.debug("Initializing partition "+NISTool.NIS_TOOL +".");

            PenroseConfig penroseConfig = project.getPenroseConfig();
            PenroseContext penroseContext = project.getPenroseContext();

            PartitionFactory partitionFactory = new PartitionFactory();
            partitionFactory.setPartitionsDir(partitionConfigs.getPartitionsDir());
            partitionFactory.setPenroseConfig(penroseConfig);
            partitionFactory.setPenroseContext(penroseContext);

            Partition partition = partitionFactory.createPartition(partitionConfig);

            log.debug("Creating database tables in "+NISTool.NIS_TOOL +".");

            Connection connection = partition.getConnection(NISTool.NIS_CONNECTION_NAME);

            JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
            JDBCClient client = adapter.getClient();
            try {
                client.createDatabase(NISTool.NIS_TOOL);
            } catch (Exception e) {
                log.debug(e.getMessage());
            }

            SourceConfigs sourceConfigs = partitionConfig.getSourceConfigs();
            for (SourceConfig sourceConfig : sourceConfigs.getSourceConfigs()) {
                try {
                    client.createTable(sourceConfig);
                } catch (Exception e) {
                    log.debug(e.getMessage());
                }
            }

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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ConnectionConfig getDbConnectionConfig() {
        return dbConnectionConfig;
    }

    public void setDbConnectionConfig(ConnectionConfig dbConnectionConfig) {
        this.dbConnectionConfig = dbConnectionConfig;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }

    public ConnectionConfig getLdapConnectionConfig() {
        return ldapConnectionConfig;
    }

    public void setLdapConnectionConfig(ConnectionConfig ldapConnectionConfig) {
        this.ldapConnectionConfig = ldapConnectionConfig;
    }
}
