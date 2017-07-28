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
package org.vaadin.addon.calendar.ui;

import com.vaadin.util.ReflectTools;
import org.vaadin.addon.calendar.Calendar;
import org.vaadin.addon.calendar.client.CalendarEventId;
import org.vaadin.addon.calendar.item.CalendarItem;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.EventListener;

/**
 * Interface for all Vaadin Calendar items.
 *
 * @since 7.1.0
 * @author Vaadin Ltd.
 *
 */
public interface CalendarComponentEvents extends Serializable {

    /**
     * Notifier interface for notifying listener of calendar items
     */

    interface CalendarEventNotifier extends Serializable {
        /**
         * Get the assigned event handler for the given eventId.
         *
         * @param eventId
         * @return the assigned eventHandler, or null if no handler is assigned
         */
        EventListener getHandler(String eventId);
    }

    /**
     * Handler interface for day or time cell drag-marking with mouse.
     */

    interface RangeSelectNotifier extends Serializable, CalendarEventNotifier {

        /**
         * Set the RangeSelectHandler that listens for drag-marking.
         *
         * @param listener
         *            RangeSelectHandler to be added.
         */
        void setHandler(RangeSelectHandler listener);
    }

    /**
     * RangeSelectEvent is sent when day or time cells are drag-marked with
     * mouse.
     */
    @SuppressWarnings("serial")
    class RangeSelectEvent extends CalendarComponentEvent {

        public static final String EVENT_ID = CalendarEventId.RANGESELECT;

        /** Calendar event's start date. */
        private ZonedDateTime start;

        /** Calendar event's end date. */
        private ZonedDateTime end;

        /**
         * RangeSelectEvent needs a start and end date.
         *
         * @param source
         *            Calendar component.
         * @param start
         *            Start date.
         * @param end
         *            End date.
         */
        public RangeSelectEvent(Calendar source, ZonedDateTime start, ZonedDateTime end) {
            super(source);
            this.start = start;
            this.end = end;
        }

        /**
         * Get start date.
         *
         * @return Start date.
         */
        public ZonedDateTime getStart() {
            return start;
        }

        /**
         * Get end date.
         *
         * @return End date.
         */
        public ZonedDateTime getEnd() {
            return end;
        }

    }

    /** RangeSelectHandler handles RangeSelectEvent. */
    interface RangeSelectHandler extends EventListener, Serializable {

        /** Trigger method for the RangeSelectEvent. */
        Method rangeSelectMethod = ReflectTools.findMethod(
                RangeSelectHandler.class, "rangeSelect",
                RangeSelectEvent.class);

        /**
         * This method will be called when day or time cells are drag-marked
         * with mouse.
         *
         * @param event
         *            RangeSelectEvent that contains range start and end date.
         */
        void rangeSelect(RangeSelectEvent event);
    }

    /** Notifier interface for navigation listening. */
    interface NavigationNotifier extends Serializable {
        /**
         * Add a forward navigation listener.
         *
         * @param handler
         *            ForwardHandler to be added.
         */
        void setHandler(ForwardHandler handler);

        /**
         * Add a backward navigation listener.
         *
         * @param handler
         *            BackwardHandler to be added.
         */
        void setHandler(BackwardHandler handler);

        /**
         * Add a date click listener.
         *
         * @param handler
         *            DateClickHandler to be added.
         */
        void setHandler(DateClickHandler handler);

        /**
         * Add a item click listener.
         *
         * @param handler
         *            ItemClickHandler to be added.
         */
        void setHandler(ItemClickHandler handler);

        /**
         * Add a week click listener.
         *
         * @param handler
         *            WeekClickHandler to be added.
         */
        void setHandler(WeekClickHandler handler);
    }

    /**
     * ForwardEvent is sent when forward navigation button is clicked.
     */
    @SuppressWarnings("serial")
    class ForwardEvent extends CalendarComponentEvent {

        public static final String EVENT_ID = CalendarEventId.FORWARD;

        /**
         * ForwardEvent needs only the source component.
         *
         * @param source
         *            Calendar component.
         */
        public ForwardEvent(Calendar source) {
            super(source);
        }
    }

    /** ForwardHandler handles ForwardEvent. */
    interface ForwardHandler extends EventListener, Serializable {

        /** Trigger method for the ForwardEvent. */
        Method forwardMethod = ReflectTools.findMethod(
                ForwardHandler.class, "forward", ForwardEvent.class);

        /**
         * This method will be called when date range is moved forward.
         *
         * @param event
         *            ForwardEvent
         */
        void forward(ForwardEvent event);
    }

    /**
     * BackwardEvent is sent when backward navigation button is clicked.
     */
    @SuppressWarnings("serial")
    class BackwardEvent extends CalendarComponentEvent {

        public static final String EVENT_ID = CalendarEventId.BACKWARD;

        /**
         * BackwardEvent needs only the source source component.
         *
         * @param source
         *            Calendar component.
         */
        public BackwardEvent(Calendar source) {
            super(source);
        }
    }

    /** BackwardHandler handles BackwardEvent. */
    interface BackwardHandler extends EventListener, Serializable {

        /** Trigger method for the BackwardEvent. */
        Method backwardMethod = ReflectTools.findMethod(
                BackwardHandler.class, "backward", BackwardEvent.class);

        /**
         * This method will be called when date range is moved backwards.
         *
         * @param event
         *            BackwardEvent
         */
        void backward(BackwardEvent event);
    }

    /**
     * DateClickEvent is sent when a date is clicked.
     */
    @SuppressWarnings("serial")
    class DateClickEvent extends CalendarComponentEvent {

        public static final String EVENT_ID = CalendarEventId.DATECLICK;

        /** Date that was clicked. */
        private ZonedDateTime date;

        /** DateClickEvent needs the target date that was clicked. */
        public DateClickEvent(Calendar source, ZonedDateTime date) {
            super(source);
            this.date = date;
        }

        /**
         * Get clicked date.
         *
         * @return Clicked date.
         */
        public ZonedDateTime getDate() {
            return date;
        }
    }

    /** DateClickHandler handles DateClickEvent. */
    interface DateClickHandler extends EventListener, Serializable {

        /** Trigger method for the DateClickEvent. */
        Method dateClickMethod = ReflectTools.findMethod(
                DateClickHandler.class, "dateClick", DateClickEvent.class);

        /**
         * This method will be called when a date is clicked.
         *
         * @param event
         *            DateClickEvent containing the target date.
         */
        void dateClick(DateClickEvent event);
    }

    /**
     * WeekClick is sent when week is clicked.
     */
    @SuppressWarnings("serial")
    class WeekClick extends CalendarComponentEvent {

        public static final String EVENT_ID = CalendarEventId.WEEKCLICK;

        /** Target week. */
        private int week;

        /** Target year. */
        private int year;

        /**
         * WeekClick needs a target year and week.
         *
         * @param source
         *            Target source.
         * @param week
         *            Target week.
         * @param year
         *            Target year.
         */
        public WeekClick(Calendar source, int week, int year) {
            super(source);
            this.week = week;
            this.year = year;
        }

        /**
         * Get week as a integer. See {@link java.util.Calendar} for the allowed
         * values.
         *
         * @return Week as a integer.
         */
        public int getWeek() {
            return week;
        }

        /**
         * Get year as a integer. See {@link java.util.Calendar} for the allowed
         * values.
         *
         * @return Year as a integer
         */
        public int getYear() {
            return year;
        }
    }

    /** WeekClickHandler handles WeekClicks. */
    interface WeekClickHandler extends EventListener, Serializable {

        /** Trigger method for the WeekClick. */
        Method weekClickMethod = ReflectTools.findMethod(
                WeekClickHandler.class, "weekClick", WeekClick.class);

        /**
         * This method will be called when a week is clicked.
         *
         * @param event
         *            WeekClick containing the target week and year.
         */
        void weekClick(WeekClick event);
    }

    /**
     * ItemResizeEvent is sent when an item is resized
     */
    @SuppressWarnings("serial")
    class ItemResizeEvent extends CalendarComponentEvent {

        public static final String EVENT_ID = CalendarEventId.ITEM_RESIZE;

        private CalendarItem calendarItem;

        private ZonedDateTime startTime;

        private ZonedDateTime endTime;

        public ItemResizeEvent(Calendar source, CalendarItem calendarItem,
                               ZonedDateTime startTime, ZonedDateTime endTime) {
            super(source);
            this.calendarItem = calendarItem;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        /**
         * Get target event.
         *
         * @return Target event.
         */
        public CalendarItem getCalendarItem() {
            return calendarItem;
        }

        /**
         * @deprecated Use {@link #getNewStart()} instead
         *
         * @return the new start time
         */
        public ZonedDateTime getNewStartTime() {
            return startTime;
        }

        /**
         * Returns the updated start date/time of the event
         *
         * @return The new date for the event
         */
        public ZonedDateTime getNewStart() {
            return startTime;
        }

        /**
         * @deprecated Use {@link #getNewEnd()} instead
         *
         * @return the new end time
         */
        public ZonedDateTime getNewEndTime() {
            return endTime;
        }

        /**
         * Returns the updates end date/time of the event
         *
         * @return The new date for the item
         */
        public ZonedDateTime getNewEnd() {
            return endTime;
        }
    }

    /**
     * Notifier interface for item resizing.
     */
    interface ItemResizeNotifier extends Serializable {

        /**
         * Set a ItemResizeHandler.
         *
         * @param handler
         *            ItemResizeHandler to be set
         */
        void setHandler(EventResizeHandler handler);
    }

    /**
     * Handler for ItemResizeEvent event.
     */
    interface EventResizeHandler extends EventListener, Serializable {

        /** Trigger method for the ItemResizeEvent. */
        Method itemResizeMethod = ReflectTools.findMethod(
                EventResizeHandler.class, "itemResize", ItemResizeEvent.class);

        void itemResize(ItemResizeEvent event);
    }

    /**
     * Notifier interface for item drag & drops.
     */
    interface ItemMoveNotifier extends CalendarEventNotifier {

        /**
         * Set the ItemMoveHandler.
         *
         * @param listener
         *            ItemMoveHandler to be added
         */
        void setHandler(ItemMoveHandler listener);

    }

    /**
     * ItemMoveEvent is sent when existing item is dragged to a new position.
     */
    @SuppressWarnings("serial")
    class ItemMoveEvent extends CalendarComponentEvent {

        public static final String EVENT_ID = CalendarEventId.ITEM_MOVE;

        /** Index for the moved Schedule.Item. */
        private CalendarItem calendarItem;

        /** New starting date for the moved Calendar.Item. */
        private ZonedDateTime newStart;

        /**
         * ItemMoveEvent needs the target event and new start date.
         *
         * @param source
         *            Calendar component.
         * @param calendarItem
         *            Target event.
         * @param newStart
         *            Target event's new start date.
         */
        public ItemMoveEvent(Calendar source, CalendarItem calendarItem,
                             ZonedDateTime newStart) {
            super(source);

            this.calendarItem = calendarItem;
            this.newStart = newStart;
        }

        /**
         * Get target event.
         *
         * @return Target event.
         */
        public CalendarItem getCalendarItem() {
            return calendarItem;
        }

        /**
         * Get new start date.
         *
         * @return New start date.
         */
        public ZonedDateTime getNewStart() {
            return newStart;
        }
    }

    /**
     * Handler interface for when items are being dragged on the calendar     *
     */
    interface ItemMoveHandler extends EventListener, Serializable {

        /** Trigger method for the ItemMoveEvent. */
        Method itemMoveMethod = ReflectTools.findMethod(
                ItemMoveHandler.class, "itemMove", ItemMoveEvent.class);

        /**
         * This method will be called when item has been moved to a new
         * position.
         *
         * @param event
         *            ItemMoveEvent containing specific information of the new
         *            position and target event.
         */
        void itemMove(ItemMoveEvent event);
    }

    /**
     * ItemClickEvent is sent when an item is clicked.
     */
    @SuppressWarnings("serial")
    class ItemClickEvent extends CalendarComponentEvent {

        public static final String EVENT_ID = CalendarEventId.ITEM_CLICK;

        /** Clicked source event. */
        private CalendarItem calendarItem;

        /** Target source event is needed for the ItemClickEvent. */
        public ItemClickEvent(Calendar source, CalendarItem calendarItem) {
            super(source);
            this.calendarItem = calendarItem;
        }

        /**
         * Get the clicked event.
         *
         * @return Clicked event.
         */
        public CalendarItem getCalendarItem() {
            return calendarItem;
        }
    }

    /**
     * ItemClickHandler handles ItemClickEvent.
     */
    interface ItemClickHandler extends EventListener, Serializable {

        /** Trigger method for the ItemClickEvent. */
        Method itemClickMethod = ReflectTools.findMethod(
                ItemClickHandler.class, "itemClick", ItemClickEvent.class);

        /**
         * This method will be called when an item is clicked.
         *
         * @param event
         *            ItemClickEvent containing the target item.
         */
        void itemClick(ItemClickEvent event);
    }
}
