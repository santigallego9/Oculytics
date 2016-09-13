package com.santigallego.oculytics.helpers;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.santigallego.oculytics.R;
import com.santigallego.oculytics.activities.ContactSpecificsActivity;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import cn.refactor.library.ShapeImageView;

/*
 * Created by santigallego on 8/20/16.
 */
public class SmsContactDetailsHelper {

    public SmsContactDetailsHelper() {}

    // Create templates for top three card
    public static Bitmap createContactSmsDetails(final Activity activity, HashMap<String, String> contact, LinearLayout parent, Boolean full) {

        Bitmap bitmap = null;

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int width = size.x;
        final int height = size.y;

        View smsDetails;

        if(full) {
            smsDetails = activity.getLayoutInflater().inflate(R.layout.template_contact_sms_details_full, null);
        } else {
            smsDetails = activity.getLayoutInflater().inflate(R.layout.template_contact_sms_details, null);
        }

        final ShapeImageView contactImage = (ShapeImageView) smsDetails.findViewById(R.id.contact_image);
        final ShapeImageView streakCounter = (ShapeImageView) smsDetails.findViewById(R.id.streak);

        final TextView nameView = (TextView) smsDetails.findViewById(R.id.name_view);
        TextView sentView = (TextView) smsDetails.findViewById(R.id.sent_view);
        TextView receivedView = (TextView) smsDetails.findViewById(R.id.received_view);

        Uri contactImageUri = null; //Contacts.getContactPhoto(Long.parseLong(Contacts.searchContactsUsingNumber(contact.get("number"), activity).get("id")));
        Boolean contactHasImage = Contacts.hasContactPhoto(Long.parseLong(Contacts.searchContactsUsingNumber(contact.get("number"), activity).get("id")), activity);

        if(contactHasImage) {
            contactImageUri = Contacts.getContactPhoto(Long.parseLong(Contacts.searchContactsUsingNumber(contact.get("number"), activity).get("id")));
            /*try {
                Bitmap image = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), contactImageUri);
                if(image == null) {
                    contactHasImage = false;
                    image.recycle();
                }
            } catch (Exception e) {
                Log.d("ERROR", e.toString());
                e.printStackTrace();
            }*/
        }

        // Log.d("test", Contacts.getContactPhoto(Long.parseLong(Contacts.searchContactsUsingNumber(contact.get("number"), activity).get("id"))) + "");

        String name = Contacts.searchContactsUsingNumber(contact.get("number"), activity).get("name");

        if(full) {
            TextView numberView = (TextView) smsDetails.findViewById(R.id.number_view);

            numberView.setText(contact.get("number"));
        }

        // Log.d("PICASSO", " ");
        // Log.d("PICASSO", "NAME: " + name + "\nNUMBER: " + contact.get("number") + "\nID: " + Long.parseLong(Contacts.searchContactsUsingNumber(contact.get("number"), activity).get("id")));

        if (contactHasImage) {
            try {
                // Log.d("PICASSO", "CONTACT " + name + " HAS PHOTO");
                if (ScreenInfo.isPortrait(activity.getResources())) {
                    //Log.d("PICASSO", contacts.get(0).get("id"));
                    Picasso.with(activity)
                            .load(contactImageUri)
                            .resize(width / 8, width / 8)
                            //.transform(new CircleTransform())
                            .into(contactImage);

                } else if (ScreenInfo.isLandscape(activity.getResources())) {
                    Picasso.with(activity)
                            .load(contactImageUri)
                            .resize(height / 8, height / 8)
                            //.transform(new CircleTransform())
                            .into(contactImage);
                }
            } catch (Exception e) {
                // Log.d("PICASSO", "CONTACT " + name + " HAS PHOTO: " + e.toString());
            }
        } else {
            try {



                if (Character.isLetter(name.charAt(0))) {

                    // Log.d("PICASSO", "CONTACT " + name + " DOES NOT HAVE PHOTO - LETTER");

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

                    // Log.d("PICASSO", "CONTACT " + name + " DOES NOT HAVE PHOTO - NO LETTER");

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
                // Log.d("PICASSO", "CONTACT " + name + " DOES NOT HAVE PHOTO " + e.toString());

                e.printStackTrace();
            }
        }


        try {
            bitmap = ((BitmapDrawable) contactImage.getDrawable()).getBitmap();
        } catch (NullPointerException e) {
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
        }

        /*try {
            Bitmap image = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), contactImageUri);
            if(image == null) {
                contactHasImage = false;
                image.recycle();
            }
        } catch (Exception e) {
            Log.d("ERROR", e.toString());
            e.printStackTrace();
        }*/

        // Log.d("PICASSO", " ");

        int streak = Streaks.getStreak(activity, Streaks.getContactId(activity, contact.get("number")));
        // Log.d("STREAK_COUNTER", Contacts.searchContactsUsingNumber(contact.get("number"), activity).get("name") + " - " + streak);

        if(streak >= 2) {
            streakCounter.setVisibility(View.VISIBLE);
            streakCounter.setText("" + streak);
        } else {
            streakCounter.setVisibility(View.VISIBLE);
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

                // Log.d("ON_CLICK_TEST", "ID: " + contact.get("id"));
                // Log.d("ON_CLICK_TEST", "Name: " + contact.get("name"));
                // Log.d("ON_CLICK_TEST", "Number: " + contact.get("number"));

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

                Bitmap bitmap;

                try {
                    bitmap = ((BitmapDrawable) contactImg).getBitmap();
                } catch (NullPointerException e) {
                    int bitmapSize;

                    if (ScreenInfo.isPortrait(activity.getResources())) {
                        bitmapSize = width / 8;
                    } else {
                        bitmapSize = height / 8;
                    }

                    bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);
                    bitmap.eraseColor(activity.getResources().getColor(R.color.md_blue));

                    bitmap = drawTextToBitmap(activity, bitmap, name.substring(0, 1));
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] b = baos.toByteArray();

                intent.putExtra("contact_image", b);

                activity.startActivity(intent);
            }
        });

        return bitmap;
    }

    public static void setContactImage(Activity activity, Boolean contactHasImage, HashMap<String, String> contact, Uri contactImageuri) {

    }

    public static Bitmap drawTextToBitmap(Activity activity, Bitmap bitmap, String gText) {
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
