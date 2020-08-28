package com.example.ferreteriacontrol;

public class Product {
    private String image;
    private String name;
    private  String brand;
    private double price; //change it to double
    private int amount;
    private int group;


    public Product(String name, String brand, double price, int amount, String image, int group){
        this.image = image;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.amount = amount;
        this.group = group;
    }


    public Product(String name, String brand, double price, int amount, int group){
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.amount = amount;
        this.group = group;
    }

    public Product(){
        //empty constructor
    }
    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public double getPrice() {
        return price;
    }

    public int getAmount() {
        return amount;
    }

    public int getGroup() {
        return group;
    }

}
