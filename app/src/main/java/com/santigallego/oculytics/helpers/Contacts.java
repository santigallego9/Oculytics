package com.santigallego.oculytics.helpers;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.santigallego.oculytics.activites.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;

/*
 * Created by santigallego on 8/19/16.
 */
public class Contacts {

    public Contacts() {}

    public static boolean hasContactPhoto(long contactId, Activity activity) {
        try {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            Cursor cursor = activity.getContentResolver().query(photoUri,
                    new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
            if (cursor == null) {
                return false;
            }
            try {
                if (cursor.moveToFirst()) {
                    byte[] data = cursor.getBlob(0);
                    if (data != null) {
                        return true;
                    }
                }
            } finally {
                cursor.close();
            }
            return false;
        } catch (Exception e) {
            Log.e("CONTACT", e.toString());
            return false;
        }
    }


    public static Uri getContactPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);

        return photoUri;
    }

    public static HashMap<String, String> searchContacts(String number, Activity activity) {

        SQLiteDatabase db = activity.openOrCreateDatabase(Database.DATABASE_NAME, MainActivity.MODE_PRIVATE, null);

        HashMap<String, String> map = new HashMap<>();

        number = PhoneNumbers.formatNumber(number, false);
        String tempNumber = PhoneNumbers.formatNumber(number, true);

        boolean found = false;

        String query = "SELECT * FROM phone_contacts WHERE number = " + number + ";";

        Cursor cr = db.rawQuery(query, null);

        if(cr.moveToFirst()) {
            String s_phone_id = cr.getString(cr.getColumnIndex("phone_id"));
            String s_name = cr.getString(cr.getColumnIndex("name"));
            String s_phoneNumber = cr.getString(cr.getColumnIndex("number"));

            map.put("id", s_phone_id);
            map.put("name", s_name);
            map.put("number", s_phoneNumber);

        } else {
            query = "SELECT * FROM phone_contacts WHERE number = " + tempNumber + ";";

            Cursor c = db.rawQuery(query, null);

            if(c.moveToFirst()) {
                String s_phone_id = c.getString(c.getColumnIndex("phone_id"));
                String s_name = c.getString(c.getColumnIndex("name"));
                String s_phoneNumber = c.getString(c.getColumnIndex("number"));

                map.put("id", s_phone_id);
                map.put("name", s_name);
                map.put("number", s_phoneNumber);
            } else {
                map.put("id", "-1");
                map.put("name", number);
                map.put("number", number);
            }
            c.close();
        }
        cr.close();

        return map;
    }
}
