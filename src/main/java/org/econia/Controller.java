package org.econia;

import com.jfoenix.controls.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Controller.
 */
public class Controller {

    private static final String COMBO_BOX_CATEGORY_PROMPT_TEXT = "Оберіть категорію";
    private static final String COMBO_BOX_SHOP_PROMPT_TEXT = "Оберіть магазин";
    private static final String COMBO_BOX_REGION_PROMPT_TEXT = "Оберіть регіон";
    private static final String BRANDS_CHOOSE_CATEGORY = "Спочатку оберіть категорію ↑";
    private static final String BRANDS_CHOOSE_ALL = "Обрати всі / Скасувати";
    private static final double FREQUENCY = 10_000.0;
    private static final String APP_ICON = "appIcon1.jpg";
    private ScrapProcessor scrapProcessor;

    private static List<Category> categoryArrayList;
    private static List<String> categoriesNames;
    private static List<Brand> brandArrayList;
    private static List<String> brandsNames;
    private static List<JFXCheckBox> checkBoxesBrands;
    private static List<Shop> shopArrayList;
    private static List<String> shopsNames;
    private static List<Region> regionArrayList;
    private static List<String> regionsNames;

    public static List<Category> getCategoryArrayList() {
        return categoryArrayList;
    }

    public static List<String> getCategoriesNames() {
        return categoriesNames;
    }

    public static List<Brand> getBrandArrayList() {
        return brandArrayList;
    }

    public static List<String> getBrandsNames() {
        return brandsNames;
    }

    public static List<JFXCheckBox> getCheckBoxesBrands() {
        return checkBoxesBrands;
    }

    public static List<Shop> getShopArrayList() {
        return shopArrayList;
    }

    public static List<String> getShopsNames() {
        return shopsNames;
    }

    public static List<Region> getRegionArrayList() {
        return regionArrayList;
    }

    public static List<String> getRegionsNames() {
        return regionsNames;
    }

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
    @FXML
    public Label timeLabel;
    @FXML
    public JFXProgressBar progressBar;
    @FXML
    public JFXButton loadButton;
    @FXML
    public BorderPane borderPane;
    @FXML
    public BorderPane borderPaneBottom;
    @FXML
    public ScrollPane scrollPane;

    private LineChart<Number, Number> lineChart;
    private final NumberAxis dateAxis = new NumberAxis();
    private final NumberAxis priceAxis = new NumberAxis();

    private static void initializeLists() {
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

    private void configurePanel() {
        comboBoxShop.setDisable(true);
        comboBoxRegion.setDisable(true);
        buttonTrack.setDisable(true);
        datePickerFrom.setDisable(true);
        datePickerTo.setDisable(true);
        comboBoxCategory.getItems().setAll(categoriesNames);
        comboBoxCategory.setPromptText(COMBO_BOX_CATEGORY_PROMPT_TEXT);
        comboBoxShop.setPromptText(COMBO_BOX_SHOP_PROMPT_TEXT);
        comboBoxRegion.setPromptText(COMBO_BOX_REGION_PROMPT_TEXT);
        vBoxBrands.getChildren().add(new Label(BRANDS_CHOOSE_CATEGORY));
    }

    private void configureLoadButton() {
        loadButton.setDisable(true);
        timeLabel.setVisible(false);
        progressBar.setVisible(false);

        Task<Void> taskCreateScrapeProcessorObject = new Task<Void>() {
            @Override
            protected Void call() {
                scrapProcessor = new ScrapProcessor();
                loadButton.setDisable(false);
                return null;
            }
        };
        new Thread(taskCreateScrapeProcessorObject).start();

        loadButton.setOnAction(loadButtonAction -> new Thread(() -> {
            Platform.runLater(() -> {
                loadButton.setDisable(true);
                timeLabel.setVisible(true);
                progressBar.setProgress(0);
                progressBar.setVisible(true);
            });
            long startTime = System.currentTimeMillis();
            Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
                long diff = System.currentTimeMillis() - startTime;
                long minutes = diff / 1_000 / 60;
                long seconds = diff / 1_000 - minutes * 60;
                timeLabel.setText("Час завантаження: " + String.format("%02d:%02d", minutes, seconds));
                progressBar.setProgress(scrapProcessor.getProgress());
            }), new KeyFrame(Duration.seconds(1)));
            clock.setCycleCount(Animation.INDEFINITE);
            Task<Void> loadPrices = new Task<Void>() {
                @Override
                protected Void call() {
                    clock.play();
                    scrapProcessor.scrapePricesInRange(0, 1000);
                    clock.stop();
                    return null;
                }
            };
            loadPrices.run();
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                timeLabel.setText("Ціни на продукцію успішно завантажені.");
                timeLabel.setTextFill(Paint.valueOf("green"));
                timeLabel.setFont(new Font("System Bold Italic", 20));
            });
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(() -> {
                timeLabel.setText("");
                timeLabel.setTextFill(Paint.valueOf("black"));
                timeLabel.setFont(new Font("System Italic", 18));
                timeLabel.setVisible(false);
                loadButton.setDisable(false);
            });
        }).start());
    }

    /**
     * Initialize.
     */
    @FXML
    public void initialize() {
        scrollPane.fitToWidthProperty().set(true);
        scrollPane.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.ALWAYS);
        final double SPEED = 0.1;
        scrollPane.getContent().setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * SPEED;
            scrollPane.setVvalue(scrollPane.getVvalue() - deltaY);
        });
        scrollPane.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);

        configureLoadButton();

        initializeLists();

        configureLineChart();

        //blockAllComboBoxes while user don't choose Category
        //set default values for comboBoxes
        configurePanel();

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
            List<Integer> brandsIDs = DBProcessor.getBrandsAccordingToCategory(categoryID);

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
                } else {
                    for (JFXCheckBox jfxCheckBox : checkBoxesBrands) {
                        jfxCheckBox.setSelected(false);
                    }
                }
                if (allDataSelected()) {
                    buttonTrack.setDisable(false);
                } else if (noOneBrandIsSelected()) {
                    buttonTrack.setDisable(true);
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

        //Tuning datePickers and logic how they interact.
        configureDatePickers();

        buttonTrack.setOnAction(eventTrack -> {
            //building query for selected parameters.
            int catId = categoriesNames.indexOf(comboBoxCategory.getValue()) + 1;
            ArrayList<Integer> brandIDs = new ArrayList<>();
            for (JFXCheckBox checkBoxesBrand : checkBoxesBrands) {
                if (checkBoxesBrand.isSelected()) {
                    brandIDs.add(brandsNames.indexOf(checkBoxesBrand.getText()) + 1);
                }
            }
            int shopId = shopsNames.indexOf(comboBoxShop.getValue()) + 1;
            int regionId = regionsNames.indexOf(comboBoxRegion.getValue()) + 1;
            StringBuilder brandPartOfQuery = new StringBuilder();
            for (Integer brandID : brandIDs) {
                brandPartOfQuery.append("brand_id = ").append(brandID).append(" OR ");
            }
            Date dateFrom = Date.valueOf(datePickerFrom.getValue());
            Date dateTo = Date.valueOf(datePickerTo.getValue());
            //@params: cat_id, shop_id, region_id, "brand_id = 1 OR brand_id = 2"
            String query = String.format("SELECT * FROM Products WHERE cat_id = %d AND shop_id = %d AND region_id = %d AND (%s) ORDER BY product_id ASC;",
                    catId, shopId, regionId, brandPartOfQuery.substring(0, brandPartOfQuery.toString().length() - 4));

            ArrayList<Product> products = new ArrayList<>();
            try (ResultSet resultSet = DBProcessor.getConnection().createStatement().executeQuery(query)) {
                while (resultSet.next()) {
                    Product product = new Product(resultSet.getInt("product_id"), resultSet.getInt("cat_id"),
                            resultSet.getInt("brand_id"), resultSet.getInt("shop_id"),
                            resultSet.getInt("region_id"), resultSet.getString("link"));
                    products.add(product);
                }
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
            int minDate = Integer.MAX_VALUE;
            int maxDate = Integer.MIN_VALUE;
            lineChart.getData().clear();
            for (Product product : products) {
                String queryRecords = String.format("SELECT * FROM Records WHERE product_id = %d AND date >= '%s' AND date <= '%s';",
                        product.getProduct_id(), dateFrom.toString(), dateTo.toString());
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                ObservableList<XYChart.Data<Number, Number>> observableList = FXCollections.observableArrayList();
                series.setName(brandArrayList.get(product.getBrand_id() - 1).getName());
                try (ResultSet resultSet = DBProcessor.getConnection().createStatement().executeQuery(queryRecords)) {
                    while (resultSet.next()) {
                        String dateStr = resultSet.getDate("date").toString().replace("-", "").substring(2, 8);
                        int date = Integer.parseInt(dateStr.substring(4, 6) + dateStr.substring(2, 4) + dateStr.substring(0, 2));
                        observableList.add(new XYChart.Data<>(date, resultSet.getDouble("price")));
                        if (date > maxDate) {
                            maxDate = date;
                        } else if (date < minDate) {
                            minDate = date;
                        }
                    }
                } catch (SQLException throwable) {
                    throwable.printStackTrace();
                }
                series.setData(observableList);
                lineChart.getData().add(series);
            }
            dateAxis.setLowerBound(minDate - FREQUENCY);
            dateAxis.setUpperBound(maxDate + FREQUENCY);
            lineChart.setTitle(comboBoxCategory.getValue());
        });

        defineActionAddProductMenuItem();
    }

    private void configureDatePickers() {
        datePickerFrom.setDayCellFactory(d -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(item.isAfter(LocalDate.now()));
            }
        });
        datePickerTo.setDayCellFactory(d -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(item.isAfter(LocalDate.now()));
            }
        });
        datePickerFrom.valueProperty().addListener(datePickerFromListener -> {
            LocalDate from = datePickerFrom.getValue();
            datePickerTo.setDayCellFactory(d -> new DateCell() {
                @Override
                public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    setDisable(item.isAfter(LocalDate.now()) || item.isBefore(from));
                }
            });
        });
        datePickerTo.valueProperty().addListener(datePickerToListener -> {
            LocalDate to = datePickerTo.getValue();
            datePickerFrom.setDayCellFactory(d -> new DateCell() {
                @Override
                public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    setDisable(item.isAfter(to));
                }
            });
        });
    }

    private void defineActionAddProductMenuItem() {
        addProduct.setOnAction(eventAddProduct -> {
            Parent root;
            try {
                root = FXMLLoader.load(getClass().getResource("addProduct.fxml"));
                Stage stage = new Stage();
                stage.setTitle("Додати продукцію");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.getIcons().add(new Image(getClass().getResourceAsStream(APP_ICON)));
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void configureLineChart() {
        dateAxis.setForceZeroInRange(false);
        dateAxis.setAutoRanging(false);
        dateAxis.setTickUnit(FREQUENCY);
        dateAxis.setMinorTickVisible(false);
        dateAxis.setTickLabelFormatter(new StringConverter<Number>() {
            //input: 180820
            @Override
            public String toString(Number number) {
                String numStr = number.toString();
                if (numStr.length() >= 6) {
                    return numStr.substring(0, 2) + "." + numStr.substring(2, 4) + "." + numStr.substring(4, 6);
                } else {
                    return null;
                }
            }

            //input: 18.08.20
            @Override
            public Number fromString(String string) {
                return Integer.parseInt(string.replace(".", ""));
            }
        });
        lineChart = new LineChart<>(dateAxis, priceAxis);
        borderPane.setCenter(lineChart);
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