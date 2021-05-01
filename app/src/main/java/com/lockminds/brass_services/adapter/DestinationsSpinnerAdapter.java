package com.lockminds.brass_services.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.lockminds.brass_services.R;
import com.lockminds.brass_services.model.Destination;

import java.util.ArrayList;
public class DestinationsSpinnerAdapter extends ArrayAdapter<Destination> {

    private ArrayList<Destination> statuses;
    public Resources res;
    Destination currRowVal = null;
    LayoutInflater inflater;

    public DestinationsSpinnerAdapter(Context context,
                              int textViewResourceId, ArrayList<Destination> statuses,
                              Resources resLocal) {
        super(context, textViewResourceId, statuses);
        this.statuses = statuses;
        this.res = resLocal;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        View row = inflater.inflate(R.layout.item_spinner, parent, false);
        currRowVal = null;
        currRowVal = (Destination) statuses.get(position);
        TextView label = (TextView) row.findViewById(R.id.spinnerItem);
        if (position == 0) {
            label.setText("Please select status");
        } else {
            label.setText(currRowVal.getName());
        }

        return row;
    }
}
