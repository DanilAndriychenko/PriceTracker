package org.econia;

import java.sql.Date;

public class RecordAvailability {

    private int productId;
    private int subcatId;
    private int brandId;
    private int shopId;
    private Date date;
    private String availability;

    public RecordAvailability(int productId, int subcatId, int brandId, int shopId, Date date, String availability) {
        this.productId = productId;
        this.subcatId = subcatId;
        this.brandId = brandId;
        this.shopId = shopId;
        this.date = date;
        this.availability = availability;
    }

    public int getProductId() {
        return productId;
    }

    public Date getDate() {
        return date;
    }

    public int getSubcatId() {
        return subcatId;
    }

    public int getBrandId() {
        return brandId;
    }

    public int getShopId(){
        return shopId;
    }

    public String getAvailability() {
        return availability;
    }

    @Override
    public String toString() {
        return "RecordAvailability{" +
                "product_id=" + productId +
                ", subcatId=" + subcatId +
                ", brand_id=" + brandId +
                ", date=" + date +
                ", availability='" + availability + '\'' +
                '}';
    }
}
