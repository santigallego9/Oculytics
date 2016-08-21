package com.santigallego.oculytics.helpers;

import android.util.Log;

/*
 * Created by santigallego on 8/19/16.
 */
public class PhoneNumbers {

    public PhoneNumbers() {}

    public static String formatNumber(String number, boolean doAreaCode) {
        number = number.replaceAll("-", "");
        number = number.replaceAll("\\(", "");
        number = number.replaceAll("\\)", "");
        number = number.replaceAll(" ", "");

        if(number.contains("+")) {
            int country_code = number.length() - 10;

            if(country_code > 0) {
                number = number.substring(country_code);
            } else {
                number = number.substring(1);
            }
        }

        if(doAreaCode && number.length() == 10) {
            number = number.substring(3);
        }

        return number;
    }
}
