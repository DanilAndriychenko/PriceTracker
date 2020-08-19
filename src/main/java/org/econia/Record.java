package org.econia;

import java.sql.Date;

public class Record {

    private int product_id;
    private Date date;
    private Double price;

    public Record(int product_id, Date date, Double price) {
        this.product_id = product_id;
        this.date = date;
        this.price = price;
    }

    public int getProduct_id() {
        return product_id;
    }

    public Date getDate() {
        return date;
    }

    public Double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Record: [product_id: " + product_id + ",\tdate: " + date + ",\tprice: " + price + "];";
    }
}
