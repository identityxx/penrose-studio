package org.safehaus.penrose.studio.federation.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.connection.wizard.ConnectionDriverPage;
import org.safehaus.penrose.studio.jdbc.connection.JDBCConnectionWizardPage;
import org.safehaus.penrose.studio.driver.Driver;
import org.safehaus.penrose.studio.util.Helper;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.studio.federation.Federation;
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
public class FederationWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;
    private PartitionConfig partitionConfig;

    public ConnectionDriverPage dbDriverPage;
    public JDBCConnectionWizardPage jdbcPage;

    public FederationWizard() {
        setWindowTitle("Setup Wizard");
    }

    public void init(Project project) throws Exception {
        this.project = project;

        File workDir = project.getWorkDir();
        File sampleDir = new File(workDir, "samples/"+Federation.PARTITION);

        project.download("samples/"+Federation.PARTITION);

        PartitionConfigs partitionConfigs = project.getPartitionConfigs();
        partitionConfig = partitionConfigs.load(sampleDir);
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

        ConnectionConfig jdbcConfig = partitionConfig.getConnectionConfigs().getConnectionConfig(Federation.JDBC);

        jdbcPage = new JDBCConnectionWizardPage();
        jdbcPage.setDescription("Enter database connection parameters.");
        jdbcPage.setConnectionConfig(jdbcConfig);
        jdbcPage.setShowDatabase(false);
        addPage(jdbcPage);
    }

    public boolean performFinish() {
        try {
            ConnectionConfig jdbcConfig = partitionConfig.getConnectionConfigs().getConnectionConfig(Federation.JDBC);

            Map<String,String> allParameters = jdbcPage.getAllParameters();
            String url = allParameters.get(JDBCClient.URL);
            url = Helper.replace(url, allParameters);

            Map<String,String> parameters = jdbcPage.getParameters();
            parameters.put(JDBCClient.URL, url);
            jdbcConfig.setParameters(parameters);
/*
            log.debug("Creating partition "+Federation.PARTITION +".");

            File sampleDir = new File(project.getWorkDir(), "samples/"+Federation.PARTITION);
            File partitionDir = new File(project.getWorkDir(), "partitions/"+Federation.PARTITION);
            FileUtil.copy(sampleDir, partitionDir);

            PartitionConfigs partitionConfigs = project.getPartitionConfigs();
            partitionConfigs.addPartitionConfig(partitionConfig);
            project.save(partitionConfig);

            log.debug("Initializing partition "+Federation.PARTITION +".");

            PenroseConfig penroseConfig = project.getPenroseConfig();
            PenroseContext penroseContext = project.getPenroseContext();

            PartitionFactory partitionFactory = new PartitionFactory();
            partitionFactory.setPartitionsDir(partitionConfigs.getPartitionsDir());
            partitionFactory.setPenroseConfig(penroseConfig);
            partitionFactory.setPenroseContext(penroseContext);

            Partition partition = partitionFactory.createPartition(partitionConfig);

            log.debug("Creating database tables in "+Federation.PARTITION +".");

            Connection connection = partition.getConnection(Federation.JDBC);

            JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
            JDBCClient client = adapter.getClient();
            try {
                client.createDatabase(Federation.PARTITION);
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
*/
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

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }
}
