package com.santigallego.oculytics.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.santigallego.oculytics.R;
import com.santigallego.oculytics.activities.ContactSpecificsActivity;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

import cn.refactor.library.ShapeImageView;

/*
 * Created by santigallego on 8/20/16.
 */
public class SmsContactDetailsHelper {

    public SmsContactDetailsHelper() {}

    // Create templates for top three card
    public static Bitmap createContactSmsDetails(HashMap<String, String> contact, LinearLayout parent, final Activity activity) {

        Bitmap bitmap = null;

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        View smsDetails = activity.getLayoutInflater().inflate(R.layout.contact_sms_details, null);

        final ShapeImageView contactImage = (ShapeImageView) smsDetails.findViewById(R.id.contact_image);

        final TextView nameView = (TextView) smsDetails.findViewById(R.id.name_view);
        TextView sentView = (TextView) smsDetails.findViewById(R.id.sent_view);
        TextView receivedView = (TextView) smsDetails.findViewById(R.id.received_view);

        Log.d("test", Contacts.getContactPhoto(Long.parseLong(Contacts.searchContactsUsingNumber(contact.get("number"), activity).get("id"))) + "");

        String name = Contacts.searchContactsUsingNumber(contact.get("number"), activity).get("name");

        if (Contacts.hasContactPhoto(Long.parseLong(Contacts.searchContactsUsingNumber(contact.get("number"), activity).get("id")), activity)) {
            try {
                if (ScreenInfo.isPortrait(activity.getResources())) {
                    //Log.d("PICASSO", contacts.get(0).get("id"));
                    Picasso.with(activity)
                            .load(Contacts.getContactPhoto(Long.parseLong(Contacts.searchContactsUsingNumber(contact.get("number"), activity).get("id"))))
                            .resize(width / 8, width / 8)
                            //.transform(new CircleTransform())
                            .into(contactImage);

                } else if (ScreenInfo.isLandscape(activity.getResources())) {
                    Picasso.with(activity)
                            .load(Contacts.getContactPhoto(Long.parseLong(Contacts.searchContactsUsingNumber(contact.get("number"), activity).get("id"))))
                            .resize(height / 8, height / 8)
                            //.transform(new CircleTransform())
                            .into(contactImage);
                }
            } catch (Exception e) {
                Log.d("PICASSO", e.toString());
            }
        } else {
            try {

                if (Character.isLetter(name.charAt(0))) {

                    int bitmapSize;

                    if (ScreenInfo.isPortrait(activity.getResources())) {
                        bitmapSize = width / 8;
                    } else {
                        bitmapSize = height / 8;
                    }

                    bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);
                    bitmap.eraseColor(activity.getResources().getColor(R.color.md_blue));

                    bitmap = drawTextToBitmap(activity, bitmap, name.substring(0, 1));

                    contactImage.setImageBitmap(bitmap);

                } else {

                    if (ScreenInfo.isPortrait(activity.getResources())) {
                        //Log.d("PICASSO", contacts.get(0).get("id"));
                        Picasso.with(activity)
                                .load(R.drawable.default_user_nameless)
                                .resize(width / 8, width / 8)
                                //.transform(new CircleTransform())
                                .into(contactImage);

                    } else if (ScreenInfo.isLandscape(activity.getResources())) {
                        Picasso.with(activity)
                                .load(R.drawable.default_user_nameless)
                                .resize(height / 8, height / 8)
                                //z.transform(new CircleTransform())
                                .into(contactImage);
                    }
                }
            } catch (Exception e) {
                Log.d("PICASSO", e.toString());

                e.printStackTrace();
            }
        }

        nameView.setText(Contacts.searchContactsUsingNumber(contact.get("number"), activity).get("name"));
        sentView.setText(contact.get("sent"));
        receivedView.setText(contact.get("received"));

        parent.addView(smsDetails);

        smsDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = nameView.getText().toString();
                Drawable contactImg = contactImage.getDrawable();

                HashMap<String, String> contact = Contacts.searchContactsUsingName(name, activity);

                Log.d("ON_CLICK_TEST", "ID: " + contact.get("id"));
                Log.d("ON_CLICK_TEST", "Name: " + contact.get("name"));
                Log.d("ON_CLICK_TEST", "Number: " + contact.get("number"));

                Intent intent = new Intent(activity, ContactSpecificsActivity.class);

                if (!contact.isEmpty()) {
                    intent.putExtra("id", contact.get("id"));
                    intent.putExtra("name", contact.get("name"));
                    intent.putExtra("number", contact.get("number"));
                } else {
                    intent.putExtra("id", "-1");
                    intent.putExtra("name", name);
                    intent.putExtra("number", name);
                }

                Bitmap bitmap = ((BitmapDrawable) contactImg).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] b = baos.toByteArray();

                intent.putExtra("contact_image", b);

                activity.startActivity(intent);
            }
        });

        return bitmap;
    }

    public static Bitmap drawTextToBitmap(Activity activity, Bitmap bitmap, String gText) throws IOException {
        Resources resources = activity.getResources();
        float scale = resources.getDisplayMetrics().density;

        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888 ,true);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color
        paint.setColor(activity.getResources().getColor(R.color.white));
        // text size in pixels
        paint.setTextSize((int) (14 * scale));
        // text shadow
        //paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width())/2;
        int y = (bitmap.getHeight() + bounds.height())/2;

        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }
}
