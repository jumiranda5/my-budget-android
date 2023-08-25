package com.jgm.mybudgetapp;


import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import static com.android.billingclient.api.BillingClient.FeatureType.PRODUCT_DETAILS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.gms.ads.MobileAds;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.jgm.mybudgetapp.databinding.ActivityLoginBinding;
import com.jgm.mybudgetapp.utils.Populate;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.List;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    // Login flow:
    // - check device credentials =>
    //      if ok, proceed to Billing Client.
    //      Else, stop and show error
    // - billing client: query purchases =>
    //      if premium user, show auth prompt.
    //      Else, init ads and show auth

    private static final String LOG = "debug-login";
    private static final String LOG_UMP = "debug-consent";
    private static final String LOG_AUTH = "debug-auth";
    private static final String LOG_BILLING = "debug-billing";
    private boolean keep = true;
    private boolean hasWarning = false;

    // Consent
    private ConsentInformation consentInformation;

    // Billing
    private BillingClient billingClient;

    // UI
    private ActivityLoginBinding binding;
    private Button mContinueButton, mRetryButton;
    private ProgressBar mProgressBar;
    private Group mIapGroup, mAdsGroup, mAuthGroup;
    private TextView mAuthMessage;
    private ImageView mLockIcon;

    private void setBinding() {
        mContinueButton = binding.loginContinueButton;
        mRetryButton = binding.loginRetryButton;
        mProgressBar = binding.loginProgressBar;
        mIapGroup = binding.groupLoginIap;
        mAdsGroup = binding.groupLoginAds;
        mAuthGroup = binding.groupLoginAuth;
        mAuthMessage = binding.loginAuthWarning;
        mLockIcon = binding.loginIcon;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(Tags.LOG_LIFECYCLE, "Login Activity onCreate");

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setBinding();

        handleSplashScreenDelay(splashScreen);

        mRetryButton.setOnClickListener(v -> {
            initBillingClient();
            checkDeviceCredentials();
            mProgressBar.setVisibility(View.VISIBLE);
            mAuthGroup.setVisibility(View.GONE);
            mAdsGroup.setVisibility(View.GONE);
            mIapGroup.setVisibility(View.GONE);
            mContinueButton.setVisibility(View.GONE);
            mRetryButton.setVisibility(View.GONE);
        });

        mContinueButton.setOnClickListener(v -> initAuthPrompt());

    }

    // verify if user is premium
    // if premium => init authentication
    // if not premium => init admob => init authentication


    /* ---------------------------------------------------------------------------------------------
                                          SPLASH SCREEN
     -------------------------------------------------------------------------------------------- */

    private void handleSplashScreenDelay(SplashScreen splashScreen) {

        Log.d(LOG, "Init splash screen timer");

        splashScreen.setKeepOnScreenCondition(() -> keep);

        Populate.initDefaultAccounts(this);
        Populate.initDefaultCategories(this);

        initBillingClient();

        int DELAY = 1000;
        Handler handler = new Handler();
        handler.postDelayed(() -> {

            checkDeviceCredentials();

            keep = false;

        }, DELAY);
    }

    /* ---------------------------------------------------------------------------------------------
                                          AUTHENTICATION
     -------------------------------------------------------------------------------------------- */

    // Check if the device supports biometric authentication
    private void checkDeviceCredentials() {
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                == BiometricManager.BIOMETRIC_SUCCESS) {
            Log.d(LOG_AUTH, "App can authenticate using biometrics.");
            connectToGooglePlay();
        }
        else {
            Log.e(LOG_AUTH, "Biometric features are currently not available.");
            mAuthGroup.setVisibility(View.VISIBLE);
            mRetryButton.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    // Show the prompt
    private void initAuthPrompt() {
        Log.d(LOG_AUTH, "Should init auth prompt");
        // Authentication
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Log.e(LOG_AUTH, "onAuthenticationError: " + errString);
                        showAuthError();
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Log.d(LOG_AUTH, "Successfully authenticated.");
                        mLockIcon.setImageDrawable(ContextCompat.getDrawable(LoginActivity.this, R.drawable.ic_48_lock_open_fill0_200));
                        mRetryButton.setVisibility(View.GONE);
                        mContinueButton.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        mAuthGroup.setVisibility(View.GONE);
                        mAdsGroup.setVisibility(View.GONE);
                        mIapGroup.setVisibility(View.GONE);
                        startMainActivity();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Log.d(LOG_AUTH, "onAuthenticationFailed");
                        showAuthError();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                .build();

        biometricPrompt.authenticate(promptInfo);
        mRetryButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    private void showAuthError() {
        mAuthGroup.setVisibility(View.VISIBLE);
        mRetryButton.setVisibility(View.VISIBLE);
        mAuthMessage.setText(getString(R.string.auth_fail));
        mProgressBar.setVisibility(View.GONE);
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
            if (!hasWarning) initAuthPrompt();
            else {
                mRetryButton.setVisibility(View.VISIBLE);
                mContinueButton.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
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
                                    showAdsFailureWarning();
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
                    showAdsFailureWarning();
                });
    }

    private void showAdsFailureWarning() {
        mAdsGroup.setVisibility(View.VISIBLE);
        hasWarning = true;
    }


    /* ---------------------------------------------------------------------------------------------
                                           IN APP PURCHASE
     -------------------------------------------------------------------------------------------- */

    private void initBillingClient() {
        Log.d(LOG_BILLING, "=> initBillingClient");

        PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
            Log.w(LOG_BILLING, "No purchases updates on this activity");
        };

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
    }

    private void connectToGooglePlay() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                int responseCode = billingResult.getResponseCode();
                if (responseCode == BillingClient.BillingResponseCode.OK) {

                    Log.d(LOG_BILLING, "Billing client is ready!");

                    BillingResult supportsProductDetail = billingClient.isFeatureSupported(PRODUCT_DETAILS);
                    if ( supportsProductDetail.getResponseCode() != BillingClient.BillingResponseCode.OK ) {
                        Log.e(LOG_BILLING, "feature unsupported - show warning to user...");
                        new Handler(Looper.getMainLooper()).post(() -> {
                            mIapGroup.setVisibility(View.VISIBLE);
                            hasWarning = true;
                            requestLatestConsentInformation();
                        });
                    }
                    else queryPurchases();

                }
                else {
                    // Show warning
                    Log.e(LOG_BILLING, "Billing client not connected: " + billingResult.getDebugMessage());
                    new Handler(Looper.getMainLooper()).post(() -> {
                        mIapGroup.setVisibility(View.VISIBLE);
                        hasWarning = true;
                        requestLatestConsentInformation();
                    });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(LOG_BILLING, "BillingClient disconnected");
                new Handler(Looper.getMainLooper()).post(() -> {
                    mIapGroup.setVisibility(View.VISIBLE);
                    hasWarning = true;
                    requestLatestConsentInformation();
                });
            }
        });
    }

    private void queryPurchases() {
        Log.d(LOG_BILLING, "=> queryPurchases");

        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                new PurchasesResponseListener() {
                    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult,
                                                         @NonNull List<Purchase> purchases) {
                        processPurchase(purchases);
                    }
                }
        );
    }

    private void processPurchase(List<Purchase> purchases) {
        Log.d(LOG_BILLING, "purchases list size: " + purchases.size());
        if (purchases.size() > 0) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        }
        else requestLatestConsentInformation();
    }

    private void handlePurchase(Purchase purchase) {
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = billingResult -> {
            // purchase is verified => init auth
            new Handler(Looper.getMainLooper()).post(this::initAuthPrompt);
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
                new Handler(Looper.getMainLooper()).post(this::initAuthPrompt);
            }
        }
        else {
            // purchase not verified => init ads
            new Handler(Looper.getMainLooper()).post(this::requestLatestConsentInformation);
        }
    }

}