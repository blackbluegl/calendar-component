package org.vaadin.addon.calendar.client.ui.schedule;

import java.io.Serializable;

/**
 * Represent a day
 *
 * @author guettler
 * @since 2.0 (at 31.07.17)
 */

public class CalDate implements Serializable {

    public int year;
    public int month;
    public int day;

    public CalTime time;

    public CalDate() { super(); }

    public CalDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }
}
