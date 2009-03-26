package org.safehaus.penrose.studio.dialog;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.ietf.ldap.LDAPException;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Endi Sukma Dewata
 */
public class ErrorDialog {

    public static void open(Throwable t) {
        if (t instanceof InvocationTargetException) {
            InvocationTargetException e = (InvocationTargetException)t;
            open(e.getCause());

        } else if (t instanceof LDAPException) {
            LDAPException e = (LDAPException)t;
            open(e);

        } else {
            open("ERROR", t);
        }
    }
    
    public static void open(LDAPException e) {
        StringWriter sw = new StringWriter();

        PrintWriter pw = new PrintWriter(sw);
        pw.println("LDAP Error ("+e.getResultCode()+"): "+e.getMessage());

        String matchedDn = e.getMatchedDN();
        if (matchedDn != null && !"".equals(matchedDn)) {
            pw.println("Matched DN: "+matchedDn);
        }

        String errorMessage = e.getLDAPErrorMessage();
        if (errorMessage != null && !"".equals(errorMessage)) {
            pw.println("Message: "+errorMessage);
        }

        open("ERROR", sw.toString());
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
