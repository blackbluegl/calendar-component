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

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.vaadin.addon.calendar.client.ui.VCalendar;

/**
 *
 * @since 7.1.0
 * @author Vaadin Ltd.
 *
 */
public class SimpleDayToolbar extends HorizontalPanel implements ClickHandler {

    public static int STYLE_BACK_PIXEL_WIDTH = 20;
    public static int STYLE_NEXT_PIXEL_WIDTH = 15;

    private int width = 0;
    private boolean isWidthUndefined = false;

    protected Button backLabel;
    protected Button nextLabel;

    private VCalendar calendar;

    public SimpleDayToolbar(VCalendar calendar) {

        this.calendar = calendar;

        setStylePrimaryName("v-calendar-header-month");

        backLabel = new Button();
        backLabel.setStylePrimaryName("v-calendar-back");
        backLabel.addClickHandler(this);

        nextLabel = new Button();
        nextLabel.addClickHandler(this);
        nextLabel.setStylePrimaryName("v-calendar-next");

    }

    public void setDayNames(String[] dayNames) {
        clear();

        addBackButton();

        for (String dayName : dayNames) {
            Label l = new Label(dayName);
            l.setStylePrimaryName("v-calendar-header-day");
            add(l);
        }

        addNextButton();

        updateCellWidth();
    }

    public void setWidthPX(int width) {
        this.width = width;

        setWidthUndefined(width == -1);

        if (!isWidthUndefined()) {
            super.setWidth(this.width + "px");
            if (getWidgetCount() == 0) {
                return;
            }
        }
        updateCellWidth();
    }

    private boolean isWidthUndefined() {
        return isWidthUndefined;
    }

    private void setWidthUndefined(boolean isWidthUndefined) {
        this.isWidthUndefined = isWidthUndefined;

        if (isWidthUndefined) {
            addStyleDependentName("Hsized");

        } else {
            removeStyleDependentName("Hsized");
        }
    }

    private void updateCellWidth() {

        setCellWidth(backLabel, STYLE_BACK_PIXEL_WIDTH + "px");
        setCellWidth(nextLabel, STYLE_NEXT_PIXEL_WIDTH + "px");
        setCellHorizontalAlignment(backLabel, ALIGN_LEFT);
        setCellHorizontalAlignment(nextLabel, ALIGN_RIGHT);

        int cellw = -1;
        int widgetCount = getWidgetCount();
        if (widgetCount <= 0) {
            return;
        }

        if (isWidthUndefined()) {

            Widget widget = getWidget(1);
            String w = widget.getElement().getStyle().getWidth();

            if (w.length() > 2) {
                cellw = Integer.parseInt(w.substring(0, w.length() - 2));
            }

        } else {
            cellw = width / getWidgetCount();
        }

        if (cellw > 0) {

            int cW;

            for (int i = 1; i < getWidgetCount() -1; i++) {

                Widget widget = getWidget(i);

                cW = cellw - (i == getWidgetCount() -2 ? STYLE_NEXT_PIXEL_WIDTH : 0);

                setCellWidth(widget, cW + "px");
            }
        }
    }

    private void addBackButton() {
        if (!calendar.isBackwardNavigationEnabled()) {
            nextLabel.getElement().getStyle().setHeight(0, Style.Unit.PX);
        }
        add(backLabel);
    }

    private void addNextButton() {
        if (!calendar.isForwardNavigationEnabled()) {
            backLabel.getElement().getStyle().setHeight(0, Style.Unit.PX);
        }
        add(nextLabel);
    }

    @Override
    public void onClick(ClickEvent event) {
        if (!calendar.isDisabled()) {
            if (event.getSource() == nextLabel) {
                if (calendar.getForwardListener() != null) {
                    calendar.getForwardListener().forward();
                }
            } else if (event.getSource() == backLabel) {
                if (calendar.getBackwardListener() != null) {
                    calendar.getBackwardListener().backward();
                }
            }
        }
    }
}
