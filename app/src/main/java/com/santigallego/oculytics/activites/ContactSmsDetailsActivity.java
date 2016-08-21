package com.santigallego.oculytics.activites;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.model.ChartSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.BarChartView;
import com.db.chart.view.ChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.santigallego.oculytics.helpers.Charts;
import com.santigallego.oculytics.helpers.Contacts;
import com.santigallego.oculytics.helpers.Database;
import com.santigallego.oculytics.R;
import com.santigallego.oculytics.helpers.SmsContactDetailsHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class ContactSmsDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_sms_details);

        // Setup toolbar actionbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        startupFunctions();
    }

    public void startupFunctions() {
        setList();
        setCompareChart();
        setupRefreshListener();
    }

    public void setInformation() {
        setList();
        setCompareChart();
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
        LinearLayout allLayout = (LinearLayout) findViewById(R.id.all_contacts_container);

        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MODE_PRIVATE, null);

        BarChartView barChart = (BarChartView) findViewById(R.id.sms_details_chart);


        BarSet dataset = new BarSet();

        //barChart.(getResources().getColor(R.color.colorAccent));

        //barChart.color

        String query = "SELECT * FROM contacts ORDER BY sent + received DESC LIMIT 3;";

        Cursor cr = db.rawQuery(query, null);

        int highestValue = 0;

        if (cr.moveToFirst()) {
            do {
                String number = cr.getString(cr.getColumnIndex("number"));
                String sent = cr.getInt(cr.getColumnIndex("sent")) + "";
                String received = cr.getInt(cr.getColumnIndex("received")) + "";

                HashMap<String, String> new_contact = Contacts.searchContacts(number, this);

                int value = Integer.parseInt(received) - Integer.parseInt(sent);

                Bar bar = new Bar(new_contact.get("name"), value);

                if(value < 0) {
                    bar.setColor(Color.GRAY);
                } else {
                    bar.setColor(Color.WHITE);
                }

                if(Math.abs(value) > highestValue) {
                    highestValue = Math.abs(value);
                }

                dataset.addBar(bar);

            } while (cr.moveToNext());
            cr.close();
        }

        int value = highestValue / 10;

        if(value == 0) {
            value++;
        }

        //dataset.setColor(getResources().getColor(R.color.colorAccent));
        barChart.setSetSpacing(10);
        barChart.setRoundCorners(10);
        barChart.setStep(value)
                .setAxisLabelsSpacing(40)
                .setLabelsColor(Color.WHITE)
                .setGrid(ChartView.GridType.HORIZONTAL, new Paint())
                .setXAxis(false)
                .setYAxis(false)
                .setXLabels(XController.LabelPosition.OUTSIDE)
                .setYLabels(YController.LabelPosition.OUTSIDE)
                .setLabelsColor(Color.WHITE)
                .setAxisColor(Color.parseColor("#86705c"))
                .addData(dataset);
        barChart.setSetSpacing(10);

        barChart.show();
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
