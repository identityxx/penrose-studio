package org.safehaus.penrose.studio.federation.linking;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.SWT;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.ldap.SearchResult;
import org.safehaus.penrose.federation.LinkingData;

import java.util.Collection;

/**
 * @author Endi Sukma Dewata
 */
public class LocalTableLabelProvider implements ITableLabelProvider, ITableColorProvider {

    Color red;
    Color green;
    Color blue;

    public LocalTableLabelProvider() {

        Display display = Display.getDefault();

        red = display.getSystemColor(SWT.COLOR_RED);
        green = display.getSystemColor(SWT.COLOR_GREEN);
        blue = display.getSystemColor(SWT.COLOR_BLUE);
    }

    public void dispose() {
    }

    public Image getColumnImage(Object object, int index) {
        return null;
    }

    public String getColumnText(Object object, int index) {

        LinkingData data = (LinkingData)object;

        switch (index) {
            case 0:
                return data.getDn().toString();
            case 1:
                String status = data.getStatus();
                return status == null ? "" : status;
        }

        return "";
    }

    public void addListener(ILabelProviderListener listener) {
    }

    public boolean isLabelProperty(Object object, String property) {
        return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    }

    public Color getForeground(Object object, int index) {
        LinkingData data = (LinkingData)object;

        if (index == 0) return null;

        Collection<SearchResult> links = data.getLinkedEntries();

        if (!links.isEmpty()) {

            if (links.size() == 1) {
                return green;

            } else {
                return red;
            }
        }

        if (!data.isSearched()) return null;

        Collection<SearchResult> matches = data.getMatchedEntries();

        if (!matches.isEmpty()) {

            if (matches.size() == 1) {
                return blue;

            } else {
                return blue;
            }
        }

        return red;
    }

    public Color getBackground(Object object, int i) {
        return null;
    }
}
