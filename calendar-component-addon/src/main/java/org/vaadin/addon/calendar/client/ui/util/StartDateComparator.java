package org.vaadin.addon.calendar.client.ui.util;

import org.vaadin.addon.calendar.client.ui.schedule.CalendarItem;

/**
 * TODO new class is undocumented
 *
 * @author guettler
 * @since 30.06.17
 */
public class StartDateComparator extends AbstractEventComparator {

    public StartDateComparator(boolean ascending) {
        isAscending = ascending;
    }

    @Override
    public int doCompare(CalendarItem e1, CalendarItem e2) {
        int result = startDateCompare(e1, e2, isAscending);
        if (result == 0) {
            // show a longer event after a shorter event
            return ItemDurationComparator.durationCompare(e1, e2,
                    isAscending);
        }
        return result;
    }

    static int startDateCompare(CalendarItem e1, CalendarItem e2,
                                       boolean ascending) {
        int result = e1.getStartTime().compareTo(e2.getStartTime());
        return ascending ? -result : result;
    }

    private boolean isAscending;
}
