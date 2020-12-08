package com.subgraph.orchid.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.subgraph.orchid.TorParsingException;

@SuppressWarnings("unused")
public class Timestamp {
	private final Date date;
	
	public static Timestamp createFromDateAndTimeString(String dateAndTime) {
		final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		format.setLenient(false);
		try {
			return new Timestamp(format.parse(dateAndTime));
		} catch (ParseException e) {
			throw new TorParsingException("Could not parse timestamp string: "+ dateAndTime);
		}
	}
	
	public Timestamp(Date date) {
		this.date = date;
	}
	
	public long getTime() {
		return date.getTime();
	}

	public Date getDate() {
		return new Date(date.getTime());
	}
	
	public boolean hasPassed() {
		final Date now = new Date();
		return date.before(now);
	}
	
	public boolean isBefore(Timestamp ts) {
		return date.before(ts.getDate());
	}
	
	public String toString() {
		return  date.toString();
	}

}
