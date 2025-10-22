package com.Abdul_Raqeeb.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LogoutTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE = "https://www.demoblaze.com";

    @BeforeAll
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test
    @DisplayName("Login using saved credentials and then logout")
    public void testLogout() {
        String[] creds = TestUtils.readCredentials();
        if (creds == null) {
            fail("No credentials found. Run SignUpTest first (it saves credentials to test-credentials.txt).");
        }
        String username = creds[0];
        String password = creds[1];

        driver.get(BASE);

        // Open login modal
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login2"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginusername")));

        // Enter credentials and submit
        driver.findElement(By.id("loginusername")).sendKeys(username);
        driver.findElement(By.id("loginpassword")).sendKeys(password);
        driver.findElement(By.xpath("//button[text()='Log in']")).click();

        // Wait for either welcome text or an alert (login failure)
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.id("nameofuser")),
                    ExpectedConditions.alertIsPresent()
            ));
        } catch (TimeoutException te) {
            fail("Neither welcome text nor login alert appeared after login attempt.");
        }

        // If alert present -> login failed; capture text and fail test
        try {
            Alert a = driver.switchTo().alert();
            String alertText = a.getText();
            a.accept();
            fail("Login failed with alert: " + alertText);
            return;
        } catch (NoAlertPresentException ignored) {
            // no alert, continue
        } catch (UnhandledAlertException e) {
            // unexpected other alert
            try {
                String txt = driver.switchTo().alert().getText();
                driver.switchTo().alert().accept();
                fail("Login failed with unexpected alert: " + txt);
                return;
            } catch (Exception ignored2) {}
        }

        // Verify login welcome name contains username (some sites show part of it)
        WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nameofuser")));
        assertTrue(welcome.getText().toLowerCase().contains(username.toLowerCase()),
                "Welcome text should contain username. Found: " + welcome.getText());

        // Click logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout2")));
        logout.click();

        // After logout, login button should reappear
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login2")));
        assertTrue(driver.findElement(By.id("login2")).isDisplayed(), "Login button should be visible after logout.");
    }

    @AfterAll
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}
