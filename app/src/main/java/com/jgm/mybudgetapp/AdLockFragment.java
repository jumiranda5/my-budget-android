package com.jgm.mybudgetapp;

import static com.android.billingclient.api.BillingClient.FeatureType.PRODUCT_DETAILS;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.card.MaterialCardView;
import com.google.common.collect.ImmutableList;
import com.jgm.mybudgetapp.databinding.FragmentAccountsBinding;
import com.jgm.mybudgetapp.databinding.FragmentAdLockBinding;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;
import com.jgm.mybudgetapp.utils.Tags;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class AdLockFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";

    private String mPage;

    public AdLockFragment() {
        // Required empty public constructor
    }

    public static AdLockFragment newInstance(String param1) {
        AdLockFragment fragment = new AdLockFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    // Constants
    private static final String LOG = "debug-ads";

    // Vars
    private RewardedAd rewardedAd;
    private boolean isRewardGranted = false;
    private String prefsTag;
    private String ad_unit;

    // UI
    private FragmentAdLockBinding binding;
    private TextView mPageTitle, mAdButtonText, mAdButtonLoaderText;
    private ProgressBar mAdLoader;
    private MaterialCardView mButtonWatchAd, mButtonPremium;
    private ImageView mAdWatchIcon;
    private ImageButton mClose;

    private void setBinding() {
        mPageTitle = binding.adLockPageName;
        mAdButtonText = binding.adLockWatchButtonText;
        mAdButtonLoaderText = binding.adLockLoaderText;
        mAdLoader = binding.adLockProgressBar;
        mButtonWatchAd = binding.adLockWatch;
        mButtonPremium = binding.adLockPremium;
        mAdWatchIcon = binding.adLockMovieIcon;
        mClose = binding.adLockClose;
    }

    // Interfaces
    private Context mContext;
    private AdInterface mInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mInterface = (AdInterface) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAdLockBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null)
            isRewardGranted = savedInstanceState.getBoolean("reward", false);

        Log.d(LOG, "------------- onCreateView ---------");

        setPageInfo();
        initButtons();
        loadAd();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("reward", isRewardGranted);
    }

    /* ---------------------------------------------------------------------------------------------
                                             PAGE INFO
     -------------------------------------------------------------------------------------------- */

    private void setPageInfo() {
        Log.d(LOG, "Page name: " + mPage);
        prefsTag = mPage;
        switch (mPage) {
            case Tags.transactionFormTag:
                mPageTitle.setText("Form");
                ad_unit = mContext.getString(R.string.ad_form);
                break;
            case Tags.categoriesTag:
                mPageTitle.setText("Categories");
                ad_unit = mContext.getString(R.string.ad_categories);
                break;
            case Tags.yearTag:
                mPageTitle.setText("Year Balance");
                ad_unit = mContext.getString(R.string.ad_year);
                break;
        }
    }

    /* ---------------------------------------------------------------------------------------------
                                          AD BUTTON (CardView)
     -------------------------------------------------------------------------------------------- */

    private void initButtons() {

        mClose.setOnClickListener(v -> mInterface.onAdFragmentDismiss(false));

        mButtonWatchAd.setOnClickListener(v -> {
            if (rewardedAd == null) loadAd();
            else showAd();
        });

    }

    private void setAdButtonLoading() {
        // disable cardView while ad is loading
        mButtonWatchAd.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.disabled_button));
        mButtonWatchAd.setClickable(false);
        // set loader visibility
        mAdLoader.setVisibility(View.VISIBLE);
        mAdButtonText.setVisibility(View.VISIBLE);
        mAdButtonText.setVisibility(View.GONE);
        mAdWatchIcon.setVisibility(View.GONE);
    }

    private void setAdButtonReady() {
        // disable cardView while ad is loading
        mButtonWatchAd.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.savings));
        mButtonWatchAd.setClickable(true);
        // set loader visibility
        mAdLoader.setVisibility(View.GONE);
        mAdButtonLoaderText.setVisibility(View.GONE);
        mAdButtonText.setVisibility(View.VISIBLE);
        mAdWatchIcon.setVisibility(View.VISIBLE);
    }

    private void setAdButtonFail() {
        mButtonWatchAd.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.savings));
        mButtonWatchAd.setClickable(true);
        mAdLoader.setVisibility(View.GONE);
        mAdButtonLoaderText.setVisibility(View.GONE);
        mAdButtonText.setVisibility(View.VISIBLE);
        mAdWatchIcon.setVisibility(View.VISIBLE);
        mAdButtonLoaderText.setText("Retry");
    }

    /* ---------------------------------------------------------------------------------------------
                                              LOAD AD
     -------------------------------------------------------------------------------------------- */

    private void loadAd() {
        setAdButtonLoading();
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(mContext, ad_unit,
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(LOG, loadAdError.toString());
                        rewardedAd = null;
                        setAdButtonFail();
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        Log.d(LOG, "Ad was loaded.");
                        setAdButtonReady();
                    }
                });
    }

    private void showAd() {

        if (rewardedAd != null) {

            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {

                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    Log.d(LOG, "Ad dismissed fullscreen content.");
                    rewardedAd = null;
                    // go to unlocked page if reward was granted or reload ad if not
                    if (isRewardGranted) mInterface.onAdFragmentDismiss(true);
                    else loadAd();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when ad fails to show.
                    Log.e(LOG, "Ad failed to show fullscreen content.");
                    rewardedAd = null;
                    loadAd();
                }

            });

            rewardedAd.show((Activity) mContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    Log.d(LOG, "The user earned the reward. / " + rewardItem.getType());
                    long time = Instant.now().toEpochMilli();
                    SettingsPrefs.setSettingsPrefsMilliseconds(mContext, prefsTag, time);
                    isRewardGranted = true;
                }
            });
        }
        else {
            Log.d(LOG, "The rewarded ad wasn't ready yet.");
            setAdButtonFail();
        }
    }


    /* ---------------------------------------------------------------------------------------------
                                           IN APP PURCHASE
     -------------------------------------------------------------------------------------------- */

    /*
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
    */
}