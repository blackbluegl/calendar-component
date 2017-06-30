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

import com.google.gwt.user.client.ui.HTML;
import org.vaadin.addon.calendar.client.ui.VCalendar;

import java.util.Date;

/**
 * Represents a cell used in {@link WeeklyLongItems}
 *
 * @since 7.1
 */
public class WeeklyLongItemsDateCell extends HTML implements HasTooltipKey {

    private Date date;
    private CalendarItem calendarItem;
    private VCalendar calendar;

    public WeeklyLongItemsDateCell() {
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setItem(CalendarItem event) {
        calendarItem = event;
    }

    public CalendarItem getItem() {
        return calendarItem;
    }

    public void setCalendar(VCalendar calendar) {
        this.calendar = calendar;
    }

    public VCalendar getCalendar() {
        return calendar;
    }

    @Override
    public Object getTooltipKey() {
        if (calendarItem != null) {
            return calendarItem.getIndex();
        }
        return null;
    }
}
