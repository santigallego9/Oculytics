package com.santigallego.oculytics.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

/*
 * Created by santigallego on 9/9/16.
 */
public class FileUtils {

    public FileUtils() {}

    private static final int FILE_SELECT_CODE = 0;

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static void writeToFile(String filename, Activity activity) {

        // Get the directory for the user's public pictures directory.
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "Oculytics");
        if (!dir.mkdirs()) {
            if(!dir.mkdir()) {
                // Log.e("DOWNLOAD", "Directory not created");
            }
        }

        // create the file in which we will write the contents
        File file = new File(dir, filename + ".txt");
        try {
            FileOutputStream os = new FileOutputStream(file);
            String data = DownloadHelper.createString(activity);
            os.write(data.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showFileChooser(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            activity.startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(activity, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
}
