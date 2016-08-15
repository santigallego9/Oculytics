package com.santigallego.oculytics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by santigallego on 8/14/16.
 */
public class SmsBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SmsMessage[] msgs =
                Telephony.Sms.Intents.getMessagesFromIntent(intent);
        String smsBody = "";
        String address = "";
        for (int i = 0; i < msgs.length; ++i) {
            smsBody = msgs[i].getMessageBody().toString();
            address = msgs[i].getOriginatingAddress();
        }
        String msgText = "FROM: " + address + "\nMESSAGE: " + smsBody;
        Database.messageReceived(context, address);
        Log.d("TEXT_MESSAGE", msgText);
    }
}