package com.apicarv.testCarver.uitestrunner;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.WebDriverBackedEmbeddedBrowser;
import com.crawljax.browser.WebDriverBrowserBuilder;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.Plugins;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.apicarv.testCarver.utils.DriverProvider;

public class InstrumentedBrowserProvider implements Provider<EmbeddedBrowser>{
	private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverBrowserBuilder.class);
	private static final boolean SYSTEM_OFFLINE = false;
	private final CrawljaxConfiguration configuration;
	private final Plugins plugins;

	@Inject
	public InstrumentedBrowserProvider(CrawljaxConfiguration configuration, Plugins plugins) {
		this.configuration = configuration;
		this.plugins = plugins;
	}
	
	@Override
	public EmbeddedBrowser get() {
		ImmutableSortedSet<String> filterAttributes =
				configuration.getCrawlRules().getPreCrawlConfig().getFilterAttributeNames();
		long crawlWaitReload = configuration.getCrawlRules().getWaitAfterReloadUrl();
		long crawlWaitEvent = configuration.getCrawlRules().getWaitAfterEvent();

		WebDriver driverChrome = DriverProvider.getInstance().getDriver(); 
		
		EmbeddedBrowser browser = WebDriverBackedEmbeddedBrowser.withDriver(driverChrome, filterAttributes,
				crawlWaitEvent, crawlWaitReload);
		
		plugins.runOnBrowserCreatedPlugins(browser);

		return browser;
	}
	
}
