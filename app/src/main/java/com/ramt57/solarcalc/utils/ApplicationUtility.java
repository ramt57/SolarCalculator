package com.ramt57.solarcalc.utils;

import android.content.Context;
import android.content.DialogInterface;
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

    public static PhaseTimeModel getPhaseTime(String current_date, PlacesModel placesModel) {
        Calendar c=getCalenderFromDate(current_date);
        Date date = c.getTime();
        if(placesModel.getLng()==null||placesModel.getLat()==null){
            return null;
        }
        PhaseTimeUtils phaseTimeUtils=new PhaseTimeUtils(date.getDate(),date.getMonth(),date.getYear(),placesModel.getLng()
                ,placesModel.getLat());
        double sunrise=phaseTimeUtils.getLocalTimeZone(true,Zenith.OFFICIAL,c);
        double sunset=phaseTimeUtils.getLocalTimeZone(false,Zenith.OFFICIAL,c);
        double moonrise=sunset+1;
        if(moonrise>24){
            moonrise-=24;
        }
        double moonset=sunrise-1;
        if(moonset<0){
            moonset+=24;
        }

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_EVEN);
        PhaseTimeModel phaseTimeModel=new PhaseTimeModel();
        String mSunrise= df.format(sunrise);
        phaseTimeModel.setSunrise(mSunrise);
        String mSunset=df.format(sunset);
        phaseTimeModel.setSunset(mSunset);
        String mMoonrise=df.format(moonrise);
        phaseTimeModel.setMoonrise(mMoonrise);
        String mMoonset=df.format(moonset);
        phaseTimeModel.setMoonset(mMoonset);
        return  phaseTimeModel;
    }

    private static Calendar getCalenderFromDate(String current_date){
        String date=current_date;
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
}
