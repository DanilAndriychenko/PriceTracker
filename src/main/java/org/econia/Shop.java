package org.econia;

public class Shop {

    private int brand_id;
    private String name;

    public Shop(int brand_id, String name) {
        this.brand_id= brand_id;
        this.name = name;
    }

    public int getBrand_id() {
        return brand_id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Category: [id: " + brand_id + ",\tname: " + name + "];";
    }
}
