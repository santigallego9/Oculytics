package com.santigallego.oculytics.activities;

import android.Manifest;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
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

import com.santigallego.oculytics.helpers.Database;
import com.santigallego.oculytics.R;
import com.santigallego.oculytics.helpers.Dates;
import com.santigallego.oculytics.helpers.FileUtils;
import com.santigallego.oculytics.helpers.MathHelper;
import com.santigallego.oculytics.helpers.SmsContactDetailsHelper;
import com.santigallego.oculytics.helpers.StartupDialog;
import com.santigallego.oculytics.helpers.Streaks;
import com.santigallego.oculytics.services.ObserverService;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private static final int FILE_SELECT_CODE = 0;
    long startTime, endTime;
    //private static ArrayList<Bitmap> bitmaps = new ArrayList<>();
    //public final static DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startTime = System.nanoTime();

        //Log.d("TIMER", " ");
        //Log.d("TIMER", "Toolbar:");
        // long // start =System.nanoTime();
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        //Log.d("TIMER", "" + time(start, System.nanoTime()));


        //Log.d("TIMER", " ");
        //Log.d("TIMER", "Search view:");
        // start =System.nanoTime();
        try {

            SearchView searchView = (SearchView) findViewById(R.id.action_search);

            int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            TextView textView = (TextView) searchView.findViewById(id);
            textView.setTextColor(Color.BLACK);
        }catch (Exception e) {
            //Log.d("SEARCH", e.toString());
            e.printStackTrace();
        }

        //Log.d("TIMER", "" + time(start, System.nanoTime()));

        /*SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        db.execSQL("DROP TABLE IF EXISTS totals");
        db.execSQL("DROP TABLE IF EXISTS mms_totals");
        db.execSQL("DROP TABLE IF EXISTS contacts");
        db.execSQL("DROP TABLE IF EXISTS streaks");
        db.execSQL("DROP TABLE IF EXISTS sms_sent");
        db.execSQL("DROP TABLE IF EXISTS sms_received");
        db.execSQL("DROP TABLE IF EXISTS mms_sent");
        db.execSQL("DROP TABLE IF EXISTS mms_received");*/

        startupFunctions();
        

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean runStartup = prefs.getBoolean("startup", true);


        if(runStartup) {
            StartupDialog alert = new StartupDialog();
            alert.startupDialog(this);
            setInformation();
        }
    }

    public double time(long startTime, long stopTime) {
        return (double) stopTime / 1000000000.0 - (double) startTime / 1000000000.0;
    }

    @Override
    protected void onResume() {
        super.onResume();

        setInformation();

        endTime = System.nanoTime();

        //Log.d("TIMER", " ");
        //Log.d("TIMER", "TOTAL:");
        //Log.d("TIMER", time(startTime, endTime) + "");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    // Log.d("UPLOAD", "File Uri: " + uri.toString());
                    // Get the path
                    String path = FileUtils.getPath(this, uri);
                    //Log.d("UPLOAD", "File Path: " + path);
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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

            case R.id.action_delete:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...

                new AlertDialog.Builder(this)
                        .setTitle("Clear data")
                        .setMessage("Are you sure you want to delete everything?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SQLiteDatabase db = MainActivity.this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

                                db.execSQL("DROP TABLE IF EXISTS totals");
                                db.execSQL("DROP TABLE IF EXISTS mms_totals");
                                db.execSQL("DROP TABLE IF EXISTS contacts");
                                db.execSQL("DROP TABLE IF EXISTS streaks");
                                db.execSQL("DROP TABLE IF EXISTS sms_sent");
                                db.execSQL("DROP TABLE IF EXISTS sms_received");
                                db.execSQL("DROP TABLE IF EXISTS mms_sent");
                                db.execSQL("DROP TABLE IF EXISTS mms_received");

                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean("startup", true);
                                editor.apply();

                                startupFunctions();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                return true;

            case R.id.action_download:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                /*Intent download = new Intent(this, FileInfoActivity.class);
                download.putExtra("download", true);
                startActivity(download);*/
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
                        //String number = cr.getString(cr.getColumnIndex("number"));

                        Streaks.checkForStreakClear(MainActivity.this, id);
                        Streaks.updateStreak(MainActivity.this, id);

                    } while(cr.moveToNext());
                }
                cr.close();

                db.close();
            }
        };
        thread.run();
    }

    // Run all startup functions SHOULD NOT be called as a refresh
    private void startupFunctions() {

        //Log.d("TIMER", " ");
        //Log.d("TIMER", "Permissions:");
        // long // start =System.nanoTime();
        // Ask for permissions
        permissions();
        //Log.d("TIMER", "" + time(start, System.nanoTime()));


        //Log.d("TIMER", " ");
        //Log.d("TIMER", "Start observer service:");
        // start =System.nanoTime();
        // Startup outgoing messages service
        Intent intent = new Intent(this, ObserverService.class);
        startService(intent);
        //Log.d("TIMER", "" + time(start, System.nanoTime()));


        //Log.d("TIMER", " ");
        //Log.d("TIMER", "Setup Refresh Listener:");
        // start =System.nanoTime();
        setupRefreshListener();
        //Log.d("TIMER", "" + time(start, System.nanoTime()));


        //Log.d("TIMER", " ");
        //Log.d("TIMER", "Populate Database:");
        // start =System.nanoTime();
        Database.populateDatabase(R.raw.seed, this);
        //Log.d("TIMER", "" + time(start, System.nanoTime()));


        //Log.d("TIMER", " ");
        //Log.d("TIMER", "Setup History Chart:");
        // start =System.nanoTime();
        setupHistoryChart();
        //Log.d("TIMER", "" + time(start, System.nanoTime()));


        //Log.d("TIMER", " ");
        //Log.d("TIMER", "Check Battery State:");
        // start =System.nanoTime();
        checkBatteryState();
        //Log.d("TIMER", "" + time(start, System.nanoTime()));


        //Log.d("TIMER", " ");
        //Log.d("TIMER", "Setup Ads:");
        // start =System.nanoTime();
        setupAds();
        //Log.d("TIMER", "" + time(start, System.nanoTime()));
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
        //Log.d("TIMER", " ");
        //Log.d("TIMER", "Check streaks:");
        // long // start =System.nanoTime();
        checkStreaks();
        //Log.d("TIMER", "" + time(start, System.nanoTime()));


        //Log.d("TIMER", " ");
        //Log.d("TIMER", "Setup totals:");
        // start =System.nanoTime();
        setTotals();
        //Log.d("TIMER", "" + time(start, System.nanoTime()));


        //Log.d("TIMER", " ");
        //Log.d("TIMER", "Set top three:");
        // start =System.nanoTime();
        setTopThree();
        //Log.d("TIMER", "" + time(start, System.nanoTime()));
    }

    private void setInformation(SwipeRefreshLayout swipeRefreshLayout) {
        checkStreaks();
        setTotals();
        setTopThree();

        swipeRefreshLayout.setRefreshing(false);
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

                SmsContactDetailsHelper.createContactSmsDetails(this, contact, sentLayout, false);

                /*String msg = "NUMBER: " + number + " \n" +
                             "SENT: " + sent + " \n" +
                             "RECEIVED: " + received;*/
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

                SmsContactDetailsHelper.createContactSmsDetails(this, contact, receivedLayout, false);

                /*String msg = "NUMBER: " + number + " \n" +
                             "SENT: " + sent + " \n" +
                             "RECEIVED: " + received;*/
                // Log("COUNT_TOP_THREE", msg);

            } while (c.moveToNext());
            c.close();
        }

        db.close();
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int days = prefs.getInt("chartHistory", 31);
        //Log.d("SP", ""  + history);

        //int days = 31;
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
                        // clearBitmaps();

                        swipeRefreshLayout.setRefreshing(true);
                        setInformation(swipeRefreshLayout);
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
