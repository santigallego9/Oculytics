package com.santigallego.oculytics;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissions();

        Intent intent = new Intent(this, SmsOutgoingObserver.class);
        startService(intent);

        //DateTime

        try {
            SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);
            Scanner scan = new Scanner(getResources()
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

    private  boolean permissions() {
        int permissionSendMessage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS);
        int contactPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int phonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        int internetPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        int wakelockPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (contactPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
        }
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (phonePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }
        if (internetPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET);
        }
        if (wakelockPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WAKE_LOCK);
        }


        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
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

        String datetime = Dates.timeAgo(0, 0, 0, 2, 0, 0);
        String query = "SELECT * FROM sms_sent WHERE sent_on <= '" + datetime + "';";

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
}
