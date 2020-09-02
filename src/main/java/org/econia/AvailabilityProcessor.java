package org.econia;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.logging.Logger;

public class AvailabilityProcessor {

    private WebDriver driver = null;
    private WebDriverWait wait = null;
    private static final Logger SCRAPE_PROCESSOR_LOGGER = Logger.getLogger("AvailabilityProcessor Logger");

    public AvailabilityProcessor() {
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


}
