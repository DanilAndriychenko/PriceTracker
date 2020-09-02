package org.econia;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;
import org.apache.commons.validator.routines.UrlValidator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AddAvailabilityController {

    private static final String COMBO_BOX_PROMPT_TEXT = "Не вказано";
    private static final Paint BLACK = Paint.valueOf("black");
    private static final Paint RED = Paint.valueOf("red");

    private static final UrlValidator URL_VALIDATOR = new UrlValidator();

    @FXML
    public JFXComboBox<String> comboBoxSubcategory;
    @FXML
    public JFXComboBox<String> comboBoxBrand;
    @FXML
    public JFXComboBox<String> comboBoxShop;
    @FXML
    public JFXTextField textFieldLink;
    @FXML
    public JFXButton buttonAdd;
    @FXML
    public Label labelStatus;

    @FXML
    public void initialize() {
        //blockAllComboBoxes while user don't choose Category
        //set default values for comboBoxes
        comboBoxSubcategory.setDisable(true);
        comboBoxShop.setDisable(true);
        buttonAdd.setDisable(true);
        comboBoxSubcategory.setPromptText(COMBO_BOX_PROMPT_TEXT);
        comboBoxBrand.setPromptText(COMBO_BOX_PROMPT_TEXT);
        comboBoxShop.setPromptText(COMBO_BOX_PROMPT_TEXT);

        comboBoxBrand.getItems().setAll(App.getController().getOurBrandsNames());

        comboBoxBrand.setOnAction(eventCategoryAction -> {
            comboBoxSubcategory.setDisable(false);
//            Get brand from comboBox and get subcategories that this brand has.
            String brand = comboBoxBrand.getValue();
            int brandId = App.getController().getBrandsNames().indexOf(brand) + 1;
            List<Integer> subcategoriesIds = DBProcessor.getSubcategoriesAccordingToBrand(brandId);
            //Clear checkBoxBrand and set those brands there
            comboBoxSubcategory.getItems().clear();
            for (Category subcategory : App.getController().getSubcategoryArrayList()) {
                if (subcategoriesIds.contains(subcategory.getCat_id())) {
                    comboBoxSubcategory.getItems().add(subcategory.getName());
                }
            }
            comboBoxSubcategory.setPromptText(COMBO_BOX_PROMPT_TEXT);
        });

        buttonAdd.setOnAction(eventButton -> {
            if (!dataSelectedCorrectly()) {
                if (!URL_VALIDATOR.isValid(textFieldLink.getText())) {
                    textFieldLink.setUnFocusColor(RED);
                } else {
                    textFieldLink.setUnFocusColor(BLACK);
                }
                if (comboBoxBrand.getValue() == null) {
                    comboBoxBrand.setUnFocusColor(RED);
                } else {
                    comboBoxBrand.setUnFocusColor(BLACK);
                }
            } else {
                //change color on black if successful
                comboBoxBrand.setUnFocusColor(BLACK);
                textFieldLink.setUnFocusColor(BLACK);

                int catId = App.getController().getCategoriesNames().indexOf(comboBoxSubcategory.getValue()) + 1;
                int brandId = App.getController().getBrandsNames().indexOf(comboBoxBrand.getValue()) + 1;
                int shopId = App.getController().getShopsNames().indexOf(comboBoxShop.getValue()) + 1;
                String link = textFieldLink.getText();

                //check if record for this product already exists.
                //If exist --> update link, else insert as new Product.
                String querySelect = String.format("SELECT * FROM Products WHERE cat_id = %d AND brand_id = %d AND shop_id = %d AND region_id = %d",
                        catId, brandId, shopId);
                int productIDSelected = getProductID(querySelect);
                if (productIDSelected != -1) {
                    String queryUpdate = String.format("UPDATE Products SET link = '%s' WHERE product_id = %d;", link, productIDSelected);
                    try {
                        DBProcessor.getConnection().createStatement().executeUpdate(queryUpdate);
                    } catch (SQLException throwable) {
                        throwable.printStackTrace();
                    }
                } else {
                    String queryInsert = String.format("INSERT INTO Products (cat_id, brand_id, shop_id, region_id, link) VALUES (%d, %d, %d, %d, '%s');",
                            catId, brandId, shopId, link);
                    try {
                        DBProcessor.getConnection().createStatement().executeUpdate(queryInsert);
                    } catch (SQLException throwable) {
                        throwable.printStackTrace();
                    }
                }
                new Thread(() -> {
                    Platform.runLater(() -> {
                        labelStatus.setText("Продукт додано.");
                        labelStatus.setTextFill(Paint.valueOf("green"));
                    });
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    Platform.runLater(() -> labelStatus.setText(""));
                }).start();
                textFieldLink.clear();
            }
        });
    }

    private int getProductID(String query) {
        try (ResultSet resultSet = DBProcessor.getConnection().createStatement().executeQuery(query)) {
            if (resultSet.isBeforeFirst()) {
                resultSet.next();
                return resultSet.getInt("product_id");
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return -1;
    }

    private boolean dataSelectedCorrectly() {
        return new UrlValidator().isValid(textFieldLink.getText()) && comboBoxBrand.getValue() != null;
    }
}
