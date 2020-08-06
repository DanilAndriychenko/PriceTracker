package org.econia;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The type Controller.
 */
public class Controller {

    private static final String COMBO_BOX_CATEGORY_PROMPT_TEXT = "Оберіть категорію";
    private static final String COMBO_BOX_SHOP_PROMPT_TEXT = "Оберіть магазин";
    private static final String COMBO_BOX_REGION_PROMPT_TEXT = "Оберіть регіон";
    private static final String BRANDS_CHOOSE_CATEGORY = "Спочатку оберіть категорію ↑";
    private static final String BRANDS_CHOOSE_ALL = "Обрати всі / Скасувати";

    public static ArrayList<Category> categoryArrayList;
    public static ArrayList<String> categoriesNames;
    public static ArrayList<Brand> brandArrayList;
    public static ArrayList<String> brandsNames;
    public static ArrayList<JFXCheckBox> checkBoxesBrands;
    public static ArrayList<Shop> shopArrayList;
    public static ArrayList<String> shopsNames;
    public static ArrayList<Region> regionArrayList;
    public static ArrayList<String> regionsNames;

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
    @FXML
    public MenuItem addProduct;

    static {
        categoryArrayList = DBProcessor.getAllCategories();
        categoriesNames = DBProcessor.getCategoriesNames(categoryArrayList);
        brandArrayList = DBProcessor.getAllBrands();
        brandsNames = DBProcessor.getBrandsNames(brandArrayList);
        checkBoxesBrands = new ArrayList<>();
        shopArrayList = DBProcessor.getAllShops();
        shopsNames = DBProcessor.getShopsNames(shopArrayList);
        regionArrayList = DBProcessor.getAllRegions();
        regionsNames = DBProcessor.getRegionsNames(regionArrayList);
    }

    /**
     * Initialize.
     */
    @FXML
    public void initialize() {
        //initialize DBProcessor and Lists


        //blockAllComboBoxes while user don't choose Category
        //set default values for comboBoxes
        comboBoxShop.setDisable(true);
        vBoxBrands.getChildren().add(new Label(BRANDS_CHOOSE_CATEGORY));
        comboBoxRegion.setDisable(true);
        buttonTrack.setDisable(true);
        datePickerFrom.setDisable(true);
        datePickerTo.setDisable(true);
        comboBoxCategory.getItems().setAll(categoriesNames);
        comboBoxCategory.setPromptText(COMBO_BOX_CATEGORY_PROMPT_TEXT);
        comboBoxShop.setPromptText(COMBO_BOX_SHOP_PROMPT_TEXT);
        comboBoxRegion.setPromptText(COMBO_BOX_REGION_PROMPT_TEXT);

        //Category <---> SetOnAction
        comboBoxCategory.setOnAction(eventCategoryAction -> {
            //Unlock Shops ComboBox and load available shops.
            comboBoxShop.setDisable(false);
            comboBoxShop.getItems().setAll(shopsNames);
            comboBoxShop.setOnAction(eventShopAction -> {
                comboBoxRegion.setDisable(false);
                comboBoxRegion.getItems().setAll(regionsNames);
                comboBoxRegion.setOnAction(eventRegionAction -> {
                    datePickerFrom.setDisable(false);
                    datePickerTo.setDisable(false);
                    datePickerFrom.setOnAction(eventDateFrom -> {
                        if (allDataSelected()) {
                            buttonTrack.setDisable(false);
                        }
                    });
                    datePickerTo.setOnAction(eventDateTo -> {
                        if (allDataSelected()) {
                            buttonTrack.setDisable(false);
                        }
                    });
                });
            });

            //Get category and get brands that compete in this category.
            String category = comboBoxCategory.getValue();
            int categoryID = categoriesNames.indexOf(category) + 1;
            ArrayList<Integer> brandsIDs = DBProcessor.getBrandsAccordingToCategory(categoryID);

            //Create Choose All functionality.
            vBoxBrands.getChildren().clear();
            checkBoxesBrands.clear();
            buttonTrack.setDisable(true);
            JFXCheckBox jfxCheckBoxAll = new JFXCheckBox(BRANDS_CHOOSE_ALL);
            jfxCheckBoxAll.setPadding(new Insets(10, 0, 0, 0));
            jfxCheckBoxAll.setOnAction(eventChooseAll -> {
                if (jfxCheckBoxAll.isSelected()) {
                    for (JFXCheckBox jfxCheckBox : checkBoxesBrands) {
                        jfxCheckBox.setSelected(true);
                    }
                    if (allDataSelected()) {
                        buttonTrack.setDisable(false);
                    } else if (noOneBrandIsSelected()) {
                        buttonTrack.setDisable(true);
                    }
                } else {
                    for (JFXCheckBox jfxCheckBox : checkBoxesBrands) {
                        jfxCheckBox.setSelected(false);
                    }
                    if (allDataSelected()) {
                        buttonTrack.setDisable(false);
                    } else if (noOneBrandIsSelected()) {
                        buttonTrack.setDisable(true);
                    }
                }

            });
            vBoxBrands.getChildren().add(jfxCheckBoxAll);

            //Add all brands that compete in this category.
            for (Brand brand : brandArrayList) {
                if (brandsIDs.contains(brand.getBrand_id())) {
                    JFXCheckBox jfxCheckBox = new JFXCheckBox(brand.getName());
                    jfxCheckBox.setPadding(new Insets(10, 0, 0, 0));
                    jfxCheckBox.setOnAction(eventCheckBoxSelected -> {
                        if (allDataSelected()) {
                            buttonTrack.setDisable(false);
                        } else if (noOneBrandIsSelected()) {
                            buttonTrack.setDisable(true);
                        }
                    });
                    checkBoxesBrands.add(jfxCheckBox);
                    vBoxBrands.getChildren().add(jfxCheckBox);
                }
            }
        });

        addProduct.setOnAction(eventAddProduct -> {
            Parent root;
            try {
                root = FXMLLoader.load(getClass().getResource("addProduct.fxml"));
                Stage stage = new Stage();
                stage.setTitle("Додати продукцію");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean allDataSelected() {
        if (datePickerFrom.getValue() != null && datePickerTo.getValue() != null) {
            for (JFXCheckBox checkBox : checkBoxesBrands) {
                if (checkBox.isSelected()) return true;
            }
        }
        return false;
    }

    private boolean noOneBrandIsSelected() {
        for (JFXCheckBox checkBox : checkBoxesBrands) {
            if (checkBox.isSelected()) return false;
        }
        return true;
    }
}