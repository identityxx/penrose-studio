package org.safehaus.penrose.studio.federation.partition;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.studio.ldap.connection.LDAPConnectionWizardPage;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.connection.ConnectionClient;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.client.PenroseClient;

import javax.naming.Context;

/**
 * @author Endi S. Dewata
 */
public class FederationDomainEditorWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    LDAPConnectionWizardPage connectionPage;
    //LDAPPartitionsWizardPage partitionsPage;

    PenroseClient client;
    PartitionClient partitionClient;
    FederationClient federationClient;
    FederationRepositoryConfig repository;

    public FederationDomainEditorWizard(FederationClient federationClient) {
        this.federationClient = federationClient;
        this.client = federationClient.getClient();
        this.partitionClient = federationClient.getPartitionClient();

        setWindowTitle(federationClient.getName());
    }

    public void addPages() {

        connectionPage = new LDAPConnectionWizardPage();
        //partitionsPage = new LDAPPartitionsWizardPage();

        try {
            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

            ConnectionClient connectionClient = connectionManagerClient.getConnectionClient("LDAP");
            ConnectionConfig connectionConfig = connectionClient.getConnectionConfig();

            SourceClient sourceClient = sourceManagerClient.getSourceClient("LDAP");
            SourceConfig sourceConfig = sourceClient.getSourceConfig();

            String url = connectionConfig.getParameter(Context.PROVIDER_URL);
            String suffix = sourceConfig.getParameter("baseDn");
            String bindDn = connectionConfig.getParameter(Context.SECURITY_PRINCIPAL);
            String bindPassword = connectionConfig.getParameter(Context.SECURITY_CREDENTIALS);

            connectionPage.setProviderUrl(url);
            connectionPage.setSuffix(suffix);
            connectionPage.setBindDn(bindDn);
            connectionPage.setBindPassword(bindPassword);

            //partitionsPage.setSuffix(repository.getParameter(GlobalRepository.SUFFIX));
            //partitionsPage.setTemplate(repository.getParameter(GlobalRepository.TEMPLATE));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        addPage(connectionPage);
        //addPage(partitionsPage);
    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        //if (!partitionsPage.isPageComplete()) return false;
        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (connectionPage == page) {
            if (repository == null) {
                String suffix = connectionPage.getSuffix();
                //partitionsPage.setSuffix(suffix);
            }
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            String name = federationClient.getName();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
/*
            if (repository == null) {
                repository = new FederationRepositoryConfig();
                repository.setName(FederationClient.GLOBAL);
                repository.setType("GLOBAL");
            }

            repository.setParameter(GlobalRepository.URL, connectionPage.getProviderUrl());
            repository.setParameter(GlobalRepository.SUFFIX, connectionPage.getSuffix());
            repository.setParameter(GlobalRepository.USER, connectionPage.getBindDn());
            repository.setParameter(GlobalRepository.PASSWORD, connectionPage.getBindPassword());
*/
            log.debug("Getting existing configuration.");

            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

            ConnectionClient connectionClient = connectionManagerClient.getConnectionClient("LDAP");
            ConnectionConfig connectionConfig = connectionClient.getConnectionConfig();

            SourceClient sourceClient = sourceManagerClient.getSourceClient("LDAP");
            SourceConfig sourceConfig = sourceClient.getSourceConfig();

            String url = connectionPage.getProviderUrl();
            String suffix = connectionPage.getSuffix();
            String bindDn = connectionPage.getBindDn();
            String bindPassword = connectionPage.getBindPassword();

            if (url == null) {
                connectionConfig.removeParameter(Context.PROVIDER_URL);
            } else {
                connectionConfig.setParameter(Context.PROVIDER_URL, url);
            }

            if (suffix == null) {
                sourceConfig.removeParameter("baseDn");
            } else {
                sourceConfig.setParameter("baseDn", suffix);
            }

            if (bindDn == null) {
                connectionConfig.removeParameter(Context.SECURITY_PRINCIPAL);
            } else {
                connectionConfig.setParameter(Context.SECURITY_PRINCIPAL, bindDn);
            }

            if (bindPassword == null) {
                connectionConfig.removeParameter(Context.SECURITY_CREDENTIALS);
            } else {
                connectionConfig.setParameter(Context.SECURITY_CREDENTIALS, bindPassword);
            }

            //repository.setParameter(GlobalRepository.SUFFIX, partitionsPage.getSuffix());
            //repository.setParameter(GlobalRepository.TEMPLATE, partitionsPage.getTemplate());

            connectionClient.setConnectionConfig(connectionConfig);
            sourceClient.setSourceConfig(sourceConfig);
            partitionManagerClient.storePartition(name);

            partitionManagerClient.stopPartition(name);
            //partitionManagerClient.unloadPartition(name);
            //partitionManagerClient.loadPartition(name);
            partitionManagerClient.startPartition(name);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }
}