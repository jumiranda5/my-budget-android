package com.jgm.mybudgetapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;

import com.jgm.mybudgetapp.databinding.ActivityLoginBinding;
import com.jgm.mybudgetapp.utils.Populate;
import com.jgm.mybudgetapp.utils.Tags;

public class LoginActivity extends AppCompatActivity {

    private static final String LOG = "debug-login";
    private boolean keep = true;

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

        if (savedInstanceState == null) {
            handleSplashScreenDelay(splashScreen);
        }

        mLoginButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        });

    }

    private void handleSplashScreenDelay(SplashScreen splashScreen) {

        Log.d(LOG, "Init splash screen timer");

        splashScreen.setKeepOnScreenCondition(() -> keep);

        int DELAY = 1000;
        Handler handler = new Handler();
        handler.postDelayed(() -> {

            Populate.initDefaultAccounts(this);
            Populate.initDefaultCategories(this);

            // todo: init login (biometric or pin)
            keep = false;

        }, DELAY);
    }
}