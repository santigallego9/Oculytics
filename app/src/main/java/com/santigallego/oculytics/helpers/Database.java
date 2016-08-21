package com.santigallego.oculytics.helpers;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.santigallego.oculytics.R;
import com.santigallego.oculytics.activites.MainActivity;

import java.util.Scanner;

/*
 * Created by Santi Gallego on 8/14/16.
 */
public class Database {

    public Database() {}

    public static final String DATABASE_NAME = "oculytics";


    // Create and/or populate the database
    public static void populateDatabase(Activity activity) {
        // Populate database if empty
        try {
            SQLiteDatabase db = activity.openOrCreateDatabase(DATABASE_NAME, MainActivity.MODE_PRIVATE, null);
            Scanner scan = new Scanner(activity.getResources()
                    .openRawResource(R.raw.seed));

            String query = "";
            while (scan.hasNextLine()) { // build and execute queries
                query += scan.nextLine() + "\n";
                if (query.trim().endsWith(";")) {
                    db.execSQL(query);
                    query = "";
                }
            }
        } catch (Exception e) {
            Log.d("TABLE_EXISTS", e.toString());
        }
    }

    public static void messageReceived(Context context, String number) {

        number = PhoneNumbers.formatNumber(number, false);

        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, MainActivity.MODE_PRIVATE, null);
        String insertQuery, updateQuery, selectQuery;

        // UPDATE totals SET received = received + 1
        updateQuery = "UPDATE totals SET received = received + 1, updated_on = current_timestamp;";
        Log.d("TEXT_MESSAGE", updateQuery);
        db.execSQL(updateQuery);

        selectQuery = "SELECT * FROM contacts WHERE number = " + number + ";";
        Cursor cr = db.rawQuery(selectQuery, null);

        //boolean checker = false;
        if (cr.moveToFirst()) {
            do {
                int id = cr.getInt(cr.getColumnIndex("id"));
                updateQuery = "UPDATE contacts SET received = received + 1, updated_on = current_timestamp WHERE number = " + number + ";";
                insertQuery = "INSERT INTO sms_received (contact_id) VALUES (" + id + ");";
                Log.d("TEXT_MESSAGE", updateQuery);
                Log.d("TEXT_MESSAGE", insertQuery);
                db.execSQL(updateQuery);
                db.execSQL(insertQuery);

            } while (cr.moveToNext());
            cr.close();
        } else {
            insertQuery = "INSERT INTO contacts (number, received) VALUES (" + number + ", 1);";
            Log.d("TEXT_MESSAGE", insertQuery);
            db.execSQL(insertQuery);

            Cursor c = db.rawQuery(selectQuery, null);
            //boolean checker = false;
            if (c.moveToFirst()) {
                do {
                    int id = c.getInt(c.getColumnIndex("id"));
                    insertQuery = "INSERT INTO sms_received (contact_id) VALUES (" + id + ");";
                    Log.d("TEXT_MESSAGE", insertQuery);
                    db.execSQL(insertQuery);

                } while (c.moveToNext());
                c.close();
            }
        }
    }

    public static void messageSent(Context context, String number) {

        number = PhoneNumbers.formatNumber(number, false);

        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        String insertQuery, updateQuery, selectQuery;

        // UPDATE totals SET received = received + 1
        updateQuery = "UPDATE totals SET sent = sent + 1, updated_on = current_timestamp;";
        Log.d("TEXT_MESSAGE", updateQuery);
        db.execSQL(updateQuery);

        selectQuery = "SELECT * FROM contacts WHERE number = " + number + ";";
        Cursor cr = db.rawQuery(selectQuery, null);

        //boolean checker = false;
        if (cr.moveToFirst()) {
            do {
                int id = cr.getInt(cr.getColumnIndex("id"));
                updateQuery = "UPDATE contacts SET sent = sent + 1, updated_on = current_timestamp WHERE number = " + number + ";";
                insertQuery = "INSERT INTO sms_sent (contact_id) VALUES (" + id + ");";
                Log.d("TEXT_MESSAGE", updateQuery);
                Log.d("TEXT_MESSAGE", insertQuery);
                db.execSQL(updateQuery);
                db.execSQL(insertQuery);

            } while (cr.moveToNext());
            cr.close();
        } else {
            insertQuery = "INSERT INTO contacts (number, sent) VALUES (" + number + ", 1);";
            Log.d("TEXT_MESSAGE", insertQuery);
            db.execSQL(insertQuery);

            Cursor c = db.rawQuery(selectQuery, null);

            //boolean checker = false;
            if (c.moveToFirst()) {
                do {
                    int id = c.getInt(c.getColumnIndex("id"));
                    insertQuery = "INSERT INTO sms_sent (contact_id) VALUES (" + id + ");";
                    Log.d("TEXT_MESSAGE", insertQuery);
                    db.execSQL(insertQuery);

                } while (c.moveToNext());
                c.close();
            }
        }
    }

    public static void mmsReceived(Context context, String number) {
        number = PhoneNumbers.formatNumber(number, false);

        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, MainActivity.MODE_PRIVATE, null);
        String insertQuery, updateQuery, selectQuery;

        // UPDATE totals SET received = received + 1
        updateQuery = "UPDATE mms_totals SET received = received + 1, updated_on = current_timestamp;";
        Log.d("MMS", updateQuery);
        db.execSQL(updateQuery);

        selectQuery = "SELECT * FROM contacts WHERE number = " + number + ";";
        Cursor cr = db.rawQuery(selectQuery, null);

        //boolean checker = false;
        if (cr.moveToFirst()) {
            do {
                int id = cr.getInt(cr.getColumnIndex("id"));
                updateQuery = "UPDATE contacts SET received_mms = received_mms + 1, updated_on = current_timestamp WHERE number = " + number + ";";
                insertQuery = "INSERT INTO mms_received (contact_id) VALUES (" + id + ");";
                Log.d("MMS", updateQuery);
                Log.d("MMS", insertQuery);
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
                    Log.d("MMS", insertQuery);
                    db.execSQL(insertQuery);

                } while (c.moveToNext());
                c.close();
            }
        }
    }

    public static void mmsSent(Context context, String number) {

        number = PhoneNumbers.formatNumber(number, false);

        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        String insertQuery, updateQuery, selectQuery;

        // UPDATE totals SET received = received + 1
        updateQuery = "UPDATE mms_totals SET sent = sent + 1, updated_on = current_timestamp;";

        Log.d("MMS", updateQuery);
        db.execSQL(updateQuery);

        selectQuery = "SELECT * FROM contacts WHERE number = " + number + ";";

        Cursor cr = db.rawQuery(selectQuery, null);

        //boolean checker = false;
        if (cr.moveToFirst()) {
            do {
                int id = cr.getInt(cr.getColumnIndex("id"));
                updateQuery = "UPDATE contacts SET sent_mms = sent_mms + 1, updated_on = current_timestamp WHERE number = " + number + ";";
                insertQuery = "INSERT INTO mms_sent (contact_id) VALUES (" + id + ");";
                Log.d("MMS", updateQuery);
                Log.d("MMS", insertQuery);
                db.execSQL(updateQuery);
                db.execSQL(insertQuery);

            } while (cr.moveToNext());
            cr.close();
        } else {
            insertQuery = "INSERT INTO contacts (number, sent_mms) VALUES (" + number + ", 1);";
            Log.d("MMS", insertQuery);
            db.execSQL(insertQuery);

            Cursor c = db.rawQuery(selectQuery, null);

            //boolean checker = false;
            if (c.moveToFirst()) {
                do {
                    Log.d("TEXT_MESSAGE", "EXISTS");
                    int id = c.getInt(c.getColumnIndex("id"));
                    insertQuery = "INSERT INTO mms_sent (contact_id) VALUES (" + id + ");";
                    Log.d("MMS", insertQuery);
                    db.execSQL(insertQuery);

                } while (c.moveToNext());
                c.close();
            }
        }
    }
}
