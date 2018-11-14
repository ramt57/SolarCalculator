package com.ramt57.solarcalc.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.ramt57.solarcalc.MapsActivity;
import com.ramt57.solarcalc.R;
import com.ramt57.solarcalc.model.PhaseTimeModel;
import com.ramt57.solarcalc.model.PlacesModel;
import com.ramt57.solarcalc.notification.BroadcastReciever;
import com.ramt57.solarcalc.phasetimecalculator.PhaseTimeUtils;
import com.ramt57.solarcalc.phasetimecalculator.Zenith;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ApplicationUtility {
    /*method for calculating phasetime*/
    public static PhaseTimeModel getPhaseTime(String current_date, PlacesModel placesModel) {
        Calendar c = getCalenderFromDate(current_date);
        Date date = c.getTime();
        if (placesModel.getLng() == null || placesModel.getLat() == null) {
            return null;
        }
        PhaseTimeUtils phaseTimeUtils = new PhaseTimeUtils(date.getDate(), date.getMonth(), date.getYear(), placesModel.getLng()
                , placesModel.getLat());
        /*todo by default we use official zenith you can pass any Zenith obj here*/
        double sunrise = phaseTimeUtils.getLocalTimeZone(true, Zenith.OFFICIAL, c);
        double sunset = phaseTimeUtils.getLocalTimeZone(false, Zenith.OFFICIAL, c);
        double moonrise = sunset + 1;
        if (moonrise > 24) {
            moonrise -= 24;
        }
        double moonset = sunrise - 1;
        if (moonset < 0) {
            moonset += 24;
        }

        DecimalFormat df = new DecimalFormat("#.##");
//        df.setRoundingMode(RoundingMode.HALF_EVEN);
        PhaseTimeModel phaseTimeModel = new PhaseTimeModel();
        String mSunrise = df.format(sunrise);
        phaseTimeModel.setSunrise(converttimetoampm(mSunrise));
        String mSunset = df.format(sunset);
        phaseTimeModel.setSunset(converttimetoampm(mSunset));
        String mMoonrise = df.format(moonrise);
        phaseTimeModel.setMoonrise(converttimetoampm(mMoonrise));
        String mMoonset = df.format(moonset);
        phaseTimeModel.setMoonset(converttimetoampm(mMoonset));
        return phaseTimeModel;
    }

    private static Calendar getCalenderFromDate(String current_date) {
        String date = current_date;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        try {
            cal.setTime(sdf.parse(date));
            return cal;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getCurrentDate(Calendar calendar) {
        Date c = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        return df.format(c);
    }

    private static String converttimetoampm(String time) {
        try {
            String replace = time.replace(".", ":");
            SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date _24HourDt = _24HourSDF.parse(replace);
            return _12HourSDF.format(_24HourDt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static long getTimeInMiliSecond(String time){
        SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        try {
            Date _24HourDt = _12HourSDF.parse(time);
            return _24HourDt.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /*alarm manger to start service for notifcation at a given time*/

    public static void setNotificationForPhaseTime(Context context, long miliseconds_time) {
        Intent notifyIntent = new Intent(context, BroadcastReciever.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (context, 2002, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, miliseconds_time,
                1000 * 60 * 60 * 24, pendingIntent);
    }
}
