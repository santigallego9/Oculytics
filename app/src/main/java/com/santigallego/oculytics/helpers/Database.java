package com.santigallego.oculytics.helpers;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.santigallego.oculytics.R;
import com.santigallego.oculytics.activities.MainActivity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.util.Scanner;

/*
 * Created by Santi Gallego on 8/14/16.
 */
public class Database {

    public Database() {}

    public static final String DATABASE_NAME = "oculytics";

    // Create and/or populate the database
    public static void populateDatabase(int id, Activity activity) {

        SQLiteDatabase db = activity.openOrCreateDatabase(DATABASE_NAME, MainActivity.MODE_PRIVATE, null);
        // Populate database if empty
        try {
            Scanner scan = new Scanner(activity.getResources()
                    .openRawResource(id));

            String query = "";
            while (scan.hasNextLine()) { // build and execute queries
                query += scan.nextLine() + "\n";
                if (query.trim().endsWith(";")) {
                    db.execSQL(query);
                    query = "";
                }
            }
        } catch (Exception e) {
            // Log.d("TABLE_EXISTS", e.toString());
        }
        db.close();
    }

    public static void populateDatabase(String filename, Activity activity) {

        SQLiteDatabase db = activity.openOrCreateDatabase(DATABASE_NAME, MainActivity.MODE_PRIVATE, null);
        // Populate database if empty
        try {
            Scanner scan = new Scanner(new File(filename));

            String query = "";
            while (scan.hasNextLine()) { // build and execute queries
                query += scan.nextLine() + "\n";
                if (query.trim().endsWith(";")) {
                    db.execSQL(query);
                    query = "";
                }
            }
        } catch (Exception e) {
            db.close();
            // Log.d("TABLE_EXISTS", e.toString());
        }
    }

    public static void messageReceived(Context context, String number) {

        number = PhoneNumbers.formatNumber(number, false);

        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, MainActivity.MODE_PRIVATE, null);
        String insertQuery, updateQuery, selectQuery;

        // UPDATE totals SET received = received + 1
        updateQuery = "UPDATE totals SET received = received + 1, updated_on = current_timestamp;";
        // Log.d("TEXT_MESSAGE", updateQuery);
        db.execSQL(updateQuery);

        selectQuery = "SELECT * FROM contacts WHERE number = " + number + ";";
        Cursor cr = db.rawQuery(selectQuery, null);

        //boolean checker = false;
        if (cr.moveToFirst()) {
            do {
                int id = cr.getInt(cr.getColumnIndex("id"));
                updateQuery = "UPDATE contacts SET received = received + 1, updated_on = current_timestamp, received_updated_on = current_timestamp WHERE number = " + number + ";";
                insertQuery = "INSERT INTO sms_received (contact_id) VALUES (" + id + ");";
                // Log.d("TEXT_MESSAGE", updateQuery);
                // Log.d("TEXT_MESSAGE", insertQuery);
                db.execSQL(updateQuery);
                db.execSQL(insertQuery);

            } while (cr.moveToNext());
            cr.close();
        } else {
            insertQuery = "INSERT INTO contacts (number, received) VALUES (" + number + ", 1);";
            // Log.d("TEXT_MESSAGE", insertQuery);
            db.execSQL(insertQuery);

            Cursor c = db.rawQuery(selectQuery, null);
            //boolean checker = false;
            if (c.moveToFirst()) {
                do {
                    int id = c.getInt(c.getColumnIndex("id"));
                    insertQuery = "INSERT INTO sms_received (contact_id) VALUES (" + id + ");";
                    // Log.d("TEXT_MESSAGE", insertQuery);
                    db.execSQL(insertQuery);
                    insertQuery = "INSERT INTO streaks (contact_id) VALUES (" + id + ");";
                    db.execSQL(insertQuery);

                } while (c.moveToNext());
                c.close();
            }
        }

        db.close();
    }

    public static void messageSent(Context context, String number) {

        number = PhoneNumbers.formatNumber(number, false);

        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        String insertQuery, updateQuery, selectQuery;

        // UPDATE totals SET received = received + 1
        updateQuery = "UPDATE totals SET sent = sent + 1, updated_on = current_timestamp;";
        // Log.d("TEXT_MESSAGE", updateQuery);
        db.execSQL(updateQuery);

        selectQuery = "SELECT * FROM contacts WHERE number = " + number + ";";
        Cursor cr = db.rawQuery(selectQuery, null);

        //boolean checker = false;
        if (cr.moveToFirst()) {
            do {
                int id = cr.getInt(cr.getColumnIndex("id"));
                updateQuery = "UPDATE contacts SET sent = sent + 1, updated_on = current_timestamp, sent_updated_on = current_timestamp WHERE number = " + number + ";";
                insertQuery = "INSERT INTO sms_sent (contact_id) VALUES (" + id + ");";
                // Log.d("TEXT_MESSAGE", updateQuery);
                // Log.d("TEXT_MESSAGE", insertQuery);
                db.execSQL(updateQuery);
                db.execSQL(insertQuery);

            } while (cr.moveToNext());
            cr.close();
        } else {
            insertQuery = "INSERT INTO contacts (number, sent) VALUES (" + number + ", 1);";
            // Log.d("TEXT_MESSAGE", insertQuery);
            db.execSQL(insertQuery);

            Cursor c = db.rawQuery(selectQuery, null);

            //boolean checker = false;
            if (c.moveToFirst()) {
                do {
                    int id = c.getInt(c.getColumnIndex("id"));
                    insertQuery = "INSERT INTO sms_sent (contact_id) VALUES (" + id + ");";
                    // Log.d("TEXT_MESSAGE", insertQuery);
                    db.execSQL(insertQuery);
                    insertQuery = "INSERT INTO streaks (contact_id) VALUES (" + id + ");";
                    db.execSQL(insertQuery);

                } while (c.moveToNext());
                c.close();
            }
        }
        db.close();
    }

    public static void mmsReceived(Context context, String number) {
        number = PhoneNumbers.formatNumber(number, false);

        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, MainActivity.MODE_PRIVATE, null);
        String insertQuery, updateQuery, selectQuery;

        // UPDATE totals SET received = received + 1
        updateQuery = "UPDATE mms_totals SET received = received + 1, updated_on = current_timestamp;";
        // Log.d("MMS", updateQuery);
        db.execSQL(updateQuery);

        selectQuery = "SELECT * FROM contacts WHERE number = " + number + ";";
        Cursor cr = db.rawQuery(selectQuery, null);

        //boolean checker = false;
        if (cr.moveToFirst()) {
            do {
                int id = cr.getInt(cr.getColumnIndex("id"));
                updateQuery = "UPDATE contacts SET received_mms = received_mms + 1, updated_on = current_timestamp, received_updated_on = current_timestamp WHERE number = " + number + ";";
                insertQuery = "INSERT INTO mms_received (contact_id) VALUES (" + id + ");";
                // Log.d("MMS", updateQuery);
                // Log.d("MMS", insertQuery);
                db.execSQL(updateQuery);
                db.execSQL(insertQuery);

            } while (cr.moveToNext());
            cr.close();
        } else {
            insertQuery = "INSERT INTO contacts (number, received_mms) VALUES (" + number + ", 1);";
            db.execSQL(insertQuery);

            Cursor c = db.rawQuery(selectQuery, null);
            //boolean checker = false;
            if (c.moveToFirst()) {
                do {
                    int id = c.getInt(c.getColumnIndex("id"));
                    insertQuery = "INSERT INTO mms_received (contact_id) VALUES (" + id + ");";
                    // Log.d("MMS", insertQuery);
                    db.execSQL(insertQuery);
                    insertQuery = "INSERT INTO streaks (contact_id) VALUES (" + id + ");";
                    db.execSQL(insertQuery);

                } while (c.moveToNext());
                c.close();
            }
        }
        db.close();
    }

    public static void mmsSent(Context context, String number) {

        number = PhoneNumbers.formatNumber(number, false);

        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        String insertQuery, updateQuery, selectQuery;

        // UPDATE totals SET received = received + 1
        updateQuery = "UPDATE mms_totals SET sent = sent + 1, updated_on = current_timestamp;";

        // Log.d("MMS", updateQuery);
        db.execSQL(updateQuery);

        selectQuery = "SELECT * FROM contacts WHERE number = " + number + ";";

        Cursor cr = db.rawQuery(selectQuery, null);

        //boolean checker = false;
        if (cr.moveToFirst()) {
            do {
                int id = cr.getInt(cr.getColumnIndex("id"));
                updateQuery = "UPDATE contacts SET sent_mms = sent_mms + 1, updated_on = current_timestamp, sent_updated_on = current_timestamp WHERE number = " + number + ";";
                insertQuery = "INSERT INTO mms_sent (contact_id) VALUES (" + id + ");";
                // Log.d("MMS", updateQuery);
                // Log.d("MMS", insertQuery);
                db.execSQL(updateQuery);
                db.execSQL(insertQuery);

            } while (cr.moveToNext());
            cr.close();
        } else {
            insertQuery = "INSERT INTO contacts (number, sent_mms) VALUES (" + number + ", 1);";
            // Log.d("MMS", insertQuery);
            db.execSQL(insertQuery);

            Cursor c = db.rawQuery(selectQuery, null);

            //boolean checker = false;
            if (c.moveToFirst()) {
                do {
                    // Log.d("TEXT_MESSAGE", "EXISTS");
                    int id = c.getInt(c.getColumnIndex("id"));
                    insertQuery = "INSERT INTO mms_sent (contact_id) VALUES (" + id + ");";
                    // Log.d("MMS", insertQuery);
                    db.execSQL(insertQuery);
                    insertQuery = "INSERT INTO streaks (contact_id) VALUES (" + id + ");";
                    db.execSQL(insertQuery);

                } while (c.moveToNext());
                c.close();
            }
        }
        db.close();
    }

    /* -------------------------------------------------------------------------------- */
    /* -------------------------------------------------------------------------------- */
    /* -------------------------------------------------------------------------------- */
    /* ---------                                                             ---------- */
    /* ---------    THESE FUNCTIONS ARE FOR GETTING MESSAGES FROM HISTORY    ---------- */
    /* ---------                                                             ---------- */
    /* -------------------------------------------------------------------------------- */
    /* -------------------------------------------------------------------------------- */
    /* -------------------------------------------------------------------------------- */


    public static void messageReceived(Context context, String number, String date) {

        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, MainActivity.MODE_PRIVATE, null);
        try {

            number = PhoneNumbers.formatNumber(number, false);

            String f_date = "\"" + date + "\"";

            String insertQuery, updateQuery, selectQuery;

            // UPDATE totals SET received = received + 1
            updateQuery = "UPDATE totals SET received = received + 1, updated_on = " + f_date + ";";
            // Log.d("TEXT_MESSAGE", updateQuery);
            db.execSQL(updateQuery);

            selectQuery = "SELECT * FROM contacts WHERE number = " + number + ";";
            Cursor cr = db.rawQuery(selectQuery, null);

            //boolean checker = false;
            if (cr.moveToFirst()) {
                do {
                    int id = cr.getInt(cr.getColumnIndex("id"));
                    updateQuery = "UPDATE contacts SET received = received + 1, updated_on = " + f_date + ", received_updated_on = " + f_date + " WHERE number = " + number + ";";
                    insertQuery = "INSERT INTO sms_received (contact_id, received_on) VALUES (" + id + ", " + f_date + ");";
                    //// Log.d("TEXT_MESSAGE", updateQuery);
                    //// Log.d("TEXT_MESSAGE", insertQuery);
                    db.execSQL(updateQuery);
                    db.execSQL(insertQuery);

                } while (cr.moveToNext());
                cr.close();
            } else {
                insertQuery = "INSERT INTO contacts (number, received, created_on, updated_on, received_updated_on) VALUES (" + number + ", 1, " + f_date + ", " + f_date + ", " + f_date + ");";
                // Log.d("TEXT_MESSAGE", insertQuery);
                db.execSQL(insertQuery);

                Cursor c = db.rawQuery(selectQuery, null);
                //boolean checker = false;"(" + number + ", 1, " + f_date + ", " + f_date + ", " + f_date + ")"
                if (c.moveToFirst()) {
                    do {
                        int id = c.getInt(c.getColumnIndex("id"));
                        insertQuery = "INSERT INTO sms_received (contact_id, received_on) VALUES (" + id + ", " + f_date + ");";
                        // Log.d("TEXT_MESSAGE", insertQuery);
                        db.execSQL(insertQuery);
                        insertQuery = "INSERT INTO streaks (contact_id, streak_updated_on) VALUES (" + id + ", " + f_date + ");";
                        db.execSQL(insertQuery);

                    } while (c.moveToNext());
                    c.close();
                }
                db.close();
            }
        } catch (Exception e) {
            db.close();
            // Log.d("MESSAGE_FAILED", "This message failed to save");
        }
    }

    public static void messageSent(Context context, String number, String date) {

        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, MainActivity.MODE_PRIVATE, null);
        try {

            String f_date = "\"" + date + "\"";

            number = PhoneNumbers.formatNumber(number, false);


            String insertQuery, updateQuery, selectQuery;

            // UPDATE totals SET received = received + 1
            updateQuery = "UPDATE totals SET sent = sent + 1, updated_on = " + f_date + ";";
            // Log.d("TEXT_MESSAGE", updateQuery);
            db.execSQL(updateQuery);

            selectQuery = "SELECT * FROM contacts WHERE number = " + number + ";";
            Cursor cr = db.rawQuery(selectQuery, null);

            //boolean checker = false;
            if (cr.moveToFirst()) {
                do {
                    int id = cr.getInt(cr.getColumnIndex("id"));
                    updateQuery = "UPDATE contacts SET sent = sent + 1, updated_on = " + f_date + ", sent_updated_on = " + f_date + " WHERE number = " + number + ";";
                    insertQuery = "INSERT INTO sms_sent (contact_id, sent_on) VALUES (" + id + ", " + f_date + ");";
                    // Log.d("TEXT_MESSAGE", updateQuery);
                    // Log.d("TEXT_MESSAGE", insertQuery);
                    db.execSQL(updateQuery);
                    db.execSQL(insertQuery);

                } while (cr.moveToNext());
                cr.close();
            } else {
                insertQuery = "INSERT INTO contacts (number, sent, created_on, updated_on, sent_updated_on) VALUES (" + number + ", 1, " + f_date + ", " + f_date + ", " + f_date + ");";
                // Log.d("TEXT_MESSAGE", insertQuery);
                db.execSQL(insertQuery);

                Cursor c = db.rawQuery(selectQuery, null);

                //boolean checker = false;
                if (c.moveToFirst()) {
                    do {
                        int id = c.getInt(c.getColumnIndex("id"));
                        insertQuery = "INSERT INTO sms_sent (contact_id, sent_on) VALUES (" + id + ", " + f_date + ");";

                        // Log.d("TEXT_MESSAGE", insertQuery);
                        db.execSQL(insertQuery);
                        insertQuery = "INSERT INTO streaks (contact_id, streak_updated_on) VALUES (" + id + ", " + f_date + ");";
                        db.execSQL(insertQuery);

                    } while (c.moveToNext());
                    c.close();
                }
            }
        } catch (Exception e) {
            // Log.d("MESSAGE_FAILED", "This message failed to save");
        }

        db.close();
    }
    /*

    public static void mmsReceived(Context context, String number, Boolean original) {

        String f_date = "\"" + date + "\"";

        number = PhoneNumbers.formatNumber(number, false);

        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, MainActivity.MODE_PRIVATE, null);
        String insertQuery, updateQuery, selectQuery;

        // UPDATE totals SET received = received + 1
        updateQuery = "UPDATE mms_totals SET received = received + 1, updated_on = " + f_date + ";";
        // Log.d("MMS", updateQuery);
        db.execSQL(updateQuery);

        selectQuery = "SELECT * FROM contacts WHERE number = " + number + ";";
        Cursor cr = db.rawQuery(selectQuery, null);

        //boolean checker = false;
        if (cr.moveToFirst()) {
            do {
                int id = cr.getInt(cr.getColumnIndex("id"));
                updateQuery = "UPDATE contacts SET received_mms = received_mms + 1, updated_on = " + f_date + ", received_updated_on = " + f_date + " WHERE number = " + number + ";";
                insertQuery = "INSERT INTO mms_received (contact_id, received_on) VALUES (" + id + ", " + f_date + ");";
                // Log.d("MMS", updateQuery);
                // Log.d("MMS", insertQuery);
                db.execSQL(updateQuery);
                db.execSQL(insertQuery);

            } while (cr.moveToNext());
            cr.close();
        } else {
            insertQuery = "INSERT INTO contacts (number, received_mms, created_on, updated_on, received_updated_on) VALUES (" + number + ", 1, " + f_date + ", " + f_date + ", " + f_date + ");";
            db.execSQL(insertQuery);

            Cursor c = db.rawQuery(selectQuery, null);
            //boolean checker = false;
            if (c.moveToFirst()) {
                do {
                    int id = c.getInt(c.getColumnIndex("id"));
                    insertQuery = "INSERT INTO mms_received (contact_id, received_on) VALUES (" + id + ", " + f_date + ");";
                    // Log.d("MMS", insertQuery);
                    db.execSQL(insertQuery);
                    insertQuery = "INSERT INTO streaks (contact_id, streak_updated_on) VALUES (" + id + ", " + f_date + ");";
                    db.execSQL(insertQuery);

                } while (c.moveToNext());
                c.close();
            }
        }
        db.close();
    }

    public static void mmsSent(Context context, String number, String date) {

        String f_date = "\"" + date + "\"";

        number = PhoneNumbers.formatNumber(number, false);

        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        String insertQuery, updateQuery, selectQuery;

        // UPDATE totals SET received = received + 1
        updateQuery = "UPDATE mms_totals SET sent = sent + 1, updated_on = " + f_date + ";";

        // Log.d("MMS", updateQuery);
        db.execSQL(updateQuery);

        selectQuery = "SELECT * FROM contacts WHERE number = " + number + ";";

        Cursor cr = db.rawQuery(selectQuery, null);

        //boolean checker = false;
        if (cr.moveToFirst()) {
            do {
                int id = cr.getInt(cr.getColumnIndex("id"));
                updateQuery = "UPDATE contacts SET sent_mms = sent_mms + 1, updated_on = " + f_date + ", sent_updated_on = " + f_date + " WHERE number = " + number + ";";
                insertQuery = "INSERT INTO mms_sent (contact_id, sent_on) VALUES (" + id + ", " + f_date + ");";
                // Log.d("MMS", updateQuery);
                // Log.d("MMS", insertQuery);
                db.execSQL(updateQuery);
                db.execSQL(insertQuery);

            } while (cr.moveToNext());
            cr.close();
        } else {
            insertQuery = "INSERT INTO contacts (number, sent_mms, created_on, updated_on, sent_updated_on) VALUES (" + number + ", 1, " + f_date + ", " + f_date + ", " + f_date + ");";
            // Log.d("MMS", insertQuery);
            db.execSQL(insertQuery);

            Cursor c = db.rawQuery(selectQuery, null);

            //boolean checker = false;
            if (c.moveToFirst()) {
                do {
                    // Log.d("TEXT_MESSAGE", "EXISTS");
                    int id = c.getInt(c.getColumnIndex("id"));
                    insertQuery = "INSERT INTO mms_sent (contact_id, sent_on) VALUES (" + id + ", " + f_date + ");";
                    // Log.d("MMS", insertQuery);
                    db.execSQL(insertQuery);
                    insertQuery = "INSERT INTO streaks (contact_id, streak_updated_on) VALUES (" + id + ", " + f_date + ");";
                    db.execSQL(insertQuery);

                } while (c.moveToNext());
                c.close();
            }
        }

        db.close();
    }*/


}
