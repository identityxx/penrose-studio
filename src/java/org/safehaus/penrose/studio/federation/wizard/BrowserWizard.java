package org.safehaus.penrose.studio.federation.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.apache.log4j.Logger;
import org.safehaus.penrose.management.PartitionClient;
import org.safehaus.penrose.ldap.DN;

/**
 * @author Endi Sukma Dewata
 */
public class BrowserWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private DN baseDn;
    private PartitionClient partitionClient;

    BrowserPage browserPage;

    private String dn;

    public BrowserWizard() {
        setWindowTitle("Browser Wizard");
    }

    public void addPages() {
        browserPage = new BrowserPage();
        browserPage.setBaseDn(baseDn);
        browserPage.setPartitionClient(partitionClient);
        browserPage.setDn(dn);
        addPage(browserPage);
    }

    public boolean performFinish() {
        try {
            dn = browserPage.getDn();
            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public DN getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(DN baseDn) {
        this.baseDn = baseDn;
    }

    public PartitionClient getPartitionClient() {
        return partitionClient;
    }

    public void setPartitionClient(PartitionClient partitionClient) {
        this.partitionClient = partitionClient;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }
}
