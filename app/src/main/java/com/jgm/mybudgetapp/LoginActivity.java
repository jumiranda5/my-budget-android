package com.jgm.mybudgetapp;


import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import static com.android.billingclient.api.BillingClient.FeatureType.PRODUCT_DETAILS;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import android.app.KeyguardManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.gms.ads.MobileAds;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.jgm.mybudgetapp.databinding.ActivityLoginBinding;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;
import com.jgm.mybudgetapp.utils.HideKeyboard;
import com.jgm.mybudgetapp.utils.Populate;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    // Login flow:
    // - check device credentials =>
    //      if ok, proceed to auth prompt.
    //      Else, stop and show error
    // - start billing client: query purchases =>
    //      if premium user, proceed to Main activity.
    //      Else, init ads and proceed to Main Activity

    private static final String LOG = "debug-login";
    private static final String LOG_UMP = "debug-consent";
    private static final String LOG_AUTH = "debug-auth";
    private static final String LOG_BILLING = "debug-billing";
    private boolean keep = true;
    private boolean isIapStep = false;
    private boolean isAdsStep = false;

    // Consent
    private ConsentInformation consentInformation;

    // Billing
    private BillingClient billingClient;

    // UI
    private ActivityLoginBinding binding;
    private ImageView mLockIcon;
    private TextView mProgressText, mAuthText;
    private Button mLoginButton, mContinueButton, mRetryButton;
    private Group mGroupProgress, mGroupAuthWarning, mGroupIapWarning, mGroupAdsWarning;
    private EditText mTestPassword;
    private ImageButton mTestLogin;

    private void setBinding() {
        mLockIcon = binding.loginIcon;
        mProgressText = binding.loginProgressBarText;
        mLoginButton = binding.loginButton;
        mRetryButton = binding.loginRetryButton;
        mContinueButton = binding.loginContinueButton;
        mGroupProgress = binding.groupLoginProgress;
        mGroupAuthWarning = binding.groupLoginAuth;
        mGroupIapWarning = binding.groupLoginIap;
        mGroupAdsWarning = binding.groupLoginAds;
        mAuthText = binding.loginAuthWarning;
        mTestPassword = binding.loginTestInput;
        mTestLogin = binding.loginTestButton;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        Log.i(Tags.LOG_LIFECYCLE, "Login Activity onCreate");

        initAppOpenCount();

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setBinding();

        Log.d(LOG, "Clear premium user on shared prefs");
        SettingsPrefs.setSettingsPrefsBoolean(this, Tags.keyIsPremium, false);
        SettingsPrefs.setSettingsPrefsString(this, Tags.keyIapOrder, "");

        resetVisibilities();
        mGroupProgress.setVisibility(View.GONE);

        initButtons();

        handleSplashScreenDelay(splashScreen);

    }

    private void startMainActivity() {
        resetVisibilities();
        mGroupProgress.setVisibility(View.VISIBLE);
        mProgressText.setText(getString(R.string.msg_loading_home));
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void initAppOpenCount() {
        Log.d(LOG, "Set dark/light mode");
        int appOpenCount = SettingsPrefs.getSettingsPrefsInteger(this, "appOpenCount");
        boolean isDark;
        if (appOpenCount > 0) {
            isDark = SettingsPrefs.getSettingsPrefsBoolean(this, Tags.keyDarkMode);
        }
        else {
            isDark = true;
            SettingsPrefs.setSettingsPrefsBoolean(this, Tags.keyDarkMode, true);
        }
        switchDarkMode(isDark);
        appOpenCount++;
        SettingsPrefs.setSettingsPrefsInteger(this, "appOpenCount", appOpenCount);
    }

    /* ---------------------------------------------------------------------------------------------
                                                  UI
    --------------------------------------------------------------------------------------------- */

    private void switchDarkMode(boolean isDark) {
        Log.d(LOG, "=> switchDarkMode");

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (isDark) {
            Log.d(LOG, "Dark Mode");
            if (currentNightMode != Configuration.UI_MODE_NIGHT_YES)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            Log.d(LOG, "Light Mode");
            if (currentNightMode != Configuration.UI_MODE_NIGHT_NO)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // Loading

    private void showAuthLoading() {
        // show button to relaunch auth dialog in case the activity is stopped in the middle of the process
        mLoginButton.setVisibility(View.VISIBLE);
    }

    private void showIapLoading() {
        mGroupProgress.setVisibility(View.VISIBLE);
        mProgressText.setText(getString(R.string.msg_loading_iap));
    }

    private void showAdsLoading() {
        mGroupProgress.setVisibility(View.VISIBLE);
        mProgressText.setText(getString(R.string.msg_loading_ads));
        mRetryButton.setVisibility(View.GONE);
        mContinueButton.setVisibility(View.GONE);
    }

    // Error

    private void showAuthError(String msg) {
        mGroupAuthWarning.setVisibility(View.VISIBLE);
        mGroupProgress.setVisibility(View.GONE);
        mLoginButton.setVisibility(View.VISIBLE);
        mAuthText.setText(msg);
    }

    private void showIapError() {
        mGroupProgress.setVisibility(View.GONE);
        mGroupIapWarning.setVisibility(View.VISIBLE);
        mRetryButton.setVisibility(View.VISIBLE);
        mContinueButton.setVisibility(View.VISIBLE);
    }

    private void showAdsError() {
        mGroupProgress.setVisibility(View.GONE);
        mGroupAdsWarning.setVisibility(View.VISIBLE);
        mRetryButton.setVisibility(View.VISIBLE);
        mContinueButton.setVisibility(View.VISIBLE);
    }

    // Success

    private void showAuthSuccess() {
        mLoginButton.setVisibility(View.GONE);
        mGroupAuthWarning.setVisibility(View.GONE);
        mLockIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_48_lock_open_fill0_200));
        mLockIcon.setImageTintList(ContextCompat.getColorStateList(this, R.color.success));
        mLockIcon.setContentDescription(getString(R.string.title_logged));
    }


    // ----------

    private void resetVisibilities() {
        mGroupProgress.setVisibility(View.GONE);
        mGroupAuthWarning.setVisibility(View.GONE);
        mGroupIapWarning.setVisibility(View.GONE);
        mGroupAdsWarning.setVisibility(View.GONE);
        mLoginButton.setVisibility(View.GONE);
        mRetryButton.setVisibility(View.GONE);
        mContinueButton.setVisibility(View.GONE);
    }

    private void initButtons() {
        mLoginButton.setOnClickListener(v -> {
            resetVisibilities();
            checkDeviceCredentials();
        });

        mRetryButton.setOnClickListener(v -> {
            resetVisibilities();
            connectToGooglePlay();
        });

        mContinueButton.setOnClickListener(v -> {
            resetVisibilities();
            // continue from billing client error => init ads
            if (isIapStep) requestLatestConsentInformation();
            // continue from ads error => open main activity
            else if (isAdsStep) startMainActivity();
        });
    }


    /* ---------------------------------------------------------------------------------------------
                                          SPLASH SCREEN
     -------------------------------------------------------------------------------------------- */

    private void handleSplashScreenDelay(SplashScreen splashScreen) {

        Log.d(LOG, "Init splash screen timer");

        splashScreen.setKeepOnScreenCondition(() -> keep);

        initBillingClient();
        Populate.initDefaultAccounts(this);
        Populate.initDefaultCategories(this);

        int DELAY = 500;
        Handler handler = new Handler();
        handler.postDelayed(() -> {

            checkDeviceCredentials();

            keep = false;

            Log.d(LOG, "Finish splash screen timer");

        }, DELAY);
    }

    /* ---------------------------------------------------------------------------------------------
                                          AUTHENTICATION
     -------------------------------------------------------------------------------------------- */

    // Check if the device supports biometric authentication
    private void checkDeviceCredentials() {
        BiometricManager biometricManager = BiometricManager.from(this);

        int canAuthenticateResponse;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            boolean isSecure = keyguardManager.isDeviceSecure();
            Log.w(LOG_AUTH, "API 29 isDeviceSecure: " + isSecure);
            if (isSecure) canAuthenticateResponse = BiometricManager.BIOMETRIC_SUCCESS;
            else canAuthenticateResponse = 11;
        }
        else {
            canAuthenticateResponse = biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
        }

        if (canAuthenticateResponse == BiometricManager.BIOMETRIC_SUCCESS) {
            Log.d(LOG_AUTH, "App can authenticate using biometrics.");
            initAuthPrompt();
        }
        else {
            Log.e(LOG_AUTH, "Biometric features are currently not available.");
            String msg = getString(R.string.auth_credentials_not_configured);
            showAuthError(msg);
            allowTesting();
        }
    }

    private void allowTesting() {
        final int[] clickCount = {0};
        mAuthText.setOnClickListener(v -> {
            clickCount[0]++;
            if (clickCount[0] == 7) {
                clickCount[0] = 0;
                mTestPassword.setVisibility(View.VISIBLE);
                mTestLogin.setVisibility(View.VISIBLE);
                mTestLogin.setOnClickListener(v2 -> {
                    String pwd = mTestPassword.getText().toString().trim().toLowerCase();
                    HideKeyboard.hide(this);
                    if (pwd.equals(getString(R.string.test))) {
                        mTestPassword.setVisibility(View.GONE);
                        mTestLogin.setVisibility(View.GONE);
                        showAuthSuccess();
                        connectToGooglePlay();
                    }
                    else {
                        mTestPassword.setText("");
                        mTestPassword.setHint("Wrong password");
                    }
                });
            }
        });
    }

    // Show the prompt
    private void initAuthPrompt() {
        Log.d(LOG_AUTH, "=> Init auth prompt");
        // Authentication
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Log.e(LOG_AUTH, "onAuthenticationError: " + errorCode + " / " + errString);

                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q && errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS) {
                            mLoginButton.setOnClickListener(v -> tryAuthWithPinOnOlderApis());
                            allowTesting();
                        }

                        String msg = getString(R.string.auth_fail);
                        showAuthError(msg);

                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Log.d(LOG_AUTH, "Successfully authenticated.");
                        showAuthSuccess();
                        connectToGooglePlay();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Log.d(LOG_AUTH, "onAuthenticationFailed");
                        String msg = getString(R.string.auth_fail);
                        showAuthError(msg);
                    }
                });

        try {
            // Init prompt
            BiometricPrompt.PromptInfo promptInfo;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                promptInfo = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle(getString(R.string.title_login))
                        .setDescription(getString(R.string.description_login))
                        .setNegativeButtonText(getString(R.string.action_cancel))
                        .build();
            }
            else {
                promptInfo = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle(getString(R.string.title_login))
                        .setDescription(getString(R.string.description_login))
                        .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                        .build();
            }
            biometricPrompt.authenticate(promptInfo);
            showAuthLoading();
        }
        catch (Exception e) {
            Log.e(LOG_AUTH, "BiometricPrompt error: " + e);
            showAuthError(getString(R.string.auth_fail));
        }

    }

    private void tryAuthWithPinOnOlderApis() {
        // reset login button
        initBillingClient();

        Log.d(LOG_AUTH, "=> Init auth prompt 2");
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt2 = new BiometricPrompt(LoginActivity.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Log.e(LOG_AUTH, "onAuthenticationError: " + errorCode + " / " + errString);
                        String msg = getString(R.string.auth_fail);
                        showAuthError(msg);
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Log.d(LOG_AUTH, "Successfully authenticated.");
                        showAuthSuccess();
                        connectToGooglePlay();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Log.d(LOG_AUTH, "onAuthenticationFailed");
                        String msg = getString(R.string.auth_fail);
                        showAuthError(msg);
                    }
                });

        try {
            // Init prompt
            BiometricPrompt.PromptInfo promptInfo;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                promptInfo = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle(getString(R.string.title_login))
                        .setDescription(getString(R.string.description_login))
                        .setDeviceCredentialAllowed(true)
                        .build();
                biometricPrompt2.authenticate(promptInfo);
                showAuthLoading();
            }
        }
        catch (Exception e) {
            Log.e(LOG_AUTH, "BiometricPrompt error: " + e);
            showAuthError(getString(R.string.auth_fail));
        }
    }

    /* ---------------------------------------------------------------------------------------------
                                           IN APP PURCHASE
     -------------------------------------------------------------------------------------------- */

    private void initBillingClient() {
        Log.d(LOG_BILLING, "=> initBillingClient");

        PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) ->
                Log.w(LOG_BILLING, "No purchases updates on this activity");

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
    }

    private void connectToGooglePlay() {

        isIapStep = true;
        showIapLoading();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                    Log.d(LOG_BILLING, "Billing client is ready!");

                    BillingResult supportsProductDetail = billingClient.isFeatureSupported(PRODUCT_DETAILS);
                    if ( supportsProductDetail.getResponseCode() != BillingClient.BillingResponseCode.OK ) {
                        Log.e(LOG_BILLING, "feature unsupported - show warning to user...");
                        new Handler(Looper.getMainLooper()).post(() -> showIapError());
                    }
                    else queryPurchases();

                }
                else {
                    // Show warning
                    Log.e(LOG_BILLING, "Billing client not connected: " + billingResult.getDebugMessage());
                    new Handler(Looper.getMainLooper()).post(() -> showIapError());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(LOG_BILLING, "BillingClient disconnected");
                new Handler(Looper.getMainLooper()).post(() -> showIapError());
            }
        });
    }

    private void queryPurchases() {
        Log.d(LOG_BILLING, "=> queryPurchases");

        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                        .build();

        billingClient.queryPurchasesAsync(params, (billingResult, purchases) -> {

            Log.d(LOG_BILLING, "purchases list size: " + purchases.size());
            if (purchases.size() > 0) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
            }
            else new Handler(Looper.getMainLooper()).post(this::requestLatestConsentInformation);

        });

    }

    private void handlePurchase(Purchase purchase) {

        String orderId = purchase.getOrderId();

        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = billingResult -> {
            // purchase is verified => init auth
            new Handler(Looper.getMainLooper()).post(() -> setPremiumUser(orderId));
        };

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            Log.d(LOG_BILLING, "is acknowledged: " + purchase.isAcknowledged());
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();

                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
            else {
                // purchase is verified => init auth
                new Handler(Looper.getMainLooper()).post(() -> setPremiumUser(orderId));
            }
        }
        else {
            // purchase not verified => init ads
            new Handler(Looper.getMainLooper()).post(this::requestLatestConsentInformation);
        }
    }

    private void setPremiumUser(String orderId) {
        Log.d(LOG, "Set premium user on shared prefs");
        SettingsPrefs.setSettingsPrefsString(this, Tags.keyIapOrder, orderId);
        SettingsPrefs.setSettingsPrefsBoolean(this, Tags.keyIsPremium, true);
        startMainActivity();
    }


    /* ---------------------------------------------------------------------------------------------
                                                ADS
     -------------------------------------------------------------------------------------------- */

    private void requestLatestConsentInformation() {
        Log.d(LOG_UMP, "LOADING CONSENT");
        isIapStep = false;
        isAdsStep = true;
        showAdsLoading();

        // Set tag for underage of consent. false means users are not underage.
        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        Log.d(LOG_UMP, "Consent status: " + consentInformation.getConsentStatus());

        consentInformation.requestConsentInfoUpdate(this, params,
                (ConsentInformation.OnConsentInfoUpdateSuccessListener) () -> {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                            this,
                            (ConsentForm.OnConsentFormDismissedListener) loadAndShowError -> {
                                if (loadAndShowError != null) {
                                    // Consent gathering failed.
                                    Log.e(LOG_UMP, String.format("%s: %s", loadAndShowError.getErrorCode(), loadAndShowError.getMessage()));
                                    showAdsError();
                                }

                                // Consent has been gathered.
                                if (consentInformation.canRequestAds()) initializeAdsSdk();

                            }
                    );
                },
                (ConsentInformation.OnConsentInfoUpdateFailureListener) requestConsentError -> {
                    // Consent gathering failed.
                    Log.w(LOG_UMP, String.format("%s: %s", requestConsentError.getErrorCode(), requestConsentError.getMessage()));
                    showAdsError();
                });
    }

    private void initializeAdsSdk() {
        MobileAds.initialize(this, initializationStatus -> {
            Log.d(LOG_UMP, "MobileAds initialized");
            startMainActivity();
        });
    }

}