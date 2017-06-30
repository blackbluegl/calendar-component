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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.FocusableFlowPanel;
import org.vaadin.addon.calendar.client.DateConstants;
import org.vaadin.addon.calendar.client.ui.VCalendar;

import java.util.Date;

/**
 * A class representing a single cell within the calendar in month-view
 *
 * @since 7.1
 * @author Vaadin Ltd.
 */
public class SimpleDayCell extends FocusableFlowPanel implements MouseUpHandler,
        MouseDownHandler, MouseOverHandler, MouseMoveHandler {

    private static int BOTTOMSPACERHEIGHT = -1;
    private static int EVENTHEIGHT = -1;
    private static final int BORDERPADDINGSIZE = 1;

    private final VCalendar calendar;
    private Date date;
    private int intHeight;
    private final HTML bottomspacer;
    private final Label caption;
    private final CalendarItem[] calendarItems = new CalendarItem[10];
    private final int cell;
    private final int row;
    private boolean monthNameVisible;
    private HandlerRegistration mouseUpRegistration;
    private HandlerRegistration mouseDownRegistration;
    private HandlerRegistration mouseOverRegistration;
    private boolean monthEventMouseDown;
    private boolean labelMouseDown;
    private int itemCount = 0;

    private int startX = -1;
    private int startY = -1;
    private int startYrelative;
    private int startXrelative;
    // "from" date of date which is source of Dnd
    private Date dndSourceDateFrom;
    // "to" date of date which is source of Dnd
    private Date dndSourceDateTo;
    // "from" time of date which is source of Dnd
    private Date dndSourceStartDateTime;
    // "to" time of date which is source of Dnd
    private Date dndSourceEndDateTime;

    private int prevDayDiff = 0;
    private int prevWeekDiff = 0;
    private HandlerRegistration moveRegistration;
    private CalendarItem moveEvent;
    private Widget clickedWidget;
    private HandlerRegistration bottomSpacerMouseDownHandler;
    private boolean scrollable = false;
    private MonthGrid monthGrid;
    private HandlerRegistration keyDownHandler;

    public SimpleDayCell(VCalendar calendar, int row, int cell) {
        this.calendar = calendar;
        this.row = row;
        this.cell = cell;
        setStylePrimaryName("v-calendar-month-day");
        caption = new Label();
        bottomspacer = new HTML();
        bottomspacer.setStyleName("v-calendar-bottom-spacer-empty");
        bottomspacer.setWidth(3 + "em");
        caption.setStyleName("v-calendar-day-number");
        add(caption);
        add(bottomspacer);
        caption.addMouseDownHandler(this);
        caption.addMouseUpHandler(this);
    }

    @Override
    public void onLoad() {
        BOTTOMSPACERHEIGHT = bottomspacer.getOffsetHeight();
        EVENTHEIGHT = BOTTOMSPACERHEIGHT;
    }

    public void setMonthGrid(MonthGrid monthGrid) {
        this.monthGrid = monthGrid;
    }

    public MonthGrid getMonthGrid() {
        return monthGrid;
    }

    @SuppressWarnings("deprecation")
    public void setDate(Date date) {
        int dateOfMonth = date.getDate();
        if (monthNameVisible) {
            caption.setText(dateOfMonth + " "
                    + calendar.getMonthNames()[date.getMonth()]);
        } else {
            caption.setText("" + dateOfMonth);
        }
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void reDraw(boolean clear) {
        setHeightPX(intHeight + BORDERPADDINGSIZE, clear);
    }

    /*
     * Events and whole cell content are drawn by this method. By the
     * clear-argument, you can choose to clear all old content. Notice that
     * clearing will also remove all element's event handlers.
     */
    public void setHeightPX(int px, boolean clear) {
        // measure from DOM if needed
        if (px < 0) {
            intHeight = getOffsetHeight() - BORDERPADDINGSIZE;
        } else {
            intHeight = px - BORDERPADDINGSIZE;
        }

        // Couldn't measure height or it ended up negative. Don't bother
        // continuing
        if (intHeight == -1) {
            return;
        }

        if (clear) {
            while (getWidgetCount() > 1) {
                remove(1);
            }
        }

        // How many calendarItems can be shown in UI
        int slots = 0;
        if (scrollable) {
            for (int i = 0; i < calendarItems.length; i++) {
                if (calendarItems[i] != null) {
                    slots = i + 1;
                }
            }
            setHeight(intHeight + "px"); // Fixed height
        } else {
            // Dynamic height by the content
            getElement().removeAttribute("height");
            slots = (intHeight - caption.getOffsetHeight() - BOTTOMSPACERHEIGHT)
                    / EVENTHEIGHT;
            if (slots > 10) {
                slots = 10;
            }
        }

        updateItems(slots, clear);

    }

    public void updateItems(int slots, boolean clear) {
        int eventsAdded = 0;

        for (int i = 0; i < slots; i++) {
            CalendarItem e = calendarItems[i];
            if (e == null) {
                // Empty slot
                HTML slot = new HTML();
                slot.setStyleName("v-calendar-spacer");
                if (!clear) {
                    remove(i + 1);
                    insert(slot, i + 1);
                } else {
                    add(slot);
                }
            } else {
                // Item slot
                eventsAdded++;
                if (!clear) {
                    Widget w = getWidget(i + 1);
                    if (!(w instanceof MonthItemLabel)) {
                        remove(i + 1);
                        insert(createMonthItemLabel(e), i + 1);
                    }
                } else {
                    add(createMonthItemLabel(e));
                }
            }
        }

        int remainingSpace = intHeight - ((slots * EVENTHEIGHT)
                + BOTTOMSPACERHEIGHT + caption.getOffsetHeight());
        int newHeight = remainingSpace + BOTTOMSPACERHEIGHT;
        if (newHeight < 0) {
            newHeight = EVENTHEIGHT;
        }
        bottomspacer.setHeight(newHeight + "px");

        if (clear) {
            add(bottomspacer);
        }

        int more = itemCount - eventsAdded;
        if (more > 0) {
            if (bottomSpacerMouseDownHandler == null) {
                bottomSpacerMouseDownHandler = bottomspacer
                        .addMouseDownHandler(this);
            }
            bottomspacer.setStyleName("v-calendar-bottom-spacer");
            bottomspacer.setText("+ " + more);
        } else {
            if (!scrollable && bottomSpacerMouseDownHandler != null) {
                bottomSpacerMouseDownHandler.removeHandler();
                bottomSpacerMouseDownHandler = null;
            }

            if (scrollable) {
                bottomspacer.setText("[ - ]");
            } else {
                bottomspacer.setStyleName("v-calendar-bottom-spacer-empty");
                bottomspacer.setText("");
            }
        }
    }

    private MonthItemLabel createMonthItemLabel(CalendarItem e) {
        long rangeInMillis = e.getRangeInMilliseconds();
        boolean timeEvent = rangeInMillis <= DateConstants.DAYINMILLIS
                && !e.isAllDay();
        Date fromDatetime = e.getStartTime();

        // Create a new MonthItemLabel
        MonthItemLabel eventDiv = new MonthItemLabel();
        eventDiv.addStyleDependentName("month");
        eventDiv.addMouseDownHandler(this);
        eventDiv.addMouseUpHandler(this);
        eventDiv.setCalendar(calendar);
        eventDiv.setItemIndex(e.getIndex());
        eventDiv.setCalendarItem(e);

        if (timeEvent) {
            eventDiv.setTimeSpecificEvent(true);
            if (e.getStyleName() != null) {
                eventDiv.addStyleDependentName(e.getStyleName());
            }
            eventDiv.setCaption(e.getCaption());
            eventDiv.setTime(fromDatetime);

        } else {
            eventDiv.setTimeSpecificEvent(false);
            Date from = e.getStart();
            Date to = e.getEnd();
            if (e.getStyleName().length() > 0) {
                eventDiv.addStyleName("month-event " + e.getStyleName());
            } else {
                eventDiv.addStyleName("month-event");
            }
            int fromCompareToDate = from.compareTo(date);
            int toCompareToDate = to.compareTo(date);
            eventDiv.addStyleDependentName("all-day");
            if (fromCompareToDate == 0) {
                eventDiv.addStyleDependentName("start");
                eventDiv.setCaption(e.getCaption());

            } else if (fromCompareToDate < 0 && cell == 0) {
                eventDiv.addStyleDependentName("continued-from");
                eventDiv.setCaption(e.getCaption());
            }
            if (toCompareToDate == 0) {
                eventDiv.addStyleDependentName("end");
            } else if (toCompareToDate > 0
                    && (cell + 1) == getMonthGrid().getCellCount(row)) {
                eventDiv.addStyleDependentName("continued-to");
            }
            if (e.getStyleName() != null) {
                eventDiv.addStyleDependentName(e.getStyleName() + "-all-day");
            }
        }

        return eventDiv;
    }

    private void setUnlimitedCellHeight() {
        scrollable = true;
        addStyleDependentName("scrollable");
    }

    private void setLimitedCellHeight() {
        scrollable = false;
        removeStyleDependentName("scrollable");
    }

    public void addItem(CalendarItem e) {
        itemCount++;
        int slot = e.getSlotIndex();
        if (slot == -1) {
            for (int i = 0; i < calendarItems.length; i++) {
                if (calendarItems[i] == null) {
                    calendarItems[i] = e;
                    e.setSlotIndex(i);
                    break;
                }
            }
        } else {
            calendarItems[slot] = e;
        }
    }

    @SuppressWarnings("deprecation")
    public void setMonthNameVisible(boolean b) {
        monthNameVisible = b;
        int dateOfMonth = date.getDate();
        caption.setText(
                dateOfMonth + " " + calendar.getMonthNames()[date.getMonth()]);
    }

    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
        return addDomHandler(handler, MouseMoveEvent.getType());
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        mouseUpRegistration = addDomHandler(this, MouseUpEvent.getType());
        mouseDownRegistration = addDomHandler(this, MouseDownEvent.getType());
        mouseOverRegistration = addDomHandler(this, MouseOverEvent.getType());
    }

    @Override
    protected void onDetach() {
        mouseUpRegistration.removeHandler();
        mouseDownRegistration.removeHandler();
        mouseOverRegistration.removeHandler();
        super.onDetach();
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
        if (event.getNativeButton() != NativeEvent.BUTTON_LEFT) {
            return;
        }

        Widget w = (Widget) event.getSource();
        if (moveRegistration != null) {
            Event.releaseCapture(getElement());
            moveRegistration.removeHandler();
            moveRegistration = null;
            keyDownHandler.removeHandler();
            keyDownHandler = null;
        }

        if (w == bottomspacer && monthEventMouseDown) {
            GWT.log("Mouse up over bottomspacer");

        } else if (clickedWidget instanceof MonthItemLabel
                && monthEventMouseDown) {
            MonthItemLabel mel = (MonthItemLabel) clickedWidget;

            int endX = event.getClientX();
            int endY = event.getClientY();
            int xDiff = 0, yDiff = 0;
            if (startX != -1 && startY != -1) {
                xDiff = startX - endX;
                yDiff = startY - endY;
            }
            startX = -1;
            startY = -1;
            prevDayDiff = 0;
            prevWeekDiff = 0;

            if (xDiff < -3 || xDiff > 3 || yDiff < -3 || yDiff > 3) {
                itemMoved(moveEvent);

            } else if (calendar.getItemClickListener() != null) {
                CalendarItem e = getItemByWidget(mel);
                calendar.getItemClickListener().itemClick(e);
            }

            moveEvent = null;
        } else if (w == this) {
            getMonthGrid().setSelectionReady();

        } else if (w instanceof Label && labelMouseDown) {
            String clickedDate = calendar.getDateFormat().format(date);
            if (calendar.getDateClickListener() != null) {
                calendar.getDateClickListener().dateClick(clickedDate);
            }
        }
        monthEventMouseDown = false;
        labelMouseDown = false;
        clickedWidget = null;
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {
        if (calendar.isDisabled()
                || event.getNativeButton() != NativeEvent.BUTTON_LEFT) {
            return;
        }

        Widget w = (Widget) event.getSource();
        clickedWidget = w;

        if (w instanceof MonthItemLabel) {
            // event clicks should be allowed even when read-only
            monthEventMouseDown = true;

            if (calendar.isItemMoveAllowed()) {
                startCalendarItemDrag(event, (MonthItemLabel) w);
            }
        } else if (w == bottomspacer) {
            if (scrollable) {
                setLimitedCellHeight();
            } else {
                setUnlimitedCellHeight();
            }
            reDraw(true);
        } else if (w instanceof Label) {
            labelMouseDown = true;
        } else if (w == this && !scrollable) {
            MonthGrid grid = getMonthGrid();
            if (grid.isEnabled() && calendar.isRangeSelectAllowed()) {
                grid.setSelectionStart(this);
                grid.setSelectionEnd(this);
            }
        }

        event.stopPropagation();
        event.preventDefault();
    }

    @Override
    public void onMouseOver(MouseOverEvent event) {
        event.preventDefault();
        getMonthGrid().setSelectionEnd(this);
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {

        if (clickedWidget instanceof MonthItemLabel && !monthEventMouseDown
                || (startY < 0 && startX < 0)) {
            return;
        }

        MonthItemLabel w = (MonthItemLabel) clickedWidget;

        if (calendar.isDisabled()) {
            Event.releaseCapture(getElement());
            monthEventMouseDown = false;
            startY = -1;
            startX = -1;
            return;
        }

        int currentY = event.getClientY();
        int currentX = event.getClientX();
        int moveY = (currentY - startY);
        int moveX = (currentX - startX);
        if ((moveY < 5 && moveY > -6) && (moveX < 5 && moveX > -6)) {
            return;
        }

        int dateCellWidth = getWidth();
        int dateCellHeigth = getHeigth();

        Element parent = getMonthGrid().getElement();
        int relativeX = event.getRelativeX(parent);
        int relativeY = event.getRelativeY(parent);
        int weekDiff = 0;
        if (moveY > 0) {
            weekDiff = (startYrelative + moveY) / dateCellHeigth;
        } else {
            weekDiff = (moveY - (dateCellHeigth - startYrelative))
                    / dateCellHeigth;
        }

        int dayDiff = 0;
        if (moveX >= 0) {
            dayDiff = (startXrelative + moveX) / dateCellWidth;
        } else {
            dayDiff = (moveX - (dateCellWidth - startXrelative))
                    / dateCellWidth;
        }
        // Check boundaries
        if (relativeY < 0
                || relativeY >= (calendar.getMonthGrid().getRowCount()
                        * dateCellHeigth)
                || relativeX < 0
                || relativeX >= (calendar.getMonthGrid().getColumnCount()
                        * dateCellWidth)) {
            return;
        }

        GWT.log("Item moving delta: " + weekDiff + " weeks " + dayDiff
                + " days" + " (" + getCell() + "," + getRow() + ")");

        CalendarItem e = moveEvent;
        if (e == null) {
            e = getItemByWidget(w);
        }

        Date from = e.getStart();
        Date to = e.getEnd();

        long daysMs = dayDiff * DateConstants.DAYINMILLIS;
        long weeksMs = weekDiff * DateConstants.WEEKINMILLIS;

        setDates(e, from, to, weeksMs + daysMs, false);
        e.setStart(from);
        e.setEnd(to);
        if (w.isTimeSpecificEvent()) {
            Date start = new Date();
            Date end = new Date();
            setDates(e, start, end, weeksMs + daysMs, true);
            e.setStartTime(start);
            e.setEndTime(end);
        } else {
            e.setStartTime(new Date(from.getTime()));
            e.setEndTime(new Date(to.getTime()));
        }

        updateDragPosition(w, dayDiff, weekDiff);
    }

    private void setDates(CalendarItem e, Date start, Date end, long shift,
                          boolean isDateTime) {
        Date currentStart;
        Date currentEnd;
        if (isDateTime) {
            currentStart = e.getStartTime();
            currentEnd = e.getEndTime();
        } else {
            currentStart = e.getStart();
            currentEnd = e.getEnd();
        }
        long duration = currentEnd.getTime() - currentStart.getTime();
        if (isDateTime) {
            start.setTime(dndSourceStartDateTime.getTime() + shift);
        } else {
            start.setTime(dndSourceDateFrom.getTime() + shift);
        }
        end.setTime((start.getTime() + duration));
    }

    private void itemMoved(CalendarItem e) {
        calendar.updateItemToMonthGrid(e);
        if (calendar.getItemMovedListener() != null) {
            calendar.getItemMovedListener().itemMoved(e);
        }
    }

    public void startCalendarItemDrag(MouseDownEvent event, final MonthItemLabel label) {

        moveRegistration = addMouseMoveHandler(this);
        startX = event.getClientX();
        startY = event.getClientY();
        startYrelative = event.getRelativeY(label.getParent().getElement())
                % getHeigth();
        startXrelative = event.getRelativeX(label.getParent().getElement())
                % getWidth();

        CalendarItem e = getItemByWidget(label);
        dndSourceDateFrom = (Date) e.getStart().clone();
        dndSourceDateTo = (Date) e.getEnd().clone();

        dndSourceStartDateTime = (Date) e.getStartTime().clone();
        dndSourceEndDateTime = (Date) e.getEndTime().clone();

        Event.setCapture(getElement());
        keyDownHandler = addKeyDownHandler(keyDownHandler -> {
            if (keyDownHandler.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
                cancelItemDrag(label);
            }
        });

        focus();

        GWT.log("Start drag");
    }

    protected void cancelItemDrag(MonthItemLabel label) {
        if (moveRegistration != null) {
            // reset position
            if (moveEvent == null) {
                moveEvent = getItemByWidget(label);
            }

            moveEvent.setStart(dndSourceDateFrom);
            moveEvent.setEnd(dndSourceDateTo);
            moveEvent.setStartTime(dndSourceStartDateTime);
            moveEvent.setEndTime(dndSourceEndDateTime);
            calendar.updateItemToMonthGrid(moveEvent);

            // reset drag-related properties
            Event.releaseCapture(getElement());
            moveRegistration.removeHandler();
            moveRegistration = null;
            keyDownHandler.removeHandler();
            keyDownHandler = null;
            setFocus(false);
            monthEventMouseDown = false;
            startY = -1;
            startX = -1;
            moveEvent = null;
            labelMouseDown = false;
            clickedWidget = null;
        }
    }

    public void updateDragPosition(MonthItemLabel label, int dayDiff,
                                   int weekDiff) {
        // Draw event to its new position only when position has changed
        if (dayDiff == prevDayDiff && weekDiff == prevWeekDiff) {
            return;
        }

        prevDayDiff = dayDiff;
        prevWeekDiff = weekDiff;

        if (moveEvent == null) {
            moveEvent = getItemByWidget(label);
        }

        calendar.updateItemToMonthGrid(moveEvent);
    }

    public int getRow() {
        return row;
    }

    public int getCell() {
        return cell;
    }

    public int getHeigth() {
        return intHeight + BORDERPADDINGSIZE;
    }

    public int getWidth() {
        return getOffsetWidth() - BORDERPADDINGSIZE;
    }

    public void setToday(boolean today) {
        if (today) {
            addStyleDependentName("today");
        } else {
            removeStyleDependentName("today");
        }
    }

    public boolean removeItem(CalendarItem targetEvent, boolean reDrawImmediately) {
        int slot = targetEvent.getSlotIndex();
        if (slot < 0) {
            return false;
        }

        CalendarItem e = getCalendarItem(slot);
        if (targetEvent.equals(e)) {
            calendarItems[slot] = null;
            itemCount--;
            if (reDrawImmediately) {
                reDraw(moveEvent == null);
            }
            return true;
        }
        return false;
    }

    private CalendarItem getItemByWidget(MonthItemLabel eventWidget) {
        int index = getWidgetIndex(eventWidget);
        return getCalendarItem(index - 1);
    }

    public CalendarItem getCalendarItem(int i) {
        return calendarItems[i];
    }

    public CalendarItem[] getCalendarItems() {
        return calendarItems;
    }

    public int getItemCount() {
        return itemCount;
    }

    public CalendarItem getMoveItem() {
        return moveEvent;
    }

    public void addEmphasisStyle() {
        addStyleDependentName("dragemphasis");
    }

    public void removeEmphasisStyle() {
        removeStyleDependentName("dragemphasis");
    }
}
