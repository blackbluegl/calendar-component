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

import com.google.gwt.i18n.client.DateTimeFormat;
import org.vaadin.addon.calendar.client.DateConstants;

import java.util.Date;

/**
 * A client side implementation of a calendar event
 *
 * @since 7.1
 * @author Vaadin Ltd.
 */
public class CalendarItem {

    public static final String SINGLE_TIME = "%s";
    public static final String RANGE_TIME = "%s - %s";

    private int index;
    private String caption;
    private Date start, end;
    private String styleName;
    private Date startTime, endTime;
    private String description;
    private int slotIndex = -1;
    private boolean format24h;

    private String dateCaptionFormat = SINGLE_TIME;

    DateTimeFormat dateformat_date = DateTimeFormat.getFormat("h:mm a"); // TODO make user adjustable
    DateTimeFormat dateformat_date24 = DateTimeFormat.getFormat("H:mm"); // TODO make user adjustable
    private boolean allDay;

    private boolean moveable = true;
    private boolean resizeable = true;
    private boolean clickable = true;

    /**
     * @return The time caption format (eg. ['%s'] )
     */
    public String getDateCaptionFormat() {
        return dateCaptionFormat;
    }

    /**
     * Set the time caption format. Only the '%s' placeholder is supported.
     *
     * @param dateCaptionFormat The time caption format
     */
    public void setDateCaptionFormat(String dateCaptionFormat) {
        this.dateCaptionFormat = dateCaptionFormat;
    }

    /**
     * @see org.vaadin.addon.calendar.item.CalendarItem#getStyleName()
     */
    public String getStyleName() {
        return styleName;
    }

    /**
     * @see org.vaadin.addon.calendar.item.CalendarItem#getStart()
     */
    public Date getStart() {
        return start;
    }

    /**
     * @see org.vaadin.addon.calendar.item.CalendarItem#getStyleName()
     * @param style  The stylename
     */
    public void setStyleName(String style) {
        styleName = style;
    }

    /**
     * @see org.vaadin.addon.calendar.item.CalendarItem#getStart()
     * @param start The start date
     */
    public void setStart(Date start) {
        this.start = start;
    }

    /**
     * @see org.vaadin.addon.calendar.item.CalendarItem#getEnd()
     * @return The end date
     */
    public Date getEnd() {
        return end;
    }

    /**
     * @see org.vaadin.addon.calendar.item.CalendarItem#getEnd()
     * @param end The end date
     */
    public void setEnd(Date end) {
        this.end = end;
    }

    /**
     * Returns the start time of the event
     *
     * @return Time embedded in the {@link Date} object
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Set the start time of the event
     *
     * @param startTime
     *            The time of the event. Use the time fields in the {@link Date}
     *            object
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Get the end time of the event
     *
     * @return Time embedded in the {@link Date} object
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Set the end time of the event
     *
     * @param endTime
     *            Time embedded in the {@link Date} object
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * Get the (server side) index of the event
     *
     * @return the (server side) index of the event
     */
    public int getIndex() {
        return index;
    }

    /**
     * Get the index of the slot where the event in rendered
     *
     * @return the index of the slot where the event in rendered
     */
    public int getSlotIndex() {
        return slotIndex;
    }

    /**
     * Set the index of the slot where the event in rendered
     *
     * @param index
     *            The index of the slot
     */
    public void setSlotIndex(int index) {
        slotIndex = index;
    }

    /**
     * Set the (server side) index of the event
     *
     * @param index
     *            The index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Get the caption of the event. The caption is the text displayed in the
     * calendar on the event.
     *
     * @return The visible caption of the event
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Set the caption of the event. The caption is the text displayed in the
     * calendar on the event.
     *
     * @param caption
     *            The visible caption of the event
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * Get the description of the event. The description is the text displayed
     * when hoovering over the event with the mouse
     *
     * @return The description is the text displayed
     *                      when hoovering over the event with the mouse
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of the event. The description is the text displayed
     * when hoovering over the event with the mouse
     *
     * @param description The description is the text displayed
     *                      when hoovering over the event with the mouse
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Does the event use the 24h time format
     *
     * @param format24h
     *            True if it uses the 24h format, false if it uses the 12h time
     *            format
     */
    public void setFormat24h(boolean format24h) {
        this.format24h = format24h;
    }

    /**
     * Is the event an all day event.
     *
     * @param allDay
     *            True if the event should be rendered all day
     */
    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    /**
     * Is the event an all day event.
     *
     * @return The event an all day event.
     */
    public boolean isAllDay() {
        return allDay;
    }

    /**
     * Get the start time as a formatted string
     *
     * @return The start time as a formatted string
     */
    public String getFormattedStartTime() {
        if (format24h) {
            return dateformat_date24.format(startTime);
        } else {
            return dateformat_date.format(startTime);
        }
    }
    /**
     * Get the end time as a formatted string
     *
     * @return The end time as a formatted string
     */
    public String getFormattedEndTime() {
        if (format24h) {
            return dateformat_date24.format(endTime);
        } else {
            return dateformat_date.format(endTime);
        }
    }


    /**
     * Get the amount of milliseconds between the start and end of the event
     *
     * @return the amount of milliseconds between the start and end of the event
     */
    public long getRangeInMilliseconds() {
        return getEndTime().getTime() - getStartTime().getTime();
    }

    /**
     * Get the amount of minutes between the start and end of the event
     *
     * @return the amount of minutes between the start and end of the event
     */
    public long getRangeInMinutes() {
        return (getRangeInMilliseconds() / DateConstants.MINUTEINMILLIS);
    }

    /**
     * Answers whether the start of the event and end of the event is within
     * the same day. 
     *
     * @return true if start and end are in the same day, false otherwise
     */
    @SuppressWarnings("deprecation")
    public boolean isSingleDay() {
        Date start = getStart();
        Date end = getEnd();
        return start.getYear() == end.getYear() && start.getMonth() == end.getMonth() && start.getDate() == end.getDate();
    }

    /**
     * Get the amount of minutes for the event on a specific day. This is useful
     * if the event spans several days.
     *
     * @param targetDay
     *            The date to check
     * @return the amount of minutes for the event on a specific day. This is useful
     * if the event spans several days.
     */
    public long getRangeInMinutesForDay(Date targetDay) {

        long rangeInMinutesForDay;

        // we must take into account that here can be not only 1 and 2 days, but
        // 1, 2, 3, 4... days first and last days - special cases all another
        // days between first and last - have range "ALL DAY"
        if (isTimeOnDifferentDays()) {
            if (targetDay.compareTo(getStart()) == 0) { // for first day
                rangeInMinutesForDay = DateConstants.DAYINMINUTES
                        - (getStartTime().getTime() - getStart().getTime())
                        / DateConstants.MINUTEINMILLIS;

            } else if (targetDay.compareTo(getEnd()) == 0) { // for last day
                rangeInMinutesForDay = (getEndTime().getTime()
                        - getEnd().getTime())
                        / DateConstants.MINUTEINMILLIS;

            } else { // for in-between days
                rangeInMinutesForDay = DateConstants.DAYINMINUTES;
            }
        } else { // simple case - period is in one day
            rangeInMinutesForDay = getRangeInMinutes();
        }
        return rangeInMinutesForDay;
    }

    /**
     * Does the item span several days
     *
     * @return  true, if the item span several days
     */
    @SuppressWarnings("deprecation")
    public boolean isTimeOnDifferentDays() {
        // if difference between start and end times is more than day - of
        // course it is not one day, but several days

        return getEndTime().getTime() - getStartTime().getTime() > DateConstants.DAYINMILLIS
                || getStart().compareTo(getEnd()) != 0
                && !((getEndTime().getHours() == 0 && getEndTime().getMinutes() == 0));
    }

    public boolean isMoveable() {
        return moveable;
    }

    public void setMoveable(boolean moveable) {
        this.moveable = moveable;
    }

    public boolean isResizeable() {
        return resizeable;
    }

    public void setResizeable(boolean resizeable) {
        this.resizeable = resizeable;
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }
}
