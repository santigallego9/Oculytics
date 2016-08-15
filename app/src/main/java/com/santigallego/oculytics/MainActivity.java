package com.santigallego.oculytics;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    final private int REQUEST_CODE_ASK_PERMISSIONS_TWO = 321;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        smsPermissions();
        contactPermissions();

        try {
            SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

            // UPDATE totals SET received = received + 1
            String create = "CREATE TABLE totals (sent INTEGER, received INTEGER);";
            String insert = "INSERT INTO totals (sent, received) VALUES (0, 0);";

            db.execSQL(create);
            db.execSQL(insert);

            // UPDATE totals SET received = received + 1
            create = "CREATE TABLE contacts (id INTEGER PRIMARY KEY, number TEXT, sent INTEGER, received INTEGER);";

            db.execSQL(create);
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
                    REQUEST_CODE_ASK_PERMISSIONS);

        }
    }


    public void getCount(View view) {
        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        // UPDATE totals SET received = received + 1
        String query = "SELECT received FROM totals;";

        Cursor cr = db.rawQuery(query, null);

        if (cr.moveToFirst()) {
            do {
                int total = cr.getInt(cr.getColumnIndex("received"));
                Log.d("TEXT_MESSAGE", total + "");
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
                String text = "ID: " + id + ", NUMBER: " + number + ", REC: " + received + ", SENT: " + sent;
                Log.d("TEXT_MESSAGE", text);
            } while (cr.moveToNext());
            cr.close();
        }
    }
}
