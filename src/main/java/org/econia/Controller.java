package org.econia;

import com.jfoenix.controls.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * The type Controller.
 */
public class Controller {

    private LineChart<Number, Number> lineChart;
    private final NumberAxis dateAxis = new NumberAxis();
    private final NumberAxis priceAxis = new NumberAxis();
    private ScrapeProcessor scrapeProcessor;
    private DBProcessor.AccessLevel accessLevel;
    private String ip;
    private String dateFormatPattern;

    private static final String COMBO_BOX_CATEGORY_PROMPT_TEXT = "Оберіть категорію";
    private static final String COMBO_BOX_SHOP_PROMPT_TEXT = "Оберіть магазин";
    private static final String COMBO_BOX_REGION_PROMPT_TEXT = "Оберіть регіон";
    private static final String BRANDS_CHOOSE_CATEGORY = "Спочатку оберіть категорію ↑";
    private static final String BRANDS_CHOOSE_ALL = "Обрати всі / Скасувати";
    private static final String APP_ICON = "appIcon1.jpg";
    private static final String SYSTEM_ITALIC = "System Italic";

    private List<Category> categoryArrayList;
    private List<String> categoriesNames;
    private List<Brand> brandArrayList;
    private List<String> brandsNames;
    private List<JFXCheckBox> checkBoxesBrands;
    private List<Shop> shopArrayList;
    private List<String> shopsNames;
    private List<Region> regionArrayList;
    private List<String> regionsNames;

    public List<Category> getCategoryArrayList() {
        return categoryArrayList;
    }

    public List<String> getCategoriesNames() {
        return categoriesNames;
    }

    public List<Brand> getBrandArrayList() {
        return brandArrayList;
    }

    public List<String> getBrandsNames() {
        return brandsNames;
    }

    public List<JFXCheckBox> getCheckBoxesBrands() {
        return checkBoxesBrands;
    }

    public List<Shop> getShopArrayList() {
        return shopArrayList;
    }

    public List<String> getShopsNames() {
        return shopsNames;
    }

    public List<Region> getRegionArrayList() {
        return regionArrayList;
    }

    public List<String> getRegionsNames() {
        return regionsNames;
    }

    public String getIp() {
        return ip;
    }

    public DBProcessor.AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public String getDateFormatPattern() {
        return dateFormatPattern;
    }

    @FXML
    private JFXComboBox<String> comboBoxCategory;
    @FXML
    private VBox vBoxBrands;
    @FXML
    private JFXComboBox<String> comboBoxShop;
    @FXML
    private JFXComboBox<String> comboBoxRegion;
    @FXML
    private JFXDatePicker datePickerFrom;
    @FXML
    private JFXDatePicker datePickerTo;
    @FXML
    private JFXButton buttonTrack;
    @FXML
    private MenuItem addProduct;
    @FXML
    private Label timeLabel;
    @FXML
    private JFXProgressBar progressBar;
    @FXML
    private JFXButton loadButton;
    @FXML
    private BorderPane borderPaneCenter;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private MenuItem dateFormatMenuItem;
    @FXML
    private MenuItem accessMenuItem;

    private void initializeLists() {
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
                Platform.runLater(() -> {
                    timeLabel.setText("Можливість \"Оновити ціни примусово\" завантажується...");
                    timeLabel.setFont(new Font(SYSTEM_ITALIC, 14));
                    timeLabel.setVisible(true);
                });
                scrapeProcessor = new ScrapeProcessor();
                Platform.runLater(() -> {
                    timeLabel.setText("Час завантаження: 00:00 - 00,00%");
                    timeLabel.setFont(new Font(SYSTEM_ITALIC, 18));
                    timeLabel.setVisible(false);
                    if (accessLevel.isForceUpdateToggleButtonEnabled()) {
                        loadButton.setDisable(false);
                    }
                });
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
                if (!Double.isNaN(scrapeProcessor.getProgress())) {
                    timeLabel.setText("Час завантаження: " + String.format("%02d:%02d", minutes, seconds) + " - " +
                            String.format("%.2f%%", scrapeProcessor.getProgress() * 100));
                }
                progressBar.setProgress(scrapeProcessor.getProgress());
            }), new KeyFrame(Duration.seconds(1)));
            clock.setCycleCount(Animation.INDEFINITE);
            Task<Void> loadPrices = new Task<Void>() {
                @Override
                protected Void call() {
                    clock.play();
                    scrapeProcessor.scrapePricesInRange(350, 1000);
                    clock.stop();//TODO doesn't stop
                    return null;
                }
            };
            loadPrices.run();
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                timeLabel.setText("Ціни на продукцію успішно завантажені.");
                timeLabel.setTextFill(Paint.valueOf("green"));
            });
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(() -> {
                timeLabel.setText("");
                timeLabel.setTextFill(Paint.valueOf("black"));
                timeLabel.setVisible(false);
                loadButton.setDisable(false);
            });
        }).start());
    }

    public void refreshAccess() {
        accessLevel = DBProcessor.getAccessLevel(ip);
        if (accessLevel != null) {
            addProduct.setDisable(!accessLevel.isAddSKUToggleButtonEnabled());
            loadButton.setDisable(!accessLevel.isForceUpdateToggleButtonEnabled());
            if (accessLevel.isAutoUpdateToggleButtonEnabled()) {
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date date = null;
                try {
                    date = dateFormatter.parse("2020-08-26 15:06:00");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Timer timer = new Timer();
//                int period = accessLevel.getPeriod()*24*60*60*1000;
                int period = accessLevel.getPeriod()*1000;
                timer.schedule(new AutoUpdateTimerTask(), date, period);
                //TODO stop timer
            }
        }
    }

    private class AutoUpdateTimerTask extends TimerTask{
        @Override
        public void run() {
//            loadButton.fire();
//            System.out.println("fire");
        }
    }

    private void configureIP() {
        try {
            ip = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void configureControlAccessMenuItem() {
        accessMenuItem.setOnAction(accessItemEvent -> {
            try {
                AccessController accessController = new AccessController();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("accessControl.fxml"));
                fxmlLoader.setController(accessController);
                Parent root = fxmlLoader.load();
                Stage stage = new Stage();
                stage.setTitle("Налаштування доступу");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.getIcons().add(new Image(getClass().getResourceAsStream(APP_ICON)));
                stage.setScene(new Scene(root));
                stage.setOnCloseRequest(closeEvent -> {
                    DBProcessor.setAccessLevel(App.getController().getIp(),
                            new DBProcessor.AccessLevel(accessController.getUnlockToggleButton().isSelected(), accessController.getAddSKUToggleButton().isSelected(),
                                    accessController.getForceUpdateToggleButton().isSelected(), accessController.getAutoUpdateToggleButton().isSelected(),
                                    (int) accessController.getSliderFrequency().getValue()));
                    App.getController().refreshAccess();
                });
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void configureScrollPane() {
        scrollPane.fitToWidthProperty().set(true);
        scrollPane.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.ALWAYS);
        final double SPEED = 0.1;
        scrollPane.getContent().setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * SPEED;
            scrollPane.setVvalue(scrollPane.getVvalue() - deltaY);
        });
        scrollPane.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);
    }

    /**
     * Initialize.
     */
    @FXML
    public void initialize() {

        configureIP();
        configureControlAccessMenuItem();
        App.setController(this);
        configureScrollPane();
        refreshAccess();
        configureLoadButton();
        initializeLists();
        configureLineChart();
        /*
        BlockAllComboBoxes while user don't choose Category.
        Set default values for comboBoxes.
         */
        configurePanel();
        //Tuning datePickers and logic how they interact.
        configureDatePickers();
        defineActionAddProductMenuItem();
        defineDateFormatMenuItemAction();

        comboBoxCategory.setOnAction(eventCategoryAction -> {
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
//            long minDate = Integer.MAX_VALUE;
//            long maxDate = Integer.MIN_VALUE;
            lineChart.getData().clear();
            for (Product product : products) {
                String queryRecords = String.format("SELECT * FROM Records WHERE product_id = %d AND date >= '%s' AND date <= '%s';",
                        product.getProduct_id(), dateFrom.toString(), dateTo.toString());
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                ObservableList<XYChart.Data<Number, Number>> observableList = FXCollections.observableArrayList();
                series.setName(brandArrayList.get(product.getBrand_id() - 1).getName());
                try (ResultSet resultSet = DBProcessor.getConnection().createStatement().executeQuery(queryRecords)) {
                    while (resultSet.next()) {
                        Date dateSQL = resultSet.getDate("date");
                        long daysSince = dateSQL.getTime() / 1_000 / 24 / 3_600 + 1;
                        XYChart.Data<Number, Number> data = new XYChart.Data<>(daysSince, resultSet.getDouble("price"));
                        observableList.add(data);
//                        if (daysSince < minDate) minDate = daysSince;
//                        if (daysSince > maxDate) maxDate = daysSince;
                    }
                } catch (SQLException throwable) {
                    throwable.printStackTrace();
                }
                series.setData(observableList);
                lineChart.getData().add(series);
            }
//            If we want to see days from calendar.
            long daysFromSince = dateFrom.getTime() / 1000 / 24 / 3600;
            long daysToSince = dateTo.getTime() / 1000 / 24 / 3600 + 2;
            dateAxis.setLowerBound(daysFromSince);
            dateAxis.setUpperBound(daysToSince);

//            If we want to see min and max date.
//            dateAxis.setLowerBound(minDate-1.0);
//            dateAxis.setUpperBound(maxDate+1.0);

            lineChart.setTitle(comboBoxCategory.getValue());

            for (XYChart.Series<Number, Number> series : lineChart.getData()) {
                for (XYChart.Data<Number, Number> data : series.getData()) {
                    Tooltip tooltip = new Tooltip();
                    tooltip.setText("Дата: " + data.getXValue() + "\nЦіна: " + data.getYValue());
                    tooltip.setFont(new Font(SYSTEM_ITALIC, 14));
                    hackTooltipStartTiming(tooltip);
                    Tooltip.install(data.getNode(), tooltip);
                }
            }
        });

    }

    private static void hackTooltipStartTiming(Tooltip tooltip) {
        try {
            Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            Object objBehavior = fieldBehavior.get(tooltip);
            Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
            fieldTimer.setAccessible(true);
            Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);
            objTimer.getKeyFrames().clear();
            objTimer.getKeyFrames().add(new KeyFrame(new Duration(1)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void defineDateFormatMenuItemAction() {
        dateFormatMenuItem.setOnAction(dateFormatEvent -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("changeDateFormat.fxml"));
                Stage stage = new Stage();
                stage.setTitle("Формат дати");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.getIcons().add(new Image(getClass().getResourceAsStream(APP_ICON)));
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    List<String> lineColorsArray = Arrays.asList("255, 0, 0", "255, 127, 0", "255, 255, 0", "127, 255, 0", "0, 255, 0",
            "0, 255, 127", "0,255,255", "0,127,255", "0,0,255", "127,0,255", "255,0,255", "255,0,127");

    private void setSeriesColors(LineChart<Number, Number> chart) {
        Platform.runLater(() -> {
            Collections.shuffle(lineColorsArray);
            Node line;
            String rgb;
            ObservableList<XYChart.Series<Number, Number>> list = chart.getData();
            for (int i = 0; i < list.size(); i++) {
                line = list.get(i).getNode().lookup(".chart-series-line");
                rgb = lineColorsArray.get(i % lineColorsArray.size());
                Set<Node> nodes = chart.lookupAll(".series" + i);
                for (Node n : nodes) {
                    n.setStyle("-fx-background-color: rgb(" + rgb + ")" + ", white;");
                }
                line.setStyle("-fx-stroke: rgba(" + rgb + ", 1.0);");
            }
        });

    }

    public void setDateFormatPattern(String dateFormatPattern) {
        this.dateFormatPattern = dateFormatPattern;
        dateAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number number) {
                long ms = number.longValue() * 1000 * 24 * 3600;
                java.util.Date date = new java.util.Date(ms);
                return new SimpleDateFormat(dateFormatPattern).format(date);
            }

            @Override
            public Number fromString(String string) {
                /*
                We can't write universal method,
                especially if take into account that there can be no year in the end of the string.
                */
                return null;
            }
        });
    }


    private void configureLineChart() {
        dateAxis.setLabel("Дата");
        priceAxis.setLabel("Ціна, грн");
        priceAxis.setMinorTickVisible(false);
        dateAxis.setStyle("-fx-font-size: 16");
        priceAxis.setStyle("-fx-font-size: 16");
        dateAxis.setTickLabelFont(new Font(SYSTEM_ITALIC, 12));
        priceAxis.setTickLabelFont(new Font(SYSTEM_ITALIC, 12));
        dateAxis.setTickLabelRotation(60);
        dateAxis.setForceZeroInRange(false);
        dateAxis.setAutoRanging(false);
        dateAxis.setTickUnit(1);
        dateAxis.setMinorTickVisible(false);
        setDateFormatPattern("dd MMM yy");
        lineChart = new LineChart<>(dateAxis, priceAxis);
        lineChart.setAnimated(false);
        borderPaneCenter.setCenter(lineChart);
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