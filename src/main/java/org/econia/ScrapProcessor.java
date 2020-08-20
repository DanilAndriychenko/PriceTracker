package org.econia;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.SocketTimeoutException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScrapProcessor {

    private WebDriver driver = null;
    private WebDriverWait wait = null;
    private static final Logger logger = Logger.getLogger("ScrapProcessor Logger");
    private int rsSize = 0;
    private int rsLoaded = 0;

    public int getRsSize() {
        return rsSize;
    }

    public int getRsLoaded() {
        return rsLoaded;
    }

    //TODO replace all xpath's
    private static final String SELECT_PAMPIK = "div.product-info__price-current > span";
    private static final String ANTOSHKA_PRICE_BLOCK = "product-price-block";
    private static final String ANTOSHKA_OLD_PRICE = "old-price";
    private static final String ANTOSHKA_PRICE = "price";
    private static final String SELECT_ROZETKA_SMALL = "p.product-prices__small";
    private static final String SELECT_ROZETKA_BIG = "p.product-prices__big";
    private static final String SELECT_APTEKA911 = "div.price-new[content]";
    private static final String SELECT_MEGAMARKET_COMPARE_PRICE = "div.compare_price";
    private static final String SELECT_MEGAMARKET_PRICE = "div.price";
    private static final String SELECT_ATB = "span.price";
    private static final String SILPO_ACTIVE_INPUT = "active-input";
    private static final String SILPO_ADDRESS = "Tarasa Shevchenka Boulevard, Kyiv 26/4";
    private static final String SILPO_COMBOBOX_KIEV = "store-select__autocomplete-item";
    private static final String SILPO_DELIVERY_CHECKBOX = "extra-delivery-item";
    private static final String SILPO_SUBMIT_BUTTON = "/html/body/div/div/div/div/div[1]/div[4]/button";
    private static final String SILPO_OLD_INTEGER = "/html/body/div/div/div/div[2]/div[1]/div/div[1]/div/div[2]/div[3]/div[1]/div[2]/div[1]";
    private static final String SILPO_OLD_FRACTION = "/html/body/div/div/div/div[2]/div[1]/div/div[1]/div/div[2]/div[3]/div[1]/div[2]/div[2]";
    private static final String SILPO_CURRENT_INTEGER = "/html/body/div/div/div/div[2]/div[1]/div/div[1]/div[1]/div[2]/div[2]/div[1]/div/div[1]";
    private static final String SILPO_CURRENT_FRACTION = "/html/body/div/div/div/div[2]/div[1]/div/div[1]/div[1]/div[2]/div[2]/div[1]/div/div[2]";
    private static final String FORA_PRICE_BLOCK = "/html/body/div[1]/div[3]/div/div/div[1]/div[2]/div/div[1]";
    private static final String FORA_INPUT = "/html/body/div/div/form/div[4]/div[1]/div/div[1]/input";
    private static final String FORA_COMBOBOX = "/html/body/div/div/form/div[4]/div[1]/div/div[2]/div";
    private static final String FORA_COMBOBOX_ELEMENT_KYIV = "//*[text()[contains(.,'Київ')]]";
    private static final String FORA_BUTTON = "/html/body/div/div/form/div[4]/div[3]/button";
    private static final String AUCHAN_OLD_PRICE = "/html/body/div[1]/main/div/div/div[1]/div[1]/section/div[2]/div[1]/div[3]/div[1]/span";
    private static final String AUCHAN_ACTUAL_PRICE = "/html/body/div[1]/main/div/div/div[1]/div[1]/section/div[2]/div[1]/div[3]/div[2]/span";
    private static final String SELECT_ZAKAZ = "span[data-marker-value='Price']";
    private static final String SELECT_EPICENTR_PRICE_VALUE = "span.old-price-value";
    private static final String SELECT_EPICENTR_PRICE_WRAPPER = "span.price-wrapper";
    private static final String SELECT_TAVRIAV_EX_PRICE = "span.ex-price";
    private static final String SELECT_TAVRIAV_SALE_PRICE = "span.sale-price";
    private static final String SELECT_ROST_OLD_PRICE = "span.old-price";
    private static final String SELECT_ROST_PRICE = "span.price";
    private static final String SELECT_KOPEYKA_FULL_ADD = "div.full-add";
    private static final String SELECT_KOPEYKA_PRICE = "li.new-prc";


    /*public static void main(String[] args) {
        long start = System.currentTimeMillis();
        ScrapProcessor scrapProcessor = new ScrapProcessor();
        scrapProcessor.scrapePricesInRange(0, 1000);

        *//*scrapProcessor.scrapePricesInRange(463, 494);
        System.out.println("Time for scraping all prices: " + (System.currentTimeMillis() - start));

        System.out.println(scrapProcessor.scrapePrice(7, "https://shop.silpo.ua/detail/748396"));
        System.out.println(System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        System.out.println(scrapProcessor.scrapePrice(7, "https://shop.silpo.ua/detail/748396"));
        System.out.println(System.currentTimeMillis() - start);*//*
    }*/

    public ScrapProcessor() {
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
        wait = new WebDriverWait(driver, 10);
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
                //TODO this doesn't work.
                driver.findElement(By.className(SILPO_DELIVERY_CHECKBOX)).click();
                driver.findElement(By.className(SILPO_DELIVERY_CHECKBOX)).click();
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath(SILPO_SUBMIT_BUTTON)));
                driver.findElement(By.xpath(SILPO_SUBMIT_BUTTON)).click();
                wait.until(ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(By.xpath(SILPO_OLD_INTEGER)),
                        ExpectedConditions.presenceOfElementLocated(By.xpath(SILPO_CURRENT_INTEGER))));
            } catch (TimeoutException e) {
                logger.log(Level.SEVERE, "Silpo: timeout exception. Price 0.0 returned.");
                return 0.0;
            }
        }
        if (!driver.findElements(By.xpath(SILPO_OLD_INTEGER)).isEmpty()) {
            String oldInteger = driver.findElement(By.xpath(SILPO_OLD_INTEGER)).getText();
            String oldFraction = driver.findElement(By.xpath(SILPO_OLD_FRACTION)).getText();
            return Double.parseDouble(oldInteger + "." + oldFraction);
        } else if (!driver.findElements(By.xpath(SILPO_CURRENT_INTEGER)).isEmpty()) {
            String currentInteger = driver.findElement(By.xpath(SILPO_CURRENT_INTEGER)).getText();
            String currentFraction = driver.findElement(By.xpath(SILPO_CURRENT_FRACTION)).getText();
            return Double.parseDouble(currentInteger + "." + currentFraction);
        }
        logger.log(Level.SEVERE, "Silpo: price block not found. Price 0.0 returned.");
        return 0.0;
    }

    private Double getPriceAntoshka(String url) {
        driver.navigate().to(url);
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className(ANTOSHKA_PRICE_BLOCK)));
        } catch (TimeoutException e) {
            logger.log(Level.SEVERE, "Antoshka: timeout exception.");
            return 0.0;
        }
        if (!driver.findElements(By.className(ANTOSHKA_OLD_PRICE)).isEmpty() &&
                driver.findElements(By.className(ANTOSHKA_OLD_PRICE)).contains(driver.findElement(By.className(ANTOSHKA_OLD_PRICE)))) {
            String text = driver.findElement(By.className(ANTOSHKA_OLD_PRICE)).getText();
            return Double.parseDouble(formatText(text, 4));
        } else if (!driver.findElements(By.className(ANTOSHKA_PRICE)).isEmpty() &&
                driver.findElements(By.className(ANTOSHKA_PRICE)).contains(driver.findElement(By.className(ANTOSHKA_PRICE)))) {
            String text = driver.findElement(By.className(ANTOSHKA_PRICE)).getText();
            return Double.parseDouble(formatText(text, 4));
        }
        logger.log(Level.SEVERE, "Antoshka: price block not found. Price 0.0 returned.");
        return 0.0;
    }

    private Double getPriceFora(String url) {
        driver.navigate().to(url);
        if (driver.findElements(By.xpath(FORA_PRICE_BLOCK)).isEmpty()) {
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(FORA_INPUT)));
                driver.findElement(By.xpath(FORA_INPUT)).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(FORA_COMBOBOX)));
                driver.findElement(By.xpath(FORA_COMBOBOX)).findElement(By.xpath(FORA_COMBOBOX_ELEMENT_KYIV)).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(FORA_BUTTON)));
                driver.findElement(By.xpath(FORA_BUTTON)).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(FORA_PRICE_BLOCK)));
            } catch (TimeoutException e) {
                logger.log(Level.SEVERE, "Fora: timeout exception. Price 0.0 returned.");
                return 0.0;
            }
        }
        if (!driver.findElements(By.xpath(FORA_PRICE_BLOCK + "/span[2]")).isEmpty()) {
            String text = driver.findElement(By.xpath(FORA_PRICE_BLOCK + "/span[2]")).getText();
            return Double.parseDouble(formatText(text, 5));
        } else if (!driver.findElements(By.xpath(FORA_PRICE_BLOCK + "/span")).isEmpty()) {
            String text = driver.findElement(By.xpath(FORA_PRICE_BLOCK + "/span")).getText();
            return Double.parseDouble(formatText(text, 5));
        }
        logger.log(Level.SEVERE, "Fora: price block not found. Price 0.0 returned.");
        return 0.0;
    }

    private Double getPriceAuchan(String url) {
        driver.navigate().to(url);
        try {
            wait.until(ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(By.xpath(AUCHAN_OLD_PRICE)),
                    ExpectedConditions.presenceOfElementLocated(By.xpath(AUCHAN_ACTUAL_PRICE))));
        } catch (TimeoutException e) {
            logger.log(Level.SEVERE, "Auchan: timeout exception. Price 0.0 returned.");
            return 0.0;
        }
        if (!driver.findElements(By.xpath(AUCHAN_OLD_PRICE)).isEmpty()) {
            String text = driver.findElement(By.xpath(AUCHAN_OLD_PRICE)).getText();
            return Double.parseDouble(formatText(text, 5));
        } else if (!driver.findElements(By.xpath(AUCHAN_ACTUAL_PRICE)).isEmpty()) {
            String text = driver.findElement(By.xpath(AUCHAN_ACTUAL_PRICE)).getText();
            return Double.parseDouble(formatText(text, 5));
        }
        logger.log(Level.SEVERE, "Auchan: price block not found. Price 0.0 returned.");
        return 0.0;
    }

    private Double getPriceZakaz(Document document) {
        if (!document.select(SELECT_ZAKAZ).isEmpty()){
            return Double.parseDouble(document.select(SELECT_ZAKAZ).text());
        }else {
            return 0.0;
        }
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
//                Pampik.
                text = document.select(SELECT_PAMPIK).get(0).text();
                return Double.parseDouble(formatText(text, 0));
            case 2:
//                Antoshka.
                return getPriceAntoshka(url);
            case 3:
//                Rozetka.
                if (!document.select(SELECT_ROZETKA_SMALL).isEmpty()) {
                    text = document.select(SELECT_ROZETKA_SMALL).text();
                } else if (!document.select(SELECT_ROZETKA_BIG).isEmpty()) {
                    text = document.select(SELECT_ROZETKA_BIG).text();
                }
                return Double.parseDouble(formatText(text, 1));
            case 4:
//                Apteka 911.
                text = document.select(SELECT_APTEKA911).get(0).text();
                return Double.parseDouble(formatText(text, 5));
            case 5:
//                MegaMarket.
                elements = document.select(SELECT_MEGAMARKET_COMPARE_PRICE);
                if (!elements.isEmpty()) {
                    text = elements.get(0).text();
                    return Double.parseDouble(formatText(text, 0));
                }
                text = document.select(SELECT_MEGAMARKET_PRICE).get(0).text();
                return Double.parseDouble(formatText(text, 4));
            case 6:
//                ATB.
                text = document.select(SELECT_ATB).get(0).text();
                return Double.parseDouble(formatText(text, 2) + "." +
                        document.select(SELECT_ATB + " > span").get(0).text());
            case 7:
//                Silpo.
                return getPriceSilpo(url);
            case 8:
//                Fora.
                return getPriceFora(url);
            case 9:
//                Auchan.
                return getPriceAuchan(url);
            case 10:
            case 15:
//                Shops from zakaz.ua. NOVUS. METRO.
                return getPriceZakaz(document);
            case 11:
//                Epicentr.
                if (!document.select(SELECT_EPICENTR_PRICE_VALUE).isEmpty()) {
                    return Double.parseDouble(formatText(document.select(SELECT_EPICENTR_PRICE_VALUE).get(0).text(), 0));
                } else if (!document.select(SELECT_EPICENTR_PRICE_WRAPPER).isEmpty()) {
                    return Double.parseDouble(formatText(document.select(SELECT_EPICENTR_PRICE_WRAPPER).get(0).text(), 0));
                }
                logger.log(Level.SEVERE, "Epicentr: price block not found. Price 0.0 returned.");
                return 0.0;
            case 12:
//                Tavriav.
                if (!document.select(SELECT_TAVRIAV_EX_PRICE).isEmpty()) {
                    return Double.parseDouble(formatText(document.select(SELECT_TAVRIAV_EX_PRICE).get(0).text(), 1));
                } else if (!document.select(SELECT_TAVRIAV_SALE_PRICE).isEmpty()) {
                    return Double.parseDouble(formatText(document.select(SELECT_TAVRIAV_SALE_PRICE).get(0).text(), 1));
                }
                logger.log(Level.SEVERE, "TavriaV: price block not found. Price 0.0 returned.");
                return 0.0;
            case 13:
//                РОСТ.
                if (!document.select(SELECT_ROST_OLD_PRICE).isEmpty()) {
                    return Double.parseDouble(formatText(document.select(SELECT_ROST_OLD_PRICE).get(0).text(), 3));
                } else if (!document.select(SELECT_ROST_PRICE).isEmpty()) {
                    return Double.parseDouble(document.select(SELECT_ROST_PRICE + " > span").get(0).text() + "." + document.select(SELECT_ROST_PRICE + " > sup").get(0).text());
                }
                logger.log(Level.SEVERE, "ROST: price block not found. Price 0.0 returned.");
                return 0.0;
            case 14:
//                Kopeyka.
                text = document.select(SELECT_KOPEYKA_FULL_ADD).select(SELECT_KOPEYKA_PRICE).get(0).text();
                return Double.parseDouble(formatText(text, 3));
            default:
                logger.log(Level.SEVERE, "Default block: we can't be here at all. " +
                        "Problems occurred in switch, please, ensure to fix this problem. Price 0.0 returned.");
                return 0.0;
        }
    }

    public void scrapeAllPrices() {
        scrapePricesInRange(0, 1000);
    }

    public void scrapePricesInRange(int beginProd, int endProd) {
        List<Product> products = DBProcessor.getProductsSetPartly(beginProd, endProd);
        rsSize = products.size();
        rsLoaded=0;
        for (Product product : products) {
            rsLoaded++;
            Double price = scrapePrice(product.getShop_id(), product.getLink());
//            System.out.print(product.getLink() + "\t");
            DBProcessor.makeRecord(product.getProduct_id(), Date.valueOf(LocalDate.now()), price);
        }
        rsLoaded=0;
    }

    public double getProgress() {
        return (double)rsLoaded / rsSize;
    }
}
