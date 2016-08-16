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
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            //Log.d("OUTGOING", "ENTERD");
            Uri uriSMSURI = Uri.parse("content://sms");
            Cursor cur = SmsOutgoingObserver.this.getContentResolver().query(uriSMSURI, null, null, null, null);
            // this will make it point to the first record, which is the last SMS sent
            cur.moveToNext();
            String address = cur.getColumnName(0) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(1))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(2))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(3))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(4))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(5))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(6))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(7))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(8))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(9))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(10))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(11))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(12))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(13))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(14))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(15))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(16))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(17))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(18))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(19))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(20))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(21))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(22))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(23))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(24))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(25))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(26))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(27))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(28))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(29))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(30))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(31))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(32))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(33))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(34))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(35))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(36))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(37))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(38))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(39))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(40))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(41))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(42))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(43))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(44))) + "\n";
            address += cur.getString(cur.getColumnIndex(cur.getColumnName(45))) + "\n";
            //String address = cur.getString(cur.getColumnIndex("type"));

            Log.d("OUTGOING", address);

            cur.close();

            //Database.messageReceived(SmsOutgoingObserver.this, address);
        }
    }
}
