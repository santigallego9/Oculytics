package com.santigallego.oculytics.helpers;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/*
 * Created by Santi Gallego on 8/16/16.
 */
public class Dates {

    //  yyyy-mm-dd hh:mm:ss   patter
    //  2016-08-16 20:00:00   example
    //  0123456789012345678   ones
    //  0000000000111111111   tens

    public final static DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static String timeAgo (int yearsAgo, int monthsAgo, int weeksAgo, int daysAgo, int hoursAgo, int minutesAgo, int secondsAgo) {

        String date = dtfOut.print(new DateTime(DateTimeZone.UTC));

        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        DateTime time = dtf.parseDateTime(date);

        time = time.minusSeconds(secondsAgo);
        time = time.minusMinutes(minutesAgo);
        time = time.minusHours(hoursAgo);
        time = time.minusDays(daysAgo);
        time = time.minusWeeks(weeksAgo);
        time = time.minusMonths(monthsAgo);
        time = time.minusYears(yearsAgo);

        String datetime = dtfOut.print(time);

        return datetime;
    }

    public static String timeBefore (int yearsAgo, int monthsAgo, int weeksAgo, int daysAgo, int hoursAgo, int minutesAgo, int secondsAgo, String date) {

        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        DateTime time = dtf.parseDateTime(date);

        time = time.minusSeconds(secondsAgo);
        time = time.minusMinutes(minutesAgo);
        time = time.minusHours(hoursAgo);
        time = time.minusDays(daysAgo);
        time = time.minusWeeks(weeksAgo);
        time = time.minusMonths(monthsAgo);
        time = time.minusYears(yearsAgo);

        String datetime = dtfOut.print(time);

        return datetime;
    }

    public static String toDisplay(String date) {

        DateTimeFormatter dtf = DateTimeFormat.forPattern("MMM dd");

        DateTime time = dtfOut.parseDateTime(date);

        date = dtf.print(time);

        return date;
    }

    public static String fromUtcToLocal(String date) {

        String local = dtfOut.print(new LocalDateTime());
        String utc = dtfOut.print(new DateTime(DateTimeZone.UTC));

        int l_hours = Integer.parseInt(local.substring(11, 13));
        int u_hours = Integer.parseInt(utc.substring(11, 13));
        int d_hours = Integer.parseInt(date.substring(8, 10));
        int d_days = Integer.parseInt(date.substring(8, 10));

        int diff = l_hours - u_hours;
        d_hours += diff;

        if(d_hours < 0) {
            d_days--;
            d_hours += 24;
        } else if(d_hours > 24) {
            d_days++;
            d_hours -= 24;
        }

        String s_days = d_days + "";
        String s_hours = d_hours + "";

        if(s_days.length() < 2) { s_days = "0" + s_days; }
        if(s_hours.length() < 2) { s_hours = "0" + s_hours; }

        date = date.substring(0, 8) + s_days + " " + s_hours + date.substring(13);

        return date;
    }


    public static String formatToMidnight(String date) {

        date = date.substring(0, 11) + "00:00:00";

        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        DateTime time = dtf.parseDateTime(date);

        time = time.plusDays(1);

        date = dtfOut.print(time);

        return date;
    }
}
