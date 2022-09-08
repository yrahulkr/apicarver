package tests.booker;

import java.util.concurrent.TimeUnit;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawlRules.FormFillMode;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.forms.FormInput.InputType;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.apicarv.testCarver.uitestrunner.CrawlingRules;

public class BookerCrawlingRules implements CrawlingRules {
	static final long WAIT_TIME_AFTER_EVENT = 1000;
	static final long WAIT_TIME_AFTER_RELOAD = 1000;

	public static final boolean visualData = true;
	public static final String URL = "http://localhost:8080";

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
		builder.crawlRules().dontClickChildrenOf("div").withId("collapseBanner");
		builder.crawlRules().dontClick("a").underXPath("//*[@id=\"root\"]/div[2]/div/div[5]/div/div[2]/div[2]/div/div[1]/span[1]/button[1]");
		builder.crawlRules().dontClick("a").underXPath("//*[@id=\"root\"]/div[2]/div/div[5]/div/div[2]/div[2]/div/div[1]/span[1]/button[2]");
		builder.crawlRules().dontClick("a").underXPath("//*[@id=\"root\"]/div[2]/div/div[5]/div/div[2]/div[2]/div/div[1]/span[1]/button[3]");
		builder.crawlRules().dontClick("button").withAttribute("class", "rbc-button-link");
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
		builder.crawlRules().setInputSpec(BookerCrawlingRules.bookerInputSpec());

		/* CrawlOverview. */
		builder.addPlugin(new CrawlOverview());
		builder.addPlugin(new bookerManualPlugin());
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
		BookerForms.detailsPage(inputBooker);
		BookerForms.login(inputBooker);

		inputBooker.inputField(InputType.TEXT, new Identification(How.name, "phone")).inputValues("123412341211", "22121212111");
		inputBooker.inputField(InputType.TEXT, new Identification(How.id, "phone")).inputValues("12341234121", "12212121211");
		inputBooker.inputField(InputType.TEXT, new Identification(How.id, "roomName")).inputValues("102", "103", "104");
		inputBooker.inputField(InputType.TEXT, new Identification(How.id, "roomPrice")).inputValues("60", "200", "120");

		inputBooker.inputField(InputType.TEXT, new Identification(How.name, "email")).inputValues("mail@mail.com", "jdoe@mail.com");
		inputBooker.inputField(InputType.TEXT, new Identification(How.id, "email")).inputValues("mail@mail.com", "jdoe@mail.com");
		inputBooker.inputField(InputType.TEXTAREA, new Identification(How.id, "description")).inputValues("This is the description of query for booking the hotel room.");
		
		
		// Room creation
		
		
		//		inputPhonecat.inputField(InputType.TEXT, new Identification(How.name, "telephone")).inputValues("3212341513", "2131231231", "7447922213");
//		inputPhonecat.inputField(InputType.TEXT, new Identification(How.name, "birthDate")).inputValues("2020-09-09", "2021-07-09", "2019-02-21");
//		inputPhonecat.inputField(InputType.TEXT, new Identification(How.name, "date")).inputValues("2021-10-10", "2020-10-10", "2019-10-10");

		return inputBooker;
	}

}
