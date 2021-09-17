package com.unisalento.iotlab.deviceconfigurationservice.app;

/**
 * Created by WhatAWonderfulMussel on 06/08/2017.
 */

public class AppConfig {

    public static final String ipServer = "192.168.1.108";

    //SERVER URL
    public static String URL_LOGIN = "http://"+ ipServer +"/technicianLogin.php";
    public static String URL_SEARCH_USER = "http://"+ ipServer +"/technician/getUser.php";
    public static String URL_GET_USER_BUILDINGS = "http://"+ ipServer +"/technician/getUserBuildings.php";
    public static String URL_GET_BUILDING_FLOORS = "http://"+ ipServer +"/technician/getBuildingFloors.php";
    public static String URL_GET_FLOOR_ROOMS = "http://"+ ipServer +"/technician/getFloorRooms.php";
    public static String URL_GET_ROOM_DEVICE  = "http://"+ ipServer +"/technician/getRoomDevice.php";
    public static String URL_CONFIGURE_DEVICE = "http://"+ ipServer +"/technician/configDevice.php";
    public static String URL_DISSOCIATE_DEVICE = "http://"+ ipServer +"/technician/dissociateDevice.php";



}
