package tests.jawa;

import java.util.concurrent.TimeUnit;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawlRules.FormFillMode;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.apicarv.testCarver.uitestrunner.CrawlingRules;

public class JawaCrawlingRules implements CrawlingRules {
	static final long WAIT_TIME_AFTER_EVENT = 1000;
	static final long WAIT_TIME_AFTER_RELOAD = 1000;

	public static final boolean visualData = true;
    public static final String URL = "http://localhost:8080";

    /**
	 * List of crawling rules for the Angular Phonecat application.
	 */
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
		builder.crawlRules().dontClick("a").underXPath("//*[@id=\"headerPanel\"]/ul[1]/li[6]/a");
		builder.crawlRules().dontClickChildrenOf("app-dashboard");
		builder.crawlRules().dontClick("a").withAttribute("href", "/notifications");
		builder.crawlRules().dontClick("a").withAttribute("href", "/maps");
		builder.crawlRules().dontClick("a").withAttribute("href", "/icons");
		builder.crawlRules().dontClick("a").withAttribute("href", "/typography");
		builder.crawlRules().dontClick("a").withAttribute("href", "/dashboard");
		
		

		builder.crawlRules().dontClick("a").withAttribute("id", "navbarDropdownMenuLink");
		
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
		builder.crawlRules().setInputSpec(JawaCrawlingRules.parabankInputSpecification());

		/* CrawlOverview. */
		builder.addPlugin(new CrawlOverview());
	}

    @Override
    public boolean getVisualData() {
        return visualData;
    }

    /**
	 * List of inputs to crawl the Phonecat application.
	 */
	static InputSpecification parabankInputSpecification() {

		InputSpecification inputParabank = new InputSpecification();
		JawaForms.newUser(inputParabank);
		JawaForms.findByDate(inputParabank);
		JawaForms.findBetweenDates(inputParabank);
		
		JawaForms.findByAmount(inputParabank);
		JawaForms.search(inputParabank);

		
//		inputPhonecat.inputField(InputType.TEXT, new Identification(How.name, "telephone")).inputValues("3212341513", "2131231231", "7447922213");
//		inputPhonecat.inputField(InputType.TEXT, new Identification(How.name, "birthDate")).inputValues("2020-09-09", "2021-07-09", "2019-02-21");
//		inputPhonecat.inputField(InputType.TEXT, new Identification(How.name, "date")).inputValues("2021-10-10", "2020-10-10", "2019-10-10");

		return inputParabank;
	}

}
