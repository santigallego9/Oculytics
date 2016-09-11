package com.santigallego.oculytics.helpers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;

import com.santigallego.oculytics.activities.MainActivity;

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
            // Log.e("CONTACT", e.toString());
            return false;
        }
    }


    public static Uri getContactPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);

        return photoUri;
    }

    public static HashMap<String, String> searchContactsUsingNumber(String number, Activity activity) {

        HashMap<String, String> contact = new HashMap<>();

        number = PhoneNumbers.formatNumber(number, false);

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        String name = number;
        boolean failed = true;

        ContentResolver contentResolver = activity.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID, ContactsContract.PhoneLookup.NUMBER,
                ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                contact.put("id", contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID)));
                contact.put("name", contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)));
                contact.put("number", PhoneNumbers.formatNumber(contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.PhoneLookup.NUMBER)), false));

                failed = false;
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        if(failed) {

            String tempnumber = PhoneNumbers.formatNumber(number, false);

            uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(tempnumber));

            contentResolver = activity.getContentResolver();
            Cursor contactLookupNoAreaCode = contentResolver.query(uri, new String[] {BaseColumns._ID, ContactsContract.PhoneLookup.NUMBER,
                    ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

            try {
                if (contactLookupNoAreaCode != null && contactLookupNoAreaCode.getCount() > 0) {
                    contactLookupNoAreaCode.moveToNext();
                    contact.put("id", contactLookupNoAreaCode.getString(contactLookupNoAreaCode.getColumnIndex(BaseColumns._ID)));
                    contact.put("name", contactLookupNoAreaCode.getString(contactLookupNoAreaCode.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)));
                    contact.put("number", PhoneNumbers.formatNumber(contactLookupNoAreaCode.getString(contactLookupNoAreaCode.getColumnIndex(ContactsContract.PhoneLookup.NUMBER)), false));

                    failed = false;
                }
            } finally {
                if (contactLookupNoAreaCode != null) {
                    contactLookupNoAreaCode.close();
                }
            }

            if(failed) {
                contact.put("id", "-1");
                contact.put("name", number);
                contact.put("number", number);
            }
        }

        return contact;
    }

    public static HashMap<String, String> searchContactsUsingName(String name, Activity activity) {


        HashMap<String, String> contact = new HashMap<>();

        //String number = null;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%" + name +"%'";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null);

        if (c.moveToFirst()) {
            name = c.getString(0);
            contact = searchContactsUsingNumber(name, activity);
        } else {
            name = PhoneNumbers.formatNumber(name, false);
            contact.put("id", "-1");
            contact.put("name", name);
            contact.put("number", name);
        }

        return contact;
    }
}
