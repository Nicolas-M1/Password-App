package com.makovniknicolas.strongpasswordgenerator;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executor;

import static android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //////////// TUTORIAL ///////////


    public static final String ENCRYPTED_SP_NAME = "logInStorage";
    public static final String PREFERENCES_NAME = "com.makovniknicolas.strongpasswordgenerator.preferences";
    public static final int KEY_SIZE = 256;
    public static final String THEME_KEY = "app_theme";
    private boolean onCreateFinished = false;

    LogInInfo[] infoArray;
    PriorityQueue<LogInInfo> infoPQ;
    ListView viewList;
    LinearLayout infoLinearLayout;
    int TEXT_SIZE = 20; //sp
    InfoRemoverFragment remover;
    Button openFragmentButton, openSettingsButton;

    private boolean isAuthenticated = true;
    private boolean removerFragmentAdded = false;
    private boolean isDarkTheme = false;

    FragmentTransaction ft;
    int itemCount = 0;
    SharedPreferences sharedPrefs;
    SharedPreferences encryptedData;
    HashMap<Button,LogInInfo> buttonToLogInInfo;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPrefs = getSharedPreferences("com.makovniknicolas.strongpasswordgenerator.preferences", Context.MODE_PRIVATE);
        isDarkTheme = determineDarkTheme();
        updateTheme();
        super.onCreate(savedInstanceState);


        try{ setEncryptedSharedPrefs(); }
        catch(GeneralSecurityException e){
            Toast toast = Toast.makeText(getApplicationContext(),"Failed to decrypt data", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        catch (IOException e){
            Toast toast = Toast.makeText(getApplicationContext(),"Failed to retrieve data, something is wrong with the application files.",
                    Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        setContentView(R.layout.activity_main);
        remover= InfoRemoverFragment.newInstance();
        ft = getSupportFragmentManager().beginTransaction();
        infoLinearLayout = findViewById(R.id.info_linear_layout);
        buttonToLogInInfo = new HashMap<Button,LogInInfo>();


        openFragmentButton = findViewById(R.id.delete_button_main);
        openSettingsButton = findViewById(R.id.openSettingsButton);
        openSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });

        transferFromPQ();
        onCreateFinished = true;
    }//onCreate

    public void addInfo(View view) {
        endFragment();
        startActivity(new Intent(this, AddInfoActivity.class));
    }

    private void openSettings(){
        finish();
        startActivity(new Intent(this, SettingsActivity.class));
    }//openSettings


    @Override
    protected void onStart() {
        super.onStart();
        if(onCreateFinished) {
            infoLinearLayout.removeAllViews();
            transferFromPQ();
        }//if
    }//onStart

    private void transferFromPQ() {
        updateInfo();

        Iterator<LogInInfo> itr = infoPQ.iterator();
        while(itr.hasNext()){
            LogInInfo current = itr.next();
            View singleInfoView = createView(current);
            infoLinearLayout.addView(singleInfoView);
        }//while
    }

    // updates the contents of the info array to be the same as the stored info
    private void updateInfo() {
        Map<String, LogInInfo> spValues = getEncryptedChildren();
        infoPQ = new PriorityQueue<LogInInfo>();
        Set<String> keys = spValues.keySet();
        for(String currentKey : keys){
            LogInInfo currentInfo = spValues.get(currentKey);
            infoPQ.add(currentInfo);
        }//for
    }


    //Creates a view for the correspondingInfo then returns it, does not add it to the linear layout
    private View createView(LogInInfo correspondingInfo){
        Context context = getApplicationContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View singleInfoView = inflater.inflate(R.layout.single_info_layout,null,false);

        TextView description = singleInfoView.findViewById(R.id.singleInfo_descriptionText);
        TextView data = singleInfoView.findViewById(R.id.singleInfo_infoText);
        Button actionButton = singleInfoView.findViewById(R.id.singleInfo_button);

        String descriptionStr = ("Your "+correspondingInfo.getApplication()+" account:");
        description.setText(descriptionStr);

        String infoStr = "Password: "+correspondingInfo.getPassword();
        data.setText(infoStr);

        buttonToLogInInfo.put(actionButton, correspondingInfo);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogInInfo current = buttonToLogInInfo.get(v);
                addClip(current.getPassword());
            }
        });

        boolean isDarkThemed = isDarkTheme();
        if(isDarkThemed) {
            singleInfoView.setBackground(getDrawable(R.drawable.dark_window_background));
            description.setTextAppearance(R.style.TextDark);
            data.setTextAppearance(R.style.TextDark);
            actionButton.setTextColor(getResources().getColor(R.color.darkThemeText,null));
            actionButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.darkButtonTint));
        }//if
        else{
            singleInfoView.setBackground(getDrawable(R.drawable.light_window_background));
            description.setTextAppearance(R.style.TextLight);
            data.setTextAppearance(R.style.TextLight);
            actionButton.setTextColor(getResources().getColor(R.color.colorText,null));
        }//else


        return singleInfoView;
    }//createView

    private void addClip(String input){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied From My App",input);
        clipboard.setPrimaryClip(clip);
    }//addClip

    public void openFragment(View view) { //called when edit list button is clicked to open or close the editor fragment
        if(!isAuthenticated){
            Toast.makeText(getApplicationContext(), "You must be authenticated to perform actions",
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }//if
        getFragmentManager().executePendingTransactions(); //prevents crash
        if(removerFragmentAdded){
            endFragment();
        }//if
        else {
            removerFragmentAdded = true;
            ft.replace(R.id.fragmentPlaceholder, remover);
            ft.commitNow();
        }//else
    }

    public void endFragment(){
        getSupportFragmentManager().beginTransaction().remove(remover).commit();
        removerFragmentAdded = false;
    }//endFragment

    private void enableButtons(boolean enable){
        Button addInfoActivityButton = findViewById(R.id.add_info_button);
        Button deleteInfoFragmentButton = openFragmentButton;
        addInfoActivityButton.setEnabled(enable);
        deleteInfoFragmentButton.setEnabled(enable);
        openSettingsButton.setEnabled(enable);
    }//enableButtons


    @Override
    public void onClick(View v) { //used by delete selected button in remover fragment
        Map<CheckBox, LogInInfo> checkToInfo = remover.getCheckToInfoMap();
        SharedPreferences.Editor spEditor = encryptedData.edit();
        for(CheckBox key : checkToInfo.keySet()){
            //iterates through checkboxes, if they are checked, the corresponding log in info is deleted from shared prefs
            if(key.isChecked()){
                String infoSpKey = checkToInfo.get(key).makeKey();
                spEditor.remove(infoSpKey);
            }//if
        }//for
        spEditor.apply();
        endFragment();
        infoLinearLayout.removeAllViews();
        transferFromPQ();
    }

    public void appendToFile(String output){

    }//appendToFile

    // returns the Map from the encrypted shared preferences file
    private Map<String, LogInInfo> getEncryptedChildren(){

        Map<String,String> gsonMap = (Map<String, String>) encryptedData.getAll();
        Map<String, LogInInfo> cleanedMap = new TreeMap<String,LogInInfo>();
        for(String key : gsonMap.keySet()){
            LogInInfo currentInfo = LogInInfo.fromGson(gsonMap.get(key));
            cleanedMap.put(key, currentInfo);
        }//for
        return cleanedMap;
    }//getEncryptedChildren

    public PriorityQueue<LogInInfo> getInfoPQ(){
        return infoPQ;
    }

    private void setEncryptedSharedPrefs() throws GeneralSecurityException, IOException {
        Context c = getApplication();
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
            MasterKey.DEFAULT_MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .build();

        MasterKey masterKey = new MasterKey.Builder(MainActivity.this)
                .setKeyGenParameterSpec(spec)
                .build();
        encryptedData = EncryptedSharedPreferences.create(
                getApplicationContext(),
                MainActivity.ENCRYPTED_SP_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }//setEncryptedSharedPrefs

    public BiometricPrompt getBiometricPrompt(){
        return this.biometricPrompt;
    }//getBiometricPrompt

    public BiometricPrompt.PromptInfo getPromptInfo(){
        return this.promptInfo;
    }//getPromptInfo

    private void updateTheme(){
        boolean isDark = isDarkTheme();

        if(isDark){
            setTheme(R.style.DarkTheme);
        }//if
        else{
            setTheme(R.style.LightTheme);
        }//else
    }//isDark
    private boolean determineDarkTheme(){
        SharedPreferences sp = this.getSharedPreferences("com.makovniknicolas.strongpasswordgenerator.preferences", Context.MODE_PRIVATE);
        return sp.getBoolean(THEME_KEY, false);
    }//isDark

    public boolean isDarkTheme(){
        return isDarkTheme;
    }//isDarkTheme
}