package org.vaadin.addon.calendar.client.ui.schedule;

import java.io.Serializable;

/**
 * Represent a day
 *
 * @author guettler
 * @since 2.0 (at 31.07.17)
 */

public class CalDate implements Serializable {

    public int y;
    public int m;
    public int d;

    public CalTime t;

    public CalDate() { super(); }

    public CalDate(int year, int month, int day) {
        this.y = year;
        this.m = month;
        this.d = day;
    }

    public CalDate(int year, int month, int day, CalTime time) {
        this.y = year;
        this.m = month;
        this.d = day;
        this.t = time;
    }
}
