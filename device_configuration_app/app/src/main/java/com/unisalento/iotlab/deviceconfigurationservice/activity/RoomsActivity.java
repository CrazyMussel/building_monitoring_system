package com.unisalento.iotlab.deviceconfigurationservice.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.unisalento.iotlab.deviceconfigurationservice.R;
import com.unisalento.iotlab.deviceconfigurationservice.activity.adapter.RoomAdapter;
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


public class RoomsActivity extends Activity {

    private static final String TAG  = BuildingsActivity.class.getSimpleName();
    private static final String messageAssignedDevice = "Il dispositivo è già stato assegnato, si desidera dissociarlo dalla stanza?";

    private SQLiteHandler db;

    private ListView roomsList;
    private ProgressDialog progDialog;

    private ArrayList<Map<String, String>> rooms = new ArrayList<Map<String, String>>();
    private ArrayList<Map<String, String>> devices = new ArrayList<Map<String, String>>();
    private int idFloor;
    private int idDevice;
    private boolean deviceInfoCollected;

    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.itemsmenu);
        progDialog = new ProgressDialog(this);
        progDialog.setCancelable(false);

        db = new SQLiteHandler(getApplicationContext());
        this.deviceInfoCollected = false;

        idFloor = ActivityConfig.getIdFloor();

        getRooms();
    }




    /**
     * Getting  all floor rooms
     **/
    public void getRooms(){
        rooms = db.getRoomsByFloorID(ActivityConfig.getIdFloor());
        if (rooms == null){
            roomsRequest();
        } else {
            getDevice(rooms);
        }
    }

    /**
     * Getting devices associated to the rooms
     * */
    public void getDevice(ArrayList<Map<String, String>> rooms){

        int i=0;
        while(i<rooms.size()) {
            int idRoom = Integer.valueOf(rooms.get(i).get("idRoom"));
            String nameRoom = rooms.get(i).get("name");
            deviceRequest(idRoom, nameRoom);
            if (( rooms.size() - 1) == i ){
                this.deviceInfoCollected = true;
            }
            i++;
        }
    }






    //VOLLEY MANAGING METHODS
    /**
     * Make device request by roomID
     * */
    public void deviceRequest(final int idRoom, String name){

        String tag_req = "req_device";

        progDialog.setMessage("Ricerca dispositivo " + name + "...");
        showDialog();

        StringRequest request = new StringRequest(Method.POST,
                AppConfig.URL_GET_ROOM_DEVICE, this.createResponseListenerDevice(),
                this.createErrorListener()){

            @Override
            protected Map<String, String> getParams(){

                Map<String, String> params = new HashMap<String, String>();
                params.put("idRoom", String.valueOf(idRoom));

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request, tag_req);
    }


    /**
     * Make rooms request by floorID
     */
    public void roomsRequest() {

        String tag_req = "req_rooms";

        progDialog.setMessage("Ricerca stanze piano...");
        showDialog();

        StringRequest request = new StringRequest(Method.POST,
                AppConfig.URL_GET_FLOOR_ROOMS, this.createResponseListenerRoom(),
                this.createErrorListener()){

            @Override

            protected Map<String, String> getParams(){

                Map<String, String> params = new HashMap<String, String>();
                params.put("idFloor", String.valueOf(idFloor));
                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(request, tag_req);
    }


    /**
     * Make device dissociation request
     * */

    public void dissociationRequest(final int idDevice){

        String tag_req = "dissoc_req";

        progDialog.setMessage("Dissociazione dispositivo...");
        showDialog();

        StringRequest request = new StringRequest(Method.POST,
                AppConfig.URL_DISSOCIATE_DEVICE, this.createResponseListenerRoom(),
                this.createErrorListener()){

            @Override

            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("idDevice", String.valueOf(idDevice));
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request, tag_req);
    }


    /**
     * Create response listener for reaching room info
     * */
    public Response.Listener createResponseListenerRoom(){

        Response.Listener responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d(TAG, "Room searching response: " + response.toString());

                hideDialog();

                try {
                    response = response.substring(response.indexOf('{'));
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("ERROR");

                    if (!error) {

                        JSONArray rm = jObj.getJSONArray("Rooms");
                        ArrayList<Map<String, String>> floorRooms = JSONToolbox.getInstance().JSONtoMapList(rm);

                        // Inserting row in room table
                        db.addRoom(floorRooms);
                        //getting rooms from database
                        rooms = db.getRoomsByFloorID(idFloor);

                        //getting device associated to the room
                        getDevice(rooms);

                    } else {
                        //Get the error message
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
     * Create response listener for reaching device info
     * */
    public Response.Listener createResponseListenerDevice(){

        Response.Listener responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d(TAG, "Device searching response: " + response.toString());

                hideDialog();

                try {

                    Map<String, String> device = new HashMap<String, String>();

                    try{
                        response = response.substring(response.indexOf('{'));
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("ERROR");


                        if (!error) {

                            JSONObject dev = jObj.getJSONObject("Device");


                            device.put("idDevice", String.valueOf(dev.getInt("idDevice")));
                            device.put("macAddress", dev.getString("macAddress"));
                            device.put("idRoom", String.valueOf(dev.getInt("idRoom")));
                        }

                    } catch(StringIndexOutOfBoundsException e) {

                        device.put("idDevice", "Nessuno");
                        device.put("macAddress", "");
                        device.put("idRoom", "");


                        }
                    devices.add(device);

                    if (deviceInfoCollected == true) {
                        //create list view
                        createListView();
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
     * Create response listener for reaching device info
     * */
    public Response.Listener createResponseListenerDissociation(){

        Response.Listener responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d(TAG, "Dissociation result: " + response.toString());

                hideDialog();

                try {
                    response = response.substring(response.indexOf('{'));
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("ERROR");
                    String message = jObj.getString("OUTCOME");

                    if(!error){

                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }else{

                        Toast.makeText(getApplicationContext(), "ERROR: " + message, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {

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





    @Override
    public void onBackPressed(){

        launchPreviousActivity();
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

    public void createListView(){

        roomsList = (ListView) findViewById(R.id.list);
        final ArrayList<String> roomName = new ArrayList<String>();

        for(int i=0; i < rooms.size(); i++){

            roomName.add(rooms.get(i).get("name"));
        }

        final RoomAdapter adapter = new RoomAdapter(this, rooms, devices);

        roomsList.setAdapter(adapter);

        roomsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Map<String, String> room =  (Map<String, String>) roomsList.getItemAtPosition(position);
                String roomStr = room.get("name");
                idDevice = 0;

                for (int i=0; i<devices.size(); i++) {

                    if (devices.get(i).get("idRoom").equals(room.get("idRoom")))
                        idDevice = Integer.valueOf(devices.get(i).get("idDevice"));
                }
                ActivityConfig.setIdRoom(getRoomID(roomStr));


                //launch next activity
                if ( idDevice != 0 ) {
                    showAlertDialog(messageAssignedDevice);
                }else{
                    launchNextActivity();
                }


            }
        });
    }


    //UTILITY
    /**
     * Get room ID by name
     * */
    public int getRoomID(final String name){

        int idRoom =0;
        for (int i=0; i<rooms.size(); i++){
            String roomName = rooms.get(i).get("name");
            if (roomName.equals(name)){
                idRoom = Integer.valueOf(rooms.get(i).get("idRoom"));
            }
        }
        return idRoom;
    }



    public void launchNextActivity(){

        Intent intent = new Intent(RoomsActivity.this,
                DeviceConfigActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
        finish();
    }

    public void launchPreviousActivity(){

        Intent intent = new Intent(RoomsActivity.this,
                FloorsActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
        finish();
    }

    public void refreshActivity(){

        Intent intent = new Intent(RoomsActivity.this,
                RoomsActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
        finish();
    }


    public void showAlertDialog(String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(RoomsActivity.this);

        builder.setTitle("AVVISO");
        builder.setMessage(message);
        builder.setPositiveButton("Dissocia", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                dissociationRequest(idDevice);
                refreshActivity();
            }
        });
        builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }
        });
        builder.show();
    }

}



