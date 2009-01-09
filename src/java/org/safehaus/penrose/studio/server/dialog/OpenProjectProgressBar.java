package org.safehaus.penrose.studio.server.dialog;

import org.safehaus.penrose.studio.dialog.ProgressBarDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Endi Sukma Dewata
 */
public class OpenProjectProgressBar extends ProgressBarDialog {

    private String[] info = null;

    public OpenProjectProgressBar(Shell parent) {
        super(parent);
    }

    public void initGauge() {

        info = new String[100];
        for (int i = 0; i < info.length; i++) {
            info[i] = "process task " + i + ".";
        }

        setExecuteTime(100);
        setMayCancel(true);
        setProcessMessage("please waiting....");
        setText("Demo");

    }

    protected String process(int n) {
        try {
            Thread.sleep((long) (Math.random() * 300));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return info[n - 1];
    }

}

