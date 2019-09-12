package org.vaadin.addon.calendar.client.ui.schedule;

import java.io.Serializable;

/**
 * A styled time slot on a day
 */
public class CalTimeSlot implements Serializable {

    public long start;
    public String style;

    public CalTimeSlot(final long start, final String style) {
        this.start = start;
        this.style = style;
    }
}
