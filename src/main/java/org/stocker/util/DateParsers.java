package org.stocker.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 *
 */
public class DateParsers {
    public static final DateFormat stockDataClientDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
    public static final DateFormat ISODateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
}
