package org.vaadin.addon.calendar.demo.meetings;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.addon.calendar.Calendar;
import org.vaadin.addon.calendar.event.BasicItemProvider;
import org.vaadin.addon.calendar.handler.BasicDateClickHandler;
import org.vaadin.addon.calendar.ui.CalendarComponentEvents;

import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;


public class MeetingCalendar extends CustomComponent {

    private final Random R = new Random(0);

    private MeetingDataProvider eventProvider;

    private Calendar<MeetingItem> calendar;

    public MeetingCalendar() {

        setId("meeting-meetings");
        setSizeFull();

        initCalendar();

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setSpacing(false);
        layout.setSizeFull();
        layout.addComponent(calendar);
        setCompositionRoot(layout);

    }

    public void setWeekDayRange(int from, int to) {
        assert (from >= 1 && from < to && to <= 7);
        calendar.setFirstVisibleDayOfWeek(1); // XXX reset to week
        calendar.setLastVisibleDayOfWeek(7); // XXX reset to week
        calendar.setFirstVisibleDayOfWeek(from);
        calendar.setLastVisibleDayOfWeek(to);
    }

    private void onCalendarRangeSelect(CalendarComponentEvents.RangeSelectEvent event) {

        Meeting meeting = new Meeting();

        meeting.setStart(event.getStart());
        meeting.setEnd(event.getEnd());
        meeting.setName("A Name");
        meeting.setDetails("A Detail");

        // Random state
        meeting.setState(R.nextInt(2) == 1 ? Meeting.State.planned : Meeting.State.confirmed);

        eventProvider.addItem(new MeetingItem(meeting));
	}

    private void onCalendarClick(CalendarComponentEvents.ItemClickEvent event) {

        MeetingItem item = (MeetingItem) event.getCalendarItem();

        final Meeting meeting = item.getMeeting();

        Notification.show(meeting.getName(), meeting.getDetails(), Type.HUMANIZED_MESSAGE);
    }

    private void initCalendar() {

        eventProvider = new MeetingDataProvider();

        calendar = new Calendar<>(eventProvider);

        calendar.addStyleName("meetings");
        calendar.setLocale(Locale.getDefault());
        calendar.setWidth(100.0f, Unit.PERCENTAGE);
        calendar.setHeight(100.0f, Unit.PERCENTAGE);
        calendar.setItemCaptionAsHtml(true);
        calendar.setResponsive(true);

        calendar.setContentMode(ContentMode.HTML);

        calendar.setFirstVisibleDayOfWeek(1);
        calendar.setLastVisibleDayOfWeek(7);

        addCalendarEventListeners();

        setupBlockedTimeSlots();
    }

    private void setupBlockedTimeSlots() {

        java.util.Calendar cal = (java.util.Calendar)calendar.getInternalCalendar().clone();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(java.util.Calendar.MINUTE);
        cal.clear(java.util.Calendar.SECOND);
        cal.clear(java.util.Calendar.MILLISECOND);

        GregorianCalendar bcal = new GregorianCalendar(UI.getCurrent().getLocale());

        bcal.clear();

        long start = bcal.getTimeInMillis();

        bcal.add(java.util.Calendar.HOUR, 7);
        bcal.add(java.util.Calendar.MINUTE, 30);
        long end = bcal.getTimeInMillis();

        calendar.addTimeBlock(start, end, "");

        cal.add(java.util.Calendar.DAY_OF_WEEK, 1);

        bcal.clear();
        bcal.add(java.util.Calendar.HOUR, 14);
        bcal.add(java.util.Calendar.MINUTE, 30);
        start = bcal.getTimeInMillis();

        bcal.add(java.util.Calendar.MINUTE, 60);
        end = bcal.getTimeInMillis();

        calendar.addTimeBlock(start, end);

    }

    private void addCalendarEventListeners() {
//        calendar.setHandler(new ExtendedForwardHandler());
//        calendar.setHandler(new ExtendedBackwardHandler());
//        calendar.setHandler(new ExtendedBasicItemMoveHandler());
//        calendar.setHandler(new ExtendedItemResizeHandler());
        calendar.setHandler(new BasicDateClickHandler(false));
        calendar.setHandler(this::onCalendarClick);
        calendar.setHandler(this::onCalendarRangeSelect);
    }

//    private final class ExtendedBasicItemMoveHandler extends BasicItemMoveHandler {
//
//        @Override
//        public void itemMove(CalendarComponentEvents.ItemMoveEvent event) {
//
//            MeetingItem item = (MeetingItem) event.getCalendarItem();
//            long length = item.getEnd().getTime() - item.getStart().getTime();
//            Date newStart = event.getNewStart();
//            Date newEnd = new Date(newStart.getTime() + length);
//            updateMeeting(item, newStart, newEnd);
//        }
//    }

//    private final class ExtendedItemResizeHandler extends BasicItemResizeHandler {
//
//        @Override
//        public void itemResize(CalendarComponentEvents.ItemResizeEvent event) {
//
//            MeetingItem item = (MeetingItem) event.getCalendarItem();
//            updateMeeting(item, event.getNewStart(), event.getNewEnd());
//        }
//    }

//    private final class ExtendedForwardHandler extends BasicForwardHandler {
//
//        @Override
//        protected void setDates(CalendarComponentEvents.ForwardEvent event, Date start, Date end) {
//
//            /*
//             * TODO Load entities from next week here
//             */
//
//            super.setDates(event, start, end);
//        }
//    }

//    private final class ExtendedBackwardHandler extends BasicBackwardHandler {
//
//        @Override
//        protected void setDates(CalendarComponentEvents.BackwardEvent event, Date start, Date end) {
//
//            /*
//             * TODO Load entities from prev week here
//             */
//
//            super.setDates(event, start, end);
//        }
//    }

    private final class MeetingDataProvider extends BasicItemProvider<MeetingItem> {

        void removeAllEvents() {
            this.itemList.clear();
            fireItemSetChanged();
        }
    }

}

