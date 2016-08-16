package com.santigallego.oculytics;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/*
 * Created by santigallego on 8/14/16.
 */
public class Database {

    public Database() {}

    public static final String DATABASE_NAME = "oculytics";

    public static void messageReceived(Context context, String number) {
        if(number.contains("+")) {
            Log.d("TEXT_MESSAGE", "----------------------");
            int country_code = number.length() - 10;
            Log.d("TEXT_MESSAGE", "CODE: " + number.substring(0, country_code));

            if(country_code > 0) {
                number = number.substring(country_code);
            } else {
                number = number.substring(1);
            }
            Log.d("TEXT_MESSAGE", "----------------------");
        }

        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, MainActivity.MODE_PRIVATE, null);
        String insertQuery, updateQuery, selectQuery;

        // UPDATE totals SET received = received + 1
        updateQuery = "UPDATE totals SET received = received + 1, updated_on = current_timestamp;";
        db.execSQL(updateQuery);

        selectQuery = "SELECT * FROM contacts WHERE number = " + number + ";";
        Cursor cr = db.rawQuery(selectQuery, null);

        //boolean checker = false;
        if (cr.moveToFirst()) {
            Log.d("TEXT_MESSAGE", "ENTERED " + number);
            do {
                Log.d("TEXT_MESSAGE", "EXISTS");
                int id = cr.getInt(cr.getColumnIndex("id"));
                updateQuery = "UPDATE contacts SET received = received + 1, updated_on = current_timestamp WHERE number = " + number + ";";
                insertQuery = "INSERT INTO sms_received (contact_id) VALUES (" + id + ");";
                Log.d("TEXT_MESSAGE", insertQuery);
                db.execSQL(updateQuery);
                db.execSQL(insertQuery);

            } while (cr.moveToNext());
            cr.close();
        } else {
            Log.d("TEXT_MESSAGE", "DOES NOT EXIST");
            insertQuery = "INSERT INTO contacts (number, received) VALUES (" + number + ", 1);";
            db.execSQL(insertQuery);

            Cursor c = db.rawQuery(selectQuery, null);
            //boolean checker = false;
            if (c.moveToFirst()) {
                do {
                    Log.d("TEXT_MESSAGE", "EXISTS");
                    int id = c.getInt(c.getColumnIndex("id"));
                    insertQuery = "INSERT INTO sms_received (contact_id) VALUES (" + id + ");";
                    db.execSQL(insertQuery);

                } while (c.moveToNext());
                c.close();
            }
        }
    }

    public static void messageSent(Context context, String number) {
        if(number.contains("+")) {
            Log.d("TEXT_MESSAGE", "----------------------");
            int country_code = number.length() - 10;
            Log.d("TEXT_MESSAGE", "CODE: " + number.substring(0, country_code));

            if(country_code > 0) {
                number = number.substring(country_code);
            } else {
                number = number.substring(1);
            }
            Log.d("TEXT_MESSAGE", "----------------------");
        }

        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        String insertQuery, updateQuery, selectQuery;

        // UPDATE totals SET received = received + 1
        updateQuery = "UPDATE totals SET sent = sent, updated_on = current_timestamp + 1;";

        db.execSQL(updateQuery);

        selectQuery = "SELECT * FROM contacts WHERE number = " + number + ";";

        Cursor cr = db.rawQuery(selectQuery, null);

        //boolean checker = false;
        if (cr.moveToFirst()) {
            Log.d("TEXT_MESSAGE", "ENTERED " + number);
            do {
                Log.d("TEXT_MESSAGE", "EXISTS");
                int id = cr.getInt(cr.getColumnIndex("id"));
                updateQuery = "UPDATE contacts SET sent = sent + 1, updated_on = current_timestamp WHERE number = " + number + ";";
                insertQuery = "INSERT INTO sms_sent (contact_id) VALUES (" + id + ");";
                db.execSQL(updateQuery);
                db.execSQL(insertQuery);

            } while (cr.moveToNext());
            /*if (!checker) {
                Log.d("TEXT_MESSAGE", "DOES NOT EXIST");
                insertQuery = "INSERT INTO contacts (number, sent, received) VALUES (" + number + ", 1);";
                db.execSQL(insertQuery);
            }*/
            cr.close();
        } else {
            Log.d("TEXT_MESSAGE", "DOES NOT EXIST");
            insertQuery = "INSERT INTO contacts (number, sent) VALUES (" + number + ", 1);";
            db.execSQL(insertQuery);

            Cursor c = db.rawQuery(selectQuery, null);

            //boolean checker = false;
            if (c.moveToFirst()) {
                do {
                    Log.d("TEXT_MESSAGE", "EXISTS");
                    int id = c.getInt(c.getColumnIndex("id"));
                    insertQuery = "INSERT INTO sms_sent (contact_id) VALUES (" + id + ");";
                    db.execSQL(insertQuery);

                } while (c.moveToNext());
                c.close();
            }
        }
    }

}
