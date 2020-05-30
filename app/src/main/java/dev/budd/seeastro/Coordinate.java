package dev.budd.seeastro;

import android.widget.FrameLayout;

public class Coordinate {

    //all values stored in radians
    private double ra;
    private double dec;

    private double raDeg;
    private double raMin;
    private double raSec;

    private double decDeg;
    private double decMin;
    private double decSec;

    public Coordinate(double ra, double dec){
        this.ra = ra;
        this.dec = dec;
        this.raDeg = Math.floor(Math.toDegrees(ra) / 15);
        double temp = ((Math.toDegrees(ra) / 15) - raDeg) * 60;
        this.raMin = Math.floor(temp);
        this.raSec = Math.round((temp - raMin) * 60);

        this.decDeg = Math.floor(Math.toDegrees(Math.abs(dec)));
        Double temp2 = ((Math.toDegrees(Math.abs(dec))) - this.decDeg) * 60;
        this.decMin = Math.floor(temp2);
        long decSec = Math.round((temp2 - decMin) * 60);
        this.decDeg = (dec < 0) ? this.decDeg *= -1 : this.decDeg;
        //System.out.println("New Coord: " + Math.round(raDeg) + ":"+ Math.round(raMin) + ":"+ Math.round(raSec) + " | "  + Math.round(decDeg) + ":"+ Math.round(decMin) + ":"+ Math.round(decSec));
    }

    public double getRa(){
        return this.ra;
    }

    public double getDec(){
        return this.dec;
    }

    public Coordinate(double raDeg, double raMin, double raSec, double decDeg, double decMin, double decSec){
        this.raDeg = raDeg;
        this.raMin = raMin;
        this.raSec = raSec;
        this.decDeg = decDeg;
        this.decMin = decMin;
        this.decSec = decSec;
    }

    public void printCoordinate(){
//        System.out.println("Radians:--------------");
//        System.out.println("RA:" + ra + " DEC:" + dec);
        //System.out.println("Degrees:--------------");
        System.out.println("RA: " + degreeMinuteSecondString(ra) + " DEC: " + degreeMinuteSecondString(dec));
    }

    private String degreeMinuteSecondString(double radian){
        double degree = Math.toDegrees(radian);
        String deg = String.valueOf(Math.floor(degree));
        String min = String.valueOf(Math.floor(60.0 * remainder(degree)));
        String sec = String.valueOf(Math.round(60 * (60 * remainder(degree) - Math.floor(60.0 * remainder(degree)))));
        return deg + ":" + min + ":" + sec;

    }

    double getRADegrees(){
        return Math.round(Math.toDegrees(this.ra));
    }
    double getDecDegrees(){
        return Math.round(Math.toDegrees(this.dec));
    }

    private double remainder(double x){
        x=x-Math.floor(x);
        if(x < 0){
            x++;
        }
        return x;
    }
}
