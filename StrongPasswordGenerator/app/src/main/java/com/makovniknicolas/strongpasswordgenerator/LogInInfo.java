package com.makovniknicolas.strongpasswordgenerator;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LogInInfo implements Comparable<LogInInfo>{
    private final String application;
    private final String password;

    public LogInInfo(){
        application = null;
        password = null;
    }//LogInInfo

    public LogInInfo(String app, String pass){
        this.application = app;
        this.password = pass;
    }//LogInInfo

    public String getApplication() {
        return application;
    }

    public String getPassword() {
        return password;
    }

    public String toGson(){
        Gson gson = (new GsonBuilder()).create();
        String jsonString = gson.toJson(this);
        return jsonString;
    }//toGson

    public static LogInInfo fromGson(String inputGson){
        Gson gson = new GsonBuilder().create();
        LogInInfo current = gson.fromJson(inputGson,LogInInfo.class);
        return current;
    }//fromGson

    public String makeKey(){
        return this.application;
    }//makeKey

    @Override
    public int compareTo(LogInInfo other) {
        return this.application.compareTo(other.application);
    }
}
