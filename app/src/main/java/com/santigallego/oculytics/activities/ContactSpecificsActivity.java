package com.santigallego.oculytics.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.LinearEase;
import com.santigallego.oculytics.R;
import com.santigallego.oculytics.helpers.Database;
import com.santigallego.oculytics.helpers.Dates;
import com.santigallego.oculytics.helpers.MathHelper;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class ContactSpecificsActivity extends AppCompatActivity {

    String id, name, number;
    int contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_specifics);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Intent intent = getIntent();

        id = intent.getStringExtra("id");
        name = intent.getStringExtra("name");
        number = intent.getStringExtra("number");

        getSupportActionBar().setTitle(name);

        setTotals();
        setupHistoryChart();

    }

    private void setTotals() {
        //Layout layout = (Layout) findViewById(R.id.)
        SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        // UPDATE totals SET received = received + 1
        String query = "SELECT * FROM contacts WHERE number = " + number + " LIMIT 1;";

        Cursor cr = db.rawQuery(query, null);

        int sms_sent = 0, sms_received = 0, mms_sent = 0, mms_received = 0;

        if (cr.moveToFirst()) {
            contactId = cr.getInt(cr.getColumnIndex("id"));
            sms_sent = cr.getInt(cr.getColumnIndex("sent"));
            sms_received = cr.getInt(cr.getColumnIndex("received"));
            mms_sent = cr.getInt(cr.getColumnIndex("sent_mms"));
            mms_received = cr.getInt(cr.getColumnIndex("received_mms"));
            cr.close();
        }

        TextView sentText = (TextView) findViewById(R.id.totals_sent_text);
        TextView recText = (TextView) findViewById(R.id.totals_rec_text);

        String sent = sms_sent + "";
        String received = sms_received + "";

        sentText.setText(sent);
        recText.setText(received);

        sentText = (TextView) findViewById(R.id.mms_totals_sent_text);
        recText = (TextView) findViewById(R.id.mms_totals_rec_text);

        sent = mms_sent + "";
        received = mms_received + "";

        sentText.setText(sent);
        recText.setText(received);
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

            String query = "SELECT * FROM sms_sent WHERE sent_on <= '" + new_date + "' AND sent_on >= '" + last_date + "' AND contact_id = " + contactId + ";";
            Cursor cr = db.rawQuery(query, null);
            int sent = cr.getCount();
            Log.d("DEBUG", "SENT: " + sent);
            cr.close();

            query = "SELECT * FROM sms_received WHERE received_on <= '" + new_date + "' AND received_on >= '" + last_date + "' AND contact_id = " + contactId + ";";
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
        highestValue -= highestValue % 4;
        if(highestValue < 4) {highestValue = 4;}

        int yStep = (int) MathHelper.gcd(highestValue, 0) / 4;

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
}
