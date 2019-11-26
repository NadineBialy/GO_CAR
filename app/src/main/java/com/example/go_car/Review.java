package com.example.go_car;

public class Review {


    private String review ;
    private String name ;

    public Review(String name , String review) {

        this.name = name ;
        this.review=review;
    }



    public String getReview() {
        return review;
    }

    public String getName() {
        return name;
    }

    public void print(){
        System.out.println(name + "INSIDE REVIREW");

    }



}
