package ru.jvdev.demoapp.client.android.spinner;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

/**
 * Created by ilshat on 30.08.16.
 */
public class SpinnerWithChooseItemListener implements AdapterView.OnItemSelectedListener {

    private Context context;

    public SpinnerWithChooseItemListener(Context context) {
        this.context = context;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // If user change the default selection
        // First item is disable and it is used for hint
        if (position > 0) {
            int color = ContextCompat.getColor(context, android.R.color.primary_text_light);
            ((TextView) view).setTextColor(color);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
