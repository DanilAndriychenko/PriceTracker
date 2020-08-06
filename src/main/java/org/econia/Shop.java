package org.econia;

public class Shop {

    private int shop_id;
    private String name;

    public Shop(int shop_id, String name) {
        this.shop_id= shop_id;
        this.name = name;
    }

    public int getShop_id() {
        return shop_id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Shop: [id: " + shop_id + ",\tname: " + name + "];";
    }
}
