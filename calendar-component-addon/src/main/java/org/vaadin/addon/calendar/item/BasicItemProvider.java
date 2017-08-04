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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * {@link ItemSetChangedNotifier
 * ItemSetChangedNotifier} and
 * {@link EditableCalendarItem.ItemChangeListener
 * ItemChangeListener} are also implemented, so the Calendar is notified when
 * an event is added, changed or removed.
 * </p>
 *
 * @since 7.1.0
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")

public class BasicItemProvider<ITEM extends BasicItem> implements
        CalendarEditableItemProvider<ITEM>,
        CalendarItemProvider.ItemSetChangedNotifier,
        EditableCalendarItem.ItemChangeListener {

    protected List<ITEM> itemList = new ArrayList<>();

    private List<ItemSetChangedListener> listeners = new ArrayList<>();

    /*
     * (non-Javadoc)
     *
     * @see
     * org.vaadin.addon.calendar.event.CalendarItemProvider#getItems(java.
     * util.Date, java.util.Date)
     */
    @Override
    public List<ITEM> getItems(ZonedDateTime startDate, ZonedDateTime endDate) {

        final long startRange = startDate.toEpochSecond();
        final long endRange = endDate.toEpochSecond();

        return itemList.parallelStream().filter(i -> {

            long itemStart = i.getStart().toEpochSecond();
            long itemEnd = i.getEnd().toEpochSecond();

            // Select only items that overlaps with startDate and endDate.

            return ((itemStart >= startRange && itemStart <= endRange)
                    || (itemEnd >= startRange && itemEnd <= endRange)
                    || (itemStart <= startRange && itemEnd >= endRange));

        }).collect(Collectors.toList());
    }

    /**
     * Does this event provider container this event
     *
     * @param item
     *            The event to check for
     * @return If this provider has the event then true is returned, else false
     */
    @SuppressWarnings("unused")
    public boolean containsEvent(ITEM item) {
        return itemList.contains(item);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.vaadin.addon.calendar.ui.CalendarComponentEvents.
     * ItemSetChangedNotifier #addListener
     * (org.vaadin.addon.calendar.ui.CalendarComponentEvents.
     * ItemSetChangedListener )
     */
    @Override
    public void addItemSetChangedListener(ItemSetChangedListener listener) {
        listeners.add(listener);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.vaadin.addon.calendar.ui.CalendarComponentEvents.
     * ItemSetChangedNotifier #removeListener
     * (org.vaadin.addon.calendar.ui.CalendarComponentEvents.
     * ItemSetChangedListener )
     */
    @Override
    public void removeItemSetChangedListener(ItemSetChangedListener listener) {
        listeners.remove(listener);
    }

    /**
     * Fires a eventsetchange event. The event is fired when either an event is
     * added or removed to the event provider
     */
    protected void fireItemSetChanged() {
        ItemSetChangedEvent<ITEM> changeEvent = new ItemSetChangedEvent<>(this);
        for (ItemSetChangedListener listener : listeners) {
            listener.itemSetChanged(changeEvent);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.vaadin.addon.calendar.ui.CalendarComponentEvents.ItemChangeListener
     * #itemChanged
     * (org.vaadin.addon.calendar.ui.CalendarComponentEvents.EventSetChange)
     */
    @Override
    public void itemChanged(EditableCalendarItem.ItemChangedEvent changedEvent) {
        // naive implementation
        fireItemSetChanged();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.vaadin.addon.calendar.event.CalendarEditableItemProvider#addItem
     * (org.vaadin.addon.calendar.event.CalendarItem)
     */
    @Override
    public void addItem(ITEM item) {
        itemList.add(item);

        item.getNotifier().addListener(this);

        fireItemSetChanged();
    }

    /*
         * (non-Javadoc)
         *
         * @see
         * org.vaadin.addon.calendar.event.CalendarEditableItemProvider#removeItem
         * (org.vaadin.addon.calendar.event.CalendarItem)
         */
    @Override
    public void removeItem(ITEM item) {
        itemList.remove(item);

        item.getNotifier().removeListener(this);

        fireItemSetChanged();
    }

    public void setItems(Collection<ITEM> items) {

        for (ITEM item : items) {
            itemList.add(item);
            item.getNotifier().addListener(this);
        }

        fireItemSetChanged();
    }
}
