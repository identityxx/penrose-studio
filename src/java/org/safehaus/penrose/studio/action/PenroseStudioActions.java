package org.safehaus.penrose.studio.action;

import org.safehaus.penrose.studio.server.action.*;
import org.safehaus.penrose.studio.partition.action.NewPartitionAction;
import org.safehaus.penrose.studio.partition.action.ImportPartitionAction;
import org.safehaus.penrose.studio.partition.action.NewLDAPSnapshotPartitionAction;
import org.safehaus.penrose.studio.partition.action.NewLDAPProxyPartitionAction;
import org.safehaus.penrose.studio.schema.action.NewSchemaAction;
import org.safehaus.penrose.studio.schema.action.ImportSchemaAction;
import org.safehaus.penrose.studio.service.action.NewServiceAction;
import org.safehaus.penrose.studio.object.ObjectsAction;
import org.safehaus.penrose.studio.validation.ValidationAction;
import org.safehaus.penrose.studio.console.ConsoleAction;
import org.safehaus.penrose.studio.welcome.action.WelcomeAction;
import org.safehaus.penrose.studio.welcome.action.ShowCommercialFeaturesAction;
import org.safehaus.penrose.studio.welcome.action.EnterLicenseKeyAction;
import org.safehaus.penrose.studio.welcome.action.AboutAction;
import org.safehaus.penrose.studio.PenroseStudio;

/**
 * @author Endi S. Dewata
 */
public class PenroseStudioActions {

    private NewAction newAction;
    private OpenAction openAction;
    private CloseAction closeAction;
    private CloseAllAction closeAllAction;

    private SaveAction saveAction;
    private UploadAction uploadAction;
    private ExitAction exitAction;

    private CutAction cutAction;
    private CopyAction copyAction;
    private PasteAction pasteAction;
    private DeleteAction deleteAction;
    private PropertiesAction propertiesAction;

    private NewPartitionAction newPartitionAction;
    private ImportPartitionAction importPartitionAction;
    private NewLDAPSnapshotPartitionAction newLDAPSnapshotPartitionAction;
    private NewLDAPProxyPartitionAction newLDAPProxyPartitionAction;

    private NewSchemaAction newSchemaAction;
    private ImportSchemaAction importSchemaAction;

    private NewServiceAction newServiceAction;

    private ObjectsAction objectsAction;
    private ValidationAction validationAction;
    private ConsoleAction consoleAction;

    private BrowserAction browserAction;
    private PreviewAction previewAction;
    private RestartAction restartAction;

    private WelcomeAction welcomeAction;

    private ShowCommercialFeaturesAction showCommercialFeaturesAction;
    private EnterLicenseKeyAction enterLicenseKeyAction;

    private AboutAction aboutAction;

    public PenroseStudioActions(PenroseStudio penroseStudio) {

        // File
        newAction = new NewAction();

        openAction = new OpenAction();
        penroseStudio.addChangeListener(openAction);
        penroseStudio.addSelectionListener(openAction);

        closeAction = new CloseAction();
        penroseStudio.addChangeListener(closeAction);
        penroseStudio.addSelectionListener(closeAction);

        closeAllAction = new CloseAllAction();

        saveAction = new SaveAction();
        penroseStudio.addChangeListener(saveAction);
        penroseStudio.addSelectionListener(saveAction);

        uploadAction = new UploadAction();
        penroseStudio.addChangeListener(uploadAction);
        penroseStudio.addSelectionListener(uploadAction);

        restartAction = new RestartAction();
        penroseStudio.addChangeListener(restartAction);
        penroseStudio.addSelectionListener(restartAction);

        exitAction = new ExitAction();

        // Edit
        cutAction = new CutAction();
        penroseStudio.addChangeListener(cutAction);
        penroseStudio.addSelectionListener(cutAction);

        copyAction = new CopyAction();
        penroseStudio.addChangeListener(copyAction);
        penroseStudio.addSelectionListener(copyAction);

        pasteAction = new PasteAction();
        penroseStudio.addChangeListener(pasteAction);
        penroseStudio.addSelectionListener(pasteAction);

        deleteAction = new DeleteAction();
        penroseStudio.addChangeListener(deleteAction);
        penroseStudio.addSelectionListener(deleteAction);

        propertiesAction = new PropertiesAction();
        penroseStudio.addChangeListener(propertiesAction);
        penroseStudio.addSelectionListener(propertiesAction);

        // Partition
        newPartitionAction = new NewPartitionAction();
        importPartitionAction = new ImportPartitionAction();
        newLDAPSnapshotPartitionAction = new NewLDAPSnapshotPartitionAction();
        newLDAPProxyPartitionAction = new NewLDAPProxyPartitionAction();

        // Schema
        newSchemaAction = new NewSchemaAction();
        importSchemaAction = new ImportSchemaAction();

        // Service
        newServiceAction = new NewServiceAction();

        // Tools
        browserAction = new BrowserAction();
        penroseStudio.addChangeListener(browserAction);
        penroseStudio.addSelectionListener(browserAction);

        previewAction = new PreviewAction();
        penroseStudio.addChangeListener(previewAction);
        penroseStudio.addSelectionListener(previewAction);

        // Window
        objectsAction = new ObjectsAction();
        validationAction = new ValidationAction();
        consoleAction = new ConsoleAction();

        // Help
        welcomeAction = new WelcomeAction();
        showCommercialFeaturesAction = new ShowCommercialFeaturesAction();
        enterLicenseKeyAction = new EnterLicenseKeyAction();
        aboutAction = new AboutAction();
    }

    public void setConnected(boolean connected) {
        //openAction.setEnabled(!connected);
        //closeAction.setEnabled(connected);
        //saveAction.setEnabled(connected);
        uploadAction.setEnabled(connected);
        //deleteAction.setEnabled(!connected);

        newPartitionAction.setEnabled(connected);
        importPartitionAction.setEnabled(connected);
        newLDAPSnapshotPartitionAction.setEnabled(connected);
        newLDAPProxyPartitionAction.setEnabled(connected);

        newSchemaAction.setEnabled(connected);
        importSchemaAction.setEnabled(connected);

        newServiceAction.setEnabled(connected);

        browserAction.setEnabled(connected);
        previewAction.setEnabled(connected);
        restartAction.setEnabled(connected);
    }

    public NewAction getNewAction() {
        return newAction;
    }

    public void setNewAction(NewAction newAction) {
        this.newAction = newAction;
    }

    public OpenAction getOpenAction() {
        return openAction;
    }

    public void setOpenAction(OpenAction openAction) {
        this.openAction = openAction;
    }

    public CloseAction getCloseAction() {
        return closeAction;
    }

    public void setCloseAction(CloseAction closeAction) {
        this.closeAction = closeAction;
    }

    public CloseAllAction getCloseAllAction() {
        return closeAllAction;
    }

    public void setCloseAllAction(CloseAllAction closeAllAction) {
        this.closeAllAction = closeAllAction;
    }

    public SaveAction getSaveAction() {
        return saveAction;
    }

    public void setSaveAction(SaveAction saveAction) {
        this.saveAction = saveAction;
    }

    public UploadAction getUploadAction() {
        return uploadAction;
    }

    public void setUploadAction(UploadAction uploadAction) {
        this.uploadAction = uploadAction;
    }

    public ExitAction getExitAction() {
        return exitAction;
    }

    public void setExitAction(ExitAction exitAction) {
        this.exitAction = exitAction;
    }

    public CopyAction getCopyAction() {
        return copyAction;
    }

    public void setCopyAction(CopyAction copyAction) {
        this.copyAction = copyAction;
    }

    public PasteAction getPasteAction() {
        return pasteAction;
    }

    public void setPasteAction(PasteAction pasteAction) {
        this.pasteAction = pasteAction;
    }

    public DeleteAction getDeleteAction() {
        return deleteAction;
    }

    public void setDeleteAction(DeleteAction deleteAction) {
        this.deleteAction = deleteAction;
    }

    public NewPartitionAction getNewPartitionAction() {
        return newPartitionAction;
    }

    public void setNewPartitionAction(NewPartitionAction newPartitionAction) {
        this.newPartitionAction = newPartitionAction;
    }

    public ImportPartitionAction getImportPartitionAction() {
        return importPartitionAction;
    }

    public void setImportPartitionAction(ImportPartitionAction importPartitionAction) {
        this.importPartitionAction = importPartitionAction;
    }

    public NewLDAPSnapshotPartitionAction getNewLDAPSnapshotPartitionAction() {
        return newLDAPSnapshotPartitionAction;
    }

    public void setNewLDAPSnapshotPartitionAction(NewLDAPSnapshotPartitionAction newLDAPSnapshotPartitionAction) {
        this.newLDAPSnapshotPartitionAction = newLDAPSnapshotPartitionAction;
    }

    public NewLDAPProxyPartitionAction getNewLDAPProxyPartitionAction() {
        return newLDAPProxyPartitionAction;
    }

    public void setNewLDAPProxyPartitionAction(NewLDAPProxyPartitionAction newLDAPProxyPartitionAction) {
        this.newLDAPProxyPartitionAction = newLDAPProxyPartitionAction;
    }

    public NewSchemaAction getNewSchemaAction() {
        return newSchemaAction;
    }

    public void setNewSchemaAction(NewSchemaAction newSchemaAction) {
        this.newSchemaAction = newSchemaAction;
    }

    public ImportSchemaAction getImportSchemaAction() {
        return importSchemaAction;
    }

    public void setImportSchemaAction(ImportSchemaAction importSchemaAction) {
        this.importSchemaAction = importSchemaAction;
    }

    public NewServiceAction getNewServiceAction() {
        return newServiceAction;
    }

    public void setNewServiceAction(NewServiceAction newServiceAction) {
        this.newServiceAction = newServiceAction;
    }

    public ObjectsAction getObjectsAction() {
        return objectsAction;
    }

    public void setObjectsAction(ObjectsAction objectsAction) {
        this.objectsAction = objectsAction;
    }

    public ValidationAction getValidationAction() {
        return validationAction;
    }

    public void setValidationAction(ValidationAction validationAction) {
        this.validationAction = validationAction;
    }

    public ConsoleAction getConsoleAction() {
        return consoleAction;
    }

    public void setConsoleAction(ConsoleAction consoleAction) {
        this.consoleAction = consoleAction;
    }

    public BrowserAction getBrowserAction() {
        return browserAction;
    }

    public void setBrowserAction(BrowserAction browserAction) {
        this.browserAction = browserAction;
    }

    public PreviewAction getPreviewAction() {
        return previewAction;
    }

    public void setPreviewAction(PreviewAction previewAction) {
        this.previewAction = previewAction;
    }

    public RestartAction getRestartAction() {
        return restartAction;
    }

    public void setRestartAction(RestartAction restartAction) {
        this.restartAction = restartAction;
    }

    public WelcomeAction getWelcomeAction() {
        return welcomeAction;
    }

    public void setWelcomeAction(WelcomeAction welcomeAction) {
        this.welcomeAction = welcomeAction;
    }

    public ShowCommercialFeaturesAction getShowCommercialFeaturesAction() {
        return showCommercialFeaturesAction;
    }

    public void setShowCommercialFeaturesAction(ShowCommercialFeaturesAction showCommercialFeaturesAction) {
        this.showCommercialFeaturesAction = showCommercialFeaturesAction;
    }

    public EnterLicenseKeyAction getEnterLicenseKeyAction() {
        return enterLicenseKeyAction;
    }

    public void setEnterLicenseKeyAction(EnterLicenseKeyAction enterLicenseKeyAction) {
        this.enterLicenseKeyAction = enterLicenseKeyAction;
    }

    public AboutAction getAboutAction() {
        return aboutAction;
    }

    public void setAboutAction(AboutAction aboutAction) {
        this.aboutAction = aboutAction;
    }

    public CutAction getCutAction() {
        return cutAction;
    }

    public void setCutAction(CutAction cutAction) {
        this.cutAction = cutAction;
    }

    public PropertiesAction getPropertiesAction() {
        return propertiesAction;
    }

    public void setPropertiesAction(PropertiesAction propertiesAction) {
        this.propertiesAction = propertiesAction;
    }
}
