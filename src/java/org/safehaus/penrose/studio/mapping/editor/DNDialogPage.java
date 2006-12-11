package org.safehaus.penrose.studio.mapping.editor;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.*;
import org.eclipse.swt.SWT;
import org.safehaus.penrose.mapping.EntryMapping;

/**
 * @author Endi S. Dewata
 */
public class DNDialogPage extends MappingDialogPage {

    Text parentDnText;
    Text rdnText;

    public DNDialogPage(MappingDialog dialog, Composite parent, int style) {
        super(dialog, parent, style);
    }

    public void init() {
        setLayout(new GridLayout(3, false));

        Label parentDnLabel = new Label(this, SWT.NONE);
        parentDnLabel.setText("Parent DN:");

        GridData gd = new GridData();
        gd.widthHint = 100;
        parentDnLabel.setLayoutData(gd);

        parentDnText = new Text(this, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        parentDnText.setLayoutData(gd);

        parentDnText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                entryMapping.setParentDn("".equals(parentDnText.getText()) ? null : parentDnText.getText());
            }
        });

        Button browseButton = new Button(this, SWT.PUSH);
        browseButton.setText("Browse...");

        gd = new GridData();
        gd.widthHint = 100;
        browseButton.setLayoutData(gd);

        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                EntrySelectionDialog dialog = new EntrySelectionDialog(getShell(), SWT.NONE);
                dialog.setText("Select parent entry...");
                dialog.setPartition(partition);
                dialog.open();

                EntryMapping parentEntry = dialog.getEntryMapping();
                if (parentEntry == null) return;

                parentDnText.setText(parentEntry.getDn());
            }
        });

        Label rdnLabel = new Label(this, SWT.NONE);
        rdnLabel.setText("RDN:");

        rdnText = new Text(this, SWT.BORDER);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        rdnText.setLayoutData(gd);

        rdnText.setEditable(false);
        rdnText.setEnabled(false);
    }

    public void refresh() {
        parentDnText.setText(entryMapping.getParentDn() == null ? "" : entryMapping.getParentDn());
        rdnText.setText(entryMapping.getRdn());
    }
}
