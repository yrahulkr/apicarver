package tests.medical;

import com.crawljax.core.configuration.CrawlRules.FormFillMode;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.apicarv.testCarver.uitestrunner.CrawlingRules;

import java.util.concurrent.TimeUnit;

public class MedicalDoctorCrawlingRules implements CrawlingRules {
	public static final boolean visualData = true;
	public static final String URL = "http://localhost:3000";

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

		builder.crawlRules().click("div").withAttribute("class", "MuiListItem-button");
		builder.crawlRules().dontClick("div").withText("Logout");
		/* set timeouts. */
		// builder.setUnlimitedRuntime();
		builder.setUnlimitedCrawlDepth();
		builder.setUnlimitedStates();
		builder.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
		builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);

		/* set browser. */

		/* input data. */
		builder.crawlRules().setInputSpec(MedicalDoctorCrawlingRules.medicalInputSpec());

		/* CrawlOverview. */
		builder.addPlugin(new CrawlOverview());
//		builder.addPlugin(new MedicalManualPlugin());
		builder.addPlugin(new MedicalPreCrawlPlugin("doctor", "doctor1", "doctor1"));
	}

    @Override
    public boolean getVisualData() {
        return visualData;
    }

    /**
	 * List of inputs to crawl the Phonecat application.
	 */
	static InputSpecification medicalInputSpec() {

		InputSpecification inputMedical = new InputSpecification();
		MedicalForms.login(inputMedical, "//*[@id=\"scrollable-prevent-tabpanel-0\"]/form/div[1]/div/label[2]/span[1]/span[1]/input", "doctor1", "doctor1");
		return inputMedical;
	}

}
