package com.jgm.mybudgetapp;


import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import static com.android.billingclient.api.BillingClient.FeatureType.PRODUCT_DETAILS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.gms.ads.MobileAds;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.google.common.collect.ImmutableList;
import com.jgm.mybudgetapp.databinding.ActivityLoginBinding;
import com.jgm.mybudgetapp.utils.Populate;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private static final String LOG = "debug-login";
    private static final String LOG_UMP = "debug-consent";
    private static final String LOG_AUTH = "debug-auth";
    private static final String LOG_BILLING = "debug-billing";
    private boolean keep = true;

    // Consent
    private ConsentInformation consentInformation;

    // Authentication
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    // Billing
    private BillingClient billingClient;
    private ProductDetails productDetails;

    // UI
    private ActivityLoginBinding binding;
    private Button mLoginButton, mBuyPremiumAccess;
    private ProgressBar mBillingProgressBar;

    private void setBinding() {
        mLoginButton = binding.loginButton;
        mBuyPremiumAccess = binding.buttonTestBilling;
        mBillingProgressBar = binding.progressBarTestBilling;
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

        // set purchase button to disable until connection is ready
        mBuyPremiumAccess.setEnabled(false);
        mBuyPremiumAccess.setOnClickListener(v -> launchBillingFlow());

    }

    /* ---------------------------------------------------------------------------------------------
                                          SPLASH SCREEN
     -------------------------------------------------------------------------------------------- */

    private void handleSplashScreenDelay(SplashScreen splashScreen) {

        Log.d(LOG, "Init splash screen timer");

        splashScreen.setKeepOnScreenCondition(() -> keep);

        mLoginButton.setEnabled(false);

        requestLatestConsentInformation();

        int DELAY = 1000;
        Handler handler = new Handler();
        handler.postDelayed(() -> {

            Populate.initDefaultAccounts(this);
            Populate.initDefaultCategories(this);

            checkDeviceAuth();
            initAuthPrompt();

            initBillingClient();
            connectToGooglePlay();

            keep = false;

        }, DELAY);
    }

    private void enablePurchaseButton() {
        mBillingProgressBar.setVisibility(View.GONE);
        mBuyPremiumAccess.setEnabled(true);
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


    /* ---------------------------------------------------------------------------------------------
                                           IN APP PURCHASE
     -------------------------------------------------------------------------------------------- */

    private void initBillingClient() {
        Log.d(LOG_BILLING, "=> initBillingClient");

        PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
            Log.d(LOG_BILLING, "BillingClient: onPurchaseUpdated");

            int responseCode = billingResult.getResponseCode();

            if (purchases != null) {
                switch (responseCode) {
                    case BillingClient.BillingResponseCode.OK:
                        Log.i(LOG_BILLING, "onPurchasesUpdated: OK");
                        for (Purchase purchase : purchases) {
                            handlePurchase(purchase);
                        }
                        break;
                    case BillingClient.BillingResponseCode.USER_CANCELED:
                        Log.i(LOG_BILLING, "onPurchasesUpdated: User canceled the purchase");
                        break;
                    case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                        Log.i(LOG_BILLING, "onPurchasesUpdated: The user already owns this item");
                        break;
                    default:
                        Log.e(LOG_BILLING, "Error: " + responseCode + " | " + billingResult.getDebugMessage());
                }
            }

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
                    }
                    else queryProducts();

                    // todo: for login activity: query purchases
                    queryPurchases();

                }
                else {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                    Log.e(LOG_BILLING, "Billing client not connected: " + billingResult.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(LOG_BILLING, "BillingClient disconnected");
            }
        });

    }

    private void queryProducts() {
        Log.d(LOG_BILLING, "querySkuDetails");

        String productId = getString(R.string.product_id);

        QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(
                        ImmutableList.of(
                                QueryProductDetailsParams.Product.newBuilder()
                                        .setProductId(productId)
                                        .setProductType(BillingClient.ProductType.INAPP)
                                        .build()))
                .build();


        billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(@NonNull BillingResult billingResult,
                                                 @NonNull List<ProductDetails> list) {

                // check billingResult
                // process returned productDetailsList

                int responseCode = billingResult.getResponseCode();
                String debugMessage = billingResult.getDebugMessage();

                if (responseCode == BillingClient.BillingResponseCode.OK) {
                    if (list.size() > 0) {

                        // This app only offers 1 product
                        productDetails = list.get(0);

                        Log.d(LOG_BILLING, "onProductDetailsResponse: \n" +
                                productDetails.getName() + "\n" +
                                productDetails.getDescription() + "\n" +
                                productDetails.getProductType() + "\n" +
                                Objects.requireNonNull(list.get(0).getOneTimePurchaseOfferDetails()).getFormattedPrice());

                        new Handler(Looper.getMainLooper()).post(() -> enablePurchaseButton());
                    }

                }
                else {
                    Log.e(LOG_BILLING, debugMessage);
                }

            }
        });

    }

    private void launchBillingFlow() {

        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();


        // Launch the billing flow
        BillingResult billingResult = billingClient.launchBillingFlow(LoginActivity.this, billingFlowParams);
        Log.d(LOG, "launchBillingFlow: response: " + billingResult.getDebugMessage());

        int responseCode = billingResult.getResponseCode();
        switch (responseCode) {
            case BillingClient.BillingResponseCode.OK:
                Log.i(LOG_BILLING, "OK");
                break;
            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                Log.i(LOG_BILLING, "Service disconnected");
                break;
            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                Log.i(LOG_BILLING, "Service unavailable");
                break;
            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                Log.i(LOG_BILLING, "Billing unavailable");
                break;
            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                Log.i(LOG_BILLING, "Item unavailable");
                break;
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                Log.i(LOG_BILLING, "Developer error");
                break;
            case BillingClient.BillingResponseCode.ERROR:
                Log.e(LOG_BILLING, "Error: " + billingResult.getDebugMessage());
                break;
            case BillingClient.BillingResponseCode.USER_CANCELED:
                Log.i(LOG_BILLING, "User canceled");
                break;
            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                Log.i(LOG_BILLING, "Feature not supported");
                break;
            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                Log.i(LOG_BILLING, "Item already owned");
                break;
            default:
                Log.d(LOG_BILLING, "onSkuDetailsResponse: " + billingResult.getDebugMessage());
        }
    }

    private void handlePurchase(Purchase purchase) {
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = billingResult -> {
            int responseCode = billingResult.getResponseCode();
            String debugMessage = billingResult.getDebugMessage();

            if (responseCode == BillingClient.BillingResponseCode.OK)
                Log.d(LOG_BILLING, "acknowledgePurchase: ok");
            else
                Log.e(LOG_BILLING, "acknowledgePurchase: responseCode: " + responseCode +
                    "debugMessage: "  + debugMessage);
        };

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            Log.d(LOG_BILLING, "is acknowledged: " + purchase.isAcknowledged());
            Log.d(LOG_BILLING, "id: " + purchase.getOrderId());

            if (!purchase.isAcknowledged()) {

                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();

                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);

            }
        }
    }

    private void queryPurchases() {
        Log.d(LOG_BILLING, "=> queryPurchases");

        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                new PurchasesResponseListener() {
                    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> purchases) {
                        // check billingResult
                        // process returned purchase list, e.g. display the plans user owns
                        Log.d(LOG_BILLING, "purchases list size: " + purchases.size());
                        if (purchases.size() > 0) {
                            for (Purchase purchase : purchases) {
                                handlePurchase(purchase);
                            }
                        }
                    }
                }
        );
    }
}