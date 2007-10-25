package org.safehaus.penrose.studio.dialog;

import org.eclipse.swt.widgets.Display;

/**
 * @author Endi Sukma Dewata
*/
public class ProcessThread extends Thread {

    int max = 0;
    boolean closed = false;
    ProgressBarDialog progressBarDialog;

    public ProcessThread(ProgressBarDialog progressBarDialog, int max) {
        this.progressBarDialog = progressBarDialog;
        this.max = max;
    }

    public void run() {

        Display display = progressBarDialog.getShell().getDisplay();

        progressBarDialog.doBefore();

        for (final int[] i = new int[]{1}; i[0] <= max; i[0]++) {
            //
            final String info = progressBarDialog.process(i[0]);
            if (display.isDisposed()) return;

            display.syncExec(new Runnable() {
                public void run() {
                    if (progressBarDialog.getProgressBar().isDisposed()) return;

                    progressBarDialog.setMessage(info);
                    progressBarDialog.setSelection(i[0]);

                    if (i[0] == max || progressBarDialog.closed) {
                        if (progressBarDialog.closed) {
                            closed = true;
                            progressBarDialog.cleanUp();
                        }
                        progressBarDialog.getShell().close();
                    }
                }
            });

            if (closed) break;
        }

        progressBarDialog.doAfter();
    }
}
