package org.econia;

public class Product {

    private int product_id, cat_id, brand_id, shop_id, region_id;
    private String link;

    public Product(int product_id, int cat_id, int brand_id, int shop_id, int region_id, String link) {
        this.product_id = product_id;
        this.cat_id = cat_id;
        this.brand_id = brand_id;
        this.shop_id = shop_id;
        this.region_id = region_id;
        this.link = link;
    }

    public int getProduct_id() {
        return product_id;
    }

    public int getCat_id() {
        return cat_id;
    }

    public int getBrand_id() {
        return brand_id;
    }

    public int getShop_id() {
        return shop_id;
    }

    public int getRegion_id() {
        return region_id;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return "Product: [product_id: " + product_id + ",\tcat_id: " + cat_id +
                ",\tbrand_id: " + brand_id + ",\tshop_id: " + shop_id + ",\tregion_id: " + region_id + ",\tlink: " + link + "];";
    }
}
