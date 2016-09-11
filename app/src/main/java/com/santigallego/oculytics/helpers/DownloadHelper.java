package com.santigallego.oculytics.helpers;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.santigallego.oculytics.activities.MainActivity;

/*
 * Created by santigallego on 9/9/16.
 */
public class DownloadHelper {

    public DownloadHelper() {}

    public static String createString(Activity activity) {
        
        SQLiteDatabase db = activity.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);
        
        String returnQuery = "";
        
        
        
        /* ------------------- TABLE TOTALS ------------------- */

        String selectQuery = "SELECT * FROM totals;";
        Cursor totals = db.rawQuery(selectQuery, null);

        
        if (totals.moveToFirst()) {
            do {
                int sent = totals.getInt(totals.getColumnIndex("sent"));
                int received = totals.getInt(totals.getColumnIndex("received"));
                String created_on = totals.getString(totals.getColumnIndex("created_on"));
                String updated_on = totals.getString(totals.getColumnIndex("updated_on"));
                
                String insertQuery = "UPDATE totals SET sent = " + sent + ", " +
                                                       "received = " + received + ", " +
                                                       "created_on = \"" + created_on + "\", " +
                                                       "updated_on = \"" + updated_on + "\";";

                /*, received, created_on, updated_on) " +
                     "VALUES (" + sent + ", " + received + ", \"" + created_on + "\", \"" + updated_on + "\");";*/
                
                returnQuery += insertQuery + "\n";
                
                //INSERT INTO sms_received (contact_id) VALUES (1);
                
            } while (totals.moveToNext());
            totals.close();
        }
        /* ---------------------------------------------------- */
        
        returnQuery += "\n\n\n\n";
        
        
        /* ----------------- TABLE MMS TOTALS ----------------- */

        selectQuery = "SELECT * FROM mms_totals;";
        Cursor mms_totals = db.rawQuery(selectQuery, null);
        
        
        if (mms_totals.moveToFirst()) {
            do {
                int sent = mms_totals.getInt(mms_totals.getColumnIndex("sent"));
                int received = mms_totals.getInt(mms_totals.getColumnIndex("received"));
                String created_on = mms_totals.getString(mms_totals.getColumnIndex("created_on"));
                String updated_on = mms_totals.getString(mms_totals.getColumnIndex("updated_on"));


                String insertQuery = "UPDATE mms_totals SET sent = " + sent + ", " +
                                                           "received = " + received + ", " +
                                                           "created_on = \"" + created_on + "\", " +
                                                           "updated_on = \"" + updated_on + "\";";


                /*String insertQuery = "INSERT INTO mms_totals (sent, received, created_on, updated_on) " +
                     "VALUES (" + sent + ", " + received + ", \"" + created_on + "\", \"" + updated_on + "\");";*/

                returnQuery += insertQuery + "\n";

                //INSERT INTO sms_received (contact_id) VALUES (1);

            } while (mms_totals.moveToNext());
            mms_totals.close();
        }
        /* ---------------------------------------------------- */

        returnQuery += "\n\n\n\n";
        
        
        /* ------------------ TABLE CONTACTS ------------------ */
        
        selectQuery = "SELECT * FROM contacts;";
        Cursor contacts = db.rawQuery(selectQuery, null);
        
        
        if(contacts.moveToFirst()) {
            do {
                int id = contacts.getInt(contacts.getColumnIndex("id"));
                String number = contacts.getString(contacts.getColumnIndex("number"));
                int sent = contacts.getInt(contacts.getColumnIndex("sent"));
                int received = contacts.getInt(contacts.getColumnIndex("received"));
                int sent_mms = contacts.getInt(contacts.getColumnIndex("sent_mms"));
                int received_mms = contacts.getInt(contacts.getColumnIndex("received_mms"));
                String created_on = contacts.getString(contacts.getColumnIndex("created_on"));
                String updated_on = contacts.getString(contacts.getColumnIndex("updated_on"));
                String sent_updated_on = "" + contacts.getString(contacts.getColumnIndex("sent_updated_on"));
                String received_updated_on = "" + contacts.getString(contacts.getColumnIndex("received_updated_on"));

                String insertQuery = "INSERT INTO contacts (id, number, sent, received, sent_mms, " +
                        "received_mms, created_on, updated_on, sent_updated_on, received_updated_on) " +
                        "VALUES (" + id + ", \"" + number + "\", " + sent + ", " + received + ", " + sent_mms + ", " + received_mms +
                        ", \"" + created_on + "\", \"" + updated_on + "\", \"" + sent_updated_on + "\", \"" + received_updated_on + "\");";

                returnQuery += insertQuery + "\n";

            } while (contacts.moveToNext());
            contacts.close();
        }
        /* ---------------------------------------------------- */

        returnQuery += "\n\n\n\n";
        
        
        /* ------------------ TABLE STREAKS ------------------- */

        selectQuery = "SELECT * FROM streaks;";
        Cursor streaks = db.rawQuery(selectQuery, null);


        if (streaks.moveToFirst()) {
            do {
                int id = streaks.getInt(streaks.getColumnIndex("id"));
                int contact_id = streaks.getInt(streaks.getColumnIndex("contact_id"));
                int streak = streaks.getInt(streaks.getColumnIndex("streak"));
                String streak_updated_on = streaks.getString(streaks.getColumnIndex("streak_updated_on"));

                String insertQuery = "INSERT INTO streaks (id, contact_id, streak, streak_updated_on) " +
                        "VALUES (" + id + ", " + contact_id + ", " + streak + ", \"" + streak_updated_on + "\");";

                returnQuery += insertQuery + "\n";


            } while (streaks.moveToNext());
            streaks.close();
        }
        /* ---------------------------------------------------- */

        returnQuery += "\n\n\n\n";
        

        /* ------------------ TABLE SMS SENT ------------------ */

        selectQuery = "SELECT * FROM sms_sent;";
        Cursor sms_sent = db.rawQuery(selectQuery, null);


        if (sms_sent.moveToFirst()) {
            do {
                int id = sms_sent.getInt(sms_sent.getColumnIndex("id"));
                int contact_id = sms_sent.getInt(sms_sent.getColumnIndex("contact_id"));
                String sent_on = sms_sent.getString(sms_sent.getColumnIndex("sent_on"));

                String insertQuery = "INSERT INTO sms_sent (id, contact_id, sent_on) " +
                        "VALUES (" + id + ", " + contact_id + ", \"" + sent_on + "\");";

                returnQuery += insertQuery + "\n";


            } while (sms_sent.moveToNext());
            sms_sent.close();
        }
        /* ---------------------------------------------------- */

        returnQuery += "\n\n\n\n";


        /* ---------------- TABLE SMS RECEIVED ---------------- */

        selectQuery = "SELECT * FROM sms_received;";
        Cursor sms_received = db.rawQuery(selectQuery, null);


        if (sms_received.moveToFirst()) {
            do {
                int id = sms_received.getInt(sms_received.getColumnIndex("id"));
                int contact_id = sms_received.getInt(sms_received.getColumnIndex("contact_id"));
                String received_on = sms_received.getString(sms_received.getColumnIndex("received_on"));

                String insertQuery = "INSERT INTO sms_received (id, contact_id, received_on) " +
                        "VALUES (" + id + ", " + contact_id + ", \"" + received_on + "\");";

                returnQuery += insertQuery + "\n";


            } while (sms_received.moveToNext());
            sms_received.close();
        }
        /* ---------------------------------------------------- */

        returnQuery += "\n\n\n\n";
        
        
        /* ------------------ TABLE MMS SENT ------------------ */

        selectQuery = "SELECT * FROM mms_sent;";
        Cursor mms_sent = db.rawQuery(selectQuery, null);


        if (mms_sent.moveToFirst()) {
            do {
                int id = mms_sent.getInt(mms_sent.getColumnIndex("id"));
                int contact_id = mms_sent.getInt(mms_sent.getColumnIndex("contact_id"));

                for(int i = 0; i < 3; i++) {
                    // Log.d("TESTER", mms_sent.getColumnName(i));
                }

                String sent_on = mms_sent.getString(mms_sent.getColumnIndex("sent_on"));

                String insertQuery = "INSERT INTO mms_sent (id, contact_id, sent_on) " +
                        "VALUES (" + id + ", " + contact_id + ", \"" + sent_on + "\");";

                returnQuery += insertQuery + "\n";


            } while (mms_sent.moveToNext());
            mms_sent.close();
        }
        /* ---------------------------------------------------- */

        returnQuery += "\n\n\n\n";


        /* ---------------- TABLE MMS RECEIVED ---------------- */

        selectQuery = "SELECT * FROM mms_received;";
        Cursor mms_received = db.rawQuery(selectQuery, null);


        if (mms_received.moveToFirst()) {
            do {
                int id = mms_received.getInt(mms_received.getColumnIndex("id"));
                int contact_id = mms_received.getInt(mms_received.getColumnIndex("contact_id"));
                String received_on = mms_received.getString(mms_received.getColumnIndex("received_on"));

                String insertQuery = "INSERT INTO mms_received (id, contact_id, received_on) " +
                        "VALUES (" + id + ", " + contact_id + ", \"" + received_on + "\");";

                returnQuery += insertQuery + "\n";


            } while (mms_received.moveToNext());
            mms_received.close();
        }
        /* ---------------------------------------------------- */

        returnQuery += "\n\n\n\n";
        
        
        return returnQuery;
    }
}
