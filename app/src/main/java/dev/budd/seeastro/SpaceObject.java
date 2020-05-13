package dev.budd.seeastro;

import java.time.Instant;
import java.util.regex.Pattern;

/**
 * NGC and IC space object
 */
public class SpaceObject {
    private String name;
    private String type;
    private String RA;
    private String Dec;
    private String Const;
    private String MajAx;
    private String MinAx;
    private String PosAng;
    private String BMag;
    private String VMag;
    private String JMag;
    private String HMag;
    private String KMag;
    private String SurfBr;
    private String Hubble;
    private String CStarUMag;
    private String CStarBMag;
    private String CStarVMag;
    private String M;
    private String NGC;
    private String IC;
    private String CStarName;
    private String Identifiers;
    private String CommonName;
    private String NedNotes;
    private String OpenNGCNotes;

    private Coordinate coordinate;

    public SpaceObject(String[] items){
        if(items.length == 27) {
            //System.out.println("First Item: " + items[0]);
            this.name = items[1];
            this.type = items[2];
            this.RA = items[3];
            this.Dec = items[4];
            this.Const = items[5];
            this.MajAx = items[6];
            this.MinAx = items[7];
            this.PosAng = items[8];
            this.BMag = items[9];
            this.VMag = items[10];
            this.JMag = items[11];
            this.HMag = items[12];
            this.KMag = items[13];
            this.SurfBr = items[14];
            this.Hubble = items[15];
            this.CStarUMag = items[16];
            this.CStarBMag = items[17];
            this.CStarVMag = items[18];
            this.M = items[19];
            this.NGC = items[20];
            this.IC = items[21];
            this.CStarName = items[22];
            this.Identifiers = items[23];
            this.CommonName = items[24];
            this.NedNotes = items[25];
            this.OpenNGCNotes = items[26];
            //this.coordinate = new Coordinate(Double.parseDouble(this.RA), Double.parseDouble(this.Dec));
        }else{
            System.err.println("MALFORMED");
        }
    }

    public String getCommonName(){
        return this.CommonName;
    }
    public String getRA(){return this.RA; }
    public String getDec(){return this.Dec; }
    public String getName(){
        return this.name;
    }
    public String getVMag(){
        return this.VMag;
    }
    public String getBMag(){
        return this.BMag;
    }

    public String getShortenedRADecString(){

        double ra = Double.parseDouble(this.getRA());
        double dec = Double.parseDouble(this.getDec());

        double raDeg = Math.floor(Math.toDegrees(ra) / 15);
        Double temp = ((Math.toDegrees(ra) / 15) - raDeg) * 60;
        Double raMin = Math.floor(temp);
        long raSec = Math.round((temp - raMin) * 60);

        Double decDeg = Math.floor(Math.toDegrees(Math.abs(dec)));
        Double temp2 = ((Math.toDegrees(Math.abs(dec))) - decDeg) * 60;
        Double decMin = Math.floor(temp2);
        long decSec = Math.round((temp2 - decMin) * 60);
        decDeg = (dec < 0) ? decDeg *= -1 : decDeg;
        return Math.round(raDeg) + ":"+ Math.round(raMin) + ":"+ Math.round(raSec) + " | "  + Math.round(decDeg) + ":"+ Math.round(decMin) + ":"+ Math.round(decSec);
    }

    public Double getBrightness(){
        if(!this.VMag.isEmpty()){
            return Double.parseDouble(this.VMag);
        }else if(!this.BMag.isEmpty()) {
            return Double.parseDouble(this.BMag);
        }else if(!this.JMag.isEmpty() && !this.HMag.isEmpty() && !this.KMag.isEmpty()){
            return (Double.parseDouble(this.JMag) + Double.parseDouble(this.HMag) + Double.parseDouble(this.KMag)) / 3;
        }
        return Double.MAX_VALUE;
    }

    public boolean isStar(){
        return (this.type.charAt(0) == '*');
    }


}
