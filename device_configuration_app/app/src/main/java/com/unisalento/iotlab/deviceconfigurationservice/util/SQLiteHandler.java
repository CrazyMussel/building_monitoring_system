package com.unisalento.iotlab.deviceconfigurationservice.util;

import android.database.sqlite.SQLiteOpenHelper;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    //database version
    private static final int DATABASE_VERSION = 1;
    //database name
    private static final String DATABASE_NAME = "android_api";
    //login table name
    private static final String TABLE_TECHNICIAN = "Technician";
    //user table name
    private static final String TABLE_USER = "User";
    //building table name
    private static final String TABLE_BUILDING = "Building";
    //floor table name
    private static final String TABLE_FLOOR = "Floor";
    //room table name
    private static final String TABLE_ROOM = "Room";

    //technician columns name
    private static final String KEY_ID = "Id";
    private static final String KEY_NAME = "Name";
    private static final String KEY_SURNAME = "Surname";
    private static final String KEY_EMAIL = "Email";
    private static final String KEY_CELLPHONE = "CellPhoneNumb";
    private static final String KEY_HEADQUARTERS = "Headquarters";
    private static final String KEY_WORKZONE = "WorkZone";

    //user columns name
    private static final String KEY_USER_ID = "idUser";
    private static final String KEY_USERNAME = "Username";
    private static final String KEY_USER_NAME = "Name";
    private static final String KEY_USER_SURNAME = "Surname";
    private static final String KEY_USER_EMAIL = "Email";

    //building columns name
    private static final String KEY_BUILDING_ID = "idBuilding";
    private static final String KEY_NAME_BUILDING = "Name";
    private static final String KEY_NATION = "Nation";
    private static final String KEY_CITY = "City";
    private static final String KEY_ADDRESS = "Address";
    private static final String KEY_ZIPCODE = "ZipCode";
    private static final String KEY_LATITUDE_DD = "LatitudeDD";
    private static final String KEY_LONGITUDE_DD = "LongitudeDD";

    //floor columns name
    private static final String KEY_FLOOR_ID = "idFloor";
    private static final String KEY_NAME_FLOOR = "Name";

    //room column name
    private static final String KEY_ROOM_ID = "idRoom";
    private static final String KEY_NAME_ROOM = "Name";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    //creating tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_TECHNICIAN + " ("
                + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT, "
                + KEY_SURNAME + " TEXT," + KEY_EMAIL + " TEXT NOT NULL UNIQUE, "
                + KEY_CELLPHONE + " TEXT, " + KEY_HEADQUARTERS + " TEXT, "
                + KEY_WORKZONE + " TEXT" + " )";

        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + " ("
                + KEY_USER_ID + " INTEGER PRIMARY KEY, " + KEY_USERNAME
                + " TEXT, " + KEY_USER_NAME + " TEXT, " + KEY_USER_SURNAME
                + " TEXT, " + KEY_USER_EMAIL + " TEXT " + ")";

        String CREATE_BUILDING_TABLE = "CREATE TABLE " + TABLE_BUILDING + " ("
                + KEY_BUILDING_ID + " INTEGER PRIMARY KEY, " + KEY_USER_ID
                + " INTEGER, " + KEY_NAME_BUILDING + " TEXT, "+ KEY_NATION
                + " TEXT, " + KEY_CITY + " TEXT, " + KEY_ADDRESS + " TEXT, "
                + KEY_ZIPCODE + " TEXT, " + KEY_LATITUDE_DD + " TEXT, "
                + KEY_LONGITUDE_DD + " TEXT " + ")";

        String CREATE_FLOOR_TABLE = "CREATE TABLE " + TABLE_FLOOR + " ("
                + KEY_FLOOR_ID + " INTEGER PRIMARY KEY, " + KEY_BUILDING_ID
                + " INTEGER, " + KEY_NAME_FLOOR + " TEXT " + ")";

        String CREATE_ROOM_TABLE = "CREATE TABLE " + TABLE_ROOM + " ("
                + KEY_ROOM_ID + " INTEGER PRIMARY KEY, " + KEY_FLOOR_ID +
                " INTEGER, " + KEY_NAME_ROOM + " TEXT" + " )";

        db.execSQL(CREATE_LOGIN_TABLE);
        Log.d(TAG, "Database technician table created!");

        db.execSQL(CREATE_USER_TABLE);
        Log.d(TAG, "Database user table created!");

        db.execSQL(CREATE_BUILDING_TABLE);
        Log.d(TAG, "Database building table created!");

        db.execSQL(CREATE_FLOOR_TABLE);
        Log.d(TAG, "Database floor table created!");

        db.execSQL(CREATE_ROOM_TABLE);
        Log.d(TAG, "Database room table created!");
    }



    //upgrade database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TECHNICIAN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUILDING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FLOOR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOM);
        //create table again
        onCreate(db);
    }






    //TECHNICIAN CRUD

    /**
     * Storing technician details in database
     */
    public void addTechnician(String id, String name, String surname, String email, String cellphone, String headquarters, String workzone) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, id);
        values.put(KEY_NAME, name);
        values.put(KEY_SURNAME, surname);
        values.put(KEY_EMAIL, email);
        values.put(KEY_CELLPHONE, cellphone);
        values.put(KEY_HEADQUARTERS, headquarters);
        values.put(KEY_WORKZONE, workzone);

        //inserting row
        long idIns = db.insert(TABLE_TECHNICIAN, null, values);
        db.close(); //close database connection

        Log.d(TAG, "New technician inserted in SQLite database: " + idIns);
    }


    /**
     * Getting technician data from database
     */
    public Map<String, String> getTechnicianDetails() {
        Map<String, String> technician = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_TECHNICIAN;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        //move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            technician.put("Id", cursor.getString(0));
            technician.put("name", cursor.getString(1));
            technician.put("surname", cursor.getString(2));
            technician.put("email", cursor.getString(3));
            technician.put("cellphone", cursor.getString(4));
            technician.put("headquarters", cursor.getString(5));
            technician.put("workzone", cursor.getString(6));
        }
        cursor.close();
        db.close();
        //return result
        Log.d(TAG, "Fetching technician from SQLite database: " + technician.toString());

        return technician;
    }

    /**
     * delete technician from database
     **/
    public void deleteTechnician() {
        SQLiteDatabase db = this.getWritableDatabase();
        //delete all rows
        db.delete(TABLE_TECHNICIAN, null, null);
        db.close();

        Log.d(TAG, "Deleted all technician info from local sqlite database");
    }






    //USER CRUD

    /**
     * Storing user details in database
     */
    public void addUser(int idUser, String username, String name, String surname, String email) {

        SQLiteDatabase db = this.getWritableDatabase();
        String id = String.valueOf(idUser);
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, id);
        values.put(KEY_USERNAME, username);
        values.put(KEY_USER_NAME, name);
        values.put(KEY_USER_SURNAME, surname);
        values.put(KEY_USER_EMAIL, email);
        //inserting row
        long idIns = db.insert(TABLE_USER, null, values);
        db.close(); //close database connection

        Log.d(TAG, "New user inserted in SQLite database: " + idIns);
    }

    /**
     * Get user details from database
     * */
    public Map<String, String> getUserDetails(){
        Map<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0){
            user.put("idUser", String.valueOf(cursor.getInt(0)));
            user.put("username", cursor.getString(1));
            user.put("name", cursor.getString(2));
            user.put("surname", cursor.getString(3));
            user.put("email", cursor.getString(4));
        }

        cursor.close();
        db.close();

        Log.d(TAG, "Fetching user from SQLite database: " + user.toString());

        return user;
    }

    /**
     * Delete user from database
     * */
    public void deleteUser(){
        SQLiteDatabase db = this.getWritableDatabase();
        //delete all rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from local sqlite database" );
    }







    //BUILDING CRUD

    /**
     * Storing all buildings in database
     * */
    public void addBuilding(ArrayList<Map<String, String>> buildingDetails, int idUser){

        SQLiteDatabase db = this.getWritableDatabase();
        for(int i=0; i<buildingDetails.size(); i++){
            ContentValues values = new ContentValues();
            values.put(KEY_BUILDING_ID, buildingDetails.get(i).get("idBuilding"));
            values.put(KEY_USER_ID, String.valueOf(idUser));
            values.put(KEY_NAME_BUILDING, buildingDetails.get(i).get("name"));
            values.put(KEY_NATION, buildingDetails.get(i).get("nation"));
            values.put(KEY_CITY, buildingDetails.get(i).get("city"));
            values.put(KEY_ADDRESS, buildingDetails.get(i).get("address"));
            values.put(KEY_ZIPCODE, buildingDetails.get(i).get("zipcode"));
            values.put(KEY_LATITUDE_DD, buildingDetails.get(i).get("latitudeDD"));
            values.put(KEY_LONGITUDE_DD, buildingDetails.get(i).get("longitudeDD"));
            long id = db.insert(TABLE_BUILDING, null, values);
            Log.d(TAG, "New building inserted in SQLite database: " + String.valueOf(id));
        }
        db.close();
        Log.d(TAG, "All buildings inserted in SQLite database!");
    }

    /**
     * Getting all user's buildings from database
     * */
    public ArrayList<Map<String, String>> getAllBuildings(){
        ArrayList<Map<String, String>> allBuildings = new ArrayList<Map<String, String>>();

        String selectQuery = "SELECT * FROM " + TABLE_BUILDING;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        try {
            do {
                HashMap<String, String> building = new HashMap<String, String>();
                building.put("idBuilding", String.valueOf(cursor.getInt(0)));
                building.put("idUser", String.valueOf(cursor.getInt(1)));
                building.put("name", cursor.getString(2));
                building.put("nation", cursor.getString(3));
                building.put("city", cursor.getString(4));
                building.put("address", cursor.getString(5));
                building.put("zipcode", cursor.getString(6));
                building.put("latitudeDD", cursor.getString(7));
                building.put("longitudeDD", cursor.getString(8));
                allBuildings.add(building);
            } while (cursor.moveToNext());
        } catch (Exception e){ return null;}

        cursor.close();
        db.close();

        Log.d(TAG, "Fetching all buildings from SQLite database");

        return allBuildings;

    }

    /**
     * Get building by name
     * */
    public Map<String, String> getBuilding(String name){
        Map<String, String> building = new HashMap<String, String>();

        String selectQuery = "SELECT * FROM " + TABLE_BUILDING + " WHERE Name = " + name;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            building.put("idBuilding", String.valueOf(cursor.getInt(0)));
            building.put("name", cursor.getString(1));
            building.put("nation", cursor.getString(2));
            building.put("city", cursor.getString(3));
            building.put("address", cursor.getString(4));
            building.put("zipcode", cursor.getString(5));
            building.put("latitudeDD", cursor.getString(6));
            building.put("longitudeDD", cursor.getString(7));
        }
        cursor.close();
        db.close();

        Log.d(TAG, "Fetching building from SQLite database: " + building.toString());

        return building;

    }

    /**
     * Delete all buildings from database
     * */
    public void deleteBuildings(){

        SQLiteDatabase db = this.getWritableDatabase();
        //delete all rows
        db.delete(TABLE_BUILDING, null, null);
        db.close();

        Log.d(TAG, "Deleted all buildings info from SQLite database");

    }






    //FLOOR CRUD
    /**
     * Storing new building floor in database
     * */
    public void addFloor(int idFloor, int idBuilding, String name){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FLOOR_ID, String.valueOf(idFloor));
        values.put(KEY_BUILDING_ID, String.valueOf(idBuilding));
        values.put(KEY_NAME_FLOOR, name);

        long idIns = db.insert(TABLE_FLOOR, null, values);
        db.close();

        Log.d(TAG, "New floor inserted in SQLite database: " + idIns);
    }

    /**
     * Storing floors in database
     * */
    public void addFloor(ArrayList<Map<String, String>> floors) {

        SQLiteDatabase db = this.getWritableDatabase();
        for (int i=0; i < floors.size(); i++){
            ContentValues values = new ContentValues();
            values.put(KEY_FLOOR_ID, floors.get(i).get("idFloor"));
            values.put(KEY_BUILDING_ID, floors.get(i).get("idBuilding"));
            values.put(KEY_NAME_FLOOR, floors.get(i).get("name"));

            long idIns = db.insert(TABLE_FLOOR, null, values);
            Log.d(TAG, "New floor inserted: " + idIns);


        }
        db.close();

        Log.d(TAG, "All floors inserted in SQLite database!");

    }

    /**
     * Getting all floors from database
     * */
    public ArrayList<Map<String, String>> getFloorByBuildingID(int idBuilding){
        ArrayList<Map<String, String>> allFloors = new ArrayList<Map<String, String>>();

        String selectQuery = "SELECT * FROM " + TABLE_FLOOR + " WHERE idBuilding = " + idBuilding;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        try {
            do {
                HashMap<String, String> floor = new HashMap<String, String>();
                floor.put("idFloor", String.valueOf(cursor.getInt(0)));
                floor.put("idBuilding", String.valueOf(cursor.getInt(1)));
                floor.put("name", cursor.getString(2));
                allFloors.add(floor);
            } while (cursor.moveToNext());
        } catch (Exception e){ return null; }

        return allFloors;

    }

    /**
     * Getting floor by id from database
     * */
    public Map<String, String> getFloor(int idFloor){

        Map<String, String> floor = new HashMap<String, String>();

        String selectQuery = "SELECT * FROM " + TABLE_FLOOR + " WHERE idFloor = " + String.valueOf(idFloor);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            floor.put("idFloor", String.valueOf(cursor.getInt(0)));
            floor.put("idBuilding", String.valueOf(cursor.getInt(1)));
            floor.put("name", cursor.getString(2));
        }
        cursor.close();
        db.close();

        Log.d(TAG, "Fetching floor from SQLite database: " + floor.toString());

        return floor;
    }

    /**
     * Getting floor by name from database
     * */
    public Map<String, String> getFloor(final String name){

        Map<String, String> floor = new HashMap<String, String>();

        String selectQuery = "SELECT * FROM " + TABLE_FLOOR + " WHERE Name = " + String.valueOf(name);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            floor.put("idFloor", String.valueOf(cursor.getInt(0)));
            floor.put("idBuilding", String.valueOf(cursor.getInt(1)));
            floor.put("name", cursor.getString(2));
        }
        cursor.close();
        db.close();

        Log.d(TAG, "Fetching floor from SQLite database: " + floor.toString());

        return floor;
    }

    /**
     * Delete all floors from database
     * */
    public void deleteFloors(){

        SQLiteDatabase db = this.getWritableDatabase();
        //delete all rows
        db.delete(TABLE_FLOOR, null, null);
        db.close();

        Log.d(TAG, "Deleted all floors info from SQLite database");

    }





    //ROOM CRUD

    /**
     * Storing new room in database
     * */
    public void addRoom(int idRoom, int idFloor, String name){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ROOM_ID, idRoom);
        values.put(KEY_FLOOR_ID, idFloor);
        values.put(KEY_NAME_FLOOR, name);

        long insId = db.insert(TABLE_ROOM, null ,values);
        db.close();

        Log.d(TAG, "New room inserted in SQLite database: " + insId);
    }

    /**
     * Storing a group of rooms in database
     * */
    public void addRoom(ArrayList<Map<String, String>> rooms){

        SQLiteDatabase db = this.getWritableDatabase();

        for (int i=0; i<rooms.size(); i++){

            ContentValues values = new ContentValues();
            values.put(KEY_ROOM_ID, Integer.valueOf(rooms.get(i).get("idRoom")));
            values.put(KEY_FLOOR_ID, Integer.valueOf(rooms.get(i).get("idFloor")));
            values.put(KEY_NAME_ROOM, rooms.get(i).get("name"));
            db.insert(TABLE_ROOM, null, values);
        }
        db.close();

        Log.d(TAG, "All rooms inserted in SQLite database!");
    }

    /**
     * Getting all rooms from database
     * */
    public ArrayList<Map<String, String>> getRoomsByFloorID(int idFloor){
        ArrayList<Map<String, String>> allRooms = new ArrayList<Map<String, String>>();

        String selectQuery = "SELECT * FROM " + TABLE_ROOM + " WHERE idFloor = " + String.valueOf(idFloor);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        try {
            do {
                HashMap<String, String> room = new HashMap<String, String>();
                room.put("idRoom", String.valueOf(cursor.getInt(0)));
                room.put("idFloor", String.valueOf(cursor.getInt(1)));
                room.put("name", cursor.getString(2));
                allRooms.add(room);
            } while (cursor.moveToNext());
        } catch (Exception e){ return null; }

        return allRooms;

    }

    /**
     * Getting room by id from database
     * */
    public Map<String, String> getRoomById(int idRoom){

        Map<String, String> room = new HashMap<String, String>();

        String selectQuery = "SELECT * FROM " + TABLE_ROOM + " WHERE idRoom = " + String.valueOf(idRoom);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            room.put("idRoom", cursor.getString(0));
            room.put("idFloor", cursor.getString(1));
            room.put("name", cursor.getString(2));
        }
        cursor.close();
        db.close();

        Log.d(TAG, "Fetching room from SQLite database: " + room.toString());

        return room;
    }

    /**
     * Delete all rooms from database
     * */
    public void deleteRooms(){

        SQLiteDatabase db = this.getWritableDatabase();
        //delete all rows
        db.delete(TABLE_ROOM, null, null);
        db.close();

        Log.d(TAG, "Deleted all rooms info from SQLite database");

    }
}

