package org.econia;

import java.sql.Date;

public class RecordAvailability {

    private int product_id;
    private int subcatId;
    private int brand_id;
    private Date date;
    private String availability;

    public RecordAvailability(int product_id, int subcatId, int brand_id, Date date, String availability) {
        this.product_id = product_id;
        this.subcatId = subcatId;
        this.brand_id = brand_id;
        this.date = date;
        this.availability = availability;
    }

    public int getProduct_id() {
        return product_id;
    }

    public Date getDate() {
        return date;
    }

    public int getSubcatId() {
        return subcatId;
    }

    public int getBrand_id() {
        return brand_id;
    }

    public String getAvailability() {
        return availability;
    }

    @Override
    public String toString() {
        return "RecordAvailability{" +
                "product_id=" + product_id +
                ", subcatId=" + subcatId +
                ", brand_id=" + brand_id +
                ", date=" + date +
                ", availability='" + availability + '\'' +
                '}';
    }
}
