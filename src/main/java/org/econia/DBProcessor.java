package org.econia;

import com.mysql.cj.jdbc.Driver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class DBProcessor {

    private static final String USERNAME = "econiadj_pricemonitoring";
    private static final String PASSWORD = "PriceMonitoring";
    private static final String URL =
            "jdbc:mysql://54.36.173.35:3306/econiadj_pricemonitoring?useSSL=false&serverTimezone=UTC";

    public static String getUSERNAME() {
        return USERNAME;
    }

    public static String getPASSWORD() {
        return PASSWORD;
    }

    private static Connection connection;

    public static Connection getConnection() {
        return connection;
    }

    static {
        try {
            DriverManager.registerDriver(new Driver());
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private DBProcessor() {
    }

    public static List<Category> getAllCategories() {
        String query = "SELECT * FROM Category ORDER BY cat_id ASC;";
        ArrayList<Category> categoryArrayList = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                categoryArrayList.add(new Category(resultSet.getInt("cat_id"), resultSet.getString("name")));
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return categoryArrayList;
    }


    public static List<String> getCategoriesNames(List<Category> categoryArrayList) {
        ArrayList<String> categoriesNames = new ArrayList<>();
        for (Category category : categoryArrayList) {
            categoriesNames.add(category.getName());
        }
        return categoriesNames;
    }

    public static List<Brand> getAllBrands() {
        String query = "SELECT * FROM Brands ORDER BY brand_id ASC;";
        ArrayList<Brand> brandsArrayList = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                brandsArrayList.add(new Brand(resultSet.getInt("brand_id"), resultSet.getString("name")));
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return brandsArrayList;
    }

    public static List<String> getBrandsNames(List<Brand> brandArrayList) {
        ArrayList<String> brandsNames = new ArrayList<>();
        for (Brand brand : brandArrayList) {
            brandsNames.add(brand.getName());
        }
        return brandsNames;
    }

    public static List<Integer> getBrandsAccordingToCategory(int categoryID) {
        String query = "SELECT * FROM Connections WHERE cat_id = " + categoryID + " ORDER BY brand_id ASC;";
        ArrayList<Integer> brandsCat = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                brandsCat.add(resultSet.getInt("brand_id"));
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return brandsCat;
    }

    public static List<Shop> getAllShops() {
        String query = "SELECT * FROM Shops ORDER BY shop_id ASC;";
        ArrayList<Shop> shopsArrayList = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                shopsArrayList.add(new Shop(resultSet.getInt("shop_id"), resultSet.getString("name")));
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return shopsArrayList;
    }

    public static List<String> getShopsNames(List<Shop> shopArrayList) {
        ArrayList<String> shopsNames = new ArrayList<>();
        for (Shop shop : shopArrayList) {
            shopsNames.add(shop.getName());
        }
        return shopsNames;
    }

    public static List<Region> getAllRegions() {
        String query = "SELECT * FROM Regions ORDER BY region_id ASC;";
        ArrayList<Region> regionsArrayList = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                regionsArrayList.add(new Region(resultSet.getInt("region_id"), resultSet.getString("name")));
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return regionsArrayList;
    }

    public static List<String> getRegionsNames(List<Region> regionArrayList) {
        ArrayList<String> regionsNames = new ArrayList<>();
        for (Region region : regionArrayList) {
            regionsNames.add(region.getName());
        }
        return regionsNames;
    }

    public static List<Product> getProductsSetPartly(int beginProd, int endProd) {
        ArrayList<Product> products = new ArrayList<>();
        String query = String.format("SELECT * FROM Products WHERE product_id >= %d AND product_id <= %d ORDER BY product_id ASC;", beginProd, endProd);
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Product product = new Product(resultSet.getInt("product_id"), resultSet.getInt("cat_id"),
                        resultSet.getInt("brand_id"), resultSet.getInt("shop_id"),
                        resultSet.getInt("region_id"), resultSet.getString("link"));
                products.add(product);
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return products;
    }

    //TODO read about using batch
    //TODO Replace with prepared statement.
    public static void makeRecord(int productId, Date date, Double price) {
        String query;
        try (Formatter formatter = new Formatter(Locale.US)) {
            query = formatter.format("INSERT INTO Records (product_id, date, price) VALUES (%d, '%s', %.2f);", productId, date.toString(), price).toString();
        }
        System.out.println(query);
        if (price != 0.0) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(query);
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private static List<Integer> gaps(String date) {
        List<Integer> gaps = new ArrayList<>();
        List<Product> products = getProductsSetPartly(0, 1000);
        List<Integer> productIDs = new ArrayList<>();
        String query = String.format("SELECT * FROM Records WHERE date = '%s'", date);
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                productIDs.add(resultSet.getInt("product_id"));
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        for (Product product : products) {
            if (!productIDs.contains(product.getProduct_id())) gaps.add(product.getProduct_id());
        }
        return gaps;
    }

    public static AccessLevel getAccessLevel(String ip) {
        String query = String.format("SELECT * FROM Users WHERE ip = '%s'", ip);
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query);
             Statement statementInsert = connection.createStatement()) {
            if (resultSet.next()) {
                return new AccessLevel(resultSet.getBoolean("unlocked"), resultSet.getBoolean("add_SKU"),
                        resultSet.getBoolean("force_update"), resultSet.getBoolean("auto_update"), resultSet.getInt("period"));
            } else {
                String insertQuery = String.format("INSERT INTO Users (ip, unlocked, add_SKU, force_update, auto_update, period) VALUES ('%s', 0, 0, 0, 0, 0)", ip);
                statementInsert.execute(insertQuery);
                return new AccessLevel(false, false, false, false, 0);
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    public static void setAccessLevel(String ip, AccessLevel accessLevel){
        String query = String.format("UPDATE Users SET unlocked = %d, add_SKU = %d, force_update = %d, auto_update = %d, period = %d WHERE ip = '%s';",
                accessLevel.isUnlockToggleButtonEnabled() ? 1 : 0, accessLevel.isAddSKUToggleButtonEnabled() ? 1 : 0,
                accessLevel.isForceUpdateToggleButtonEnabled() ? 1 : 0, accessLevel.isAutoUpdateToggleButtonEnabled() ? 1 : 0,
                accessLevel.getPeriod(), ip);
        try(Statement statement = connection.createStatement()){
            statement.executeUpdate(query);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    public static class AccessLevel {

        private boolean unlockToggleButtonEnabled;
        private boolean addSKUToggleButtonEnabled;
        private boolean forceUpdateToggleButtonEnabled;
        private boolean autoUpdateToggleButtonEnabled;
        private int period;

        AccessLevel(boolean unlockToggleButtonEnabled, boolean addSKUToggleButtonEnabled, boolean forceUpdateToggleButtonEnabled,
                    boolean autoUpdateToggleButtonEnabled, int period) {
            this.unlockToggleButtonEnabled = unlockToggleButtonEnabled;
            this.addSKUToggleButtonEnabled = addSKUToggleButtonEnabled;
            this.forceUpdateToggleButtonEnabled = forceUpdateToggleButtonEnabled;
            this.autoUpdateToggleButtonEnabled = autoUpdateToggleButtonEnabled;
            this.period = period;
        }

        public boolean isUnlockToggleButtonEnabled() {
            return unlockToggleButtonEnabled;
        }

        public void setUnlockToggleButtonEnabled(boolean unlockToggleButtonEnabled) {
            this.unlockToggleButtonEnabled = unlockToggleButtonEnabled;
        }

        public boolean isAddSKUToggleButtonEnabled() {
            return addSKUToggleButtonEnabled;
        }

        public void setAddSKUToggleButtonEnabled(boolean addSKUToggleButtonEnabled) {
            this.addSKUToggleButtonEnabled = addSKUToggleButtonEnabled;
        }

        public boolean isForceUpdateToggleButtonEnabled() {
            return forceUpdateToggleButtonEnabled;
        }

        public void setForceUpdateToggleButtonEnabled(boolean forceUpdateToggleButtonEnabled) {
            this.forceUpdateToggleButtonEnabled = forceUpdateToggleButtonEnabled;
        }

        public boolean isAutoUpdateToggleButtonEnabled() {
            return autoUpdateToggleButtonEnabled;
        }

        public void setAutoUpdateToggleButtonEnabled(boolean autoUpdateToggleButtonEnabled) {
            this.autoUpdateToggleButtonEnabled = autoUpdateToggleButtonEnabled;
        }



        public int getPeriod() {
            return period;
        }

        public void setPeriod(int period) {
            this.period = period;
        }

        @Override
        public String toString() {
            return "ACCESS_LEVEL{" +
                    "unlockToggleButtonEnabled=" + unlockToggleButtonEnabled +
                    ", addSKUToggleButtonEnabled=" + addSKUToggleButtonEnabled +
                    ", forceUpdateToggleButtonEnabled=" + forceUpdateToggleButtonEnabled +
                    ", autoUpdateToggleButtonEnabled=" + autoUpdateToggleButtonEnabled +
                    ", period=" + period + '}';
        }
    }

    public static void main(String[] args) {
        List<Integer> gaps = gaps("2020-08-27");
        System.out.println(gaps.size());
        for (Integer integer : gaps){
            System.out.print(integer + ", ");
        }


        /*try {
            System.out.println(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }*/

//        System.out.println(LocalDate.now());
    }
}
