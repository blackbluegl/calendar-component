package org.vaadin.addon.calendar.demo.meetings;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.addon.calendar.Calendar;
import org.vaadin.addon.calendar.event.BasicEventProvider;
import org.vaadin.addon.calendar.handler.BasicBackwardHandler;
import org.vaadin.addon.calendar.handler.BasicEventMoveHandler;
import org.vaadin.addon.calendar.handler.BasicEventResizeHandler;
import org.vaadin.addon.calendar.handler.BasicForwardHandler;
import org.vaadin.addon.calendar.ui.CalendarComponentEvents;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;


public class MeetingCalendar extends CustomComponent {

    private MeetingDataProvider eventProvider;

    private Calendar calendar;

    public MeetingCalendar() {

        setId("meeting-meetings");
        setSizeFull();

        initCalendar();

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.addComponent(calendar);
        setCompositionRoot(layout);

    }

    public void setMeetings(Collection<Meeting> meetings) {

        // cleanup
        eventProvider.removeAllEvents();
        if (meetings == null || meetings.size() == 0) {
            return;
        }

        // erstelle Kalendereinträge neu
        for (Meeting boo : meetings) {
            MeetingItem i = new MeetingItem(boo);
            eventProvider.addEvent(i);
        }
    }

    private void onCalendarRangeSelect(CalendarComponentEvents.RangeSelectEvent event) {

        Meeting meeting = new Meeting();

        meeting.setStart(event.getStart());
        meeting.setEnd(event.getEnd());
        meeting.setName("A Name");
        meeting.setDetails("A Detail");

        eventProvider.addEvent(new MeetingItem(meeting));
	}

    private void onCalendarClick(CalendarComponentEvents.EventClick event) {

        MeetingItem item = (MeetingItem) event.getCalendarEvent();

        final Meeting meeting = item.getMeeting();

        Notification.show(meeting.getName(), meeting.getDetails(), Type.HUMANIZED_MESSAGE);
    }

	private void updateMeeting(MeetingItem item, Date start, Date end) {
		item.setStart(start);
		item.setEnd(end);
	}

    private void initCalendar() {

        eventProvider = new MeetingDataProvider();

        calendar = new Calendar(eventProvider);

        calendar.addStyleName("noselect");
        calendar.setLocale(Locale.getDefault());
        calendar.setWidth(100.0f, Unit.PERCENTAGE);
        calendar.setHeight(100.0f, Unit.PERCENTAGE);
        calendar.setEventCaptionAsHtml(true);
        calendar.setResponsive(true);

        calendar.setFirstVisibleDayOfWeek(1);
        calendar.setLastVisibleDayOfWeek(7);

        addCalendarEventListeners();
    }


    private void addCalendarEventListeners() {
        calendar.setHandler(new ExtendedForwardHandler());
        calendar.setHandler(new ExtendedBackwardHandler());
        calendar.setHandler(new ExtendedBasicEventMoveHandler());
        calendar.setHandler(new ExtendedEventResizeHandler());
        calendar.setHandler(this::onCalendarClick);
        calendar.setHandler(this::onCalendarRangeSelect);
    }

    private final class ExtendedBasicEventMoveHandler extends BasicEventMoveHandler {

        @Override
        public void eventMove(CalendarComponentEvents.MoveEvent event) {

            MeetingItem item = (MeetingItem) event.getCalendarEvent();

            Meeting meeting = item.getMeeting();

            long length = item.getEnd().getTime() - item.getStart().getTime();

            Date newStart = event.getNewStart();

            Date newEnd = new Date(newStart.getTime() + length);

            if (meeting.isEditable()) {
                // TODO remove
                updateMeeting(item, newStart, newEnd);
            } else {
                updateMeeting(item, meeting.getStart(), meeting.getEnd());
            }
        }
    }

    private final class ExtendedEventResizeHandler extends BasicEventResizeHandler {

        @Override
        public void eventResize(CalendarComponentEvents.EventResize event) {


            MeetingItem item = (MeetingItem) event.getCalendarEvent();
            Meeting meeting = item.getMeeting();

            if (meeting.isEditable()) {
                // TODO remove
                updateMeeting(item, event.getNewStart(), event.getNewEnd());
            }
            else {
                updateMeeting(item, meeting.getStart(), meeting.getEnd());
            }
        }
    }

    private final class ExtendedForwardHandler extends BasicForwardHandler {

        @Override
        protected void setDates(CalendarComponentEvents.ForwardEvent event, Date start, Date end) {

            /*
             * TODO Load entities from next week here
             */

            super.setDates(event, start, end);
        }
    }

    private final class ExtendedBackwardHandler extends BasicBackwardHandler {

        @Override
        protected void setDates(CalendarComponentEvents.BackwardEvent event, Date start, Date end) {

            /*
             * TODO Load entities from prev week here
             */

            super.setDates(event, start, end);
        }
    }

    private final class MeetingDataProvider extends BasicEventProvider {

        void removeAllEvents() {
            this.eventList.clear();
            fireEventSetChange();
        }
    }

}

