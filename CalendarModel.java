import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CalendarModel implements Serializable {
	
	private int maxDays;
	private int selectedDate;
	private HashMap<String, ArrayList<Event>> mapOfEvent;
	private ArrayList<ChangeListener> listeners;
	private GregorianCalendar c = new GregorianCalendar();
	private boolean monthChanged = false;
	
	/**
	 * Constructor for the CalendarModel
	 */
	public CalendarModel() {
		maxDays = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		selectedDate = c.get(Calendar.DATE);
		mapOfEvent = new HashMap<>();
		listeners = new ArrayList<ChangeListener>();
		load();
	}
	
	/**
	 * Attaches the ChangeListener to the array
	 * @param l ChangeListener
	 */
	public void attach(ChangeListener l) {
		listeners.add(l);
	}
	
	/**
	 * Updates the ChangeListeners in the array
	 */
	public void update() {
		for(ChangeListener l : listeners) {
			l.stateChanged(new ChangeEvent(this));
		}
	}
	
	/**
	 * Sets the selected date
	 * @param day
	 */
	public void setSelectedDate(int day) {
		selectedDate = day;
	}
	
	/**
	 * Gets the selected date
	 * @return selectedDate
	 */
	public int getSelectedDate() {
		return selectedDate;
	}
	
	/**
	 * Gets the current year
	 * @return current year
	 */
	public int getYear() {
		return c.get(Calendar.YEAR);
	}
	
	/**
	 * Gets the current month
	 * @return current month
	 */
	public int getMonth() {
		return c.get(Calendar.MONTH);
	}
	
	/**
	 * Gets the current value that represents the day of the week
	 * @param i Day of the month
	 * @return Day of the week (1-7)
	 */
	public int getDayOfWeek(int i) {
		c.set(Calendar.DAY_OF_MONTH, i);
		return c.get(Calendar.DAY_OF_WEEK);
	}
	
	/**
	 * Gets the maximum number of days in the month
	 * @return maxDays
	 */
	public int getMaxDays() {
		return maxDays;
	}
	
	/**
	 * Calendar goes forward by one month
	 */
	public void goNextMonth() {
		c.add(Calendar.MONTH, 1);
		maxDays = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		monthChanged = true;
		update();
	}
	
	/**
	 * Calendar goes backwards by one month
	 */
	public void goPreviousMonth() {
		c.add(Calendar.MONTH, -1);
		maxDays = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		monthChanged = true;
		update();
	}
	
	/**
	 * Calendar goes to the next day
	 */
	public void goNextDay() {
		selectedDate++;
		if(selectedDate > c.getActualMaximum(Calendar.DAY_OF_MONTH)) {
			goNextMonth();
			selectedDate = 1;
		}
		update();
	}
	
	/**
	 * Calendar goes to the previous day
	 */
	public void goPreviousDay() {
		selectedDate--;
		if(selectedDate < 1) {
			goPreviousMonth();
			selectedDate = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		}
		update();
	}
	
	/**
	 * This will check to see if the month has changed 
	 * In other words, if the user changed to another month
	 * @return True if month has changed; False if not
	 */
	public boolean hasMonthChanged() {
		return monthChanged;
	}
	
	/**
	 * This will reset the monthChanged to false
	 */
	public void resetMonthChanged() {
		monthChanged = false;
	}
	
	/**
	 * This will check to see if the selected date has any events scheduled
	 * @param date Selected date in the format of MM/DD/YYYY
	 * @return True if the date has an event scheduled
	 */
	public boolean hasEvent(String date) {
		return mapOfEvent.containsKey(date);
	}
	
	/**
	 * Creating an event, with the title, date, start and end time
	 * Create an array list of events
	 * If there is an event on the specified date, the hash map will get that date
	 * The event will be added to the array list
	 * 
	 * @param title Title of the event
	 * @param startTime Starting time of the event
	 * @param endTime Ending time of the event
	 */
	public void createEvent(String title, String startTime, String endTime) {
		String date = (c.get(Calendar.MONTH) + 1) + "/" + selectedDate + "/" + c.get(Calendar.YEAR);
		Event e = new Event(title, date, startTime, endTime);
		ArrayList<Event> listOfEvent = new ArrayList<>();
		if(hasEvent(date)) {
			listOfEvent = mapOfEvent.get(date);
		}
		listOfEvent.add(e);
		mapOfEvent.put(date, listOfEvent);		
	}
	
	/**
	 * Converts the 24:00 time to minutes
	 * @param time Time in the 24:00 format
	 * @return time converted to minutes
	 */
	public int convertHourToMin(String time) {
		int hours = Integer.valueOf(time.substring(0, 2));
		return hours * 60 + Integer.valueOf(time.substring(3));
	}
	
	/**
	 * Comparator to compare the times
	 * @return comparator
	 */
	public static Comparator<Event> timeComparator() {
		return new Comparator<Event>() {
			public int compare(Event event1, Event event2) {
				if(event1.startTime.substring(0, 2).equals(event2.startTime.substring(0, 2))) {
					return Integer.parseInt(event1.startTime.substring(3, 5)) - Integer.parseInt(event2.startTime.substring(3, 5));
				}
				return Integer.parseInt(event1.startTime.substring(0, 2)) - Integer.parseInt(event2.startTime.substring(0,2));
			}
		};
	}
	
	/**
	 * This will check to see if there are any event time conflicts; Uses comparator
	 * @param timeStart Starting time of the event
	 * @param timeEnd Ending time of the event
	 * @return
	 */
	public Boolean hasEventConflict(String timeStart, String timeEnd) {
		String date = (getMonth() + 1) + "/" + selectedDate + "/" + getYear();
		if (!hasEvent(date)) {
			return false;
		}
		
		ArrayList<Event> eventArray = mapOfEvent.get(date);
		Collections.sort(eventArray, timeComparator());
		
		int timeStartMins = convertHourToMin(timeStart), timeEndMins = convertHourToMin(timeEnd);
		for (Event e : eventArray) {
			int eventStartTime = convertHourToMin(e.startTime), eventEndTime = convertHourToMin(e.endTime);
			if (timeStartMins >= eventStartTime && timeStartMins < eventEndTime) {
				return true;
			} else if (timeStartMins <= eventStartTime && timeEndMins > eventStartTime) {
				return true;
			}
		}
		return false;
	}	
	
	/**
	 * String representation of all the events scheduled on the selected date
	 * @param date Date to get all the events
	 * @return String representation of all events scheduled on the date
	 */
	public String getEvents(String date) {
		ArrayList<Event> listOfEvent = mapOfEvent.get(date);
		Collections.sort(listOfEvent, timeComparator());
		String events = "";
		int i = 1;
		for(Event e : listOfEvent) {
			events += i + "   " + e.toString() + "\n";
			i++;
		}
		return events;
	}
	
	/**
	 * Saves all events to "events.txt".
	 */
	public void save() {
		if (mapOfEvent.isEmpty()) {
			return;
		}
		try {
			FileOutputStream fOut = new FileOutputStream("events.txt");
			ObjectOutputStream oOut = new ObjectOutputStream(fOut);
			oOut.writeObject(mapOfEvent);
			oOut.close();
			fOut.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Loads all events from "events.ser".
	 */
	private void load() {
		try {
			FileInputStream fIn = new FileInputStream("events.txt");
			ObjectInputStream oIn = new ObjectInputStream(fIn);
			HashMap<String, ArrayList<Event>> temp = (HashMap<String, ArrayList<Event>>) oIn.readObject();
			for (String date : temp.keySet()) {
				if (hasEvent(date)) {
					ArrayList<Event> eventArray = mapOfEvent.get(date);
					eventArray.addAll(temp.get(date));
				} else {
					mapOfEvent.put(date, temp.get(date));
				}
			}
			oIn.close();
			fIn.close();
		} catch (IOException ioe) {
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
		}
	}
	
	/**
	 * Deletes an event from the array
	 */
	public void deleteEvent() {
		String date = (c.get(Calendar.MONTH) + 1) + "/" + selectedDate + "/" + c.get(Calendar.YEAR);
		ArrayList<Event> listOfEvent = mapOfEvent.get(date);
		listOfEvent.clear();
		update();
	}
	
	/**
	 * Deletes an specific event from the array
	 */
	public void deleteSpecificEvent(String eventNum) {
		String date = (c.get(Calendar.MONTH) + 1) + "/" + selectedDate + "/" + c.get(Calendar.YEAR);
		ArrayList<Event> listOfEvent = mapOfEvent.get(date);
		listOfEvent.remove(Integer.parseInt(eventNum));
		update();
	}

}

