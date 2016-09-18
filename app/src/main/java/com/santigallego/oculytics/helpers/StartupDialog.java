package com.santigallego.oculytics.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.santigallego.oculytics.R;
import com.santigallego.oculytics.activities.MainActivity;
import com.santigallego.oculytics.services.SetupMessagesService;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/*
 * Created by santigallego on 9/14/16.
 */
public class StartupDialog {
    
    //private Activity activity;
    TextView percentage;
    Dialog loadingDialog;
    public final static DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private static final String TAG = "BroadcastTest";
    private Intent intent;

    public void startupDialog(final Activity activity) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        final SharedPreferences.Editor editor = prefs.edit();



        //activity = activity;
        final android.app.Dialog dialog = new android.app.Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.startup_dialog);

        Button importButton = (Button) dialog.findViewById(R.id.import_button);
        Button historyButton = (Button) dialog.findViewById(R.id.history_button);
        Button neitherButton = (Button) dialog.findViewById(R.id.neither_button);

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editor.putBoolean("startup", false);
                editor.apply();

                FileUtils.showFileChooser(activity);

                loadingDialog(activity);

                dialog.dismiss();
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                loadingDialog(activity);

                dialog.dismiss();
            }
        });

        neitherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editor.putBoolean("startup", false);
                editor.apply();

                dialog.dismiss();
            }
        });

        dialog.show();

    }

    public void doneDialog(final Activity activity) {

        //activity = activity;
        final android.app.Dialog dialog = new android.app.Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.done_dialog);

        Button ok_button = (Button) dialog.findViewById(R.id.ok_button);

        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();
            }
        });

        dialog.show();

    }

    public void loadingDialog(final Activity activity) {

        new SetupMessagesTask().execute(activity);

        //activity = activity;
        final android.app.Dialog dialog = new android.app.Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.loading_dialog);

        loadingDialog = dialog;

        dialog.show();
    }

    
    /*public void setupMessages(Activity activity) {
        //Activity activity = MainActivity.this;


        final android.app.Dialog dialog = new android.app.Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.loading_dialog);
        Dialog loadingDialog = dialog;

        percentage = (TextView) dialog.findViewById(R.id.progress_text);

        dialog.show();


        // Do the long-running work in her
    }*/


    public TextView getPercentage() {
        return this.percentage;
    }

    private class SetupMessagesTask extends AsyncTask<Activity, Integer, Long> {
        protected Long doInBackground(Activity... activities) {
            /*int count = urls.length;
            long totalSize = 0;
            for (int i = 0; i < count; i++) {
                totalSize += Downloader.downloadFile(urls[i]);
                publishProgress((int) ((i / (float) count) * 100));
                // Escape early if cancel() is called
                if (isCancelled()) break;
            }*/

            // this is a change

            Log.d("HISTORY_START", "starting....");

            int s_id = -1, sent = 0, received = 0;

            Uri uriSMSURI = Uri.parse("content://sms");
            Cursor cr = activities[0].getContentResolver().query(uriSMSURI, null, null, null, null);

            uriSMSURI = Uri.parse("content://mms");
            Cursor cmms = activities[0].getContentResolver().query(uriSMSURI, null, null, null, null);

            int crc = 0, cmmsc = 0;
            long totalSize = 0;

            try { crc = cr.getCount(); } catch (Exception e) { e.printStackTrace(); }
            try { cmmsc = cmms.getCount(); } catch (Exception e) { e.printStackTrace(); }

            int total = crc + cmmsc;
            double counter = 0;


            // this will make it point to the first record, which is the last SMS sent
            if(cr.moveToLast()) {
                do {
                    counter++;
                    totalSize++;
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
                            received++;
                            try {

                                boolean contactExists = true;


                                // log message into database
                                Database.messageReceived(activities[0], address, finalDateString);

                            } catch (Exception e) {
                                // Log("MESSAGE_ERROR", "Ignoring this message");
                            }
                        } else if(type == 2) {
                            //smsType = "SENT";
                            sent++;
                            try {

                                boolean contactExists = true;

                                // if contact exists check if streaks needs to be cleared
                                // log message into database
                                Database.messageSent(activities[0], address, finalDateString);

                                // get id if contact did not exist before

                            } catch (Exception e) {
                                // Log("MESSAGE_ERROR", "Ignoring this message");
                            }
                        }

                    } else {
                        // Log("TESTING", "MESSAGE ALREADY LOGGED");
                    }
                    //publishProgress((int)counter);
                } while(cr.moveToPrevious());
            }


            cr.close();



            s_id = -1;

            if(cmms.moveToLast()) {
                do {
                    counter++;
                    try {
                        String isIncoming = getIncomingMmsAddress(cmms.getInt(cmms.getColumnIndex(cmms.getColumnName(0))), activities[0]);
                        if (isIncoming.equals("insert-address-token")) {
                            String address = getOutgoingMmsAddress(cmms.getInt(cmms.getColumnIndex(cmms.getColumnName(0))), activities[0]);
                            if (address.length() > 1) {
                                if (s_id != Integer.parseInt(cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))))) {
                                    s_id = Integer.parseInt(cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))));
                                    // Log("MMS", cmms.getColumnName(0) + ": " + cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))));
                                    // Log("MMS", "OUTGOING: " + getOutgoingMmsAddress(cmms.getInt(cmms.getColumnIndex(cmms.getColumnName(0)))));

                                    Database.mmsSent(activities[0], address);


                                }
                            }
                        } else {
                            String address = getIncomingMmsAddress(cmms.getInt(cmms.getColumnIndex(cmms.getColumnName(0))), activities[0]);
                            if (address.length() > 1) {
                                if (s_id != Integer.parseInt(cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))))) {
                                    s_id = Integer.parseInt(cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))));
                                    // Log("MMS", cmms.getColumnName(0) + ": " + cmms.getString(cmms.getColumnIndex(cmms.getColumnName(0))));
                                    // Log("MMS", "INCOMING: " + getIncomingMmsAddress(cmms.getInt(cmms.getColumnIndex(cmms.getColumnName(0))), activities[0]));

                                    Database.mmsReceived(activities[0], address);
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Log("MMS", "BROKE DOWN");
                    }
                    //publishProgress((int)counter);
                } while (cmms.moveToPrevious());
            }

            cmms.close();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activities[0]);
            final SharedPreferences.Editor editor = prefs.edit();

            editor.putBoolean("startup", false);
            editor.apply();


            try {
                loadingDialog.dismiss();
            } catch (Exception e) {
                Log.d("HISTORY_CLICK", e.toString());
            }

            return totalSize;
        }

        public String getOutgoingMmsAddress(int id, Activity activity) {
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

        public String getIncomingMmsAddress(int id, Activity service) {
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

        // This is called each time you call publishProgress()
        protected void onProgressUpdate(Double... progress) {

            //double percent = progress[0] / (double) total * 100;

            // Log("PERCENT", "" + percent);
        }

        // This is called when doInBackground() is finished
        protected void onPostExecute(Long result) {
            //setInformation();
        }
    }
}