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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScrapeProcessor {

    private WebDriver driver = null;
    private WebDriverWait wait = null;
    private static final Logger logger = Logger.getLogger("ScrapeProcessor Logger");
    private int rsSize = 0;
    private int rsLoaded = 0;

    public int getRsSize() {
        return rsSize;
    }

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
    private static final String SILPO_ADDRESS = "Tarasa Shevchenka Boulevard, Kyiv 26/4";
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
    private static final String SELECT_ZAKAZ = "span.jsx-2148813386.big-product-card__price-value";
    private static final String SELECT_EPICENTR_PRICE_WRAPPER = "span.price-wrapper";
    private static final String SELECT_TAVRIAV_SALE_PRICE = "span.sale-price";
    private static final String SELECT_ROST_PRICE = "span.price";
    private static final String SELECT_KOPEYKA_FULL_ADD = "div.full-add";
    private static final String SELECT_KOPEYKA_PRICE = "li.new-prc";


    public static void main(String[] args) {
        ScrapeProcessor scrapeProcessor = new ScrapeProcessor();

//        scrapProcessor.scrapePricesInRange(635, 680);

        System.out.println(scrapeProcessor.scrapePrice(13, "https://rost.kh.ua/catalog/produktovaya_gruppa-detskoe_pitanie-pyure_dlya_detei/4308989/"));
        System.out.println(scrapeProcessor.scrapePrice(13, "https://rost.kh.ua/catalog/produktovaya_gruppa-detskoe_pitanie-pyure_dlya_detei/4038103/"));
    }

    public ScrapeProcessor() {
        setupDriver();
    }

    private void setupDriver() {
        /*
        Setup chromeDriver.
        CHECK if it works on other pc, especially if there is no chrome installed on pc.
         */
        WebDriverManager.chromedriver().setup();
        /*
        Creating chrome options.
        One of them is that selenium shouldn't open each url in a new window.
         */
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");
        /*
        Creating driver and wait based on options above.
         */
        driver = new ChromeDriver(chromeOptions);
        wait = new WebDriverWait(driver, 8);
    }

    //    This method takes into account silpo's redirecting and fills all fields that in result allow to scrape price.
    private Double getPriceSilpo(String url) {
        driver.navigate().to(url);
//        If silpo never runs before, configure address and redirect to product.
        if (!driver.findElements(By.className(SILPO_ACTIVE_INPUT)).isEmpty()) {
            driver.findElement(By.className(SILPO_ACTIVE_INPUT)).sendKeys(SILPO_ADDRESS);
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.className(SILPO_COMBOBOX_KIEV)));
                driver.findElement(By.className(SILPO_COMBOBOX_KIEV)).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.className(SILPO_DELIVERY_CHECKBOX)));
//            This work if our cursor isn't on the browser screen.
                driver.findElement(By.className(SILPO_DELIVERY_CHECKBOX)).click();
                driver.findElement(By.className(SILPO_DELIVERY_CHECKBOX)).click();
                wait.until(ExpectedConditions.elementToBeClickable(By.tagName(SILPO_SUBMIT_BUTTON)));
                driver.findElement(By.tagName(SILPO_SUBMIT_BUTTON)).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.className(SILPO_CURRENT_INTEGER)));
            } catch (TimeoutException e) {
                logger.log(Level.SEVERE, "Silpo: timeout exception. Price 0.0 returned.");
                return 0.0;
            }
        }
        try {
            String currentInteger = driver.findElement(By.className(SILPO_CURRENT_INTEGER)).getText();
            String currentFraction = driver.findElement(By.className(SILPO_CURRENT_FRACTION)).getText();
            return Double.parseDouble(currentInteger + "." + currentFraction);
        } catch (NoSuchElementException e) {
            logger.log(Level.SEVERE, "Silpo: NoSuchElementException. Price 0.0 returned.");
            return 0.0;
        }
    }

    private Double getPriceAntoshka(String url) {
        driver.navigate().to(url);
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className(ANTOSHKA_PRICE_BLOCK)));
        } catch (TimeoutException e) {
            logger.log(Level.SEVERE, "Antoshka: timeout exception.");
            return 0.0;
        }
        if (!driver.findElements(By.className(ANTOSHKA_PRICE)).isEmpty() &&
                driver.findElements(By.className(ANTOSHKA_PRICE)).contains(driver.findElement(By.className(ANTOSHKA_PRICE)))) {
            String text = driver.findElement(By.className(ANTOSHKA_PRICE)).getText();
            return Double.parseDouble(formatText(text, 4));
        }
        logger.log(Level.SEVERE, "Antoshka: price block not found. Price 0.0 returned.");
        return 0.0;
    }

    private Double getPriceFora(String url) {
        driver.navigate().to(url);
        if (driver.findElements(By.xpath(FORA_PRICE_CLASS)).isEmpty()) {
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
                logger.log(Level.SEVERE, "Fora: timeout exception. Price 0.0 returned.");
                return 0.0;
            }
        }
        try {
            List<WebElement> elements = driver.findElements(By.className(FORA_PRICE_CLASS));
            String text = elements.get(0).findElements(By.tagName(FORA_PRICE_SPAN)).get(0).getText();
            return Double.parseDouble(formatText(text, 4));
        } catch (NoSuchElementException e) {
            logger.log(Level.SEVERE, "Fora: price not found. Price 0.0 returned.");
            return 0.0;
        }
    }

    private Double getPriceAuchan(String url) {
        driver.navigate().to(url);
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("productDetails_price_actual__12u8E")));
        } catch (TimeoutException e) {
            logger.log(Level.SEVERE, "Auchan: timeout exception. Price 0.0 returned.");
            return 0.0;
        }
        try {
            String text = driver.findElement(By.className("productDetails_price_actual__12u8E"))
                    .findElement(By.tagName("span")).getText();
            return Double.parseDouble(formatText(text, 4));
        } catch (NoSuchElementException e) {
            logger.log(Level.SEVERE, "Auchan: price not found. Price 0.0 returned.");
            return 0.0;
        }
    }

    private Double getPriceZakaz(Document document) {
        if (!document.select(SELECT_ZAKAZ).isEmpty()) {
            return Double.parseDouble(formatText(document.select(SELECT_ZAKAZ).get(0).text(), 0));
        } else {
            logger.log(Level.SEVERE, "Zakaz: price not found. Price 0.0 returned.");
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


    public Double scrapePrice(int shopId, String url) {
        Document document = new Document(url);
        if (shopId != 2 && shopId != 7 && shopId != 8 && shopId != 9) {
            document = scrapeJSoup(url);
        }
        if (shopId > 15 || shopId < 1) {
            logger.log(Level.SEVERE, "Shop id is greater than 15 or smaller than 1. Price 0.0 returned.");
            return 0.0;
        }
        String text = "";
        Elements elements;
        switch (shopId) {
            case 1:
                text = document.select(SELECT_PAMPIK).get(0).text();
                return Double.parseDouble(formatText(text, 0));
            case 2:
                return getPriceAntoshka(url);
            case 3:
                if (!document.select(SELECT_ROZETKA_PRICE).isEmpty()) {
                    text = document.select(SELECT_ROZETKA_PRICE).get(0).text();
                }
                return Double.parseDouble(formatText(text, 1));
            case 4:
                text = document.select(SELECT_APTEKA911).get(0).text();
                return Double.parseDouble(formatText(text, 5));
            case 5:
                if (!document.select(SELECT_MEGAMARKET_PRICE).isEmpty()) {
                    text = document.select(SELECT_MEGAMARKET_PRICE).get(0).text();
                    if (!text.equals("")) {
                        return Double.parseDouble(formatText(text, 3));
                    }
                }
                logger.log(Level.SEVERE, "Megamarket: price not found. Price 0.0 returned.");
                return 0.0;
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
                if (!document.select(SELECT_EPICENTR_PRICE_WRAPPER).isEmpty()) {
                    text = document.select(SELECT_EPICENTR_PRICE_WRAPPER).get(0).text();
                    return Double.parseDouble(formatText(text, 0));
                } else {
                    logger.log(Level.SEVERE, "Epicentr: price block not found. Price 0.0 returned.");
                    return 0.0;
                }
            case 12:
                if (!document.select(SELECT_TAVRIAV_SALE_PRICE).isEmpty()) {
                    return Double.parseDouble(formatText(document.select(SELECT_TAVRIAV_SALE_PRICE).get(0).text(), 1));
                } else {
                    logger.log(Level.SEVERE, "TavriaV: price block not found. Price 0.0 returned.");
                    return 0.0;
                }
            case 13:
                if (!document.select(SELECT_ROST_PRICE).isEmpty()) {
                    text = document.select(SELECT_ROST_PRICE + " > span").get(0).text() + "." + document.select(SELECT_ROST_PRICE + " > sup").get(0).text();
                    return Double.parseDouble(formatText(text, 0));
                } else {
                    logger.log(Level.SEVERE, "ROST: price block not found. Price 0.0 returned.");
                    return 0.0;
                }
            case 14:
                if (!document.select(SELECT_KOPEYKA_FULL_ADD).select(SELECT_KOPEYKA_PRICE).isEmpty()) {
                    text = document.select(SELECT_KOPEYKA_FULL_ADD).select(SELECT_KOPEYKA_PRICE).get(0).text();
                    return Double.parseDouble(formatText(text, 3));
                } else {
                    logger.log(Level.SEVERE, "Kopeyka: price block not found. Price 0.0 returned.");
                    return 0.0;
                }
            default:
                logger.log(Level.SEVERE, "Default block: something went wrong in method ScrapProcessor#scrapePrice" +
                        "Problems occurred in switch, please, ensure to fix this problem. Price 0.0 returned.");
                return 0.0;
        }
    }

    public void scrapeAllPrices() {
        //TODO replace 1000 -> actual size
        scrapePricesInRange(0, 1000);
    }

    public void scrapePricesInRange(int beginProd, int endProd) {
        List<Product> products = DBProcessor.getProductsSetPartly(beginProd, endProd);
        rsSize = products.size();
        rsLoaded = 0;
        for (Product product : products) {
            rsLoaded++;
            Double price = scrapePrice(product.getShop_id(), product.getLink());
            System.out.print(product.getLink() + "\t");
            DBProcessor.makeRecord(product.getProduct_id(), Date.valueOf(LocalDate.now()), price);
        }
        rsLoaded = 0;
    }

    public double getProgress() {
        return (double) rsLoaded / rsSize;
    }
}
