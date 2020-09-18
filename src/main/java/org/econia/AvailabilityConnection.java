package org.econia;

public class AvailabilityConnection {

    private int brandId;
    private int subcatId;

    public AvailabilityConnection(int brandId, int subcatId) {
        this.brandId = brandId;
        this.subcatId = subcatId;
    }

    public int getBrandId() {
        return brandId;
    }

    public int getSubcatId() {
        return subcatId;
    }

    @Override
    public String toString() {
        return "AvailabilityConnection{" +
                "brandId=" + brandId +
                ", subcatId=" + subcatId +
                '}';
    }
}
