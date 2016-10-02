package ru.jvdev.demoapp.client.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.support.annotation.IdRes;
import android.view.View;

/**
 * Created by ilshat on 23.07.16.
 */
public class ViewSwitcher {

    private Activity activity;
    private Object parent; // Activity, View or Dialog

    private int progressBarResId;
    private int mainLayoutResId;
    private int errorLayoutResId;

    public ViewSwitcher(Activity activity, Dialog dialog,
                        @IdRes int progressBarResId,
                        @IdRes int mainLayoutResId) {
        this(activity, dialog, progressBarResId, mainLayoutResId, 0);

    }

    public ViewSwitcher(Activity activity,
                        @IdRes int progressBarResId,
                        @IdRes int mainLayoutResId,
                        @IdRes int errorLayoutResId) {
        this(activity, activity, progressBarResId, mainLayoutResId, errorLayoutResId);
    }

    public ViewSwitcher(Activity activity, View view,
                        @IdRes int progressBarResId,
                        @IdRes int mainLayoutResId,
                        @IdRes int errorLayoutResId) {
        this(activity, (Object) view, progressBarResId, mainLayoutResId, errorLayoutResId);
    }

    private ViewSwitcher(Activity activity, Object parent,
                         @IdRes int progressBarResId,
                         @IdRes int mainLayoutResId,
                         @IdRes int errorLayoutResId) {
        this.activity = activity;
        this.parent = parent;
        this.progressBarResId = progressBarResId;
        this.mainLayoutResId = mainLayoutResId;
        this.errorLayoutResId = errorLayoutResId;
    }

    public void showProgressBar() {
        show(progressBarResId);
    }

    public void showMainLayout() {
        show(mainLayoutResId);
    }

    public void showErrorLayout() {
        show(errorLayoutResId);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void show(int resId) {
        View progressBar, mainLayout, errorLayout;
        if (parent instanceof Activity) {
            progressBar = ((Activity) parent).findViewById(progressBarResId);
            mainLayout = ((Activity) parent).findViewById(mainLayoutResId);
            errorLayout = ((Activity) parent).findViewById(errorLayoutResId);
        } else if (parent instanceof View) {
            progressBar = ((View) parent).findViewById(progressBarResId);
            mainLayout = ((View) parent).findViewById(mainLayoutResId);
            errorLayout = ((View) parent).findViewById(errorLayoutResId);
        } else if (parent instanceof Dialog) {
            progressBar = ((Dialog) parent).findViewById(progressBarResId);
            mainLayout = ((Dialog) parent).findViewById(mainLayoutResId);
            errorLayout = ((Dialog) parent).findViewById(errorLayoutResId);
        } else {
            throw new IllegalArgumentException("Unexpected parent type: " + parent.getClass().toString());
        }

        boolean showProgressBar = (progressBar.getId() == resId);
        showView(progressBar, showProgressBar);

        boolean showMainLayout = (mainLayout.getId() == resId);
        showView(mainLayout, showMainLayout);

        if (errorLayout != null) {
            boolean showErrorLayout = (errorLayout.getId() == resId);
            showView(errorLayout, showErrorLayout);
        }
    }

    private void showView(final View view, final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = activity.getResources().getInteger(android.R.integer.config_shortAnimTime);

            view.setVisibility(show ? View.VISIBLE : View.GONE);
            view.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            view.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
