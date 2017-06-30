package org.vaadin.addon.calendar.client.ui.util;

import org.vaadin.addon.calendar.client.ui.schedule.CalendarItem;

import java.util.Comparator;

/**
 *
 * @author guettler
 * @since 30.06.17
 */

public abstract class AbstractEventComparator implements Comparator<CalendarItem> {

    @Override
    public int compare(CalendarItem e1, CalendarItem e2) {
        if (e1.isAllDay() != e2.isAllDay()) {
            if (e2.isAllDay()) {
                return 1;
            }
            return -1;
        }
        int result = doCompare(e1, e2);
        if (result == 0) {
            return indexCompare(e1, e2);
        }
        return result;
    }

    protected int indexCompare(CalendarItem e1, CalendarItem e2) {
        return ((Integer) e2.getIndex()).compareTo(e1.getIndex());
    }

    protected abstract int doCompare(CalendarItem o1, CalendarItem o2);

}
