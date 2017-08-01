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

import com.vaadin.shared.annotations.Delayed;
import com.vaadin.shared.communication.ServerRpc;
import org.vaadin.addon.calendar.client.ui.schedule.CalDate;
import org.vaadin.addon.calendar.client.ui.schedule.SelectionRange;

/**
 * @since 7.1
 * @author Vaadin Ltd.
 */
public interface CalendarServerRpc extends ServerRpc {

    void itemMove(int itemIndex, CalDate date);

    void itemClick(int itemIndex);

    void itemResize(int itemIndex, CalDate newStartDate, CalDate newEndDate);

    void rangeSelect(SelectionRange dateSelectionRange);

    void forward();

    void backward();

    void dateClick(CalDate date);

    void weekClick(String eventValue);

    void actionOnEmptyCell(String actionKey, String startDate, String endDate);

    void actionOnItem(String actionKey, String startDate, String endDate, int itemIndex);

    @Delayed(lastOnly = true)
    void scroll(int scrollPosition);
}
