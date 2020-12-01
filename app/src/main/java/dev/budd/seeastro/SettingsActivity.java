package dev.budd.seeastro;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //Toolbar toolbar = findViewById(R.id.toolbar);

//        try{
//            setSupportActionBar(toolbar);
//            ActionBar actionBar = getSupportActionBar();
//            if(actionBar != null){
//                actionBar.setDisplayShowTitleEnabled(false);
//                actionBar.setDisplayHomeAsUpEnabled(true);
//                actionBar.setDisplayShowHomeEnabled(true);
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
