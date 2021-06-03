package com.makovniknicolas.strongpasswordgenerator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import static com.makovniknicolas.strongpasswordgenerator.R.id.account_input;

public class AddInfoActivity extends AppCompatActivity {

    EditText websiteInput;

    String currentPassword;

    Toast toast;

    Fragment randomFragment, manualFragment;

    boolean isManualFragment = false;

    boolean isDarkTheme;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isDarkTheme = determineDarkTheme();
        updateTheme();
        randomFragment = RandomPassFragment.newInstance();
        manualFragment = ManualPassFragment.newInstance();
        setContentView(R.layout.activity_add_info);
        websiteInput = findViewById(account_input);
        toast = Toast.makeText(getApplicationContext(),"", Toast.LENGTH_SHORT);
        randomFragment(); //puts the random passcode generator fragment in which is the default for this app

    }

    public void displayToast(String toDisplay){
        toast.setText(toDisplay);
        toast.show();
    }//displayToast


    public void manualFragment(){
        isManualFragment = true;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.passcodeFragmentFrame, manualFragment);
        ft.commit();
    }//manualFragment

    public void randomFragment(){
        isManualFragment = false;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.passcodeFragmentFrame, randomFragment);
        ft.commit();
    }//randomFragment


    public void saveInfo(View view) { //called by the button
        LogInInfo currentInfo = getInfo();
        if(currentInfo.getPassword() == null || currentInfo.getPassword().length() == 0){
            toast.setText("Invalid password");
            toast.show();
            return;
        }//if
        else if(currentInfo.getApplication().trim().length() == 0){
            toast.setText("No website or app entered");
            toast.show();
            return;
        }//else if

        String gsonInfo = currentInfo.toGson();

        try{ addData(currentInfo.makeKey(), gsonInfo); }
        catch(Exception e){
            toast.setText("Failed to save data, encryption process failed or file may have been corrupted");
            toast.show();
            return;
        }

        finish();
    }

    private LogInInfo getInfo(){
        EditText input = ((EditText)findViewById(R.id.account_input));
        String accountFor = String.valueOf(input.getText());
        LogInInfo created = new LogInInfo(accountFor.trim(), getCurrentPassword());
        return created;
    }//getInfo

    public void setCurrentPassword(String setTo) {
        currentPassword = setTo;
    }

    public String getCurrentPassword() {
        if(isManualFragment){
            currentPassword = String.valueOf(((ManualPassFragment)manualFragment).getPassword());
        }//if
        return currentPassword;
    }

    public void addData(String key, String output) throws GeneralSecurityException, IOException {
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(MainActivity.KEY_SIZE)
                .build();

        MasterKey masterKey = new MasterKey.Builder(this)
                .setKeyGenParameterSpec(spec)
                .build();
        SharedPreferences.Editor encryptedSpEditor = EncryptedSharedPreferences.create(
                getApplicationContext(),
                MainActivity.ENCRYPTED_SP_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ).edit();

        encryptedSpEditor.putString(key,output);

        encryptedSpEditor.apply();
    }//appendToFile


    private void updateTheme(){
        boolean isDark = isDarkTheme();

        if(isDark){
            setTheme(R.style.DarkTheme);
        }//if
        else{
            setTheme(R.style.LightTheme);
        }//else
    }//isDark

    public boolean isDarkTheme(){
        return isDarkTheme;
    }//isDarkTheme

    private boolean determineDarkTheme(){
        SharedPreferences sp = this.getSharedPreferences("com.makovniknicolas.strongpasswordgenerator.preferences", Context.MODE_PRIVATE);
        return sp.getBoolean(MainActivity.THEME_KEY, false);
    }//isDark
}