package org.safehaus.penrose.studio.mapping.editor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.schema.SchemaManager;
import org.safehaus.penrose.schema.ObjectClass;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class ObjectClassesDialogPage extends MappingDialogPage {

    Table objectClassesTable;

    public ObjectClassesDialogPage(MappingDialog dialog, Composite parent, int style) {
        super(dialog, parent, style);
    }

    public void init() {
        setLayout(new GridLayout(2, false));

        objectClassesTable = new Table(this, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        objectClassesTable.setLayoutData(gd);
        objectClassesTable.setLayout(new FillLayout());

        Composite buttons = new Composite(this, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ObjectClassSelectionDialog dialog = new ObjectClassSelectionDialog(getShell(), SWT.NONE);
                    dialog.setText("Add object classes...");

                    SchemaManager schemaManager = server.getSchemaManager();

                    Collection ocNames = new ArrayList();
                    for (Iterator i=schemaManager.getObjectClasses().iterator(); i.hasNext(); ) {
                        ObjectClass objectClass = (ObjectClass)i.next();
                        ocNames.add(objectClass.getName());
                    }
                    dialog.setObjectClasses(ocNames);

                    dialog.open();

                    for (Iterator i=dialog.getSelections().iterator(); i.hasNext(); ) {
                        String objectClass = (String)i.next();
                        entryMapping.addObjectClass(objectClass);
                    }

                    refresh();

                } catch (Exception e) {
                    MessageDialog.openError(
                            getShell(),
                            "ERROR",
                            e.getMessage()
                    );
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (objectClassesTable.getSelectionCount() == 0) return;

                TableItem items[] = objectClassesTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    String objectClass = (String)items[i].getData();
                    entryMapping.removeObjectClass(objectClass);
                }

                refresh();
            }
        });
    }

    public Map getObjectClasses(Collection ocNames) {

        SchemaManager schemaManager = server.getSchemaManager();

        Map objectClasses = new TreeMap();

        for (Iterator i=ocNames.iterator(); i.hasNext(); ) {
            String ocName = (String)i.next();
            Collection ocs = schemaManager.getAllObjectClasses(ocName);

            for (Iterator j=ocs.iterator(); j.hasNext(); ) {
                ObjectClass oc = (ObjectClass)j.next();
                objectClasses.put(oc.getName(), oc);
            }
        }

        return objectClasses;
    }

    public void refresh() {
        objectClassesTable.removeAll();

        Map objectClasses = getObjectClasses(entryMapping.getObjectClasses());

        for (Iterator i=objectClasses.values().iterator(); i.hasNext(); ) {
            ObjectClass objectClass = (ObjectClass)i.next();
            String ocName = objectClass.getName();
            entryMapping.addObjectClass(ocName);
        }

        for (Iterator i=entryMapping.getObjectClasses().iterator(); i.hasNext(); ) {
            String ocName = (String)i.next();

            TableItem item = new TableItem(objectClassesTable, SWT.CHECK);
            item.setText(ocName);
            item.setData(ocName);
        }
    }
}
