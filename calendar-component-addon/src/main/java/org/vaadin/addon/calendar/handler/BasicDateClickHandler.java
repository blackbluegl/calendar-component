/*
 * Copyright 2000-2016 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.vaadin.addon.calendar.handler;

import org.vaadin.addon.calendar.ui.CalendarComponentEvents;

import java.util.Calendar;
import java.util.Date;

/**
 * Implements basic functionality needed to switch to day view when a single day
 * is clicked.
 *
 * @since 7.1
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")

public class BasicDateClickHandler implements CalendarComponentEvents.DateClickHandler {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.vaadin.addon.calendar.ui.CalendarComponentEvents.DateClickHandler
     * #dateClick
     * (org.vaadin.addon.calendar.ui.CalendarComponentEvents.DateClickEvent)
     */
    @Override
    public void dateClick(CalendarComponentEvents.DateClickEvent event) {

        org.vaadin.addon.calendar.Calendar comp = event.getComponent();

        Date clickedDate = event.getDate();

        java.util.Calendar cal = comp.getInternalCalendar();

        Date start, end;

        if (comp.isDayMode()) {

            // switch to weekly mode

            cal.set(java.util.Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
            cal.clear(java.util.Calendar.MINUTE);
            cal.clear(java.util.Calendar.SECOND);
            cal.clear(java.util.Calendar.MILLISECOND);
            cal.setTime(clickedDate);
            cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

            cal.add(java.util.Calendar.DAY_OF_WEEK, comp.getFirstVisibleDayOfWeek() -1);

            start = cal.getTime();

            cal.add(java.util.Calendar.DAY_OF_WEEK, comp.getLastVisibleDayOfWeek() -1);

            end = cal.getTime();

            setDates(event, start, end);

        } else if (comp.isWeeklyMode()) {

            // switch to monthly mode

            cal.set(java.util.Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
            cal.clear(java.util.Calendar.MINUTE);
            cal.clear(java.util.Calendar.SECOND);
            cal.clear(java.util.Calendar.MILLISECOND);
            cal.setTime(clickedDate);
            cal.set(Calendar.DAY_OF_MONTH, 1);

            start = cal.getTime();

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

            end = cal.getTime();

        } else {

            // switch to daily mode
            cal.setTime(clickedDate);

            start = end = cal.getTime();
        }

        setDates(event, start, end);
    }

    /**
     * Set the start and end dates for the event
     *
     * @param event
     *            The event that the start and end dates should be set
     * @param start
     *            The start date
     * @param end
     *            The end date
     */
    protected void setDates(CalendarComponentEvents.DateClickEvent event, Date start, Date end) {
        event.getComponent().setStartDate(start);
        event.getComponent().setEndDate(end);
    }
}
