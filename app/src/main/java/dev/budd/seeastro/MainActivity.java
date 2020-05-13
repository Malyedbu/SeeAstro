package dev.budd.seeastro;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final double VISIBLE_RANGE_RADIANS = 0.4;
    private static  ArrayList<SpaceObject> SPACE_OBJECT_ARRAY_LIST;

    //Greenwich Default Longitude lat/long
    int latDeg = 33;
    int latMin = 44;
    int latSec = 10;
    String latitudeChar = "S";

    int longDeg = 151;
    int longMin = 12;
    int longSec = 52;
    String longitudeChar = "E";

    Spinner latSpinner;
    Spinner longSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SPACE_OBJECT_ARRAY_LIST = readCSVIntoObjects();
        latSpinner = findViewById(R.id.dropDownSpinnerLatitude);
        longSpinner = findViewById(R.id.dropDownSpinnerLongitude);

        ArrayAdapter<CharSequence>  latAdaptor = ArrayAdapter.createFromResource(this, R.array.northSouth, android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence>  longAdaptor = ArrayAdapter.createFromResource(this, R.array.eastWest, android.R.layout.simple_spinner_dropdown_item);
        latSpinner.setAdapter(latAdaptor);
        longSpinner.setAdapter(longAdaptor);
        latSpinner.setOnItemSelectedListener(new SpinnerActivity());
        longSpinner.setOnItemSelectedListener(new SpinnerActivity());
        startAnalysis();
    }

    public void setNewVariables(View v){

        try{
            EditText latD = findViewById(R.id.latitudeDegreeEditText);
            EditText latM = findViewById(R.id.latitudeMinuteEditText);
            EditText latS = findViewById(R.id.latitudeSecondEditText);
            EditText longD = findViewById(R.id.longitudeDegreeEditText);
            EditText longM = findViewById(R.id.longitudeMinuteEditText);
            EditText longS = findViewById(R.id.longitudeSecondEditText);

            latDeg = Integer.parseInt(latD.getText().toString());
            latMin = Integer.parseInt(latM.getText().toString());
            latSec = Integer.parseInt(latS.getText().toString());
            longDeg = Integer.parseInt(longD.getText().toString());
            longMin = Integer.parseInt(longM.getText().toString());
            longSec = Integer.parseInt(longS.getText().toString());
        }catch(NumberFormatException e){
            Toast.makeText(this, "Please Enter Valid Numbers", Toast.LENGTH_SHORT);
        }
        System.out.println("Set new variables...");
        startAnalysis();
    }
    public void startAnalysis(){
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
        double JD = getJulDay(day, month, year, ut);
        //double longitude = decimalFromDegrees(longDeg, longMin, longSec);
        double longitude = getLongitude(longDeg, longMin, longSec);
        double GMST = gmSiderealTime(JD);
        double LMST = 24.0 * remainder((GMST + longitude / 15) / 24);
        double stHour = Math.floor(LMST);
        double stMin = Math.floor(60.0 * remainder(LMST));
        double stSec = Math.round(60 * (60 * remainder(LMST) - stMin));
        System.out.println("LMST: " + "H:" + stHour + " M:" + stMin + " S:" + stSec);

        Coordinate zenith = new Coordinate(Math.toRadians(decimalFromDegrees(stHour, stMin, stSec)), Math.toRadians(getLatitude(latDeg, latMin, latSec)));
        List<SpaceObject> visibleObjects = visibleObjects(SPACE_OBJECT_ARRAY_LIST, zenith);
        visibleObjects = sortBrightest(visibleObjects);
        System.out.println("Brightest Object: " + visibleObjects.get(0).getName());
        addToView(visibleObjects);
    }

    private double getLongitude(double deg, double min, double sec){
       if (longitudeChar.matches("W")){
           System.out.println("negative longitude");
           deg *= -1;
       }
       return decimalFromDegrees(deg, min, sec);
    }
    private double getLatitude(double deg, double min, double sec){
        if (longitudeChar.matches("S")){
            System.out.println("negative latitude");
            deg *= -1;
        }
        System.out.println(decimalFromDegrees(deg, min, sec));
        return decimalFromDegrees(deg, min, sec);
    }

    private void addToView(List<SpaceObject> objects){
        TableLayout tableLayout = findViewById(R.id.mainObjectTableLayout);
        tableLayout.removeAllViews();
        TableRow headings = new TableRow(this);
        headings.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

        TextView nameHeading = new TextView(this);
        TextView brightnessHeading = new TextView(this);
        TextView RADecHeading = new TextView(this);

        List<TextView> headingList = new ArrayList<>();
        headingList.add(nameHeading);
        headingList.add(brightnessHeading);
        headingList.add(RADecHeading);
        for (TextView tv: headingList){
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 2f));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(getResources().getDimension(R.dimen.tableHeadingTextSize));
            headings.addView(tv);
        }

        nameHeading.setText(R.string.objectName);
        brightnessHeading.setText(R.string.objectBrightness);
        RADecHeading.setText(R.string.objectRADEC);

        tableLayout.addView(headings);

        for(SpaceObject object: objects) {
            TableRow objectRow = new TableRow(this);
            objectRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

            TextView name = new TextView(this);
            TextView brightness = new TextView(this);
            TextView radec = new TextView(this);

            name.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 2f));
            brightness.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 2f));
            radec.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 2f));

            name.setGravity(Gravity.CENTER);
            brightness.setGravity(Gravity.CENTER);
            radec.setGravity(Gravity.CENTER);

            name.setText(object.getName());
            Double betterBrightness = round(object.getBrightness(), 2);
            brightness.setText(betterBrightness.toString());
            radec.setText(object.getShortenedRADecString());

            objectRow.addView(name);
            objectRow.addView(brightness);
            objectRow.addView(radec);

            tableLayout.addView(objectRow);
        }
    }

    /**
     *
     * @param objectList list of all objects in the object csv file.
     * @param zenith the coordinates for directly above the observer.
     * @return all objects in the list which are visible to the observer.
     */
    private ArrayList<SpaceObject> visibleObjects(ArrayList<SpaceObject> objectList, Coordinate zenith){
        System.out.print("Checking if visible with zenith: ");
        zenith.printCoordinate();
        System.out.println("Degree Range:" + Math.toDegrees(VISIBLE_RANGE_RADIANS));
        ArrayList<SpaceObject> visible = new ArrayList<>();
        for (SpaceObject obj: objectList){
            if(!obj.isStar()) {
                if (!obj.getRA().isEmpty() && !obj.getDec().isEmpty()) {
                    Coordinate objCoordinate = new Coordinate(Double.parseDouble(obj.getRA()), Double.parseDouble(obj.getDec()));

                    //TODO: This needs a rework, perhaps a method like canSee(zenith, objCoord) something is going awry.
                    //if (zenith.canSee(objCoordinate, VISIBLE_RANGE_RADIANS)) {
                    if (coordinatesCanSee(zenith, objCoordinate, VISIBLE_RANGE_RADIANS)) {
                        //System.out.print("Zenith: "); zenith.printCoordinate();
                        double degreeRange = Math.toDegrees(VISIBLE_RANGE_RADIANS);
                        String radecO = obj.getShortenedRADecString();
                        System.out.println(obj.getName() + ": " + radecO + " :: " + zenith.getRADegrees()+"," + zenith.getDecDegrees() + ", " + (Math.abs(zenith.getDecDegrees() - objCoordinate.getDecDegrees()) < degreeRange) + ", " + (Math.abs(zenith.getRADegrees() - objCoordinate.getRADegrees()) < degreeRange));
                        visible.add(obj);
                    }
                }
            }
        }
        System.out.println(visible.size() + " visible objects.");
        return visible;
    }

    private boolean coordinatesCanSee(Coordinate zenith, Coordinate object, double range){
        double degreeRange = Math.toDegrees(range);
        return ((Math.abs(zenith.getDecDegrees() - object.getDecDegrees()) < degreeRange) && (Math.abs(zenith.getRADegrees() - object.getRADegrees()) < degreeRange));
    }

    /**
     *
     * @param objectList list of space objects to be sorted by brightness.
     * @return a list of of count number space objects sorted by brightness.
     */
    private List<SpaceObject> sortBrightest(List<SpaceObject> objectList){
        System.out.println("Sorting by brightness...");
        Collections.sort(objectList, new Comparator<SpaceObject>() {
            @Override
            public int compare(SpaceObject o1, SpaceObject o2) {
                return o1.getBrightness().compareTo(o2.getBrightness());
            }
        });
        System.out.println("Returning top " + objectList.size() + " brightest visible objects.");
        return objectList;

    }

    /**
     * Converts deg,min,sec to decimal hours.
     * @return decimal hours
     */
    private double decimalFromDegrees(double deg, double min, double sec){

        return deg + (min / 60.0) + (sec / 3600.0);
    }

    /**
     * Reads the objects.csv file into a list of objects.
     * The csv file contains NGC, IC and some other smaller catalogue items.
     * @return Array list of space objects from the csv.
     */
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

    /**
     * Pretty print of a space object - TODO: Make this nicer and more informative should be moved to SpaceObject class.
     * @param o the object to pretty print
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

    /**
     * Prints the RA and Dec of an object in D:M:S form
     * @param o the object to print
     */
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
        if(decDeg < 0){
            decDeg = decDeg * -1;
        }
        //decDeg = (dec < 0) ? decDeg *= -1 : decDeg;
        System.out.println("-Coordinates: RA: " + raDeg + ":"+ raMin + ":"+ raSec + " Dec: "  + decDeg + ":"+ decMin + ":"+ decSec);
    }



    /**
     * If the object has visual magnitude or blue magnitude print it,
     * other wise print no data available.
     * @param o the object to print data on.
     * TODO: also print near infrared magnitude
     */
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
     * Get the julian day time from the current day, month, year, and decimal time.
     * This function is full of magic numbers but unfortunately that is the nature of
     * calculating Julian Day Numbers.
     * @return the julian day number
     */
    private double getJulDay(int d, int m, int y, double u){
        if (m <= 2){
            m+=12;
            y-=1;
        }
        int a = y/100;
        int b = a / 4;
        int c = 2-a+b;
        int e = (int) Math.floor(365.25*(y+4716));
        int f = (int) Math.floor(30.6001*(m+1));
        return c + d + e + f -1524.5 + u/24;
    }

    /**
     * Once again more awful magic numbers.
     * @param jd the current julian day time.
     * @return the Greenwich mean sidereal time
     */
    private double gmSiderealTime(double jd){
        //jd = 2458977.5548;
        double t_eph, ut, MJD0, MJD;
        MJD = jd - 2400000.5;
        MJD0 = Math.floor(MJD);
        ut = (MJD - MJD0) * 24.0;
        t_eph = (MJD0 - 51544.5) / 36525.0;
        return 6.697374558 + 1.0027379093 * ut + (8640184.812866 + (0.093104 - 0.0000062 * t_eph) * t_eph) * t_eph / 3600.0;
    }

    /**
     * gets the fraction left after removing the whole number from a double.
     */
    private double remainder(double x){
        x=x-Math.floor(x);
        if(x < 0){
            x++;
        }
        return x;
    }

    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String letter = parent.getItemAtPosition(position).toString();
            switch (letter){
                case "N": latitudeChar = letter;
                case "S": latitudeChar = letter;
                case "E": longitudeChar = letter;
                case "W": longitudeChar = letter;
            }
            System.out.println(letter);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
