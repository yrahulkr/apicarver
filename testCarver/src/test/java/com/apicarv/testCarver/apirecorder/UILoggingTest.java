package com.apicarv.testCarver.apirecorder;

import static org.testng.Assert.assertTrue;

import com.apicarv.testCarver.utils.DriverProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

public class UILoggingTest {

	WebDriver driver;

	@Before
	public void setUp() throws Exception {
		UIActionLogger.getInstance().reset();
		System.out.println("Starting Driver");
		driver = DriverProvider.getInstance().getDriver();
	}

	@Test
	public void testUIListener() {
		UIActionLogger actionLogger = UIActionLogger.getInstance();

		driver.navigate().to("http://localhost:8080");

		assertTrue(actionLogger.getUiActions().size() == 2);
		assertTrue(actionLogger.getLatestBeforeAction().getActionType() == UIAction.ActionType.nav);
		assertTrue(actionLogger.getLatestAfterAction().getActionType() == UIAction.ActionType.nav);

	}
	
	@Test
	public void testUIListener2() {
		UIActionLogger actionLogger = UIActionLogger.getInstance();

		driver.navigate().to("http://localhost:8080");

		assertTrue(actionLogger.getUiActions().size() == 2);
		assertTrue(actionLogger.getLatestBeforeAction().getActionType() == UIAction.ActionType.nav);
		assertTrue(actionLogger.getLatestAfterAction().getActionType() == UIAction.ActionType.nav);

	}

	@After
	public void tearDown() {
		System.out.println("Closing Driver");
		driver.close();
	}

}
