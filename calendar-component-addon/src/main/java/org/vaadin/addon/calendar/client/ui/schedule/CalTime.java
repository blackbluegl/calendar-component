package org.vaadin.addon.calendar.client.ui.schedule;

import java.io.Serializable;

/**
 * Represent a time
 *
 * @author guettler
 * @since 2.0 (at 31.07.17)
 */

public class CalTime implements Serializable {

    public int hour;
    public int minute;
    public int second;

    public CalTime() { super(); }

    public CalTime(int hour, int minute, int second) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }
}
