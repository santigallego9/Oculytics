package com.santigallego.oculytics.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.santigallego.oculytics.activities.MainActivity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/*
 * Created by santigallego on 9/8/16.
 */
public class Streaks {

    public Streaks() {}

    public final static DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static int getContactId(Context context, String number) {

        number = PhoneNumbers.formatNumber(number, false);

        SQLiteDatabase db = context.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);
        String selectQuery = "SELECT * FROM contacts WHERE number = \"" + number + "\" LIMIT 1;";
        Cursor cr = db.rawQuery(selectQuery, null);

        int id = -1;

        //boolean checker = false;
        if (cr.moveToFirst()) {
            id = cr.getInt(cr.getColumnIndex("id"));
        }

        cr.close();
        db.close();

        return id;
    }

    public static int getStreak(Context context, int id) {

        SQLiteDatabase db = context.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);
        String selectQuery = "SELECT streak FROM streaks WHERE contact_id = " + id + " LIMIT 1;";
        Cursor cr = db.rawQuery(selectQuery, null);

        int streak = 0;

        //boolean checker = false;
        if (cr.moveToFirst()) {
            streak = cr.getInt(cr.getColumnIndex("streak"));
        }

        cr.close();
        db.close();

        return streak;
    }

    // only clear if streak is NOT 0
    public static void checkForStreakClear(Context context, int id) {

        boolean sent = false, received = false;

        DateTime dayAgo = new DateTime(DateTimeZone.UTC).minusDays(1);

        // Check to see if message has been sent in last 24 hours
        SQLiteDatabase db = context.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);
        String query = "SELECT * FROM contacts WHERE id = " + id + " AND sent_updated_on > \"" + dtfOut.print(dayAgo) + "\";";
        Cursor cr = db.rawQuery(query, null);

        if(cr.moveToFirst()) {
            sent = true;
        }
        cr.close();


        // Check to see if message has been received in last 24 hours
        query = "SELECT * FROM contacts WHERE id = " + id + " AND received_updated_on > \"" + dtfOut.print(dayAgo) + "\";";
        Cursor cs = db.rawQuery(query, null);

        if(cs.moveToFirst()) {
            received = true;
        }
        cs.close();


        // If a message has not been sent or received in last 24 enter
        if(!received || !sent) {

            // make sure it's been 24 hours since streaks was updated
            String checker_query = "SELECT * FROM streaks WHERE contact_id = " + id + " AND streak_updated_on < \"" + dtfOut.print(dayAgo) + "\";";
            Cursor c = db.rawQuery(checker_query, null);

            // if any record exists, its been at least 24 hours since last update
            if (c.moveToFirst()) {
                int streak = c.getInt(c.getColumnIndex("streak"));

                // if streak is greater than 0, clear it to zero
                if (streak > 0) {
                    String update_query = "UPDATE streaks SET streak = 0, streak_updated_on = current_timestamp WHERE contact_id = " + id + ";";
                    db.execSQL(update_query);
                }
            }
            c.close();
        }

        db.close();
    }

    public static void updateStreak(Context context, int id) {

        boolean sent = false, received = false;

        DateTime dayAgo = new DateTime(DateTimeZone.UTC).minusDays(1);

        // Check to see if message has been sent in last 24 hours
        SQLiteDatabase db = context.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        //db.beginTransaction();
        String query = "SELECT * FROM contacts WHERE id = " + id + " AND sent_updated_on > \"" + dtfOut.print(dayAgo) + "\";";
        Cursor cr = db.rawQuery(query, null);

        if(cr.moveToFirst()) {
            sent = true;
        }
        cr.close();


        // Check to see if message has been received in last 24 hours
        query = "SELECT * FROM contacts WHERE id = " + id + " AND received_updated_on > \"" + dtfOut.print(dayAgo) + "\";";
        Cursor cs = db.rawQuery(query, null);

        if(cs.moveToFirst()) {
            received = true;
        }
        cs.close();


        // If a message has been sent and received in last 24 enter
        if(received && sent) {

            String checker_query = "SELECT * FROM streaks WHERE contact_id = " + id + ";";
            Cursor c = db.rawQuery(checker_query, null);

            if (c.moveToFirst()) {

                int streak = c.getInt(c.getColumnIndex("streak"));

                if (streak == 0) {
                    String update_query = "UPDATE streaks SET streak = streak + 1, streak_updated_on = current_timestamp;";
                    db.execSQL(update_query);
                } else {

                    // check to see if streaks has been updated in the last 24 hours
                    checker_query = "SELECT * FROM streaks WHERE contact_id = " + id + " AND streak_updated_on < \"" + dtfOut.print(dayAgo) + "\";";
                    Cursor cp = db.rawQuery(checker_query, null);

                    // if any records, has not been updated, increase by 1, update timestamp
                    if (cp.moveToFirst()) {
                        String update_query = "UPDATE streaks SET streak = streak + 1, streak_updated_on = current_timestamp WHERE contact_id = " + id + ";";
                        db.execSQL(update_query);
                    }
                    cp.close();
                }
            }
            c.close();
        }

        //db.endTransaction();
        db.close();


        // Unsure why I wrote this code like this.... (Why always increase if 0?)

        /*
            if (streak == 0) {
                String update_query = "UPDATE streaks SET streak = streak + 1, streak_updated_on = current_timestamp;";
                db.execSQL(update_query);
            } else {

                checker_query = "SELECT * FROM streaks WHERE contact_id = " + id + " AND streak_updated_on < \"" + dtfOut.print(dayAgo) + "\";";

                Cursor cp = db.rawQuery(checker_query, null);

                if (cp.moveToFirst()) {

                    String update_query = "UPDATE streaks SET streak = streak + 1, streak_updated_on = current_timestamp;";
                    db.execSQL(update_query);
                }
                cp.close();
            }
            c.close();
         */
    }
}
