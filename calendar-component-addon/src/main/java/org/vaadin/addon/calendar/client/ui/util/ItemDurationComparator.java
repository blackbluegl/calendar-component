package org.vaadin.addon.calendar.client.ui.util;

import org.vaadin.addon.calendar.client.DateConstants;
import org.vaadin.addon.calendar.client.ui.schedule.CalendarItem;

/** *
 * @author guettler
 * @since 30.06.17
 */
public class ItemDurationComparator extends AbstractEventComparator {

    public ItemDurationComparator(boolean ascending) {
        isAscending = ascending;
    }

    @Override
    public int doCompare(CalendarItem e1, CalendarItem e2) {
        int result = durationCompare(e1, e2, isAscending);
        if (result == 0) {
            return StartDateComparator.startDateCompare(e1, e2,
                    isAscending);
        }
        return result;
    }

    static int durationCompare(CalendarItem e1, CalendarItem e2,
                               boolean ascending) {
        int result = doDurationCompare(e1, e2);
        return ascending ? -result : result;
    }

    private static int doDurationCompare(CalendarItem e1,
                                         CalendarItem e2) {
        Long d1 = e1.getRangeInMilliseconds();
        Long d2 = e2.getRangeInMilliseconds();
        if (!d1.equals(0L) && !d2.equals(0L)) {
            return d2.compareTo(d1);
        }

        if (d2.equals(0L) && d1.equals(0L)) {
            return 0;
        } else if (d2.equals(0L) && d1 >= DateConstants.DAYINMILLIS) {
            return -1;
        } else if (d2.equals(0L) && d1 < DateConstants.DAYINMILLIS) {
            return 1;
        } else if (d1.equals(0L) && d2 >= DateConstants.DAYINMILLIS) {
            return 1;
        } else if (d1.equals(0L) && d2 < DateConstants.DAYINMILLIS) {
            return -1;
        }
        return d2.compareTo(d1);
    }

    private boolean isAscending;

}