package com.unisalento.iotlab.deviceconfigurationservice.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.Request.Method;
import com.unisalento.iotlab.deviceconfigurationservice.R;
import com.unisalento.iotlab.deviceconfigurationservice.activity.adapter.BuildingAdapter;
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


public class BuildingsActivity extends Activity {

    private static final String TAG  = BuildingsActivity.class.getSimpleName();

    private boolean responseReached = false;

    private SQLiteHandler db;

    private ListView buildingsList;
    private ProgressDialog progDialog;

    private ArrayList<Map<String, String>> buildings = new ArrayList<Map<String, String>>();


    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.itemsmenu);
        progDialog = new ProgressDialog(this);
        progDialog.setCancelable(false);

        db = new SQLiteHandler(getApplicationContext());

        getBuildings();

    }





    //SQLITE DATABASE REQUEST METHODS
    /**
     * Getting all user's buildings from SQLite database
     * */
    public void getBuildings() {

        buildings = db.getAllBuildings();
        if (buildings == null)
            makeBuildingsRequest();
        else{
            createListView();
        }

    }

    /**
     * Set a Volley request to get buildings info
     * */
    public void makeBuildingsRequest(){

        String tag_string_req = "req_building";

        progDialog.setMessage("Ricerca edifici user...");
        showDialog();

        StringRequest request = new StringRequest(Method.POST,
                AppConfig.URL_GET_USER_BUILDINGS, this.createResponseListenerBuildings(),this.createErrorListener()){

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("idUser", String.valueOf(ActivityConfig.getIdUser()));

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request, tag_string_req);
    }


    /**
     * Create Volley Response Listener for reaching buildings info
     */
    public Response.Listener createResponseListenerBuildings() {

        Response.Listener responseListener = new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Buildings searching Response: " + response.toString());
                hideDialog();

                try {
                    response = response.substring(response.indexOf('{'));
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("ERROR");

                    // Check for error node in json
                    if (!error) {

                        JSONArray bd = jObj.getJSONArray("Building");
                        ArrayList<Map<String, String>> userBuildings = JSONToolbox.getInstance().JSONtoMapList(bd);

                        // Inserting row in building table
                        db.addBuilding(userBuildings, ActivityConfig.getIdUser());

                        //getting buildings from DB
                        buildings = db.getAllBuildings();

                        //create list view
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
     * Get building ID by name
     * */
    public int getBuildingID(final String nameBuilding){

        int idBuilding =0;
        for (int i=0; i<buildings.size(); i++){
            String builName = buildings.get(i).get("name");
            if (builName.equals(nameBuilding)){
                idBuilding = Integer.valueOf(buildings.get(i).get("idBuilding"));
            }

        }
        return idBuilding;
    }




    public void showDialog(){
        if(!progDialog.isShowing()) {
            progDialog.show();
        }
    }


    public void hideDialog(){
        if(progDialog.isShowing()){
            progDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed(){

        launchPreviousActivity();
    }

    public void createListView(){

        buildingsList = (ListView) findViewById(R.id.list);
        final ArrayList<String> buildingName = new ArrayList<String>();

        for(int i=0; i < buildings.size(); i++){

            buildingName.add(buildings.get(i).get("name"));
        }

        BuildingAdapter adapter = new BuildingAdapter(this, buildings );

        buildingsList.setAdapter(adapter);

        buildingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Map<String, String> building =  (Map<String, String>) buildingsList.getItemAtPosition(position);
                String buildName = building.get("name");

                ActivityConfig.setIdBuilding(getBuildingID(buildName));

                //launch next activity
                launchNextActivity();

            }
        });
    }


    public void launchNextActivity(){

        Intent intent = new Intent(BuildingsActivity.this,
                FloorsActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
        finish();
    }

    public void launchPreviousActivity(){
        db.deleteUser();
        db.deleteBuildings();
        db.deleteFloors();
        db.deleteRooms();
        Intent intent = new Intent(BuildingsActivity.this,
                UserActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
        finish();
    }


}



