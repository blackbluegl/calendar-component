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
package org.vaadin.addon.calendar.client.ui.schedule;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.vaadin.addon.calendar.client.CalendarState;

/**
 * Utility class used to represent a day when updating views. Only used
 * internally.
 *
 * @since 7.1
 * @author Vaadin Ltd.
 */
public class CalendarDay {

    private Date date;
    private String localizedDateFormat;
    private int dayOfWeek;
    private int week;
    private int yearOfWeek;
    private Map<Long, CalTimeSlot> styledSlots;

    public CalendarDay(Date date, String localizedDateFormat, int dayOfWeek, int week, int yearOfWeek, Set<CalendarState.SlotStyle> slotStyles) {
        super();
        this.date = date;
        this.localizedDateFormat = localizedDateFormat;
        this.dayOfWeek = dayOfWeek;
        this.week = week;
        this.yearOfWeek = yearOfWeek;

        this.styledSlots = new HashMap<>();
        for (CalendarState.SlotStyle slot : slotStyles) {
            styledSlots.put(slot.slotStart, new CalTimeSlot(slot.slotStart, slot.styleName));
        }
    }

    public Date getDate() {
        return date;
    }

    public String getLocalizedDateFormat() {
        return localizedDateFormat;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getWeek() {
        return week;
    }

    public int getYearOfWeek() {
        return yearOfWeek;
    }

    public Map<Long, CalTimeSlot> getStyledSlots() {
        return styledSlots;
    }
}
