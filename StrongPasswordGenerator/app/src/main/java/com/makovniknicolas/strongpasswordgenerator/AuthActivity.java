package com.makovniknicolas.strongpasswordgenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.Executor;

public class AuthActivity extends AppCompatActivity {

    BiometricPrompt biometricPrompt;
    Executor executor;
    BiometricPrompt.PromptInfo promptInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        Button retryPromptButton = findViewById(R.id.authenticateButton);

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "Success!",
                        Toast.LENGTH_SHORT)
                        .show();
                startMain();
            }
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authentication required to access your info")
                .setSubtitle("Log in using your biometric credential")
                .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL | BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build();

        final BiometricManager biometricManager = BiometricManager.from(this);
        if(!(BiometricManager.BIOMETRIC_SUCCESS ==
                biometricManager.canAuthenticate(
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL |
                                BiometricManager.Authenticators.BIOMETRIC_WEAK))) {
            //if the device has no password, pin, pattern, or biometrics, the app will be unlocked
            Toast.makeText(getApplicationContext(), "No pin, pattern, password, or biometrics in place; opening application.",
                    Toast.LENGTH_SHORT)
                    .show();
            startMain();
        }//if
        else {
            biometricPrompt.authenticate(promptInfo);
        }//else

        retryPromptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                biometricPrompt.authenticate(promptInfo);
            }
        });

    }

    private void startMain(){
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }//startMain
}