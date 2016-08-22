package com.santigallego.oculytics.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.santigallego.oculytics.helpers.Database;
import com.santigallego.oculytics.helpers.Dates;
import com.santigallego.oculytics.R;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


    }

    public void getCount(View view) {
        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        // UPDATE totals SET received = received + 1
        String query = "SELECT * FROM totals;";
        String totals = "", contacts = "", sms_received = "", sms_sent = "";

        Cursor cr = db.rawQuery(query, null);

        if (cr.moveToFirst()) {
            do {
                int total_sent = cr.getInt(cr.getColumnIndex("sent"));
                int total_received = cr.getInt(cr.getColumnIndex("received"));
                Log.d("TEXT_MESSAGE", "RECEIVED: " + total_received +  ", SENT: " + total_sent);
                totals = "RECEIVED: " + total_received + "\nSENT: " + total_sent;
            } while (cr.moveToNext());
            cr.close();
        }

        query = "SELECT * FROM contacts;";

        cr = db.rawQuery(query, null);

        if (cr.moveToFirst()) {
            do {
                int id = cr.getInt(cr.getColumnIndex("id"));
                String number = cr.getString(cr.getColumnIndex("number"));
                int received = cr.getInt(cr.getColumnIndex("received"));
                int sent = cr.getInt(cr.getColumnIndex("sent"));
                String created_on = cr.getString(cr.getColumnIndex("created_on"));
                String updated_on = cr.getString(cr.getColumnIndex("updated_on"));
                String text = "ID: " + id + ", NUMBER: " + number + ", REC: " + received + ", SENT: " + sent + ", CREATED_ON: " + created_on + ", UPDATED_ON: " + updated_on;
                contacts += text + "\n\n";
                Log.d("TEXT_MESSAGE", text);
            } while (cr.moveToNext());
            cr.close();
        }

        query = "SELECT * FROM sms_received;";

        cr = db.rawQuery(query, null);

        if (cr.moveToFirst()) {
            do {
                int id = cr.getInt(cr.getColumnIndex("id"));
                int contact_id = cr.getInt(cr.getColumnIndex("contact_id"));
                String received_on = cr.getString(cr.getColumnIndex("received_on"));
                String text = "ID: " + id + ", CONTACT_ID: " + contact_id + ", RECEIVED_ON: " + received_on;
                sms_received += text + "\n\n";
                Log.d("TEXT_MESSAGE", text);
            } while (cr.moveToNext());
            cr.close();
        }

        query = "SELECT * FROM sms_sent;";

        cr = db.rawQuery(query, null);

        if (cr.moveToFirst()) {
            do {
                int id = cr.getInt(cr.getColumnIndex("id"));
                int contact_id = cr.getInt(cr.getColumnIndex("contact_id"));
                String sent_on = cr.getString(cr.getColumnIndex("sent_on"));
                String text = "ID: " + id + ", CONTACT_ID: " + contact_id + ", SENT_ON: " + sent_on;
                sms_sent += text + "\n\n";
                Log.d("TEXT_MESSAGE", text);
            } while (cr.moveToNext());
            cr.close();
        }

        String final_text = "TOTALS\n------------------\n" + totals + "\n\n\n\nCONTACTS\n------------------\n" +
                contacts + "\n\n\n\nSMS RECEIVED\n------------------\n" +
                sms_received  + "\n\n\n\nSMS SENT\n------------------\n" + sms_sent;

        TextView queryView = (TextView) findViewById(R.id.query_text);

        queryView.setText(final_text);
    }

    public void sendText(View view) {
        SmsManager mgr = SmsManager.getDefault();

        mgr.sendTextMessage("+12543969600", null,
                "TEST TEXT", null, null);
    }

    public void datetimeClick(View view) {

        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        //String datetime = Dates.timeAgo(0, 0, 0, 0, 0, 0);

        Toast.makeText(this, Dates.fromUtcToLocal("2016-08-17 02:19:00"), Toast.LENGTH_LONG).show();

        String query = "SELECT * FROM sms_sent WHERE sent_on <= '2016-08-17 02:19:00';";

        Cursor cr = db.rawQuery(query, null);

        String sent_on = "";

        if (cr.moveToFirst()) {
            do {
                sent_on += cr.getString(cr.getColumnIndex("sent_on")) + "\n";
            } while (cr.moveToNext());
            cr.close();
        }

        TextView textView = (TextView) findViewById(R.id.query_text);

        textView.setText(sent_on);

    }

    public void testClick(View view) {
        Toast.makeText(this, Dates.fromUtcToLocal("2016-08-17 02:19:00"), Toast.LENGTH_LONG).show();
    }
}
