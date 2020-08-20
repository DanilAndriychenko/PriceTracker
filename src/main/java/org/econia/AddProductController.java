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

public class AddProductController {

    private static final String COMBO_BOX_PROMPT_TEXT = "Не вказано";
    private static final Paint BLACK = Paint.valueOf("black");
    private static final Paint RED = Paint.valueOf("red");

    private static final UrlValidator URL_VALIDATOR = new UrlValidator();

    @FXML
    public JFXComboBox<String> comboBoxCategory;
    @FXML
    public JFXComboBox<String> comboBoxBrand;
    @FXML
    public JFXComboBox<String> comboBoxShop;
    @FXML
    public JFXComboBox<String> comboBoxRegion;
    @FXML
    public JFXTextField textFieldLink;
    @FXML
    public JFXButton buttonAddProduct;
    @FXML
    public Label labelStatus;

    @FXML
    public void initialize() {
        //blockAllComboBoxes while user don't choose Category
        //set default values for comboBoxes
        comboBoxBrand.setDisable(true);
        comboBoxShop.setDisable(true);
        comboBoxRegion.setDisable(true);
        buttonAddProduct.setDisable(true);
        comboBoxCategory.setPromptText(COMBO_BOX_PROMPT_TEXT);
        comboBoxBrand.setPromptText(COMBO_BOX_PROMPT_TEXT);
        comboBoxShop.setPromptText(COMBO_BOX_PROMPT_TEXT);
        comboBoxRegion.setPromptText(COMBO_BOX_PROMPT_TEXT);

        comboBoxCategory.getItems().setAll(Controller.getCategoriesNames());

        comboBoxCategory.setOnAction(eventCategoryAction -> {
            comboBoxBrand.setDisable(false);

            //Get category and get brands that compete in this category.
            String category = comboBoxCategory.getValue();
            int categoryID = Controller.getCategoriesNames().indexOf(category) + 1;
            List<Integer> brandsIDs = DBProcessor.getBrandsAccordingToCategory(categoryID);

            //Clear checkBoxBrand and set those brands there
            //Also set value of previous brand if new list of brands contains it.
            String brandPrev = comboBoxBrand.getValue();
            comboBoxBrand.getItems().clear();
            for (Brand brand : Controller.getBrandArrayList()) {
                if (brandsIDs.contains(brand.getBrand_id())) {
                    comboBoxBrand.getItems().add(brand.getName());
                    if (brand.getName().equals(brandPrev)) {
                        comboBoxBrand.setValue(brandPrev);
                    }
                }
            }
            comboBoxBrand.setPromptText(COMBO_BOX_PROMPT_TEXT);

            comboBoxBrand.setOnAction(eventBrandAction -> {
                comboBoxShop.setDisable(false);
                comboBoxShop.getItems().setAll(Controller.getShopsNames());

                comboBoxShop.setOnAction(eventShopAction -> {
                    comboBoxRegion.setDisable(false);
                    comboBoxRegion.getItems().setAll(Controller.getRegionsNames());

                    comboBoxRegion.setOnAction(eventRegionAction -> buttonAddProduct.setDisable(false));
                });
            });
        });

        buttonAddProduct.setOnAction(eventButton -> {
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

                int catId = Controller.getCategoriesNames().indexOf(comboBoxCategory.getValue()) + 1;
                int brandId = Controller.getBrandsNames().indexOf(comboBoxBrand.getValue()) + 1;
                int shopId = Controller.getShopsNames().indexOf(comboBoxShop.getValue()) + 1;
                int regionId = Controller.getRegionsNames().indexOf(comboBoxRegion.getValue()) + 1;
                String link = textFieldLink.getText();

                //check if record for this product already exists.
                //If exist --> update link, else insert as new Product.
                String querySelect = String.format("SELECT * FROM Products WHERE cat_id = %d AND brand_id = %d AND shop_id = %d AND region_id = %d",
                        catId, brandId, shopId, regionId);
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
                            catId, brandId, shopId, regionId, link);
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
