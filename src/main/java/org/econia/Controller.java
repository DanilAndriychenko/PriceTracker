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
    private Timer timer = new Timer();

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
    private List<Category> subcategoryArrayList;
    private List<String> subcategoryStrings;
    private List<String> ourBrandsNames;

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

    public List<Category> getSubcategoryArrayList() {
        return subcategoryArrayList;
    }

    public List<String> getSubcategoryStrings() {
        return subcategoryStrings;
    }

    public List<String> getOurBrandsNames() {
        return ourBrandsNames;
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
    @FXML
    private MenuItem addAvailability;

    /**
     * Initialize.
     */
    @FXML
    public void initialize() {
//        Create and load some lists via DBProcessor.
        initializeLists();
//        Just set ip variable to localHostName.
        configureIP();
        /*
        Block/unblock buttons AddProduct and ForceUpdateData.
        Reset timer if needed.
         */
        refreshAccess();
//        Set scroll speed and other settings.
        configureScrollPane();
        /*
        Creating instance of ScrapeProcessor and set actions for click on loadButton.
         */
        configureLoadButton();
//      Configure all that refers to axes and line chart.
        configureLineChart();
        /*
        Block all combo boxes until user selects a category.
        Set prompt text for panel controls.
         */
        configurePanel();
//        Configure datePickers and logic how they interact.
        configureDatePickers();
        /*
        Open windows: addProduct, accessControl, dateFormat when user clicks on appropriate menu item.
         */
        configureAddProductMenuItem();
        configureControlAccessMenuItem();
        configureDateFormatMenuItem();
        configureAddAvailability();

//        Set actions when user choose something in combo boxes on panel.
        configureComboBoxes();

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
                    tooltip.setText("Дата: " + dateAxis.getTickLabelFormatter().toString(data.getXValue()) + "\nЦіна: " + data.getYValue());
                    tooltip.setFont(new Font(SYSTEM_ITALIC, 14));
                    hackTooltipStartTiming(tooltip);
                    Tooltip.install(data.getNode(), tooltip);
                }
            }
        });
    }

    private void configureAddAvailability(){
        addAvailability.setOnAction(eventAddAvailability -> {
            Parent root;
            try {
                root = FXMLLoader.load(getClass().getResource("addAvailability.fxml"));
                Stage stage = new Stage();
                stage.setTitle("Додати наявність");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.getIcons().add(new Image(getClass().getResourceAsStream(APP_ICON)));
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void configureComboBoxes(){
        comboBoxCategory.setOnAction(eventCategoryAction -> {
            comboBoxShop.setDisable(false);
            //Get category and get brands that compete in this category.
            String category = comboBoxCategory.getValue();
            int categoryID = categoriesNames.indexOf(category) + 1;
            List<Integer> brandsIDs = DBProcessor.getBrandsAccordingToCategory(categoryID);
            /*
            Yeah, we already disable button track when configuring the panel,
            but we need to disable it each time category changes, because on this action we clear all brands checkboxes.
             */
            buttonTrack.setDisable(true);
//            Add brands checkboxes on vbox on panel.
            fillBrandsVBoxAccordingToCategory(brandsIDs);
        });

        comboBoxShop.setOnAction(eventShopAction -> {
            comboBoxRegion.setDisable(false);
            comboBoxRegion.getItems().setAll(regionsNames);
        });

        comboBoxRegion.setOnAction(eventRegionAction -> {
            datePickerFrom.setDisable(false);
            datePickerTo.setDisable(false);
        });
    }

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
        subcategoryArrayList = DBProcessor.getAllSubcategories();
        subcategoryStrings = DBProcessor.getSubcategoriesNames(subcategoryArrayList);
        ourBrandsNames = DBProcessor.getOurBrands(brandArrayList);
    }

    private void configureIP() {
        try {
            ip = InetAddress.getLocalHost().getHostName();
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

    public void refreshAccess() {
        accessLevel = DBProcessor.getAccessLevel(ip);
        if (accessLevel != null) {
            addProduct.setDisable(!accessLevel.isAddSKUToggleButtonEnabled());
            loadButton.setDisable(!accessLevel.isForceUpdateToggleButtonEnabled());
            if (accessLevel.isAutoUpdateToggleButtonEnabled()) {
                long currTime = System.currentTimeMillis();
                long firstTime = ((currTime / 1_000 / 60 / 60 / 24 + 1) * 24 + -3 + 7) * 60 * 60 * 1_000;
                java.util.Date date = new java.util.Date(firstTime);
                try {
                    timer.cancel();
                    timer = new Timer();
                } catch (IllegalStateException illegalStateException) {
                    illegalStateException.printStackTrace();
                }
                int period = (accessLevel.getPeriod() * 24 * 60 * 60 +1) * 1000;
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        loadButton.fire();
                    }
                }, date, period);
            }
        }
    }

    private void configureLoadButton() {
        loadButton.setDisable(true);
        timeLabel.setVisible(false);
        progressBar.setVisible(false);
        /*
        Task that creates ScrapeProcessor object and while this,
        show message that info that ability to force push is downloading.
         */
        new Thread(getTaskCreateScrapeProcessorObject()).start();

        loadButton.setOnAction(loadButtonAction -> new Thread(() -> {
            Platform.runLater(() -> {
                loadButton.setDisable(true);
                timeLabel.setVisible(true);
                progressBar.setProgress(0);
                progressBar.setVisible(true);
            });
            Timeline clock = getClockTimeLine();
            Task<Void> loadPrices = getTaskLoadPrices(clock);
            loadPrices.run();

            showLabelDataLoaded();
        }).start());
    }

    private void showLabelDataLoaded(){
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
    }

    private Task<Void> getTaskLoadPrices(Timeline clock){
        return new Task<Void>() {
            @Override
            protected Void call() {
                clock.play();
                try {
                    scrapeProcessor.scrapeAllPrices();
                } catch (RuntimeException runtimeException) {
                    runtimeException.printStackTrace();
                }
                clock.stop();
                return null;
            }
        };
    }

    private Timeline getClockTimeLine(){
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
        return clock;
    }

    private Task<Void> getTaskCreateScrapeProcessorObject(){
        return new Task<Void>() {
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
    }

    private void configureLineChart() {
//        Date axis.
        dateAxis.setLabel("Дата");
        dateAxis.setStyle("-fx-font-size: 16");
        dateAxis.setTickLabelFont(new Font(SYSTEM_ITALIC, 12));
        dateAxis.setTickLabelRotation(60);
        dateAxis.setForceZeroInRange(false);
        dateAxis.setAutoRanging(false);
        dateAxis.setTickUnit(1);
        dateAxis.setMinorTickVisible(false);
        setDateFormatPattern("dd MMM yy");
//        Price axis.
        priceAxis.setLabel("Ціна, грн");
        priceAxis.setMinorTickVisible(false);
        priceAxis.setStyle("-fx-font-size: 16");
        priceAxis.setTickLabelFont(new Font(SYSTEM_ITALIC, 12));
        /*
        Create non-animated line chart with axes configured above;
        Set line chart to the center of border pane.
         */
        lineChart = new LineChart<>(dateAxis, priceAxis);
        lineChart.setAnimated(false);
        borderPaneCenter.setCenter(lineChart);
    }

    private void configurePanel() {
        comboBoxShop.setDisable(true);
        comboBoxRegion.setDisable(true);
        buttonTrack.setDisable(true);
        datePickerFrom.setDisable(true);
        datePickerTo.setDisable(true);

        comboBoxCategory.setPromptText(COMBO_BOX_CATEGORY_PROMPT_TEXT);
        comboBoxShop.setPromptText(COMBO_BOX_SHOP_PROMPT_TEXT);
        comboBoxRegion.setPromptText(COMBO_BOX_REGION_PROMPT_TEXT);
        vBoxBrands.getChildren().add(new Label(BRANDS_CHOOSE_CATEGORY));

        comboBoxCategory.getItems().setAll(categoriesNames);
        comboBoxShop.getItems().setAll(shopsNames);
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
    }

    private void configureAddProductMenuItem() {
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

    private void configureDateFormatMenuItem() {
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


    private JFXCheckBox getJFXCheckBoxAll(){
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
        return jfxCheckBoxAll;
    }

    private void fillBrandsVBoxAccordingToCategory(List<Integer> brandsIDs){
        vBoxBrands.getChildren().clear();
        checkBoxesBrands.clear();
        JFXCheckBox jfxCheckBoxAll = getJFXCheckBoxAll();
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


    List<String> lineColorsArray = Arrays.asList("255, 0, 0", "255, 127, 0", "255, 255, 0", "127, 255, 0", "0, 255, 0",
            "0, 255, 127", "0,255,255", "0,127,255", "0,0,255", "127,0,255", "255,0,255", "255,0,127");

    /*private void setSeriesColors(LineChart<Number, Number> chart) {
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

    }*/

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