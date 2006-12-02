package org.safehaus.penrose.studio.service.editor;

import org.apache.log4j.Logger;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.safehaus.penrose.service.ServiceConfig;

/**
 * @author Endi S. Dewata
 */
public class ServiceEditorPage extends FormPage {

    public Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    ServiceConfig serviceConfig;

    public ServiceEditorPage(ServiceEditor editor, String name, String label) {
        super(editor, name, label);

        serviceConfig = editor.getServiceConfig();
    }

    public void createFormContent(IManagedForm managedForm) {

        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText(serviceConfig.getName());
    }

    public void setActive(boolean b) {
        super.setActive(b);
        if (b) refresh();
    }

    public void refresh() {
    }

    public FormToolkit getToolkit() {
        return toolkit;
    }

    public void setToolkit(FormToolkit toolkit) {
        this.toolkit = toolkit;
    }

    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    public void setServiceConfig(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    public void checkDirty() {
        ServiceEditor editor = (ServiceEditor)getEditor();
        editor.checkDirty();
    }
}
