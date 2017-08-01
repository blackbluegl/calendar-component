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
package org.vaadin.addon.calendar.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.*;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.client.ui.Action;
import com.vaadin.client.ui.ActionOwner;
import com.vaadin.client.ui.SimpleManagedLayout;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.Connect.LoadStyle;
import com.vaadin.shared.util.SharedUtil;
import org.vaadin.addon.calendar.client.ui.VCalendar;
import org.vaadin.addon.calendar.client.ui.schedule.*;
import org.vaadin.addon.calendar.client.ui.schedule.dd.CalendarDropHandler;
import org.vaadin.addon.calendar.client.ui.schedule.dd.CalendarMonthDropHandler;
import org.vaadin.addon.calendar.client.ui.schedule.dd.CalendarWeekDropHandler;

import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles communication between Calendar on the server side and
 * {@link VCalendar} on the client side.
 *
 * @author Vaadin Ltd.
 * @since 7.1
 */
@Connect(value = org.vaadin.addon.calendar.Calendar.class, loadStyle = LoadStyle.LAZY)
public class CalendarConnector extends AbstractComponentConnector
        implements ActionOwner, SimpleManagedLayout , Paintable {

    private final HashMap<String, String> actionMap = new HashMap<>();
    private CalendarServerRpc rpc = RpcProxy.create(CalendarServerRpc.class, this);
    private HashMap<Object, String> tooltips = new HashMap<>();
    private List<String> actionKeys = new ArrayList<>();

    private static final String DROPHANDLER_ACCEPT_CRITERIA_PAINT_TAG = "-ac";

    /**
     *
     */
    public CalendarConnector() {

        // Listen to items
        registerListeners();
    }

    @Override
    protected void init() {
        super.init();
        registerRpc(CalendarClientRpc.class, (CalendarClientRpc) scrollPosition -> {
            // TODO widget scroll
        });
        getLayoutManager().registerDependency(this, getWidget().getElement());
    }

    @Override
    public void onUnregister() {
        super.onUnregister();
        getLayoutManager().unregisterDependency(this, getWidget().getElement());
    }

    @Override
    public VCalendar getWidget() {
        return (VCalendar) super.getWidget();
    }

    @Override
    public CalendarState getState() {
        return (CalendarState) super.getState();
    }

    /**
     * Registers listeners on the calendar so server can be notified of the
     * items
     */
    protected void registerListeners() {
        getWidget().setListener((VCalendar.DateClickListener) date -> {
            if (!getWidget().isDisabled()
                    && hasEventListener(CalendarEventId.DATECLICK)) {
                rpc.dateClick(date);
            }
        });
        getWidget().setListener((VCalendar.ForwardListener) () -> {
            if (hasEventListener(CalendarEventId.FORWARD)) {
                rpc.forward();
            }
        });
        getWidget().setListener((VCalendar.BackwardListener) () -> {
            if (hasEventListener(CalendarEventId.BACKWARD)) {
                rpc.backward();
            }
        });
        getWidget().setListener((VCalendar.RangeSelectListener) rangeSelection -> {
            if (hasEventListener(CalendarEventId.RANGESELECT)) {
                rpc.rangeSelect(rangeSelection);
            }
        });
        getWidget().setListener((VCalendar.WeekClickListener) event -> {
            if (!getWidget().isDisabled()
                    && hasEventListener(CalendarEventId.WEEKCLICK)) {
                rpc.weekClick(event);
            }
        });
        getWidget().setListener((VCalendar.ItemMovedListener) item -> {
            if (hasEventListener(CalendarEventId.ITEM_MOVE)) {
                rpc.itemMove(item.getIndex(), DateConstants.toRPCDateTime(item.getStartTime()));
            }
        });
        getWidget().setListener((VCalendar.ItemResizeListener) item -> {
            if (hasEventListener(CalendarEventId.ITEM_RESIZE)) {
                rpc.itemResize(item.getIndex(),
                        DateConstants.toRPCDateTime(item.getStartTime()),
                        DateConstants.toRPCDateTime(item.getEndTime()));
            }
        });
        getWidget().setListener((VCalendar.ScrollListener) scrollPosition -> {
            // This call is @Delayed (== non-immediate)
            rpc.scroll(scrollPosition);
        });
        getWidget().setListener((VCalendar.ItemClickListener) item -> {
            if (hasEventListener(CalendarEventId.ITEM_CLICK)) {
                rpc.itemClick(item.getIndex());
            }
        });
        getWidget().setListener((event, widget) -> {
            final NativeEvent ne = event.getNativeEvent();
            int left = ne.getClientX();
            int top = ne.getClientY();
            top += Window.getScrollTop();
            left += Window.getScrollLeft();
            getClient().getContextMenu().showAt(new ActionOwner() {
                @Override
                public String getPaintableId() {
                    return CalendarConnector.this.getPaintableId();
                }

                @Override
                public ApplicationConnection getClient() {
                    return CalendarConnector.this.getClient();
                }

                @Override
                @SuppressWarnings("deprecation")
                public Action[] getActions() {

                    if (widget instanceof SimpleDayCell) {
                        /*
                         * Month view
                         */
                        SimpleDayCell cell = (SimpleDayCell) widget;
                        Date start = new Date(cell.getDate().getYear(),
                                cell.getDate().getMonth(),
                                cell.getDate().getDate(), 0, 0, 0);

                        Date end = new Date(cell.getDate().getYear(),
                                cell.getDate().getMonth(),
                                cell.getDate().getDate(), 23, 59, 59);

                        return CalendarConnector.this.getActionsBetween(start, end);

                    } else if (widget instanceof MonthItemLabel) {
                        MonthItemLabel mel = (MonthItemLabel) widget;
                        CalendarItem event = mel.getCalendarItem();
                        Action[] actions = CalendarConnector.this.getActionsBetween(event.getStartTime(),
                                        event.getEndTime());
                        for (Action action : actions) {
                            ((VCalendarAction) action).setEvent(event);
                        }
                        return actions;

                    } else if (widget instanceof DateCell) {
                        /*
                         * Week and Day view
                         */
                        DateCell cell = (DateCell) widget;
                        int slotIndex = DOM.getChildIndex(cell.getElement(), ne.getEventTarget().cast());
                        DateCell.DateCellSlot slot = cell.getSlot(slotIndex);
                        return CalendarConnector.this.getActionsBetween(slot.getFrom(), slot.getTo());

                    } else if (widget instanceof DateCellDayItem) {
                        /*
                         * Context menu on event
                         */
                        DateCellDayItem dayEvent = (DateCellDayItem) widget;
                        CalendarItem event = dayEvent.getCalendarItem();

                        Action[] actions = CalendarConnector.this.getActionsBetween(event.getStartTime(),
                                        event.getEndTime());

                        for (Action action : actions) {
                            ((VCalendarAction) action).setEvent(event);
                        }

                        return actions;
                    }
                    return null;
                }
            }, left, top);
        });
    }

    private boolean showingMonthView() {
        return getState().days.size() > 7;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.terminal.gwt.client.Paintable#updateFromUIDL(com.vaadin.
     * terminal .gwt.client.UIDL,
     * com.vaadin.terminal.gwt.client.ApplicationConnection)
        */
    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        Iterator<Object> childIterator = uidl.getChildIterator();
        while (childIterator.hasNext()) {
            UIDL child = (UIDL) childIterator.next();
            if (DROPHANDLER_ACCEPT_CRITERIA_PAINT_TAG.equals(child.getTag())) {
                if (getWidget().getDropHandler() == null) {
                    getWidget().setDropHandler(showingMonthView()
                            ? new CalendarMonthDropHandler(this)
                            : new CalendarWeekDropHandler(this));
                }
                getWidget().getDropHandler().updateAcceptRules(child);
            } else {
                getWidget().setDropHandler(null);
            }
        }
    }


    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        CalendarState state = getState();
        VCalendar widget = getWidget();

        // Enable or disable the forward and backward navigation buttons
        widget.setForwardNavigationEnabled(hasEventListener(CalendarEventId.FORWARD));
        widget.setBackwardNavigationEnabled(hasEventListener(CalendarEventId.BACKWARD));

        widget.set24HFormat(state.format24H);
        widget.setDayNames(state.dayNames);
        widget.setMonthNames(state.monthNames);
        widget.setFirstDayNumber(state.firstVisibleDayOfWeek);
        widget.setLastDayNumber(state.lastVisibleDayOfWeek);
        widget.setFirstHourOfTheDay(state.firstHourOfDay);
        widget.setLastHourOfTheDay(state.lastHourOfDay);
        widget.setDisabled(!state.enabled);

        widget.setRangeSelectAllowed(hasEventListener(CalendarEventId.RANGESELECT));
        widget.setRangeMoveAllowed(hasEventListener(CalendarEventId.ITEM_MOVE));
        widget.setItemMoveAllowed(hasEventListener(CalendarEventId.ITEM_MOVE));
        widget.setItemResizeAllowed(hasEventListener(CalendarEventId.ITEM_RESIZE));

        widget.setItemCaptionAsHtml(state.itemCaptionAsHtml);

        CalendarState.ItemSortOrder oldOrder = getWidget().getSortOrder();
        if (!SharedUtil.equals(oldOrder, getState().itemSortOrder)) {
            getWidget().setSortOrder(getState().itemSortOrder);
        }

        updateView();

        updateSizes();

        registerEventToolTips(state.items);
        updateActionMap(state.actions);
    }

    /**
     * Returns the ApplicationConnection used to connect to the server side
     */
    @Override
    public ApplicationConnection getClient() {
        return getConnection();
    }

    /**
     * Register the description of the items as tooltips. This way, any event
     * displaying widget can use the event index as a key to display the
     * tooltip.
     */
    private void registerEventToolTips(List<CalendarState.Item> items) {
        for (CalendarState.Item e : items) {
            if (e.description != null && !"".equals(e.description)) {
                tooltips.put(e.index, e.description);
            } else {
                tooltips.remove(e.index);
            }
        }
    }

    @Override
    public TooltipInfo getTooltipInfo(Element element) {

        TooltipInfo tooltipInfo = null;

        Widget w = WidgetUtil.findWidget(element, DateCellDayItem.class);

        if (w instanceof HasTooltipKey) {
            String title = tooltips.get(((HasTooltipKey) w).getTooltipKey());
            tooltipInfo = new TooltipInfo(title != null ? title : "", getState().descriptionContentMode,null, this);
        }

        if (tooltipInfo == null) {
            tooltipInfo = super.getTooltipInfo(element);
        }

        return tooltipInfo;
    }

    @Override
    public boolean hasTooltip() {
        /*
         * Tooltips are not processed until updateFromUIDL, so we can't be sure
         * that there are no tooltips during onStateChange when this is used.
         */
        return true;
    }

    private void updateView() {

        CalendarState state = getState();
        List<CalendarState.Day> days = state.days;
        List<CalendarState.Item> items = state.items;

        CalendarDropHandler dropHandler = getWidget().getDropHandler();
        if (showingMonthView()) {
            updateMonthView(days, items);
            if (dropHandler != null
                    && !(dropHandler instanceof CalendarMonthDropHandler)) {
                getWidget().setDropHandler(new CalendarMonthDropHandler(this));
            }
        } else {
            updateWeekView(days, items);
            if (dropHandler != null
                    && !(dropHandler instanceof CalendarWeekDropHandler)) {
                getWidget().setDropHandler(new CalendarWeekDropHandler(this));
            }
        }
    }

    private void updateMonthView(List<CalendarState.Day> days,
                                 List<CalendarState.Item> items) {
        CalendarState state = getState();
        getWidget().updateMonthView(state.firstDayOfWeek,
                DateConstants.toClientDateTime(state.now),
                days.size(),
                calendarEventListOf(items, state.format24H),
                calendarDayListOf(days));
    }

    private void updateWeekView(List<CalendarState.Day> days, List<CalendarState.Item> items) {

        CalendarState state = getState();
        getWidget().updateWeekView(
                state.scroll,
                DateConstants.toClientDateTime(state.now),
                state.firstDayOfWeek,
                calendarEventListOf(items, state.format24H),
                calendarDayListOf(days)
        );
    }

    private Action[] getActionsBetween(Date start, Date end) {
        List<Action> actions = new ArrayList<>();
        List<String> ids = new ArrayList<>();

        for (String actionKey : actionKeys) {

            String id = getActionID(actionKey);
            if (!ids.contains(id)) {

                Date actionStartDate;
                Date actionEndDate;
                try {
                    actionStartDate = getActionStartDate(actionKey);
                    actionEndDate = getActionEndDate(actionKey);
                } catch (ParseException pe) {
                    Logger.getLogger(CalendarConnector.class.getName()).
                            log(Level.SEVERE, "Failed to parse action date");
                    continue;
                }

                // Case 0: action inside event timeframe
                // Action should start AFTER or AT THE SAME TIME as the event,
                // and
                // Action should end BEFORE or AT THE SAME TIME as the event
                boolean test0 = actionStartDate.compareTo(start) >= 0
                        && actionEndDate.compareTo(end) <= 0;

                // Case 1: action intersects start of timeframe
                // Action end time must be between start and end of event
                boolean test1 = actionEndDate.compareTo(start) > 0
                        && actionEndDate.compareTo(end) <= 0;

                // Case 2: action intersects end of timeframe
                // Action start time must be between start and end of event
                boolean test2 = actionStartDate.compareTo(start) >= 0
                        && actionStartDate.compareTo(end) < 0;

                // Case 3: event inside action timeframe
                // Action should start AND END before the event is complete
                boolean test3 = start.compareTo(actionStartDate) >= 0
                        && end.compareTo(actionEndDate) <= 0;

                if (test0 || test1 || test2 || test3) {
                    VCalendarAction a = new VCalendarAction(this, rpc, actionKey);
                    a.setCaption(getActionCaption(actionKey));
                    a.setIconUrl(getActionIcon(actionKey));
                    a.setActionStartDate(start);
                    a.setActionEndDate(end);
                    actions.add(a);
                    ids.add(id);
                }
            }
        }

        return actions.toArray(new Action[actions.size()]);
    }

    private void updateActionMap(List<CalendarState.Action> actions) {
        actionMap.clear();
        actionKeys.clear();

        if (actions == null) {
            return;
        }

        for (CalendarState.Action action : actions) {
            String id = action.actionKey + "-" + action.startDate + "-"
                    + action.endDate;
            actionMap.put(id + "_k", action.actionKey);
            actionMap.put(id + "_c", action.caption);
            actionMap.put(id + "_s", action.startDate);
            actionMap.put(id + "_e", action.endDate);
            actionKeys.add(id);
            if (action.iconKey != null) {
                actionMap.put(id + "_i", getResourceUrl(action.iconKey));

            } else {
                actionMap.remove(id + "_i");
            }
        }

        Collections.sort(actionKeys);
    }

    /**
     * Get the original action ID that was passed in from the shared state
     *
     * @param actionKey the unique action key
     * @return The original action ID that was passed in from the shared state
     * @since 7.1.2
     */
    public String getActionID(String actionKey) {
        return actionMap.get(actionKey + "_k");
    }

    /**
     * Get the text that is displayed for a context menu item
     *
     * @param actionKey The unique action key
     * @return The text that is displayed for a context menu item
     */
    public String getActionCaption(String actionKey) {
        return actionMap.get(actionKey + "_c");
    }

    /**
     * Get the icon url for a context menu item
     *
     * @param actionKey The unique action key
     * @return The icon url for a context menu item
     */
    public String getActionIcon(String actionKey) {
        return actionMap.get(actionKey + "_i");
    }

    /**
     * Get the start date for an action item
     *
     * @param actionKey The unique action key
     * @return The start date for an action item
     * @throws ParseException on parse
     */
    public Date getActionStartDate(String actionKey) throws ParseException {
        String dateStr = actionMap.get(actionKey + "_s");
        return VCalendar.ACTION_DATE_TIME_FORMAT.parse(dateStr);
    }

    /**
     * Get the end date for an action item
     *
     * @param actionKey The unique action key
     * @return The end date for an action item
     * @throws ParseException on parse
     */
    public Date getActionEndDate(String actionKey) throws ParseException {
        String dateStr = actionMap.get(actionKey + "_e");
        return VCalendar.ACTION_DATE_TIME_FORMAT.parse(dateStr);
    }

    /**
     * Returns ALL currently registered items.
     */

    @Override
    public Action[] getActions() {
        List<Action> actions = new ArrayList<>();

        for (final String actionKey : actionKeys) {
            final VCalendarAction a = new VCalendarAction(this, rpc, actionKey);
            a.setCaption(getActionCaption(actionKey));
            a.setIconUrl(getActionIcon(actionKey));

            try {
                a.setActionStartDate(getActionStartDate(actionKey));
                a.setActionEndDate(getActionEndDate(actionKey));
            } catch (ParseException pe) {
                Logger.getLogger(CalendarConnector.class.getName()).log(Level.SEVERE, "", pe);
            }

            actions.add(a);
        }

        return actions.toArray(new Action[actions.size()]);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.terminal.gwt.client.ui.ActionOwner#getPaintableId()
     */
    @Override
    public String getPaintableId() {
        return getConnectorId();
    }

    private List<CalendarItem> calendarEventListOf(List<CalendarState.Item> items, boolean format24h) {

        List<CalendarItem> list = new ArrayList<>(items.size());

        for (CalendarState.Item item : items) {

            // TODO replace with timestamps or object states
            final String dateFrom = item.dateFrom;
            final String dateTo = item.dateTo;
            final String timeFrom = item.timeFrom;
            final String timeTo = item.timeTo;

            CalendarItem calendarItem = new CalendarItem();
            calendarItem.setFormat24h(format24h);
            calendarItem.setAllDay(item.allDay);
            calendarItem.setCaption(item.caption);
            calendarItem.setDescription(item.description);
            calendarItem.setStyleName(item.styleName);
            calendarItem.setIndex(item.index);
            calendarItem.setMoveable(item.moveable);
            calendarItem.setResizeable(item.resizeable);
            calendarItem.setClickable(item.clickable);

            calendarItem.setStart(VCalendar.DATE_FORMAT.parse(dateFrom));
            calendarItem.setEnd(VCalendar.DATE_FORMAT.parse(dateTo));
            calendarItem.setStartTime(VCalendar.ACTION_DATE_TIME_FORMAT.parse(dateFrom + " " + timeFrom));
            calendarItem.setEndTime(VCalendar.ACTION_DATE_TIME_FORMAT.parse(dateTo + " " + timeTo));

            list.add(calendarItem);
        }
        return list;
    }

    private List<CalendarDay> calendarDayListOf(List<CalendarState.Day> days) {
        List<CalendarDay> list = new ArrayList<>(days.size());
        for (CalendarState.Day day : days) {
            CalendarDay d = new CalendarDay(
                    DateConstants.toClientDate(day.date),
                    day.localizedDateFormat, day.dayOfWeek, day.week, day.yearOfWeek, day.blockedSlots);
            list.add(d);
        }
        return list;
    }

    @Override
    public void layout() {
        updateSizes();
    }

    private void updateSizes() {
        int height = getLayoutManager()
                .getOuterHeight(getWidget().getElement());
        int width = getLayoutManager().getOuterWidth(getWidget().getElement());

        if (isUndefinedWidth()) {
            width = -1;
        }
        if (isUndefinedHeight()) {
            height = -1;
        }

        getWidget().setSizeForChildren(width, height);

    }
}
