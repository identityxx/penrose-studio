package org.safehaus.penrose.studio.project;

import org.safehaus.penrose.studio.tree.Node;

/**
 * @author Endi Sukma Dewata
 */
public class ErrorNode extends Node {
    
    public ErrorNode(String message, Node parent) {
        super(message, null, null, parent);
    }
}
