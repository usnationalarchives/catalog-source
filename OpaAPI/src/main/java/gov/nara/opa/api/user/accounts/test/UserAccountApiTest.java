package gov.nara.opa.api.user.accounts.test;

import gov.nara.opa.api.services.impl.system.ConnectionManagerImpl;
import gov.nara.opa.api.services.system.ConnectionManager;
import gov.nara.opa.architecture.utils.TimestampUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.helpers.ISO8601DateFormat;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.format.datetime.DateFormatter;

public class UserAccountApiTest {
	
	
	
	public static void main(String[] args) {
		//String result = "";
		System.out.println("start");
		
		//UserAccountApiTest testClass = new UserAccountApiTest();
		
	
		Timestamp ts = TimestampUtils.getTimestamp();
		System.out.println(TimestampUtils.getUtcString(ts));
		
		DateTime dt = new DateTime(new Date());
		System.out.println(dt.toString() );
	
		dt = dt.toDateTime(DateTimeZone.UTC);
		System.out.println(dt.toString() );
		
//		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
//		System.out.println(fmt.print(dt));
		
		
		
	}

}
