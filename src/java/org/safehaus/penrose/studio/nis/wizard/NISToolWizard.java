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
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.partition.PartitionContext;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.partition.PartitionConfig;
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
    private ConnectionConfig connectionConfig;

    public ConnectionDriverPage driverPage;
    public JDBCConnectionWizardPage jdbcPage;
    public NISDatabaseWizardPage databasePage;

    public NISToolWizard() {
        setWindowTitle("NIS Tool");
    }

    public boolean canFinish() {
        if (!driverPage.isPageComplete()) return false;
        if (!jdbcPage.isPageComplete()) return false;
        if (!databasePage.isPageComplete()) return false;

        return true;
    }

    public void addPages() {

        driverPage = new ConnectionDriverPage();
        driverPage.setDescription("Select the type of the database.");
        driverPage.removeDriver("LDAP");

        jdbcPage = new JDBCConnectionWizardPage();
        jdbcPage.setDescription("Enter database connection parameters.");
        jdbcPage.setConnectionConfig(connectionConfig);
        jdbcPage.setShowDatabase(false);

        databasePage = new NISDatabaseWizardPage();
        databasePage.setShowLoadOption(false);

        addPage(driverPage);
        addPage(jdbcPage);
        addPage(databasePage);
    }

    public boolean performFinish() {
        try {
            Map<String,String> parameters = jdbcPage.getParameters();
            connectionConfig.setParameters(parameters);

            Map<String,String> allParameters = jdbcPage.getAllParameters();
            String url = allParameters.get(JDBCClient.URL);
            url = Helper.replace(url, allParameters);
            connectionConfig.setParameter(JDBCClient.URL, url);

            log.debug("Creating partition "+ NISTool.NIS_PARTITION_NAME+".");

            File sampleDir = new File(project.getWorkDir(), "samples/"+NISTool.NIS_PARTITION_NAME);
            File partitionDir = new File(project.getWorkDir(), "partitions/"+NISTool.NIS_PARTITION_NAME);
            FileUtil.copy(sampleDir, partitionDir);

            PartitionConfigs partitionConfigs = project.getPartitionConfigs();
            partitionConfigs.addPartitionConfig(partitionConfig);
            project.save(partitionConfig);

            log.debug("Initializing partition "+NISTool.NIS_PARTITION_NAME+".");

            PenroseConfig penroseConfig = project.getPenroseConfig();
            PenroseContext penroseContext = project.getPenroseContext();

            PartitionContext partitionContext = new PartitionContext();
            partitionContext.setPenroseConfig(penroseConfig);
            partitionContext.setPenroseContext(penroseContext);

            Partition partition = new Partition();
            partition.init(partitionConfig, partitionContext);

            if (databasePage.isCreate()) {
                log.debug("Creating database tables in "+NISTool.NIS_PARTITION_NAME+".");

                Connection connection = partition.getConnection(NISTool.NIS_CONNECTION_NAME);
                connection.start();

                JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
                JDBCClient client = adapter.getClient();
                client.createDatabase(NISTool.NIS_PARTITION_NAME);

                SourceConfigs sourceConfigs = partitionConfig.getSourceConfigs();
                for (SourceConfig sourceConfig : sourceConfigs.getSourceConfigs()) {
                    client.createTable(sourceConfig);
                }

                connection.stop();
            }

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public IWizardPage getNextPage(IWizardPage page) {
        try {
            if (page == driverPage) {
                Driver driver = (Driver)driverPage.getDriver().clone();
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

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }
}
