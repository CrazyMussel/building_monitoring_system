package com.unisalento.iotlab.deviceconfigurationservice.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.unisalento.iotlab.deviceconfigurationservice.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class RoomAdapter extends ArrayAdapter<Map<String, String>> {

    private int idDev;
    private ArrayList<Map<String, String>> devices = new ArrayList<Map<String, String>>();

    public RoomAdapter(Context context, ArrayList<Map<String, String>> rooms, ArrayList<Map<String, String>> devices){

        super(context, 0, rooms);
        this.devices = devices;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        Map<String, String> room = getItem(position);
        Map<String, String> device = new HashMap<String, String>();

        //check room - device corrisponding
        for (int i=0; i<devices.size(); i++) {

            if (devices.get(i).get("idRoom").equals(room.get("idRoom")))
                device = devices.get(i);
        }
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_room, parent, false);
        }

        TextView nameRoom = (TextView) convertView.findViewById(R.id.nameRoom);
        TextView idDevice = (TextView) convertView.findViewById(R.id.id_device);

        nameRoom.setText("Stanza: " + room.get("name"));
        try {
            idDev = Integer.valueOf(device.get("idDevice"));
        }catch(Exception e){ idDev = 0; }

        if( idDev != 0 ) {
            idDevice.setText("Dispositivo assegnato: " + String.valueOf(idDev));
        } else {
            idDevice.setText("Dispositivo assegnato: Nessuno");
        }

        return convertView;
    }

    public int getIdDev(){
        return idDev;
    }
}
