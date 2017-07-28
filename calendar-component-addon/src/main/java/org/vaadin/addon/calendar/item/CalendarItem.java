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

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * <p>
 * Item in the calendar. Customize your own event by implementing this
 * interface.
 * </p>
 *
 * <li>Start and end fields are mandatory.</li>
 *
 * <li>In "allDay" items longer than one day, starting and ending clock times
 * are omitted in UI and only dates are shown.</li>
 *
 * @since 7.1.0
 * @author Vaadin Ltd.
 *
 */

public interface CalendarItem extends Serializable {

    /**
     * Gets start date of event.
     *
     * @return Start date.
     */
    ZonedDateTime getStart();

    /**
     * Get end date of event.
     *
     * @return End date;
     */
    ZonedDateTime getEnd();

    /**
     * Gets caption of event.
     *
     * @return Caption text
     */
    String getCaption();

    /**
     * Gets description of event. Shown as a tooltip over the event.
     *
     * @return Description text.
     */
    String getDescription();

    /**
     * <p>
     * Gets style name of event. In the client, style name will be set to the
     * event's element class name and can be styled by CSS
     * </p>
     * Styling example:</br>
     * <code>Java code: </br>
     * event.setStyleName("color1");
     * </br></br>
     * CSS:</br>
     * .v-calendar-event-color1 {</br>
     * &nbsp;&nbsp;&nbsp;background-color: #9effae;</br>}</code>
     *
     * @return Style name.
     */
    String getStyleName();

    /**
     * An all-day event typically does not occur at a specific time but targets
     * a whole day or days. The rendering of all-day items differs from normal
     * items.
     *
     * @return true if this event is an all-day event, (default)false otherwise
     */
    default boolean isAllDay() {
        return false;
    }

    /**
     * @return (default)true, if this item is moveable.
     */
    default boolean isMoveable() {
        return true;
    }

    /**
     * @return (default)true, if this item is resizeable.
     */
    default boolean isResizeable() {
        return true;
    }

    /**
     * @return (default)true, if this item is clickable.
     */
    default boolean isClickable() {
        return true;
    }

}
