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
package org.vaadin.addon.calendar;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.DropTarget;
import com.vaadin.event.dd.TargetDetails;
import com.vaadin.server.KeyMapper;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.declarative.DesignAttributeHandler;
import com.vaadin.ui.declarative.DesignContext;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.vaadin.addon.calendar.client.CalendarEventId;
import org.vaadin.addon.calendar.client.CalendarServerRpc;
import org.vaadin.addon.calendar.client.CalendarState;
import org.vaadin.addon.calendar.client.DateConstants;
import org.vaadin.addon.calendar.client.ui.schedule.CalDate;
import org.vaadin.addon.calendar.client.ui.schedule.CalTime;
import org.vaadin.addon.calendar.client.ui.schedule.SelectionRange;
import org.vaadin.addon.calendar.handler.*;
import org.vaadin.addon.calendar.item.BasicItemProvider;
import org.vaadin.addon.calendar.item.CalendarItem;
import org.vaadin.addon.calendar.item.CalendarItemProvider;
import org.vaadin.addon.calendar.item.EditableCalendarItem;
import org.vaadin.addon.calendar.ui.*;

import java.lang.reflect.Method;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * <p>
 * Vaadin Calendar is for visualizing items in a calendar. Calendar items can
 * be visualized in the variable length view depending on the start and end
 * dates.
 * </p>
 *
 * <li>You can set the viewable date range with the {@link #setStartDate(ZonedDateTime)}
 * and {@link #setEndDate(ZonedDateTime)} methods. Calendar has a default date range of
 * one week</li>
 *
 * <li>Calendar has two kind of views: monthly and weekly view</li>
 *
 * <li>If date range is seven days or shorter, the weekly view is used.</li>
 *
 * <li>Calendar queries its items by using a {@link CalendarItemProvider}. By
 * default, a {@link BasicItemProvider} is used.</li>
 *
 * @since 7.1
 * @author Vaadin Ltd.
 *
 */
@SuppressWarnings("serial")

public class Calendar<ITEM extends EditableCalendarItem> extends AbstractComponent implements
        CalendarComponentEvents.NavigationNotifier,
        CalendarComponentEvents.ItemMoveNotifier,
        CalendarComponentEvents.RangeSelectNotifier,
        CalendarComponentEvents.ItemResizeNotifier,
        CalendarItemProvider.ItemSetChangedListener,
        DropTarget,
        Action.Container
        //,LegacyComponent
        ,CalendarItemProvider<ITEM>
{

    /**
     * Calendar can use either 12 hours clock or 24 hours clock.
     */
    public enum TimeFormat {
        Format12H(), Format24H()
    }

    /** Defines currently active format for time. 12H/24H. */
    protected TimeFormat currentTimeFormat;

    /** Defines the component's active time zone. */
    protected ZoneId zoneId = ZoneId.systemDefault();

    /** Defines the calendar's date range starting point. */
    protected ZonedDateTime startDate = null;

    /** Defines the calendar's date range ending point. */
    protected ZonedDateTime endDate = null;

    /** Item provider. */
    private CalendarItemProvider<ITEM> calendarItemProvider;

    /**
     * Internal buffer for the items that are retrieved from the item provider.
     */
    protected List<? extends CalendarItem> items;


    /** Date format that will be used in the UIDL for dates. */
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(DateConstants.DATE_FORMAT_PATTERN);

    /** Time format that will be used in the UIDL for time. */
    private final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern(DateConstants.TIME_FORMAT_PATTERN);

    /** Time format that will be used in the UIDL for time. */
    protected final DateTimeFormatter ACTION_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern(DateConstants.ACTION_DATE_TIME_FORMAT_PATTERN);

    /**
     * Week view's scroll position. Client sends updates to this value so that
     * scroll position wont reset all the time.
     */
    private int scrollTop = 0;

    /** Caption format provuder for the weekly view */
    private WeeklyCaptionProvider weeklyCaptionFormatProvider = date -> DateTimeFormatter.ofPattern("yyyy/MM/dd", getLocale()).format(date);

    /** Map from event ids to event handlers */
    private final Map<String, EventListener> handlers;

    /**
     * Drop Handler for Vaadin DD. By default null.
     */
    private DropHandler dropHandler;

    /**
     * First day to show for a week
     */
    private int firstDay = 1;

    /**
     * Last day to show for a week
     */
    private int lastDay = 7;

    /**
     * First hour to show for a day
     */
    private int firstHour = 0;

    /**
     * Last hour to show for a day
     */
    private int lastHour = 23;

    /**
     * List of action handlers.
     */
    private LinkedList<Handler> actionHandlers = null;

    /**
     * Action mapper.
     */
    private KeyMapper<Action> actionMapper = null;

    /**
     *
     */
    private CalendarServerRpcImpl rpc = new CalendarServerRpcImpl();

    /**
     * The cached minimum minute shown when using
     * {@link #autoScaleVisibleHoursOfDay()}.
     */
    private Integer minTimeInMinutes;

    /**
     * The cached maximum minute shown when using
     * {@link #autoScaleVisibleHoursOfDay()}.
     */
    private Integer maxTimeInMinutes;

//    private Integer customFirstDayOfWeek;

    /**
     * A map with blocked timeslots.<br>
     *     Contains a set with timestamp of starttimes.
     */
    private final Map<Date, Set<Long>> blockedTimes = new HashMap<>();

    /**
     * Initial date for all blocked times
     */
    private final Date allOverDate = new Date(0);

    /**
     * Returns the logger for the calendar
     */
    protected Logger getLogger() {
        return Logger.getLogger(Calendar.class.getName());
    }

    /**
     * Construct a Vaadin Calendar with a BasicItemProvider and no caption.
     * Default date range is one week.
     */
    public Calendar() {
        this(null, new BasicItemProvider());
    }

    /**
     * Construct a Vaadin Calendar with a BasicItemProvider and the provided
     * caption. Default date range is one week.
     *
     * @param caption
     */
    public Calendar(String caption) {
        this(caption, new BasicItemProvider());
    }

    /**
     * <p>
     * Construct a Vaadin Calendar with event provider. Item provider is
     * obligatory, because calendar component will query active items through
     * it.
     * </p>
     *
     * <p>
     * By default, Vaadin Calendar will show dates from the start of the current
     * week to the end of the current week. Use {@link #setStartDate(ZonedDateTime)} and
     * {@link #setEndDate(ZonedDateTime)} to change this.
     * </p>
     *
     * @param dataProvider
     *            Item provider, cannot be null.
     */
    public Calendar(CalendarItemProvider<ITEM> dataProvider) {
        this(null, dataProvider);
    }

    /**
     * <p>
     * Construct a Vaadin Calendar with item provider and a caption. Item
     * provider is obligatory, because calendar component will query active
     * items through it.
     * </p>
     *
     * <p>
     * By default, Vaadin Calendar will show dates from the start of the current
     * week to the end of the current week. Use {@link #setStartDate(ZonedDateTime)} and
     * {@link #setEndDate(ZonedDateTime)} to change this.
     * </p>
     *
     * @param dataProvider
     *            Item provider, cannot be null.
     */
    // this is the constructor every other constructor calls
    public Calendar(String caption, CalendarItemProvider<ITEM> dataProvider) {
        registerRpc(rpc);
        setCaption(caption);
        handlers = new HashMap<>();
        setDefaultHandlers();
        setDataProvider(dataProvider);
        getState().firstDayOfWeek = firstDay;
        getState().lastVisibleDayOfWeek = lastDay;
        getState().firstHourOfDay = firstHour;
        getState().lastHourOfDay = lastHour;
        setTimeFormat(null);
    }

    @Override
    public CalendarState getState() {
        return (CalendarState) super.getState();
    }

    @Override
    protected CalendarState getState(boolean markAsDirty) {
        return (CalendarState) super.getState(markAsDirty);
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);

        getState().format24H = TimeFormat.Format24H == getTimeFormat();
        setupDaysAndActions();
        setupCalendarItems();
        rpc.scroll(scrollTop);
    }

    /**
     * Set the ContentMode
     *
     * @param contentMode The new content mode
     */
    public void setContentMode(ContentMode contentMode) {
        getState().descriptionContentMode = Objects.isNull(contentMode) ? ContentMode.PREFORMATTED : contentMode;
    }

    /**
     * @return The content mode
     */
    public ContentMode getContentMode() {
        return getState(false).descriptionContentMode;
    }

    /**
     * Set all the wanted default handlers here. This is always called after
     * constructing this object. All other items have default handlers except
     * range and event click.
     */
    protected void setDefaultHandlers() {
        setHandler(new BasicBackwardHandler());
        setHandler(new BasicForwardHandler());
        setHandler(new BasicWeekClickHandler());
        setHandler(new BasicDateClickHandler());
        setHandler(new BasicItemMoveHandler());
        setHandler(new BasicItemResizeHandler());
    }

    /**
     * Gets the calendar's start date.
     *
     * @return First visible date.
     */
    public ZonedDateTime getStartDate() {
        if (startDate == null) {
            // A new datetime with components zone id
            startDate = ZonedDateTime.now(getZoneId())
                    // and first day in week
                    .with(ChronoField.DAY_OF_WEEK, 1)
                    // with a time of 00:00:00:000
                    .with(LocalTime.MIN);
        }
        return startDate;
    }

    /**
     * Sets start date for the calendar. This and {@link #setEndDate(ZonedDateTime)}
     * control the range of dates visible on the component. The default range is
     * one week.
     *
     * @param date
     *            First visible date to show.
     */
    public void setStartDate(ZonedDateTime date) {
        // reset all time information
        date = date.with(LocalTime.MIN);

        // and update, if the given date is not the same as current date
        if (!date.equals(startDate)) {
            startDate = date;
            markAsDirty();
        }
    }

    /**
     * Gets the calendar's end date.
     *
     * @return Last visible date.
     */
    public ZonedDateTime getEndDate() {
        if (endDate == null) {
            endDate = ZonedDateTime.now(getZoneId())
                    // and last day of week
                    .with(ChronoField.DAY_OF_WEEK, 7)
                    // with a time of 23:59:59.999999999
                    .with(LocalTime.MAX);
        }
        return endDate;
    }

    /**
     * Sets end date for the calendar. Starting from startDate, only six weeks
     * will be shown if duration to endDate is longer than six weeks.
     *
     * This and {@link #setStartDate(ZonedDateTime)} control the range of dates visible
     * on the component. The default range is one week.
     *
     * @param date
     *            Last visible date to show.
     */
    public void setEndDate(ZonedDateTime date) {
        // reset all time information
        date = date.with(LocalTime.MIN);

        // check start after end
        if (startDate != null && startDate.isAfter(date)) {
            startDate = date;
            markAsDirty();
        } else

            // and end is not the same as current end
            if (!date.equals(endDate)) {
            endDate = date;
            markAsDirty();
        }
    }

    /**
     * Sets the locale to be used in the Calendar component.
     *
     * @see AbstractComponent#setLocale(Locale)
     */
    @Override
    public void setLocale(Locale newLocale) {
        super.setLocale(newLocale);
        markAsDirty();
    }

    private void setupCalendarItems() {

        long durationInDays = Duration.between(startDate, endDate).toDays();
        durationInDays++;

        if (durationInDays > 60) {
            throw new RuntimeException(
                    "Daterange is too big (max 60) = " + durationInDays);
        }

        ZonedDateTime firstDateToShow = expandStartDate(startDate, durationInDays > 7);
        ZonedDateTime lastDateToShow = expandEndDate(endDate, durationInDays > 7);

        items = getDataProvider().getItems(firstDateToShow, lastDateToShow);
        cacheMinMaxTimeOfDay(items);

        List<CalendarState.Item> calendarStateItems = new ArrayList<>();
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                CalendarItem calItem = items.get(i);
                CalendarState.Item item = new CalendarState.Item();
                item.index = i;
                item.caption = calItem.getCaption() == null ? "" : calItem.getCaption();

// TODO STRING FORMATTER yyyy-MM-dd
                item.dateFrom = DATE_FORMAT.format(calItem.getStart());
                item.dateTo = DATE_FORMAT.format(calItem.getEnd());
// TODO STRING FORMATTER HH:mm:ss
                item.timeFrom = TIME_FORMAT.format(calItem.getStart());
                item.timeTo = TIME_FORMAT.format(calItem.getEnd());

                item.description = calItem.getDescription() == null ? "" : calItem.getDescription();
                item.styleName = calItem.getStyleName() == null ? "" : calItem.getStyleName();
                item.allDay = calItem.isAllDay();
                item.moveable = calItem.isMoveable();
                item.resizeable = calItem.isResizeable();
                item.clickable = calItem.isClickable();
                calendarStateItems.add(item);
            }
        }
        getState().items = calendarStateItems;
    }

    /**
     * Stores the minimum and maximum time-of-day in minutes for the items.
     *
     * @param items
     *            A list of calendar items. Can be <code>null</code>.
     */
    private void cacheMinMaxTimeOfDay(List<? extends CalendarItem> items) {
        minTimeInMinutes = null;
        maxTimeInMinutes = null;
        if (items != null) {
            for (CalendarItem item : items) {
                int minuteOfDayStart = getMinuteOfDay(item.getStart());
                int minuteOfDayEnd = getMinuteOfDay(item.getEnd());
                if (minTimeInMinutes == null) {
                    minTimeInMinutes = minuteOfDayStart;
                    maxTimeInMinutes = minuteOfDayEnd;
                } else {
                    if (minuteOfDayStart < minTimeInMinutes) {
                        minTimeInMinutes = minuteOfDayStart;
                    }
                    if (minuteOfDayEnd > maxTimeInMinutes) {
                        maxTimeInMinutes = minuteOfDayEnd;
                    }
                }
            }
        }
    }

    private static int getMinuteOfDay(ZonedDateTime date) {
        return date.getHour() * 60 + date.getMinute();
    }

    /**
     * Sets the displayed start and end time to fit all current items that were
     * retrieved from the last call to getItems().
     * <p>
     * If no items exist, nothing happens.
     * <p>
     * <b>NOTE: triggering this method only does this once for the current
     * items - items that are not in the current visible range, are
     * ignored!</b>
     *
     * @see #setFirstVisibleHourOfDay(int)
     * @see #setLastVisibleHourOfDay(int)
     */
    public void autoScaleVisibleHoursOfDay() {
        if (minTimeInMinutes != null) {
            setFirstVisibleHourOfDay(minTimeInMinutes / 60);
            // Do not show the final hour if last minute ends on it
            setLastVisibleHourOfDay((maxTimeInMinutes - 1) / 60);
        }
    }

    /**
     * Resets the {@link #setFirstVisibleHourOfDay(int)} and
     * {@link #setLastVisibleHourOfDay(int)} to the default values, 0 and 23
     * respectively.
     *
     * @see #autoScaleVisibleHoursOfDay()
     * @see #setFirstVisibleHourOfDay(int)
     * @see #setLastVisibleHourOfDay(int)
     */
    public void resetVisibleHoursOfDay() {
        setFirstVisibleHourOfDay(0);
        setLastVisibleHourOfDay(23);
    }

    private void setupDaysAndActions() {

        CalendarState state = getState();

        state.firstDayOfWeek = java.util.Calendar.getInstance(getLocale()).getFirstDayOfWeek();

        // If only one is null, throw exception
        // If both are null, set defaults
        if (startDate == null ^ endDate == null) {
            String message = "Schedule cannot be painted without a proper date range.\n";
            if (startDate == null) {
                throw new IllegalStateException(message
                        + "You must set a start date using setStartDate(Date).");

            } else {
                throw new IllegalStateException(message
                        + "You must set an end date using setEndDate(Date).");
            }

        } else if (startDate == null) {
            // set defaults
            startDate = getStartDate();
            endDate = getEndDate();
        }

        long durationInDays = Duration.between(startDate, endDate).toDays();
        durationInDays++;
        if (durationInDays > 60) {
            throw new RuntimeException( "Daterange is too big (max 60) = " + durationInDays);
        }

        boolean monthView = durationInDays > 7;

        state.dayNames = getDayNamesShort();
        state.monthNames = getMonthNamesShort();

        // Show "now"-marker in browser within given timezone.
        final ZonedDateTime now = ZonedDateTime.now(getZoneId());
        state.now = new CalDate(now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                new CalTime(now.getHour(), now.getMinute(), now.getSecond()));

        // Send all dates to client from server. This
        // approach was taken because gwt doesn't
        // support date localization properly.
        ZonedDateTime firstDateToShow = expandStartDate(startDate, monthView);
        ZonedDateTime lastDateToShow = expandEndDate(endDate, monthView);
        ZonedDateTime dateToShow = firstDateToShow;

        Map<CalendarDateRange, Set<Action>> actionMap = new HashMap<>();
        List<CalendarState.Day> days = new ArrayList<>();

        while (dateToShow.compareTo(lastDateToShow) < 1) {

            final CalendarState.Day day = new CalendarState.Day();

            day.date = new CalDate(dateToShow.getYear(), dateToShow.getMonthValue(), dateToShow.getDayOfMonth());

            day.localizedDateFormat = weeklyCaptionFormatProvider.captionFrom(dateToShow);

            day.dayOfWeek = dateToShow.getDayOfWeek().getValue();
            day.week = (int) WeekFields.of(getLocale()).weekOfYear().getFrom(dateToShow);
            day.yearOfWeek = dateToShow.getYear();

            day.blockedSlots = new HashSet<>();

            if (blockedTimes.containsKey(allOverDate)) {
                day.blockedSlots.addAll(blockedTimes.get(allOverDate));
            }

            if (blockedTimes.containsKey(dateToShow)) {
                day.blockedSlots.addAll(blockedTimes.get(dateToShow));
            }

            days.add(day);

            // Get actions for a specific date
            if (actionHandlers != null) {

                for (Action.Handler actionHandler : actionHandlers) {

                    // Get day start and end times
                    ZonedDateTime start = dateToShow.with(LocalTime.MIN);
                    ZonedDateTime end = dateToShow.with(LocalTime.MAX);

                    /*
                     * If in day or week view add actions for each half-an-hour.
                     * If in month view add actions for each day
                     */

                    if (monthView) {
                        setActionsForDay(actionMap, start, end, actionHandler);
                    } else {
                        setActionsForEachHalfHour(actionMap, start, end, actionHandler);
                    }

                }
            }

            dateToShow = dateToShow.plus(1, ChronoUnit.DAYS);
        }

        state.days = days;
        state.actions = createActionsList(actionMap);
    }

    private void setActionsForEachHalfHour(Map<CalendarDateRange, Set<Action>> actionMap,
                                           ZonedDateTime start, ZonedDateTime end, Action.Handler actionHandler) {

        ZonedDateTime actionTime = start;
        while (actionTime.isBefore(end)) {

            ZonedDateTime endTime = actionTime.plus(30, ChronoUnit.MINUTES);

            CalendarDateRange range = new CalendarDateRange(actionTime, endTime);

            Action[] actions = actionHandler.getActions(range, this);
            if (actions != null) {
                Set<Action> actionSet = new LinkedHashSet<>(Arrays.asList(actions));
                actionMap.put(range, actionSet);
            }

            actionTime = endTime;
        }
    }

    private void setActionsForDay(Map<CalendarDateRange, Set<Action>> actionMap,
                                  ZonedDateTime start, ZonedDateTime end, Action.Handler actionHandler) {

        CalendarDateRange range = new CalendarDateRange(start, end);
        Action[] actions = actionHandler.getActions(range, this);
        if (actions != null) {
            Set<Action> actionSet = new LinkedHashSet<>(Arrays.asList(actions));
            actionMap.put(range, actionSet);
        }
    }

    private List<CalendarState.Action> createActionsList(Map<CalendarDateRange, Set<Action>> actionMap) {

        if (actionMap.isEmpty()) {
            return null;
        }

        List<CalendarState.Action> calendarActions = new ArrayList<>();

        for (Entry<CalendarDateRange, Set<Action>> entry : actionMap.entrySet()) {

            CalendarDateRange range = entry.getKey();

            for (Action action : entry.getValue()) {
                String key = actionMapper.key(action);
                CalendarState.Action calendarAction = new CalendarState.Action();
                calendarAction.actionKey = key;
                calendarAction.caption = action.getCaption();
                setResource(key, action.getIcon());
                calendarAction.iconKey = key;
                calendarAction.startDate = ACTION_DATE_TIME_FORMAT.format(range.getStart());
                calendarAction.endDate = ACTION_DATE_TIME_FORMAT.format(range.getEnd());
                calendarActions.add(calendarAction);
            }
        }

        return calendarActions;
    }

    /**
     * Gets currently active time format. Value is either TimeFormat.Format12H
     * or TimeFormat.Format24H.
     *
     * @return TimeFormat Format for the time.
     */
    public TimeFormat getTimeFormat() {
        if (currentTimeFormat == null) {
            SimpleDateFormat f;
            if (getLocale() == null) {
                f = (SimpleDateFormat) SimpleDateFormat
                        .getTimeInstance(SimpleDateFormat.SHORT);
            } else {
                f = (SimpleDateFormat) SimpleDateFormat
                        .getTimeInstance(SimpleDateFormat.SHORT, getLocale());
            }
            String p = f.toPattern();
            if (p.contains("H")) {
                return TimeFormat.Format24H;
            }
            return TimeFormat.Format12H;
        }
        return currentTimeFormat;
    }

    /**
     * Example: <code>setTimeFormat(TimeFormat.Format12H);</code></br>
     * Set to null, if you want the format being defined by the locale.
     *
     * @param format
     *            Set 12h or 24h format. Default is defined by the locale.
     */
    public void setTimeFormat(TimeFormat format) {
        currentTimeFormat = format;
        markAsDirty();
    }

    /**
     * Returns a time zone that is currently used by this component.
     *
     * @return Component's Time zone
     */
    public ZoneId getZoneId() {
        return zoneId;
    }

    /**
     * Set time zone that this component will use. Null value sets the default
     * time zone.
     *
     * @param zone
     *            Time zone to use
     */
    public void setZoneId(ZoneId zone) {

        if (!zoneId.equals(zone)) {
            zoneId = zone;

            setStartDate(ZonedDateTime.ofInstant(getStartDate().toInstant(), zone));
            setEndDate(ZonedDateTime.ofInstant(getEndDate().toInstant(), zone));

            markAsDirty();
        }
    }

    /**
     * <p>
     * This method restricts the weekdays that are shown. This affects both the
     * monthly and the weekly view. The general contract is that <b>firstDay <
     * lastDay</b>.
     * </p>
     *
     * <p>
     * Note that this only affects the rendering process. Items are still
     * requested by the dates set by {@link #setStartDate(ZonedDateTime)} and
     * {@link #setEndDate(ZonedDateTime)}.
     * </p>
     *
     * @param firstDay
     *            the first day of the week to show, between 1 and 7
     * @param lastDay
     *            the first day of the week to show, between 1 and 7
     */
    public void setVisibleDayRange(int firstDay, int lastDay) {
        assert (firstDay >= 1 && firstDay < lastDay && lastDay <= 7);

        this.firstDay = firstDay;
        this.lastDay = lastDay;

        getState(false).firstVisibleDayOfWeek = firstDay;
        getState().lastVisibleDayOfWeek = lastDay;

    }

    /**
     * Get the first visible day of the week. Returns the weekdays as integers
     * represented by {@link java.util.Calendar#DAY_OF_WEEK}
     *
     * @return An integer representing the week day according to
     *         {@link java.util.Calendar#DAY_OF_WEEK}
     */
    public int getFirstVisibleDayOfWeek() {
        return firstDay;
    }

    /**
     * Get the last visible day of the week. Returns the weekdays as integers
     * represented by {@link java.util.Calendar#DAY_OF_WEEK}
     *
     * @return An integer representing the week day according to
     *         {@link java.util.Calendar#DAY_OF_WEEK}
     */
    public int getLastVisibleDayOfWeek() {
        return lastDay;
    }

    /**
     * <p>
     * This method restricts the hours that are shown per day. This affects the
     * weekly view. The general contract is that <b>firstHour < lastHour</b>.
     * </p>
     *
     * <p>
     * Note that this only affects the rendering process. Items are still
     * requested by the dates set by {@link #setStartDate(ZonedDateTime)} and
     * {@link #setEndDate(ZonedDateTime)}.
     * </p>
     * You can use {@link #autoScaleVisibleHoursOfDay()} for automatic scaling
     * of the visible hours based on current items.
     *
     * @param firstHour
     *            the first hour of the day to show, between 0 and 23
     * @see #autoScaleVisibleHoursOfDay()
     */
    public void setFirstVisibleHourOfDay(int firstHour) {
        if (this.firstHour != firstHour && firstHour >= 0 && firstHour <= 23  && firstHour <= getLastVisibleHourOfDay()) {
            this.firstHour = firstHour;
            getState().firstHourOfDay = firstHour;
        }
    }

    /**
     * Returns the first visible hour in the week view. Returns the hour using a
     * 24h time format
     *
     */
    public int getFirstVisibleHourOfDay() {
        return firstHour;
    }

    /**
     * This method restricts the hours that are shown per day. This affects the
     * weekly view. The general contract is that <b>firstHour < lastHour</b>.
     * <p>
     * Note that this only affects the rendering process. Items are still
     * requested by the dates set by {@link #setStartDate(ZonedDateTime)} and
     * {@link #setEndDate(ZonedDateTime)}.
     * <p>
     * You can use {@link #autoScaleVisibleHoursOfDay()} for automatic scaling
     * of the visible hours based on current items.
     *
     * @param lastHour
     *            the first hour of the day to show, between 0 and 23
     * @see #autoScaleVisibleHoursOfDay()
     */
    public void setLastVisibleHourOfDay(int lastHour) {
        if (this.lastHour != lastHour && lastHour >= 0 && lastHour <= 23 && lastHour >= getFirstVisibleHourOfDay()) {
            this.lastHour = lastHour;
            getState().lastHourOfDay = lastHour;
        }
    }

    /**
     * Returns the last visible hour in the week view. Returns the hour using a
     * 24h time format
     *
     */
    public int getLastVisibleHourOfDay() {
        return lastHour;
    }

    /**
     * Gets the custom date caption provider for the weekly view.
     *
     * @return The custom date caption provider for the weekly view.
     */
    public WeeklyCaptionProvider getWeeklyCaptionProvider() {
        return weeklyCaptionFormatProvider;
    }

    /**
     * Sets custom date caption provider for the weekly view. This is the caption of the
     * date. Format could be like "mmm MM/dd".
     *
     * @param captionProvider
     *            The caption provider.
     */
    public void setWeeklyCaptionProvider(WeeklyCaptionProvider captionProvider) {
        if (captionProvider != null) {
            weeklyCaptionFormatProvider = captionProvider;
            markAsDirty();
        }
    }

    /**
     * Sets sort order for items. By default sort order is
     * {@link CalendarState.ItemSortOrder#DURATION_DESC}.
     *
     * @param order
     *            sort strategy for items
     */
    public void setItemSortOrder(CalendarState.ItemSortOrder order) {
        if (order == null) {
            getState().itemSortOrder = CalendarState.ItemSortOrder.DURATION_DESC;
        } else {
            getState().itemSortOrder = CalendarState.ItemSortOrder.values()[order.ordinal()];
        }
    }

    /**
     * Returns sort order for items.
     *
     * @return currently active sort strategy
     */
    public CalendarState.ItemSortOrder getItemSortOrder() {
        CalendarState.ItemSortOrder order = getState(false).itemSortOrder;
        if (order == null) {
            return CalendarState.ItemSortOrder.DURATION_DESC;
        } else {
            return order;
        }
    }

    /**
     * Is the user allowed to trigger items which alters the items
     *
     * @return true if the client is allowed to send changes to server
     */
    protected boolean isClientChangeAllowed() {
        return isEnabled();
    }

    /**
     * Fires an event when the user selecing moving forward/backward in the
     * calendar.
     *
     * @param forward
     *            True if the calendar moved forward else backward is assumed.
     */
    protected void fireNavigationEvent(boolean forward) {
        if (forward) {
            fireEvent(new CalendarComponentEvents.ForwardEvent(this));
        } else {
            fireEvent(new CalendarComponentEvents.BackwardEvent(this));
        }
    }

    /**
     * Fires an item move event to all server side move listerners
     *
     * @param index
     *            The index of the item in the items list
     * @param newFromDatetime
     *            The changed from date time
     */
    protected void fireItemMove(int index, ZonedDateTime newFromDatetime) {

        CalendarComponentEvents.ItemMoveEvent event =
                new CalendarComponentEvents.ItemMoveEvent(this, items.get(index), newFromDatetime);

        if (calendarItemProvider instanceof CalendarComponentEvents.ItemMoveHandler) {

            // Notify event provider if it is an event move handler
            ((CalendarComponentEvents.ItemMoveHandler) calendarItemProvider).itemMove(event);
        }

        // Notify event move handler attached by using the
        // setHandler(ItemMoveHandler) method
        fireEvent(event);
    }

    /**
     * Fires event when a week was clicked in the calendar.
     *
     * @param week
     *            The week that was clicked
     * @param year
     *            The year of the week
     */
    protected void fireWeekClick(int week, int year) {
        fireEvent(new CalendarComponentEvents.WeekClick(this, week, year));
    }

    /**
     * Fires event when a date was clicked in the calendar. Uses an existing
     * event from the event cache.
     *
     * @param index
     *            The index of the event in the event cache.
     */
    protected void fireItemClick(Integer index) {
        fireEvent(new CalendarComponentEvents.ItemClickEvent(this, items.get(index)));
    }

    /**
     * Fires event when a date was clicked in the calendar. Creates a new event
     * for the date and passes it to the listener.
     *
     * @param date
     *            The date and time that was clicked
     */
    protected void fireDateClick(ZonedDateTime date) {
        fireEvent(new CalendarComponentEvents.DateClickEvent(this, date));
    }

    /**
     * Fires an event range selected event. The event is fired when a user
     * highlights an area in the calendar. The highlighted areas start and end
     * dates are returned as arguments.
     *
     * @param from
     *            The start date and time of the highlighted area
     * @param to
     *            The end date and time of the highlighted area
     */
    protected void fireRangeSelect(ZonedDateTime from, ZonedDateTime to) {
        fireEvent(new CalendarComponentEvents.RangeSelectEvent(this, from, to));
    }

    /**
     * Fires an item resize event. The event is fired when a user resizes the
     * item in the calendar causing the time range of the item to increase or
     * decrease. The new start and end times are returned as arguments to this
     * method.
     *
     * @param index
     *            The index of the item in the item cache
     * @param startTime
     *            The new start date and time of the item
     * @param endTime
     *            The new end date and time of the item
     */
    protected void fireItemResize(int index, ZonedDateTime startTime, ZonedDateTime endTime) {

        CalendarComponentEvents.ItemResizeEvent event =
                new CalendarComponentEvents.ItemResizeEvent(this, items.get(index), startTime, endTime);

        if (calendarItemProvider instanceof CalendarComponentEvents.EventResizeHandler) {
            // Notify event provider if it is an event resize handler
            ((CalendarComponentEvents.EventResizeHandler) calendarItemProvider).itemResize(event);
        }

        // Notify event resize handler attached by using the
        // setHandler(ItemMoveHandler) method
        fireEvent(event);
    }

    /**
     * Localized display names for week days starting from sunday. Returned
     * array's length is always 7.
     *
     * @return Array of localized weekday names.
     */
    protected String[] getDayNamesShort() {
        DateFormatSymbols s = new DateFormatSymbols(getLocale());
        return Arrays.copyOfRange(s.getWeekdays(), 1, 8);
    }

    /**
     * Localized display names for months starting from January. Returned
     * array's length is always 12.
     *
     * @return Array of localized month names.
     */
    protected String[] getMonthNamesShort() {
        DateFormatSymbols s = new DateFormatSymbols(getLocale());
        return Arrays.copyOf(s.getShortMonths(), 12);
    }

    /**
     * Gets a date that is first day in the week that target given date belongs
     * to.
     *
     * @param date
     *            Target date
     * @return Date that is first date in same week that given date is.
     */

    public ZonedDateTime getfirstDayOfWeek(ZonedDateTime date) {
        return date.with(ChronoField.DAY_OF_WEEK, 1);
    }


    /**
     * Gets a date that is last day in the week that target given date belongs
     * to.
     *
     * @param date
     *            Target date
     * @return Date that is last date in same week that given date is.
     */
    public ZonedDateTime getLastDayOfWeek(ZonedDateTime date) {
        return date.with(ChronoField.DAY_OF_WEEK, 7);
    }

    /**
     * Finds the first day of the week and returns a day representing the start
     * of that day
     *
     * @param start
     *            The actual date
     * @param expandToFullWeek
     *            Should the returned date be moved to the start of the week
     * @return If expandToFullWeek is set then it returns the first day of the
     *         week, else it returns a clone of the actual date with the time
     *         set to the start of the day
     */
    protected ZonedDateTime expandStartDate(ZonedDateTime start, boolean expandToFullWeek) {

        if (expandToFullWeek) {
            start = getfirstDayOfWeek(start);

        } else {
            start = ZonedDateTime.from(start);
        }

        // Always expand to the start of the first day to the end of the last day
        return start.with(LocalTime.MIN);
    }

    /**
     * Finds the last day of the week and returns a day representing the end of
     * that day
     *
     * @param end
     *            The actual date
     * @param expandToFullWeek
     *            Should the returned date be moved to the end of the week
     * @return If expandToFullWeek is set then it returns the last day of the
     *         week, else it returns a clone of the actual date with the time
     *         set to the end of the day
     */
    protected ZonedDateTime expandEndDate(ZonedDateTime end, boolean expandToFullWeek) {

        if (expandToFullWeek) {
            end = getLastDayOfWeek(end);
        } else {
            end = ZonedDateTime.from(end);
        }

        // Always expand to the start of the first day to the end of the last day
        return end.with(LocalTime.MAX);
    }

    /**
     * Set the {@link CalendarItemProvider} to be used with this calendar. The
     * DataProvider is used to query for items to show, and must be non-null.
     * By default a {@link BasicItemProvider} is used.
     *
     * @param calendarItemProvider
     *            the calendarItemProvider to set. Cannot be null.
     */
    public void setDataProvider(CalendarItemProvider<ITEM> calendarItemProvider) {

        if (calendarItemProvider == null) {
            throw new IllegalArgumentException(
                    "Calendar event provider cannot be null");
        }

        // remove old listener
        if (getDataProvider() instanceof CalendarItemProvider.ItemSetChangedNotifier) {
            ((ItemSetChangedNotifier) getDataProvider()).removeItemSetChangedListener(this);
        }

        this.calendarItemProvider = calendarItemProvider;

        // add new listener
        if (calendarItemProvider instanceof CalendarItemProvider.ItemSetChangedNotifier) {
            ((ItemSetChangedNotifier) calendarItemProvider).addItemSetChangedListener(this);
        }
    }

    /**
     * @return the {@link CalendarItemProvider} currently used
     */
    public CalendarItemProvider<ITEM> getDataProvider() {
        return calendarItemProvider;
    }

    @Override
    public void itemSetChanged(ItemSetChangedEvent changeEvent) {
        // sanity check
        if (calendarItemProvider == changeEvent.getProvider()) {
            markAsDirty();
        }
    }

    /**
     * Set the handler for the given type information. Mirrors
     * {@link #addListener(String, Class, Object, Method) addListener} from
     * AbstractComponent
     *
     * @param eventId
     *            A unique id for the event. Usually one of
     *            {@link CalendarEventId}
     * @param eventType
     *            The class of the event, most likely a subclass of
     *            {@link CalendarComponentEvent}
     * @param listener
     *            A listener that listens to the given event
     * @param listenerMethod
     *            The method on the lister to call when the event is triggered
     */
    protected void setHandler(String eventId, Class<?> eventType, EventListener listener, Method listenerMethod) {
        if (handlers.get(eventId) != null) {
            removeListener(eventId, eventType, handlers.get(eventId));
            handlers.remove(eventId);
        }

        if (listener != null) {
            addListener(eventId, eventType, listener, listenerMethod);
            handlers.put(eventId, listener);
        }
    }

    @Override
    public void setHandler(CalendarComponentEvents.ForwardHandler listener) {
        setHandler(CalendarComponentEvents.ForwardEvent.EVENT_ID, CalendarComponentEvents.ForwardEvent.class, listener,
                CalendarComponentEvents.ForwardHandler.forwardMethod);
    }

    @Override
    public void setHandler(CalendarComponentEvents.BackwardHandler listener) {
        setHandler(CalendarComponentEvents.BackwardEvent.EVENT_ID, CalendarComponentEvents.BackwardEvent.class, listener,
                CalendarComponentEvents.BackwardHandler.backwardMethod);
    }

    @Override
    public void setHandler(CalendarComponentEvents.DateClickHandler listener) {
        setHandler(CalendarComponentEvents.DateClickEvent.EVENT_ID, CalendarComponentEvents.DateClickEvent.class, listener,
                CalendarComponentEvents.DateClickHandler.dateClickMethod);
    }

    @Override
    public void setHandler(CalendarComponentEvents.ItemClickHandler listener) {
        setHandler(CalendarComponentEvents.ItemClickEvent.EVENT_ID, CalendarComponentEvents.ItemClickEvent.class, listener,
                CalendarComponentEvents.ItemClickHandler.itemClickMethod);
    }

    @Override
    public void setHandler(CalendarComponentEvents.WeekClickHandler listener) {
        setHandler(CalendarComponentEvents.WeekClick.EVENT_ID, CalendarComponentEvents.WeekClick.class, listener,
                CalendarComponentEvents.WeekClickHandler.weekClickMethod);
    }

    @Override
    public void setHandler(CalendarComponentEvents.EventResizeHandler listener) {
        setHandler(CalendarComponentEvents.ItemResizeEvent.EVENT_ID, CalendarComponentEvents.ItemResizeEvent.class, listener,
                CalendarComponentEvents.EventResizeHandler.itemResizeMethod);
    }

    @Override
    public void setHandler(CalendarComponentEvents.RangeSelectHandler listener) {
        setHandler(CalendarComponentEvents.RangeSelectEvent.EVENT_ID, CalendarComponentEvents.RangeSelectEvent.class, listener,
                CalendarComponentEvents.RangeSelectHandler.rangeSelectMethod);
    }

    @Override
    public void setHandler(CalendarComponentEvents.ItemMoveHandler listener) {
        setHandler(CalendarComponentEvents.ItemMoveEvent.EVENT_ID, CalendarComponentEvents.ItemMoveEvent.class, listener,
                CalendarComponentEvents.ItemMoveHandler.itemMoveMethod);
    }

    @Override
    public EventListener getHandler(String eventId) {
        return handlers.get(eventId);
    }

    /**
     * Get the currently active drop handler
     */
    @Override
    public DropHandler getDropHandler() {
        return dropHandler;
    }

    /**
     * Set the drop handler for the calendar See {@link DropHandler} for
     * implementation details.
     *
     * @param dropHandler
     *            The drop handler to set
     */
    public void setDropHandler(DropHandler dropHandler) {
        this.dropHandler = dropHandler;
    }

    @Override
    public TargetDetails translateDropTargetDetails(Map<String, Object> clientVariables) {
        Map<String, Object> serverVariables = new HashMap<>();

        if (clientVariables.containsKey("dropSlotIndex")) {
            int slotIndex = (Integer) clientVariables.get("dropSlotIndex");
            int dayIndex = (Integer) clientVariables.get("dropDayIndex");

            ZonedDateTime dropTime = startDate
                    .with(LocalTime.MIN)
                    .plus(dayIndex, ChronoUnit.DAYS)
                    .plus(slotIndex * 30, ChronoUnit.MINUTES);

            serverVariables.put("dropTime", dropTime.toEpochSecond() * 1000);

        } else {
            int dayIndex = (Integer) clientVariables.get("dropDayIndex");

            ZonedDateTime dropTime = expandStartDate(startDate, true)
                    .plus(dayIndex, ChronoUnit.DAYS);

            serverVariables.put("dropDay", dropTime.toEpochSecond() * 1000);
        }
        serverVariables.put("mouseEvent", clientVariables.get("mouseEvent"));

        CalendarTargetDetails td = new CalendarTargetDetails(serverVariables, this);
        td.setHasDropTime(clientVariables.containsKey("dropSlotIndex"));

        return td;
    }

    @Override
    public List<ITEM> getItems(ZonedDateTime startDate, ZonedDateTime endDate) {
        List<ITEM> events = getDataProvider().getItems(startDate, endDate);
        cacheMinMaxTimeOfDay(events);
        return events;
    }

    /**
     * Adds an action handler to the calendar that handles event produced by the
     * context menu.
     *
     * <p>
     * The {@link Handler#getActions(Object, Object)} parameters depend on what
     * view the Calendar is in:
     * <ul>
     * <li>If the Calendar is in <i>Day or Week View</i> then the target
     * parameter will be a {@link CalendarDateRange} with a range of
     * half-an-hour. The {@link Handler#getActions(Object, Object)} method will
     * be called once per half-hour slot.</li>
     * <li>If the Calendar is in <i>Month View</i> then the target parameter
     * will be a {@link CalendarDateRange} with a range of one day. The
     * {@link Handler#getActions(Object, Object)} will be called once for each
     * day.
     * </ul>
     * The Dates passed into the {@link CalendarDateRange} are in the same
     * timezone as the calendar is.
     * </p>
     *
     * <p>
     * The {@link Handler#handleAction(Action, Object, Object)} parameters
     * depend on what the context menu is called upon:
     * <ul>
     * <li>If the context menu is called upon an item then the target parameter
     * is the item, i.e. instanceof {@link CalendarItem}</li>
     * <li>If the context menu is called upon an empty slot then the target is a
     * {@link Date} representing that slot
     * </ul>
     * </p>
     */
    @Override
    public void addActionHandler(Handler actionHandler) {
        if (actionHandler != null) {
            if (actionHandlers == null) {
                actionHandlers = new LinkedList<>();
                actionMapper = new KeyMapper<>();
            }

            if (!actionHandlers.contains(actionHandler)) {
                actionHandlers.add(actionHandler);
                markAsDirty();
            }
        }
    }

    /**
     * Is the calendar in a mode where all days of the month is shown
     *
     * @return Returns true if calendar is in monthly mode and false if it is in
     *         weekly mode
     */
    public boolean isMonthlyMode() {
        CalendarState state = getState(false);
        return state.days == null || state.days.size() > 7;
    }

    /**
     * Is the calendar in a mode where one day of the month is shown
     *
     * @return Returns true if calendar is in day mode and false if it is in
     *         weekly mode
     */
    public boolean isDayMode() {
        CalendarState state = getState(false);
        return state.days == null || state.days.size() == 1;
    }

    /**
     * Is the calendar in a mode where two day or max 7 days of the month is shown
     *
     * @return Returns true if calendar is in weekly mode and false if not
     */
    public boolean isWeeklyMode() {
        return !isDayMode() && !isMonthlyMode();
    }

    @Override
    public void removeActionHandler(Handler actionHandler) {
        if (actionHandlers != null && actionHandlers.contains(actionHandler)) {
            actionHandlers.remove(actionHandler);
            if (actionHandlers.isEmpty()) {
                actionHandlers = null;
                actionMapper = null;
            }
            markAsDirty();
        }
    }

    private class CalendarServerRpcImpl implements CalendarServerRpc {

        @Override
        public void itemResize(int itemIndex, CalDate newStartDate, CalDate newEndDate) {

            if (!isClientChangeAllowed()) {
                return;
            }

            fireItemResize(itemIndex,
                    ZonedDateTime.of(
                            newStartDate.y, newStartDate.m, newStartDate.d,
                            newStartDate.t.h, newStartDate.t.m, newStartDate.t.s, 0, getZoneId()),
                    ZonedDateTime.of(
                            newEndDate.y, newEndDate.m, newEndDate.d,
                            newEndDate.t.h, newEndDate.t.m, newEndDate.t.s, 0, getZoneId()));
        }

        @Override
        public void itemMove(int itemIndex, CalDate newDate) {

            if (!isClientChangeAllowed()) {
                return;
            }

            if (itemIndex >= 0 && itemIndex < items.size() && items.get(itemIndex) != null) {
                fireItemMove(itemIndex, ZonedDateTime.of(
                        newDate.y, newDate.m, newDate.d, newDate.t.h, newDate.t.m, newDate.t.s, 0, getZoneId()));
            }
        }

        @Override
        public void rangeSelect(SelectionRange selectionRange) {

            if (!isClientChangeAllowed()) {
                return;
            }

            // MounthSelection
            if (selectionRange.s != null && selectionRange.e != null) {

                fireRangeSelect(
                        ZonedDateTime.of(
                                selectionRange.s.y,
                                selectionRange.s.m,
                                selectionRange.s.d,
                                0,0,0,0, getZoneId()),
                        ZonedDateTime.of(
                                selectionRange.e.y,
                                selectionRange.e.m,
                                selectionRange.e.d,
                                23, 59, 59, 999999, getZoneId()));

            } else if (selectionRange.s != null) {

                ZonedDateTime dateTime = ZonedDateTime.of(
                        selectionRange.s.y,
                        selectionRange.s.m,
                        selectionRange.s.d,
                        0,0,0,0, getZoneId());

                ZonedDateTime start = ZonedDateTime.from(dateTime.plus(selectionRange.sMin, ChronoUnit.MINUTES));
                ZonedDateTime end   = ZonedDateTime.from(dateTime.plus(selectionRange.eMin, ChronoUnit.MINUTES));

                fireRangeSelect(start, end);

            }
        }

        @Override
        public void forward() {
            fireNavigationEvent(true);
        }

        @Override
        public void backward() {
            fireNavigationEvent(false);
        }

        @Override
        public void dateClick(CalDate date) {
            fireDateClick(ZonedDateTime.of(date.y, date.m, date.d, 0,0,0,0, getZoneId()));
        }

        @Override
        public void weekClick(String eventValue) {
            if (eventValue.length() > 0 && eventValue.contains("w")) {
                String[] splitted = eventValue.split("w");
                if (splitted.length == 2) {
                    try {
                        int yr = Integer.parseInt(splitted[0]);
                        int week = Integer.parseInt(splitted[1]);
                        fireWeekClick(week, yr);
                    } catch (NumberFormatException e) {
                        // NOP
                    }
                }
            }
        }

        @Override
        public void itemClick(int itemIndex) {
            if (itemIndex >= 0 && itemIndex < items.size()
                    && items.get(itemIndex) != null) {
                fireItemClick(itemIndex);
            }
        }

        @Override
        public void scroll(int scrollPosition) {
            scrollTop = scrollPosition;
            markAsDirty();
        }

        @Override
        public void actionOnEmptyCell(String actionKey, CalDate startDate, CalDate endDate) {

            Action action = actionMapper.get(actionKey);

            for (Action.Handler ah : actionHandlers) {
                ah.handleAction(action, Calendar.this,
                        ZonedDateTime.of(startDate.y, startDate.m, startDate.d,
                                startDate.t.h, startDate.t.m, startDate.t.s, 0, getZoneId()));
            }

        }

        @Override
        public void actionOnItem(String actionKey, CalDate startDate, CalDate endDate, int itemIndex) {

            Action action = actionMapper.get(actionKey);

            for (Action.Handler ah : actionHandlers) {
                ah.handleAction(action, Calendar.this, items.get(itemIndex));
            }
        }
    }

//    @Override
//    public void changeVariables(Object source, Map<String, Object> variables) {
//        /*
//         * Only defined to fulfill the LegacyComponent interface used for
//         * calendar drag & drop. No implementation required.
//         */
//    }
//
//    @Override
//    public void paintContent(PaintTarget target) throws PaintException {
//        if (dropHandler != null) {
//            dropHandler.getAcceptCriterion().paint(target);
//        }
//    }

    /**
     * Sets whether the item captions are rendered as HTML.
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
        getState().itemCaptionAsHtml = itemCaptionAsHtml;
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
        return getState(false).itemCaptionAsHtml;
    }

    @Override
    public void readDesign(Element design, DesignContext designContext) {
        super.readDesign(design, designContext);

        Attributes attr = design.attributes();

        ZoneId zoneId = ZoneId.systemDefault();

        if (design.hasAttr("time-zone")) {
            zoneId = ZoneId.of(DesignAttributeHandler.readAttribute("end-date", attr, String.class));
        }

        if (design.hasAttr("time-format")) {
            setTimeFormat(TimeFormat.valueOf(
                    "Format" + design.attr("time-format").toUpperCase()));
        }

        if (design.hasAttr("start-date")) {
            setStartDate(
                    ZonedDateTime.ofInstant(DesignAttributeHandler.readAttribute("start-date", attr, Date.class)
                            .toInstant(), zoneId));
        }

        if (design.hasAttr("end-date")) {
            setEndDate(
                    ZonedDateTime.ofInstant(DesignAttributeHandler.readAttribute("end-date", attr, Date.class)
                            .toInstant(), zoneId));
        }
    }

    @Override
    public void writeDesign(Element design, DesignContext designContext) {
        super.writeDesign(design, designContext);

        if (currentTimeFormat != null) {
            design.attr("time-format",
                    currentTimeFormat == TimeFormat.Format12H ? "12h" : "24h");
        }
        if (startDate != null) {
            design.attr("start-date", DATE_FORMAT.format(getStartDate()));
        }
        if (endDate != null) {
            design.attr("end-date", DATE_FORMAT.format(getEndDate()));
        }
        if (!getZoneId().equals(ZoneId.systemDefault())) {
            design.attr("time-zone", getZoneId().getId());
        }
    }

    @Override
    protected Collection<String> getCustomAttributes() {
        Collection<String> customAttributes = super.getCustomAttributes();
        customAttributes.add("time-format");
        customAttributes.add("start-date");
        customAttributes.add("end-date");
        return customAttributes;
    }

    /*
     * Allow setting first day of week independent of Locale. Set to null if you
     * want first day of week being defined by the locale
     *
     * @since 7.6
     * @param dayOfWeek
     *            any of java.util.Calendar.SUNDAY..java.util.Calendar.SATURDAY
     *            or null to revert to default first day of week by locale

    public void setFirstDayOfWeek(Integer dayOfWeek) {

        int minimalSupported = java.util.Calendar.SUNDAY;
        int maximalSupported = java.util.Calendar.SATURDAY;

        if (dayOfWeek != null && (dayOfWeek < minimalSupported || dayOfWeek > maximalSupported)) {
            throw new IllegalArgumentException(String.format(
                    "Day of week must be between %s and %s. Actually received: %s",
                    minimalSupported, maximalSupported, dayOfWeek));
        }

        customFirstDayOfWeek = dayOfWeek;
        markAsDirty();
    } */

    /**
     * Add a time block start index. Time steps are half hour beginning at 0
     * and a minimal time slot length of 1800000 milliseconds is used.
     *
     * @param day Day for this time slot
     * @param fromMillies time millies from where the block starts
     * @param styleName css class for this block (currently unused)
     */
    protected final void addTimeBlockInternaly(Date day, Long fromMillies, String styleName) {
        Set<Long> times;
        if (blockedTimes.containsKey(day)) {
            times = blockedTimes.get(day);
        } else {
            times = new HashSet<>();
        }
        times.add(fromMillies);
        blockedTimes.put(day, times);
    }

    /**
     * Add a time block marker for a range of time. Time steps are half hour,
     * so a minimal time slot is 1800000 milliseconds long.
     *
     * @param fromMillies time millies from where the block starts
     * @param toMillies time millies from where the block ends
     */
    public void addTimeBlock(long fromMillies, long toMillies) {
        addTimeBlock(fromMillies, toMillies, "");
    }

    /**
     * Add a time block marker for a range of time. Time steps are half hour,
     * so a minimal time slot is 1800000 milliseconds long.
     *
     * @param fromMillies time millies from where the block starts
     * @param toMillies time millies from where the block ends
     * @param styleName css class for this block (currently unused)
     */
    public void addTimeBlock(long fromMillies, long toMillies, String styleName) {
        addTimeBlock(allOverDate, fromMillies, toMillies, styleName);
    }

    /**
     * Add a time block marker for a range of time. Time steps are half hour,
     * so a minimal time slot is 1800000 milliseconds long.
     *
     * @param day Day for this time slot
     * @param fromMillies time millies from where the block starts
     * @param toMillies time millies from where the block ends
     */
    public void addTimeBlock(Date day, long fromMillies, long toMillies) {
        addTimeBlock(day, fromMillies, toMillies, "");
    }

    /**
     * Add a time block marker for a range of time. Time steps are half hour,
     * so a minimal time slot is 1800000 milliseconds long.
     *
     * @param day Day for this time slot
     * @param fromMillies time millies from where the block starts
     * @param toMillies time millies from where the block ends
     * @param styleName css class for this block (currently unused)
     */
    public void addTimeBlock(Date day, long fromMillies, long toMillies, String styleName) {
        assert (toMillies > fromMillies && fromMillies % 1800000 == 0 && toMillies % 1800000 == 0);

        while (fromMillies < toMillies) {

            addTimeBlockInternaly(day, fromMillies, styleName);
            fromMillies += 1800000;
        }

        markAsDirty();
    }

    public void clearBlockedTimes() {
        blockedTimes.clear();
        markAsDirty();
    }

    public void clearBlockedTimes(Date day) {
        if (blockedTimes.containsKey(day)) {
            blockedTimes.remove(day);
        }
        markAsDirty();
    }

}