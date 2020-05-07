package dev.budd.seeastro;

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

    public SpaceObject(String[] items){
        if(items.length == 27) {
            //System.out.println("First Item: " + items[0]);
            name = items[1];
            type = items[2];
            RA = items[3];
            Dec = items[4];
            Const = items[5];
            MajAx = items[6];
            MinAx = items[7];
            PosAng = items[8];
            BMag = items[9];
            VMag = items[10];
            JMag = items[11];
            HMag = items[12];
            KMag = items[13];
            SurfBr = items[14];
            Hubble = items[15];
            CStarUMag = items[16];
            CStarBMag = items[17];
            CStarVMag = items[18];
            M = items[19];
            NGC = items[20];
            IC = items[21];
            CStarName = items[22];
            Identifiers = items[23];
            CommonName = items[24];
            NedNotes = items[25];
            OpenNGCNotes = items[26];
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


}
