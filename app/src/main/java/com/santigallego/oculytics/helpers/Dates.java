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

    final static DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static String timeAgo (int yearsAgo, int monthsAgo, int daysAgo, int hoursAgo, int minutesAgo, int secondsAgo) {

        String date = dtfOut.print(new DateTime(DateTimeZone.UTC));

        int years = Integer.parseInt(date.substring(0, 4));
        int months = Integer.parseInt(date.substring(5, 7));
        int days = Integer.parseInt(date.substring(8, 10));
        int hours = Integer.parseInt(date.substring(11, 13));
        int minutes = Integer.parseInt(date.substring(14, 16));
        int seconds = Integer.parseInt(date.substring(17));

        int new_years = years - yearsAgo;
        int new_months = months - monthsAgo;
        int new_days = days - daysAgo;
        int new_hours = hours - hoursAgo;
        int new_minutes = minutes - minutesAgo;
        int new_seconds = seconds - secondsAgo;

        if (new_years < 0) { new_years = 0; }
        if (new_months < 0) { new_months = 0; }
        if (new_days < 0) { new_days = 0; }
        if (new_hours < 0) { new_hours = 0; }
        if (new_minutes < 0) { new_minutes = 0; }
        if (new_seconds < 0) { new_seconds = 0; }

        String s_years = new_years + "";
        String s_months = new_months + "";
        String s_days = new_days + "";
        String s_hours = new_hours + "";
        String s_minutes = new_minutes + "";
        String s_seconds = new_seconds + "";

        if(s_years.length() < 4) {
            while (s_years.length() < 4) {
                s_years = "0" + s_years;
            }
        }

        if(s_months.length() < 2) { s_months = "0" + s_months; }
        if(s_days.length() < 2) { s_days = "0" + s_days; }
        if(s_hours.length() < 2) { s_hours = "0" + s_hours; }
        if(s_minutes.length() < 2) { s_minutes = "0" + s_minutes; }
        if(s_seconds.length() < 2) { s_seconds = "0" + s_seconds; }

        String datetime = s_years + "-" + s_months + "-" + s_days + " " + s_hours + ":" + s_minutes + ":" + s_seconds;

        Log.d("DATETIME", datetime);

        return datetime;
    }

    public static String timeBefore (int yearsAgo, int monthsAgo, int daysAgo, int hoursAgo, int minutesAgo, int secondsAgo, String date) {

        int years = Integer.parseInt(date.substring(0, 4));
        int months = Integer.parseInt(date.substring(5, 7));
        int days = Integer.parseInt(date.substring(8, 10));
        int hours = Integer.parseInt(date.substring(11, 13));
        int minutes = Integer.parseInt(date.substring(14, 16));
        int seconds = Integer.parseInt(date.substring(17));

        int new_years = years - yearsAgo;
        int new_months = months - monthsAgo;
        int new_days = days - daysAgo;
        int new_hours = hours - hoursAgo;
        int new_minutes = minutes - minutesAgo;
        int new_seconds = seconds - secondsAgo;

        if (new_years < 0) { new_years = 0; }
        if (new_months < 0) { new_months = 0; }
        if (new_days < 0) { new_days = 0; }
        if (new_hours < 0) { new_hours = 0; }
        if (new_minutes < 0) { new_minutes = 0; }
        if (new_seconds < 0) { new_seconds = 0; }

        String s_years = new_years + "";
        String s_months = new_months + "";
        String s_days = new_days + "";
        String s_hours = new_hours + "";
        String s_minutes = new_minutes + "";
        String s_seconds = new_seconds + "";

        if(s_years.length() < 4) {
            while (s_years.length() < 4) {
                s_years = "0" + s_years;
            }
        }

        if(s_months.length() < 2) { s_months = "0" + s_months; }
        if(s_days.length() < 2) { s_days = "0" + s_days; }
        if(s_hours.length() < 2) { s_hours = "0" + s_hours; }
        if(s_minutes.length() < 2) { s_minutes = "0" + s_minutes; }
        if(s_seconds.length() < 2) { s_seconds = "0" + s_seconds; }

        String datetime = s_years + "-" + s_months + "-" + s_days + " " + s_hours + ":" + s_minutes + ":" + s_seconds;

        Log.d("DATETIME", datetime);

        return datetime;
    }

    public static String fromUtcToLocal(String date) {

        String local = dtfOut.print(new LocalDateTime());
        String utc = dtfOut.print(new DateTime(DateTimeZone.UTC));

        int l_hours = Integer.parseInt(local.substring(11, 13));
        int u_hours = Integer.parseInt(utc.substring(11, 13));
        int d_hours = Integer.parseInt(date.substring(11, 13));
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


}
