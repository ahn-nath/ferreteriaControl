package com.example.ferreteriacontrol;

public class Product {
    private String image;
    private String name;
    private  String brand;
    private  String unit;
    private double price; //change it to double
    private int amount;
    private int group;


    public Product(String name, String brand, double price, int amount, String unit, String image, int group){
        this.image = image;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.amount = amount;
        this.unit = unit;
        this.group = group;
    }


    public Product(String name, String brand, double price, int amount, String unit, int group){
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.amount = amount;
        this.unit = unit;
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

    public String getUnit() {
        return unit;
    }

    public int getGroup() {
        return group;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public void setBrand(String brand) {
        this.brand = brand.trim();
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setGroup(int group) {
        this.group = group;
    }

}
