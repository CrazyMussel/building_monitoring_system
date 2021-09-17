package com.unisalento.iotlab.deviceconfigurationservice.activity;

/**
 * LOGIN activity for technician.
 */


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.unisalento.iotlab.deviceconfigurationservice.app.AppConfig;
import com.unisalento.iotlab.deviceconfigurationservice.app.AppController;
import com.unisalento.iotlab.deviceconfigurationservice.R;
import com.unisalento.iotlab.deviceconfigurationservice.util.SQLiteHandler;
import com.unisalento.iotlab.deviceconfigurationservice.util.SessionManager;


public class LoginActivity extends Activity {

    private static final String TAG = ConfigMenuActivity.class.getSimpleName();
    private Button btnLogin;
    private EditText inputId;
    private EditText inputPassword;
    private ProgressDialog progDialog;

    private SQLiteHandler db;
    private SessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setting default screen to login.xml
        setContentView(R.layout.login);

        inputId = (EditText) findViewById(R.id.idEmployee);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        //progress bar
        progDialog = new ProgressDialog(this);
        progDialog.setCancelable(false);

        session = new SessionManager(getApplicationContext());


        //check if technician is already logged in
        if(session.isLoggedIn()) {
            //redirect to ConfigurationMenu
            Intent intent = new Intent(LoginActivity.this, ConfigMenuActivity.class);
            startActivity(intent);
            finish();
        }



        //Login button click event
        btnLogin.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                String id = inputId.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                //check for empty id or password
                if(!id.isEmpty() && !password.isEmpty()) {
                    //login technician
                    checkLogin(id, password);
                } else {
                    //Prompt technician to enter credentials
                    Toast.makeText(getApplicationContext(), "Inserire credenziali!", Toast.LENGTH_LONG).show();
                }
            }

        });
    }


    /**
     * Verify login details in mysql db
     * */
    private void checkLogin(final String id, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        progDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_LOGIN, this.createResponseListenerLogin(),
                this.createErrorListenerLogin()){

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    /**
     * Create Volley Response Listener
     */
    public Response.Listener createResponseListenerLogin() {

        Response.Listener responseListener = new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    response = response.substring(response.indexOf('{'));
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("ERROR");

                    // Check for error node in json
                    if (!error) {
                        // user successfully logged in
                        // get or create session
                        session.setLogin(true);

                        // Now store the user in SQLite
                        String idTech = jObj.getString("idTech");

                        JSONObject technician = jObj.getJSONObject("technician");
                        String id = technician.getString("Id");
                        String name = technician.getString("name");
                        String surname = technician.getString("surname");
                        String email = technician.getString("email");
                        String cellphone = technician.getString("cellphone");
                        String headquarters = technician.getString("headquarters");
                        String workzone = technician.getString("workZone");


                        // Inserting row in technician table
                        db = new SQLiteHandler(getApplicationContext());
                        db.addTechnician(id, name, surname, email, cellphone, headquarters, workzone);

                        // Launch main activity
                        Intent intent = new Intent(LoginActivity.this,
                                ConfigMenuActivity.class);
                        startActivity(intent);
                        finish();


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
    public Response.ErrorListener createErrorListenerLogin(){

        Response.ErrorListener responseErrorListener = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        };

        return responseErrorListener;
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
}
