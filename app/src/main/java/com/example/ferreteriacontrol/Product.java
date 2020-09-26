package com.example.ferreteriacontrol;

import androidx.annotation.NonNull;

public class Product {
    private String image;
    private String name;
    private  String brand;
    private  String unit;
    private double price;
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

    public boolean setName(String name) {
        if(name.trim() != null && name.trim().length() > 3) {
            this.name = name.trim().substring(0,1).toUpperCase() + name.trim().substring(1);
            return true;
        }
        return false;
    }

    public boolean setBrand(String brand) {
        if(brand.trim() != null && brand.trim().length() > 3) {
            this.brand = brand.trim().substring(0,1).toUpperCase() + brand.trim().substring(1);
            return true;
        }
        return false;
    }

    public boolean setPrice(double price) {
        if(price > 0.0) {
            this.price = price;
            return true;
        }
        return false;
    }

    public boolean setAmount(int amount) {
        if(amount > 0) {
            this.amount = amount;
            return true;
        }
        return false;
    }

    public boolean setUnit(String unit) {
      unit = unit.toLowerCase();
        if(unit != null && (unit.trim().equals("u") || unit.trim().equals("kg") || unit.trim().equals("g"))) {
            this.unit = unit;
            return true;
        }
        return false;
    }

    public boolean setGroup(int group) {
        if(group > 0 && group <= 11) {
            this.group = group;
            return true;
        }
        return false;
        }

    public boolean readContentExcel(String token, int c){
    switch (c){
        case 0:
            return setName(token);

        case 1:
            return setBrand(token);

        case 2:
            if(isNumeric(token, 1)) {
                return setAmount(Integer.parseInt(token));
            }
            return false;

        case 3:
            return setUnit(token);

        case 4:
            if(isNumeric(token, 2)) {
             return setPrice(Double.parseDouble(token));
            }
            return false;

        case 5:
            if(isNumeric(token, 1)) {
                return setGroup(Integer.parseInt(token));
            }
            return false;
    }

    return false;
    }

    private static boolean isNumeric(String strNum, int c) {
        if (strNum.trim() == null) {
            return false;
        }
        try {
            if(c == 1) {
                int i = Integer.parseInt(strNum);
            }
            if(c == 2){
                double d = Double.parseDouble(strNum);
            }
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name + ";" + this.brand + ";" + this.amount + ";" +
                this.unit + ";" + this.price + ";" + this.group;
    }
}
