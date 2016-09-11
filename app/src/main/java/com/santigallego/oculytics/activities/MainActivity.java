package com.santigallego.oculytics.activities;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.LinearEase;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import com.santigallego.oculytics.helpers.Contacts;
import com.santigallego.oculytics.helpers.Database;
import com.santigallego.oculytics.R;
import com.santigallego.oculytics.helpers.Dates;
import com.santigallego.oculytics.helpers.MathHelper;
import com.santigallego.oculytics.helpers.PhoneNumbers;
import com.santigallego.oculytics.helpers.SmsContactDetailsHelper;
import com.santigallego.oculytics.helpers.Streaks;
import com.santigallego.oculytics.services.ObserverService;
import com.squareup.picasso.Downloader;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private static ArrayList<Bitmap> bitmaps = new ArrayList<>();
    public final static DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        /*db.execSQL("DROP TABLE IF EXISTS totals");
        db.execSQL("DROP TABLE IF EXISTS mms_totals");
        db.execSQL("DROP TABLE IF EXISTS contacts");
        db.execSQL("DROP TABLE IF EXISTS streaks");
        db.execSQL("DROP TABLE IF EXISTS sms_sent");
        db.execSQL("DROP TABLE IF EXISTS sms_received");
        db.execSQL("DROP TABLE IF EXISTS mms_sent");
        db.execSQL("DROP TABLE IF EXISTS mms_received");*/

        //

        db.close();

        startupFunctions();

        //SetupExistingMessagesThread thread = new SetupExistingMessagesThread();
        //thread.run();

        //
        // new SetupMessagesTask().execute("");
    }

    private class SetupMessagesTask extends AsyncTask<String, Integer, Long> {

        double total;

        // Do the long-running work in here
        protected Long doInBackground(String... strings) {
            /*int count = urls.length;
            long totalSize = 0;
            for (int i = 0; i < count; i++) {
                totalSize += Downloader.downloadFile(urls[i]);
                publishProgress((int) ((i / (float) count) * 100));
                // Escape early if cancel() is called
                if (isCancelled()) break;
            }*/

            // this is a change

            int s_id = 0, sent = 0, received = 0;

            Uri uriSMSURI = Uri.parse("content://sms");
            Cursor cr = getContentResolver().query(uriSMSURI, null, null, null, null);

            uriSMSURI = Uri.parse("content://mms");
            Cursor cmms = MainActivity.this.getContentResolver().query(uriSMSURI, null, null, null, null);


            total = cr.getCount() + cmms.getCount();
            double counter = 0;

            // this will make it point to the first record, which is the last SMS sent
            long totalSize = 0;
            if(cr.moveToLast()) {
                do {
                    counter++;
                    totalSize++;
                    if (s_id != cr.getInt(cr.getColumnIndex("_id"))) {
                        s_id = cr.getInt(cr.getColumnIndex("_id"));

                        String address = cr.getString(cr.getColumnIndex("address"));
                        String date = cr.getString(cr.getColumnIndex("date"));
                        int type = cr.getInt(cr.getColumnIndex("type"));
                        String smsType = "";

                        long milliSeconds = Long.parseLong(date);
                        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(milliSeconds);
                        String finalDateString = formatter.format(calendar.getTime());

                        if(type == 1) {
                            smsType = "RECEIVED";
                            received++;
                            try {
                                Database.messageReceived(MainActivity.this, address, finalDateString);
                            } catch (Exception e) {
                                // Log("MESSAGE_ERROR", "Ignoring this message");
                            }
                        } else if(type == 2) {
                            smsType = "SENT";
                            sent++;
                            try {
                                Database.messageSent(MainActivity.this, address, finalDateString);
                            } catch (Exception e) {
                                // Log("MESSAGE_ERROR", "Ignoring this message");
                            }
                        }





                        // Log("TESTING", " ");
                        // Log("TESTING", "Name: " + Contacts.searchContactsUsingNumber(PhoneNumbers.formatNumber(address, false), MainActivity.this).get("name"));
                        // Log("TESTING", "Date: " + finalDateString);
                        // Log("TESTING", "Type: " + smsType);
                        // Log("TESTING", " ");

                    } else {
                        // Log("TESTING", "MESSAGE ALREADY LOGGED");
                    }
                    publishProgress((int)counter);
                } while(cr.moveToPrevious());
            }

            // Log("TESTING", " ");
            // Log("TESTING", "TOTALS");
            // Log("TESTING", "-------------------------");
            // Log("TESTING", "Sent: " + sent);
            // Log("TESTING", "Received: " + received);
            // Log("TESTING", "-------------------------");
            // Log("TESTING", " ");

            cr.close();

            s_id = 0;

            // this will make it point to the first record, which is the last SMS sent


            if(cmms.moveToLast()) {
                do {
                    counter++;
                    try {
                        String isIncoming = getIncomingMmsAddress(cmms.getInt(cmms.getColumnIndex(cmms.getColumnName(0))), MainActivity.this);
                        if (isIncoming.equals("insert-address-token")) {
                            String address = getOutgoingMmsAddress(cmms.getInt(cmms.getColumnIndex(cmms.getColumnName(0))));
                            if (address.length() > 1) {
                                if (s_id != Integer.parseInt(cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))))) {
                                    s_id = Integer.parseInt(cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))));
                                    // Log("MMS", cmms.getColumnName(0) + ": " + cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))));
                                    // Log("MMS", "OUTGOING: " + getOutgoingMmsAddress(cmms.getInt(cmms.getColumnIndex(cmms.getColumnName(0)))));


                                    Database.mmsSent(MainActivity.this, address);


                                } else {
                                    // Log("MMS", "ALREADY LOGGED");
                                }
                            }
                        } else {
                            String address = getIncomingMmsAddress(cmms.getInt(cmms.getColumnIndex(cmms.getColumnName(0))), MainActivity.this);
                            if (address.length() > 1) {
                                if (s_id != Integer.parseInt(cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))))) {
                                    s_id = Integer.parseInt(cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))));
                                    // Log("MMS", cmms.getColumnName(0) + ": " + cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))));
                                    // Log("MMS", "INCOMING: " + getIncomingMmsAddress(cmms.getInt(cmms.getColumnIndex(cmms.getColumnName(0))), MainActivity.this));

                                    Database.mmsReceived(MainActivity.this, address);
                                } else {
                                    // Log("MMS", "ALREADY LOGGED");
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Log("MMS", "BROKE DOWN");
                    }
                    publishProgress((int)counter);
                } while (cmms.moveToPrevious());
            }

            cmms.close();



            return totalSize;
        }

        public String getOutgoingMmsAddress(int id) {
            String selectionAdd = new String("msg_id=" + id);
            String uriStr = "content://mms/" + id + "/addr";
            Uri uriAddress = Uri.parse(uriStr);
            Cursor cAdd = getContentResolver().query(uriAddress, null,
                    selectionAdd, null, null);
            String name = null;
            if (cAdd.moveToFirst()) {
                do {
                    String number = cAdd.getString(cAdd.getColumnIndex("address"));
                    if (number != null) {
                        try {
                            Long.parseLong(number.replace("-", ""));
                            name = number;
                        } catch (NumberFormatException nfe) {
                            if (name == null) {
                                name = number;
                            }
                        }
                    }
                } while (cAdd.moveToNext());
            }
            if (cAdd != null) {
                cAdd.close();
            }
            return name;
        }

        public String getIncomingMmsAddress(int id, Activity service) {
            String addrSelection = "type=137 AND msg_id=" + id;
            String uriStr = "content://mms/" + id + "/addr";
            Uri uriAddress = Uri.parse(uriStr);
            String[] columns = { "address" };
            Cursor cursor = service.getContentResolver().query(uriAddress, columns,
                    addrSelection, null, null);
            String address = "";
            String val;
            if (cursor.moveToFirst()) {
                do {
                    val = cursor.getString(cursor.getColumnIndex("address"));
                    if (val != null) {
                        address = val;
                        // Use the first one found if more than one
                        break;
                    }
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
            // return address.replaceAll("[^0-9]", "");
            return address;
        }

        // This is called each time you call publishProgress()
        protected void onProgressUpdate(Double... progress) {

            double percent = progress[0] / total * 100;

            // Log("PERCENT", "" + percent);
        }

        // This is called when doInBackground() is finished
        protected void onPostExecute(Long result) {
            setInformation();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_activity, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(searchItem);


        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        ComponentName componentName = new ComponentName(this, SearchableActivity.class);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;

            case R.id.action_upload:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                Intent upload = new Intent(this, FileInfoActivity.class);
                upload.putExtra("upload", false);
                startActivity(upload);
                return true;

            case R.id.action_download:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                Intent download = new Intent(this, FileInfoActivity.class);
                download.putExtra("download", true);
                startActivity(download);
                return true;

            /*case R.id.action_search:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                Intent search = new Intent(this, SearchableActivity.class);
                startActivity(search);
                return true;*/

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void updateDB() {
        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        //db.execSQL("DROP TABLE streaks;");
        //db.execSQL("CREATE TABLE streaks (id INTEGER PRIMARY KEY, contact_id INTEGER, streak INTEGER default 1, streak_updated_on DATETIME default current_timestamp);");

        try {
            db.execSQL("DROP TABLE streaks;");
            //db.execSQL("ALTER TABLE contacts ADD COLUMN sent_updated_on DATETIME;");
            //db.execSQL("ALTER TABLE contacts ADD COLUMN received_updated_on DATETIME;");

            db.execSQL("CREATE TABLE streaks (id INTEGER PRIMARY KEY, contact_id INTEGER, streak INTEGER default 1, streak_updated_on DATETIME default current_timestamp);");
        } catch (Exception e) {
            // Log("FAILED", "CREATE: " + e.toString());
        }

        String query = "SELECT * FROM contacts;";

        Cursor cr = db.rawQuery(query, null);

        if(cr.moveToFirst()) {
            do {
                String updatedOn = cr.getString(cr.getColumnIndex("updated_on"));
                int id = cr.getInt(cr.getColumnIndex("id"));


                String sms_sent_on = getStreakInfo(id, "sms_sent", "sent_on");
                String sms_received_on = getStreakInfo(id, "sms_received", "received_on");
                String mms_sent_on = getStreakInfo(id, "mms_sent", "sent_on");
                String mms_received_on = getStreakInfo(id, "mms_received", "received_on");

                DateTime sent_on = null, received_on = null;

                boolean sms_sent = false, sms_received = false, mms_sent = false, mms_received = false, sent = true, received = true;

                DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

                DateTime dt_sms_sent_on = null, dt_sms_received_on = null, dt_mms_sent_on = null, dt_mms_received_on = null;

                if(sms_sent_on.length() > 0) {
                    dt_sms_sent_on = dtf.parseDateTime(sms_sent_on);
                    sms_sent = true;
                }

                if(sms_received_on.length() > 0) {
                    dt_sms_received_on = dtf.parseDateTime(sms_received_on);
                    sms_received = true;
                }

                if(mms_sent_on.length() > 0) {
                    dt_mms_sent_on = dtf.parseDateTime(mms_sent_on);
                    mms_sent = true;
                }

                if(mms_received_on.length() > 0) {
                    dt_mms_received_on = dtf.parseDateTime(mms_received_on);
                    mms_received = true;
                }

                if(sms_sent && mms_sent) {
                    boolean sms = dt_sms_sent_on.isAfter(dt_mms_sent_on);
                    if(sms) {
                        sent_on = dt_sms_sent_on;
                    } else {
                        sent_on = dt_mms_sent_on;
                    }
                } else if (sms_sent && !mms_sent) {
                    sent_on = dt_sms_sent_on;
                } else if (!sms_sent && mms_sent){
                    sent_on = dt_mms_sent_on;
                } else {
                    sent = false;
                }

                if(sms_received && mms_received) {
                    boolean sms = dt_sms_received_on.isAfter(dt_mms_received_on);
                    if(sms) {
                        received_on = dt_sms_received_on;
                    } else {
                        received_on = dt_mms_received_on;
                    }
                } else if (sms_received && !mms_received) {
                    received_on = dt_sms_sent_on;
                } else if (!sms_received && mms_received){
                    received_on = dt_mms_sent_on;
                } else {
                    received = false;
                }

                String updateQuery;

                if(sent && received) {
                    updateQuery = "UPDATE contacts SET sent_updated_on = \"" + dtfOut.print(sent_on) + "\", received_updated_on = \"" + dtfOut.print(received_on) + "\";";
                } else if (!sent && received) {
                    updateQuery = "UPDATE contacts SET received_updated_on = \"" + dtfOut.print(received_on) + "\";";
                } else {
                    updateQuery = "UPDATE contacts SET sent_updated_on = \"" + dtfOut.print(sent_on) + "\";";
                }

                try {
                    db.execSQL(updateQuery);
                } catch (Exception e) {
                    // Log("FAILED", "UPDATE contacts: " + e.toString());
                }


                String insertQuery = "INSERT INTO streaks (contact_id, streak_updated_on) VALUES (" + id + ", \"" + updatedOn + "\");";
                try {
                    db.execSQL(insertQuery);
                } catch (Exception e) {
                    // Log("FAILED", "INSERT INTO streaks: " + e.toString());
                }
            } while (cr.moveToNext());

            db.close();
        }
    }

    private void checkStreaks() {

        Thread thread = new Thread() {
            @Override
            public void run() {
                SQLiteDatabase db = MainActivity.this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

                String query = "SELECT * FROM contacts;";

                Cursor cr = db.rawQuery(query, null);


                if(cr.moveToFirst()) {
                    do {
                        int id = cr.getInt(cr.getColumnIndex("id"));
                        String number = cr.getString(cr.getColumnIndex("number"));

                        Streaks.checkForStreakClear(MainActivity.this, id);
                        Streaks.updateStreak(MainActivity.this, id);

                    } while(cr.moveToNext());
                }

                db.close();
            }
        };
        thread.run();
    }

    private String getStreakInfo(int id, String table_name, String column_name) {
        String timestamp;

        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        try {

            String query = "SELECT * FROM '" + table_name + "' WHERE contact_id = " + id + " ORDER BY '" + column_name + "' DESC LIMIT 1";

            Cursor cr = db.rawQuery(query, null);

            if (cr.moveToFirst()) {
                do {
                    timestamp = cr.getString(cr.getColumnIndex(column_name));
                } while (cr.moveToNext());
                cr.close();
            } else {
                db.close();
                return "";
            }
        } catch (Exception e) {
            db.close();
            return "";
        }
        db.close();

        return timestamp;
    }

    // Run all startup functions SHOULD NOT be called as a refresh
    private void startupFunctions() {

        // Ask for permissions
        permissions();

        // Startup outgoing messages service
        Intent intent = new Intent(this, ObserverService.class);
        startService(intent);

        setupRefreshListener();
        Database.populateDatabase(R.raw.seed, this);
        setInformation();
        setupHistoryChart();
        checkBatteryState();
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
    private void setInformation() {
        checkStreaks();
        setTotals();
        setTopThree();
    }

    private void setTotals() {
        //Layout layout = (Layout) findViewById(R.id.)
        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        // UPDATE totals SET received = received + 1
        String query = "SELECT * FROM totals;";

        Cursor cr = db.rawQuery(query, null);

        int total_sent = 0, total_received = 0;

        if (cr.moveToFirst()) {
            do {
                total_sent = cr.getInt(cr.getColumnIndex("sent"));
                total_received = cr.getInt(cr.getColumnIndex("received"));
                // Log("TEXT_MESSAGE", "RECEIVED: " + total_received +  ", SENT: " + total_sent);
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
                // Log("MMS", "RECEIVED: " + total_received +  ", SENT: " + total_sent);
            } while (c.moveToNext());
            c.close();
        }

        sentText = (TextView) findViewById(R.id.mms_totals_sent_text);
        recText = (TextView) findViewById(R.id.mms_totals_rec_text);

        sent = total_sent + "";
        received = total_received + "";

        sentText.setText(sent);
        recText.setText(received);

        db.close();
    }

    // Populate the top three card
    private void setTopThree() {

        try {
            for (int i = 0; i < 6; i++) {
                LinearLayout topLayout = (LinearLayout) findViewById(R.id.top_container);

                View detailView = topLayout.findViewById(R.id.sms_details_container);

                ((ViewGroup) detailView.getParent()).removeView(detailView);
            }
        } catch (NullPointerException e) {
            // Log("TOP_THREE", "Top three does not exist");
        }

        LinearLayout sentLayout = (LinearLayout) findViewById(R.id.sent_containter);

        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        String query = "SELECT * FROM contacts ORDER BY sent DESC, received DESC LIMIT 3;";
        Cursor cr = db.rawQuery(query, null);

        if (cr.moveToFirst()) {
            do {
                HashMap<String, String> contact = new HashMap<>();

                int id = cr.getInt(cr.getColumnIndex("id"));
                String number = cr.getString(cr.getColumnIndex("number"));
                String sent = cr.getInt(cr.getColumnIndex("sent")) + "";
                String received = cr.getInt(cr.getColumnIndex("received")) + "";

                contact.put("number", number);
                contact.put("sent", sent);
                contact.put("received", received);
                contact.put("streak", "" + Streaks.getStreak(this, id));

                Bitmap bitmap = SmsContactDetailsHelper.createContactSmsDetails(this, contact, sentLayout, false);
                if(bitmap != null) {
                    bitmaps.add(bitmap);
                }

                String msg = "NUMBER: " + number + " \n" +
                             "SENT: " + sent + " \n" +
                             "RECEIVED: " + received;
                // Log("COUNT_TOP_THREE", msg);

            } while (cr.moveToNext());
            cr.close();
        }

        LinearLayout receivedLayout = (LinearLayout) findViewById(R.id.received_containter);

        query = "SELECT * FROM contacts ORDER BY received DESC, sent DESC LIMIT 3;";

        Cursor c = db.rawQuery(query, null);

        if (c.moveToFirst()) {
            do {
                HashMap<String, String> contact = new HashMap<>();

                int id = c.getInt(cr.getColumnIndex("id"));
                String number = c.getString(c.getColumnIndex("number"));
                String sent = c.getInt(c.getColumnIndex("sent")) + "";
                String received = c.getInt(c.getColumnIndex("received")) + "";

                contact.put("number", number);
                contact.put("sent", sent);
                contact.put("received", received);
                contact.put("streak", "" + Streaks.getStreak(this, id));

                Bitmap bitmap = SmsContactDetailsHelper.createContactSmsDetails(this, contact, receivedLayout, false);
                if(bitmap != null) {
                    bitmaps.add(bitmap);
                }

                String msg = "NUMBER: " + number + " \n" +
                             "SENT: " + sent + " \n" +
                             "RECEIVED: " + received;
                // Log("COUNT_TOP_THREE", msg);

            } while (c.moveToNext());
            c.close();
        }

        db.close();
    }

    // Clear any bitmaps set for totals
    private void clearBitmaps() {
        for(Bitmap bitmap : bitmaps) {
            bitmap.recycle();
        }

        bitmaps.clear();
    }

    // Set animation for charts
    private void smsHistoryChartAnimation(int id) {

        ChartView chart = (ChartView) findViewById(id);

        Animation animation = new Animation();

        animation.setDuration(1000)
                .setEasing(new LinearEase());

        chart.show(animation);
    }

    // sets up 30 day history chart
    private void setupHistoryChart() {

        LineSet sentDataset = new LineSet();
        LineSet receivedDataset = new LineSet();

        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        String date = Dates.dtfOut.print(new DateTime(DateTimeZone.UTC));

        date = Dates.fromUtcToLocal(date);
        date = Dates.formatToMidnight(date);

        int days = 31;
        int step = days / 5;
        int highestValue = 0;
        for(int i = days; i >= 0; i--) {
            // Log("TEST", "ENTERED");

            String new_date = Dates.timeBefore(0, 0, 0, i, 0, 0, 0, date);
            String last_date = Dates.timeBefore(0, 0, 0, i + 1, 0, 0, 0, date);

            String query = "SELECT * FROM sms_sent WHERE sent_on <= '" + new_date + "' AND sent_on >= '" + last_date + "';";
            Cursor cr = db.rawQuery(query, null);
            int sent = cr.getCount();
            // Log("DEBUG", "SENT: " + sent);
            cr.close();

            query = "SELECT * FROM sms_received WHERE received_on <= '" + new_date + "' AND received_on >= '" + last_date + "';";
            Cursor c = db.rawQuery(query, null);
            int received = c.getCount();
            // Log("DEBUG", "RECEIVED: " + received);
            c.close();

            String label = Dates.toDisplay(new_date);

            // Log("COUNT", label + ", SENT: " + sent + ", RECEIVED: " + received);

            if(i == days) {
                sentDataset.addPoint("", sent);
                receivedDataset.addPoint("", received);
            } else if(i % step == 0) {
                sentDataset.addPoint(label, sent);
                receivedDataset.addPoint(label, received);
            } else {
                sentDataset.addPoint("", sent);
                receivedDataset.addPoint("", received);
            }

            if(sent > highestValue) {
                highestValue = sent;
            }
            if(received > highestValue) {
                highestValue = received;
            }
        }

        highestValue += (highestValue / 20);
        int temp = highestValue % 6;
        highestValue += 6 - temp;

        int yStep = (int) MathHelper.gcd(highestValue, 0) / 6;

        // Log("HIGH", "HIGH: " + highestValue + " STEP: " + yStep);
        int rows = highestValue / yStep;



        LineChartView lineChart = (LineChartView) findViewById(R.id.linechart);


        sentDataset.setColor(0x26A69A)
                .setSmooth(true);
                //.setDashed(new float[]{50f,10f});

        receivedDataset.setFill(Color.WHITE)
                .setSmooth(true);

        lineChart.addData(receivedDataset);
        lineChart.addData(sentDataset);

        lineChart.setAxisBorderValues(0, highestValue, yStep)
                .setXAxis(false)
                .setYAxis(false)
                .setLabelsColor(Color.WHITE)
                .setAxisColor(Color.WHITE)
                .setXLabels(XController.LabelPosition.OUTSIDE)
                .setYLabels(YController.LabelPosition.OUTSIDE)
                .setGrid(ChartView.GridType.HORIZONTAL, rows, 1, new Paint())
                .setAxisLabelsSpacing(50);

        smsHistoryChartAnimation(R.id.linechart);

        db.close();


    }

    // Setup the refresh listener for swipe down
    private void setupRefreshListener() {
        // Setup refresh layout listener
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_main);

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        // Log.i("REFRESH", "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        clearBitmaps();
                        setInformation();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );
    }

    // Setup the ad
    private void setupAds() {
        // Setup mobile ads
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    // check battery state
    public void checkBatteryState() {
        /*// Log("CHARGING", "ENTERED");
        if(Build.VERSION.SDK_INT >= 21) {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = this.registerReceiver(null, ifilter);

            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

            String msg = "CHARGING: " + isCharging + "\n" +
                    "USB: " + usbCharge + "\n" +
                    "AC: " + acCharge;

            // Log("CHARGING", msg);

            ActionBar actionBar = getActionBar();

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            if (isCharging) {
                if (acCharge) {
                    window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDarkAc));
                    //setTheme(R.style.AcTheme);
                } else if (usbCharge) {
                    window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDarkUsb));
                    //setTheme(R.style.UsbTheme);
                }
            } else {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                float batteryPct = level / (float) scale;

                if (batteryPct < 5) {
                    try {
                        actionBar.setBackgroundDrawable(new ColorDrawable(0xF44336));
                    } catch (NullPointerException e) {
                        // Log("ACTION_BAR_ERROR", e.toString());
                    }
                    window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDarkCriticalBattery));
                    //setTheme(R.style.CriticalBatteryAppTheme);
                } else if (batteryPct < 20) {
                    window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDarkLowBattery));
                    //setTheme(R.style.LowBatteryTheme);
                } else {
                    window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                    //setTheme(R.style.AppTheme);
                }
            }
        }*/
    }

    // Launch contacts detail activity
    public void smsContactsDetailsClick(View view) {
        Intent intent = new Intent(this, ContactSmsDetailsActivity.class);
        startActivity(intent);
    }

}
