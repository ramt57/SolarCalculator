package com.ramt57.solarcalc.model;

public class PlacesModel {
//    private static PlacesModel mInstance = null;

    private String name;
    private String address;
    private Double lat;
    private Double lng;
    private int id;

//    public static synchronized PlacesModel getInstance() {
//        if (mInstance == null)
//            mInstance = new PlacesModel();
//        return mInstance;
//    }

    /*database fields for given pojo*/
    public static final String TABLE_NAME = "places";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_Address = "address";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LNG = "lng";
    // Create Placestable SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NAME + " TEXT,"
                    + COLUMN_Address + " TEXT,"
                    + COLUMN_LAT + " TEXT,"
                    + COLUMN_LNG + " TEXT"
                    + ")";


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
