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

import java.util.Collection;

/**
 * An item provider which allows adding and removing item
 *
 * @since 7.1.0
 * @author Vaadin Ltd.
 * @author l. guettler
 */

public interface CalendarEditableItemProvider<EDITITEM extends EditableCalendarItem> extends CalendarItemProvider<EDITITEM> {

    /**
     * Adds an item to the event provider
     *
     * @param item
     *            The item to add
     */
    void addItem(EDITITEM item);

    /**
     * Removes an item from the event provider
     *
     * @param item
     *            The item
     */
    void removeItem(EDITITEM item);

    /**
     * Add a set of items to the event provider
     *
     * @param items The item set
     */
    void setItems(Collection<EDITITEM> items);

}
