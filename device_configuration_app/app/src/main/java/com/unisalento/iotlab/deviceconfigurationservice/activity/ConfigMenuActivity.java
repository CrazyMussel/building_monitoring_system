package com.unisalento.iotlab.deviceconfigurationservice.activity;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

import com.unisalento.iotlab.deviceconfigurationservice.R;
import com.unisalento.iotlab.deviceconfigurationservice.util.SQLiteHandler;
import com.unisalento.iotlab.deviceconfigurationservice.util.SessionManager;


public class ConfigMenuActivity extends Activity {

    private static final String userDevicesTAG = BuildingsActivity.class.getSimpleName();
    private Button btnDevicesHandler;
    private Button btnLogout;

    private SQLiteHandler db;
    private SessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setting configMenu.xml
        setContentView(R.layout.configmenu);
        btnDevicesHandler = (Button) findViewById(R.id.btnDeviceHandler);
        btnLogout = (Button) findViewById(R.id.btnLogout);

        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        //Config new device button event
        btnDevicesHandler.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){

                Intent intent = new Intent(ConfigMenuActivity.this, UserActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
                finish();
            }

        });


        btnLogout.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){

                if(session.isLoggedIn()){
                    SQLiteDatabase database = db.getWritableDatabase();
                    /*db.deleteTechnician();
                    db.deleteUser();
                    db.deleteBuildings();
                    db.deleteFloors();
                    db.deleteRooms();*/
                    db.onUpgrade(database, 1, 1);
                    session.setLogin(false);
                    Intent intent = new Intent(ConfigMenuActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        });

    }







}
