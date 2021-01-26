package org.econia;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.nodes.Element;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type Scrape processor.
 */
public class ScrapeProcessor {

    public static void main(String[] args) {
        ScrapeProcessor scrapeProcessor = new ScrapeProcessor();
        scrapeProcessor.scrapeAvailabilityInRange(282, 304);
//        System.out.println(scrapeProcessor.scrapeAvailability("https://shop.silpo.ua/detail/386913", 7));
//        System.out.println(scrapeProcessor.scrapeAvailability("https://shop.silpo.ua/detail/386912", 7));
    }

    private WebDriver driver = null;
    private WebDriverWait wait = null;
    private static final Logger SCRAPE_PROCESSOR_LOGGER = Logger.getLogger("ScrapeProcessor Logger");
    private int rsSize = 0;
    private int rsLoaded = 0;

    /**
     * Gets rs size.
     *
     * @return the rs size
     */
    public int getRsSize() {
        return rsSize;
    }

    /**
     * Gets rs loaded.
     *
     * @return the rs loaded
     */
    public int getRsLoaded() {
        return rsLoaded;
    }

    private static final String SELECT_PAMPIK = "div.product-info__price-current > span";
    private static final String ANTOSHKA_PRICE_BLOCK = "product-price-block";
    private static final String ANTOSHKA_PRICE = "price";
    private static final String SELECT_ROZETKA_PRICE = "div.product-prices__inner > p";
    private static final String SELECT_APTEKA911 = "div.price-new[content]";
    private static final String SELECT_MEGAMARKET_PRICE = "div.price";
    private static final String SELECT_ATB = "span.price";
    private static final String SILPO_ACTIVE_INPUT = "active-input";
    private static final String SILPO_ADDRESS = "просп. Бандери Степана, 36";
    private static final String SILPO_COMBOBOX_KIEV = "store-select__autocomplete-item";
    private static final String SILPO_DELIVERY_CHECKBOX = "extra-delivery-item";
    private static final String SILPO_SUBMIT_BUTTON = "button";
    private static final String SILPO_CURRENT_INTEGER = "current-integer";
    private static final String SILPO_CURRENT_FRACTION = "current-fraction";
    private static final String FORA_DELIVERY_CITY_COMBOBOX = "delivery_city-selectized";
    private static final String FORA_DELIVERY_CITY_VALUE = "div[data-value='1']";
    private static final String FORA_DELIVERY_STORE_COMBOBOX = "delivery_store-selectized";
    private static final String FORA_DELIVERY_STORE_SELECT_WRAPPER = "delivery_store-select-wrapper";
    private static final String FORA_DELIVERY_STORE_VALUE = "div[data-value='26']";
    private static final String FORA_SUBMIT_BUTTON = "button";
    private static final String FORA_PRICE_CLASS = "price";
    private static final String FORA_PRICE_SPAN = "span";
    private static final String SELECT_ZAKAZ = "span.Price__value_title";
    private static final String SELECT_EPICENTR_PRICE_WRAPPER = "div.p-price__main";
    private static final String SELECT_TAVRIAV_SALE_PRICE = "p.price__current";
    private static final String SELECT_ROST_PRICE = "span.price";
    private static final String SELECT_KOPEYKA_FULL_ADD = "div.full-add";
    private static final String SELECT_KOPEYKA_PRICE = "li.new-prc";

    /**
     * Instantiates a new Scrape processor.
     */
    public ScrapeProcessor() {
        setupDriver();
    }

    private void setupDriver() {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\danil\\Documents\\GitHub\\PriceTracker\\src\\main\\resources\\org\\econia\\chromedriver.exe");
        System.setProperty("webdriver.chrome.silentOutput", "true");
        /*
        Setup chromeDriver.
         */
        WebDriverManager.chromedriver().setup();
        /*
        Creating chrome options.
        One of them is that selenium shouldn't open each url in a new window.
         */
        ChromeOptions chromeOptions = new ChromeOptions();
//        chromeOptions.addArguments("--headless");
        chromeOptions.addArguments("--disable-gpu");
        chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-browser-side-navigation");
        /*
        Creating driver and wait based on options above.
         */
        driver = new ChromeDriver(chromeOptions);
        driver.manage().timeouts().pageLoadTimeout(30L, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(10L, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, 30);
    }

    private Double getPriceSilpo(String url) {
        driver.navigate().to(url);
        if (!driver.findElements(By.className("address-resolver-exp")).isEmpty()) {
//            This work if our cursor isn't on the browser screen.
            driver.findElements(By.className("button-switch-item")).get(1).click();
            driver.findElements(By.className("button-switch-item")).get(1).click();
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.className("store-select__store")));
                driver.findElement(By.className("store-select__store")).sendKeys(SILPO_ADDRESS);
//                wait.until(ExpectedConditions.presenceOfElementLocated(By.className(SILPO_COMBOBOX_KIEV)));
//                driver.findElement(By.className(SILPO_COMBOBOX_KIEV)).click();
//                wait.until(ExpectedConditions.presenceOfElementLocated(By.className(SILPO_DELIVERY_CHECKBOX)));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.className("root-page__holder")));
                driver.findElement(By.className("root-page__holder")).click();
                wait.until(ExpectedConditions.elementToBeClickable(By.tagName(SILPO_SUBMIT_BUTTON)));
                driver.findElement(By.tagName(SILPO_SUBMIT_BUTTON)).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.className(SILPO_CURRENT_INTEGER)));
            } catch (TimeoutException e) {
                SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Silpo: timeout exception. Price 0.0 returned.");
                return 0.0;
            }
        }
        if (!driver.findElements(By.className("no-data__content")).isEmpty()) {
            SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Silpo: NoSuchElementException. Price 0.0 returned.");
            return 0.0;
        }
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className(SILPO_CURRENT_INTEGER)));
            String currentInteger = driver.findElement(By.className(SILPO_CURRENT_INTEGER)).getText();
            String currentFraction = driver.findElement(By.className(SILPO_CURRENT_FRACTION)).getText();
            return Double.parseDouble(currentInteger + "." + currentFraction);
        } catch (NoSuchElementException e) {
            SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Silpo: NoSuchElementException. Price 0.0 returned.");
            return 0.0;
        }
    }

    private Double getPriceAntoshka(String url) {
        try {
            driver.navigate().to(url);
        } catch (TimeoutException timeoutException) {
            System.out.println("Antoshka: timeout exception");
//            timeoutException.printStackTrace();
        }
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className(ANTOSHKA_PRICE_BLOCK)));
        } catch (TimeoutException e) {
            SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Antoshka: timeout exception.");
            return 0.0;
        }
        if (!driver.findElements(By.cssSelector("div.price")).isEmpty()) {
            try {
                String text = driver.findElement(By.cssSelector("div.price")).getText();
                return Double.parseDouble(formatText(text, 4));
            } catch (IndexOutOfBoundsException | NumberFormatException runtimeException) {
                SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Antoshka: price not found or can''t parse that string. Price 0.0 returned.");
                return 0.0;
            }
        }
        SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Antoshka: price block not found. Price 0.0 returned.");
        return 0.0;
    }

    private Double getPriceFora(String url) {
        driver.navigate().to(url);
        if (!driver.findElements(By.className("order-form-wrapper")).isEmpty()) {
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id(FORA_DELIVERY_CITY_COMBOBOX)));
                driver.findElement(By.id(FORA_DELIVERY_CITY_COMBOBOX)).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(FORA_DELIVERY_CITY_VALUE)));
                driver.findElement(By.cssSelector(FORA_DELIVERY_CITY_VALUE)).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id(FORA_DELIVERY_STORE_COMBOBOX)));
                driver.findElement(By.id(FORA_DELIVERY_STORE_COMBOBOX)).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.className(FORA_DELIVERY_STORE_SELECT_WRAPPER)));
                driver.findElement(By.className(FORA_DELIVERY_STORE_SELECT_WRAPPER)).findElement(By.cssSelector(FORA_DELIVERY_STORE_VALUE)).click();
                wait.until(ExpectedConditions.elementToBeClickable(By.tagName(FORA_SUBMIT_BUTTON)));
                driver.findElement(By.tagName(FORA_SUBMIT_BUTTON)).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.className(FORA_PRICE_CLASS)));
            } catch (TimeoutException e) {
                SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Fora: timeout exception. Price 0.0 returned.");
                return 0.0;
            }
        }
        try {
            List<WebElement> elements = driver.findElements(By.className(FORA_PRICE_CLASS));
            String text = elements.get(0).findElements(By.tagName(FORA_PRICE_SPAN)).get(0).getText();
            return Double.parseDouble(formatText(text, 4));
        } catch (NoSuchElementException | IndexOutOfBoundsException | NumberFormatException e) {
            SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Fora: price not found or can''t parse that string. Price 0.0 returned.");
            return 0.0;
        }
    }

    private Double getPriceAuchan(String url) {
        driver.navigate().to(url);
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("productDetails_price_actual__12u8E")));
        } catch (TimeoutException e) {
            SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Auchan: timeout exception. Price 0.0 returned.");
            return 0.0;
        }
        try {
            String text = driver.findElement(By.className("productDetails_price_actual__12u8E"))
                    .findElement(By.tagName("span")).getText();
            return Double.parseDouble(formatText(text, 4));
        } catch (NoSuchElementException e) {
            SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Auchan: price not found. Price 0.0 returned.");
            return 0.0;
        }
    }

    private Double getPriceZakaz(Document document) {
        if (!document.select(SELECT_ZAKAZ).isEmpty()) {
            return Double.parseDouble(formatText(document.select(SELECT_ZAKAZ).get(0).text(), 0));
        } else {
            SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Zakaz: price not found. Price 0.0 returned.");
            return 0.0;
        }
//        Here can be problem with links like this: https://novus.zakaz.ua/uk/products/00883314734126/plate-luminarc/
//        Don't understand why this method returns 0.0 on such links.
//        Already tried this one selector -> "span.jsx-3360872049.product-tile__active-price-value.product-tile__active-price_discounted"
    }

    private Document scrapeJSoup(String url) {
        Document document = new Document(url);
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document;
    }

    private String formatText(String text, int numOfCutChars) {
        return text.trim().substring(0, text.length() - numOfCutChars).replace(',', '.').replace(" ", "").trim();
    }

    /**
     * Scrape price double.
     *
     * @param shopId the shop id
     * @param url    the url
     * @return the double
     */
    public Double scrapePrice(int shopId, String url) {
        Document document = new Document(url);
        if (shopId != 2 && shopId != 7 && shopId != 8 && shopId != 9) {
            document = scrapeJSoup(url);
        }
        if (shopId > 15 || shopId < 1) {
            SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Shop id is greater than 15 or smaller than 1. Price 0.0 returned.");
            return 0.0;
        }
        String text = "";
        Elements elements;
        switch (shopId) {
            case 1:
                try {
                    text = document.select(SELECT_PAMPIK).get(0).text();
                    return Double.parseDouble(formatText(text, 0));
                } catch (IndexOutOfBoundsException | NumberFormatException runtimeException) {
                    SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Pampik: price not found or can''t parse that string. Price 0.0 returned.");
                    return 0.0;
                }
            case 2:
                return getPriceAntoshka(url);
            case 3:
                driver.navigate().to(url);
                try {
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-prices__big")));
                } catch (TimeoutException e) {
                    SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Rozetka: timeout exception. Price 0.0 returned.");
                    return 0.0;
                }
                try {
                    text = driver.findElement(By.cssSelector("p.product-prices__big")).getText();
                    return Double.parseDouble(formatText(text, 1));
                } catch (NoSuchElementException e) {
                    SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Rozetka: price not found. Price 0.0 returned.");
                    return 0.0;
                }
                /*try {
                    text = document.select(SELECT_ROZETKA_PRICE).text();
                    return Double.parseDouble(formatText(text, 1));
                } catch (IndexOutOfBoundsException | NumberFormatException runtimeException) {
                    SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Rozetka: price not found or can''t parse that string. Price 0.0 returned.");
                    return 0.0;
                }*/
            case 4:
                try {
                    text = document.select(SELECT_APTEKA911).get(0).text();
                    return Double.parseDouble(formatText(text, 5));
                } catch (IndexOutOfBoundsException | NumberFormatException runtimeException) {
                    SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Apteka911: price not found or can''t parse that string. Price 0.0 returned.");
                    return 0.0;
                }
            case 5:
                try {
                    text = document.select(SELECT_MEGAMARKET_PRICE).get(0).text();
                    return Double.parseDouble(formatText(text, 3));
                } catch (IndexOutOfBoundsException | NumberFormatException runtimeException) {
                    SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Megamarket: price not found or can''t parse that string. Price 0.0 returned.");
                    return 0.0;
                }
            case 6:
                elements = document.select(SELECT_ATB);
                Element element = null;
                if (elements.size() == 1) {
                    element = elements.get(0);
                } else if (elements.size() == 2) {
                    element = elements.get(1);
                }
                if (element != null) {
                    text = element.text();
                    return Double.parseDouble(formatText(text, 2) + "." +
                            document.select(SELECT_ATB + " > span").get(0).text());
                }
                return 0.0;
            case 7:
                return getPriceSilpo(url);
            case 8:
                return getPriceFora(url);
            case 9:
                return getPriceAuchan(url);
            case 10:
            case 15:
                return getPriceZakaz(document);
            case 11:
                try {
                    text = document.select(SELECT_EPICENTR_PRICE_WRAPPER).get(0).text();
                    return Double.parseDouble(formatText(text, 0));
                } catch (IndexOutOfBoundsException | NumberFormatException runtimeException) {
                    SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Epicentr: price not found or can''t parse that string. Price 0.0 returned.");
                    return 0.0;
                }
            case 12:
                try {
                    text = document.select(SELECT_TAVRIAV_SALE_PRICE).get(1).text();
                    return Double.parseDouble(formatText(text, 6));
                } catch (IndexOutOfBoundsException | NumberFormatException runtimeException) {
                    SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "TavriaV: price not found or can''t parse that string. Price 0.0 returned.");
                    return 0.0;
                }
            case 13:
                try {
                    text = document.select(SELECT_ROST_PRICE + " > span").get(0).text() + "." + document.select(SELECT_ROST_PRICE + " > sup").get(0).text();
                    return Double.parseDouble(formatText(text, 0));
                } catch (IndexOutOfBoundsException | NumberFormatException runtimeException) {
                    SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "ROST: price not found or can''t parse that string. Price 0.0 returned.");
                    return 0.0;
                }
            case 14:
                try {
                    text = document.select(SELECT_KOPEYKA_FULL_ADD).select(SELECT_KOPEYKA_PRICE).get(0).text();
                    return Double.parseDouble(formatText(text, 3));
                } catch (IndexOutOfBoundsException | NumberFormatException runtimeException) {
                    SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Kopeyka: price not found or can''t parse that string. Price 0.0 returned.");
                    return 0.0;
                }
            default:
                SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Default block: something went wrong in method ScrapProcessor#scrapePrice" +
                        "Problems occurred in switch, please, ensure to fix this problem. Price 0.0 returned.");
                return 0.0;
        }
    }

    /**
     * Scrape availability string.
     *
     * @param url    the url
     * @param shopId the shop id
     * @return the string
     */
    public String scrapeAvailability(String url, int shopId) {
        if (shopId == 1) return getAvailabilityPampik(url);
        else if (shopId == 2) return getAvailabilityAntoshka(url);
        else if (shopId == 3) return getAvailabilityRozetka(url);
        else {
            if (scrapePrice(shopId, url) != 0.0) return "Available";
            else return "NotAvailable";
        }
    }

    private String getAvailabilityPampik(String url) {
        Document document = scrapeJSoup(url);
        if (!document.select("span.availabily").isEmpty()) {
            return "Available";
        } else {
            return "NotAvailable";
        }
    }

    private String getAvailabilityAntoshka(String url) {
        driver.navigate().to(url);
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className(ANTOSHKA_PRICE_BLOCK)));
        } catch (TimeoutException e) {
            SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Antoshka: timeout exception.");
            return "OutOfStock";
        }
        if (driver.findElements(By.className("product-outofstock-text")).isEmpty()) {
            return "Available";
        }
        return "NotAvailable";
    }

    private String getAvailabilityRozetka(String url) {
        /*Document document = scrapeJSoup(url);
        if (!document.select("p.product__status_color_gray").isEmpty()) {
            return "NotAvailable";
        } else if (!document.select("p.product__status_color_green").isEmpty() ||
                !document.select("p.product__status_color_orange").isEmpty()) {
            return "Available";
        }
        return "OutOfStock";*/
        driver.navigate().to(url);
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("p.product__status")));
        } catch (TimeoutException e) {
            SCRAPE_PROCESSOR_LOGGER.log(Level.SEVERE, "Rozetka: timeout exception.");
            return "OutOfStock";
        }
        if (driver.findElements(By.cssSelector("p.product__status_color_gray")).isEmpty()) {
            return "Available";
        }
        return "NotAvailable";
    }

    /**
     * Scrape all prices.
     */
    public void scrapeAllPrices() {
        scrapePricesInRange(0, 2000);
    }

    /**
     * Scrape all availability.
     */
    public void scrapeAllAvailability() {
        scrapeAvailabilityInRange(0, 1000);
    }


    /**
     * Scrape prices in range.
     *
     * @param beginProd the begin prod
     * @param endProd   the end prod
     */
    public void scrapePricesInRange(int beginProd, int endProd) {
        List<Product> products = DBProcessor.getProductsSetPartly(beginProd, endProd);
        rsSize = products.size();
        rsLoaded = 0;
        for (Product product : products) {
            rsLoaded++;
            Double price = scrapePrice(product.getShop_id(), product.getLink());
            System.out.println(product.getLink() + "\tproductID: " + product.getProduct_id() + "\tprice: " + price);
            DBProcessor.makeRecord(product.getProduct_id(), Date.valueOf(LocalDate.now()), price);
        }
    }

    /**
     * Scrape availability in range.
     *
     * @param beginProd the begin prod
     * @param endProd   the end prod
     */
    public void scrapeAvailabilityInRange(int beginProd, int endProd) {
        List<Product> products = DBProcessor.getProductsAvailabilitySetPartly(beginProd, endProd);
        rsSize = products.size();
        rsLoaded = 0;
        for (Product product : products) {
            rsLoaded++;
            String availability = scrapeAvailability(product.getLink(), product.getShop_id());
            System.out.println(product.getLink() + "\nproductID: " + product.getProduct_id() + "\tprice: " + availability + "\n");
            DBProcessor.makeRecordAvailability(product.getProduct_id(), Date.valueOf(LocalDate.now()), availability);
        }
    }

    public void scrapePriceAndAvailability() {
        rsSize = 0;
        List<Product> productsPrices = DBProcessor.getProductsSetPartly(0, 2000);
        rsSize += productsPrices.size();
        List<Product> productsAvailability = DBProcessor.getProductsAvailabilitySetPartly(0, 1000);
        rsSize += productsAvailability.size();
        rsLoaded = 0;
        for (Product product : productsPrices) {
            rsLoaded++;
            Double price = scrapePrice(product.getShop_id(), product.getLink());
            System.out.println(product.getLink() + "\tproductID: " + product.getProduct_id() + "\tprice: " + price);
            DBProcessor.makeRecord(product.getProduct_id(), Date.valueOf(LocalDate.now()), price);
        }
        for (Product product : productsAvailability) {
            rsLoaded++;
            String availability = scrapeAvailability(product.getLink(), product.getShop_id());
            System.out.println(product.getLink() + "\nproductID: " + product.getProduct_id() + "\tprice: " + availability + "\n");
            DBProcessor.makeRecordAvailability(product.getProduct_id(), Date.valueOf(LocalDate.now()), availability);
        }
    }

    /**
     * Gets progress.
     *
     * @return the progress
     */
    public double getProgress() {
        double progress = (double) (rsLoaded - 1) / rsSize;
        return Math.max(progress, 0.0);
    }
}
