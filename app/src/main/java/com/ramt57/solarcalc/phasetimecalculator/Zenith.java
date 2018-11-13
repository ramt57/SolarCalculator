package com.ramt57.solarcalc.phasetimecalculator;
/*
      zenith: Sun's zenith for sunrise/sunset
	  offical      = 90 degrees 50'
	  civil        = 96 degrees
	  nautical     = 102 degrees
	  astronomical = 108 degrees
*/
public class Zenith {
    private double degrees;

    private Zenith(double degrees) {
        this.degrees = degrees;
    }

    public double getDegrees() {
        return degrees;
    }
    /*static objects*/
    public static final Zenith ASTRONOMICAL = new Zenith(108);
    public static final Zenith NAUTICAL = new Zenith(102);
    public static final Zenith CIVIL = new Zenith(96);
    public static final Zenith OFFICIAL = new Zenith(90.833333333);
}
