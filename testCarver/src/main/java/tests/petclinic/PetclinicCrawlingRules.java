package tests.petclinic;

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

public class PetclinicCrawlingRules implements CrawlingRules {
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
		builder.crawlRules().crawlHiddenAnchors(false);
		builder.crawlRules().crawlFrames(false);
		builder.crawlRules().clickOnce(false);
		
//		builder.crawlRules().skipExploredActions(false, 5);

		builder.crawlRules().setFormFillMode(FormFillMode.RANDOM);

		/* do not click these. */
		builder.crawlRules().dontClick("a").withAttribute("class", "navbar-brand");
		builder.crawlRules().dontClick("button").underXPath("//mat-datepicker-toggle/button");
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
		builder.crawlRules().setInputSpec(PetclinicCrawlingRules.petclinicInputSpecification());

		/* CrawlOverview. */
		builder.addPlugin(new CrawlOverview());
		builder.addPlugin(new petclinicManualPlugin());
	}

	/**
	 * List of inputs to crawl the Phonecat application.
	 */
	static InputSpecification petclinicInputSpecification() {

		InputSpecification inputPhonecat = new InputSpecification();
		PetclinicForms.newOwner(inputPhonecat);
		PetclinicForms.findOwner(inputPhonecat);
		
		inputPhonecat.inputField(InputType.TEXT, new Identification(How.name, "telephone")).inputValues("3212341513", "2131231231", "7447922213");
		inputPhonecat.inputField(InputType.TEXT, new Identification(How.name, "birthDate")).inputValues("2020-09-09", "2021-07-09", "2019-02-21");
		inputPhonecat.inputField(InputType.TEXT, new Identification(How.name, "date")).inputValues("2021-10-10", "2020-10-10", "2019-10-10");

		return inputPhonecat;
	}

	@Override
	public boolean getVisualData() {
		return visualData;
	}
}
