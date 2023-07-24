package com.jgm.mybudgetapp.fragmentsSettings;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.SettingsInterface;
import com.jgm.mybudgetapp.databinding.FragmentSettingsBinding;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.sharedPrefs.SettingsPrefs;
import com.jgm.mybudgetapp.utils.Populate;
import com.jgm.mybudgetapp.utils.Tags;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    private static final String LOG_SETTINGS = "debug-settings";

    // UI
    private NestedScrollView mNestedScrollView;
    private ProgressBar mProgressBar;
    private FragmentSettingsBinding binding;
    private SwitchCompat switchDarkMode;
    private Button mOpenCategories, mOpenCreditCards, mClearDatabase;
    private ImageButton mBack;

    private void setBinding() {
        mBack = binding.settingsButtonBack;
        mNestedScrollView = binding.settingsScrollView;
        mProgressBar = binding.settingsProgressBar;
        switchDarkMode = binding.switchDarkMode;
        mOpenCategories = binding.settingsEditCategories;
        mOpenCreditCards = binding.settingsEditCards;
        mClearDatabase = binding.settingsClearData;
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

        boolean isDark = SettingsPrefs.getSettingsPrefsBoolean(mContext, "isDark");
        switchDarkMode.setChecked(isDark);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                SettingsPrefs.setSettingsPrefsBoolean(mContext, "isDark", isChecked);
                mInterface.switchDarkMode(isChecked);
            }, 200);
        });

        mOpenCategories.setOnClickListener(v -> mInterface.openCategoriesList(true));
        mOpenCreditCards.setOnClickListener(v -> mInterface.open(Tags.cardsTag));
        mClearDatabase.setOnClickListener(v -> {
            mInterface.showConfirmationDialog(getString(R.string.msg_clear_database),
                    getString(R.string.action_reset_database),
                    R.drawable.ic_app_dangerous);
        });
        mBack.setOnClickListener(v -> mInterface.navigateBack());
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
}