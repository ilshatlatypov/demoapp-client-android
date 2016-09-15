package ru.jvdev.demoapp.client.android.spinner;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import ru.jvdev.demoapp.client.android.R;

/**
 * Created by ilshat on 30.08.16.
 */
public class SpinnerWithChooseItemArrayAdapter<T> extends ArrayAdapter<T> {

    public SpinnerWithChooseItemArrayAdapter(Context context, int resource) {
        super(context, resource);
    }

    public SpinnerWithChooseItemArrayAdapter(Context context, int resource, T[] objects) {
        super(context, resource, objects);
    }

    @Override
    public boolean isEnabled(int position) {
        // Disable the first item from Spinner, first item will be use for hint
        return position > 0;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView tv = (TextView) view;
        int colorId = (position == 0) ? R.color.text_secondary : R.color.text_primary;
        tv.setTextColor(ContextCompat.getColor(this.getContext(), colorId));
        return view;
    }
}
