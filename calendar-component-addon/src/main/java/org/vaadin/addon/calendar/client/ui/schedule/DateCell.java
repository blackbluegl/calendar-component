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

import com.google.gwt.dom.client.*;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.WidgetUtil;
import org.vaadin.addon.calendar.client.DateConstants;

import java.util.*;

public class DateCell extends FocusableComplexPanel
        implements MouseDownHandler, MouseMoveHandler, MouseUpHandler, KeyDownHandler, ContextMenuHandler {

    private static final String DRAGEMPHASISSTYLE = " dragemphasis";

    private Date date;
    private int width;
    private int eventRangeStart = -1;
    private int eventRangeStop = -1;
    final WeekGrid weekgrid;
    private boolean disabled = false;
    private int height;
    private final Element[] slotElements;
    private final List<DateCellSlot> slots = new ArrayList<>();
    private int[] slotElementHeights;
    private int startingSlotHeight;
    private Date today;
    private Element todaybar;
    private final List<HandlerRegistration> handlers;
    private final int numberOfSlots;
    private final int firstHour;
    private final int lastHour;

    public class DateCellSlot extends Widget {

        private final DateCell cell;

        private final Date from;

        private final Date to;

        private final Boolean enabled;

        public DateCellSlot(DateCell cell, Date from, Date to, Boolean enabled) {
            setElement(Document.get().createDivElement());
            getElement().setInnerHTML("&nbsp;");
            this.cell = cell;
            this.from = from;
            this.to = to;
            this.enabled = enabled;
        }

        public Date getFrom() {
            return from;
        }

        public Date getTo() {
            return to;
        }

        public DateCell getParentCell() {
            return cell;
        }

        public Boolean getEnabled() {
            return enabled;
        }
    }

    public DateCell(WeekGrid parent, Date date, Set<Long> blockedSlots) {
        weekgrid = parent;
        Element mainElement = DOM.createDiv();
        setElement(mainElement);
        makeFocusable();
        setDate(date);

        addStyleName("v-calendar-day-times");

        handlers = new LinkedList<>();

        // 2 slots / hour
        firstHour = weekgrid.getFirstHour();
        lastHour = weekgrid.getLastHour();
        numberOfSlots = (lastHour - firstHour + 1) * 2;

        long slotTime = Math.round(((lastHour - firstHour + 1) * 3600000.0) / numberOfSlots);

        slotElements = new Element[numberOfSlots];
        slotElementHeights = new int[numberOfSlots];

        slots.clear();

        long dateTime = getDate().getTime();
        long start = dateTime + firstHour * 3600000;
        long end = start + slotTime;

        for (int i = 0; i < numberOfSlots; i++) {

            boolean blocked = blockedSlots != null && blockedSlots.contains(start-dateTime);

            DateCellSlot slot = new DateCellSlot(this, new Date(start), new Date(end), blocked);
            if (i % 2 == 0) {
                slot.setStyleName(blocked ? "v-datecellslot-blocked-even" : "v-datecellslot-even");
            } else {
                slot.setStyleName(blocked ? "v-datecellslot-blocked" : "v-datecellslot");
            }

            Event.sinkEvents(slot.getElement(), Event.MOUSEEVENTS);

            mainElement.appendChild(slot.getElement());
            slotElements[i] = slot.getElement();
            slots.add(slot);
            start = end;
            end = start + slotTime;
        }

        // Sink items for tooltip handling
        Event.sinkEvents(mainElement, Event.MOUSEEVENTS);
    }

    public int getFirstHour() {
        return firstHour;
    }

    public int getLastHour() {
        return lastHour;
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        handlers.add(addHandler(this, MouseDownEvent.getType()));
        handlers.add(addHandler(this, MouseUpEvent.getType()));
        handlers.add(addHandler(this, MouseMoveEvent.getType()));
        handlers.add(addDomHandler(this, ContextMenuEvent.getType()));
        handlers.add(addKeyDownHandler(this));
    }

    @Override
    protected void onDetach() {
        for (HandlerRegistration handler : handlers) {
            handler.removeHandler();
        }
        handlers.clear();

        super.onDetach();
    }

    public int getSlotIndex(Element slotElement) {
        for (int i = 0; i < slotElements.length; i++) {
            if (slotElement == slotElements[i]) {
                return i;
            }
        }

        throw new IllegalArgumentException(
                "Element not found in this DateCell");
    }

    public DateCellSlot getSlot(int index) {
        return slots.get(index);
    }

    public int getNumberOfSlots() {
        return numberOfSlots;
    }

    public void setTimeBarWidth(int timebarWidth) {
        todaybar.getStyle().setWidth(timebarWidth, Unit.PX);
    }

    /**
     * @param isHorizontalSized
     *            if true, this DateCell is sized with CSS and not via
     *            {@link #setWidthPX(int)}
     */
    public void setHorizontalSized(boolean isHorizontalSized) {
        if (isHorizontalSized) {
            addStyleDependentName("Hsized");

            width = getOffsetWidth()
                    - WidgetUtil.measureHorizontalBorder(getElement());
            // Update moveWidth for any DateCellDayItem child
            updateEventCellsWidth();
            recalculateItemWidths();
        } else {
            removeStyleDependentName("Hsized");
        }
    }

    /**
     * @param isVerticalSized
     *            if true, this DateCell is sized with CSS
     */
    public void setVerticalSized(boolean isVerticalSized) {
        if (isVerticalSized) {
            addStyleDependentName("Vsized");

            // recalc heights&size for items. all other height sizes come
            // from css
            startingSlotHeight = slotElements[0].getOffsetHeight();
            // Update slotHeight for each DateCellDayItem child
            updateEventCellsHeight();
            recalculateEventPositions();

            if (isToday()) {
                recalculateTimeBarPosition();
            }

        } else {
            removeStyleDependentName("Vsized");
        }
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setWidthPX(int cellWidth) {
        width = cellWidth;
        setWidth(cellWidth + "px");
        recalculateItemWidths();
    }

    public void setHeightPX(int height, int[] cellHeights) {
        this.height = height;
        slotElementHeights = cellHeights;
        setHeight(height + "px");
        recalculateCellHeights();
        recalculateEventPositions();
        if (today != null) {
            recalculateTimeBarPosition();
        }
    }

    // date methods are not deprecated in GWT
    @SuppressWarnings("deprecation")
    private void recalculateTimeBarPosition() {
        int h = today.getHours();
        int m = today.getMinutes();
        if (h >= firstHour && h <= lastHour) {
            int pixelTop = weekgrid.getPixelTopFor(m + 60 * h);
            todaybar.getStyle().clearDisplay();
            todaybar.getStyle().setTop(pixelTop, Unit.PX);
        } else {
            todaybar.getStyle().setDisplay(Display.NONE);
        }
    }

    private void recalculateEventPositions() {
        for (int i = 0; i < getWidgetCount(); i++) {
            DateCellDayItem dayEvent = (DateCellDayItem) getWidget(i);
            updatePositionFor(dayEvent, getDate(), dayEvent.getCalendarItem());
        }
    }

    public void recalculateItemWidths() {
        List<DateCellGroup> groups = new ArrayList<>();

        int count = getWidgetCount();

        List<Integer> handled = new ArrayList<>();

        // Iterate through all items and group them. Events that overlaps
        // with each other, are added to the same group.
        for (int i = 0; i < count; i++) {
            if (handled.contains(i)) {
                continue;
            }

            DateCellGroup curGroup = getOverlappingEvents(i);
            handled.addAll(curGroup.getItems());

            boolean newGroup = true;

            // No need to check other groups, if size equals the count
            if (curGroup.getItems().size() != count) {

                // Check other groups. When the whole group overlaps with
                // other group, the group is merged to the other.
                for (DateCellGroup g : groups) {

                    if (WeekGridMinuteTimeRange.doesOverlap(
                            curGroup.getDateRange(), g.getDateRange())) {

                        newGroup = false;
                        updateGroup(g, curGroup);
                    }
                }

            }
            else {

                if (newGroup) {
                    groups.add(curGroup);
                }

                break;
            }

            if (newGroup) {
                groups.add(curGroup);
            }
        }

        drawDayItems(groups);
    }

    private void recalculateCellHeights() {
        startingSlotHeight = height / numberOfSlots;

        for (int i = 0; i < slotElements.length; i++) {
            slotElements[i].getStyle().setHeight(slotElementHeights[i], Unit.PX);
        }

        updateEventCellsHeight();
    }

    public int getSlotHeight() {
        return startingSlotHeight;
    }

    public int getSlotBorder() {
        return WidgetUtil.measureVerticalBorder(slotElements[0]);
    }

    private void drawDayItems(List<DateCellGroup> groups) {
        for (DateCellGroup g : groups) {
            int col = 0;
            int colCount = 0;
            List<Integer> order = new ArrayList<>();
            Map<Integer, Integer> columns = new HashMap<>();
            for (Integer eventIndex : g.getItems()) {
                DateCellDayItem d = (DateCellDayItem) getWidget(eventIndex);
                d.setMoveWidth(width);

                int freeSpaceCol = findFreeColumnSpaceOnLeft(
                        new WeekGridMinuteTimeRange(
                                d.getCalendarItem().getStartTime(),
                                d.getCalendarItem().getEndTime()),
                        order, columns);
                if (freeSpaceCol >= 0) {
                    col = freeSpaceCol;
                    columns.put(eventIndex, col);
                    int newOrderindex = 0;
                    for (Integer i : order) {
                        if (columns.get(i) >= col) {
                            newOrderindex = order.indexOf(i);
                            break;
                        }
                    }
                    order.add(newOrderindex, eventIndex);
                } else {
                    // New column
                    col = colCount++;
                    columns.put(eventIndex, col);
                    order.add(eventIndex);
                }
            }

            // Update widths and left position
            int eventWidth = (width / colCount);
            for (Integer index : g.getItems()) {
                DateCellDayItem d = (DateCellDayItem) getWidget(index);
                d.getElement().getStyle().setMarginLeft(
                        (eventWidth * columns.get(index)), Unit.PX);
                d.setWidth(eventWidth + "px");
                d.setSlotHeightInPX(getSlotHeight());
            }
        }
    }

    private int findFreeColumnSpaceOnLeft(WeekGridMinuteTimeRange dateRange,
            List<Integer> order, Map<Integer, Integer> columns) {
        int freeSpot = -1;
        int skipIndex = -1;
        for (Integer eventIndex : order) {
            int col = columns.get(eventIndex);
            if (col == skipIndex) {
                continue;
            }

            if (freeSpot != -1 && freeSpot != col) {
                // Free spot found
                return freeSpot;
            }

            DateCellDayItem d = (DateCellDayItem) getWidget(eventIndex);
            WeekGridMinuteTimeRange nextRange = new WeekGridMinuteTimeRange(
                    d.getCalendarItem().getStartTime(),
                    d.getCalendarItem().getEndTime());

            if (WeekGridMinuteTimeRange.doesOverlap(dateRange, nextRange)) {
                skipIndex = col;
                freeSpot = -1;
            } else {
                freeSpot = col;
            }
        }

        return freeSpot;
    }

    /* Update top and bottom date range values. Add new index to the group. */
    private void updateGroup(DateCellGroup targetGroup, DateCellGroup byGroup) {
        Date newStart = targetGroup.getStart();
        Date newEnd = targetGroup.getEnd();
        if (byGroup.getStart().before(targetGroup.getStart())) {
            newStart = byGroup.getEnd();
        }
        if (byGroup.getStart().after(targetGroup.getEnd())) {
            newStart = byGroup.getStart();
        }

        targetGroup.setDateRange(new WeekGridMinuteTimeRange(newStart, newEnd));

        for (Integer index : byGroup.getItems()) {
            if (!targetGroup.getItems().contains(index)) {
                targetGroup.add(index);
            }
        }
    }

    /**
     * Returns all overlapping DayEvent indexes in the Group. Including the
     * target.
     *
     * @param targetIndex
     *            Index of DayEvent in the current DateCell widget.
     * @return Group that contains all Overlapping DayEvent indexes
     */
    public DateCellGroup getOverlappingEvents(int targetIndex) {
        DateCellGroup g = new DateCellGroup(targetIndex);

        int count = getWidgetCount();
        DateCellDayItem target = (DateCellDayItem) getWidget(targetIndex);
        WeekGridMinuteTimeRange targetRange = new WeekGridMinuteTimeRange(
                target.getCalendarItem().getStartTime(),
                target.getCalendarItem().getEndTime());
        Date groupStart = targetRange.getStart();
        Date groupEnd = targetRange.getEnd();

        for (int i = 0; i < count; i++) {
            if (targetIndex == i) {
                continue;
            }

            DateCellDayItem d = (DateCellDayItem) getWidget(i);
            WeekGridMinuteTimeRange nextRange = new WeekGridMinuteTimeRange(
                    d.getCalendarItem().getStartTime(),
                    d.getCalendarItem().getEndTime());
            if (WeekGridMinuteTimeRange.doesOverlap(targetRange, nextRange)) {
                g.add(i);

                // Update top & bottom values to the greatest
                if (nextRange.getStart().before(targetRange.getStart())) {
                    groupStart = targetRange.getStart();
                }
                if (nextRange.getEnd().after(targetRange.getEnd())) {
                    groupEnd = targetRange.getEnd();
                }
            }
        }

        g.setDateRange(new WeekGridMinuteTimeRange(groupStart, groupEnd));
        return g;
    }

    public Date getDate() {
        return date;
    }

    public void addItem(Date targetDay, CalendarItem calendarItem) {

        Element main = getElement();

        DateCellDayItem dayItem = new DateCellDayItem(this, weekgrid, calendarItem);
        dayItem.setSlotHeightInPX(getSlotHeight());
        dayItem.setDisabled(isDisabled());

        if (startingSlotHeight > 0) {
            updatePositionFor(dayItem, targetDay, calendarItem);
        }

        add(dayItem, main);
    }

    // date methods are not deprecated in GWT
    @SuppressWarnings("deprecation")
    private void updatePositionFor(DateCellDayItem dayItem, Date targetDay, CalendarItem calendarItem) {

        if (shouldDisplay(calendarItem)) {
            dayItem.getElement().getStyle().clearDisplay();

            Date fromDt = calendarItem.getStartTime();
            int h = fromDt.getHours();
            int m = fromDt.getMinutes();
            long range = calendarItem.getRangeInMinutesForDay(targetDay);

            boolean onDifferentDays = calendarItem.isTimeOnDifferentDays();
            if (onDifferentDays) {
                if (calendarItem.getStart().compareTo(targetDay) != 0) {
                    // Current day slot is for the end date and all in-between
                    // days. Lets fix also the start & end times.
                    h = 0;
                    m = 0;
                }
            }

            int startFromMinutes = (h * 60) + m;
            dayItem.updatePosition(startFromMinutes, range);
        } else {
            dayItem.getElement().getStyle().setDisplay(Display.NONE);
        }
    }

    public void addItem(DateCellDayItem dayItem) {

        Element main = getElement();
        int index = 0;
        List<CalendarItem> items = new ArrayList<>();

        // items are the only widgets in this panel
        // slots are just elements
        for (; index < getWidgetCount(); index++) {
            DateCellDayItem dc = (DateCellDayItem) getWidget(index);
            dc.setDisabled(isDisabled());
            items.add(dc.getCalendarItem());
        }
        items.add(dayItem.getCalendarItem());

        index = 0;
        for (CalendarItem e : weekgrid.getCalendar().sortItems(items)) {
            if (e.equals(dayItem.getCalendarItem())) {
                break;
            }
            index++;
        }
        this.insert(dayItem, main, index, true);
    }

    public void removeEvent(DateCellDayItem dayEvent) {
        remove(dayEvent);
    }

    /**
     *
     * @param item The Calendar Item
     * @return
     *
     *         This method is not necessary in the long run.. Or here can be
     *         various types of implementations..
     */
    // Date methods not deprecated in GWT
    @SuppressWarnings("deprecation")
    private boolean shouldDisplay(CalendarItem item) {
        boolean display = true;
        if (item.isTimeOnDifferentDays()) {
            display = true;
        } else { // only in case of one-day event we are able not to display
                 // event
                 // which is placed in unpublished parts on calendar
            Date eventStart = item.getStartTime();
            Date eventEnd = item.getEndTime();

            int eventStartHours = eventStart.getHours();
            int eventEndHours = eventEnd.getHours();

            /*
             * Special case (#14737): if event end time is 00:00 of the
             * following day then isTimeOnDifferentDays() returns false
             * (according to logic of this method), so this case should be
             * handled here
             */
            if (!item.getStart().equals(item.getEnd())
                    && (item.getEndTime().getHours() == 0
                            && item.getEndTime().getMinutes() == 0)) {
                eventEndHours = 23;
            }

            display = !(eventEndHours < firstHour
                    || eventStartHours > lastHour);
        }
        return display;
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
        int keycode = event.getNativeEvent().getKeyCode();
        if (keycode == KeyCodes.KEY_ESCAPE && eventRangeStart > -1) {
            cancelRangeSelect();
        }
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {
        if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
            Element e = Element.as(event.getNativeEvent().getEventTarget());
            if (e.getClassName().contains("reserved") || isDisabled()
                    || !weekgrid.getParentCalendar().isRangeSelectAllowed()) {
                eventRangeStart = -1;
            } else {
                eventRangeStart = event.getY();
                eventRangeStop = eventRangeStart;
                Event.setCapture(getElement());
                setFocus(true);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onMouseUp(MouseUpEvent event) {

        if (event.getNativeButton() != NativeEvent.BUTTON_LEFT) {
            return;
        }

        Event.releaseCapture(getElement());
        setFocus(false);

        // Drag initialized?
        if (eventRangeStart >= 0) {
            Element main = getElement();
            if (eventRangeStart > eventRangeStop) {
                if (eventRangeStop <= -1) {
                    eventRangeStop = 0;
                }
                int temp = eventRangeStart;
                eventRangeStart = eventRangeStop;
                eventRangeStop = temp;
            }

            // This happens for single clicks without dragging on the calendar
            if(eventRangeStart == eventRangeStop) {
                handleEventRange(event);
            }

            NodeList<Node> nodes = main.getChildNodes();

            int slotStart = -1;
            int slotEnd = -1;

            // iterate over all child nodes, until we find first the start,
            // and then the end
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.getItem(i);
                boolean isRangeElement = element.getClassName().contains("v-daterange");

                if (isRangeElement && slotStart == -1) {
                    slotStart = i;
                    slotEnd = i; // to catch one-slot selections

                } else if (isRangeElement) {
                    slotEnd = i;

                } else if (slotStart != -1 && slotEnd != -1) {
                    break; // FIXME ! is 'else if' right
                }
            }

            clearSelectionRange();

            int startMinutes = firstHour * 60 + slotStart * 30;
            int endMinutes = (firstHour * 60) + (slotEnd + 1) * 30;
            Date currentDate = getDate();

            if (weekgrid.getCalendar().getRangeSelectListener() != null) {

                SelectionRange weekSelection = new SelectionRange();
                weekSelection.sMin = startMinutes;
                weekSelection.eMin = endMinutes;
                weekSelection.setStartDay(DateConstants.toRPCDate(
                        currentDate.getYear(),
                        currentDate.getMonth(),
                        currentDate.getDate()));

                weekgrid.getCalendar().getRangeSelectListener().rangeSelected(weekSelection);
            }
            eventRangeStart = -1;

        } else {

            // Click event
            eventRangeStart = -1;
            cancelRangeSelect();
        }
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        handleEventRange(event);
    }

    private void handleEventRange(final MouseEvent event) {

        if (event.getNativeButton() != NativeEvent.BUTTON_LEFT) {
            return;
        }

        if (eventRangeStart >= 0) {

            int newY = event.getY();
            int fromY, toY;

            if (newY < eventRangeStart) {
                fromY = newY;
                toY = eventRangeStart;
            } else {
                fromY = eventRangeStart;
                toY = newY;
            }

            eventRangeStop = newY;

            Element main = getElement();
            NodeList<Node> nodes = main.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {

                Element c = (Element) nodes.getItem(i);

                if (Arrays.stream(slotElements).anyMatch(e -> e == c)) {

                    int elemStart = c.getOffsetTop();
                    int elemStop = elemStart + getSlotHeight();
                    if (elemStart >= fromY && elemStart <= toY) {
                        c.addClassName("v-daterange");
                    } else if (elemStop >= fromY && elemStop <= toY) {
                        c.addClassName("v-daterange");
                    } else if (elemStop >= fromY && elemStart <= toY) {
                        c.addClassName("v-daterange");
                    } else {
                        c.removeClassName("v-daterange");
                    }
                }
            }
        }

        event.preventDefault();
    }

    public void cancelRangeSelect() {
        Event.releaseCapture(getElement());
        setFocus(false);

        clearSelectionRange();
    }

    private void clearSelectionRange() {
        if (eventRangeStart > -1) {
            // clear all "selected" class names
            Element main = getElement();
            NodeList<Node> nodes = main.getChildNodes();

            for (int i = 0; i <= 47; i++) {
                Element c = (Element) nodes.getItem(i);
                if (c == null) {
                    continue;
                }
                c.removeClassName("v-daterange");
            }

            eventRangeStart = -1;
        }
    }

    public void setToday(Date today, int width) {
        this.today = today;
        addStyleDependentName("today");
        Element lastChild = (Element) getElement().getLastChild();
        if (lastChild.getClassName().equals("v-calendar-current-time")) {
            todaybar = lastChild;
        } else {
            todaybar = DOM.createDiv();
            todaybar.setClassName("v-calendar-current-time");
            getElement().appendChild(todaybar);
        }

        if (width != -1) {
            todaybar.getStyle().setWidth(width, Unit.PX);
        }

        // position is calculated later, when we know the cell heights
    }

    public Element getTodaybarElement() {
        return todaybar;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDateColor(String styleName) {
        this.setStyleName("v-calendar-datecell " + styleName);
    }

    public boolean isToday() {
        return today != null;
    }

    /**
     * @since 7.2
     */
    public void addEmphasisStyle(Element elementOver) {
        String originalStylename = getStyleName(elementOver);
        setStyleName(elementOver, originalStylename + DRAGEMPHASISSTYLE);
    }

    /**
     * @since 7.2
     */
    public void removeEmphasisStyle(Element elementOver) {
        String originalStylename = getStyleName(elementOver);
        setStyleName(elementOver, originalStylename.substring(0,
                originalStylename.length() - DRAGEMPHASISSTYLE.length()));
    }

    @Override
    public void onContextMenu(ContextMenuEvent event) {
        if (weekgrid.getCalendar().getMouseEventListener() != null) {
            event.preventDefault();
            event.stopPropagation();
            weekgrid.getCalendar().getMouseEventListener().contextMenu(event,
                    DateCell.this);
        }
    }

    private void updateEventCellsWidth() {
        for (Widget widget : getChildren()) {
            if (widget instanceof DateCellDayItem) {
                ((DateCellDayItem) widget).setMoveWidth(width);
            }
        }
    }

    private void updateEventCellsHeight() {
        for (Widget widget : getChildren()) {
            if (widget instanceof DateCellDayItem) {
                ((DateCellDayItem) widget).setSlotHeightInPX(getSlotHeight());
            }
        }
    }
}
