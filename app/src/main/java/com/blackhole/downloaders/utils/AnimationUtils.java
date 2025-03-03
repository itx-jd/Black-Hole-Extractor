package com.blackhole.downloaders.utils;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

public class AnimationUtils {

    public static void scaleImageView(ImageView imageView, int dimensionRes, int delta) {

        int fromWidth = (int) imageView.getContext().getResources().getDimension(dimensionRes);
        int toWidth = fromWidth + delta;

        ValueAnimator anim = ValueAnimator.ofInt(fromWidth, toWidth);
        anim.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            imageView.getLayoutParams().width = val;
            imageView.getLayoutParams().height = val;
            imageView.requestLayout();
        });
        anim.setDuration(200);
        anim.start();
    }

    public static void fadeInView(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(1000);
        view.startAnimation(fadeIn);
        view.setVisibility(View.VISIBLE);
    }
}
