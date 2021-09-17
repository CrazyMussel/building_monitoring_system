package com.unisalento.iotlab.deviceconfigurationservice.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.Request.Method;
import com.android.volley.DefaultRetryPolicy;
import com.unisalento.iotlab.deviceconfigurationservice.app.AppConfig;
import com.unisalento.iotlab.deviceconfigurationservice.app.AppController;
import com.unisalento.iotlab.deviceconfigurationservice.app.ActivityConfig;
import com.unisalento.iotlab.deviceconfigurationservice.util.SQLiteHandler;
import com.unisalento.iotlab.deviceconfigurationservice.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class DeviceConfigActivity extends Activity {

    private static final String TAG = DeviceConfigActivity.class.getSimpleName();
    private static final int REQUEST_TIMEOUT = 180000;

    private int idRoom;

    private Button btnConfigure;
    private EditText editSSID;
    private EditText editPassword;
    private EditText editIdDevice;
    private EditText editIntervall;

    private ProgressDialog progDialog;

    private SQLiteHandler db;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.deviceconfiguration);
        btnConfigure = (Button) findViewById(R.id.btnConfigura);
        editIdDevice = (EditText) findViewById(R.id.idDev);
        editSSID = (EditText) findViewById(R.id.ssid);
        editPassword = (EditText) findViewById(R.id.netPass);
        editIntervall = (EditText) findViewById(R.id.intervall);

        idRoom = ActivityConfig.getIdRoom();

        progDialog = new ProgressDialog(this);
        progDialog.setCancelable(false);

        btnConfigure.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                String idDev = editIdDevice.getText().toString().trim();
                String ssid = editSSID.getText().toString().trim();
                String password = editPassword.getText().toString().trim();
                String intervall = editIntervall.getText().toString().trim();

                if (!idDev.isEmpty() && !ssid.isEmpty() && !password.isEmpty() && !intervall.isEmpty()) {

                    //check if device existing and configure it
                    configureDevice(idDev, ssid, password, intervall);
                } else {
                    Toast.makeText(getApplicationContext(), "Inserire tutte le informazioni richieste!", Toast.LENGTH_LONG).show();
                }
            }

        });

    }



    /**
     * Configure device
     * */
    public void configureDevice(final String idDevice, final String ssid, final String password, final String intervall){

        String tag_req = "conf_dev";

        progDialog.setMessage("Configurazione dispositivo: " + String.valueOf(idDevice));
        showDialog();

        StringRequest request = new StringRequest(Method.POST,
                AppConfig.URL_CONFIGURE_DEVICE, this.createResponseListener(),
                this.createErrorListener()){

            @Override
            protected Map<String, String> getParams(){

                Map<String, String> params = new HashMap<String, String>();
                params.put("idDevice", idDevice);
                params.put("ssid", ssid);
                params.put("password", password);
                params.put("idRoom", String.valueOf(idRoom));
                params.put("intervall", intervall);

                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(this.REQUEST_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(request, tag_req);
    }



    /**
     * Create response listener for device configuring response
     * */
    public Response.Listener createResponseListener(){

        Response.Listener responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d(TAG, "Configuration response: " + response.toString());

                hideDialog();

                try{
                    response = response.substring(response.indexOf('{'));
                    JSONObject jObj = new JSONObject(response);


                    String outcome  = jObj.getString("OUTCOME");
                    String message = jObj.getString("OUTCOME_MSG");

                    showAlertDialog(outcome, message);

                }catch(JSONException e){
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

    public void launchPreviousActivity(){

        Intent intent = new Intent(DeviceConfigActivity.this,
                RoomsActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
        finish();
    }

    public void showAlertDialog(String outcome, String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceConfigActivity.this);

        builder.setTitle("ESITO: " + outcome);
        builder.setMessage(message);
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                launchPreviousActivity();
            }
        });
        builder.show();
    }
}
