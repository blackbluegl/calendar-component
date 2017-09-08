package org.vaadin.addon.calendar.demo;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import org.vaadin.addon.calendar.demo.meetings.MeetingCalendar;

import javax.servlet.annotation.WebServlet;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;

@Theme("demo")
@Title("Calendar Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI
{

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class)
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest request) {

        // Initialize our new UI component
        MeetingCalendar calendar = new MeetingCalendar();
        calendar.setSizeFull();

        ComboBox<CalStyle> calActionComboBox = new ComboBox<>();
        calActionComboBox.setItems(
                new CalStyle("Col 1 - 7", () -> calendar.getCalendar().withVisibleDays(1, 7)),
                new CalStyle("Col 1 - 5", () -> calendar.getCalendar().withVisibleDays(1, 5)),
                new CalStyle("Col 2 - 5", () -> calendar.getCalendar().withVisibleDays(2, 5)),
                new CalStyle("Col 6 - 7", () -> calendar.getCalendar().withVisibleDays(6, 7))
        );
        calActionComboBox.addValueChangeListener(e -> e.getValue().act());
        calActionComboBox.setEmptySelectionAllowed(false);

        Button fixedSize = new Button("fixed Size", (Button.ClickEvent clickEvent) -> calendar.panel.setHeightUndefined());
        fixedSize.setIcon(VaadinIcons.LINK);

        Button fullSize = new Button("full Size", (Button.ClickEvent clickEvent) -> calendar.panel.setHeight(100, Unit.PERCENTAGE));
        fullSize.setIcon(VaadinIcons.UNLINK);

        ComboBox<Month> months = new ComboBox<>();
        months.setItems(Month.values());
        months.setItemCaptionGenerator(month -> month.getDisplayName(TextStyle.FULL, calendar.getCalendar().getLocale()));
        months.setEmptySelectionAllowed(false);
        months.addValueChangeListener(me -> calendar.switchToMonth(me.getValue()));

        Button today = new Button("today", (Button.ClickEvent clickEvent) -> calendar.getCalendar().withDay(ZonedDateTime.now()));
        Button week = new Button("week", (Button.ClickEvent clickEvent) -> calendar.getCalendar().withWeek(ZonedDateTime.now()));

        HorizontalLayout nav = new HorizontalLayout(calActionComboBox, fixedSize, fullSize, months, today, week);
        //nav.setWidth("100%");

        // Show it in the middle of the screen
        final VerticalLayout layout = new VerticalLayout();
        layout.setStyleName("demoContentLayout");
        layout.setSizeFull();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addComponent(nav);
        layout.addComponentsAndExpand(calendar);
        setContent(layout);

    }

    private static class CalStyle {

        @FunctionalInterface
        interface CalAction {
            void update();
        }

        private String caption;

        private CalAction action;

        CalStyle(String caption, CalAction action) {
            this.caption = caption;
            this.action = action;
        }

        private void act() {
            action.update();
        }

        @Override
        public String toString() {
            return caption;
        }
    }

}
