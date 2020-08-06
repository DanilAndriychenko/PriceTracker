package org.econia;

public class Region {

    private int region_id;
    private String name;

    public Region(int region_id, String name) {
        this.region_id = region_id;
        this.name = name;
    }

    public int getRegion_id() {
        return region_id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Region: [id: " + region_id + ",\tname: " + name + "];";
    }
}
