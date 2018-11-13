package com.ramt57.solarcalc.phasetimecalculator;

import java.util.Calendar;

import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

/*algorithm source
  https://web.archive.org/web/20161202180207/http://williams.best.vwh.net/sunrise_sunset_algorithm.htm*/
public class PhaseTimeUtils {
    private int day;
    private int month;
    private int year;
    private double longitude;
    private double lattitude;

    private PhaseTimeUtils() {

    }

    public PhaseTimeUtils(int day, int month, int year, double longitude, double lattitude) {
        this.day = day;
        this.day = month;
        this.year = year;
        this.longitude = longitude;
        this.lattitude = lattitude;
    }

    /*1.first calculate the day of the year*/
    private double getDayOfYear() {
        double N1 = floor(275 * month / 9);
        double N2 = floor((month + 9) / 12);
        double N3 = (1 + floor((year - 4 * floor(year / 4) + 2) / 3));
        return N1 - (N2 * N3) + day - 30;
    }

    /*2.convert the longitude to hour value and calculate an approximate time*/
    private double getLongHour() {
        return longitude / 15;
    }

    private double getApproximateTime(boolean isSunrise) {
        int offset = 18;
        if (isSunrise) {
            offset = 6;
        }
        return getDayOfYear() + ((offset - getLongHour()) / 24);
    }

    /*3.calculate the Sun's mean anomaly*/
    private double getSunMeanAnamoly(boolean isSunrise) {
        return (0.9856 * getApproximateTime(isSunrise)) - 3.289;
    }

    /*4. calculate the Sun's true longitude*/
    /*radian issue*/
    public double getSunTrueLongitude(boolean isSunrise) {
        double sunmeananamoly = getSunMeanAnamoly(isSunrise);
        double L = sunmeananamoly + (1.916 * sin(convertDegreesToRadians(sunmeananamoly))) + (0.020 * sin(2 * convertDegreesToRadians(sunmeananamoly))) + 282.634;
        /*NOTE: L potentially needs to be adjusted into the range [0,360) by adding/subtracting 360*/
        if (L > 360) {
            return L - 360;
        } else if (L < 0) {
            return L + 360;
        } else {
            return L;
        }
    }

    /*5a. calculate the Sun's right ascension*/

    private double getRightAscension(boolean isSunrise) {
        double mSunTrueLongitude = getSunTrueLongitude(isSunrise);
        double RA = convertRadiansToDegrees(atan(0.91764 * tan(convertDegreesToRadians(mSunTrueLongitude))));
        if (RA > 360) {
            RA = RA - 360;
        } else if (RA < 0) {
            RA = RA + 360;
        }

        double Lquadrant = (floor(mSunTrueLongitude / 90)) * 90;
        double Rquadrant = (floor(RA / 90)) * 90;
        RA = RA + (Lquadrant - Rquadrant);
        return RA / 15;
    }

    /*6. calculate the Sun's declination
     */
    /*7a. calculate the Sun's local hour angle
      7b. finish calculating H and convert into hours
    * */
    private double getSunLocalHour(boolean isSunrise, Zenith zenith) {
        double sinDec = 0.39782 * sin(convertDegreesToRadians(getSunTrueLongitude(isSunrise)));
        double cosDec = cos(asin(sinDec));
        double cosH = (cos(convertDegreesToRadians(zenith.getDegrees())) -
                (sinDec * sin(convertDegreesToRadians(lattitude)))) / (cosDec * cos(convertDegreesToRadians(lattitude)));
        if (isSunrise) {
            return (360 - convertRadiansToDegrees(acos(cosH))) / 15;
        }
        return convertRadiansToDegrees(acos(cosH)) / 15;
    }

    /*8. calculate local mean time of rising/setting*/
    private double getLocalMeanTime(boolean isSunrise, Zenith zenith) {
        return getSunLocalHour(isSunrise, zenith) +
                getRightAscension(isSunrise) - (0.06571 * getApproximateTime(isSunrise)) - 6.622;
    }

    /*9. adjust back to UTC
10. convert UT value to local time zone of latitude/longitude

	localT = UT + localOffset
*/
    public double getLocalTimeZone(boolean isSunrise, Zenith zenith, Calendar date) {
        double UT = getLocalMeanTime(isSunrise, zenith) - getLongHour();
        if (UT < 0) {
            UT = UT + 24;
        } else if (UT > 24) {
            UT = UT - 24;
        }
        double localtime = UT + getUTCOffSet(date);
        if (localtime > 24) {
            localtime = localtime - 24;
        }
        return localtime;
    }

    private double getUTCOffSet(Calendar date) {
        return date.get(Calendar.ZONE_OFFSET) / 3600000;
    }

    private double convertRadiansToDegrees(double radians) {
        return radians * (180 / Math.PI);
    }

    private double convertDegreesToRadians(double degrees) {
        return degrees * (Math.PI / 180.0);
    }
}
