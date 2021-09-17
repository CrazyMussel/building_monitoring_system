package com.unisalento.iotlab.deviceconfigurationservice.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.unisalento.iotlab.deviceconfigurationservice.R;
import com.unisalento.iotlab.deviceconfigurationservice.app.AppConfig;
import com.unisalento.iotlab.deviceconfigurationservice.app.AppController;
import com.unisalento.iotlab.deviceconfigurationservice.app.ActivityConfig;
import com.unisalento.iotlab.deviceconfigurationservice.util.JSONToolbox;
import com.unisalento.iotlab.deviceconfigurationservice.util.SQLiteHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class FloorsActivity extends Activity {

    private static final String TAG = FloorsActivity.class.getSimpleName();

    private SQLiteHandler db;

    private ListView floorsList;
    private ProgressDialog progDialog;

    ArrayList<Map<String, String>> floors = new ArrayList<Map<String, String>>();
    private int idFloor;
    private int idBuilding = ActivityConfig.getIdBuilding();

    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.itemsmenu);

        db = new SQLiteHandler(getApplicationContext());

        progDialog = new ProgressDialog(this);
        progDialog.setCancelable(false);

        getFloors();
    }





    //SQLITE DATABASE REQUEST METHODS
    /**
     * Getting floor by building id from SQLite database
     */
    public void getFloors() {

        floors = db.getFloorByBuildingID(ActivityConfig.getIdBuilding());
        if (floors == null){
            makeFloorsRequest();
        }else {
            createListView();
        }
    }


    //VOLLEY MANAGING METHODS
    /**
     * Make floors request by buildingID
     */
    public void makeFloorsRequest() {

        String tag_req = "req_floors";

        progDialog.setMessage("Ricerca piani edificio...");
        showDialog();

        StringRequest request = new StringRequest(Method.POST,
                AppConfig.URL_GET_BUILDING_FLOORS, this.createResponseListenerFloor(),
                this.createErrorListener()){
            @Override
            protected Map<String, String> getParams(){

                Map<String, String> params = new HashMap<String, String>();
                params.put("idBuilding", String.valueOf(idBuilding));
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request, tag_req);
    }

    /**
     * Create response listener for reaching floor info
     * */
    public Response.Listener createResponseListenerFloor(){

        Response.Listener responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Floor searching response: " + response.toString());
                hideDialog();
                try {
                    response = response.substring(response.indexOf('{'));
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("ERROR");

                    if (!error) {

                        JSONArray flr = jObj.getJSONArray("Floors");
                        floors = JSONToolbox.getInstance().JSONtoMapList(flr);
                        db.addFloor(floors);
                        createListView();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("ERROR_MSG");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        };

        return responseListener;
    }

    /**
     * Create Volley Error Listener
     * */
    public Response.ErrorListener createErrorListener(){

        Response.ErrorListener responseErrorListener = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Searching Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        };

        return responseErrorListener;
    }




    //UTILITY
    /**
     * Get floor id by name
     * */
    public int getFloorID(final String name) {

        int idFloor = 0;

        for (int i=0; i<floors.size(); i++){
            if( floors.get(i).get("name") == name){

                idFloor = Integer.valueOf(floors.get(i).get("idFloor"));
            }
        }
        return idFloor;
    }


    public void createListView(){

        floorsList = (ListView) findViewById(R.id.list);
        final ArrayList<String> floorName = new ArrayList<String>();

        for (int i = 0; i < floors.size(); i++) {

            floorName.add(floors.get(i).get("name"));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, floorName);

        floorsList.setAdapter(adapter);

        floorsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String floor = (String) floorsList.getItemAtPosition(position);

                idFloor = getFloorID(floor);
                ActivityConfig.setIdFloor(idFloor);

                //launch next activity
                launchNextActivity();
            }
        });
    }

    @Override
    public void onBackPressed(){

        launchPreviousActivity();

    }

    public void showDialog() {
        if (!progDialog.isShowing()) {
            progDialog.show();
        }
    }


    public void hideDialog() {
        if (progDialog.isShowing()) {
            progDialog.dismiss();
        }
    }


    public void launchNextActivity(){

        Intent intent = new Intent(FloorsActivity.this,
                RoomsActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
        finish();
    }

    public void launchPreviousActivity(){

        Intent intent = new Intent(FloorsActivity.this,
                BuildingsActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
        finish();
    }
}


