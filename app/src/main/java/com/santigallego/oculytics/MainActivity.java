package com.santigallego.oculytics;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.santigallego.oculytics.SmsOutgoingObserver;

import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    final private int REQUEST_CODE_ASK_PERMISSIONS_TWO = 321;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        smsPermissions();
        contactPermissions();

        Intent intent = new Intent(this, SmsOutgoingObserver.class);
        startService(intent);

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


    private void smsPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECEIVE_MMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_MMS},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    private void contactPermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_CODE_ASK_PERMISSIONS_TWO);

        }
    }


    public void getCount(View view) {
        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        // UPDATE totals SET received = received + 1
        String query = "SELECT received FROM totals;";
        String totals = "", contacts = "", sms_received = "";

        Cursor cr = db.rawQuery(query, null);

        if (cr.moveToFirst()) {
            do {
                int total = cr.getInt(cr.getColumnIndex("received"));
                Log.d("TEXT_MESSAGE", total + "");
                totals = "TOTAL: " + total;
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

            String final_text = "TOTALS\n------------------\n" + totals + "\n\n\n\nCONTACTS\n------------------\n" + contacts + "\n\n\n\nSMS\n------------------\n" + sms_received;

            TextView queryView = (TextView) findViewById(R.id.query_text);

            queryView.setText(final_text);
        }
    }
}
