package com.santigallego.oculytics.helpers;

import android.app.Activity;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.santigallego.oculytics.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

/*
 * Created by santigallego on 8/20/16.
 */
public class SmsContactDetailsHelper {

    public SmsContactDetailsHelper() {}


    // Create templates for top three card
    public static void createContactSmsDetails(HashMap<String, String> contact, LinearLayout parent, Activity activity) {

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        View smsDetails = activity.getLayoutInflater().inflate(R.layout.contact_sms_details, null);

        final ImageView contactImage = (ImageView) smsDetails.findViewById(R.id.contact_image);

        TextView nameView = (TextView) smsDetails.findViewById(R.id.name_view);
        TextView sentView = (TextView) smsDetails.findViewById(R.id.sent_view);
        TextView receivedView = (TextView) smsDetails.findViewById(R.id.received_view);

        Log.d("test", Contacts.getContactPhoto(Long.parseLong(Contacts.searchContacts(contact.get("number"), activity).get("id"))) + "");

        if(Contacts.hasContactPhoto(Long.parseLong(Contacts.searchContacts(contact.get("number"), activity).get("id")), activity)) {
            try {
                if (ScreenInfo.isPortrait(activity.getResources())) {
                    //Log.d("PICASSO", contacts.get(0).get("id"));
                    Picasso.with(activity)
                            .load(Contacts.getContactPhoto(Long.parseLong(Contacts.searchContacts(contact.get("number"), activity).get("id"))))
                            .resize(width / 8, width / 8)
                            .transform(new CircleTransform())
                            .into(contactImage);

                } else if (ScreenInfo.isLandscape(activity.getResources())) {
                    Picasso.with(activity)
                            .load(Contacts.getContactPhoto(Long.parseLong(Contacts.searchContacts(contact.get("number"), activity).get("id"))))
                            .resize(height / 8, height / 8)
                            .transform(new CircleTransform())
                            .into(contactImage);
                }
            } catch (Exception e) {
                Log.d("PICASSO", e.toString());
            }
        } else {
            try {
                if (ScreenInfo.isPortrait(activity.getResources())) {
                    //Log.d("PICASSO", contacts.get(0).get("id"));
                    Picasso.with(activity)
                            .load(R.drawable.default_user)
                            .resize(width / 8, width / 8)
                            .transform(new CircleTransform())
                            .into(contactImage);

                } else if (ScreenInfo.isLandscape(activity.getResources())) {
                    Picasso.with(activity)
                            .load(R.drawable.default_user)
                            .resize(height / 8, height / 8)
                            .transform(new CircleTransform())
                            .into(contactImage);
                }
            } catch (Exception e) {
                Log.d("PICASSO", e.toString());
            }
        }

        nameView.setText(Contacts.searchContacts(contact.get("number"), activity).get("name"));
        sentView.setText(contact.get("sent"));
        receivedView.setText(contact.get("received"));

        parent.addView(smsDetails);
    }
}