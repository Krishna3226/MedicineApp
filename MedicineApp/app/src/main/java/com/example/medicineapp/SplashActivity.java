package com.example.medicineapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean navigated = false;

    private final Runnable goToMain = () -> {
        if (navigated || isFinishing()) return;   // prevents duplicate navigation
        navigated = true;
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(goToMain, 1500);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(goToMain);       // cancel pending navigation on recreate/exit
        super.onDestroy();
    }
}
