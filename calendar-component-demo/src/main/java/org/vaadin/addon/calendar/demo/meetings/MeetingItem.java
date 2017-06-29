package org.vaadin.addon.calendar.demo.meetings;

import org.vaadin.addon.calendar.server.ui.event.BasicEvent;

import java.util.Date;

/**
 * Meeting Pojo
 */

public class MeetingItem extends BasicEvent {

	private final Meeting meeting;

	/**
	 * constructor
	 *
	 * @param meeting A meeting
	 */

	public MeetingItem(Meeting meeting) {
        super(meeting.getDetails(), meeting.getName(), meeting.getStart(), meeting.getEnd());
        this.meeting = meeting;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof MeetingItem)) {
			return false;
		}
		MeetingItem that = (MeetingItem) o;
		return getMeeting().equals(that.getMeeting());
	}

	public Meeting getMeeting() {
		return meeting;
	}

	@Override
	public String getStyleName() {
		return "state-" + meeting.getState().name().toLowerCase();
	}

	@Override
	public int hashCode() {
		return getMeeting().hashCode();
	}

	@Override
	public boolean isAllDay() {
		return false;
	}

	@Override
	public void setEnd(Date end) {
		meeting.setEnd(end);
		super.setEnd(end);
	}

	@Override
	public void setStart(Date start) {
		meeting.setStart(start);
		super.setStart(start);
	}
}