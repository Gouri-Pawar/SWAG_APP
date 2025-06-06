package com.example.swag_app.util;

import android.app.Activity;

import com.example.swag_app.R;

public class ActivityAnimationUtil {

    // Call this when navigating to a new screen
    public static void animateForward(Activity activity) {
        activity.overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        );
    }

    // Call this when returning back to previous screen
    public static void animateBackward(Activity activity) {
        activity.overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
    }
}