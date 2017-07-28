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
import java.util.List;

/**
 * Interface for querying items. The Vaadin Calendar always has a
 * CalendarItemProvider set.
 *
 * @since 7.1.0
 * @author Vaadin Ltd.
 */

@FunctionalInterface
public interface CalendarItemProvider<ITEM extends CalendarItem> extends Serializable {

    /**
     * <p>
     * Gets all available items in the target date range between startDate and
     * endDate. The Vaadin Calendar queries the items from the range that is
     * shown, which is not guaranteed to be the same as the date range that is
     * set.
     * </p>
     *
     * <p>
     * For example, if you set the date range to be monday 22.2.2010 - wednesday
     * 24.2.2010, the used Item Provider will be queried for items between
     * monday 22.2.2010 00:00 and sunday 28.2.2010 23:59. Generally you can
     * expect the date range to be expanded to whole days and whole weeks.
     * </p>
     *
     * @param startDate
     *            Start date
     * @param endDate
     *            End date
     * @return List of items
     */
    List<ITEM> getItems(ZonedDateTime startDate, ZonedDateTime endDate);

    /**
     * Item to signal that the set of items has changed and the calendar
     * should refresh its view from the CalendarItemProvider.
     *
     */
    @SuppressWarnings("serial")
    class ItemSetChangedEvent<EVENT extends CalendarItem> implements Serializable {

        private CalendarItemProvider<EVENT> source;

        public ItemSetChangedEvent(CalendarItemProvider<EVENT> source) {
            this.source = source;
        }

        /**
         * @return the CalendarItemProvider that has changed
         */
        public CalendarItemProvider<EVENT> getProvider() {
            return source;
        }
    }

    /**
     * Listener for EventSetChange items.
     */

    @FunctionalInterface
    interface ItemSetChangedListener extends Serializable {

        /**
         * Called when the set of Events has changed.
         */

        void itemSetChanged(ItemSetChangedEvent changeEvent);
    }

    /**
     * Notifier interface for EventSetChange items.
     */

    interface ItemSetChangedNotifier extends Serializable {

        /**
         * Add a listener for listening to when new items are adding or removed
         * from the event provider.
         *
         * @param listener
         *            The listener to add
         */
        void addItemSetChangedListener(ItemSetChangedListener listener);

        /**
         * Remove a listener which listens to {@link ItemSetChangedEvent}-items
         *
         * @param listener
         *            The listener to remove
         */
        void removeItemSetChangedListener(ItemSetChangedListener listener);
    }
}
