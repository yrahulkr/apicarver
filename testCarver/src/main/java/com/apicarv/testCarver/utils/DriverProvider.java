package com.apicarv.testCarver.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apicarv.testCarver.apirecorder.NetworkLogger;
import com.apicarv.testCarver.apirecorder.UIActionListener;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
//import org.openqa.selenium.devtools.v93.network.Network;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.events.EventFiringDecorator;

import io.github.bonigarcia.wdm.WebDriverManager;

public class DriverProvider {

	private static boolean HEADLESS_BROWSER = false;
	public static boolean PERF_LOGS = false;
	private static DriverProvider ourInstance = new DriverProvider();
	public static String pageLoadScript = null;
	private DevTools chromeDevTools = null;

	private DriverProvider() {
		WebDriverManager.chromedriver().setup();
		System.setProperty("webdriver.chrome.silentOutput", "true");
		Logger.getLogger("org.openqa.selenium.remote").setLevel(Level.OFF);
	}

	public static DriverProvider getInstance() {
		return ourInstance;
	}

	public WebDriver getDriver() {
		ChromeOptions chromeOptions = new ChromeOptions();
		if (HEADLESS_BROWSER) {
			chromeOptions.addArguments("--no-sandbox", "--headless", "--disable-gpu", "--window-size=1200x600");
//			chromeOptions.addArguments("--headless");
		}

		if (PERF_LOGS) {
			// DesiredCapabilities cap = new DesiredCapabilities();
			// cap.setCapability(CapabilityType.BROWSER_NAME, "CHROME");

			LoggingPreferences logPrefs = new LoggingPreferences();

			logPrefs.enable(LogType.PERFORMANCE, Level.ALL);

			// cap.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

			// Map<String, Object> perfLogPrefs = new HashMap<String, Object>();
			//
			// perfLogPrefs.put("traceCategories", "browser,devtools.timeline,devtools"); //
			// comma-separated trace categories
			//
			// ChromeOptions options = new ChromeOptions();
			//
			// options.setExperimentalOption("perfLoggingPrefs", perfLogPrefs);
			//
			// caps.setCapability(ChromeOptions.CAPABILITY, options);
			chromeOptions.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

		}

		ChromeDriver driver = new ChromeDriver(chromeOptions);

		return configureWebDriver(driver);
	}

	public WebDriver configureWebDriver(ChromeDriver driver) {
		if (pageLoadScript != null) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("source", pageLoadScript);
			driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", parameters);
		}

		if (Settings.LOG_NETWORK) {
			chromeDevTools = driver.getDevTools();
			NetworkLogger.addNetworkListeners(chromeDevTools);
		}
		
		if(Settings.DRIVER_LISTENER) {
//			WebDriver  = driver;
			EventFiringDecorator decorator = new EventFiringDecorator(new UIActionListener());
			WebDriver driver2 = decorator.decorate(driver);
			return driver2;
		}
		return driver;
	}



	/**
	 * true for headless
	 * 
	 * @param b
	 * @return
	 */
	public WebDriver getDriver_simple(boolean headless) {
		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.addArguments("--no-sandbox", "--disable-gpu", "--window-size=1200x600");

		if (headless) {
			chromeOptions.addArguments("--headless");
		}
		return new ChromeDriver(chromeOptions);
	}

	/*public void closeDevtools() {
		if (chromeDevTools != null)
			chromeDevTools.send(Network.disable());
	}*/

}
