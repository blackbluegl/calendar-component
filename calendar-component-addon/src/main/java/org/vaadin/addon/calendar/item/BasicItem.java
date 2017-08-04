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
package org.vaadin.addon.calendar.item;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of {@link CalendarItem
 * CalendarItem}. Has setters for all required fields and fires items when
 * this event is changed.
 *
 * @since 7.1.0
 * @author Vaadin Ltd.
 */

public class BasicItem implements EditableCalendarItem {

    private String caption;
    private String description;
    private ZonedDateTime end;
    private ZonedDateTime start;
    private String styleName;

    private boolean isAllDay;

    /**
     * Default constructor
     */
    public BasicItem() {}

    /**
     * Constructor for creating an event with the same start and end date
     *
     * @param caption
     *            The caption for the event
     * @param description
     *            The description for the event
     * @param date
     *            The date the event occurred
     */
    public BasicItem(String caption, String description, ZonedDateTime date) {
        this.caption = caption;
        this.description = description;
        start = date;
        end = date;
    }

    /**
     * Constructor for creating an event with a start date and an end date.
     * Start date should be before the end date
     *
     * @param caption
     *            The caption for the event
     * @param description
     *            The description for the event
     * @param startDate
     *            The start date of the event
     * @param endDate
     *            The end date of the event
     */
    public BasicItem(String caption, String description, ZonedDateTime startDate, ZonedDateTime endDate) {
        this.caption = caption;
        this.description = description;
        start = startDate;
        end = endDate;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.vaadin.addon.calendar.event.CalendarItem#getCaption()
     */
    @Override
    public String getCaption() {
        return caption;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.vaadin.addon.calendar.event.CalendarItem#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.vaadin.addon.calendar.event.CalendarItem#getEnd()
     */
    @Override
    public ZonedDateTime getEnd() {
        return end;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.vaadin.addon.calendar.event.CalendarItem#getStart()
     */
    @Override
    public ZonedDateTime getStart() {
        return start;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.vaadin.addon.calendar.event.CalendarItem#getStyleName()
     */
    @Override
    public String getStyleName() {
        return styleName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.vaadin.addon.calendar.event.CalendarItem#isAllDay()
     */
    @Override
    public boolean isAllDay() {
        return isAllDay;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.vaadin.addon.calendar.event.CalendarEventEditor#setCaption(java.lang
     * .String)
     */
    @Override
    public void setCaption(String caption) {
        this.caption = caption;
        fireEventChange();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.vaadin.addon.calendar.event.CalendarEventEditor#setDescription(java
     * .lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
        fireEventChange();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.vaadin.addon.calendar.event.CalendarEventEditor#setEnd(java.util.
     * Date)
     */
    @Override
    public void setEnd(ZonedDateTime end) {
        this.end = end;
        fireEventChange();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.vaadin.addon.calendar.event.CalendarEventEditor#setStart(java.util
     * .Date)
     */
    @Override
    public void setStart(ZonedDateTime start) {
        this.start = start;
        fireEventChange();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.vaadin.addon.calendar.event.CalendarEventEditor#setStyleName(java
     * .lang.String)
     */
    @Override
    public void setStyleName(String styleName) {
        this.styleName = styleName;
        fireEventChange();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.vaadin.addon.calendar.event.CalendarEventEditor#setAllDay(boolean)
     */
    @Override
    public void setAllDay(boolean isAllDay) {
        this.isAllDay = isAllDay;
        fireEventChange();
    }

    /**
     * Fires an event change event to the listeners. Should be triggered when
     * some property of the event changes.
     */
    protected void fireEventChange() {
        ItemChangedEvent<EditableCalendarItem> event = new ItemChangedEvent<>(this);

        for (ItemChangeListener listener : notifier.getListeners()) {
            listener.itemChanged(event);
        }
    }

    private Notify notifier = new Notify();

    @Override
    public ItemChangeNotifier getNotifier() {
        return notifier;
    }

    private class Notify implements ItemChangeNotifier {

        private transient List<ItemChangeListener> listeners = new ArrayList<>();

        public List<ItemChangeListener> getListeners() {
            return listeners;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.vaadin.addon.calendar.ui.CalendarComponentEvents.ItemChangeNotifier
         * #addListener
         * (org.vaadin.addon.calendar.ui.CalendarComponentEvents.ItemChangeListener
         * )
         */
        @Override
        public void addListener(ItemChangeListener listener) {
            listeners.add(listener);
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.vaadin.addon.calendar.ui.CalendarComponentEvents.ItemChangeNotifier
         * #removeListener
         * (org.vaadin.addon.calendar.ui.CalendarComponentEvents.ItemChangeListener
         * )
         */
        @Override
        public void removeListener(ItemChangeListener listener) {
            listeners.remove(listener);
        }
    }
}
