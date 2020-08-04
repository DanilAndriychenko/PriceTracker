package org.econia;

public class Category {

    private int cat_id;
    private String name;

    public Category(int cat_id, String name) {
        this.cat_id = cat_id;
        this.name = name;
    }

    public int getCat_id() {
        return cat_id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Category: [id: " + cat_id + ",\tname: " + name + "];";
    }
}
