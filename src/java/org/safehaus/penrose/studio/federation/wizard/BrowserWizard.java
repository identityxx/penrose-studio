package org.safehaus.penrose.studio.federation.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.apache.log4j.Logger;
import org.safehaus.penrose.source.Source;

/**
 * @author Endi Sukma Dewata
 */
public class BrowserWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private String baseDn;
    private Source source;

    BrowserPage browserPage;

    private String dn;

    public BrowserWizard() {
        setWindowTitle("Browser Wizard");
    }

    public void addPages() {
        browserPage = new BrowserPage();
        browserPage.setBaseDn(baseDn);
        browserPage.setSource(source);
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

    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }
}
