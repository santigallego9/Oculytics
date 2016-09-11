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

        //boolean checker = false;
        if (cr.moveToFirst()) {
            db.close();
            return cr.getInt(cr.getColumnIndex("id"));
        } else {
            db.close();
            return -1;
        }


    }

    public static int getStreak(Context context, int id) {

        SQLiteDatabase db = context.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        String selectQuery = "SELECT streak FROM streaks WHERE contact_id = " + id + " LIMIT 1;";

        Cursor cr = db.rawQuery(selectQuery, null);

        //boolean checker = false;
        if (cr.moveToFirst()) {
            db.close();
            return cr.getInt(cr.getColumnIndex("streak"));
        } else {
            db.close();
            return 0;
        }
    }

    // only clear if streak is NOT 0
    public static void checkForStreakClear(Context context, int id) {

        boolean sent = false, received = false;

        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime current = new DateTime(DateTimeZone.UTC);
        DateTime dayAgo = current.minusDays(1);
        //dtfOut.print(dayago);

        // Log.d("STREAKS_CLEAR", "ENTERED");

        SQLiteDatabase db = context.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        String query = "SELECT * FROM contacts WHERE id = " + id + " AND sent_updated_on > \"" + dtfOut.print(dayAgo) + "\";";

        Cursor cr = db.rawQuery(query, null);

        if(cr.moveToFirst()) {
            // Log.d("STREAKS_CLEAR", "SENT");
            sent = true;
        }
        cr.close();

        query = "SELECT * FROM contacts WHERE id = " + id + " AND received_updated_on > \"" + dtfOut.print(dayAgo) + "\";";

        Cursor cs = db.rawQuery(query, null);

        if(cs.moveToFirst()) {
            // Log.d("STREAKS_CLEAR", "RECEIVED");
            received = true;
        }
        cs.close();

        if(!received && !sent) {
            String checker_query = "SELECT * FROM streaks WHERE contact_id = " + id + " AND streak_updated_on < \"" + dtfOut.print(dayAgo) + "\";";

            Cursor c = db.rawQuery(checker_query, null);

            // Log.d("STREAKS_CLEAR", "ENTERED NON COMMU");

            if (c.moveToFirst()) {
                int streak = c.getInt(c.getColumnIndex("streak"));

                // Log.d("STREAKS_CLEAR", "F " + streak);

                if (streak > 0) {
                    // Log.d("STREAKS_CLEAR", "G");
                    String update_query = "UPDATE streaks SET streak = 0, streak_updated_on = current_timestamp;";
                    db.execSQL(update_query);
                }
            }
            c.close();

            db.close();
        }
    }


       /* if (cr.moveToFirst()) {
            communication = true;
            boolean checker = true;

            Log.d("STREAKS", "A");

            //int id = cr.getInt(cr.getColumnIndex("id"));
            String sent_time = cr.getString(cr.getColumnIndex("sent_updated_on"));
            String received_time = cr.getString(cr.getColumnIndex("received_updated_on"));

            cr.close();



            DateTime sent_on = null, received_on = null;

                if (sent_time != null && !sent_time.isEmpty()) {
                    sent_on = dtf.parseDateTime(sent_time);
                } else {
                    checker = false;
                }
                if (received_time != null && !received_time.isEmpty()) {
                    received_on = dtf.parseDateTime(received_time);
                } else {
                    checker = false;
                }



            boolean no_communication = false;


            if(checker) {
                Log.d("STREAKS", "B");
                Log.d("STREAKS", sent_on.toString());
                Log.d("STREAKS", received_on.toString());
                if (current.minusDays(1).isAfter(sent_on) || current.minusDays(1).isAfter(received_on)) {
                    no_communication = true;
                    Log.d("STREAKS", "C");
                }
            } else {
                no_communication = true;
                Log.d("STREAKS", "D");
            }

            if(no_communication) {
                Log.d("STREAKS", "E");
                String checker_query = "SELECT * FROM streaks WHERE contact_id = " + id + ";";

                Cursor c = db.rawQuery(checker_query, null);

                if (c.moveToFirst()) {
                    Log.d("STREAKS", "F");
                    String streak_updated_on = "" + c.getString(c.getColumnIndex("streak_updated_on"));
                    int streak = c.getInt(c.getColumnIndex("streak"));

                    DateTime streak_updated = dtf.parseDateTime(streak_updated_on);

                    if (streak_updated.isBefore(current.minusDays(1)) && streak > 0) {
                        Log.d("STREAKS", "G");
                        String update_query = "UPDATE streaks SET streak = 0, streak_updated_on = current_timestamp;";
                        db.execSQL(update_query);
                    }
                }
                c.close();
            }
        }
    }*/

    public static void updateStreak(Context context, int id) {
        SQLiteDatabase db = context.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        boolean sent = false, received = false;

        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime current = new DateTime(DateTimeZone.UTC);
        DateTime dayAgo = current.minusDays(1);

        // Log.d("STREAKS_UP", "ENTERED");

        String query = "SELECT * FROM contacts WHERE id = " + id + " AND sent_updated_on > \"" + dtfOut.print(dayAgo) + "\";";

        Cursor cr = db.rawQuery(query, null);

        if(cr.moveToFirst()) {
            // Log.d("STREAKS_UP", "SENT");

            sent = true;
        }
        cr.close();

        query = "SELECT * FROM contacts WHERE id = " + id + " AND received_updated_on > \"" + dtfOut.print(dayAgo) + "\";";

        Cursor cs = db.rawQuery(query, null);

        if(cs.moveToFirst()) {
            // Log.d("STREAKS_UP", "RECEIVED");
            received = true;
        }
        cs.close();

        if(received && sent) {

            String checker_query = "SELECT * FROM streaks WHERE contact_id = " + id + ";";

            // Log.d("STREAKS_UP", "F");
            Cursor c = db.rawQuery(checker_query, null);

            if (c.moveToFirst()) {

                int streak = c.getInt(c.getColumnIndex("streak"));

                // Log.d("STREAKS_UP", "G " + streak);

                if (streak == 0) {
                    String update_query = "UPDATE streaks SET streak = streak + 1, streak_updated_on = current_timestamp;";
                    db.execSQL(update_query);
                } else {

                    checker_query = "SELECT * FROM streaks WHERE contact_id = " + id + " AND streak_updated_on < \"" + dtfOut.print(dayAgo) + "\";";

                    // Log.d("STREAKS_UP", "F");
                    Cursor cp = db.rawQuery(checker_query, null);

                    if (cp.moveToFirst()) {

                        streak = cp.getInt(cp.getColumnIndex("streak"));

                        // Log.d("STREAKS_UP", "G " + streak);

                        String update_query = "UPDATE streaks SET streak = streak + 1, streak_updated_on = current_timestamp;";
                        db.execSQL(update_query);
                    }
                    cp.close();
                }
                c.close();
            }
        }

        //number = PhoneNumbers.formatNumber(number, false);

        /*String query = "SELECT * FROM contacts WHERE id = " + id + ";";

        //String sent_time = "", received_time = "";

        Cursor cr = db.rawQuery(query, null);

        if (cr.moveToFirst()) {

            Log.d("STREAKS_UP", "A");
            boolean streak = true;

            //int id = cr.getInt(cr.getColumnIndex("id"));
            String sent_time = cr.getString(cr.getColumnIndex("sent_updated_on"));
            String received_time = cr.getString(cr.getColumnIndex("received_updated_on"));

            cr.close();

            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

            DateTime sent_on = null, received_on = null;

            if(sent_time != null && !sent_time.isEmpty()) {
                sent_on = dtf.parseDateTime(sent_time);
            } else {
                streak = false;
            }
            if(received_time != null && !received_time.isEmpty()) {
                received_on = dtf.parseDateTime(received_time);
            } else {
                streak = false;
            }

            DateTime current = new DateTime(DateTimeZone.UTC);


            if(streak) {
                Log.d("STREAKS_UP", "B");
                // makes sure there has been communication in last 24 hours and that streak has not been updated in 24 hours
                if(current.minusDays(1).isBefore(sent_on) && current.minusDays(1).isBefore(received_on)) {
                    // check to see if streak has already been updated
                    //String query = "SELECT * FROM contacts WHERE contact_id = " + id + " ORDER BY '" + column_name + "' DESC LIMIT 1";

                    Log.d("STREAKS_UP", "C");

                    String checker_query = "SELECT * FROM streaks WHERE contact_id = " + id + ";";

                    Cursor c = db.rawQuery(checker_query, null);

                    if (c.moveToFirst()) {
                        Log.d("STREAKS_UP", "D");

                        String streak_updated_on = "" + c.getString(c.getColumnIndex("streak_updated_on"));

                        DateTime streak_updated = dtf.parseDateTime(streak_updated_on);

                        if(streak_updated.isBefore(current.minusDays(1))) {
                            String update_query = "UPDATE streaks SET streak = streak + 1, streak_updated_on = current_timestamp;";
                            Log.d("STREAKS_UP", "E");

                            db.execSQL(update_query);
                        }
                        // else streak has already been updated
                    }
                    c.close();
                }
            }
        }*/

        db.close();
    }
}
