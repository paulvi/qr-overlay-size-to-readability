package com.emptech.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateTimeUtil {
	protected static Logger logger = LoggerFactory.getLogger(DateTimeUtil.class);
	
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	
	public static String dateToString(Date date) {
		if (date ==null) {
			return "null";
		}
		return sdf.format(date);
	}
	
	public static Date stringToDate(String string) {
		try {
			return sdf.parse(string);
		} catch (ParseException e) {
			logger.error("{}",e);
		}
		return null;
	}

	// https://stackoverflow.com/questions/21448500/how-to-remove-milliseconds-from-date-object-format-in-java
	public static Date truncateToSeconds(Date date){
		if (date==null){
			return null;
		}
		return Date.from(date.toInstant().truncatedTo(ChronoUnit.SECONDS));
	}

	public static void main(String[] args) {
		Date date = new Date();
		logger.info("{} -- {}", date, truncateToSeconds(date));
		logger.info("{} -- {}", dateToString(date), dateToString(truncateToSeconds(date)));
	}

}
