package com.unisalento.iotlab.deviceconfigurationservice.app;




public class ActivityConfig {


    //USER INFO
    private static int idUser;

    //BUILDING INFO
    private static int idBuilding;

    //FLOOR
    private static int idFloor;

    //ROOM
    private static int idRoom;

    //DEVICE
    private static int idDevice;


    public static int getIdUser(){return idUser;}

    public static void setIdUser(int idUser){ ActivityConfig.idUser = idUser; }

    public static int getIdBuilding() {
        return idBuilding;
    }

    public static void setIdBuilding(int idBuilding) { ActivityConfig.idBuilding = idBuilding; }

    public static int getIdFloor() {
        return idFloor;
    }

    public static void setIdFloor(int idFloor) {
        ActivityConfig.idFloor = idFloor;
    }

    public static int getIdRoom() { return idRoom; }

    public static void setIdRoom(int idRoom) {
        ActivityConfig.idRoom = idRoom;
    }

    public static int getIdDevice() {
        return idDevice;
    }

    public static void setIdDevice(int idDevice) {
        ActivityConfig.idDevice = idDevice;
    }
}
