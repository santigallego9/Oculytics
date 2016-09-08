package com.santigallego.oculytics.activities;

import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.BatteryManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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

import com.santigallego.oculytics.helpers.Database;
import com.santigallego.oculytics.R;
import com.santigallego.oculytics.helpers.Dates;
import com.santigallego.oculytics.helpers.MathHelper;
import com.santigallego.oculytics.helpers.SmsContactDetailsHelper;
import com.santigallego.oculytics.services.ObserverService;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private static ArrayList<Bitmap> bitmaps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        Database.populateDatabase(this);
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
            Log.d("TOP_THREE", "Top three does not exist");
        }

        LinearLayout sentLayout = (LinearLayout) findViewById(R.id.sent_containter);

        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        String query = "SELECT * FROM contacts ORDER BY sent DESC, received DESC LIMIT 3;";

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

                Bitmap bitmap = SmsContactDetailsHelper.createContactSmsDetails(contact, sentLayout, this);
                if(bitmap != null) {
                    bitmaps.add(bitmap);
                }

                String msg = "NUMBER: " + number + " \n" +
                             "SENT: " + sent + " \n" +
                             "RECEIVED: " + received;

                Log.d("COUNT_TOP_THREE", msg);

            } while (cr.moveToNext());
            cr.close();
        }

        LinearLayout receivedLayout = (LinearLayout) findViewById(R.id.received_containter);

        query = "SELECT * FROM contacts ORDER BY received DESC, sent DESC LIMIT 3;";

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

                Bitmap bitmap = SmsContactDetailsHelper.createContactSmsDetails(contact, receivedLayout, this);
                if(bitmap != null) {
                    bitmaps.add(bitmap);
                }

                String msg = "NUMBER: " + number + " \n" +
                             "SENT: " + sent + " \n" +
                             "RECEIVED: " + received;

                Log.d("COUNT_TOP_THREE", msg);

            } while (c.moveToNext());
            c.close();
        }
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
            Log.d("TEST", "ENTERED");

            String new_date = Dates.timeBefore(0, 0, 0, i, 0, 0, 0, date);
            String last_date = Dates.timeBefore(0, 0, 0, i + 1, 0, 0, 0, date);

            String query = "SELECT * FROM sms_sent WHERE sent_on <= '" + new_date + "' AND sent_on >= '" + last_date + "';";
            Cursor cr = db.rawQuery(query, null);
            int sent = cr.getCount();
            Log.d("DEBUG", "SENT: " + sent);
            cr.close();

            query = "SELECT * FROM sms_received WHERE received_on <= '" + new_date + "' AND received_on >= '" + last_date + "';";
            Cursor c = db.rawQuery(query, null);
            int received = c.getCount();
            Log.d("DEBUG", "RECEIVED: " + received);
            c.close();

            String label = Dates.toDisplay(new_date);

            Log.d("COUNT", label + ", SENT: " + sent + ", RECEIVED: " + received);

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

        Log.d("HIGH", "HIGH: " + highestValue + " STEP: " + yStep);
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


    }

    // Setup the refresh listener for swipe down
    private void setupRefreshListener() {
        // Setup refresh layout listener
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_main);

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("REFRESH", "onRefresh called from SwipeRefreshLayout");

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
        /*Log.d("CHARGING", "ENTERED");
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

            Log.d("CHARGING", msg);

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
                        Log.d("ACTION_BAR_ERROR", e.toString());
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
