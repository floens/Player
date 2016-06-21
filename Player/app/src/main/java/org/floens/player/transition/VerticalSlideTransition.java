package org.floens.player.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import org.floens.controller.AndroidUtils;
import org.floens.controller.ControllerTransition;

public class VerticalSlideTransition extends ControllerTransition {
    private boolean up = false;

    public VerticalSlideTransition(boolean up) {
        this.up = up;
    }

    @Override
    public void perform() {
        View view = up ? to.view : from.view;

        AndroidUtils.waitForMeasure(view, new AndroidUtils.OnMeasuredCallback() {
            @Override
            public boolean onMeasured(View view) {
                Animator y = up ? ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.getHeight(), 0) :
                        ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0, view.getHeight());

                y.setDuration(350);
                y.setInterpolator(new DecelerateInterpolator(2.2f));

                y.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onCompleted();
                    }
                });

                AnimatorSet set = new AnimatorSet();
                set.playTogether(y);
                set.start();

                return true;
            }
        });
    }
}
