package com.tripexpense.tracker.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;
import com.tripexpense.tracker.R;
import com.tripexpense.tracker.service.FirebaseAuthService;
import com.tripexpense.tracker.ui.auth.AuthActivity;
import com.tripexpense.tracker.ui.dashboard.MainActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View cardLogo = findViewById(R.id.card_logo);
        View tvTitle = findViewById(R.id.tv_splash_title);
        View tvSubtitle = findViewById(R.id.tv_splash_subtitle);

        Animation animFade = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        animFade.setDuration(1200);

        if (cardLogo != null) cardLogo.startAnimation(animFade);
        if (tvTitle != null) tvTitle.startAnimation(animFade);
        if (tvSubtitle != null) tvSubtitle.startAnimation(animFade);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseAuthService authService = FirebaseAuthService.getInstance();
            Intent intent;
            if (authService.isUserLoggedIn()) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, AuthActivity.class);
            }
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 2200);
    }
}
