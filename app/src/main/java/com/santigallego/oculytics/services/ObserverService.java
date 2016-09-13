package com.santigallego.oculytics.services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.icu.text.MessageFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.santigallego.oculytics.helpers.Database;
import com.santigallego.oculytics.helpers.Streaks;

import java.util.Set;

public class ObserverService extends Service {

    int id = 0;

    public ObserverService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ContentResolver contentResolver = this.getContentResolver();
        contentResolver.registerContentObserver(Uri.parse("content://sms"),true, new SmsObserver(new Handler()));
        contentResolver.registerContentObserver(Uri.parse("content://mms"),true, new MmsObserver(new Handler()));
    }

    /* this method handles a single incoming request */
    @Override
    public int onStartCommand(Intent intent, int flags, int id) {
        // unpack any parameters that were passed to us
        //ContentResolver contentResolver = this.getContentResolver();
        //contentResolver.registerContentObserver(Uri.parse("content://sms"),true, new SmsObserver(new Handler()));

        // Log.d("OUTGOING", "RUNNING SERVICE");

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
            Cursor cr = ObserverService.this.getContentResolver().query(uriSMSURI, null, null, null, null);
            // this will make it point to the first record, which is the last SMS sent
            cr.moveToNext();

            if (cr.getInt(cr.getColumnIndex("type")) == 2) {
                if(id != cr.getInt(cr.getColumnIndex(cr.getColumnName(0)))) {
                    id = cr.getInt(cr.getColumnIndex(cr.getColumnName(0)));

                    String address = cr.getString(cr.getColumnIndex("address"));
                    boolean contactExists = true;

                    // if contact exists check if streaks needs to be cleared
                    int db_id = Streaks.getContactId(ObserverService.this, address);
                    if(db_id != -1) {
                        contactExists = false;
                        Streaks.checkForStreakClear(ObserverService.this, db_id);
                    }

                    // log message into database

                    Database.messageSent(ObserverService.this, address);

                    // get id if contact did not exist before
                    if(!contactExists) {
                        db_id = Streaks.getContactId(ObserverService.this, address);
                    }

                    // check if streak needs to be updated
                    Streaks.updateStreak(ObserverService.this, db_id);

                    // Log.d("OUTGOING", address);
                } else {
                    // Log.d("OUTGOING", "MESSAGE ALREADY LOGGED");
                }
            };

            cr.close();
        }
    }

    public class MmsObserver extends ContentObserver {

        public MmsObserver(Handler handler) {
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
            Uri uriSMSURI = Uri.parse("content://mms");
            Cursor cr = ObserverService.this.getContentResolver().query(uriSMSURI, null, null, null, null);
            // this will make it point to the first record, which is the last SMS sent
            if(cr.moveToNext()) {

                try {
                    String isIncoming = getIncomingMmsAddress(cr.getInt(cr.getColumnIndex(cr.getColumnName(0))), ObserverService.this);
                    if (isIncoming.equals("insert-address-token")) {
                        String address = getOutgoingMmsAddress(cr.getInt(cr.getColumnIndex(cr.getColumnName(0))));
                        if (address.length() > 1) {
                            if (id != Integer.parseInt(cr.getString(cr.getColumnIndex(cr.getColumnName(0))))) {
                                id = Integer.parseInt(cr.getString(cr.getColumnIndex(cr.getColumnName(0))));

                                boolean contactExists = true;

                                // if contact exists check if streaks needs to be cleared
                                int db_id = Streaks.getContactId(ObserverService.this, address);
                                if (db_id != -1) {
                                    contactExists = false;
                                    Streaks.checkForStreakClear(ObserverService.this, db_id);
                                }

                                // log message into database
                                Database.mmsSent(ObserverService.this, address);

                                // get id if contact did not exist before
                                if (!contactExists) {
                                    db_id = Streaks.getContactId(ObserverService.this, address);
                                }

                                // check if streak needs to be updated
                                Streaks.updateStreak(ObserverService.this, db_id);

                            }
                        }
                    } else {
                        String address = getIncomingMmsAddress(cr.getInt(cr.getColumnIndex(cr.getColumnName(0))), ObserverService.this);
                        if (address.length() > 1) {
                            if (id != Integer.parseInt(cr.getString(cr.getColumnIndex(cr.getColumnName(0))))) {
                                id = Integer.parseInt(cr.getString(cr.getColumnIndex(cr.getColumnName(0))));

                                boolean contactExists = true;

                                // if contact exists check if streaks needs to be cleared
                                int db_id = Streaks.getContactId(ObserverService.this, address);
                                if (db_id != -1) {
                                    contactExists = false;
                                    Streaks.checkForStreakClear(ObserverService.this, db_id);
                                }

                                // log message into database
                                Database.mmsReceived(ObserverService.this, address);

                                // get id if contact did not exist before
                                if (!contactExists) {
                                    db_id = Streaks.getContactId(ObserverService.this, address);
                                }

                                // check if streak needs to be updated
                                Streaks.updateStreak(ObserverService.this, db_id);

                            }
                        }
                    }
                } catch (Exception e) {
                    // Log.d("MMS", "BROKE DOWN");
                }
            }

            cr.close();
        }
    }

    private String getOutgoingMmsAddress(int id) {
        String selectionAdd = "msg_id=" + id;
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

    public static String getIncomingMmsAddress(int id, Service service) {
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

}
