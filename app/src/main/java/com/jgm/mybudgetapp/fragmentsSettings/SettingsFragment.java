package com.jgm.mybudgetapp.fragmentsSettings;

import static com.android.billingclient.api.BillingClient.FeatureType.PRODUCT_DETAILS;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.google.common.collect.ImmutableList;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.SettingsActivity;
import com.jgm.mybudgetapp.SettingsInterface;
import com.jgm.mybudgetapp.databinding.FragmentSettingsBinding;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;
import com.jgm.mybudgetapp.utils.Populate;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.Objects;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    private static final String LOG_SETTINGS = "debug-settings";
    private static final String LOG_UMP = "debug-consent";
    private static final String LOG_BILLING = "debug-billing";

    // Billing
    private BillingClient billingClient;
    private ProductDetails productDetails;
    private boolean isPremium;

    // UI
    private NestedScrollView mNestedScrollView;
    private ProgressBar mProgressBar, mProgressBarAds;
    private FragmentSettingsBinding binding;
    private SwitchCompat switchDarkMode;
    private Button mOpenCategories, mOpenCreditCards, mClearDatabase, mReviewConsent, mBuyPremiumAccess;
    private ImageButton mBack;
    private TextView mOrderId;
    private ImageView mPremiumIcon;

    private void setBinding() {
        mBack = binding.settingsButtonBack;
        mNestedScrollView = binding.settingsScrollView;
        mProgressBar = binding.settingsProgressBar;
        switchDarkMode = binding.switchDarkMode;
        mOpenCategories = binding.settingsEditCategories;
        mOpenCreditCards = binding.settingsEditCards;
        mClearDatabase = binding.settingsClearData;
        mReviewConsent = binding.settingsReviewAdsConsent;
        mBuyPremiumAccess = binding.settingsPremiumMember;
        mOrderId = binding.settingsPremiumOrder;
        mPremiumIcon = binding.settingsPremiumIcon;
        mProgressBarAds = binding.settingsAdsReviewProgressBar;
    }

    // Interfaces
    private Context mContext;
    private SettingsInterface mInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(Tags.LOG_LIFECYCLE, "Settings onAttach");
        mContext = context;
        mInterface = (SettingsInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(Tags.LOG_LIFECYCLE, "Settings onCreateView");
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(Tags.LOG_LIFECYCLE, "Settings onViewCreated");

        initBillingClient();
        initButtons();
        initDarkModeSwitch();
        mOrderId.setVisibility(View.GONE);

        isPremium = SettingsPrefs.getSettingsPrefsBoolean(mContext, Tags.keyIsPremium);
        String orderId = SettingsPrefs.getSettingsPrefsString(mContext, Tags.keyIapOrder);
        if (isPremium) setPremiumUser(orderId);

    }

    private void initButtons() {
        mBack.setOnClickListener(v -> mInterface.navigateBack());
        mOpenCategories.setOnClickListener(v -> mInterface.openCategoriesList(true));
        mOpenCreditCards.setOnClickListener(v -> mInterface.open(Tags.cardsTag));
        mReviewConsent.setOnClickListener(v -> requestLatestConsentInformation());
        mClearDatabase.setOnClickListener(v ->
                mInterface.showConfirmationDialog(getString(R.string.msg_clear_database),
                getString(R.string.action_reset_database),
                R.drawable.ic_48_dangerous_300));
        mBuyPremiumAccess.setEnabled(false);
        mBuyPremiumAccess.setOnClickListener(v -> launchBillingFlow());
    }

    private void initDarkModeSwitch() {
        boolean isDark = SettingsPrefs.getSettingsPrefsBoolean(mContext, Tags.keyDarkMode);
        switchDarkMode.setChecked(isDark);
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                SettingsPrefs.setSettingsPrefsBoolean(mContext, Tags.keyDarkMode, isChecked);
                mInterface.switchDarkMode(isChecked);
            }, 200);
        });
    }

    private void showBillingError(String msg, boolean isConnectionError) {
        if (isConnectionError) mBuyPremiumAccess.setText(msg);
        else Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    private void setPremiumUser(String orderId) {
        mBuyPremiumAccess.setText(mContext.getString(R.string.action_premium));
        mBuyPremiumAccess.setTextColor(ContextCompat.getColor(mContext, R.color.savings));
        mPremiumIcon.setImageTintList(ContextCompat.getColorStateList(mContext, R.color.savings));
        String orderText = mContext.getString(R.string.label_order_id) + " " + orderId;
        mOrderId.setText(orderText);
        mOrderId.setVisibility(View.VISIBLE);
        mOrderId.setOnLongClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager)
                    mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("id:", orderId);
            clipboard.setPrimaryClip(clip);
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                Toast.makeText(mContext, mContext.getString(R.string.msg_purchase_id_copied), Toast.LENGTH_SHORT).show();
            return false;
        });
    }

    private void setPremiumButtonReady(String price) {
        if (!isPremium) {
            String buttonText = mContext.getString(R.string.action_buy_premium_access) + " - " + price;
            mBuyPremiumAccess.setText(buttonText);
            mBuyPremiumAccess.setEnabled(true);
        }
    }

    /* -------------------------------------------------------------------
                               CLEAR DATABASE
     ------------------------------------------------------------------- */

    public void clearDatabase() {
        Log.d(LOG_SETTINGS, "=> Clear database");
        mNestedScrollView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        AppDatabase db = AppDatabase.getDatabase(mContext);

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            // Clear all tables
            db.TransactionDao().clearTransactionsTable();
            db.CategoryDao().clearCategoriesTable();
            db.AccountDao().clearAccountsTable();
            db.CardDao().clearCardsTable();
            Log.d(Tags.LOG_DB, "All tables cleared");

            // Reset shared prefs defaults (for accounts and cards)
            Log.d(LOG_SETTINGS, "Reset shared prefs");
            SettingsPrefs.setSettingsPrefsBoolean(mContext, "hasInitialAccounts", false);
            SettingsPrefs.setSettingsPrefsBoolean(mContext, "hasInitialCategories", false);

            handler.post(() -> {
                // Re-populate categories and accounts
                Log.d(LOG_SETTINGS, "Populate accounts and categories");
                Populate.initDefaultAccounts(mContext);
                Populate.initDefaultCategories(mContext);
                // end progress
                mNestedScrollView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            });

        });
    }


    /* ---------------------------------------------------------------------------------------------
                                             ADS CONSENT
     -------------------------------------------------------------------------------------------- */


    private void requestLatestConsentInformation() {
        Log.d(LOG_UMP, "LOADING CONSENT");

        mProgressBarAds.setVisibility(View.VISIBLE);
        mReviewConsent.setEnabled(false);

        // Set tag for underage of consent. false means users are not underage.
        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        // Consent
        ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(mContext);
        Log.d(LOG_UMP, "Consent status: " + consentInformation.getConsentStatus());
        consentInformation.reset();

        consentInformation.requestConsentInfoUpdate(
                (SettingsActivity) mContext,
                params,
                (ConsentInformation.OnConsentInfoUpdateSuccessListener) () ->
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired((SettingsActivity) mContext,
                        (ConsentForm.OnConsentFormDismissedListener) loadAndShowError -> {
                            if (loadAndShowError != null) {
                                // Consent gathering failed.
                                Log.w(LOG_UMP, String.format("%s: %s",
                                        loadAndShowError.getErrorCode(),
                                        loadAndShowError.getMessage()));
                            }
                            mProgressBarAds.setVisibility(View.GONE);
                            mReviewConsent.setEnabled(true);
                        }
                ),
                (ConsentInformation.OnConsentInfoUpdateFailureListener) requestConsentError -> {
                    // Consent gathering failed.
                    Log.w(LOG_UMP, String.format("%s: %s",
                            requestConsentError.getErrorCode(),
                            requestConsentError.getMessage()));
                    mProgressBarAds.setVisibility(View.GONE);
                    mReviewConsent.setEnabled(true);
                });

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
                        for (Purchase purchase : purchases) { handlePurchase(purchase); }
                        break;
                    case BillingClient.BillingResponseCode.USER_CANCELED:
                        new Handler(Looper.getMainLooper()).post(() ->
                                showBillingError(mContext.getString(R.string.msg_iap_user_canceled), false));
                        break;
                    case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                        new Handler(Looper.getMainLooper()).post(() ->
                                showBillingError(mContext.getString(R.string.msg_iap_user_own_item), false));
                        break;
                    default:
                        Log.e(LOG_BILLING, "Error: " + responseCode + " | " + billingResult.getDebugMessage());
                        new Handler(Looper.getMainLooper()).post(() ->
                                showBillingError(mContext.getString(R.string.msg_iap_purchase_error), false));
                }
            }
        };

        billingClient = BillingClient.newBuilder(mContext)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        connectToGooglePlay();
    }

    private void connectToGooglePlay() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                    Log.d(LOG_BILLING, "Billing client is ready!");

                    BillingResult supportsProductDetail = billingClient.isFeatureSupported(PRODUCT_DETAILS);
                    if ( supportsProductDetail.getResponseCode() != BillingClient.BillingResponseCode.OK ) {
                        Log.e(LOG_BILLING, "feature unsupported - show warning to user...");
                        new Handler(Looper.getMainLooper()).post(() -> showBillingError(mContext.getString(R.string.action_premium_unavailable), true));
                    }
                    else queryProducts();

                }
                else {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                    Log.e(LOG_BILLING, "Billing client not connected: " + billingResult.getDebugMessage());
                    new Handler(Looper.getMainLooper()).post(() -> showBillingError(mContext.getString(R.string.action_premium_unavailable), true));
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(LOG_BILLING, "BillingClient disconnected");
                new Handler(Looper.getMainLooper()).post(() -> showBillingError(mContext.getString(R.string.action_premium_unavailable), true));
            }
        });

    }

    private void queryProducts() {
        Log.d(LOG_BILLING, "queryProducts");

        String productId = getString(R.string.product_id);

        QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build();

        QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(ImmutableList.of(product))
                .build();

        billingClient.queryProductDetailsAsync(queryProductDetailsParams, (billingResult, list) -> {

            // check billingResult
            // process returned productDetailsList

            int responseCode = billingResult.getResponseCode();
            String debugMessage = billingResult.getDebugMessage();

            if (responseCode == BillingClient.BillingResponseCode.OK) {
                if (list.size() > 0) {

                    // This app only offers 1 product
                    productDetails = list.get(0);
                    String price = Objects.requireNonNull(
                            productDetails.getOneTimePurchaseOfferDetails()).getFormattedPrice();

                    Log.d(LOG_BILLING, "onProductDetailsResponse: \n" +
                            productDetails.getName() + "\n" +
                            productDetails.getDescription() + "\n" +
                            productDetails.getProductType() + "\n" +
                            Objects.requireNonNull(list.get(0).getOneTimePurchaseOfferDetails()).getFormattedPrice());

                    new Handler(Looper.getMainLooper()).post(() -> setPremiumButtonReady(price));
                }

            }
            else {
                Log.e(LOG_BILLING, debugMessage);
                new Handler(Looper.getMainLooper()).post(() -> showBillingError(mContext.getString(R.string.action_premium_unavailable), false));
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
        BillingResult billingResult = billingClient.launchBillingFlow((Activity) mContext, billingFlowParams);
        Log.d(LOG_BILLING, "launchBillingFlow: response code: " + billingResult.getResponseCode());
    }

    private void handlePurchase(Purchase purchase) {
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = billingResult -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK)
                Log.d(LOG_BILLING, "acknowledgePurchase: ok");
            else
                Log.e(LOG_BILLING, "acknowledgePurchase: fail => " + billingResult.getResponseCode() +
                        "debugMessage: "  + billingResult.getDebugMessage());

            new Handler(Looper.getMainLooper()).post(() -> setPremiumUser(purchase.getOrderId()));
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
            else new Handler(Looper.getMainLooper()).post(() -> setPremiumUser(purchase.getOrderId()));
        }
    }

}