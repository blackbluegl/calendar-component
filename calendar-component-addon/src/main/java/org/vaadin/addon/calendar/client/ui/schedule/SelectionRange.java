package org.vaadin.addon.calendar.client.ui.schedule;

import java.io.Serializable;

/**
 * A class represent a selection of time
 *
 * @author guettler
 * @since 31.07.17
 */

public class SelectionRange implements Serializable {

    /**
     * start minutes
     */
    public int sMin = 0;

    /**
     * end minutes
     */
    public int eMin = 0;

    /**
     * start date
     */
    public CalDate s;

    /**
     * end date
     */
    public CalDate e;

    public SelectionRange() { super(); }

    public void setStartDay(int year, int month, int day) {
        s = new CalDate();
        s.y = year;
        s.m = month;
        s.d = day;
    }

    public void setEndDay(int year, int month, int day) {
        e = new CalDate();
        e.y = year;
        e.m = month;
        e.d = day;
    }

}
