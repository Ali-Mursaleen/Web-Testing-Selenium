package com.Abdul_Raqeeb.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoginTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE = "https://www.demoblaze.com";

    @BeforeAll
    void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("Login with valid credentials (from SignUpTest)")
    void testValidLogin() {
        String[] creds = TestUtils.readCredentials();
        if (creds == null) Assertions.fail("No credentials found — run SignUpTest first or create credentials file.");

        driver.get(BASE);
        driver.findElement(By.id("login2")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginusername")));

        driver.findElement(By.id("loginusername")).sendKeys(creds[0]);
        driver.findElement(By.id("loginpassword")).sendKeys(creds[1]);
        driver.findElement(By.xpath("//button[text()='Log in']")).click();

        // wait for either welcome name or alert (failure)
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nameofuser")));
            String name = driver.findElement(By.id("nameofuser")).getText();
            assertTrue(name.contains(creds[0]), "Login should show welcome with username");
        } catch (TimeoutException e) {
            // If alert appeared instead, capture text and fail
            if (ExpectedConditions.alertIsPresent() != null) {
                try {
                    Alert a = driver.switchTo().alert();
                    String t = a.getText();
                    a.accept();
                    Assertions.fail("Login failed with alert: " + t);
                } catch (Exception ex) {
                    Assertions.fail("Login failed and no welcome message was found.");
                }
            } else {
                Assertions.fail("Login failed: welcome message did not appear.");
            }
        }
    }

    @Test
    @DisplayName("Login with invalid credentials")
    void testInvalidLogin() {
        driver.get(BASE);
        driver.findElement(By.id("login2")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginusername")));

        driver.findElement(By.id("loginusername")).sendKeys("invalid_user_" + System.currentTimeMillis());
        driver.findElement(By.id("loginpassword")).sendKeys("wrongpass");
        driver.findElement(By.xpath("//button[text()='Log in']")).click();

        wait.until(ExpectedConditions.alertIsPresent());
        String text = driver.switchTo().alert().getText();
        driver.switchTo().alert().accept();

        assertTrue(text.toLowerCase().contains("user") || text.toLowerCase().contains("wrong"),
                "Expected error alert for invalid login, got: " + text);
    }

    @AfterAll
    void teardown() {
        if (driver != null) driver.quit();
    }
}
