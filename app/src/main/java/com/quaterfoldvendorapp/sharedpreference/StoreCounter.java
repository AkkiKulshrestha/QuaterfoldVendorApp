package com.quaterfoldvendorapp.sharedpreference;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by Ind3 on 16-04-18.
 */

public class StoreCounter {

    //the constants
    private static final String SHARED_PREF_NAME = "CLAIM_BIN_COUNTER";


    public static final String KEY_PENDING = "pening_counter";
    public static final String KEY_INPROGRESS = "inprogress_counter";
    public static final String KEY_REFERBACK = "referback_counter";


    private final SharedPreferences mPref;

    // Editor for Shared mPreferences
    private final SharedPreferences.Editor mEditor;

    // Context
    private final Context mContext;


    // Constructor
    public StoreCounter(Context context) {
        this.mContext = context;
        mPref = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mPref.edit();
        // mEditor.apply();
    }


    public void storeCounterValue(String pening_counter, String inprogress_counter, String referback_counter) {

        mEditor.putString(KEY_PENDING, pening_counter);
        mEditor.putString(KEY_INPROGRESS, inprogress_counter);
        mEditor.putString(KEY_REFERBACK, referback_counter);
        mEditor.apply();
    }

    /**
     * Get stored session data
     */
    public HashMap<String, String> getCounterValue() {
        HashMap<String, String> counter = new HashMap<String, String>();
        // user name
        counter.put(KEY_PENDING, mPref.getString(KEY_PENDING, null));
        // user email id
        counter.put(KEY_INPROGRESS, mPref.getString(KEY_INPROGRESS, null));
        // user id
        counter.put(KEY_REFERBACK, mPref.getString(KEY_REFERBACK, null));

        // return user
        return counter;
    }


    public void clearCounter() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}
