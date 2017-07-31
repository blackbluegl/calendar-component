package org.vaadin.addon.calendar.client.ui.schedule;

import java.io.Serializable;

/**
 * A class represent a selection of time
 *
 * @author guettler
 * @since 31.07.17
 */

public class DateRangeSelection implements Serializable {

    public int startMinutes = 0;
    public int endMinutes = 0;

    public CalDate startDay;
    public CalDate endDay;

    public DateRangeSelection() { super(); }

    public void setStartDay(int year, int month, int day) {
        startDay = new CalDate();
        startDay.year = year;
        startDay.month = month;
        startDay.day = day;
    }

    public void setEndDay(int year, int month, int day) {
        endDay = new CalDate();
        endDay.year = year;
        endDay.month = month;
        endDay.day = day;
    }

}
