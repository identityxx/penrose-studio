package org.safehaus.penrose.studio.module.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.Shell;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.module.ModulesNode;
import org.safehaus.penrose.studio.module.wizard.ModuleWizard;
import org.safehaus.penrose.studio.PenroseStudio;

public class NewLDAPSyncModuleAction extends Action {

    Logger log = Logger.getLogger(getClass());

    ModulesNode node;

    public NewLDAPSyncModuleAction(ModulesNode node) {
        this.node = node;

        setText("New LDAP Sync Module...");
        setId(getClass().getName());
    }

    public void run() {
        try {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            Shell shell = window.getShell();

            ModuleWizard wizard = new ModuleWizard(node.getPartition());
            WizardDialog dialog = new WizardDialog(shell, wizard);
            dialog.setPageSize(600, 300);
            dialog.open();

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.show(node);
            penroseStudio.fireChangeEvent();

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public boolean isEnabled() {
        return false;
    }
}
