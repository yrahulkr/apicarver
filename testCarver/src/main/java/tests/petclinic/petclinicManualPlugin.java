package tests.petclinic;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.OnUrlFirstLoadPlugin;

public class petclinicManualPlugin implements OnUrlFirstLoadPlugin{
	 public static void runManualTest(WebDriver driver) throws Exception {
		 	System.out.println("Starting manual test");
		    driver.get("http://localhost:8080/");
		    Thread.sleep(200);
		    driver.findElement(By.xpath("/html/body/app-root/div[1]/nav/div/ul/li[2]/a")).click();
		    Thread.sleep(200);
		    driver.findElement(By.xpath("/html/body/app-root/div[1]/nav/div/ul/li[2]/ul/li[1]/a/span[2]")).click();
		    Thread.sleep(200);
		    driver.findElement(By.xpath("//*[@id=\"ownersTable\"]/table/tbody/tr[1]/td[1]/a")).click();
		    Thread.sleep(200);
		    driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Edit Owner'])[1]/following::button[1]")).click();
		    Thread.sleep(200);
		    driver.findElement(By.id("name")).click();
		    Thread.sleep(200);
		    driver.findElement(By.id("name")).clear();
		    Thread.sleep(200);
		    driver.findElement(By.id("name")).sendKeys("new pet 2");
		    Thread.sleep(200);
		    driver.findElement(By.name("birthDate")).clear();
		    Thread.sleep(200);
		    driver.findElement(By.name("birthDate")).sendKeys("2022/04/04");
		    Thread.sleep(200);
		    driver.findElement(By.id("type")).click();
		    Thread.sleep(200);
		    new Select(driver.findElement(By.id("type"))).selectByVisibleText("bird");
		    driver.findElement(By.xpath("//button[@type='submit']")).click();
		    Thread.sleep(200);
		    // pet added
		    
		    driver.findElement(By.xpath("/html/body/app-root/app-owner-detail/div/div/table[2]/tr/app-pet-list[2]/table/tr/td[1]/dl/button[3]")).click();
		    Thread.sleep(200);
		    driver.findElement(By.name("date")).click();
		    Thread.sleep(200);
		    driver.findElement(By.name("date")).clear();
		    Thread.sleep(200);
		    driver.findElement(By.name("date")).sendKeys("2022/05/05");
		    Thread.sleep(200);
		    driver.findElement(By.id("description")).click();
		    Thread.sleep(200);
		    driver.findElement(By.id("description")).clear();
		    Thread.sleep(200);
		    driver.findElement(By.id("description")).sendKeys("first visit");
		    Thread.sleep(200);
		    driver.findElement(By.xpath("//button[@type='submit']")).click();
		    Thread.sleep(200);
		    
		    // Visit added
//		    driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='first visit'])[1]/following::button[1]")).click();
		    driver.findElement(By.xpath("/html/body/app-root/app-owner-detail/div/div/table[2]/tr/app-pet-list[2]/table/tr/td[1]/dl/button[3]")).click();
		    Thread.sleep(200);
		    driver.findElement(By.xpath("//button[@type='submit']")).click();
		    Thread.sleep(200);
		    driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='first visit'])[1]/following::button[1]")).click();
		    Thread.sleep(200);
		    driver.findElement(By.id("description")).click();
		    Thread.sleep(200);
		    driver.findElement(By.id("description")).clear();
		    Thread.sleep(200);
		    driver.findElement(By.id("description")).sendKeys("first visit 2");
		    Thread.sleep(200);
		    driver.findElement(By.xpath("//button[@type='submit']")).click();
		    Thread.sleep(200);
		    // visit edit
		    driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Delete Pet'])[1]/following::button[1]")).click();
		    Thread.sleep(200);
		    driver.findElement(By.name("date")).click();
		    Thread.sleep(200);
		    driver.findElement(By.name("date")).clear();
		    Thread.sleep(200);
		    driver.findElement(By.name("date")).sendKeys("2022/04/20");
		    Thread.sleep(200);
		    driver.findElement(By.id("description")).clear();
		    Thread.sleep(200);
		    driver.findElement(By.id("description")).sendKeys("second visit");
		    Thread.sleep(200);
		    driver.findElement(By.xpath("//button[@type='submit']")).click();
		    Thread.sleep(200);
		    driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='second visit'])[1]/following::button[1]")).click();
		    Thread.sleep(200);
		    driver.findElement(By.id("description")).click();
		    Thread.sleep(200);
		    driver.findElement(By.id("description")).clear();
		    Thread.sleep(200);
		    driver.findElement(By.id("description")).sendKeys("wew second visit");
		    Thread.sleep(200);
		    driver.findElement(By.xpath("//button[@type='submit']")).click();
		    Thread.sleep(200);
		    // second visit
	
		    // visit delete
		    driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Edit Visit'])[1]/following::button[1]")).click();
		    Thread.sleep(200);
		    driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Edit Visit'])[1]/following::button[1]")).click();
		    Thread.sleep(200);
		    // pet delete
		    driver.findElement(By.xpath("/html/body/app-root/app-owner-detail/div/div/table[2]/tr/app-pet-list[2]/table/tr/td[1]/dl/button[2]")).click();
		    Thread.sleep(200);
	 }
	
	 @Override
	public void onUrlFirstLoad(CrawlerContext arg0) {
		 try {
			runManualTest(arg0.getBrowser().getWebDriver());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
