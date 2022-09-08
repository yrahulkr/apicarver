package tests.booker;

import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.apicarv.testCarver.utils.DriverProvider;

public class BookerTests {

	@Test
	public void testDrag() throws Exception {
		WebDriver driver = DriverProvider.getInstance().getDriver();
//		driver.manage().window().maximize();
//		driver.manage().window().fullscreen();
//		driver.manage().window().fullscreen();
//		Thread.sleep(1000);
		bookerManualPlugin manual = new bookerManualPlugin();
		manual.bookRoom(driver, 0, 2);
		
		manual.bookRoom(driver, 3, 6);
		
		manual.login(driver);
		
		manual.createReport(driver, 8, 10);
		
		
		manual.createReport(driver, 21, 25);
		
		manual.logout(driver);
		driver.close();
	}
}
