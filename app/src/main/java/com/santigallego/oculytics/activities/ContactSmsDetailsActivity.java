package com.santigallego.oculytics.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.view.BarChartView;
import com.db.chart.view.ChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.santigallego.oculytics.helpers.Contacts;
import com.santigallego.oculytics.helpers.Database;
import com.santigallego.oculytics.R;
import com.santigallego.oculytics.helpers.Dates;
import com.santigallego.oculytics.helpers.SmsContactDetailsHelper;

import org.joda.time.DateTime;

import java.util.HashMap;

public class ContactSmsDetailsActivity extends AppCompatActivity {

    int[] entries = new int[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_sms_details);

        // Setup toolbar actionbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        try {
            String start = Dates.dtfOut.print(new DateTime());

            //Log.d("DATETIME_TEST", "BEFORE: " + start);

            String test = Dates.timeAgo(0, 6, 3, 24, 0, 0, 0);

            //Log.d("DATETIME_TEST", "AFTER:  " + test);

            String text = Dates.toDisplay(test);

            Log.d("DATETIME_TEST", "TEST:  " + text);


        } catch (Exception e) {
            Log.d("DATETIME_TEST", e.toString());
        }

        startupFunctions();
    }

    public void startupFunctions() {
        setList();
        setCompareChart();
        setupRefreshListener();
    }

    public void setInformation() {
        setList();
        // updateCompareChart();
    }

    public void setupRefreshListener() {
        // Setup refresh layout listener
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_sms_details);

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

    public void setCompareChart() {

        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MODE_PRIVATE, null);

        BarChartView barChart = (BarChartView) findViewById(R.id.sms_details_chart);


        BarSet dataset = new BarSet();

        //barChart.(getResources().getColor(R.color.colorAccent));

        //barChart.color

        String query = "SELECT * FROM contacts ORDER BY sent + received DESC LIMIT 3;";

        Cursor cr = db.rawQuery(query, null);

        int highestValue = 0;
        int i = 0;
        if (cr.moveToFirst()) {
            do {
                String number = cr.getString(cr.getColumnIndex("number"));
                String sent = cr.getInt(cr.getColumnIndex("sent")) + "";
                String received = cr.getInt(cr.getColumnIndex("received")) + "";

                HashMap<String, String> new_contact = Contacts.searchContacts(number, this);

                float floatValue = Integer.parseInt(received) - Integer.parseInt(sent);
                int intValue = Integer.parseInt(received) - Integer.parseInt(sent);

                if(intValue == 0) {
                    floatValue += .1f;
                }

                entries[i] = intValue;

                Bar bar = new Bar(new_contact.get("name"), floatValue);

                bar.setColor(Color.WHITE);

                if(Math.abs(intValue) > highestValue) {
                    highestValue = Math.abs(intValue);
                }

                dataset.addBar(bar);
                i++;
            } while (cr.moveToNext());
            cr.close();
        }

        int step = highestValue / 4;
        highestValue++;
        int lowestValue = -highestValue;
        int rows = (highestValue / step) * 2;

        if(step == 0) {
            step++;
        }

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);

        // display methods
        barChart.setSetSpacing(10);
        barChart.setRoundCorners(10);
        barChart.setBarSpacing(200);

        barChart.setContentDescription("THIS IS A TEST");

        // data methods
        barChart.setAxisLabelsSpacing(40)
                .setAxisBorderValues(lowestValue, highestValue, step)
                .setLabelsColor(Color.WHITE)
                .setAxisColor(Color.WHITE)
                .setXLabels(XController.LabelPosition.OUTSIDE)
                .setYLabels(YController.LabelPosition.OUTSIDE)
                .setGrid(ChartView.GridType.HORIZONTAL, rows, 1, paint)
                .setXAxis(false)
                .setYAxis(true)
                .addData(dataset);

        // show the chart
        barChart.show();

        barChart.setOnEntryClickListener(new OnEntryClickListener() {
            @Override
            public void onClick(int setIndex, int entryIndex, Rect rect) {
                Toast.makeText(ContactSmsDetailsActivity.this, "Difference: " + entries[entryIndex], Toast.LENGTH_SHORT).show();
            }
        });
    }

    // TODO update is not currently working
    public void updateCompareChart() {

        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MODE_PRIVATE, null);

        BarChartView barChart = (BarChartView) findViewById(R.id.sms_details_chart);


        BarSet dataset = new BarSet();

        //barChart.(getResources().getColor(R.color.colorAccent));

        //barChart.color

        String query = "SELECT * FROM contacts ORDER BY sent + received DESC LIMIT 3;";

        Cursor cr = db.rawQuery(query, null);

        int i = 0;
        float values [] = new float[3];
        if (cr.moveToFirst()) {
            do {
                // String number = cr.getString(cr.getColumnIndex("number"));
                String sent = cr.getInt(cr.getColumnIndex("sent")) + "";
                String received = cr.getInt(cr.getColumnIndex("received")) + "";

                // HashMap<String, String> new_contact = Contacts.searchContacts(number, this);

                values[i] = Integer.parseInt(received) - Integer.parseInt(sent);

            } while (cr.moveToNext());
            cr.close();
        }

        barChart.updateValues(0, values);

        barChart.notifyDataUpdate();

    }

    public void setList() {

        LinearLayout allLayout = (LinearLayout) findViewById(R.id.all_contacts_container);

        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MODE_PRIVATE, null);

        String query = "SELECT * FROM contacts ORDER BY sent + received DESC LIMIT 10;";

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

                SmsContactDetailsHelper.createContactSmsDetails(contact, allLayout, this);


            } while (cr.moveToNext());
            cr.close();
        }
    }
}
