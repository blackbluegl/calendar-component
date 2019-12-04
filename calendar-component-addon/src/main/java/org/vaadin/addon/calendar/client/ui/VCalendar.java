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
package org.vaadin.addon.calendar.client.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.vaadin.addon.calendar.client.CalendarState;
import org.vaadin.addon.calendar.client.DateConstants;
import org.vaadin.addon.calendar.client.ui.schedule.CalDate;
import org.vaadin.addon.calendar.client.ui.schedule.CalendarDay;
import org.vaadin.addon.calendar.client.ui.schedule.CalendarItem;
import org.vaadin.addon.calendar.client.ui.schedule.DayToolbar;
import org.vaadin.addon.calendar.client.ui.schedule.MonthGrid;
import org.vaadin.addon.calendar.client.ui.schedule.SelectionRange;
import org.vaadin.addon.calendar.client.ui.schedule.SimpleDayCell;
import org.vaadin.addon.calendar.client.ui.schedule.SimpleDayToolbar;
import org.vaadin.addon.calendar.client.ui.schedule.SimpleWeekToolbar;
import org.vaadin.addon.calendar.client.ui.schedule.WeekGrid;
import org.vaadin.addon.calendar.client.ui.schedule.WeeklyLongItems;
import org.vaadin.addon.calendar.client.ui.schedule.dd.CalendarDropHandler;
import org.vaadin.addon.calendar.client.ui.util.ItemDurationComparator;
import org.vaadin.addon.calendar.client.ui.util.StartDateComparator;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.dd.VHasDropHandler;

/**
 * Client side implementation for Calendar
 *
 * @since 7.1
 * @author Vaadin Ltd.
 */
@SuppressWarnings({"unused","deprecation"})
public class VCalendar extends Composite implements VHasDropHandler {

    public static final String PRIMARY_STYLE = "v-calendar";

    private static Logger getLogger() {
        return Logger.getLogger(VCalendar.class.getName());
    }

//    public static final String ATTR_FIRSTDAYOFWEEK = "firstDay";
//    public static final String ATTR_LASTDAYOFWEEK = "lastDay";
//    public static final String ATTR_FIRSTHOUROFDAY = "firstHour";
//    public static final String ATTR_LASTHOUROFDAY = "lastHour";

    // private boolean hideWeekends;
    private String[] monthNames;
    private String[] dayNames;
    private boolean format;
    private final DockPanel outer = new DockPanel();

    private boolean rangeSelectAllowed = true;
    private boolean rangeMoveAllowed = true;
    private boolean itemResizeAllowed = true;
    private boolean itemMoveAllowed = true;

    private final SimpleDayToolbar nameToolbar = new SimpleDayToolbar(this);
    private final DayToolbar dayToolbar = new DayToolbar(this);
    private final SimpleWeekToolbar weekToolbar;
    private WeeklyLongItems weeklyLongEvents;
    private MonthGrid monthGrid;
    private WeekGrid weekGrid;
    private int intWidth = 0;
    private int intHeight = 0;

    public static final DateTimeFormat ACTION_DATE_TIME_FORMAT = DateTimeFormat.getFormat(DateConstants.ACTION_DATE_TIME_FORMAT_PATTERN);
    public static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat(DateConstants.DATE_FORMAT_PATTERN);

    protected final DateTimeFormat time12format_date = DateTimeFormat.getFormat("h:mm a");
    protected final DateTimeFormat time24format_date = DateTimeFormat.getFormat("HH:mm");

    private boolean disabled = false;
    private boolean isHeightUndefined = false;
    private boolean isWidthUndefined = false;

    private int firstDay;
    private int lastDay;
    private int firstHour;
    private int lastHour;

    private CalendarState.ItemSortOrder itemSortOrder = CalendarState.ItemSortOrder.DURATION_DESC;

    private static ItemDurationComparator DEFAULT_COMPARATOR = new ItemDurationComparator(false);

    private CalendarDropHandler dropHandler;

    /**
     * Listener interface for listening to event click items
     */
    public interface DateClickListener {
        /**
         * Triggered when a date was clicked
         *
         * @param date
         *            The date and time that was clicked
         */
        void dateClick(CalDate date);
    }

    /**
     * Listener interface for listening to week number click items
     */
    public interface WeekClickListener {
        /**
         * Called when a week number was selected.
         *
         * @param event
         *            The format of the vent string is "<year>w<week>"
         */
        void weekClick(String event);
    }

    /**
     * Listener interface for listening to forward items
     */
    public interface ForwardListener {

        /**
         * Called when the calendar should move one view forward
         */
        void forward();
    }

    /**
     * Listener interface for listening to backward items
     */
    public interface BackwardListener {

        /**
         * Called when the calendar should move one view backward
         */
        void backward();
    }

    /**
     * Listener interface for listening to selection items
     */
    public interface RangeSelectListener {

        /**
         * Called when a user selected a new item by highlighting an area of
         * the calendar.
         *
         * @param dateSelectionRange
         *                  The selection range
         */
        void rangeSelected(SelectionRange dateSelectionRange);

    }

    /**
     * Listener interface for listening to click items
     */
    public interface ItemClickListener {
        /**
         * Called when an item was clicked
         *
         * @param item
         *            The item that was clicked
         */
        void itemClick(CalendarItem item);
    }

    /**
     * Listener interface for listening to item moved items. Occurs when a
     * user drags an item to a new position
     */
    public interface ItemMovedListener {
        /**
         * Triggered when an item was dragged to a new position and the start
         * and end dates was changed
         *
         * @param item
         *            The item that was moved
         */
        void itemMoved(CalendarItem item);
    }

    /**
     * Listener interface for when an item gets resized (its start or end date
     * changes)
     */
    public interface ItemResizeListener {
        /**
         * Triggers when the time limits for the item was changed.
         *
         * @param item
         *            The item that was changed. The new time limits have been
         *            updated in the item before calling this method
         */
        void itemResized(CalendarItem item);
    }

    /**
     * Listener interface for listening to scroll items.
     */
    public interface ScrollListener {
        /**
         * Triggered when the calendar is scrolled
         *
         * @param scrollPosition
         *            The scroll position in pixels as returned by
         *            {@link ScrollPanel#getScrollPosition()}
         */
        void scroll(int scrollPosition);
    }

    /**
     * Listener interface for listening to mouse items.
     */
    public interface MouseEventListener {
        /**
         * Triggered when a user wants an context menu
         *
         * @param event
         *            The context menu event
         *
         * @param widget
         *            The widget that the context menu should be added to
         */
        void contextMenu(ContextMenuEvent event, Widget widget);
    }

    /**
     * Default constructor
     */
    public VCalendar() {
        weekToolbar = new SimpleWeekToolbar(this);
        initWidget(outer);
        setStylePrimaryName(PRIMARY_STYLE);
        blockSelect(getElement());
    }

    /**
     * Hack for IE to not select text when dragging.
     *
     * @param e
     *            The element to apply the hack on
     */
    private native void blockSelect(Element e)
    /*-{
    	e.onselectstart = function() {
    		return false;
    	}

    	e.ondragstart = function() {
    		return false;
    	}
    }-*/;

    private void updateItemsToWeekGrid(CalendarItem[] items) {

        List<CalendarItem> allDayLong = new ArrayList<>();
        List<CalendarItem> belowDayLong = new ArrayList<>();

        for (CalendarItem item : items) {
            if (item.isAllDay()) {
                // Item is set on one "allDay" event or more than one.
                allDayLong.add(item);

            } else {
                // Item is set only on one day.
                belowDayLong.add(item);
            }
        }

        weeklyLongEvents.addItems(allDayLong);

        for (CalendarItem item : belowDayLong) {
            weekGrid.addItem(item);
        }
    }

    /**
     * Adds items to the month grid
     *
     * @param events
     *            The items to add
     * @param drawImmediately
     *            Should the grid be rendered immediately.
     */
    public void updateItemsToMonthGrid(Collection<CalendarItem> events, boolean drawImmediately) {
        for (CalendarItem e : sortItems(events)) {
            addItemToMonthGrid(e, drawImmediately);
        }
    }

    private void addItemToMonthGrid(CalendarItem e, boolean renderImmediately) {

        Date when = e.getStart();
        Date to = e.getEnd();

        boolean itemAdded = false;
        boolean inProgress = false; // Item adding has started
        boolean itemMoving = false;

        List<SimpleDayCell> dayCells = new ArrayList<>();
        List<SimpleDayCell> timeCells = new ArrayList<>();
        for (int row = 0; row < monthGrid.getRowCount(); row++) {

            if (itemAdded) {
                break;
            }

            for (int cell = 0; cell < monthGrid.getCellCount(row); cell++) {

                SimpleDayCell sdc = (SimpleDayCell) monthGrid.getWidget(row, cell);

                if (isItemInDay(when, to, sdc.getDate())
                        && isItemInDayWithTime(when, to, sdc.getDate(), e.getEndTime(), e.isAllDay())) {

                    if (!itemMoving) {
                        itemMoving = sdc.getMoveItem() != null;
                    }

                    long d = e.getRangeInMilliseconds();
                    if ((d > 0 && d <= DateConstants.DAYINMILLIS)
                            && !e.isAllDay()) {
                        timeCells.add(sdc);
                    } else {
                        dayCells.add(sdc);
                    }

                    inProgress = true;

                } else if (inProgress) {
                    itemAdded = true;
                    inProgress = false;
                    break;
                }
            }
        }

        updateItemSlotIndex(e, dayCells);
        updateItemSlotIndex(e, timeCells);

        for (SimpleDayCell sdc : dayCells) {
            sdc.addItem(e);
        }
        for (SimpleDayCell sdc : timeCells) {
            sdc.addItem(e);
        }

        if (renderImmediately) {
            reDrawAllMonthItems(!itemMoving);
        }
    }

    /*
     * We must also handle the special case when the event lasts exactly for 24
     * hours, thus spanning two days e.g. from 1.1.2001 00:00 to 2.1.2001 00:00.
     * That special case still should span one day when rendered.
     */
    @SuppressWarnings("deprecation")
    // Date methods are not deprecated in GWT
    private boolean isItemInDayWithTime(Date from, Date to, Date date, Date endTime, boolean isAllDay) {
        return (isAllDay || !(to.getDay() == date.getDay()
                && from.getDay() != to.getDay() && isMidnight(endTime)));
    }

    private void updateItemSlotIndex(CalendarItem item, List<SimpleDayCell> cells) {
        if (cells.isEmpty()) {
            return;
        }

        if (item.getSlotIndex() == -1) {
            // Update slot index
            int newSlot = -1;
            for (SimpleDayCell sdc : cells) {
                int slot = sdc.getItemCount();
                if (slot > newSlot) {
                    newSlot = slot;
                }
            }
            newSlot++;

            for (int i = 0; i < newSlot; i++) {
                // check for empty slot
                if (isSlotEmpty(item, i, cells)) {
                    newSlot = i;
                    break;
                }
            }
            item.setSlotIndex(newSlot);
        }
    }

    private void reDrawAllMonthItems(boolean clearCells) {
        for (int row = 0; row < monthGrid.getRowCount(); row++) {
            for (int cell = 0; cell < monthGrid.getCellCount(row); cell++) {
                SimpleDayCell sdc = (SimpleDayCell) monthGrid.getWidget(row, cell);
                sdc.reDraw(clearCells);
            }
        }
    }

    private boolean isSlotEmpty(CalendarItem addedEvent, int slotIndex, List<SimpleDayCell> cells) {
        for (SimpleDayCell sdc : cells) {
            CalendarItem item = sdc.getCalendarItem(slotIndex);
            if (item != null && !item.equals(addedEvent)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Remove a month event from the view
     *
     * @param target
     *            The event to remove
     *
     * @param repaintImmediately
     *            Should we repaint after the event was removed?
     */
    public void removeMonthItem(CalendarItem target, boolean repaintImmediately) {
        if (target != null && target.getSlotIndex() >= 0) {
            // Remove event
            for (int row = 0; row < monthGrid.getRowCount(); row++) {
                for (int cell = 0; cell < monthGrid.getCellCount(row); cell++) {

                    SimpleDayCell sdc = (SimpleDayCell) monthGrid.getWidget(row, cell);

                    if (sdc == null) return;

                    sdc.removeItem(target, repaintImmediately);
                }
            }
        }
    }

    /**
     * Updates an event in the month grid
     *
     * @param changedItem
     *            The event that has changed
     */
    public void updateItemToMonthGrid(CalendarItem changedItem) {
        removeMonthItem(changedItem, true);
        changedItem.setSlotIndex(-1);
        addItemToMonthGrid(changedItem, true);
    }

    /**
     * Sort the items by current sort order
     *
     * @param items
     *            The items to sort
     * @return An array where the items has been sorted
     */
    public CalendarItem[] sortItems(Collection<CalendarItem> items) {
        if (!CalendarState.ItemSortOrder.UNSORTED.equals(itemSortOrder)) {

            CalendarItem[] sorted = items.toArray(new CalendarItem[items.size()]);
            switch (itemSortOrder) {
                case START_DATE_ASC:
                    Arrays.sort(sorted, new StartDateComparator(true));
                    break;
                case START_DATE_DESC:
                    Arrays.sort(sorted, new StartDateComparator(false));
                    break;
                case DURATION_ASC:
                    Arrays.sort(sorted, new ItemDurationComparator(true));
                    break;
                case DURATION_DESC:
                default:
                    Arrays.sort(sorted, DEFAULT_COMPARATOR);
            }

            return sorted;
        }
        return items.toArray(new CalendarItem[items.size()]);
    }

    /*
     * Check if the given event occurs at the given date.
     */
    private boolean isItemInDay(Date when, Date to, Date gridDate) {
        return when.compareTo(gridDate) <= 0 && to.compareTo(gridDate) >= 0;
    }

    /**
     * Re-render the week grid
     *
     * @param days
     *            The days
     * @param today
     *            Todays date
     * @param realDayNames
     *            The names of the dates
     */
    @SuppressWarnings("deprecation")
    public void updateWeekGrid(List<CalendarDay> days, Date today, String[] realDayNames) {

        weekGrid.setFirstHour(getFirstHourOfTheDay());
        weekGrid.setLastHour(getLastHourOfTheDay());
        weekGrid.getTimeBar().updateTimeBar(is24HFormat());

        dayToolbar.clear();
        dayToolbar.addBackButton();
        dayToolbar.setVerticalSized(isHeightUndefined);
        dayToolbar.setHorizontalSized(isWidthUndefined);

        weekGrid.clearDates();
        weekGrid.setDisabled(isDisabled());

        for (CalendarDay day : days) {

            Date date = day.getDate();

            int dayOfWeek = day.getDayOfWeek();

            if (dayOfWeek < getFirstDayNumber() || dayOfWeek > getLastDayNumber()) {
                continue;
            }

            boolean isToday = false;
            int dayOfMonth = date.getDate();
            if (today.getDate() == dayOfMonth
                    && today.getYear() == date.getYear()
                    && today.getMonth() == date.getMonth()) {
                isToday = true;
            }

            dayToolbar.add(realDayNames[dayOfWeek - 1], date, day.getLocalizedDateFormat(), isToday ? "today" : null);
            weeklyLongEvents.addDate(date);
            weekGrid.addDate(date, day.getStyledSlots());

            if (isToday) {
                weekGrid.setToday(date, today);
            }
        }

        dayToolbar.addNextButton();
    }

    /**
     * Updates the items in the Month view
     *
     * @param daysCount
     *            How many days there are
     * @param days
     *            The days
     * @param today
     *            Todays date
     */
    @SuppressWarnings("deprecation")
    public void updateMonthGrid(int daysCount, List<CalendarDay> days, Date today) {

        int columns = getLastDayNumber() - getFirstDayNumber() + 1;
        int rows = (int) Math.ceil(daysCount / (double) 7);

        monthGrid = new MonthGrid(this, rows, columns);
        monthGrid.setEnabled(!isDisabled());
        weekToolbar.removeAllRows();
        int pos = 0;
        boolean monthNameDrawn = true;
        boolean firstDayFound = false;
        boolean lastDayFound = false;

        for (CalendarDay day : days) {

            Date date = day.getDate();

            int dayOfWeek = day.getDayOfWeek();
            int week = day.getWeek();
            int dayOfMonth = date.getDate();

            // reset at start of each month
            if (dayOfMonth == 1) {
                monthNameDrawn = false;
                if (firstDayFound) {
                    lastDayFound = true;
                }
                firstDayFound = true;
            }

            if (dayOfWeek < getFirstDayNumber() || dayOfWeek > getLastDayNumber()) {
                continue;
            }

            int y = (pos / columns);
            int x = pos - (y * columns);
            if (x == 0 && daysCount > 7) {
                // Add week to weekToolbar for navigation
                weekToolbar.addWeek(week, day.getYearOfWeek());
            }

            final SimpleDayCell cell = new SimpleDayCell(this, y, x);
            cell.setMonthGrid(monthGrid);
            cell.setDate(date);
            cell.addDomHandler(event -> {
                if (mouseEventListener != null) {
                    event.preventDefault();
                    event.stopPropagation();
                    mouseEventListener.contextMenu(event, cell);
                }
            }, ContextMenuEvent.getType());

            if (!firstDayFound) {
                cell.addStyleDependentName("prev-month");
            } else if (lastDayFound) {
                cell.addStyleDependentName("next-month");
            }

            if (dayOfMonth >= 1 && !monthNameDrawn) {
                cell.setMonthNameVisible(true);
                monthNameDrawn = true;
            }

            if (today.getDate() == dayOfMonth && today.getYear() == date.getYear()
                    && today.getMonth() == date.getMonth()) {
                cell.setToday(true);

            }
            monthGrid.setWidget(y, x, cell);
            pos++;
        }
    }

    public void setSizeForChildren(int newWidth, int newHeight) {
        intWidth = newWidth;
        intHeight = newHeight;
        isWidthUndefined = intWidth == -1;
        isHeightUndefined = intHeight == -1;
        dayToolbar.setVerticalSized(isHeightUndefined);
        dayToolbar.setHorizontalSized(isWidthUndefined);
        recalculateWidths();
        recalculateHeights();
    }

    /**
     * Recalculates the heights of the sub-components in the calendar
     */
    protected void recalculateHeights() {
        if (monthGrid != null) {

            if (intHeight == -1) {
                monthGrid.addStyleDependentName("sizedheight");
            } else {
                monthGrid.removeStyleDependentName("sizedheight");
            }

            monthGrid.updateCellSizes(
                    intWidth - weekToolbar.getOffsetWidth(),
                    intHeight - nameToolbar.getOffsetHeight());

            weekToolbar.setHeightPX((intHeight == -1) ? intHeight : intHeight - nameToolbar.getOffsetHeight());

        } else if (weekGrid != null) {
            weekGrid.setHeightPX((intHeight == -1) ? intHeight
                    : intHeight - weeklyLongEvents.getOffsetHeight()  - dayToolbar.getOffsetHeight());
        }
    }

    /**
     * Recalculates the widths of the sub-components in the calendar
     */
    protected void recalculateWidths() {
        if (!isWidthUndefined) {
            nameToolbar.setWidthPX(intWidth);
            dayToolbar.setWidthPX(intWidth);

            if (monthGrid != null) {
                monthGrid.updateCellSizes(
                        intWidth - weekToolbar.getOffsetWidth(),
                        intHeight - nameToolbar.getOffsetHeight());

            } else if (weekGrid != null) {
                weekGrid.setWidthPX(intWidth);
                weeklyLongEvents.setWidthPX(weekGrid.getInternalWidth());
            }
        } else {
            dayToolbar.setWidthPX(intWidth);
            nameToolbar.setWidthPX(intWidth);

            if (monthGrid != null) {
                if (intWidth == -1) {
                    monthGrid.addStyleDependentName("sizedwidth");

                } else {
                    monthGrid.removeStyleDependentName("sizedwidth");
                }
            } else if (weekGrid != null) {
                weekGrid.setWidthPX(intWidth);
                weeklyLongEvents.setWidthPX(weekGrid.getInternalWidth());
            }
        }
    }

    /**
     * Get the time format used to format time only (excludes date)
     *
     * @return the time format used to format time only (excludes date)
     */
    public DateTimeFormat getTimeFormat() {
        if (is24HFormat()) {
            return time24format_date;
        }
        return time12format_date;
    }

    /**
     * Is the component disabled
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Is the component disabled
     *
     * @param disabled
     *            True if disabled
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * Get the month grid component
     *
     * @return the month grid component
     */
    public MonthGrid getMonthGrid() {
        return monthGrid;
    }

    /**
     * Get the week grid component
     *
     * @return the week grid component
     */
    public WeekGrid getWeekGrid() {
        return weekGrid;
    }

    /**
     * Calculates correct size for all cells (size / amount of cells ) and
     * distributes any overflow over all the cells.
     *
     * @param totalSize
     *            the total amount of size reserved for all cells
     * @param numberOfCells
     *            the number of cells
     * @param sizeModifier
     *            a modifier which is applied to all cells before distributing
     *            the overflow
     * @return an integer array that contains the correct size for each cell
     */
    public static int[] distributeSize(int totalSize, int numberOfCells, int sizeModifier) {

        int[] cellSizes = new int[numberOfCells];
        int startingSize = totalSize / numberOfCells;
        int cellSizeOverFlow = totalSize % numberOfCells;

        for (int i = 0; i < numberOfCells; i++) {
            cellSizes[i] = startingSize + sizeModifier;
        }

        // distribute size overflow amongst all slots
        int j = 0;
        while (cellSizeOverFlow > 0) {
            cellSizes[j]++;
            cellSizeOverFlow--;
            j++;
            if (j >= numberOfCells) {
                j = 0;
            }
        }

        // cellSizes[numberOfCells - 1] += cellSizeOverFlow;

        return cellSizes;
    }

    /**
     * Is the date at midnight
     *
     * @param date
     *            The date to check
     *
     * @return true, if it's midnight
     */
    @SuppressWarnings("deprecation")
    public static boolean isMidnight(Date date) {
        return (date.getHours() == 0 && date.getMinutes() == 0
                && date.getSeconds() == 0);
    }

    /**
     * Are the dates equal (uses second resolution)
     *
     * @param date1
     *            The first the to compare
     * @param date2
     *            The second date to compare
     * @return true, if the dates equal
     */
    @SuppressWarnings("deprecation")
    public static boolean areDatesEqualToSecond(Date date1, Date date2) {
        return date1.getYear() == date2.getYear()
                && date1.getMonth() == date2.getMonth()
                && date1.getDay() == date2.getDay()
                && date1.getHours() == date2.getHours()
                && date1.getSeconds() == date2.getSeconds();
    }

    /**
     * Is the calendar event zero seconds long and is occurring at midnight
     *
     * @param event
     *            The event to check
     * @return true, if the calendar event is zero seconds long and is occurring at midnight
     */
    public static boolean isZeroLengthMidnightEvent(CalendarItem event) {
        return areDatesEqualToSecond(event.getStartTime(), event.getEndTime())
                && isMidnight(event.getEndTime());
    }

    /**
     * Should the 24h time format be used
     *
     * @param format
     *            True if the 24h format should be used else the 12h format is
     *            used
     */
    public void set24HFormat(boolean format) {
        this.format = format;
    }

    /**
     * Is the 24h time format used
     */
    public boolean is24HFormat() {
        return format;
    }

    /**
     * Set the names of the week days
     *
     * @param names
     *            The names of the days (Monday, Thursday,...)
     */
    public void setDayNames(String[] names) {
        assert (names.length == 7);
        dayNames = names;
    }

    /**
     * Get the names of the week days
     */
    public String[] getDayNames() {
        return dayNames;
    }

    /**
     * Set the names of the months
     *
     * @param names
     *            The names of the months (January, February,...)
     */
    public void setMonthNames(String[] names) {
        assert (names.length == 12);
        monthNames = names;
    }

    /**
     * Get the month names
     */
    public String[] getMonthNames() {
        return monthNames;
    }

    /**
     * Set the number when a week starts
     *
     * @param dayNumber
     *            The number of the day
     */
    public void setFirstDayNumber(int dayNumber) {
        assert (dayNumber >= 1 && dayNumber <= 7);
        firstDay = dayNumber;
    }

    /**
     * Get the number when a week starts
     */
    public int getFirstDayNumber() {
        return firstDay;
    }

    /**
     * Set the number when a week ends
     *
     * @param dayNumber
     *            The number of the day
     */
    public void setLastDayNumber(int dayNumber) {
        assert (dayNumber >= 1 && dayNumber <= 7);
        lastDay = dayNumber;
    }

    /**
     * Get the number when a week ends
     */
    public int getLastDayNumber() {
        return lastDay;
    }

    /**
     * Set the number when a week starts
     *
     * @param hour
     *            The number of the hour
     */
    public void setFirstHourOfTheDay(int hour) {
        assert (hour >= 0 && hour <= 23);
        firstHour = hour;
    }

    /**
     * Get the number when a week starts
     */
    public int getFirstHourOfTheDay() {
        return firstHour;
    }

    /**
     * Set the number when a week ends
     *
     * @param hour
     *            The number of the hour
     */
    public void setLastHourOfTheDay(int hour) {
        assert (hour >= 0 && hour <= 23);
        lastHour = hour;
    }

    /**
     * Get the number when a week ends
     */
    public int getLastHourOfTheDay() {
        return lastHour;
    }

    /**
     * Re-renders the whole week view
     *
     * @param scroll
     *            The amount of pixels to scroll the week view
     * @param today
     *            Todays date
     * @param firstDayOfWeek
     *            The first day of the week
     * @param events
     *            The items to render
     */
    public void updateWeekView(int scroll, Date today,int firstDayOfWeek,
                               Collection<CalendarItem> events, List<CalendarDay> days) {

        while (outer.getWidgetCount() > 0) {
            outer.remove(0);
        }

        monthGrid = null;

        String[] realDayNames = new String[getDayNames().length];
        int j = 0;

//        if (firstDayOfWeek == 2) {
            for (int i = 1; i < getDayNames().length; i++) {
                realDayNames[j++] = getDayNames()[i];
            }
            realDayNames[j] = getDayNames()[0];
//        } else {
//            for (int i = 0; i < getDayNames().length; i++) {
//                realDayNames[j++] = getDayNames()[i];
//            }
//        }

        weeklyLongEvents = new WeeklyLongItems(this);
        if (weekGrid == null) {
            weekGrid = new WeekGrid(this, is24HFormat());
        }

        updateWeekGrid(days, today, realDayNames);
        updateItemsToWeekGrid(sortItems(events));

        outer.add(dayToolbar, DockPanel.NORTH);
        outer.add(weeklyLongEvents, DockPanel.NORTH);
        outer.add(weekGrid, DockPanel.SOUTH);

        weekGrid.setVerticalScrollPosition(scroll);
    }

    /**
     * Re-renders the whole month view
     *
     * @param firstDayOfWeek
     *            The first day of the week
     * @param today
     *            Todays date
     * @param daysInMonth
     *            Amount of days in the month
     * @param events
     *            The items to render
     * @param days
     *            The day information
     */
    public void updateMonthView(int firstDayOfWeek, Date today, int daysInMonth,
                                Collection<CalendarItem> events, List<CalendarDay> days) {

        // Remove all week numbers from bar
        while (outer.getWidgetCount() > 0) {
            outer.remove(0);
        }

        int firstDay = getFirstDayNumber();
        int lastDay = getLastDayNumber();
        int daysPerWeek = lastDay - firstDay + 1;
        int j = 0;

        String[] dayNames = getDayNames();
        String[] realDayNames = new String[daysPerWeek];

        if (firstDayOfWeek == 2) {
            for (int i = firstDay; i < lastDay + 1; i++) {
                if (i == 7) {
                    realDayNames[j++] = dayNames[0];
                } else {
                    realDayNames[j++] = dayNames[i];
                }
            }
        } else {
            for (int i = firstDay - 1; i < lastDay; i++) {
                realDayNames[j++] = dayNames[i];
            }
        }

        nameToolbar.setDayNames(realDayNames);

        weeklyLongEvents = null;
        weekGrid = null;

        updateMonthGrid(daysInMonth, days, today);

        outer.add(nameToolbar, DockPanel.NORTH);
        outer.add(weekToolbar, DockPanel.WEST);
        weekToolbar.updateCellHeights();
        outer.add(monthGrid, DockPanel.CENTER);

        updateItemsToMonthGrid(events, false);
    }

    private DateClickListener dateClickListener;

    /**
     * Sets the listener for listening to event clicks
     *
     * @param listener
     *            The listener to use
     */
    public void setListener(DateClickListener listener) {
        dateClickListener = listener;
    }

    /**
     * Gets the listener for listening to event clicks
     *
     * @return the listener
     */
    public DateClickListener getDateClickListener() {
        return dateClickListener;
    }

    private ForwardListener forwardListener;

    /**
     * Set the listener which listens to forward items from the calendar
     *
     * @param listener
     *            The listener to use
     */
    public void setListener(ForwardListener listener) {
        forwardListener = listener;
    }

    /**
     * Get the listener which listens to forward items from the calendar
     *
     * @return the listener
     */
    public ForwardListener getForwardListener() {
        return forwardListener;
    }

    private BackwardListener backwardListener;

    /**
     * Set the listener which listens to backward items from the calendar
     *
     * @param listener
     *            The listener to use
     */
    public void setListener(BackwardListener listener) {
        backwardListener = listener;
    }

    /**
     * Set the listener which listens to backward items from the calendar
     *
     * @return the listener
     */
    public BackwardListener getBackwardListener() {
        return backwardListener;
    }

    private WeekClickListener weekClickListener;

    /**
     * Set the listener that listens to user clicking on the week numbers
     *
     * @param listener
     *            The listener to use
     */
    public void setListener(WeekClickListener listener) {
        weekClickListener = listener;
    }

    /**
     * Get the listener that listens to user clicking on the week numbers
     *
     * @return the listener
     */
    public WeekClickListener getWeekClickListener() {
        return weekClickListener;
    }

    private RangeSelectListener rangeSelectListener;

    /**
     * Set the listener that listens to the user highlighting a region in the
     * calendar
     *
     * @param listener
     *            The listener to use
     */
    public void setListener(RangeSelectListener listener) {
        rangeSelectListener = listener;
    }

    /**
     * Get the listener that listens to the user highlighting a region in the
     * calendar
     *
     * @return the listener
     */
    public RangeSelectListener getRangeSelectListener() {
        return rangeSelectListener;
    }

    private ItemClickListener itemClickListener;

    /**
     * Get the listener that listens to the user clicking on the items
     */
    public ItemClickListener getItemClickListener() {
        return itemClickListener;
    }

    /**
     * Set the listener that listens to the user clicking on the items
     *
     * @param listener
     *            The listener to use
     */
    public void setListener(ItemClickListener listener) {
        itemClickListener = listener;
    }

    private ItemMovedListener itemMovedListener;

    /**
     * Get the listener that listens to when event is dragged to a new location
     *
     * @return the listener
     */
    public ItemMovedListener getItemMovedListener() {
        return itemMovedListener;
    }

    /**
     * Set the listener that listens to when event is dragged to a new location
     *
     * @param itemMovedListener
     *            The listener to use
     */
    public void setListener(ItemMovedListener itemMovedListener) {
        this.itemMovedListener = itemMovedListener;
    }

    private ScrollListener scrollListener;

    /**
     * Get the listener that listens to when the calendar widget is scrolled
     *
     * @return the listener
     */
    public ScrollListener getScrollListener() {
        return scrollListener;
    }

    /**
     * Set the listener that listens to when the calendar widget is scrolled
     *
     * @param scrollListener
     *            The listener to use
     */
    public void setListener(ScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    private ItemResizeListener itemResizeListener;

    /**
     * Get the listener that listens to when an items time limits are being
     * adjusted
     *
     * @return the listener
     */
    public ItemResizeListener getItemResizeListener() {
        return itemResizeListener;
    }

    /**
     * Set the listener that listens to when an items time limits are being
     * adjusted
     *
     * @param itemResizeListener
     *            The listener to use
     */
    public void setListener(ItemResizeListener itemResizeListener) {
        this.itemResizeListener = itemResizeListener;
    }

    private MouseEventListener mouseEventListener;
    private boolean forwardNavigationEnabled = true;
    private boolean backwardNavigationEnabled = true;
    private boolean itemCaptionAsHtml = false;

    /**
     * Get the listener that listen to mouse items
     *
     * @return the listener
     */
    public MouseEventListener getMouseEventListener() {
        return mouseEventListener;
    }

    /**
     * Set the listener that listen to mouse items
     *
     * @param mouseEventListener
     *            The listener to use
     */
    public void setListener(MouseEventListener mouseEventListener) {
        this.mouseEventListener = mouseEventListener;
    }

    /**
     * Is selecting a range allowed?
     */
    public boolean isRangeSelectAllowed() {
        return rangeSelectAllowed;
    }

    /**
     * Set selecting a range allowed
     *
     * @param rangeSelectAllowed
     *            Should selecting a range be allowed
     */
    public void setRangeSelectAllowed(boolean rangeSelectAllowed) {
        this.rangeSelectAllowed = rangeSelectAllowed;
    }

    /**
     * Is moving a range allowed
     *
     * @return true, if moving a range is allowed
     */
    public boolean isRangeMoveAllowed() {
        return rangeMoveAllowed;
    }

    /**
     * Is moving a range allowed
     *
     * @param rangeMoveAllowed
     *            Is it allowed
     */
    public void setRangeMoveAllowed(boolean rangeMoveAllowed) {
        this.rangeMoveAllowed = rangeMoveAllowed;
    }

    /**
     * Is resizing an event allowed
     */
    public boolean isItemResizeAllowed() {
        return itemResizeAllowed;
    }

    /**
     * Is resizing an event allowed
     *
     * @param itemResizeAllowed
     *            True if allowed false if not
     */
    public void setItemResizeAllowed(boolean itemResizeAllowed) {
        this.itemResizeAllowed = itemResizeAllowed;
    }

    /**
     * Is moving an event allowed
     */
    public boolean isItemMoveAllowed() {
        return itemMoveAllowed;
    }

    /**
     * Is moving an event allowed
     *
     * @param itemMoveAllowed
     *            True if moving is allowed, false if not
     */
    public void setItemMoveAllowed(boolean itemMoveAllowed) {
        this.itemMoveAllowed = itemMoveAllowed;
    }

    public boolean isBackwardNavigationEnabled() {
        return backwardNavigationEnabled;
    }

    public void setBackwardNavigationEnabled(boolean enabled) {
        backwardNavigationEnabled = enabled;
    }

    public boolean isForwardNavigationEnabled() {
        return forwardNavigationEnabled;
    }

    public void setForwardNavigationEnabled(boolean enabled) {
        forwardNavigationEnabled = enabled;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.client.ui.dd.VHasDropHandler#getDropHandler()
     */
    @Override
    public CalendarDropHandler getDropHandler() {
        return dropHandler;
    }

    /**
     * Set the drop handler
     *
     * @param dropHandler
     *            The drophandler to use
     */
    public void setDropHandler(CalendarDropHandler dropHandler) {
        this.dropHandler = dropHandler;
    }

    /**
     * Sets whether the event captions are rendered as HTML.
     * <p>
     * If set to true, the captions are rendered in the browser as HTML and the
     * developer is responsible for ensuring no harmful HTML is used. If set to
     * false, the caption is rendered in the browser as plain text.
     * <p>
     * The default is false, i.e. to render that caption as plain text.
     *
     * @param itemCaptionAsHtml
     *            true if the captions are rendered as HTML, false if rendered
     *            as plain text
     */
    public void setItemCaptionAsHtml(boolean itemCaptionAsHtml) {
        this.itemCaptionAsHtml = itemCaptionAsHtml;
    }

    /**
     * Checks whether event captions are rendered as HTML
     * <p>
     * The default is false, i.e. to render that caption as plain text.
     *
     * @return true if the captions are rendered as HTML, false if rendered as
     *         plain text
     */
    public boolean isItemCaptionAsHtml() {
        return itemCaptionAsHtml;
    }

    /**
     * Set sort strategy for items.
     *
     * @param order
     *            sort order
     */
    public void setSortOrder(CalendarState.ItemSortOrder order) {
        if (order == null) {
            itemSortOrder = CalendarState.ItemSortOrder.DURATION_DESC;
        } else {
            itemSortOrder = order;
        }
    }

    /**
     * Return currently active sort order.
     *
     * @return current sort order
     */
    public CalendarState.ItemSortOrder getSortOrder() {
        return itemSortOrder;
    }

}
