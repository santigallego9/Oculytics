package com.santigallego.oculytics.services;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.santigallego.oculytics.helpers.Database;
import com.santigallego.oculytics.helpers.Dates;
import com.santigallego.oculytics.helpers.Streaks;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
 * Created by santigallego on 9/14/16.
 */
public class SetupMessagesService extends Service {

    //Intent progress;

    public final static DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("OUTGOING", "RUNNING SERVICE");

        /*final Intent progress = new Intent();
        progress.setAction("action");*/

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                setupMessages(SetupMessagesService.this, new Intent());
            }
        });
        thread.start();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("CLICKSERVICE", "STARTED");


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*public void sendBroadcast(float percentage, Intent progress) {
        progress.putExtra("percentage", percentage);
        sendBroadcast(progress);
    }*/

    public void setupMessages(Context context, Intent progress) {



        Log.d("HISTORY", "start");
            /*int count = urls.length;Log.d("HISTORY", "working....");
            long totalSize = 0;
            for (int i = 0; i < count; i++) {
                totalSize += Downloader.downloadFile(urls[i]);
                publishProgress((int) ((i / (float) count) * 100));
                // Escape early if cancel() is called
                if (isCancelled()) break;
            }*/

        // this is a change

        int s_id = -1;

        Uri uriSMSURI = Uri.parse("content://sms");
        Cursor cr = context.getContentResolver().query(uriSMSURI, null, null, null, null);

        uriSMSURI = Uri.parse("content://mms");
        Cursor cmms = context.getContentResolver().query(uriSMSURI, null, null, null, null);

        int crc = 0, cmmsc = 0;
        //long totalSize = 0;

        try { crc = cr.getCount(); } catch (Exception e) { e.printStackTrace(); }
        try { cmmsc = cmms.getCount(); } catch (Exception e) { e.printStackTrace(); }

        float total = crc + cmmsc;
        float counter = 0;

        Log.d("HISTORY", "working....");

        // this will make it point to the first record, which is the last SMS sent
        if(cr.moveToLast()) {
            do {
                counter++;
                //totalSize++;
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
                        //smsType = "RECEIVED";
                        //received++;
                        try {

                            boolean contactExists = true;

                            // if contact exists check if streaks needs to be cleared
                            int id = Streaks.getContactId(context, address);
                            if(id != -1) {
                                contactExists = false;
                                Streaks.checkForStreakClear(context, id);
                            }

                            // log message into database
                            Database.messageReceived(context, address, finalDateString);

                            // get id if contact did not exist before
                            if(!contactExists) {
                                id = Streaks.getContactId(context, address);
                            }

                            // check if streak needs to be updated
                            Streaks.updateStreak(context, id);

                        } catch (Exception e) {
                            // Log("MESSAGE_ERROR", "Ignoring this message");
                        }
                    } else if(type == 2) {
                        //smsType = "SENT";
                        //sent++;
                        try {

                            boolean contactExists = true;

                            // if contact exists check if streaks needs to be cleared
                            int id = Streaks.getContactId(context, address);
                            if(id != -1) {
                                contactExists = false;
                                Streaks.checkForStreakClear(context, id);
                            }

                            // log message into database
                            Database.messageSent(context, address, finalDateString);

                            // get id if contact did not exist before
                            if(!contactExists) {
                                id = Streaks.getContactId(context, address);
                            }

                            // check if streak needs to be updated
                            Streaks.updateStreak(context, id);

                        } catch (Exception e) {
                            // Log("MESSAGE_ERROR", "Ignoring this message");
                        }
                    }

                } else {
                    // Log("TESTING", "MESSAGE ALREADY LOGGED");
                }

                float percent = counter / total * 100;
                //sendBroadcast(percent, progress);

            } while(cr.moveToPrevious());

            Log.d("HISTORY", "working....");
        }


        cr.close();

        s_id = -1;

        DateTimeFormatter mmsDtf = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zz yyyy");

        if(cmms.moveToLast()) {
            Log.d("HISTORY", "working....");
            do {
                counter++;
                try {
                    String isIncoming = getIncomingMmsAddress(cmms.getInt(cmms.getColumnIndex(cmms.getColumnName(0))), context);
                    if (isIncoming.equals("insert-address-token")) {
                        String address = getOutgoingMmsAddress(cmms.getInt(cmms.getColumnIndex(cmms.getColumnName(0))), context);
                        if (address.length() > 1) {
                            if (s_id != Integer.parseInt(cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))))) {
                                s_id = Integer.parseInt(cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))));
                                // Log("MMS", cmms.getColumnName(0) + ": " + cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))));
                                // Log("MMS", "OUTGOING: " + getOutgoingMmsAddress(cmms.getInt(cmms.getColumnIndex(cmms.getColumnName(0)))));

                                long timestamp = cmms.getLong(2) * 1000;
                                Date date = new Date(timestamp);
                                String dateString = date.toString();

                                try {
                                    dateString = dtfOut.print(mmsDtf.parseDateTime(dateString));
                                } catch (Exception e) {
                                    Log.d("MMS_SENT", "ERROR: " + e.toString());
                                }

                                //Log.d("MMS_SENT", " ");
                                //Log.d("MMS_SENT", "LOCAL: " + dateString);

                                dateString = Dates.fromLocalToUtc(dateString);

                                //Log.d("MMS_SENT", "  UTC: " + dateString);
                                //Log.d("MMS_SENT", " ");

                                Database.mmsSent(context, address, dateString);


                            }
                        }
                    } else {
                        String address = getIncomingMmsAddress(cmms.getInt(cmms.getColumnIndex(cmms.getColumnName(0))), context);
                        if (address.length() > 1) {
                            if (s_id != Integer.parseInt(cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))))) {
                                s_id = Integer.parseInt(cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))));
                                // Log("MMS", cmms.getColumnName(0) + ": " + cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))));
                                // Log("MMS", "INCOMING: " + getIncomingMmsAddress(cmms.getInt(cmms.getColumnIndex(cmms.getColumnName(0))), context));

                                long timestamp = cmms.getLong(2) * 1000;
                                Date date = new Date(timestamp);
                                String dateString = date.toString();

                                // FORMAT: EEE MMM dd HH:mm:ss zz yyyy

                                //dtfOut.print(date);

                                try {
                                    dateString = dtfOut.print(mmsDtf.parseDateTime(dateString));
                                } catch (Exception e) {
                                    Log.d("MMS_SENT", "ERROR: " + e.toString());
                                }

                                //Log.d("MMS_SENT", " ");
                                //Log.d("MMS_SENT", "LOCAL: " + dateString);

                                dateString = Dates.fromLocalToUtc(dateString);

                                //Log.d("MMS_SENT", "  UTC: " + dateString);
                                //Log.d("MMS_SENT", " ");

                                Database.mmsReceived(context, address, dateString);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Log("MMS", "BROKE DOWN");
                }

                float percent = counter / total * 100;
                //sendBroadcast(percent, progress);

            } while (cmms.moveToPrevious());
            Log.d("HISTORY", "working....");
        }

        cmms.close();

        Log.d("HISTORY", "complete");
    }



    public String getOutgoingMmsAddress(int id, Context activity) {
        String selectionAdd = new String("msg_id=" + id);
        String uriStr = "content://mms/" + id + "/addr";
        Uri uriAddress = Uri.parse(uriStr);
        Cursor cAdd = activity.getContentResolver().query(uriAddress, null,
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

    public String getIncomingMmsAddress(int id, Context service) {
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
