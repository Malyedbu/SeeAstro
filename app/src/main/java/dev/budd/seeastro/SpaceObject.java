package dev.budd.seeastro;

import java.util.HashMap;
import java.util.Map;


/**
 * NGC and IC space object
 */
class SpaceObject {

    private static final double PLACEHOLDER_MAXIMUM_MAGNITUDE = 99.99;

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

    private final static Map<String, String> typeMap = new HashMap<>();
    static {
        typeMap.put("*", "Star");
        typeMap.put("**", "Double star");
        typeMap.put("*Ass", "Association of stars");
        typeMap.put("OCl", "Open Cluster");
        typeMap.put("GCl", "Globular Cluster");
        typeMap.put("Cl+N", "Cluster + Nebula");
        typeMap.put("G", "Galaxy");
        typeMap.put("GPair", "Galaxy Pair");
        typeMap.put("GTrpl", "Galaxy Triplet");
        typeMap.put("GGroup", "Galaxy group");
        typeMap.put("PN", "Planetary Nebula");
        typeMap.put("HII", "HII Ionized region");
        typeMap.put("DrkN", "Dark Nebula");
        typeMap.put("EmN", "Emission Nebula");
        typeMap.put("Neb", "Nebula");
        typeMap.put("RfN", "Reflection Nebula");
        typeMap.put("SNR", "Supernova remnant");
        typeMap.put("Nova", "Nova");
        typeMap.put("NonEx", "None");
        typeMap.put("Dup", "Duplicate");
        typeMap.put("Other", "Other");
    }

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
        return PLACEHOLDER_MAXIMUM_MAGNITUDE;
    }

    public String getType(){
        String longType = typeMap.get(this.type);
        if(longType != null && !longType.isEmpty()) {
                return longType;
        }
        return "other";
    }

    public boolean isStar(){
        return (this.type.charAt(0) == '*');
    }
    public boolean isOther(){ return (this.type.matches("Other"));}

    public double getRAAsHours(){
        return Math.floor(Math.toDegrees(Double.parseDouble(this.RA)) / 15);
    }

    public double getDecAsDegrees(){
        return Math.floor(Math.toDegrees(Double.parseDouble(this.Dec)));
    }



}
