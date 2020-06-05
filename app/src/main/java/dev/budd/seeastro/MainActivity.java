package dev.budd.seeastro;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    public static final boolean DEBUG = false;

    private static final double VISIBLE_RANGE_DEGREES = 45;
    private static  ArrayList<SpaceObject> SPACE_OBJECT_ARRAY_LIST;

    //Greenwich Default Longitude lat/long
    private int latDeg = 33;
    private int latMin = 44;
    private int latSec = 10;
    private String latitudeChar = "S";

    private int longDeg = 151;
    private int longMin = 12;
    private int longSec = 53;
    private String longitudeChar = "E";

    private ToggleButton latToggle;
    private ToggleButton longToggle;

    Coordinate zenith;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SPACE_OBJECT_ARRAY_LIST = readCSVIntoObjects();
        latToggle = findViewById(R.id.latCompassToggle);
        longToggle = findViewById(R.id.longCompassToggle);

        latToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    latitudeChar = latToggle.getTextOn().toString();
                }else{
                    latitudeChar = latToggle.getTextOff().toString();
                }

            }
        });

        longToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    longitudeChar = longToggle.getTextOn().toString();
                }else{
                    longitudeChar = longToggle.getTextOff().toString();
                }
            }
        });

        startAnalysis();
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
     * TODO: This is resource intensive, needs to be run off the main thread.
     * 1. Gets the current settings for the user location preferences.
     * 2. Calculates relevant information inorder to calculate the zenith.
     * 3. Checks if each object is visible from the zenith.
     * 4. Shows the visible astronomical objects.
     */
    private void startAnalysis(){
        setLocationPreferences();

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
        double longitude = getLongitude(longDeg, longMin, longSec);
        double GMST = gmSiderealTime(JD);
        double LMST = 24.0 * remainder((GMST + longitude / 15) / 24);
        double stHour = Math.floor(LMST);
        double stMin = Math.floor(60.0 * remainder(LMST));
        double stSec = Math.round(60 * (60 * remainder(LMST) - stMin));

        double zenithRA = Math.toRadians(decimalFromDegrees(stHour, stMin, stSec));
        double zenithDec = Math.toRadians(getLatitude(latDeg, latMin, latSec));
        zenith = new Coordinate(zenithRA, zenithDec);

        new AnalysisTask().execute(SPACE_OBJECT_ARRAY_LIST);

//        List<SpaceObject> visibleObjects = visibleObjects(SPACE_OBJECT_ARRAY_LIST, zenith);
//        visibleObjects = sortBrightest(visibleObjects);
//        if(visibleObjects.size() > 1000){
//            addToView(visibleObjects.subList(0, 999));
//        }else {
//            addToView(visibleObjects);
//        }

        if (DEBUG){
//            System.out.println("User Location: " + latDeg + ":" + latMin + ":" + latSec + latitudeChar + " " + longDeg + ":" + longMin + ":" + longSec + longitudeChar);
//            System.out.println("LMST: " + "H:" + stHour + " M:" + stMin + " S:" + stSec);
//            System.out.println("Zenith in Deg: " + zenith.getZenithDecimalHours() + ":" + zenith.getDecimalDegrees());
//            System.out.println("Number of visible objects: " + visibleObjects.size());
//            System.out.println("Brightest Object: " + visibleObjects.get(0).getName());
        }
    }

    private class AnalysisTask extends AsyncTask<ArrayList<SpaceObject>, Void, List<SpaceObject>>{

        protected void onPreExecute(){
            TableLayout tableLayout = findViewById(R.id.mainObjectTableLayout);
            tableLayout.removeAllViews();
            showLoading();
        }

        @Override
        protected List<SpaceObject> doInBackground(ArrayList<SpaceObject>... lists) {

            ArrayList<SpaceObject> allObjects = lists[0];
            List<SpaceObject> visibleObjects = visibleObjects(allObjects, zenith);
            visibleObjects = sortBrightest(visibleObjects);
            if(visibleObjects.size() > 1000){
                return visibleObjects.subList(0, 999);
            }else {
                return visibleObjects;
            }
        }

        protected void onPostExecute(List<SpaceObject> visibleObjects){
            hideLoading();
            addToView(visibleObjects);
        }
    }

    private void showLoading(){
        TextView loading = findViewById(R.id.loadingTextView);
        loading.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams params = loading.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        loading.setLayoutParams(params);
    }
    private void hideLoading(){
        TextView loading = findViewById(R.id.loadingTextView);
        loading.setVisibility(View.INVISIBLE);
        ViewGroup.LayoutParams params = loading.getLayoutParams();
        params.height = 0;
        loading.setLayoutParams(params);
    }

    /**
     * Stores the last used location in preferences to preserve location over restarts.
     */
    private void setLocationPreferences(){
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        
        int defaultLatDeg = getResources().getInteger(R.integer.defaultLatDeg);
        int userLatDeg = sharedPreferences.getInt(getString(R.string.userLatDeg), defaultLatDeg);
        int defaultLatMin = getResources().getInteger(R.integer.defaultLatMin);
        int userLatMin = sharedPreferences.getInt(getString(R.string.userLatMin), defaultLatMin);
        int defaultLatSec = getResources().getInteger(R.integer.defaultLatSec);
        int userLatSec = sharedPreferences.getInt(getString(R.string.userLatSec), defaultLatSec);
        String defaultLatCompass = getResources().getString(R.string.defaultLatCompass);
        String userLatCompass = sharedPreferences.getString(getString(R.string.userNorthSouth), defaultLatCompass);

        int defaultLongDeg = getResources().getInteger(R.integer.defaultLongDeg);
        int userLongDeg = sharedPreferences.getInt(getString(R.string.userLongDeg), defaultLongDeg);
        int defaultLongMin = getResources().getInteger(R.integer.defaultLongMin);
        int userLongMin = sharedPreferences.getInt(getString(R.string.userLongMin), defaultLongMin);
        int defaultLongSec = getResources().getInteger(R.integer.defaultLongSec);
        int userLongSec = sharedPreferences.getInt(getString(R.string.userLongSec), defaultLongSec);
        String defaultLongCompass = getResources().getString(R.string.defaultLongCompass);
        String userLongCompass = sharedPreferences.getString(getString(R.string.userEastWest), defaultLongCompass);


        latDeg = userLatDeg;
        latMin = userLatMin;
        latSec = userLatSec;
        latitudeChar = userLatCompass;

        longDeg = userLongDeg;
        longMin = userLongMin;
        longSec = userLongSec;
        longitudeChar = userLongCompass;

        showUsedLocation();
    }

    /**
     * Sets the text on the screen to what is being used in the calculations.
     */
    private void showUsedLocation(){

        EditText latD = findViewById(R.id.latitudeDegreeEditText);
        EditText latM = findViewById(R.id.latitudeMinuteEditText);
        EditText latS = findViewById(R.id.latitudeSecondEditText);
        EditText longD = findViewById(R.id.longitudeDegreeEditText);
        EditText longM = findViewById(R.id.longitudeMinuteEditText);
        EditText longS = findViewById(R.id.longitudeSecondEditText);
        ToggleButton latButton = findViewById(R.id.latCompassToggle);
        ToggleButton longButton = findViewById(R.id.longCompassToggle);

        latD.setText(String.valueOf(latDeg));
        latM.setText(String.valueOf(latMin));
        latS.setText(String.valueOf(latSec));
        longD.setText(String.valueOf(longDeg));
        longM.setText(String.valueOf(longMin));
        longS.setText(String.valueOf(longSec));

        if(!latButton.getText().toString().matches(latitudeChar)){
            latButton.toggle();
        }
        if(!longButton.getText().toString().matches(longitudeChar)){
            longButton.toggle();
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

    private double getLongitude(double deg, double min, double sec){
        if (longitudeChar.matches("W")){
            deg *= -1;
        }
        return decimalFromDegrees(deg, min, sec);
    }
    private double getLatitude(double deg, double min, double sec){
        if (latitudeChar.matches("S")){
            deg *= -1;
        }
        return decimalFromDegrees(deg, min, sec);
    }

    /**
     * Once again more awful magic numbers.
     * @param jd the current julian day time.
     * @return the Greenwich mean sidereal time
     */
    private double gmSiderealTime(double jd){
        double t_eph, ut, MJD0, MJD;
        MJD = jd - 2400000.5;
        MJD0 = Math.floor(MJD);
        ut = (MJD - MJD0) * 24.0;
        t_eph = (MJD0 - 51544.5) / 36525.0;
        return 6.697374558 + 1.0027379093 * ut + (8640184.812866 + (0.093104 - 0.0000062 * t_eph) * t_eph) * t_eph / 3600.0;
    }

    /**
     * @param objectList list of all objects in the object csv file.
     * @param zenith the coordinates for directly above the observer.
     * @return all objects in the list which are visible to the observer.
     */
    private ArrayList<SpaceObject> visibleObjects(ArrayList<SpaceObject> objectList, Coordinate zenith){
        ArrayList<SpaceObject> visible = new ArrayList<>();
        for (SpaceObject obj: objectList){
            if (!obj.isOther() && !obj.isStar() && !obj.getRA().isEmpty() && !obj.getDec().isEmpty()) {
                Coordinate objCoordinate = new Coordinate(Double.parseDouble(obj.getRA()), Double.parseDouble(obj.getDec()));
                if (canSee(zenith, objCoordinate)) {
                    visible.add(obj);
                }
            }
        }
        return visible;
    }


    /**
     * @param zen the zenith RA/Dec coordinate object relative to the inputted lat/long
     * @param obj the RA/Dec coordinate for the astronomical object
     * @return True if the zenith and the object are within the global viewable range
     */
    private boolean canSee(Coordinate zen, Coordinate obj){
        double alpha1;
        double alpha2;
        double delta1;
        double delta2;

        alpha1 = zen.getZenithDecimalHours();
        alpha1 = Math.toRadians(alpha1);

        delta1 = zen.getDecimalDegrees();
        delta1 = Math.toRadians(delta1);

        alpha2 = obj.getDecimalHours();
        alpha2 = Math.toRadians(alpha2);

        delta2 = obj.getDecimalDegrees();
        delta2 = Math.toRadians(delta2);

        double t =  Math.sin(delta1)*Math.sin(delta2) + Math.cos(delta1)*Math.cos(delta2)*Math.cos(15*(alpha1 - alpha2));
        double answer = arccos(t);

        return answer < VISIBLE_RANGE_DEGREES;

    }

    private double d2r(double d){
        return d * Math.PI / 180;
    }
    private double r2d(double d){
        return d * 180 / Math.PI;
    }
    private double arccos(double d){
        double ret;
        if (d == 1){
            ret = 0;
        }else{
            ret = 1.570796327 - Math.atan(d / Math.sqrt(-1 * d * d + 1));
        }
        return r2d(ret);
    }

    /**
     *
     * @param objectList list of space objects to be sorted by brightness.
     * @return a list of of count number space objects sorted by brightness.
     */
    private List<SpaceObject> sortBrightest(List<SpaceObject> objectList){
        Collections.sort(objectList, new Comparator<SpaceObject>() {
            @Override
            public int compare(SpaceObject o1, SpaceObject o2) {
                return o1.getBrightness().compareTo(o2.getBrightness());
            }
        });
        return objectList;
    }

    /**
     *
     * Shows the visible space objects on the screen in a useful fashion.
     * @param objects list of visible SpaceObjects
     */
    private void addToView(List<SpaceObject> objects){
        TableLayout tableLayout = findViewById(R.id.mainObjectTableLayout);
        tableLayout.removeAllViews();
        TableRow headings = new TableRow(this);
        headings.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

        TextView nameHeading = new TextView(this);
        TextView typeHeading = new TextView(this);
        TextView brightnessHeading = new TextView(this);
        TextView RADecHeading = new TextView(this);

        List<TextView> headingList = new ArrayList<>();
        headingList.add(nameHeading);
        headingList.add(typeHeading);
        headingList.add(brightnessHeading);
        headingList.add(RADecHeading);
        for (TextView tv: headingList){
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 2f));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(getResources().getDimension(R.dimen.tableHeadingTextSize));
            headings.addView(tv);
            tv.setTextColor(getResources().getColor(R.color.lightBackground));
        }
        headings.setBackgroundColor(getResources().getColor(R.color.headingBackground));

        nameHeading.setText(R.string.objectName);
        typeHeading.setText(R.string.objectType);
        brightnessHeading.setText(R.string.objectBrightness);
        RADecHeading.setText(R.string.objectRADEC);

        tableLayout.addView(headings);
        int count = 0;
        for(SpaceObject object: objects) {
            //System.out.println(object.getName() + ": " + degreeFromDecimal(Double.parseDouble(object.getRA()), Double.parseDouble(object.getDec())));
            TableRow objectRow = new TableRow(this);
            objectRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
            objectRow.setPadding(0, 20, 0, 20);

            TextView name = new TextView(this);
            TextView type = new TextView(this);
            TextView brightness = new TextView(this);
            TextView radec = new TextView(this);

            List<TextView> info = new ArrayList<>();
            info.add(name);
            info.add(type);
            info.add(brightness);
            info.add(radec);
            for(TextView tv: info){
                tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 2f));
                tv.setGravity(Gravity.CENTER);
            }

            name.setText(object.getName());
            type.setText(object.getType());
            brightness.setText(String.format(Locale.US, "%.2f", object.getBrightness()));
            radec.setText(object.getShortenedRADecString());

            objectRow.addView(name);
            objectRow.addView(type);
            objectRow.addView(brightness);
            objectRow.addView(radec);

            if (count % 2 == 0) {
                objectRow.setBackgroundColor(getResources().getColor(R.color.mediumBackground));
            }else{
                objectRow.setBackgroundColor(getResources().getColor(R.color.lightBackground));
            }
            count++;

            tableLayout.addView(objectRow);
        }
    }

    /**
     * When the run button is pressed retest with user inputted values
     * @param v the current view
     */
    public void setNewVariables(View v){


        EditText latD = findViewById(R.id.latitudeDegreeEditText);
        EditText latM = findViewById(R.id.latitudeMinuteEditText);
        EditText latS = findViewById(R.id.latitudeSecondEditText);
        EditText longD = findViewById(R.id.longitudeDegreeEditText);
        EditText longM = findViewById(R.id.longitudeMinuteEditText);
        EditText longS = findViewById(R.id.longitudeSecondEditText);
        ToggleButton latToggle = findViewById(R.id.latCompassToggle);
        ToggleButton longToggle = findViewById(R.id.longCompassToggle);

        try{
            latDeg = Integer.parseInt(latD.getText().toString());
            latMin = Integer.parseInt(latM.getText().toString());
            latSec = Integer.parseInt(latS.getText().toString());
            longDeg = Integer.parseInt(longD.getText().toString());
            longMin = Integer.parseInt(longM.getText().toString());
            longSec = Integer.parseInt(longS.getText().toString());
            latitudeChar = latToggle.getText().toString();
            longitudeChar = longToggle.getText().toString();

        }catch(NumberFormatException e){
            e.printStackTrace(System.err);
        }

        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(getString(R.string.userLatDeg), latDeg);
        editor.putInt(getString(R.string.userLatMin), latMin);
        editor.putInt(getString(R.string.userLatSec), latSec);
        editor.putInt(getString(R.string.userLongDeg), longDeg);
        editor.putInt(getString(R.string.userLongMin), longMin);
        editor.putInt(getString(R.string.userLongSec), longSec);
        editor.putString(getString(R.string.userNorthSouth), latitudeChar);
        editor.putString(getString(R.string.userEastWest), longitudeChar);

        editor.apply();

        Map<String, ?> temp = sharedPreferences.getAll();
        System.out.println("New Vars: " + temp);

            

        System.out.println("Set new variables...");
        startAnalysis();
    }


    /**
     * Converts deg,min,sec to decimal hours.
     * @return decimal hours
     */
    private double decimalFromDegrees(double deg, double min, double sec){

        return deg + (min / 60.0) + (sec / 3600.0);
    }

    private String degreeFromDecimal( double ra, double dec){
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

}
