package org.econia;

import com.mysql.cj.jdbc.Driver;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBProcessor {

    private static final Logger DB_PROCESSOR_LOGGER = Logger.getLogger("DBProcessor Logger");

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
        setupConnection();
    }

    public static void setupConnection(){
        try {
            DriverManager.registerDriver(new Driver());
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void ping(){
        getAllShops();
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

    public static List<Category> getAllSubcategories(){
        String query = "SELECT * FROM Subcategory ORDER BY subcat_id ASC;";
        ArrayList<Category> subcategoryArrayList = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                subcategoryArrayList.add(new Category(resultSet.getInt("subcat_id"), resultSet.getString("name")));
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return subcategoryArrayList;
    }


    public static List<String> getCategoriesNames(List<Category> categoryArrayList) {
        ArrayList<String> categoriesNames = new ArrayList<>();
        for (Category category : categoryArrayList) {
            categoriesNames.add(category.getName());
        }
        return categoriesNames;
    }

    public static List<String> getSubcategoriesNames(List<Category> subcategoryArrayList){
        ArrayList<String> subcategoriesNames = new ArrayList<>();
        for (Category category : subcategoryArrayList) {
            subcategoriesNames.add(category.getName());
        }
        return subcategoriesNames;
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

    public static List<Brand> getOurBrands(List<Brand> brandsArrayList){
        ArrayList<Brand> ourBrands = new ArrayList<>();
        ourBrands.add(brandsArrayList.get(0));
        ourBrands.add(brandsArrayList.get(21));
        ourBrands.add(brandsArrayList.get(26));
        ourBrands.add(brandsArrayList.get(27));
        return ourBrands;
    }
    public static List<String> getOurBrandsNames(List<Brand> brandsArrayList){
        ArrayList<String> ourBrandsNames = new ArrayList<>();
        ourBrandsNames.add(brandsArrayList.get(0).getName());
        ourBrandsNames.add(brandsArrayList.get(21).getName());
        ourBrandsNames.add(brandsArrayList.get(26).getName());
        ourBrandsNames.add(brandsArrayList.get(27).getName());
        return ourBrandsNames;
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

    public static List<Integer> getSubcategoriesAccordingToBrand(int brandId){
        String query = "SELECT * FROM ConnectionsAvailability WHERE brand_id = " + brandId + " ORDER BY subcat_id ASC;";
        ArrayList<Integer> subcategoriesArrayList = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                subcategoriesArrayList.add(resultSet.getInt("subcat_id"));
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return subcategoriesArrayList;
    }

    public static int getBrandIdAccordingToSubcategory(int subcategoryId){
        String query = "SELECT * FROM ConnectionsAvailability WHERE subcat_id = " + subcategoryId + " ORDER BY brand_id ASC;";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            resultSet.next();
            return resultSet.getInt("brand_id");
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return -1;
    }

    public static List<AvailabilityConnection> getAvailabilityConnections(){
        String query = "SELECT * FROM ConnectionsAvailability ORDER BY brand_id ASC;";
        List<AvailabilityConnection> availabilityConnections = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()){
                availabilityConnections.add(new AvailabilityConnection(resultSet.getInt("brand_id"), resultSet.getInt("subcat_id")));
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return availabilityConnections;
    }

    public static List<String> getAllDistinctDatesInRange(String dateFrom, String dateTo){
        String query = String.format("SELECT DISTINCT RecordsAvailability.date FROM RecordsAvailability where date >= '%s' AND date <= '%s';",
                dateFrom, dateTo);
        ArrayList<String> dates = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                dates.add(resultSet.getDate("date").toString());
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return dates;
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

    public static List<Product> getProductsAvailabilitySetPartly(int beginProd, int endProd) {
        ArrayList<Product> products = new ArrayList<>();
        String query = String.format("SELECT * FROM ProductsAvailability WHERE product_id >= %d AND product_id <= %d ORDER BY product_id ASC;", beginProd, endProd);
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Product product = new Product(resultSet.getInt("product_id"), resultSet.getInt("subcat_id"),
                        resultSet.getInt("brand_id"), resultSet.getInt("shop_id"),
                        -1, resultSet.getString("link"));
                products.add(product);
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return products;
    }

    public static List<RecordAvailability> getRecordsAvailability(String fromDate, String toDate){
        ArrayList<RecordAvailability> recordAvailabilityArrayList = new ArrayList<>();
        String query = String.format("SELECT RecordsAvailability.product_id, ProductsAvailability.subcat_id, ProductsAvailability.brand_id, " +
                        "ProductsAvailability.shop_id, RecordsAvailability.date, RecordsAvailability.availability FROM RecordsAvailability " +
                        "INNER JOIN ProductsAvailability ON RecordsAvailability.product_id=ProductsAvailability.product_id " +
                        "WHERE date >= '%s' AND date <= '%s';",
                fromDate, toDate);
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                RecordAvailability recordAvailability = new RecordAvailability(resultSet.getInt("product_id"),
                        resultSet.getInt("subcat_id"), resultSet.getInt("brand_id"),
                        resultSet.getInt("shop_id"), resultSet.getDate("date"),
                        resultSet.getString("availability"));
                recordAvailabilityArrayList.add(recordAvailability);
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return recordAvailabilityArrayList;
    }

    //TODO read about using batch
    //TODO Replace with prepared statement.
    public static void makeRecord(int productId, Date date, Double price) {
        String query;
        try (Formatter formatter = new Formatter(Locale.US)) {
            query = formatter.format("INSERT INTO Records (product_id, date, price) VALUES (%d, '%s', %.2f);", productId, date.toString(), price).toString();
        }
        if (price != 0.0) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(query);
            } catch (SQLException throwable) {
                DB_PROCESSOR_LOGGER.log(Level.SEVERE, throwable.getMessage());
            }
        }
    }

    public static void makeRecordAvailability(int productId, Date date, String availability) {
        String query = String.format("INSERT INTO RecordsAvailability (product_id, date, availability) VALUES (%d, '%s', '%s');",
                productId, date.toString(), availability);
        if (!availability.equals("")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(query);
            } catch (SQLException throwable) {
                DB_PROCESSOR_LOGGER.log(Level.SEVERE, throwable.getMessage());
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

    /*public static void main(String[] args) {
        *//*List<Integer> gaps = gaps("2020-09-03");
        System.out.println(gaps.size());
        for (Integer integer : gaps){
            System.out.print(integer + ", ");
        }


        try {
            System.out.println(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }*//*


//        System.out.println(LocalDate.now());
    }*/
}
