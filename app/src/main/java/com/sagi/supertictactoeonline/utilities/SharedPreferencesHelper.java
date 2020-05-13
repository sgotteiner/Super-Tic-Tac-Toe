package com.sagi.supertictactoeonline.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import com.sagi.supertictactoeonline.entities.User;
import static android.content.Context.MODE_PRIVATE;

public class SharedPreferencesHelper {

    private static final String START_TIME_MILLIS = "start time";
    private static SharedPreferences preferences;
    private static SharedPreferencesHelper mInstance;

    private final String SETTINGS_APP = "SETTINGS_APP";
    private final String IS_ALREADY_LOGIN = "IS_ALREADY_LOGIN";
    private final String NAME = "NAME";
    private final String KEY = "KEY";
    private final String LAST_TIME_SEEN = "LAST_TIME_SEEN";
    private final String RANK = "RANK";

    private SharedPreferencesHelper(Context context) {
        preferences = context.getSharedPreferences(SETTINGS_APP, MODE_PRIVATE);
    }

    public static SharedPreferencesHelper getInstance(Context context) {
        if (mInstance == null)
            mInstance = new SharedPreferencesHelper(context);

        return mInstance;
    }

    public boolean isAlreadyLogin() {
        return preferences.getBoolean(IS_ALREADY_LOGIN, false);
    }

    public void setIsAlreadyLogin(boolean isAlreadyLogin) {
        preferences.edit().putBoolean(IS_ALREADY_LOGIN, isAlreadyLogin).commit();
    }

    public User getUser() {
        String name = preferences.getString(NAME, null);
        String key = preferences.getString(KEY, null);
        long lastTimeSeen = preferences.getLong(LAST_TIME_SEEN, -1);
        int rank = preferences.getInt(RANK, 0);
        return new User(name, key, lastTimeSeen, rank);
    }

    public void setUser(User user) {
        preferences.edit().putString(NAME, user.getName()).commit();
        preferences.edit().putString(KEY, user.getKey()).commit();
        preferences.edit().putLong(LAST_TIME_SEEN, user.getLastTimeSeen()).commit();
        setRank(user.getRank());
    }

    public void setRank(int rank) {
        preferences.edit().putInt(RANK, rank).commit();
    }

    public void setStartTime(int startTimeMillis){
        preferences.edit().putInt(START_TIME_MILLIS, startTimeMillis).commit();
    }

    public int getStartTime(){
        return preferences.getInt(START_TIME_MILLIS, 0);
    }

    public void resetSharedPreferences() {
        preferences.edit().clear().commit();
    }
}
