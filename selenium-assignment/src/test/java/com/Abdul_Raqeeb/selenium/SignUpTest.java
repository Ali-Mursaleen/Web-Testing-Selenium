package com.Abdul_Raqeeb.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SignUpTest {
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
    @DisplayName("Create test user and save credentials")
    void testUserSignUp() {
        driver.get(BASE);
        driver.findElement(By.id("signin2")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sign-username")));

        String username = "qa_user_" + System.currentTimeMillis();
        String password = "Pass123!";

        driver.findElement(By.id("sign-username")).sendKeys(username);
        driver.findElement(By.id("sign-password")).sendKeys(password);
        driver.findElement(By.xpath("//button[text()='Sign up']")).click();

        // Wait for alert and accept
        wait.until(ExpectedConditions.alertIsPresent());
        String alertText = driver.switchTo().alert().getText();
        driver.switchTo().alert().accept();

        // Sign-up may show "Sign up successful." or "This user already exist." — accept either
        assertTrue(alertText.toLowerCase().contains("sign up") ||
                        alertText.toLowerCase().contains("exist") ||
                        alertText.toLowerCase().contains("successful"),
                "Unexpected alert: " + alertText);

        // Save credentials to disk for later tests to use
        TestUtils.saveCredentials(username, password);
    }

    @AfterAll
    void teardown() {
        if (driver != null) driver.quit();
    }
}
