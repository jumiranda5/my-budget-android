package com.jgm.mybudgetapp;


import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.ads.MobileAds;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.jgm.mybudgetapp.databinding.ActivityLoginBinding;
import com.jgm.mybudgetapp.utils.Populate;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private static final String LOG = "debug-login";
    private static final String LOG_UMP = "debug-consent";
    private static final String LOG_AUTH = "debug-auth";
    private boolean keep = true;

    // Consent
    private ConsentInformation consentInformation;

    // Authentication
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    // UI
    private ActivityLoginBinding binding;
    private Button mLoginButton;

    private void setBinding() {
        mLoginButton = binding.loginButton;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(Tags.LOG_LIFECYCLE, "Login Activity onCreate");

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setBinding();

        if (savedInstanceState == null) handleSplashScreenDelay(splashScreen);


    }

    /* ---------------------------------------------------------------------------------------------
                                          SPLASH SCREEN
     -------------------------------------------------------------------------------------------- */

    private void handleSplashScreenDelay(SplashScreen splashScreen) {

        Log.d(LOG, "Init splash screen timer");

        splashScreen.setKeepOnScreenCondition(() -> keep);

        mLoginButton.setEnabled(false);

        requestLatestConsentInformation();

        int DELAY = 2000;
        Handler handler = new Handler();
        handler.postDelayed(() -> {

            Populate.initDefaultAccounts(this);
            Populate.initDefaultCategories(this);

            checkDeviceAuth();
            initAuthPrompt();

            keep = false;

        }, DELAY);
    }


    /* ---------------------------------------------------------------------------------------------
                                          AUTHENTICATION
     -------------------------------------------------------------------------------------------- */

    // Check if the device supports biometric authentication
    private void checkDeviceAuth() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(LOG_AUTH, "App can authenticate using biometrics.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e(LOG_AUTH, "No biometric features available on this device.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e(LOG_AUTH, "Biometric features are currently unavailable.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.e(LOG_AUTH, "Biometric features are currently not enrolled.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                Log.e(LOG_AUTH, "Biometric features are unsupported.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                Log.e(LOG_AUTH, "Biometric security update required.");
                break;
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                Log.e(LOG_AUTH, "Biometric status unknown.");
                break;
        }
    }

    // Show the prompt
    private void initAuthPrompt() {

        // todo: show message when auth fails

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(LoginActivity.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.e(LOG_AUTH, "onAuthenticationError: " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d(LOG_AUTH, "Successfully authenticated.");
                startMainActivity();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.d(LOG_AUTH, "onAuthenticationFailed");
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                .build();

        mLoginButton.setOnClickListener(v -> biometricPrompt.authenticate(promptInfo));

    }

    private void startMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }


    /* ---------------------------------------------------------------------------------------------
                                                ADS
     -------------------------------------------------------------------------------------------- */

    private void initializeAdsSdk() {
        // Disable login button until ads library is initialized
        MobileAds.initialize(this, initializationStatus -> {
            Log.d(LOG_UMP, "MobileAds initialized");
            mLoginButton.setEnabled(true);
        });
    }

    /* ---------------------------------------------------------------------------------------------
                                             ADS CONSENT
     -------------------------------------------------------------------------------------------- */

    private void requestLatestConsentInformation() {
        Log.d(LOG_UMP, "LOADING CONSENT");

        // Set tag for underage of consent. false means users are not underage.
        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        Log.d(LOG_UMP, "Consent status: " + consentInformation.getConsentStatus());

        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                (ConsentInformation.OnConsentInfoUpdateSuccessListener) () -> {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                            this,
                            (ConsentForm.OnConsentFormDismissedListener) loadAndShowError -> {
                                if (loadAndShowError != null) {
                                    // Consent gathering failed.
                                    Log.w(LOG_UMP, String.format("%s: %s",
                                            loadAndShowError.getErrorCode(),
                                            loadAndShowError.getMessage()));
                                    mLoginButton.setEnabled(true);
                                }

                                // Consent has been gathered.
                                if (consentInformation.canRequestAds()) {
                                    initializeAdsSdk();
                                }
                            }
                    );
                },
                (ConsentInformation.OnConsentInfoUpdateFailureListener) requestConsentError -> {
                    // Consent gathering failed.
                    Log.w(LOG_UMP, String.format("%s: %s",
                            requestConsentError.getErrorCode(),
                            requestConsentError.getMessage()));

                    mLoginButton.setEnabled(true);
                });

        // Check if you can initialize the Google Mobile Ads SDK in parallel
        // while checking for new consent information. Consent obtained in
        // the previous session can be used to request ads.
        if (consentInformation.canRequestAds()) {
            initializeAdsSdk();
        }
    }

}