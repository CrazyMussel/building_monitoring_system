package com.unisalento.iotlab.deviceconfigurationservice.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import android.view.View;
import android.app.ProgressDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.Request.Method;
import com.android.volley.VolleyError;

import org.json.JSONObject;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import com.unisalento.iotlab.deviceconfigurationservice.app.AppConfig;
import com.unisalento.iotlab.deviceconfigurationservice.app.AppController;
import com.unisalento.iotlab.deviceconfigurationservice.app.ActivityConfig;
import com.unisalento.iotlab.deviceconfigurationservice.util.SQLiteHandler;
import com.unisalento.iotlab.deviceconfigurationservice.util.SessionManager;
import com.unisalento.iotlab.deviceconfigurationservice.R;



public class UserActivity extends Activity {

    private static final String TAG = BuildingsActivity.class.getSimpleName();

    private SQLiteHandler db;
    private SessionManager session;

    private int idUser;

    private Button btnAvanti;
    private EditText inputEmail;
    private ProgressDialog progDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setting xml layout
        setContentView(R.layout.useridentify);

        inputEmail = (EditText) findViewById(R.id.userEmail);
        btnAvanti = (Button) findViewById(R.id.btnAvanti);

        session = new SessionManager(getApplicationContext());

        //if technician is not logged in don't set "avanti" clickable
        if(!session.isLoggedIn()) {
            btnAvanti.setClickable(false);
        }


        progDialog = new ProgressDialog(this);
        progDialog.setCancelable(false);



        btnAvanti.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                String userEmail = inputEmail.getText().toString().trim();

                db = new SQLiteHandler(getApplicationContext());

                //check for empty id or password
                if(!userEmail.isEmpty()) {
                    //search user in server
                    makeUserRequest(userEmail);

                } else {
                    //Prompt technician to enter user email
                    Toast.makeText(getApplicationContext(), "Please enter user email address!", Toast.LENGTH_LONG).show();
                }
            }
        });



    }


    /**
     * Set a Volley request to get User info
     * */
    public void makeUserRequest(final String email) {

        String tag_string_req = "req_user";

        progDialog.setMessage("Ricerca user...");
        showDialog();

        StringRequest request = new StringRequest(Method.POST,
                AppConfig.URL_SEARCH_USER, this.createResponseListenerUserInfo(), this.createErrorListener()){

            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request, tag_string_req);


    }






    /**
     * Create Volley Response Listener for reaching user info
     */
    public Response.Listener createResponseListenerUserInfo() {

        Response.Listener responseListener = new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "User searching Response: " + response.toString());
                hideDialog();

                try {
                    response = response.substring(response.indexOf('{'));
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("ERROR");

                    // Check for error node in json
                    if (!error) {

                        JSONObject user = jObj.getJSONObject("user");
                        idUser = user.getInt("idUser");
                        String username = user.getString("username");
                        String name = user.getString("name");
                        String surname = user.getString("surname");
                        String email = user.getString("email");

                        // Inserting row in user table
                        db.addUser(idUser, username, name, surname, email);

                        ActivityConfig.setIdUser(idUser);

                        //launch next activity
                        launchNextActiviy();

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

    public void launchNextActiviy(){

        Intent intent = new Intent(UserActivity.this,
                BuildingsActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
        finish();
    }

    public void launchPreviousActivity(){

        Intent intent = new Intent(UserActivity.this,
                ConfigMenuActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
        finish();
    }
}
