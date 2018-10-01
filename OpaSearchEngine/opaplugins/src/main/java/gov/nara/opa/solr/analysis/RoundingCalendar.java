package gov.nara.opa.solr.analysis;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;


class RoundingCalendar extends GregorianCalendar {

	private String fieldValueTime;
	private Calendar calendar;	
	private TimeZone timeZone;
	private boolean bRoundDown = true;
	private Integer year = null;
	private Integer month = null;
	private Integer day = null;

	public RoundingCalendar(String fieldValueTime, boolean bRoundDown) {
		super();
		this.fieldValueTime = fieldValueTime;
		this.bRoundDown = bRoundDown;
		this.timeZone  = TimeZone.getTimeZone("GMT");
		setYearMonthDay(this.fieldValueTime);
		this.calendar = createCalendar(this.fieldValueTime, this.timeZone, this.bRoundDown);
		int i = 0;
	}

	private void setYearMonthDay(String fvtime) {
		// remove '?' from end, and 'ca. ' from beginning
		String cleanStr = fvtime.replaceAll("^ca\\. ", "");
		cleanStr = cleanStr.replaceAll("\\?$", "");
		String[] parts = cleanStr.split("/");
		if (parts.length == 1) {
			year = Integer.parseInt(parts[0]);
		} else if (parts.length == 2) {
			month = Integer.parseInt(parts[0]);
			year = Integer.parseInt(parts[1]);
		} else if (parts.length == 3) {
			month = Integer.parseInt(parts[0]);
			day = Integer.parseInt(parts[1]);
			year = Integer.parseInt(parts[2]);
//					// extract day part...
//					// 1949-03-04T21:45:00Z  -> 1949, 03, 04T21:45:00Z
//					String[] dayParts = parts[parts.length-3].split("T");
//					day = Integer.parseInt(dayParts[0]);
		}
	}

	private Calendar createCalendar(String fvTime, TimeZone tz, boolean bRoundDown) {
		Calendar cal = null;
		// we need at least year
		if (year != null) {
			cal = Calendar.getInstance(tz);
			cal.set(Calendar.YEAR, year);
			if (month == null) {
				roundTheMonth(cal, bRoundDown);
				roundTheDay(cal, bRoundDown);
			} else {
				cal.set(Calendar.MONTH, month - 1);;
				if (day == null) {
					roundTheDay(cal, bRoundDown);
				} else {
					cal.set(Calendar.DAY_OF_MONTH, day);
				}
			}
			roundTheTime(cal, bRoundDown);
		}
		return cal;
	}

	private static void roundTheTime(Calendar cal, boolean bDown) {
		if (bDown) {
			setBeginOfDay(cal);
		} else {
			setEndOfDay(cal);
		}
	}

	private static void roundTheDay(Calendar cal, boolean bDown) {
		if (bDown) {
			cal.set(Calendar.DAY_OF_MONTH,  cal.getActualMinimum(DAY_OF_MONTH));
		} else {
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		}
	}

	private static void roundTheMonth(Calendar cal, boolean bDown) {
		if (bDown) {
			cal.set(Calendar.MONTH,  cal.getActualMinimum(Calendar.MONTH));
		} else {
			cal.set(Calendar.MONTH, cal.getActualMaximum(Calendar.MONTH));
		}
	}

	String utcString() {
		return String.format("%1$tFT%1tTZ", this.getCalendar());
	}

	public static void setMaxMonth(Calendar cal) {
		cal.set(Calendar.MONTH,  cal.getActualMaximum(Calendar.MONTH));
	}

	public static void setEndOfDay(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE,      cal.getMaximum(Calendar.MINUTE));
		cal.set(Calendar.SECOND,      cal.getMaximum(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
	}
	public static void setBeginOfDay(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE,      cal.getMinimum(Calendar.MINUTE));
		cal.set(Calendar.SECOND,      cal.getMinimum(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
	}

	public Calendar getCalendar() {
		return calendar;
	}

}
