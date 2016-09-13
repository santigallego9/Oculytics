package com.santigallego.oculytics.providers;

import android.content.SearchRecentSuggestionsProvider;

/*
 * Created by santigallego on 9/12/16.
 */
public class SuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.santigallego.oculytics.providers.SuggestionProvider";

    // for one line:
    public final static int MODE = DATABASE_MODE_QUERIES;

    // for two lines:
    // public final static int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;

    public SuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
