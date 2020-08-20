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

    private DBProcessor(){

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
             ResultSet resultSet = statement.executeQuery(query)){
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
        try(Formatter formatter = new Formatter(Locale.US)){
            query = formatter.format("INSERT INTO Records (product_id, date, price) VALUES (%d, '%s', %.2f);", productId, date.toString(), price).toString();
        }
//        System.out.println(query);
        if (price!=0.0) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(query);
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
        }
    }
}
