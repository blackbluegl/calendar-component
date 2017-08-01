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

import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

/**
 * Implements basic functionality needed to change to week view when a week
 * number is clicked.
 *
 * @since 7.1
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")

public class BasicWeekClickHandler implements CalendarComponentEvents.WeekClickHandler {

    /*
     * (non-Javadoc)
     *
     * @see org.vaadin.addon.calendar.ui.CalendarComponentEvents.WeekClickHandler
     * #weekClick(org.vaadin.addon.calendar.ui.CalendarComponentEvents.WeekClick)
     */
    @Override
    public void weekClick(CalendarComponentEvents.WeekClick event) {

        int week = event.getWeek();
        int year = event.getYear();

        ZonedDateTime dateTime = ZonedDateTime.now(event.getComponent().getZoneId());
        // set correct year and month
        dateTime = dateTime.with(ChronoField.YEAR, year).with(ChronoField.ALIGNED_WEEK_OF_YEAR, week);

        setDates(event, dateTime.with(ChronoField.DAY_OF_WEEK, 1), dateTime.with(ChronoField.DAY_OF_WEEK, 7));
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
    protected void setDates(CalendarComponentEvents.WeekClick event, ZonedDateTime start, ZonedDateTime end) {
        event.getComponent().setStartDate(start);
        event.getComponent().setEndDate(end);
    }

}
