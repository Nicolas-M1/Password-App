package com.makovniknicolas.strongpasswordgenerator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class SettingsActivity extends AppCompatActivity {

    SharedPreferences sp;
    CheckBox lightCheck, darkCheck;
    Button cancelButton, applyButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = this.getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);
        updateTheme();
        setContentView(R.layout.activity_settings);

        lightCheck = findViewById(R.id.lightCheckbox);
        darkCheck = findViewById(R.id.darkCheckbox);

        cancelButton = findViewById(R.id.cancelSettingsButton);
        applyButton = findViewById(R.id.applySettingsButton);

        setListeners();

    }

    private void updateTheme(){
        boolean isDark = sp.getBoolean(MainActivity.THEME_KEY, false);
        if(isDark){
            setTheme(R.style.DarkTheme);
        }//if
        else{
            setTheme(R.style.LightTheme);
        }//else
    }//isDark


    ///////////////////////////////// LISTENERS ///////////////////////////////////////////
    private void setListeners(){
        lightCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                darkCheck.setChecked(!lightCheck.isChecked());
                //sets dark to be checked to the opposite of this
            }
        });
        darkCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lightCheck.setChecked(!darkCheck.isChecked());
                //sets light check to opposite of this
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                terminateActivity(false);
            }
        });
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                terminateActivity(true);
            }
        });
    }

    private void terminateActivity(boolean updateSettings){
        if(updateSettings){
            SharedPreferences.Editor spEditor = sp.edit();
            spEditor.putBoolean(MainActivity.THEME_KEY, darkCheck.isChecked());
            spEditor.apply();
        }//if updateSettings
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }//terminateActivity
}