package org.safehaus.penrose.studio.dialog;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.dialogs.MessageDialog;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * @author Endi Sukma Dewata
 */
public class ErrorDialog {

    public static void open(Throwable t) {
        open("ERROR", t);
    }
    
    public static void open(String message) {
        open("ERROR", message);
    }

    public static void open(String title, Throwable t) {

        StringWriter sw = new StringWriter();

        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);

        open(title, sw.toString());
    }

    public static void open(String title, String message) {

        if (message.length() > 500) {
            message = message.substring(0, 500) + "...";
        }

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        MessageDialog.openError(window.getShell(), title, message);
    }
}
