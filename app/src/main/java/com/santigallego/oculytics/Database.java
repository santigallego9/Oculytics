package com.santigallego.oculytics;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.util.Scanner;

/**
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

        String insertQuery = "", updateQuery = "", selectQuery = "";

        // UPDATE totals SET received = received + 1
        updateQuery = "UPDATE totals SET received = received + 1;";

        db.execSQL(updateQuery);

        selectQuery = "SELECT number FROM contacts;";

        Cursor cr = db.rawQuery(selectQuery, null);

        boolean checker = false;
        if (cr.moveToFirst()) {
            Log.d("TEXT_MESSAGE", "ENTERED " + number);
            do {
                if(cr.getString(cr.getColumnIndex("number")).equals(number)) {
                    Log.d("TEXT_MESSAGE", "EXISTS");
                    updateQuery = "UPDATE contacts SET received = received + 1 WHERE number = " + number;
                    db.execSQL(updateQuery);
                    checker = true;
                }
            } while (cr.moveToNext());
            if (!checker) {
                Log.d("TEXT_MESSAGE", "DOES NOT EXIST");
                insertQuery = "INSERT INTO contacts (number, sent, received) VALUES (" + number + ", 0, 1);";
                db.execSQL(insertQuery);
            }
            cr.close();
        } else {
            Log.d("TEXT_MESSAGE", "DOES NOT EXIST");
            insertQuery = "INSERT INTO contacts (number, sent, received) VALUES (" + number + ", 0, 1);";
            db.execSQL(insertQuery);
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

        String insertQuery = "", updateQuery = "", selectQuery = "";

        // UPDATE totals SET received = received + 1
        updateQuery = "UPDATE totals SET sent = sent + 1;";

        db.execSQL(updateQuery);

        selectQuery = "SELECT number FROM contacts;";

        Cursor cr = db.rawQuery(selectQuery, null);

        boolean checker = false;
        if (cr.moveToFirst()) {
            Log.d("TEXT_MESSAGE", "ENTERED " + number);
            do {
                if(cr.getString(cr.getColumnIndex("number")).equals(number)) {
                    Log.d("TEXT_MESSAGE", "EXISTS");
                    updateQuery = "UPDATE contacts SET sent = sent + 1 WHERE number = " + number;
                    db.execSQL(updateQuery);
                    checker = true;
                }
            } while (cr.moveToNext());
            if (!checker) {
                Log.d("TEXT_MESSAGE", "DOES NOT EXIST");
                insertQuery = "INSERT INTO contacts (number, sent, received) VALUES (" + number + ", 1, 0);";
                db.execSQL(insertQuery);
            }
            cr.close();
        } else {
            Log.d("TEXT_MESSAGE", "DOES NOT EXIST");
            insertQuery = "INSERT INTO contacts (number, sent, received) VALUES (" + number + ", 1, 0);";
            db.execSQL(insertQuery);
        }
    }

}
