package org.jcvi.jira.importer.utils;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * convert from a date object into the format used by JIRAs XML files
 *
 */
public class DateWriter {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    public static String convertToJIRADate(Date inputDate) {
        return formatter.format(inputDate);
    }
}
