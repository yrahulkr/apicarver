package tests.ecomm;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawlRules.FormFillMode;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.apicarv.testCarver.uitestrunner.CrawlingRules;

import java.util.concurrent.TimeUnit;

public class EcommCrawlingRules implements CrawlingRules {
	static final long WAIT_TIME_AFTER_EVENT = 1000;
	static final long WAIT_TIME_AFTER_RELOAD = 1000;

	public static final boolean visualData = true;
	public static final String URL = "http://localhost:8081";

	/**
	 * List of crawling rules for the Angular Phonecat application.
	 */
	@Override
	public void setCrawlingRules(CrawljaxConfigurationBuilder builder) {

		/* crawling rules. */
		builder.crawlRules().clickElementsInRandomOrder(false);
		builder.crawlRules().clickDefaultElements();
		builder.crawlRules().crawlHiddenAnchors(true);
		builder.crawlRules().crawlFrames(false);
		builder.crawlRules().clickOnce(false);
		
		builder.crawlRules().skipExploredActions(false, 5);

		builder.crawlRules().setFormFillMode(FormFillMode.RANDOM);

		/* do not click these. */
//		builder.crawlRules().dontClickChildrenOf("div").withId("collapseBanner");
//		builder.crawlRules().dontClick("a").underXPath("//*[@id=\"root\"]/div[2]/div/div[5]/div/div[2]/div[2]/div/div[1]/span[1]/button[1]");
//		builder.crawlRules().dontClick("a").underXPath("//*[@id=\"root\"]/div[2]/div/div[5]/div/div[2]/div[2]/div/div[1]/span[1]/button[2]");
//		builder.crawlRules().dontClick("a").underXPath("//*[@id=\"root\"]/div[2]/div/div[5]/div/div[2]/div[2]/div/div[1]/span[1]/button[3]");
//		builder.crawlRules().dontClick("button").withAttribute("class", "rbc-button-link");
		/* set timeouts. */
		// builder.setUnlimitedRuntime();
		builder.setMaximumRunTime(10, TimeUnit.MINUTES);
		builder.setUnlimitedCrawlDepth();
		builder.setUnlimitedStates();
		builder.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
		builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);

		/* set browser. */
		builder.setBrowserConfig(new BrowserConfiguration(BrowserType.CHROME, 1));

		/* input data. */
		builder.crawlRules().setInputSpec(EcommCrawlingRules.bookerInputSpec());

		/* CrawlOverview. */
		builder.addPlugin(new CrawlOverview());
		builder.addPlugin(new ecommManualPlugin());
	}

    @Override
    public boolean getVisualData() {
        return visualData;
    }

    /**
	 * List of inputs to crawl the Phonecat application.
	 */
	static InputSpecification bookerInputSpec() {

		InputSpecification inputBooker = new InputSpecification();
		EcommForms.login(inputBooker);


		return inputBooker;
	}

}
