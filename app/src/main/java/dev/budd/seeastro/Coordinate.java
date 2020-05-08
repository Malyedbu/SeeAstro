package dev.budd.seeastro;

public class Coordinate {

    //all values stored in radians
    private double ra;
    private double dec;

    public Coordinate(double ra, double dec){
        this.ra = ra;
        this.dec = dec;
    }

    public double getRa(){
        return this.ra;
    }

    public double getDec(){
        return this.dec;
    }

    public Coordinate(double raDeg, double raMin, double raSec, double decDeg, double decMin, double decSec){
        double ra = raDeg + (raMin / 60) + (raSec/3600);
        double dec = decDeg + (decMin / 60) + (decSec/3600);
        this.ra = Math.toRadians(ra);
        this.dec = Math.toRadians(dec);
    }

    public boolean canSee(Coordinate c, double radianRange){
        if (Math.abs(this.ra-c.getRa()) < radianRange && Math.abs(this.dec-c.getDec()) < radianRange){
            return true;
        }
        return false;
    }

    public void printCoordinate(){
        System.out.println("Radians:--------------");
        System.out.println("RA:" + ra + " DEC:" + dec);
        System.out.println("Degrees:--------------");
        System.out.println("RA: " + degreeMinuteSecondString(ra) + " DEC: " + degreeMinuteSecondString(dec));
    }

    private String degreeMinuteSecondString(double radian){
        double degree = Math.toDegrees(radian);
        String deg = String.valueOf(Math.floor(degree));
        String min = String.valueOf(Math.floor(60.0 * remainder(degree)));
        String sec = String.valueOf(Math.round(60 * (60 * remainder(degree) - Math.floor(60.0 * remainder(degree)))));
        return deg + ":" + min + ":" + sec;

    }

    private double remainder(double x){
        x=x-Math.floor(x);
        if(x < 0){
            x++;
        }
        return x;
    }
}
