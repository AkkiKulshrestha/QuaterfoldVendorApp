package com.quaterfoldvendorapp.sharedpreference;

import android.content.Context;
import android.content.SharedPreferences;

import com.quaterfoldvendorapp.data.Agentinfo;


/**
 * Created by Ind3 on 18-12-17.
 */

//here for this class we are using a singleton pattern

public class SharedPrefManager {

    //the constants
    private static final String SHARED_PREF_NAME = "QUATERFOLD_APP";

    private static final String KEY_ID = "id";

    private static final String KEY_VENDOR_CODE = "vendor_code";
    // private static final String KEY_IMAGE = "image";
    private static final String KEY_USER_NAME = "username";
    private static final String KEY_EMAIL = "email";

    private static final String KEY_MOBILE = "mobile";

    private static final String KEY_ADDRESS_LINE1 = "address_line_1";

    private static final String KEY_ADDRESS_LINE2 = "address_line_2";

    private static final String KEY_CITY = "city";

    private static final String KEY_STATE = "state";

    private static final String KEY_PINCODE = "pincode";

    private static final String KEY_WORKING_CITIES = "working_cities";
    private static final String KEY_IMEI = "imei_no";

    private static SharedPrefManager mInstance;
    private static Context mCtx;

    public SharedPrefManager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    //method to let the user login
    //this method will store the user data in shared preferences
    public void agentLogin(Agentinfo agentinfo) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_ID, agentinfo.getId());
        editor.putString(KEY_USER_NAME, agentinfo.getUsername());
        editor.putString(KEY_EMAIL, agentinfo.getEmail());
        editor.putString(KEY_MOBILE, agentinfo.getMobile());
        editor.putString(KEY_ADDRESS_LINE1, agentinfo.getAddress_line_1());
        editor.putString(KEY_ADDRESS_LINE2, agentinfo.getAddress_line_2());
        editor.putString(KEY_CITY, agentinfo.getCity());
        editor.putString(KEY_STATE, agentinfo.getState());
        editor.putString(KEY_WORKING_CITIES, agentinfo.getWorking_cities());
        editor.putString(KEY_IMEI, agentinfo.getImei_no());
        editor.apply();
    }

    //this method will checker whether user is already logged in or not
    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_MOBILE, null) != null;
    }

    //this method will give the logged in user
    public Agentinfo getAgent() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return new Agentinfo(
                sharedPreferences.getString(KEY_ID, null),
                sharedPreferences.getString(KEY_USER_NAME, null),
                sharedPreferences.getString(KEY_EMAIL, null),
                sharedPreferences.getString(KEY_MOBILE, null),
                sharedPreferences.getString(KEY_ADDRESS_LINE1, null),
                sharedPreferences.getString(KEY_ADDRESS_LINE2, null),
                sharedPreferences.getString(KEY_CITY, null),
                sharedPreferences.getString(KEY_STATE, null),
                sharedPreferences.getString(KEY_IMEI,null)
        );
    }

    //this method will logout the user
    public void logout() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}