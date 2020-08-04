package org.econia;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * The type Controller.
 */
public class Controller {

    private static final String USERNAME = "econiadj_pricemonitoring";
    private static final String PASSWORD = "PriceMonitoring";
    private static final String URL =
            "jdbc:mysql://54.36.173.35:3306/econiadj_pricemonitoring?useSSL=false&serverTimezone=UTC";

    private static DBProcessor dbProcessor;
    private static Connection connection;

    private static final String comboBoxCategoryPromptText = "Оберіть категорію";
    private static final String brandsVBoxEmpty = "Спочатку оберіть категорію ↑";
    private static final String brandsChooseAll = "Обрати всі / Скасувати";

    private static ArrayList<Category> categoryArrayList;
    private static ArrayList<String> categoriesNames;
    private static ArrayList<Brand> brandArrayList;
    private static ArrayList<String> brandsNames;
    private static ArrayList<JFXCheckBox> checkBoxesBrands;

    @FXML
    public JFXComboBox<String> comboBoxCategory;
    @FXML
    public VBox vBoxBrands;
    @FXML
    public JFXComboBox<String> comboBoxShop;
    @FXML
    public JFXComboBox<String> comboBoxRegion;
    @FXML
    public JFXDatePicker datePickerFrom;
    @FXML
    public JFXDatePicker datePickerTo;
    @FXML
    public JFXButton buttonTrack;

    /**
     * Initialize.
     */
    @FXML
    public void initialize() {
        //setup Connection with MySQL database
        setupConnection();

        //blockAllComboBoxes while user don't choose Category
        comboBoxShop.setDisable(true);
        vBoxBrands.getChildren().add(new Label(brandsVBoxEmpty));
        comboBoxRegion.setDisable(true);

        //initialize Lists
        categoryArrayList = getCategories();
        categoriesNames = getCategoriesNames();
        brandArrayList = getBrands();
        brandsNames = getBrandsNames();
        checkBoxesBrands = new ArrayList<>();

        //TODO set default values for all comboBoxes
        ObservableList<String> stringObservableList = FXCollections.observableArrayList(categoriesNames);
        comboBoxCategory.getItems().setAll(stringObservableList);
        comboBoxCategory.setPromptText(comboBoxCategoryPromptText);
        comboBoxCategory.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //unblock jfxComboBox with shops.
                comboBoxShop.setDisable(false);

                String category = comboBoxCategory.getValue();
                int categoryID = categoriesNames.indexOf(category)+1;
                ArrayList<Integer> brandsIDs = getBrandsAccordingToCategory(categoryID);
                vBoxBrands.getChildren().clear();
                JFXCheckBox jfxCheckBoxAll = new JFXCheckBox(brandsChooseAll);
                jfxCheckBoxAll.setPadding(new Insets(10, 0, 0, 0));
                jfxCheckBoxAll.setSelected(false);
                jfxCheckBoxAll.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (jfxCheckBoxAll.isSelected()){
                            for (JFXCheckBox jfxCheckBox : checkBoxesBrands){
                                jfxCheckBox.setSelected(true);
                            }
                        } else {
                            for (JFXCheckBox jfxCheckBox : checkBoxesBrands){
                                jfxCheckBox.setSelected(false);
                            }
                        }
                    }
                });
                vBoxBrands.getChildren().add(jfxCheckBoxAll);
                for (Brand brand : brandArrayList){
                    if (brandsIDs.contains(brand.getBrand_id())){
                        JFXCheckBox jfxCheckBox = new JFXCheckBox(brand.getName());
                        jfxCheckBox.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                //TODO
                            }
                        });
                        jfxCheckBox.setPadding(new Insets(10, 0, 0, 0));
                        checkBoxesBrands.add(jfxCheckBox);
                        vBoxBrands.getChildren().add(jfxCheckBox);
                    }
                }
            }
        });
    }

    private void setupConnection(){
        dbProcessor = new DBProcessor();
        connection = dbProcessor.getConnection(URL, USERNAME, PASSWORD);
    }

    private static ArrayList<Category> getCategories(){
        String query = "SELECT * FROM Category;";
        ArrayList<Category> categoryArrayList = new ArrayList<>();
        try (Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query)){
            while (resultSet.next()){
                categoryArrayList.add(new Category(resultSet.getInt("cat_id"), resultSet.getString("name")));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return categoryArrayList;
    }

    private static ArrayList<String> getCategoriesNames(){
        ArrayList<String> categoriesNames = new ArrayList<>();
        for (Category category : categoryArrayList){
            categoriesNames.add(category.getName());
        }
        return categoriesNames;
    }

    private static ArrayList<Brand> getBrands(){
        String query = "SELECT * FROM Brands;";
        ArrayList<Brand> brandsArrayList = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)){
            while (resultSet.next()){
                brandsArrayList.add(new Brand(resultSet.getInt("brand_id"), resultSet.getString("name")));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return brandsArrayList;
    }

    private static ArrayList<String> getBrandsNames(){
        ArrayList<String> brandsNames = new ArrayList<>();
        for (Brand brand : brandArrayList){
            brandsNames.add(brand.getName());
        }
        return brandsNames;
    }

    private static ArrayList<Integer> getBrandsAccordingToCategory(int categoryID){
        String query = "SELECT * FROM Connections WHERE cat_id = " + categoryID + ";";
        ArrayList<Integer> brandsCat = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)){
            while (resultSet.next()){
                brandsCat.add(resultSet.getInt("brand_id"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return brandsCat;
    }

//    private static ArrayList<Shop> getShops(){
//        String query = "SELECT * FROM Brands;";
//        ArrayList<Brand> brandsArrayList = new ArrayList<>();
//        try (Statement statement = connection.createStatement();
//             ResultSet resultSet = statement.executeQuery(query)){
//            while (resultSet.next()){
//                brandsArrayList.add(new Brand(resultSet.getInt("brand_id"), resultSet.getString("name")));
//            }
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//        }
//        return brandsArrayList;
//    }


}

