package gov.nara.opa.architecture.utils;

import gov.nara.opa.architecture.exception.OpaRuntimeException;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @author lvargas
 * @date March 31, 2014
 * 
 */
public class TimestampUtils {
  /**
   * Gets a timestamp string from the current time
   * 
   * @return Timestamp string
   * @deprecated Use getUtcTimestampString instead
   */
  @Deprecated
  public static String getTimestampString() {
    return String.valueOf(getTimestamp());
  }

  /**
   * Gets a timestamp value based on the application server time
   * 
   * @return A timestamp
   */
  public static Timestamp getTimestamp() {
    Date date = new Date();
    return new Timestamp(date.getTime());
  }

  public static Calendar getCurrentTimeInUtc() {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    return calendar;
  }

  public static Calendar toUtcCalendar(Timestamp timestamp) {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.setTime(timestamp);
    return calendar;
  }

  /**
   * Gets a standard format UTC timestamp from the current timestamp
   * 
   * @return
   */
  public static Timestamp getUtcTimestamp() {
    DateTime dt = new DateTime(getTimestamp().getTime());

    return new Timestamp(dt.toDateTime(DateTimeZone.UTC).getMillis());
  }

  /**
   * Gets a standard format UTC time string from the current timestamp
   * 
   * @return
   */
  public static String getUtcTimestampString() {
    return getUtcString(getTimestamp());
  }

  /**
   * Gets a standard format UTC time string
   * 
   * @param timestamp
   * @return
   */
  public static String getUtcString(Timestamp timestamp) {
    DateTime dt = new DateTime(timestamp.getTime());
    return dt.toDateTime(DateTimeZone.UTC).toString();
  }

  /**
   * Check if (ts2-ts1) is within a maxRange # of milliseconds
   * 
   * @param timestamp1
   * @param timestamp2
   * @param differential
   * @return boolean return true if: ts2- ts1 < maxRange
   */
  public static boolean compareTimestampsToDifferential(Timestamp timeStamp1,
      Timestamp timeStamp2, long maxRange) {

    // Get timestamp1 in milliseconds
    long ts1 = timeStamp1.getTime();
    long ts2 = 0;

    // If second compare timestamp is null, use current time
    if (timeStamp2 == null) {
      Date date = new java.util.Date();
      ts2 = date.getTime();
    } else {
      ts2 = timeStamp2.getTime();
    }

    // Return FALSE: if second compare TS minus first compare TS is >
    // differential
    if ((ts2 - ts1) > maxRange) {
      return false;
    }

    return true;
  }

  public static Timestamp toUtcTimestamp(String timestamp) {
    if (!timestamp.endsWith("Z")) {
      timestamp = timestamp + "Z";
    }
    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    sdf1.setTimeZone(TimeZone.getTimeZone("UTC"));
    java.util.Date date = null;
    try {
      date = sdf1.parse(timestamp);
    } catch (ParseException e) {
      throw new OpaRuntimeException(e);
    }
    return new Timestamp(date.getTime());
  }
  
  
  public static Timestamp addDaysToTs(Timestamp in, int days) {
	Calendar cal = Calendar.getInstance();
	cal.setTime(in);
	cal.add(Calendar.DATE, days);
	return new Timestamp(cal.getTimeInMillis());
  }

  
}
