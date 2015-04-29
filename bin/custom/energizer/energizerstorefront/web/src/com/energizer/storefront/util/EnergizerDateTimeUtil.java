/**
 * 
 */
package com.energizer.storefront.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author M1023097
 * 
 */
public class EnergizerDateTimeUtil
{

	public static String displayDate(final String fetchedDate) throws ParseException
	{

		final SimpleDateFormat DBDateStringfmt = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");

		final DateFormat UIdateFormat = new SimpleDateFormat("MM/dd/yyyy");

		final Date convertedDate = DBDateStringfmt.parse(fetchedDate);

		final String displayDate = UIdateFormat.format(convertedDate);

		return displayDate;
	}

}
