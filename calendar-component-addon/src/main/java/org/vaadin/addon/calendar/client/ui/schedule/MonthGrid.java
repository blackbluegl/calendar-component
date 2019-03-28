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

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import org.vaadin.addon.calendar.client.DateConstants;
import org.vaadin.addon.calendar.client.ui.VCalendar;

import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * @since 7.1
 * @author Vaadin Ltd.
 *
 */
public class MonthGrid extends FocusableGrid implements KeyDownHandler {

    private SimpleDayCell selectionStart;
    private SimpleDayCell selectionEnd;
    private final VCalendar calendar;
    private boolean rangeSelectDisabled;
    private boolean enabled = true;
    private final HandlerRegistration keyDownHandler;

    public MonthGrid(VCalendar parent, int rows, int columns) {
        super(rows, columns);
        calendar = parent;
        setCellSpacing(0);
        setCellPadding(0);
        setStylePrimaryName("v-calendar-month");

        keyDownHandler = addKeyDownHandler(this);
    }

    @Override
    protected void onUnload() {
        keyDownHandler.removeHandler();
        super.onUnload();
    }

    public void setSelectionEnd(SimpleDayCell simpleDayCell) {
        selectionEnd = simpleDayCell;
        updateSelection();
    }

    public void setSelectionStart(SimpleDayCell simpleDayCell) {
        if (!rangeSelectDisabled && isEnabled()) {
            selectionStart = simpleDayCell;
            setFocus(true);
        }

    }

    private void updateSelection() {

        if (selectionStart == null) {
            return;
        }

        if (selectionEnd != null) {
            Date startDate = selectionStart.getDate();
            Date endDate = selectionEnd.getDate();
            for (int row = 0; row < getRowCount(); row++) {
                for (int cell = 0; cell < getCellCount(row); cell++) {
                    SimpleDayCell sdc = (SimpleDayCell) getWidget(row, cell);
                    if (sdc == null) {
                        return;
                    }
                    Date d = sdc.getDate();
                    if (startDate.compareTo(d) <= 0
                            && endDate.compareTo(d) >= 0) {
                        sdc.addStyleDependentName("selected");

                    } else if (startDate.compareTo(d) >= 0
                            && endDate.compareTo(d) <= 0) {
                        sdc.addStyleDependentName("selected");

                    } else {
                        sdc.removeStyleDependentName("selected");

                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void setSelectionReady() {
        if (selectionStart != null && selectionEnd != null) {

            Date startDate = selectionStart.getDate();
            Date endDate = selectionEnd.getDate();
            if (startDate.compareTo(endDate) > 0) {
                Date temp = startDate;
                startDate = endDate;
                endDate = temp;
            }

            if (calendar.getRangeSelectListener() != null) {

                SelectionRange weekSelection = new SelectionRange();
                weekSelection.setStartDay(DateConstants.toRPCDate(
                        startDate.getYear(),
                        startDate.getMonth(),
                        startDate.getDate()));
                weekSelection.setEndDay(DateConstants.toRPCDate(
                        endDate.getYear(),
                        endDate.getMonth(),
                        endDate.getDate()));

                calendar.getRangeSelectListener().rangeSelected(weekSelection);
            }
            selectionStart = null;
            selectionEnd = null;
            setFocus(false);
        }
    }

    public void cancelRangeSelection() {
        if (selectionStart != null && selectionEnd != null) {
            for (int row = 0; row < getRowCount(); row++) {
                for (int cell = 0; cell < getCellCount(row); cell++) {
                    SimpleDayCell sdc = (SimpleDayCell) getWidget(row, cell);
                    if (sdc == null) {
                        return;
                    }
                    sdc.removeStyleDependentName("selected");
                }
            }
        }
        setFocus(false);
        selectionStart = null;
    }

    public void updateCellSizes(int totalWidthPX, int totalHeightPX) {

        boolean setHeight = totalHeightPX > 0;
        boolean setWidth = totalWidthPX > 0;

        int rows = getRowCount();
        int cells = getCellCount(0);

        int cellWidth = (totalWidthPX / cells) - 1;
        int widthRemainder = totalWidthPX % cells;

        // Division for cells might not be even. Distribute it evenly to will whole space.
        int cellHeight = (totalHeightPX / rows) - 1;
        int heightRemainder = totalHeightPX % rows;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cells; j++) {

                SimpleDayCell dayCell = (SimpleDayCell) getWidget(i, j);

                if (setWidth) {
                    if (widthRemainder > 0) {
                        dayCell.setWidth(cellWidth + 1 + "px");
                        widthRemainder--;

                    } else {
                        dayCell.setWidth(cellWidth + "px");
                    }
                }

                if (setHeight) {
                    if (heightRemainder > 0) {
                        dayCell.setHeightPX(cellHeight + 1, true);

                    } else {
                        dayCell.setHeightPX(cellHeight, true);
                    }
                } else {
                    dayCell.setHeightPX(-1, true);
                }
            }
            heightRemainder--;
        }
    }

    /**
     * Disable or enable possibility to select ranges
     */
    @SuppressWarnings("unused")
    public void setRangeSelect(boolean b) {
        rangeSelectDisabled = !b;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
        int keycode = event.getNativeKeyCode();
        if (KeyCodes.KEY_ESCAPE == keycode && selectionStart != null) {
            cancelRangeSelection();
        }
    }

    public int getDayCellIndex(SimpleDayCell dayCell) {
        int rows = getRowCount();
        int cells = getCellCount(0);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cells; j++) {
                SimpleDayCell sdc = (SimpleDayCell) getWidget(i, j);
                if (dayCell == sdc) {
                    return i * cells + j;
                }
            }
        }

        return -1;
    }

    private static Logger getLogger() {
        return Logger.getLogger(MonthGrid.class.getName());
    }
}
