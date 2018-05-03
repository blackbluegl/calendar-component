package org.vaadin.addon.calendar.demo;

import javax.servlet.annotation.WebServlet;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.addon.calendar.demo.meetings.MeetingCalendar;

@Theme("demo")
@Title("Calendar Add-on Demo")
@Push(transport = Transport.LONG_POLLING)
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
        MeetingCalendar meetings = new MeetingCalendar();
        meetings.setSizeFull();

        ComboBox<Locale> localeBox = new ComboBox<>();
        localeBox.setItems(Locale.getAvailableLocales());
        localeBox.setEmptySelectionAllowed(false);
        localeBox.setValue(UI.getCurrent().getLocale());
        localeBox.addValueChangeListener(e -> meetings.getCalendar().setLocale(e.getValue()));

        ComboBox<String> zoneBox = new ComboBox<>();
        zoneBox.setItems(ZoneId.getAvailableZoneIds());
        zoneBox.setEmptySelectionAllowed(false);
        zoneBox.setValue(meetings.getCalendar().getZoneId().getId());
        zoneBox.addValueChangeListener(e -> meetings.getCalendar().setZoneId(ZoneId.of(e.getValue())));


        CalStyle initial = new CalStyle("Day 1 - 7", () -> meetings.getCalendar().withVisibleDays(1, 7));

        ComboBox<CalStyle> calActionComboBox = new ComboBox<>();
        calActionComboBox.setItems(
                initial,
                new CalStyle("Day 1 - 5", () -> meetings.getCalendar().withVisibleDays(1, 5)),
                new CalStyle("Day 2 - 5", () -> meetings.getCalendar().withVisibleDays(2, 5)),
                new CalStyle("Day 6 - 7", () -> meetings.getCalendar().withVisibleDays(6, 7))
        );
        calActionComboBox.addValueChangeListener(e -> e.getValue().act());
        calActionComboBox.setEmptySelectionAllowed(false);

        Button fixedSize = new Button("fixed Size", (Button.ClickEvent clickEvent) -> meetings.panel.setHeightUndefined());
        fixedSize.setIcon(VaadinIcons.LINK);

        Button fullSize = new Button("full Size", (Button.ClickEvent clickEvent) -> meetings.panel.setHeight(100, Unit.PERCENTAGE));
        fullSize.setIcon(VaadinIcons.UNLINK);

        ComboBox<Month> months = new ComboBox<>();
        months.setItems(Month.values());
        months.setItemCaptionGenerator(month -> month.getDisplayName(TextStyle.FULL, meetings.getCalendar().getLocale()));
        months.setEmptySelectionAllowed(false);
        months.addValueChangeListener(me -> meetings.switchToMonth(me.getValue()));

        Button today = new Button("today", (Button.ClickEvent clickEvent) -> meetings.getCalendar().withDay(LocalDate.now()));
        Button week = new Button("week", (Button.ClickEvent clickEvent) -> meetings.getCalendar().withWeek(LocalDate.now()));

        HorizontalLayout nav = new HorizontalLayout(localeBox, zoneBox, fixedSize, fullSize, months, today, week, calActionComboBox);
        //nav.setWidth("100%");

        // Show it in the middle of the screen
        final VerticalLayout layout = new VerticalLayout();
        layout.setStyleName("demoContentLayout");
        layout.setSizeFull();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addComponent(nav);
        layout.addComponentsAndExpand(meetings);
        setContent(layout);

        calActionComboBox.setSelectedItem(initial);
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
