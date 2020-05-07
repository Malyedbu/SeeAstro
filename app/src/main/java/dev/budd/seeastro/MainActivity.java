package dev.budd.seeastro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.os.Bundle;

import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private boolean once = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArrayList<SpaceObject> objects = readCSVIntoObjects();

        SpaceObject tester = objects.get(5849);
        SpaceObject tester2 = objects.get(6045);
       prettyPrintObject(tester);
       prettyPrintObject(tester2);



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
                SpaceObject obj = new SpaceObject(nextLine);
                objects.add(obj);
            }


        }catch (Exception e){
            e.printStackTrace(System.out);
        }
        return objects;

    }

    /**
     * I want to change this to take a space pbject object
     * and pretty print relevant info.
     * @param r
     * @param d
     */

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
        Double ra = Double.parseDouble(o.getRA());
        Double dec = Double.parseDouble(o.getDec());

        Double raDeg = Math.floor(Math.toDegrees(ra) / 15);
        Double temp = ((Math.toDegrees(ra) / 15) - raDeg) * 60;
        Double raMin = Math.floor(temp);
        Long raSec = Math.round((temp - raMin) * 60);

        Double decDeg = Math.floor(Math.toDegrees(Math.abs(dec)));
        Double temp2 = ((Math.toDegrees(Math.abs(dec))) - decDeg) * 60;
        Double decMin = Math.floor(temp2);
        Long decSec = Math.round((temp2 - decMin) * 60);
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

}
