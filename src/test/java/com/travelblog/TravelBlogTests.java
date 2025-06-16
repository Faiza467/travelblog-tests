package com.travelblog;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.openqa.selenium.chrome.ChromeOptions;
import java.time.Duration;
import java.util.List;

public class TravelBlogTests {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
    System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");

    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless"); // ✅ headless mode
    options.addArguments("--no-sandbox"); // recommended for Linux
    options.addArguments("--disable-dev-shm-usage"); // avoid shared memory issues
    options.addArguments("--window-size=1920,1080"); // ensures full page rendering

    driver = new ChromeDriver(options);
    driver.manage().window().maximize();
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
}

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    public String generateRandomEmail() {
        long timestamp = System.currentTimeMillis();
        return "testuser" + timestamp + "@example.com";
    }

@Test
public void signupUser() {
    String email = generateRandomEmail();

    driver.get("http://localhost/travel-blog/signup.php");

    // No "name" field, so directly start with email
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email"))).sendKeys(email);
    driver.findElement(By.name("password")).sendKeys("password");

    // Correct XPath for button
    WebElement signupButton = driver.findElement(By.xpath("//button[contains(text(),'Sign Up')]"));
    signupButton.click();

    // Wait for redirect to login page
    wait.until(ExpectedConditions.urlContains("login.php"));

    // Verify that we're on login page
    Assertions.assertTrue(driver.getPageSource().contains("Login"), "Signup failed or no redirect to login");
}


    @Test
    public void loginUser() {
        driver.get("http://localhost/travel-blog/login.php");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email"))).sendKeys("testuser1@example.com");
        driver.findElement(By.name("password")).sendKeys("password");

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Login')]"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("index.php"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.php"), "Login failed");
    }

    public void login() {
        driver.get("http://localhost/travel-blog/login.php");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email"))).sendKeys("testuser1@example.com");
        driver.findElement(By.name("password")).sendKeys("password");
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Login')]"));
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("index.php"));
    }
    
@Test
public void addPost() throws InterruptedException {
    login();

    driver.get("http://localhost/travel-blog/add_post.php");

    wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("title"))).sendKeys("Test Post");

    driver.findElement(By.name("image_url")).sendKeys("https://...");
    driver.findElement(By.name("content")).sendKeys("Automated post content.");
    driver.findElement(By.xpath("//button[contains(text(),'Post Blog')]")).click();

    wait.until(ExpectedConditions.urlContains("index.php")); // this is enough
    Assertions.assertTrue(driver.getPageSource().contains("Test Post"), "Post was not added successfully");
}


@Test
public void deletePost() throws InterruptedException {
    login();

    driver.get("http://localhost/travel-blog/add_post.php");

    wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("title"))).sendKeys("Delete This Post");
    driver.findElement(By.name("image_url")).sendKeys("https://...");
    driver.findElement(By.name("content")).sendKeys("Post to delete");
    driver.findElement(By.xpath("//button[contains(text(),'Post Blog')]")).click();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.className("card"))); // instead of sleep

    List<WebElement> posts = driver.findElements(By.className("card"));
    boolean found = false;

    for (WebElement post : posts) {
        if (post.getText().contains("Delete This Post")) {
            WebElement deleteButton = post.findElement(By.xpath(".//button[contains(text(),'Delete')]"));
            deleteButton.click();
            found = true;
            break;
        }
    }

    Assertions.assertTrue(found, "Post to delete not found!");

    wait.until(ExpectedConditions.presenceOfElementLocated(By.className("container"))); // safe wait

    posts = driver.findElements(By.className("card"));
    boolean stillExists = posts.stream().anyMatch(n -> n.getText().contains("Delete This Post"));
    Assertions.assertFalse(stillExists, "Post was not deleted successfully!");
}


@Test
public void editPost() throws InterruptedException {
    login();

    driver.get("http://localhost/travel-blog/add_post.php");

    String originalTitle = "Original Post";
    String updatedTitle = "Edited Post";

    wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("title"))).sendKeys(originalTitle);
    driver.findElement(By.name("image_url")).sendKeys("https://...");
    driver.findElement(By.name("content")).sendKeys("Original content for editing.");
    driver.findElement(By.xpath("//button[contains(text(),'Post Blog')]")).click();

    wait.until(ExpectedConditions.urlContains("index.php"));

    List<WebElement> posts = driver.findElements(By.className("card"));

    boolean found = false;
    for (WebElement post : posts) {
        if (post.getText().contains(originalTitle)) {
            WebElement editButton = post.findElement(By.xpath(".//button[contains(text(),'Edit')]"));
            editButton.click();
            found = true;
            break;
        }
    }

    Assertions.assertTrue(found, "Original post not found for editing");

    wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("title"))).clear();
    driver.findElement(By.name("title")).sendKeys(updatedTitle);
    
    WebElement contentField = driver.findElement(By.name("content"));
    contentField.clear();
    contentField.sendKeys("Updated content after editing.");

    driver.findElement(By.xpath("//button[contains(text(),'Update Post')]")).click();

    wait.until(ExpectedConditions.urlContains("index.php"));

    Assertions.assertTrue(driver.getPageSource().contains(updatedTitle), "Post was not updated successfully");
}



@Test
public void logoutTest() throws InterruptedException {
    login(); // reusing your existing login() helper method

    // Make sure we're on home page after login
    wait.until(ExpectedConditions.urlContains("index.php"));
    Assertions.assertTrue(driver.getPageSource().contains("My Traveling Blog"), "Login failed before logout test");

    // Click the Logout button
    WebElement logoutButton = driver.findElement(By.xpath("//a[contains(text(),'Logout')]"));
    logoutButton.click();

    // Wait until redirected to login page
    wait.until(ExpectedConditions.urlContains("login.php"));

    // Verify we're back on login page
    Assertions.assertTrue(driver.getPageSource().contains("Login"), "Logout failed, still not redirected to login page");
}

@Test
public void addPostWithEmptyFields() throws InterruptedException {
    login();

    driver.get("http://localhost/travel-blog/add_post.php");

    // Leave all fields empty and submit
    WebElement submitBtn = driver.findElement(By.xpath("//button[contains(text(),'Post Blog')]"));
    submitBtn.click();

    Thread.sleep(1000);

    // Check that form still remains on the same page (i.e. not redirected)
    Assertions.assertTrue(driver.getCurrentUrl().contains("add_post.php"), "Form was submitted even with empty fields");
}

@Test
public void editPostAndCancel() throws InterruptedException {
    login();

    driver.get("http://localhost/travel-blog/add_post.php");
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("title"))).sendKeys("Edit Abort");
    driver.findElement(By.name("image_url")).sendKeys("https://...");
    driver.findElement(By.name("content")).sendKeys("Original Content");
    driver.findElement(By.xpath("//button[contains(text(),'Post Blog')]")).click();
    
    wait.until(ExpectedConditions.presenceOfElementLocated(By.className("card")));

    List<WebElement> posts = driver.findElements(By.className("card"));
    for (WebElement post : posts) {
        if (post.getText().contains("Edit Abort")) {
            WebElement editButton = post.findElement(By.xpath(".//button[contains(text(),'Edit')]"));
            editButton.click();
            break;
        }
    }

    wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("title")));

    WebElement titleField = driver.findElement(By.name("title"));
    titleField.clear();
    titleField.sendKeys("Updated Title");

    driver.navigate().back();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.className("card")));

    posts = driver.findElements(By.className("card"));
    boolean foundOriginal = posts.stream().anyMatch(n -> n.getText().contains("Edit Abort"));
    boolean foundUpdated = posts.stream().anyMatch(n -> n.getText().contains("Updated Title"));

    Assertions.assertTrue(foundOriginal, "Original title not found after canceling edit.");
    Assertions.assertFalse(foundUpdated, "Updated title appeared even though edit was canceled.");
}



@Test
public void loginWithInvalidCredentialsTest() {
    // Always open login page first
    driver.get("http://localhost/travel-blog/login.php");

    // Add wait before accessing input fields
    //WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));

    emailField.sendKeys("invalid@example.com");
    driver.findElement(By.name("password")).sendKeys("wrongpassword");
    driver.findElement(By.xpath("//button[contains(text(),'Login')]")).click();

    WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
    Assertions.assertTrue(error.getText().contains("Invalid credentials"), "Expected invalid login error message.");
}

@Test
public void loginWithEmptyFieldsTest() throws InterruptedException {
    //WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

    // Go to login page
    driver.get("http://localhost/travel-blog/login.php");

    // Wait for login page to load fully
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(),'Login')]")));

    // Don't enter anything in email/password fields

    // Click on Login button
    driver.findElement(By.xpath("//button[contains(text(),'Login')]")).click();

    Thread.sleep(2000); // No try-catch needed now

    Assertions.assertTrue(driver.getCurrentUrl().contains("login.php"), "Unexpected navigation on empty fields!");
}   
}

