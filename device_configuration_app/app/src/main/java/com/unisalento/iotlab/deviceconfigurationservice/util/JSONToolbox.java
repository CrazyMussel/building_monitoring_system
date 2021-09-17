package com.unisalento.iotlab.deviceconfigurationservice.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class JSONToolbox {

    private static JSONToolbox instance;


    //SINGLETON
    public static JSONToolbox getInstance(){

        if ( instance == null ){
            JSONToolbox instance = new JSONToolbox();
        }

        return instance;
    }




    public static ArrayList<Map<String, String>> JSONtoMapList(JSONArray jArray){


        ArrayList<Map<String, String>> jsonMapList = new ArrayList<Map<String, String>>();

        for (int i=0; i<jArray.length(); i++) {
            HashMap<String, String> jsonMap = new HashMap<String, String>();
            if (jArray != JSONObject.NULL) {
                try {
                    JSONObject jobj = (JSONObject) jArray.get(i);
                    Iterator<String> keyItr = jobj.keys();
                    while (keyItr.hasNext()) {
                        String key = keyItr.next();
                        String value = String.valueOf(jobj.get(key));

                        jsonMap.put(key, value);
                    }
                    jsonMapList.add(jsonMap);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonMapList;
    }


}

