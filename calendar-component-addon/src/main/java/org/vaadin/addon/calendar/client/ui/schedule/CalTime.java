package org.vaadin.addon.calendar.client.ui.schedule;

import java.io.Serializable;

/**
 * Represent a time
 *
 * @author guettler
 * @since 2.0 (at 31.07.17)
 */

public class CalTime implements Serializable {

    public int h;
    public int m;
    public int s;

    public CalTime() { super(); }

    public CalTime(int hour, int minute, int second) {
        this.h = hour;
        this.m = minute;
        this.s = second;
    }
}
