package com.example.swag_app;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private ImageView ivLogo;
    private TextView tvAppName;
    private TextView tvTagline;
    private MaterialButton btnStart;
    private ProgressBar progressBar;

    private static final int LOGO_ANIM_DURATION = 1000;
    private static final int TEXT_ANIM_DURATION = 800;
    private static final int BUTTON_ANIM_DURATION = 500;
    private static final int SPLASH_TIMEOUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivLogo = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);
        tvTagline = findViewById(R.id.tvTagline);
        btnStart = findViewById(R.id.btnStart);
        progressBar = findViewById(R.id.progressBar);

        tvAppName.setAlpha(0f);
        tvTagline.setAlpha(0f);
        btnStart.setAlpha(0f);
        btnStart.setVisibility(View.GONE);

        startAnimations();

        btnStart.setOnClickListener(v -> {
            animateButtonClick(btnStart);
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }, 300);
        });

        // Delay before checking login status to allow splash animations
        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.isEmailVerified()) {
                FirebaseFirestore.getInstance().collection("users")
                        .whereEqualTo("email", currentUser.getEmail())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                String role = document.getString("role");

                                Intent intent;
                                if ("Admin".equals(role)) {
                                    intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                                } else {
                                    intent = new Intent(MainActivity.this, StudentDashboard.class);
                                }

                                startActivity(intent);
                                finish();
                            } else {
                                showStartButton();
                            }
                        })
                        .addOnFailureListener(e -> {
                            e.printStackTrace();
                            showStartButton();
                        });
            } else {
                showStartButton();
            }
        }, SPLASH_TIMEOUT);
    }

    private void startAnimations() {
        ivLogo.setScaleX(0f);
        ivLogo.setScaleY(0f);

        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(ivLogo, "scaleX", 0f, 1.1f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(ivLogo, "scaleY", 0f, 1.1f, 1f);
        logoScaleX.setDuration(LOGO_ANIM_DURATION);
        logoScaleY.setDuration(LOGO_ANIM_DURATION);
        logoScaleX.setInterpolator(new OvershootInterpolator(1.5f));
        logoScaleY.setInterpolator(new OvershootInterpolator(1.5f));

        AnimatorSet logoAnimSet = new AnimatorSet();
        logoAnimSet.playTogether(logoScaleX, logoScaleY);

        // App name animation
        tvAppName.setTranslationY(60f);
        ObjectAnimator appNameFade = ObjectAnimator.ofFloat(tvAppName, "alpha", 0f, 1f);
        ObjectAnimator appNameTranslate = ObjectAnimator.ofFloat(tvAppName, "translationY", 60f, 0f);
        appNameFade.setDuration(TEXT_ANIM_DURATION);
        appNameTranslate.setDuration(TEXT_ANIM_DURATION);
        appNameFade.setInterpolator(new AccelerateDecelerateInterpolator());
        appNameTranslate.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet appNameAnimSet = new AnimatorSet();
        appNameAnimSet.playTogether(appNameFade, appNameTranslate);

        // Tagline animation
        tvTagline.setTranslationY(60f);
        ObjectAnimator taglineFade = ObjectAnimator.ofFloat(tvTagline, "alpha", 0f, 1f);
        ObjectAnimator taglineTranslate = ObjectAnimator.ofFloat(tvTagline, "translationY", 60f, 0f);
        taglineFade.setDuration(TEXT_ANIM_DURATION);
        taglineTranslate.setDuration(TEXT_ANIM_DURATION);
        taglineFade.setInterpolator(new AccelerateDecelerateInterpolator());
        taglineTranslate.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet taglineAnimSet = new AnimatorSet();
        taglineAnimSet.playTogether(taglineFade, taglineTranslate);

        AnimatorSet fullSet = new AnimatorSet();
        fullSet.playSequentially(logoAnimSet, appNameAnimSet, taglineAnimSet);
        fullSet.start();
    }

    private void showStartButton() {
        progressBar.setVisibility(View.GONE);
        btnStart.setVisibility(View.VISIBLE);
        btnStart.setAlpha(0f);
        btnStart.setScaleX(0.8f);
        btnStart.setScaleY(0.8f);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(btnStart, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(btnStart, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(btnStart, "scaleY", 0.8f, 1f);

        fadeIn.setDuration(BUTTON_ANIM_DURATION);
        scaleX.setDuration(BUTTON_ANIM_DURATION);
        scaleY.setDuration(BUTTON_ANIM_DURATION);

        fadeIn.setInterpolator(new OvershootInterpolator(1.2f));
        scaleX.setInterpolator(new OvershootInterpolator(1.2f));
        scaleY.setInterpolator(new OvershootInterpolator(1.2f));

        AnimatorSet set = new AnimatorSet();
        set.playTogether(fadeIn, scaleX, scaleY);
        set.start();
    }

    private void animateButtonClick(View button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.9f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.9f, 1f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        AnimatorSet clickAnim = new AnimatorSet();
        clickAnim.playTogether(scaleX, scaleY);
        clickAnim.start();
    }
}
