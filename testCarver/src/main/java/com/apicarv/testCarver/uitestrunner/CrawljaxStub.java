package com.apicarv.testCarver.uitestrunner;

import com.codahale.metrics.MetricRegistry;
import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.plugins.testcasegenerator.TestSuiteGenerator;
import com.crawljax.stateabstractions.dom.RTEDStateVertexFactory;
import com.crawljax.stateabstractions.hybrid.HybridStateVertexFactory;
import tests.booker.BookerCrawlingRules;
import tests.ecomm.EcommCrawlingRules;
import tests.jawa.JawaCrawlingRules;
import tests.medical.MedicalCrawlingRules;
import tests.parabank.ParabankCrawlingRules;
import tests.petclinic.PetclinicCrawlingRules;
import tests.realworld.RealworldCrawlingRules;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class CrawljaxStub {

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("Incorrect number of paramenters");
			CrawljaxStub.printUsage();
		}

		String app = args[0].toLowerCase();
		String saf = args[1].toUpperCase();
		String URL = null;
		double threshold = -1;
		long runtime = -1;
		int maxStates = -1;

		CrawlingRules crawlingRules = null;

		switch (app) {
		/*
		 * case "addressbook": URL = AddressbookRunner.URL; break;
		 */
		case "petclinic":
			URL = PetclinicCrawlingRules.URL;
			crawlingRules = new PetclinicCrawlingRules();
			break;
		case "parabank":
			URL = ParabankCrawlingRules.URL;
			crawlingRules = new ParabankCrawlingRules();
			break;
		case "realworld":
			URL = RealworldCrawlingRules.URL;
			crawlingRules = new RealworldCrawlingRules();
			break;
		case "booker":
			URL = BookerCrawlingRules.URL;
			crawlingRules = new BookerCrawlingRules();
			break;
		case "jawa":
			URL = JawaCrawlingRules.URL;
			crawlingRules = new JawaCrawlingRules();
			break;
		case "medical":
			URL = MedicalCrawlingRules.URL;
			crawlingRules = new MedicalCrawlingRules();
			break;
		case "ecomm":
			URL = EcommCrawlingRules.URL;
			crawlingRules = new EcommCrawlingRules();
			break;
		}

		try {
			runtime = Long.parseLong(args[2]);
		} catch (Exception Ex) {
			System.out.println("Exception while parsing time string. Please provide a valid time in minutes");
			CrawljaxStub.printUsage();
			System.exit(-1);
			;
		}
		if (args.length >= 4) {
			try {
				threshold = Double.parseDouble(args[3]);
			} catch (Exception Ex) {
				System.out.println(
						"Exception while parsing threshold string. Please provide a valid float number as threshold for the chosen SAF");
				CrawljaxStub.printUsage();
				System.exit(-1);
				;
			}
		}

		if (args.length == 5) {
			try {
				maxStates = Integer.parseInt(args[4]);
			} catch (Exception Ex) {
				System.out.println(
						"Exception while parsing maxStates string. Please provide a valid integer as max states for the crawl");
				CrawljaxStub.printUsage();
				System.exit(-1);
				;
			}
		}

		run(crawlingRules, app, saf, runtime, threshold, maxStates, URL);
	}

	public static void run(CrawlingRules crawlingRules, String app, String saf, long runtime, double threshold, int maxStates, String URL) {
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);

		boolean visualData = false;

		System.out.println("*******************************************************************");
		System.out.println(saf);
		crawlingRules.setCrawlingRules(builder);
		visualData = crawlingRules.getVisualData();

		

		if (maxStates != -1) {
			builder.setMaximumStates(maxStates);
			// runtime=60;
		}
		builder.setMaximumRunTime(runtime, TimeUnit.MINUTES);

		File customOuput = new File("crawlOut" + File.separator + app + File.separator + app + "_" + saf + "_"
				+ threshold + "_" + runtime + "mins");
		builder.setOutputDirectory(customOuput);
		CrawljaxRunner crawljax = null;

		// Generate Tests
		builder.addPlugin(new TestSuiteGenerator());
//		builder.addPlugin(new BrowserChangePlugin());
		builder.addPlugin(new APIMinerPlugin());
		
		 

		switch (saf) {

		
		case "DOM_RTED":
			builder.setStateVertexFactory(new RTEDStateVertexFactory(threshold));
			crawljax = new CrawljaxRunner(builder.build());
			break;
		
		case "HYBRID":
			builder.setStateVertexFactory(new HybridStateVertexFactory(0, builder, visualData));
			builder.setOutputDirectory(customOuput);
			crawljax = new CrawljaxRunner(builder.build());
			break;
		default:
			crawljax = new CrawljaxRunner(builder.build());

		}
		
		BrowserConfiguration browserConfig = new BrowserConfiguration(
				BrowserType.CHROME, 1,
				new InstrumentedBrowserProvider(builder.build(), new Plugins(builder.build(), new MetricRegistry()))
				);
//		browserConfig.setBrowserOptions(new BrowserOptions(2));
		builder.setBrowserConfig(browserConfig);

//		builder.setBrowserConfig(new BrowserConfiguration(BrowserType.CHROME_HEADLESS, 1, new BrowserOptions(2) ));
		
		if (crawljax != null)
			crawljax.call();
	}

	public static void printUsage() {
		System.out.println("Usage : "
				+ "runner <app{petclinic, parabank,}> \n"
				+ "<saf{, RTED, HYBRID}> \n"
				+ "<runtime(mins)> \n" + "<opt:threshold> \n" + "<opt:maxStates> \n");
	}
}
