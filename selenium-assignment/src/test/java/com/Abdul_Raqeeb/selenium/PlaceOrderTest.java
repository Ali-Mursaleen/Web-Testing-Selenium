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

public class PlaceOrderTest {

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
    public void testPlaceOrder() {
        // Add first product to cart
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".hrefch")));
        driver.findElement(By.cssSelector(".hrefch")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Add to cart']"))).click();
        waitForAndAcceptAlert(15);

        // Go to cart and ensure product is there
        driver.findElement(By.id("cartur")).click();
        assertTrue(waitForCartRows(20), "Cart must contain product before placing order");

        // Click "Place Order"
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Place Order']"))).click();

        // Fill order form
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name"))).sendKeys("Test User");
        driver.findElement(By.id("country")).sendKeys("Pakistan");
        driver.findElement(By.id("city")).sendKeys("Karachi");
        driver.findElement(By.id("card")).sendKeys("1111222233334444");
        driver.findElement(By.id("month")).sendKeys("10");
        driver.findElement(By.id("year")).sendKeys("2025");

        // Click "Purchase"
        driver.findElement(By.xpath("//button[text()='Purchase']")).click();

        // Verify success modal appears
        WebElement successModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".sweet-alert.showSweetAlert.visible")));
        assertTrue(successModal.getText().contains("Thank you"), "Order confirmation modal should appear.");

        // Close modal
        driver.findElement(By.xpath("//button[text()='OK']")).click();
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
