package com.unisalento.iotlab.deviceconfigurationservice.activity.adapter;


import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.widget.TextView;
import android.view.View;

import java.util.ArrayList;
import java.util.Map;

import com.unisalento.iotlab.deviceconfigurationservice.R;

public class BuildingAdapter extends ArrayAdapter<Map<String, String>>{

    public BuildingAdapter(Context context, ArrayList<Map<String, String>> buildings){

        super(context, 0, buildings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        Map<String, String> building = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_building, parent, false);
        }

        TextView nameBuilding = (TextView) convertView.findViewById(R.id.nameBuilding);
        TextView addressBuilding = (TextView) convertView.findViewById(R.id.addressBuilding);

        nameBuilding.setText(building.get("name"));
        addressBuilding.setText("Posizione: " + building.get("nation") + ", " + building.get("city")
                + ", " +building.get("address"));

        return convertView;
    }





}
