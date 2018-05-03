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

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import org.vaadin.addon.calendar.ui.CalendarComponentEvents;

/**
 * Implements basic functionality needed to enable backwards navigation.
 *
 * @since 7.1
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")

public class BasicBackwardHandler implements CalendarComponentEvents.BackwardHandler {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.vaadin.addon.calendar.ui.CalendarComponentEvents.BackwardHandler#
     * backward
     * (org.vaadin.addon.calendar.ui.CalendarComponentEvents.BackwardEvent)
     */
    @Override
    public void backward(CalendarComponentEvents.BackwardEvent event) {

        int firstDay = event.getComponent().getFirstVisibleDayOfWeek();
        int lastDay = event.getComponent().getLastVisibleDayOfWeek();

        ZonedDateTime start = event.getComponent().getStartDate();
        ZonedDateTime end = event.getComponent().getEndDate();

        long durationInDays;

        // for week view durationInDays = 7, for day view durationInDays = 1
        if (event.getComponent().isDayMode()) { // day view
            durationInDays = 1;
        } else if (event.getComponent().isWeeklyMode()) {
            durationInDays = 7;
        } else {
            durationInDays = Duration.between(start, end).toDays() +1;
        }

        start = start.minus(durationInDays, ChronoUnit.DAYS);
        end = end.minus(durationInDays, ChronoUnit.DAYS);

        if (event.getComponent().isDayMode()) { // day view

            int dayOfWeek = start.get(ChronoField.DAY_OF_WEEK);

            ZonedDateTime newDate = start;
            while (!(firstDay <= dayOfWeek && dayOfWeek <= lastDay)) {

                newDate = newDate.minus(1, ChronoUnit.DAYS);

                dayOfWeek = newDate.get(ChronoField.DAY_OF_WEEK);
            }

            setDates(event, newDate, newDate);

            return;

        }

        if (durationInDays < 28) {
            setDates(event, start, end);
        } else {
            // go 7 days before and get the last day from month
            setDates(event, start.minus(7, ChronoUnit.DAYS).with(lastDayOfMonth()), end.with(lastDayOfMonth()));
        }
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
    protected void setDates(CalendarComponentEvents.BackwardEvent event, ZonedDateTime start, ZonedDateTime end) {
        event.getComponent().setStartDate(start);
        event.getComponent().setEndDate(end);
    }
}
