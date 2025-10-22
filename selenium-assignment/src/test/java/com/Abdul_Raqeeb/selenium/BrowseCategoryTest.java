package com.Abdul_Raqeeb.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BrowseCategoryTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE = "https://www.demoblaze.com";

    @BeforeAll
    public void beforeAll() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        // 15s wait to tolerate slow loading
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test
    @DisplayName("Browse Laptops category and verify products appear")
    public void testBrowseLaptops() {
        driver.get(BASE);

        // click Laptops category
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Laptops"))).click();

        // Wait for product area to load. Try a few robust selectors:
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-title a")),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".hrefch")),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-block"))
            ));
        } catch (TimeoutException te) {
            // no products loaded within timeout
            throw new AssertionError("No products visible in Laptops category within timeout.");
        }

        // Collect product elements (several fallback options)
        List<WebElement> products = driver.findElements(By.cssSelector(".card-title a"));
        if (products.isEmpty()) {
            products = driver.findElements(By.cssSelector(".hrefch"));
        }
        if (products.isEmpty()) {
            products = driver.findElements(By.cssSelector(".card-block, .card"));
        }

        assertTrue(products.size() > 0, "Expected at least one product in Laptops category, found: " + products.size());
        System.out.println("Found " + products.size() + " products in Laptops category.");
    }

    @AfterAll
    public void afterAll() {
        if (driver != null) driver.quit();
    }
}
