package org.econia;

import com.mysql.cj.jdbc.Driver;

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBProcessor {

    private static final String USERNAME = "econiadj_pricemonitoring";
    private static final String PASSWORD = "PriceMonitoring";
    private static final String URL =
            "jdbc:mysql://54.36.173.35:3306/econiadj_pricemonitoring?useSSL=false&serverTimezone=UTC";

    private static Logger logger = Logger.getLogger(DBProcessor.class.getName());

    public static Connection connection;

    static {
        try {
            DriverManager.registerDriver(new Driver());
        } catch (SQLException e) {
            String msg = "Error occurred in attempt to register driver.\n" + e.toString();
            logger.log(Level.SEVERE, msg);
        }
        setupConnection(URL, USERNAME, PASSWORD);
    }

    private static Connection setupConnection(String url, String username, String password) {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                String msg = "Error occurred in attempt to get connection.\n" + e.toString();
                logger.log(Level.SEVERE, msg);
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


    public static ArrayList<String> getCategoriesNames(ArrayList<Category> categoryArrayList) {
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

    public static ArrayList<String> getBrandsNames(ArrayList<Brand> brandArrayList) {
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

    public static ArrayList<String> getShopsNames(ArrayList<Shop> shopArrayList) {
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

    public static ArrayList<String> getRegionsNames(ArrayList<Region> regionArrayList) {
        ArrayList<String> regionsNames = new ArrayList<>();
        for (Region region : regionArrayList) {
            regionsNames.add(region.getName());
        }
        return regionsNames;
    }
}
