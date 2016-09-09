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
    public void onReceive(Context context, Intent intent) {
        SmsMessage[] msgs =
                Telephony.Sms.Intents.getMessagesFromIntent(intent);
        String smsBody = "";
        String address = "";
        for (SmsMessage msg : msgs) {
            smsBody = msg.getMessageBody();
            address = msg.getOriginatingAddress();
        }
        String msgText = "FROM: " + address + "\nMESSAGE: " + smsBody;

        int id = Streaks.getContactId(context, address);
        if(id != -1) {
            Streaks.checkForStreakClear(context, id);
        }
        Database.messageReceived(context, address);
        id = Streaks.getContactId(context, address);
        Streaks.updateStreak(context, id);

        Log.d("TEXT_MESSAGE", msgText);
    }
}