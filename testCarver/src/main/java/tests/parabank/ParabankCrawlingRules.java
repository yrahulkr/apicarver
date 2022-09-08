package tests.parabank;

import java.util.concurrent.TimeUnit;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawlRules.FormFillMode;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.fragmentation.FragmentRules;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.apicarv.testCarver.uitestrunner.CrawlingRules;

public class ParabankCrawlingRules implements CrawlingRules {
	public static final boolean visualData = false;
    public static final String URL = "http://localhost:8080/parabank-3.0.0-SNAPSHOT/index.htm";
	static final long WAIT_TIME_AFTER_EVENT = 1000;
	static final long WAIT_TIME_AFTER_RELOAD = 1000;

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
/*		builder.crawlRules().dontClick("a").underXPath("//*[@id=\"headerPanel\"]/ul[1]/li[6]/a");
		builder.crawlRules().dontClick("a").withText("Admin Page");
		builder.crawlRules().dontClick("a").underXPath("//*[@id=\"rightPanel\"]/ul[1]/li[2]/a");
		builder.crawlRules().dontClick("a").underXPath("//*[@id=\"rightPanel\"]/ul[1]/li[3]/a");
		builder.crawlRules().dontClick("a").underXPath("//*[@id=\"rightPanel\"]/ul[1]/li[4]/a");
		builder.crawlRules().dontClick("a").underXPath("//*[@id=\"rightPanel\"]/ul[1]/li[5]/a");*/
		builder.crawlRules().dontClickChildrenOf("div").withId("footerPanel");
		builder.crawlRules().dontClickChildrenOf("div").withId("topPanel");
		builder.crawlRules().dontClickChildrenOf("div").withId("headerPanel");
//		builder.crawlRules().dontClickChildrenOf("div").withId("rightPanel");
		builder.crawlRules().dontClickChildrenOf("ul").withClass("services");
		builder.crawlRules().dontClickChildrenOf("ul").withClass("servicestwo");
		builder.crawlRules().dontClick("a").underXPath("/html/body/div[1]/div[3]/div[2]/p[1]/a");
		builder.crawlRules().dontClick("a").withText("Log Out");

		/* set timeouts. */
		// builder.setUnlimitedRuntime();
		builder.setMaximumRunTime(10, TimeUnit.MINUTES);
		builder.setUnlimitedCrawlDepth();
		builder.setUnlimitedStates();
		builder.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
		builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);
		builder.crawlRules().setUsefulFragmentRules(new FragmentRules(50, 50, 2, 2));
		/* set browser. */
		builder.setBrowserConfig(new BrowserConfiguration(BrowserType.CHROME, 1));

		/* input data. */
		builder.crawlRules().setInputSpec(ParabankCrawlingRules.parabankInputSpecification());

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
		ParabankForms.newOwner(inputParabank);
		ParabankForms.findByDate(inputParabank);
		ParabankForms.findBetweenDates(inputParabank);
		ParabankForms.billPay(inputParabank);
		ParabankForms.findByAmount(inputParabank);
		ParabankForms.login(inputParabank);

		
//		inputPhonecat.inputField(InputType.TEXT, new Identification(How.name, "telephone")).inputValues("3212341513", "2131231231", "7447922213");
//		inputPhonecat.inputField(InputType.TEXT, new Identification(How.name, "birthDate")).inputValues("2020-09-09", "2021-07-09", "2019-02-21");
//		inputPhonecat.inputField(InputType.TEXT, new Identification(How.name, "date")).inputValues("2021-10-10", "2020-10-10", "2019-10-10");

		return inputParabank;
	}

}
