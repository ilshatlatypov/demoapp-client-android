package org.hello;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by ilshat on 22.07.16.
 */
public class ViewSwitcher {

    private Activity activity;
    private int firstViewResId;
    private int secondViewResId;

    public ViewSwitcher(Activity activity, @IdRes int firstViewResId, @IdRes int secondViewResId) {
        this.activity = activity;
        this.firstViewResId = firstViewResId;
        this.secondViewResId = secondViewResId;
    }

    public void showFirst() {
        showFirst(true);
    }

    public void showSecond() {
        showFirst(false);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showFirst(final boolean first) {

        final View view1 = activity.findViewById(firstViewResId);
        final View view2 = activity.findViewById(secondViewResId);

        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = activity.getResources().getInteger(android.R.integer.config_shortAnimTime);

            view2.setVisibility(first ? View.GONE : View.VISIBLE);
            view2.animate().setDuration(shortAnimTime).alpha(
                    first ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view2.setVisibility(first ? View.GONE : View.VISIBLE);
                }
            });

            view1.setVisibility(first ? View.VISIBLE : View.GONE);
            view1.animate().setDuration(shortAnimTime).alpha(
                    first ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view1.setVisibility(first ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            view1.setVisibility(first ? View.VISIBLE : View.GONE);
            view2.setVisibility(first ? View.GONE : View.VISIBLE);
        }
    }
}
