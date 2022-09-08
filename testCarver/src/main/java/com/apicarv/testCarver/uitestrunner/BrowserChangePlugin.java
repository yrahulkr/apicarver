package com.apicarv.testCarver.uitestrunner;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
//import org.openqa.selenium.devtools.v93.network.Network;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.plugin.OnBrowserCreatedPlugin;
import com.apicarv.testCarver.utils.DriverProvider;

public class BrowserChangePlugin implements OnBrowserCreatedPlugin {

	DevTools chromeDevTools;
	//Only works for Chrome
	ChromeDriver driver;
	
	@Override
	public void onBrowserCreated(EmbeddedBrowser newBrowser) {
		driver = (ChromeDriver) newBrowser.getWebDriver();
//		if (DriverProvider.pageLoadScript != null) {
//			Map<String, Object> parameters = new HashMap<>();
//			parameters.put("source", DriverProvider.pageLoadScript);
//			driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", parameters);
//		}
//
//		if (Settings.LOG_NETWORK) {
//			chromeDevTools = driver.getDevTools();
//			NetworkLogger.addNetworkListeners(chromeDevTools);
//		}
		
		WebDriver decorateddriver = DriverProvider.getInstance().configureWebDriver(driver);
		
	}

	/*@Override
	public void onBrowserClose(CrawlerContext context) {
		if (chromeDevTools != null && driver!=null)
			chromeDevTools.send(Network.disable());
	}
	*/
}
