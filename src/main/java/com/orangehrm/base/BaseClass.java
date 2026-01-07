
package com.orangehrm.base;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.orangehrm.actiondriver.ActionDriver;
import com.orangehrm.utilities.ExtentManager;
import com.orangehrm.utilities.LoggerManager;

public class BaseClass {

	// protected static WebDriver driver;
	protected static Properties prop;
	// private static ActionDriver actionDriver;

	private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
	private static ThreadLocal<ActionDriver> actionDriver = new ThreadLocal<>();

	public static final Logger logger = LoggerManager.getLogger(BaseClass.class);

	@BeforeSuite
	// Load config.properties
	public void loadConfig() throws IOException {

		prop = new Properties();
		FileInputStream fis = new FileInputStream(
				System.getProperty("user.dir") + "\\src\\main\\resources\\config.properties");
		prop.load(fis);
		logger.info("config.properties file loaded.");

		// Start the Extent Report
		// ExtentManager.getReporter(); --This has been implemented in TestListener
	}

	@BeforeMethod
	public synchronized void setup() throws IOException {
		System.out.println("Setting up WebDriver for:" + this.getClass().getSimpleName());
		launchBrowser();
		configureBrowser();
		staticWait(2);
		logger.info("WebDriver initialized and Browser Maximized");
		logger.trace("This is a Trace message");
		logger.error("This is a Error message");
		logger.debug("This is a debug message");
		logger.fatal("This is a fatal message");
		logger.warn("This is a warn message");

		/*
		 * // Initialize the actionDriver only once if (actionDriver == null) {
		 * actionDriver = new ActionDriver(driver);
		 * logger.info("ActionDriver instance is created. " +
		 * Thread.currentThread().getId()); }
		 */

		// Initialize ActionDriver for the current Thread
		actionDriver.set(new ActionDriver(getDriver()));
		logger.info("ActionDriver initialized for thread: " + Thread.currentThread().getId());
	}

	// Initialize the WebDriver based on browser defined in config.properties file
	private synchronized void launchBrowser() {

		String browser = prop.getProperty("browser");
		String url = prop.getProperty("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");
		System.setProperty("webdriver.chrome.driver",
				"C:\\Users\\HP\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");

		// Browser setup
		if (browser.equalsIgnoreCase("chrome")) {
			// driver = new ChromeDriver();
			// Create ChromeOptions
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--headless"); // Run chrome in headless mode
			options.addArguments("--disable-gpu"); // Disable GPU for headless mode
			// options.addArguments("--window-size=1920,1080"); // Set window size
			options.addArguments("--disable-notifications"); // Disable browser notifications
			options.addArguments("--no sandbox"); // Required for some CI environments like Jenkins
			options.addArguments("--disable-dev-shm-usage"); // Resolve issues in resources-limited environments

			// driver = new ChromeDriver();
			driver.set(new ChromeDriver()); // New Changes as per Thread
			ExtentManager.registerDriver(getDriver());
			logger.info("ChromeDriver Instance is created");

		} else if (browser.equalsIgnoreCase("firefox")) {

			// Create FirefoxOptions
			FirefoxOptions options = new FirefoxOptions();
			options.addArguments("--headless"); // Run firefox in headless mode
			options.addArguments("--disable-gpu"); // Disable GPU rendering (useful for headless mode)
			options.addArguments("--width=1920"); // Set browser width
			options.addArguments("--height=1080"); // Set browser height
			options.addArguments("--disable-notifications"); // Disable browser notifications
			options.addArguments("--no-sandbox"); // Needed for CI/CD environments
			options.addArguments("--disable-dev-shm-usage"); // Prevent crsahes in low-resource environments

			// driver = new FirefoxDriver();
			driver.set(new FirefoxDriver()); // New Changes as per Thread
			ExtentManager.registerDriver(getDriver());
			logger.info("FirefoxDriver Instance is created");
		} else if (browser.equalsIgnoreCase("edge")) {

			// Create EdgeOptions
			EdgeOptions options = new EdgeOptions();
			options.addArguments("--headless"); // Run Edge in headless mode
			options.addArguments("--disable-gpu"); // Disable GPU accelerations
			options.addArguments("--window-size-1920,1080"); // Set window size
			options.addArguments("--disable-notifications"); // Disable browser notifications
			options.addArguments("--no-sandbox"); // Needed for CI/CD
			options.addArguments("disable-dev-shm-usage"); // Prevent resource-limited crashes

			// driver = new EdgeDriver();
			driver.set(new EdgeDriver()); // New Changes as per Thread
			ExtentManager.registerDriver(getDriver());
			logger.info("EdgeDriver Instance is created");
		} else {
			throw new IllegalArgumentException("Browser Not Supported: " + browser);
		}
	}

	// Configure browser settings such as implicit wait, maximize the browser and
	// navigate to the URL
	private void configureBrowser() {
		// Implicit Wait
		int implicitWait = Integer.parseInt(prop.getProperty("implicitWait"));
		getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));

		// Maximize window
		getDriver().manage().window().maximize();

		// Navigate to URL
		try {
			getDriver().get("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");
		} catch (Exception e) {
			System.out.println("Failed to Navigate to the URL:" + e.getMessage());
		}
	}

	@AfterMethod
	public synchronized void tearDown() {
		if (getDriver() != null) {
			try {
				getDriver().quit();
			} catch (Exception e) {
				System.out.println("Unable to quit the driver:" + e.getMessage());
			}
		}
		logger.info("WebDriver instance is closed.");
		driver.remove();
		actionDriver.remove();
		// driver = null;
		// actionDriver = null;
		// ExtentManager.endTest(); --This has been implemented in TestListener
	}

	/*
	 * // Driver getter method public WebDriver getDriver() { return driver; }
	 */

	// Getter Method for WebDriver
	public static WebDriver getDriver() {
		if (driver.get() == null) {
			System.out.println("WebDriver is not initialized");
			throw new IllegalStateException("WebDriver is not initialized");
		}
		return driver.get();
	}

	// Getter Method for ActionDriver
	public static ActionDriver getActionDriver() {
		if (actionDriver.get() == null) {
			System.out.println("ActionDriver is not initialized");
			throw new IllegalStateException("ActionDriver is not initialized");
		}
		return actionDriver.get();
	}

	// Getter method for prop
	public static Properties getProp() {
		return prop;
	}

	// Driver setter method
	public void setDriver(ThreadLocal<WebDriver> driver) {
		this.driver = driver;
	}

	// Static wait for pause
	public void staticWait(int seconds) {
		LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(seconds));
	}
}
