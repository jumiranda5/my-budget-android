package com.jgm.mybudgetapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.card.MaterialCardView;
import com.jgm.mybudgetapp.databinding.FragmentAccountsBinding;
import com.jgm.mybudgetapp.databinding.FragmentAdLockBinding;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;
import com.jgm.mybudgetapp.utils.Tags;

import java.time.Instant;

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
}