/**Program Assignment #4: Simple GUI Calendar
 * Author: Michelle Luong
 * Copyright (C) 2017 Michelle Luong. All Rights Reserved.
 * Version: 1.01 5/11/2017
 */

import java.io.Serializable;

public class Event implements Serializable {
	private String title;
	private String date;
	public String startTime;
	public String endTime;
	
	public Event(String title, String date, String startTime, String endTime) {
		this.title = title;
		this.date = date;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	public String getDate() {
		return date;
	}
	
	public String toString() {
		if(endTime.equals("")) {
			return startTime + ": " + title;
		}
		return startTime + " - " + endTime + ": " + title;
	}

}
