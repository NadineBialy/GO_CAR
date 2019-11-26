package com.example.go_car;

import java.util.Collections;
import java.util.Comparator;

public class car implements Comparable<car> {



    private int ID ;
    private String Name ;
    private String Latitude ;
    private String Longitude ;
    private String imagep ;
    private String Prod_Year ;
    private String Fuel_Level ;
    private double Distance ;



    public car(int id , String name  , String fuel_Level, String prod_Year  ,double distance, String Imagep, String latitude , String longitude) {

        ID = id ;
        Name = name;
        Prod_Year = prod_Year ;
        Fuel_Level = fuel_Level ;
        Distance =  distance ;
        imagep = Imagep ;
        Latitude = latitude ;
        Longitude = longitude ;
    }

    public double getDistance() {
        return Distance;
    }
    public String getName() {
        return Name;
    }

    public String getLatitude() {
        return Latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public String getImage_Path() {
        return imagep;
    }

    public String getProd_Year() {
        return Prod_Year;
    }

    public String getFuel_Level() {
        return Fuel_Level;
    }

    public String getID() {
        return Integer.toString(ID);
    }




    @Override
    public int compareTo(car other){
        // compareTo should return < 0 if this is supposed to be
        // less than other, > 0 if this is supposed to be greater than
        // other and 0 if they are supposed to be equal
        return Double.compare(this.Distance, other.Distance);
        //return last == 0 ? Double.compare(this.Distance, other.Distance) : last;
    }

}
