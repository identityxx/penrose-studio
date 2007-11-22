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

    public static void open(Exception e) {
        open("ERROR", e);
    }
    
    public static void open(String message) {
        open("ERROR", message);
    }

    public static void open(String title, Exception e) {

        StringWriter sw = new StringWriter();

        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        String message = sw.toString();
        if (message.length() > 500) {
            message = message.substring(0, 500) + "...";
        }

        open(title, message);
    }

    public static void open(String title, String message) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        MessageDialog.openError(window.getShell(), title, message);
    }
}
