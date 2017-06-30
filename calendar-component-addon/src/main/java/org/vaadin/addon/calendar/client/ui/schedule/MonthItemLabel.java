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

import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.user.client.ui.HTML;
import com.vaadin.client.WidgetUtil;
import org.vaadin.addon.calendar.client.ui.VCalendar;

import java.util.Date;

/**
 * The label in a month cell
 *
 * @since 7.1
 */
public class MonthItemLabel extends HTML implements HasTooltipKey {

    private static final String STYLENAME = "v-calendar-event";

    private boolean timeSpecificEvent = false;
    private Integer itemIndex;
    private VCalendar calendar;
    private String caption;
    private Date time;

    private CalendarItem calendarItem;

    /**
     * Default constructor
     */
    public MonthItemLabel() {
        setStylePrimaryName(STYLENAME);

        addDomHandler(contextMenuEvent -> {
            calendar.getMouseEventListener().contextMenu(contextMenuEvent, MonthItemLabel.this);
            contextMenuEvent.stopPropagation();
            contextMenuEvent.preventDefault();
        }, ContextMenuEvent.getType());
    }

    public void setCalendarItem(CalendarItem e) {
        calendarItem = e;
    }

    /**
     * Set the time of the event label
     *
     * @param date
     *            The date object that specifies the time
     */
    public void setTime(Date date) {
        time = date;
        renderCaption();
    }

    /**
     * Set the caption of the event label
     *
     * @param caption
     *            The caption string, can be HTML if
     *            {@link VCalendar#isItemCaptionAsHtml()} is true
     */
    public void setCaption(String caption) {
        this.caption = caption;
        renderCaption();
    }

    /**
     * Renders the caption in the DIV element
     */
    private void renderCaption() {
        StringBuilder html = new StringBuilder();
        String textOrHtml;
        if (calendar.isItemCaptionAsHtml()) {
            textOrHtml = caption;
        } else {
            textOrHtml = WidgetUtil.escapeHTML(caption);
        }

        if (caption != null && time != null) {
            html.append("<span class=\"" + STYLENAME + "-time\">");
            html.append(calendar.getTimeFormat().format(time));
            html.append("</span> ");
            html.append(textOrHtml);
        } else if (caption != null) {
            html.append(textOrHtml);
        } else if (time != null) {
            html.append("<span class=\"" + STYLENAME + "-time\">");
            html.append(calendar.getTimeFormat().format(time));
            html.append("</span>");
        }
        super.setHTML(html.toString());
    }

    /**
     * Set the (server side) index of the item
     *
     * @param index
     *            The integer index
     */
    public void setItemIndex(int index) {
        itemIndex = index;
    }

    /**
     * Set the Calendar instance this label belongs to
     *
     * @param calendar
     *            The calendar instance
     */
    public void setCalendar(VCalendar calendar) {
        this.calendar = calendar;
    }

    /**
     * Is the item bound to a specific time
     *
     * @return true, if the item bound to a specific time
     */
    public boolean isTimeSpecificEvent() {
        return timeSpecificEvent;
    }

    /**
     * Is the item bound to a specific time
     *
     * @param timeSpecificEvent
     *            True if the event is bound to a time, false if it is only
     *            bound to the day
     */
    public void setTimeSpecificEvent(boolean timeSpecificEvent) {
        this.timeSpecificEvent = timeSpecificEvent;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.google.gwt.user.client.ui.HTML#setHTML(java.lang.String)
     */
    @Override
    public void setHTML(String html) {
        throw new UnsupportedOperationException(
                "Use setCaption() and setTime() instead");
    }

    @Override
    public Object getTooltipKey() {
        return itemIndex;
    }

    public CalendarItem getCalendarItem() {
        return calendarItem;
    }
}
