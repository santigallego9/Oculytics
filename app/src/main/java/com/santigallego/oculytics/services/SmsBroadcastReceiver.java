package com.santigallego.oculytics.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.santigallego.oculytics.helpers.Database;
import com.santigallego.oculytics.helpers.Streaks;

/*
 * Created by Santi Gallego on 8/14/16.
 */
public class SmsBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SmsMessage[] msgs =
                        Telephony.Sms.Intents.getMessagesFromIntent(intent);
                String smsBody = "";
                String address = "";
                for (SmsMessage msg : msgs) {
                    smsBody = msg.getMessageBody();
                    address = msg.getOriginatingAddress();
                }
                String msgText = "FROM: " + address + "\nMESSAGE: " + smsBody;

                // log message into database
                Database.messageReceived(context, address);

            }
        });
        thread.start();
    }
}