package ru.jvdev.demoapp.client.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;

/**
 * Created by ilshat on 23.07.16.
 */
public class ViewSwitcher {

    private View view;
    private Activity activity;

    private int progressBarResId;
    private int mainLayoutResId;
    private int errorLayoutResId;

    public ViewSwitcher(Activity activity, @IdRes int progressBarResId, @IdRes int mainLayoutResId, @IdRes int errorLayoutResId) {
        this.activity = activity;
        this.progressBarResId = progressBarResId;
        this.mainLayoutResId = mainLayoutResId;
        this.errorLayoutResId = errorLayoutResId;
    }

    public ViewSwitcher(Activity activity, View view, @IdRes int progressBarResId, @IdRes int mainLayoutResId, @IdRes int errorLayoutResId) {
        this.activity = activity;
        this.view = view;
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
        if (view != null) {
            progressBar = view.findViewById(progressBarResId);
            mainLayout = view.findViewById(mainLayoutResId);
            errorLayout = view.findViewById(errorLayoutResId);
        } else {
            progressBar = activity.findViewById(progressBarResId);
            mainLayout = activity.findViewById(mainLayoutResId);
            errorLayout = activity.findViewById(errorLayoutResId);
        }

        boolean showProgressBar = (progressBar.getId() == resId);
        boolean showMainLayout = (mainLayout.getId() == resId);
        boolean showErrorLayout = (errorLayout.getId() == resId);

        showView(progressBar, showProgressBar);
        showView(mainLayout, showMainLayout);
        showView(errorLayout, showErrorLayout);
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
