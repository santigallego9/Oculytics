package com.santigallego.oculytics.activites;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import com.santigallego.oculytics.helpers.Charts;
import com.santigallego.oculytics.helpers.Database;
import com.santigallego.oculytics.R;
import com.santigallego.oculytics.helpers.PhoneNumbers;
import com.santigallego.oculytics.helpers.SmsContactDetailsHelper;
import com.santigallego.oculytics.services.ObserverService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup API 21 toolbar actionbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        startupFunctions();
    }

    // Run all startup functions SHOULD NOT be called as a refresh
    private void startupFunctions() {

        // Ask for permissions
        permissions();

        // Startup outgoing messages service
        Intent intent = new Intent(this, ObserverService.class);
        startService(intent);

        setupRefreshListener();
        setupContacts();
        Database.populateDatabase(this);
        setInformation();
        setupAds();
    }

    // Ask user for permissions
    private boolean permissions() {
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

    // Setup ALL information.
    public void setInformation() {
        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        // UPDATE totals SET received = received + 1
        String query = "SELECT * FROM totals;";

        Cursor cr = db.rawQuery(query, null);

        int total_sent = 0, total_received = 0;

        if (cr.moveToFirst()) {
            do {
                total_sent = cr.getInt(cr.getColumnIndex("sent"));
                total_received = cr.getInt(cr.getColumnIndex("received"));
                Log.d("TEXT_MESSAGE", "RECEIVED: " + total_received +  ", SENT: " + total_sent);
            } while (cr.moveToNext());
            cr.close();
        }

        TextView sentText = (TextView) findViewById(R.id.totals_sent_text);
        TextView recText = (TextView) findViewById(R.id.totals_rec_text);

        String sent = total_sent + "";
        String received = total_received + "";

        sentText.setText(sent);
        recText.setText(received);

        query = "SELECT * FROM mms_totals;";

        Cursor c = db.rawQuery(query, null);

        total_sent = 0;
        total_received = 0;

        if (c.moveToFirst()) {
            do {
                total_sent = c.getInt(c.getColumnIndex("sent"));
                total_received = c.getInt(c.getColumnIndex("received"));
                Log.d("MMS", "RECEIVED: " + total_received +  ", SENT: " + total_sent);
            } while (c.moveToNext());
            c.close();
        }

        sentText = (TextView) findViewById(R.id.mms_totals_sent_text);
        recText = (TextView) findViewById(R.id.mms_totals_rec_text);

        sent = total_sent + "";
        received = total_received + "";

        sentText.setText(sent);
        recText.setText(received);

        Charts.setSMSHistoryChartData(this);
        setTopThree();
    }

    // Populate the top three card
    public void setTopThree() {

        try {
            for (int i = 0; i < 6; i++) {
                LinearLayout topLayout = (LinearLayout) findViewById(R.id.top_container);

                View detailView = topLayout.findViewById(R.id.sms_details_container);

                ((ViewGroup) detailView.getParent()).removeView(detailView);
            }
        } catch (NullPointerException e) {
            Log.d("TOP_THREE", "Top three does not exist");
        }

        LinearLayout sentLayout = (LinearLayout) findViewById(R.id.sent_containter);

        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        String query = "SELECT * FROM contacts ORDER BY sent DESC LIMIT 3;";

        Cursor cr = db.rawQuery(query, null);

        if (cr.moveToFirst()) {
            do {
                HashMap<String, String> contact = new HashMap<>();

                String number = cr.getString(cr.getColumnIndex("number"));
                String sent = cr.getInt(cr.getColumnIndex("sent")) + "";
                String received = cr.getInt(cr.getColumnIndex("received")) + "";

                contact.put("number", number);
                contact.put("sent", sent);
                contact.put("received", received);

                SmsContactDetailsHelper.createContactSmsDetails(contact, sentLayout, this);

            } while (cr.moveToNext());
            cr.close();
        }

        LinearLayout receivedLayout = (LinearLayout) findViewById(R.id.received_containter);

        query = "SELECT * FROM contacts ORDER BY received DESC LIMIT 3;";

        Cursor c = db.rawQuery(query, null);

        if (c.moveToFirst()) {
            do {
                HashMap<String, String> contact = new HashMap<>();

                String number = c.getString(c.getColumnIndex("number"));
                String sent = c.getInt(c.getColumnIndex("sent")) + "";
                String received = c.getInt(c.getColumnIndex("received")) + "";

                contact.put("number", number);
                contact.put("sent", sent);
                contact.put("received", received);

                SmsContactDetailsHelper.createContactSmsDetails(contact, receivedLayout, this);

            } while (c.moveToNext());
            c.close();
        }
    }

    // Setup the refresh listener for swipe down
    public void setupRefreshListener() {
        // Setup refresh layout listener
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_main);

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("REFRESH", "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        setInformation();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );
    }

    // create or save contacts (run on thread)
    public void setupContacts() {

        final SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='phone_contacts'";

        Cursor cr = db.rawQuery(query, null);

        if (cr.moveToFirst()) {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            //try {
                                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                //String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                                    if (phones != null) {
                                        while (phones.moveToNext()) {
                                            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                            phoneNumber = PhoneNumbers.formatNumber(phoneNumber, false);

                                            String new_query = "SELECT * FROM phone_contacts WHERE phone_id = '" + contactId + "' LIMIT 1;";

                                            Cursor c = db.rawQuery(new_query, null);

                                            if(c.moveToFirst()) {
                                                String s_name = c.getString(c.getColumnIndex("name"));
                                                String s_phoneNumber = c.getString(c.getColumnIndex("number"));

                                                if(!s_phoneNumber.equals(phoneNumber)) {
                                                    String updateQuery = "UPDATE phone_contacts SET number = \"" + phoneNumber + "\" WHERE phone_id = '" + contactId + "';";
                                                    db.execSQL(updateQuery);
                                                }
                                                if(!s_name.equals(name)) {
                                                    String updateQuery = "UPDATE phone_contacts SET name = \"" + name + "\" WHERE phone_id = '" + contactId + "';";
                                                    db.execSQL(updateQuery);
                                                }
                                            } else {
                                                String insert_query = "INSERT INTO phone_contacts (phone_id, name, number) VALUES ('" + contactId + "', \"" + name + "\", \"" + phoneNumber + "\");";

                                                db.execSQL(insert_query);
                                            }
                                            c.close();
                                        }
                                        phones.close();
                                    }
                                }
                            //} catch (Exception e) {
                            //    Log.d("FATAL_ERROR", e.toString());
                            //}
                        }
                    }

                    if(cursor != null) {
                        cursor.close();
                    }

                }
            });

            thread.start();

        } else {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String query = "CREATE TABLE phone_contacts (" +
                            "id INTEGER PRIMARY KEY, " +
                            "phone_id TEXT, " +
                            "name TEXT, " +
                            "number TEXT, " +
                            "created_on DATETIME default current_timestamp, " +
                            "updated_on DATETIME default current_timestamp);";

                    db.execSQL(query);

                    //ContentResolver cr = getContentResolver();
                    Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            //try {
                                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                //String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                                    if (phones != null) {
                                        while (phones.moveToNext()) {
                                            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                            phoneNumber = PhoneNumbers.formatNumber(phoneNumber, false);

                                            query = "INSERT INTO phone_contacts (phone_id, name, number) VALUES ('" + contactId + "', \"" + name + "\", \"" + phoneNumber + "\");";

                                            db.execSQL(query);
                                        }
                                        phones.close();
                                    }
                                }
                            //} catch (Exception e) {

                                //Log.d("FATAL_ERROR", e.toString());
                            //}
                        }
                    }

                    if(cursor != null) {
                        cursor.close();
                    }
                }
            });

            thread.start();
        }

        cr.close();

    }

    // Setup the ad
    public void setupAds() {
        // Setup mobile ads
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    // todo remove : used as a testing arena
    public void testClick(View view) {
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);
    }

    // Launch contacts detail activity
    public void smsContactsDetailsClick(View view) {

        Intent intent = new Intent(this, ContactSmsDetailsActivity.class);
        startActivity(intent);
    }
}
