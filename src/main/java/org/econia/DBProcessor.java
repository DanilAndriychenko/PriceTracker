package org.econia;

import com.mysql.cj.jdbc.Driver;

import java.sql.*;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class DBProcessor {

    private static final String USERNAME = "econiadj_pricemonitoring";
    private static final String PASSWORD = "PriceMonitoring";
    private static final String URL =
            "jdbc:mysql://54.36.173.35:3306/econiadj_pricemonitoring?useSSL=false&serverTimezone=UTC";

    public static Connection connection;
    private static final Formatter FORMATTER_US = new Formatter(Locale.US);

    static {
        try {
            DriverManager.registerDriver(new Driver());
        } catch (SQLException e) {
            String msg = "Error occurred in attempt to register driver.\n" + e.toString();
        }
        setupConnection(URL, USERNAME, PASSWORD);
    }

    private static Connection setupConnection(String url, String username, String password) {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                String msg = "Error occurred in attempt to get connection.\n" + e.toString();
            }
        }
        return connection;
    }

    public static ArrayList<Category> getAllCategories() {
        String query = "SELECT * FROM Category ORDER BY cat_id ASC;";
        ArrayList<Category> categoryArrayList = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                categoryArrayList.add(new Category(resultSet.getInt("cat_id"), resultSet.getString("name")));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return categoryArrayList;
    }


    public static ArrayList<String> getCategoriesNames(List<Category> categoryArrayList) {
        ArrayList<String> categoriesNames = new ArrayList<>();
        for (Category category : categoryArrayList) {
            categoriesNames.add(category.getName());
        }
        return categoriesNames;
    }

    public static ArrayList<Brand> getAllBrands() {
        String query = "SELECT * FROM Brands ORDER BY brand_id ASC;";
        ArrayList<Brand> brandsArrayList = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                brandsArrayList.add(new Brand(resultSet.getInt("brand_id"), resultSet.getString("name")));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return brandsArrayList;
    }

    public static ArrayList<String> getBrandsNames(List<Brand> brandArrayList) {
        ArrayList<String> brandsNames = new ArrayList<>();
        for (Brand brand : brandArrayList) {
            brandsNames.add(brand.getName());
        }
        return brandsNames;
    }

    public static ArrayList<Integer> getBrandsAccordingToCategory(int categoryID) {
        String query = "SELECT * FROM Connections WHERE cat_id = " + categoryID + " ORDER BY brand_id ASC;";
        ArrayList<Integer> brandsCat = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                brandsCat.add(resultSet.getInt("brand_id"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return brandsCat;
    }

    public static ArrayList<Shop> getAllShops() {
        String query = "SELECT * FROM Shops ORDER BY shop_id ASC;";
        ArrayList<Shop> shopsArrayList = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                shopsArrayList.add(new Shop(resultSet.getInt("shop_id"), resultSet.getString("name")));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return shopsArrayList;
    }

    public static ArrayList<String> getShopsNames(List<Shop> shopArrayList) {
        ArrayList<String> shopsNames = new ArrayList<>();
        for (Shop shop : shopArrayList) {
            shopsNames.add(shop.getName());
        }
        return shopsNames;
    }

    public static ArrayList<Region> getAllRegions() {
        String query = "SELECT * FROM Regions ORDER BY region_id ASC;";
        ArrayList<Region> regionsArrayList = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                regionsArrayList.add(new Region(resultSet.getInt("region_id"), resultSet.getString("name")));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return regionsArrayList;
    }

    public static ArrayList<String> getRegionsNames(List<Region> regionArrayList) {
        ArrayList<String> regionsNames = new ArrayList<>();
        for (Region region : regionArrayList) {
            regionsNames.add(region.getName());
        }
        return regionsNames;
    }

    public static ResultSet getProductsSetPartly(int beginProd, int endProd){
        String query = String.format("SELECT product_id, shop_id, link FROM Products WHERE product_id >= %d AND product_id <= %d ORDER BY product_id ASC;", beginProd, endProd);
        ResultSet resultSet = null;
        Statement statement;
        try{
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return resultSet;
    }

    //TODO read about using batch
    //TODO Replace with prepared statement.
    public static void makeRecord(int productId, Date date, Double price){
        String query = new Formatter(Locale.US).format("INSERT INTO Records (product_id, date, price) VALUES (%d, '%s', %.2f);", productId, date.toString(), price).toString();
        System.out.println(query);
        try (Statement statement = connection.createStatement()){
            statement.execute(query);
        }catch(SQLException throwables){
            throwables.printStackTrace();
        }
    }
}
