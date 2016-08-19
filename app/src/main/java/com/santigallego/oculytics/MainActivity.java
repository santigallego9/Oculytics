package com.santigallego.oculytics;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar actionbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        startupFunctions();

    }

    private void startupFunctions() {

        // Ask for permissions
        permissions();

        // Startup outgoing messages service
        Intent intent = new Intent(this, SmsOutgoingObserver.class);
        startService(intent);

        setupRefreshListener();
        populateDatabase();
        setInformation();
        setupAds();
    }

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

        sentText.setText(total_sent + "");
        recText.setText(total_received + "");

        setSMSHistoryChartData();
    }

    public void setSMSHistoryChartData() {

        LineChart lineChart = (LineChart) findViewById(R.id.chart);

        List<Entry> entries = new ArrayList<Entry>();

        entries.add(new Entry(1, 21));
        entries.add(new Entry(2, 37));
        entries.add(new Entry(3, 50));
        entries.add(new Entry(4, 20));
        entries.add(new Entry(5, 23));
        entries.add(new Entry(6, 56));
        entries.add(new Entry(7, 23));
        entries.add(new Entry(8, 57));

        LineDataSet dataSet = new LineDataSet(entries, "Label");

        YAxis yAxis = lineChart.getAxisLeft();
        YAxis yAxisRight = lineChart.getAxisRight();
        XAxis xAxis = lineChart.getXAxis();

        yAxisRight.setEnabled(false);
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(false);
        yAxis.setSpaceBottom(5);

        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        LineData lineData = new LineData(dataSet);
        lineChart.setDescription(null);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    public void setTopThree() {
        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        String query = "SELECT * FROM contacts ORDER BY sent DESC LIMIT 3;";

        Cursor cr = db.rawQuery(query, null);

        ArrayList<HashMap> topSent = new ArrayList<>();

        if (cr.moveToFirst()) {
            do {
                HashMap<String, String> contact = new HashMap<>();

                String number = cr.getString(cr.getColumnIndex("number"));
                String sent = cr.getInt(cr.getColumnIndex("sent")) + "";
                String received = cr.getInt(cr.getColumnIndex("received")) + "";

                contact.put("number", number);
                contact.put("sent", sent);
                contact.put("received", received);

                topSent.add(contact);

            } while (cr.moveToNext());
            cr.close();
        }

        query = "SELECT * FROM contacts ORDER BY received DESC LIMIT 3;";

        Cursor c = db.rawQuery(query, null);

        ArrayList<HashMap> topReceived = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                HashMap<String, String> contact = new HashMap<>();

                String number = c.getString(c.getColumnIndex("number"));
                String sent = c.getInt(c.getColumnIndex("sent")) + "";
                String received = c.getInt(c.getColumnIndex("received")) + "";

                contact.put("number", number);
                contact.put("sent", sent);
                contact.put("received", received);

                topReceived.add(contact);

            } while (c.moveToNext());
            c.close();
        }
    }

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

    public void populateDatabase() {
        // Populate database if empty
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

    public void setupAds() {
        // Setup mobile ads
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void testClick(View view) {
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);
    }
}
