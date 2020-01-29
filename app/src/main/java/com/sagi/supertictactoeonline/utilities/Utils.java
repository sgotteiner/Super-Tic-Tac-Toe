package com.sagi.supertictactoeonline.utilities;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by User on 14/09/2018.
 */

public class Utils {

    public static long getTimeStampFromDate(int year, int month, int day) {
        Calendar calendar= Calendar.getInstance();
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month);
        calendar.set(Calendar.DAY_OF_MONTH,day);
        return calendar.getTimeInMillis();
    }

    public static String getDateAndTimeFromTimeStamp(long timeStampDate ) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date netDate = (new Date(timeStampDate));
        return sdf.format(netDate);
    }

    public static String getTimeFromTimeStamp(long timeStampDate ) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date netDate = (new Date(timeStampDate));
        return sdf.format(netDate);
    }

    public static String getDateFromTimeStamp(long timeStampDate ) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date netDate = (new Date(timeStampDate));
        return sdf.format(netDate);
    }
    public static long getTimeStampFromDateAndTime(long timeStampDate,int hour, int minuets) {
        Calendar calendar= Calendar.getInstance();
        calendar.setTimeInMillis(timeStampDate);
        calendar.set(Calendar.HOUR,hour);
        calendar.set(Calendar.MINUTE,minuets);
        calendar.set(Calendar.SECOND,0);
        return calendar.getTimeInMillis();
    }

    public static boolean isValid(String email, String fName, String lName, long dateBirthDay, Context context) {


        if ( email.equals("") || fName.equals("") || lName.equals("")) {
            Toast.makeText(context, "must fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        } else   if (dateBirthDay == -1) {
            Toast.makeText(context, "must choose a birth date", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    public static boolean isValid(String email, String pass, String fName, String lName, long dateBirthDay, Context context) {

        if (pass.equals("") || email.equals("") || fName.equals("") || lName.equals("")) {
            Toast.makeText(context, "must fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!isEmailValid(email)) {
            Toast.makeText(context, "Email is not valid", Toast.LENGTH_SHORT).show();
            return false;
        } else if (pass.length() < 6) {
            Toast.makeText(context, "Pass must have at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        } else if (dateBirthDay == -1) {
            Toast.makeText(context, "must choose a birth date", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }


    public static void hideKeyboardFrom(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    private static boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static String textEmailForFirebase(String email) {
        String emailFirebase = email;
        emailFirebase = emailFirebase.replace("@", "_");
        emailFirebase = emailFirebase.replace(".", "_");
        return emailFirebase;
    }

    public static String geteFirstLettersUpperCase(String text ) {
        return text.substring(0, 1).toUpperCase() + text.substring(1, text.length());
    }


//    public static long getTimeStampFromDate(int year, int month, int day) {
//
//        String d,m,y;
//        if(day>0&&day<10)
//            d="0"+String.valueOf(day);
//        else
//            d=String.valueOf(day);
//        if(month>0&&month+1<10)
//            m="0"+String.valueOf(month+1);
//        else
//            m=String.valueOf(month+1);
//        y=String.valueOf(year);
//        String str_date = d + "/" + m + "/" + y;
//
//        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//        Date date = null;
//        try {
//            date = dateFormat.parse(str_date);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        long time = date.getTime();
//        return time;
//    }
}
