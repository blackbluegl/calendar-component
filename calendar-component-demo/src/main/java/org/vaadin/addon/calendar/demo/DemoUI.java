package org.vaadin.addon.calendar.demo;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.addon.calendar.demo.meetings.MeetingCalendar;

import javax.servlet.annotation.WebServlet;

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
                new CalStyle("Day 1 - 7", () -> calendar.setWeekDayRange(1, 7)),
                new CalStyle("Day 1 - 5", () -> calendar.setWeekDayRange(1, 5)),
                new CalStyle("Day 2 - 5", () -> calendar.setWeekDayRange(2, 5)),
                new CalStyle("Weekend",   () -> calendar.setWeekDayRange(6, 7))
        );
        calActionComboBox.addValueChangeListener(e -> e.getValue().act());
        calActionComboBox.setEmptySelectionAllowed(false);

        HorizontalLayout nav = new HorizontalLayout(calActionComboBox);
        nav.setWidth("100%");

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
