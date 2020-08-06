package org.econia;

public class Brand {

    private int brand_id;
    private String name;

    public Brand(int brand_id, String name) {
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
        return "Brand: [id: " + brand_id + ",\tname: " + name + "];";
    }
}
