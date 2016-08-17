package com.santigallego.oculytics;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class SmsOutgoingObserver extends Service {

    int id = 0;

    public SmsOutgoingObserver() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ContentResolver contentResolver = this.getContentResolver();
        contentResolver.registerContentObserver(Uri.parse("content://sms"),true, new SmsObserver(new Handler()));
    }

    /* this method handles a single incoming request */
    @Override
    public int onStartCommand(Intent intent, int flags, int id) {
        // unpack any parameters that were passed to us
        ContentResolver contentResolver = this.getContentResolver();
        contentResolver.registerContentObserver(Uri.parse("content://sms"),true, new SmsObserver(new Handler()));

        Log.d("OUTGOING", "RUNNING SERVICE");

        return START_STICKY; // stay running
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null; // disable binding
    }

    public class SmsObserver extends ContentObserver {

        public SmsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            //Log.d("OUTGOING", "ENTERD");
            Uri uriSMSURI = Uri.parse("content://sms");
            Cursor cr = SmsOutgoingObserver.this.getContentResolver().query(uriSMSURI, null, null, null, null);
            // this will make it point to the first record, which is the last SMS sent
            cr.moveToNext();

            if (cr.getInt(cr.getColumnIndex("type")) == 2) {
                if(id != cr.getInt(cr.getColumnIndex(cr.getColumnName(0)))) {
                    id = cr.getInt(cr.getColumnIndex(cr.getColumnName(0)));
                    /*String address = cr.getColumnName(0) + ": " + cr.getString(cr.getColumnIndex(cr.getColumnName(0))) + "\n";
                    address += cr.getColumnName(1)  + ": " + cr.getString(cr.getColumnIndex(cr.getColumnName(1))) + "\n";
                    address += cr.getColumnName(2) + ": " + cr.getString(cr.getColumnIndex(cr.getColumnName(2))) + "\n";
                    address += cr.getColumnName(3) + ": " + cr.getString(cr.getColumnIndex(cr.getColumnName(3))) + "\n";
                    address += cr.getColumnName(4) + ": " + cr.getString(cr.getColumnIndex(cr.getColumnName(4))) + "\n";
                    address += cr.getColumnName(5) + ": " + cr.getString(cr.getColumnIndex(cr.getColumnName(5))) + "\n";
                    address += cr.getColumnName(6) + ": " + cr.getString(cr.getColumnIndex(cr.getColumnName(6))) + "\n";
                    address += cr.getColumnName(7) + ": " + cr.getString(cr.getColumnIndex(cr.getColumnName(7))) + "\n";
                    address += cr.getColumnName(8) + ": " + cr.getString(cr.getColumnIndex(cr.getColumnName(8))) + "\n";
                    address += cr.getColumnName(9) + ": " + cr.getString(cr.getColumnIndex(cr.getColumnName(9))) + "\n";
                    address += "\nID: " + id;*/

                    String address = cr.getString(cr.getColumnIndex("address"));
                    Database.messageSent(SmsOutgoingObserver.this, address);
                    Log.d("OUTGOING", address);
                } else {
                    Log.d("OUTGOING", "MESSAGE ALREADY LOGGED");
                }
            };

            cr.close();
        }
    }
}
