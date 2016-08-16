package com.santigallego.oculytics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

/*
 * Created by santigallego on 8/14/16.
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
        Database.messageReceived(context, address);
        Log.d("TEXT_MESSAGE", msgText);
    }
}