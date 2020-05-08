package dev.budd.seeastro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.os.Bundle;

import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    //Belrose lat/long
    int latDeg = 33;
    int latMin = 44;
    int latSec = 10;

    int longDeg = 151;
    int longMin = 12;
    int longSec = 52;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArrayList<SpaceObject> objects = readCSVIntoObjects();
        SpaceObject tester = objects.get(5849);
        SpaceObject tester2 = objects.get(5921);
        prettyPrintObject(tester);
        prettyPrintObject(tester2);

        Date currentTime = Calendar.getInstance().getTime();
        Calendar now = Calendar.getInstance();
        now.setTime(currentTime);

        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH)+1; //months start at zero doesn't work for the maths.
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY)-10; //converting to UTC+0
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);

        double ut = hour + minute / 60.0 + second / 3600.0;
        //double JD = getJulDay(day, month, year, hour, minute, second);
        double JD = getJulDay(day, month, year, ut);
        //System.out.println("JD: " + JD);
        double longitude = decimalFromDegrees(longDeg, longMin, longSec);
        //System.out.println("Longitude: " + longitude);
        double GMST = gmSiderealTime(JD);
        //System.out.println("GMST: " + GMST);
        double LMST = 24.0 * remainder((GMST + longitude / 15) / 24);
        double stHour = Math.floor(LMST);
        double stMin = Math.floor(60.0 * remainder(LMST));
        double stSec = Math.round(60 * (60 * remainder(LMST) - stMin));
        //System.out.println("LMST: " + LMST);
        System.out.println("LMST: " + "H:" + stHour + " M:" + stMin + " S:" + stSec);


        Coordinate zenith = new Coordinate(Math.toRadians(decimalFromDegrees(latDeg, latMin, latSec)), Math.toRadians(decimalFromDegrees(stHour, stMin, stSec)));
        Coordinate object = new Coordinate(Double.parseDouble(tester2.getRA()), Double.parseDouble(tester2.getDec()));
        if(zenith.canSee(Double.parseDouble(tester2.getRA()), Double.parseDouble(tester2.getDec()), 0.4)){
            zenith.printCoordinate();
            object.printCoordinate();
            System.out.println("zenith can see " + tester2.getName());
        }else{
            zenith.printCoordinate();
            object.printCoordinate();
            System.out.println("zenith can't see " + tester2.getName());
        }

    }

    private double decimalFromDegrees(double deg, double min, double sec){
        return deg + (min / 60.0) + (sec / 3600.0);
    }

    private ArrayList<SpaceObject> readCSVIntoObjects(){
        ArrayList<SpaceObject> objects = new ArrayList<>();
        try{

            AssetManager am = this.getAssets();
            InputStream csvStream = am.open("objects.csv");
            InputStreamReader isr = new InputStreamReader(csvStream);
            CSVReader reader = new CSVReader(isr);
            String[] nextLine;

            while((nextLine = reader.readNext()) != null){
                if(nextLine[0].matches("id")){
                    nextLine = reader.readNext();
                }
                SpaceObject obj = new SpaceObject(nextLine);
                objects.add(obj);
            }


        }catch (Exception e){
            e.printStackTrace(System.out);
        }
        return objects;

    }

    private void prettyPrintObject(SpaceObject o){
        System.out.print("Catalogue Name: " + o.getName());
        if(!o.getCommonName().matches("")){
            System.out.println(", Common Name: " + o.getCommonName());
        }else{
            System.out.println();
        }
        printRADec(o);
        printMagnitude(o);
        System.out.println("-----------------------------------------");
    }
    private void printRADec(SpaceObject o){
        double ra = Double.parseDouble(o.getRA());
        double dec = Double.parseDouble(o.getDec());

        double raDeg = Math.floor(Math.toDegrees(ra) / 15);
        Double temp = ((Math.toDegrees(ra) / 15) - raDeg) * 60;
        Double raMin = Math.floor(temp);
        long raSec = Math.round((temp - raMin) * 60);

        Double decDeg = Math.floor(Math.toDegrees(Math.abs(dec)));
        Double temp2 = ((Math.toDegrees(Math.abs(dec))) - decDeg) * 60;
        Double decMin = Math.floor(temp2);
        long decSec = Math.round((temp2 - decMin) * 60);
        decDeg = (dec < 0) ? decDeg *= -1 : decDeg;
        System.out.println("-Coordinates: RA: " + raDeg + ":"+ raMin + ":"+ raSec + " Dec: "  + decDeg + ":"+ decMin + ":"+ decSec);
    }

    private void printMagnitude(SpaceObject o){
        if(!o.getVMag().isEmpty()){
            System.out.println("-VMag: " + o.getVMag());
        }else if(!o.getBMag().isEmpty()){
            System.out.println("-BMag: " + o.getBMag());
        }else{
            System.out.println("No VMag or BMag data.");
        }
    }

    /**
     * BLEGH
     */
    //private double getJulDay(int d, int m, int y, int hr, int min, int sec){
    private double getJulDay(int d, int m, int y, double u){
        //System.out.println("CalculatingJD: " + " y: " + y + " m: " + m + " d: " + d + " hr: " + hr + " min: " + min + " sec: " + sec);
        //System.out.println("CalculatingJD: " + " y: " + y + " m: " + m + " d: " + d + " ut: " + u);
        if (m <= 2){
            m+=12;
            y-=1;
        }

        //return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + d - 13 - 1524.5 + u / 24.0;
        int a = y/100;
        int b = a / 4;
        int c = 2-a+b;
        int e = (int) Math.floor(365.25*(y+4716));
        int f = (int) Math.floor(30.6001*(m+1));
        return c + d + e + f -1524.5 + u/24;

//        double JDN = (1461 * (y + 4800 + (m - 14)/12))/4 + (367 * (m - 2 - 12 * ((m - 14)/12)))/12 - (3 * ((y + 4900 + (m - 14)/12)/100))/4 + d - 32075;
//        JDN = JDN + ((hr-12) / 24) + (min / 1440) + (sec / 86400.0);
//        return JDN;


    }

    private double gmSiderealTime(double jd){
        //jd = 2458977.5548;
        double t_eph, ut, MJD0, MJD;
        MJD = jd - 2400000.5;
        MJD0 = Math.floor(MJD);
        ut = (MJD - MJD0) * 24.0;
        t_eph = (MJD0 - 51544.5) / 36525.0;
        return 6.697374558 + 1.0027379093 * ut + (8640184.812866 + (0.093104 - 0.0000062 * t_eph) * t_eph) * t_eph / 3600.0;
    }

    private double remainder(double x){
        x=x-Math.floor(x);
        if(x < 0){
            x++;
        }
        return x;
    }

}
