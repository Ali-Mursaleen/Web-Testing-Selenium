package com.Abdul_Raqeeb.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CartTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get("https://www.demoblaze.com/");
    }

    @Test
    public void testAddAndRemoveCart() {
        // Click the first visible product
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".hrefch")));
        driver.findElement(By.cssSelector(".hrefch")).click();

        // Wait for product page and click "Add to cart"
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Add to cart' or contains(text(),'Add to cart')]"))).click();

        // Wait for and accept alert confirmation
        String alertText = waitForAndAcceptAlert(15);
        System.out.println("Add to cart alert: " + alertText);

        // Navigate to cart
        driver.findElement(By.id("cartur")).click();

        // Wait until the cart table has rows
        boolean hasItems = waitForCartRows(20);
        assertTrue(hasItems, "Expected at least one product row in the cart after adding.");

        // Optional: remove item
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Delete']"))).click();

        // Wait until cart empties
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("#tbodyid > tr"), 0));
    }

    private String waitForAndAcceptAlert(int timeoutSec) {
        WebDriverWait alertWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));
        alertWait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        String text = alert.getText();
        alert.accept();
        return text;
    }

    private boolean waitForCartRows(int timeoutSec) {
        WebDriverWait cartWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));
        try {
            cartWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("#tbodyid > tr"), 0));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    @AfterEach
    public void tearDown(TestInfo testInfo) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(screenshot.toPath(),
                    new File("target/screenshots/" + testInfo.getDisplayName() + ".png").toPath());
        } catch (IOException ignored) {}
        if (driver != null) driver.quit();
    }
}
