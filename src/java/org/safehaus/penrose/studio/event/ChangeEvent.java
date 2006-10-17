package org.safehaus.penrose.studio.event;

import java.util.Date;

/**
 * @author Endi S. Dewata
 */
public class ChangeEvent {

    private Date date;
    private Object object;

    public ChangeEvent(Date date, Object object) {
        this.date = date;
        this.object = object;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
