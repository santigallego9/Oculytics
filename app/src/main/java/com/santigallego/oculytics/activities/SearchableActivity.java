package com.santigallego.oculytics.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.LinearLayout;

import com.santigallego.oculytics.R;
import com.santigallego.oculytics.helpers.Contacts;
import com.santigallego.oculytics.helpers.Database;
import com.santigallego.oculytics.helpers.SmsContactDetailsHelper;
import com.santigallego.oculytics.helpers.Streaks;
import com.santigallego.oculytics.providers.SuggestionProvider;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            getSupportActionBar().setTitle(query);

            // save in history
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);

            populateUI(query);
        }
    }

    private void populateUI(String query) {
        // setup views
        LinearLayout parent = (LinearLayout) findViewById(R.id.searchable_parent);

        ArrayList<HashMap<String, String>> contacts = Contacts.searchMultipleContactsUsingName(query, this);

        for(HashMap<String, String> contact : contacts) {

            SQLiteDatabase db = this.openOrCreateDatabase(Database.DATABASE_NAME, MODE_PRIVATE, null);
            String selectQuery = "SELECT * FROM contacts WHERE number = \"" + contact.get("number") + "\";";
            Cursor cr = db.rawQuery(selectQuery, null);

            if (cr.moveToFirst()) {
                int id = cr.getInt(cr.getColumnIndex("id"));
                //String number = cr.getString(cr.getColumnIndex("number"));
                String sent = cr.getInt(cr.getColumnIndex("sent")) + "";
                String received = cr.getInt(cr.getColumnIndex("received")) + "";

                //contact.put("number", number);
                contact.put("sent", sent);
                contact.put("received", received);
                contact.put("streak", "" + Streaks.getStreak(this, id));
            }
            cr.close();

            db.close();

                /*Log.d("SEARCH", "  ");
                Log.d("SEARCH", "ID: " + contact.get("id"));
                Log.d("SEARCH", "NAME: " + contact.get("name"));
                Log.d("SEARCH", "NUMBER: " + contact.get("number"));
                Log.d("SEARCH", "  ");*/

            SmsContactDetailsHelper.createContactSmsDetails(this, contact, parent, true);
        }
    }
}
