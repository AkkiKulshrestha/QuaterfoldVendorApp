package com.quaterfoldvendorapp.sharedpreference;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by Darpan on 10-04-2015.
 */

public class UtilitySharedPreferences {

    static String prefName = "OfferMartPreferences";
    static SharedPreferences preferences;
    static SharedPreferences.Editor editor;

    public static String setPrefs(Context context, String prefKey, String prefValue) {
        preferences = context.getSharedPreferences(prefName, context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString(prefKey, prefValue);
        editor.commit();
        return prefKey;
    }

    public static String getPrefs(Context context, String prefKey) {
        preferences = context.getSharedPreferences(prefName, context.MODE_PRIVATE);
        return preferences.getString(prefKey, null);
    }

   public static void clearPref(Context context) {
        preferences = context.getSharedPreferences(prefName, context.MODE_PRIVATE);
        editor = preferences.edit();
       editor.clear().commit();
       CommonMethods.deleteCache(context);

    }

    static void clearPref1(Context context, String prefKey) {
        preferences = context.getSharedPreferences(prefName, context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.clear().commit();
    }

}
