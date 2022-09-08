package tests.petclinic;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.apicarv.testCarver.utils.DriverProvider;

public class TestManual {
	@Test 
	public void testManual() throws Exception {
		WebDriver driver = DriverProvider.getInstance().getDriver();
		 driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		 petclinicManualPlugin.runManualTest(driver);
		 driver.close();
	}
}
