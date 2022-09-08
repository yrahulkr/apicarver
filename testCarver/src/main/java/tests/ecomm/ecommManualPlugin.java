package tests.ecomm;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.OnUrlFirstLoadPlugin;
import com.apicarv.testCarver.utils.DriverProvider;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.apicarv.testCarver.utils.UtilsSelenium.*;

public class ecommManualPlugin implements OnUrlFirstLoadPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(ecommManualPlugin.class.getName());

	@Override
	public void onUrlFirstLoad(CrawlerContext context) {
		WebDriver driver = context.getBrowser().getWebDriver();
		runManualTest(driver);
	}

	public static void runManualTest(WebDriver driver) {
		login(driver);
		addToGallery(driver);
		addNewCategory(driver, "New Category", "New category description", "https://upload.wikimedia.org/wikipedia/commons/e/e7/Intel_80486DX2_bottom.jpg");
		editCategory(driver, "http://localhost:8081/admin/category/1", "Edited Category", "Edited Category Description", "https://upload.wikimedia.org/wikipedia/commons/e/e7/Intel_80486DX2_bottom.jpg");
		editCategory(driver, "http://localhost:8081/admin/category/2", "Edited Category 2", "Edited Category Description 2", "https://upload.wikimedia.org/wikipedia/commons/e/e7/Intel_80486DX2_bottom.jpg");
//
		addNewProduct(driver, "new product", "new product description", "https://upload.wikimedia.org/wikipedia/commons/e/e7/Intel_80486DX2_bottom.jpg", "New Category", "100");
		editProduct(driver, "http://localhost:8081/admin/product/1", "editedName", "edited product description", "https://upload.wikimedia.org/wikipedia/commons/e/e7/Intel_80486DX2_bottom.jpg", "Shoes", "100");
		editProduct(driver, "http://localhost:8081/admin/product/2", "editedName 2", "edited product description 2", "https://upload.wikimedia.org/wikipedia/commons/e/e7/Intel_80486DX2_bottom.jpg", "Television", "100");

		addProductToCart(driver, "http://localhost:8081/product/show/1");
		removeProductFromCart(driver);

		addProductToCart(driver, "http://localhost:8081/product/show/2");
		removeProductFromCart(driver);

		logout(driver);
	}

	@Test
	public void dummyTest(){
		WebDriver driver = DriverProvider.getInstance().getDriver();
		runManualTest(driver);
		driver.close();
	}

	static void logout(WebDriver driver) {
		try {
//			driver.navigate().to("http://localhost:8081/signin");
//			driver.findElement(By.linkText("Sign Out")).click();
			click(driver, driver.findElement(By.xpath("/html/body/div[1]/nav/div/ul/li[2]/a")));
			click(driver, driver.findElement(By.xpath("//*[@id=\"navbarSupportedContent\"]/ul/li[2]/div/a[3]")));;
			click(driver, driver.findElement(By.xpath("/html/body/div[2]/div/div[3]/div/button")));;
		}catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.error("Error loggin out");
		}
	}
	

	static void login(WebDriver driver) {
		try {
			driver.navigate().to("http://localhost:8081/signin");

			sendKeys(driver, driver.findElement(By.xpath("//*[@id=\"signin-div\"]/form/div[1]/input")), "admin@gmail.com");
			sendKeys(driver, driver.findElement(By.xpath("//*[@id=\"signin-div\"]/form/div[2]/input")),"admin");
			click(driver, driver.findElement(By.xpath("//*[@id=\"signin-div\"]/form/button")));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.error("Login failed");
		}
	}



	static void editProduct(WebDriver driver, String productURL, String prodName, String prDesc, String prURL, String prCategory, String prPrice){
		try {
			driver.navigate().to(productURL);

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			selectByVisibleText(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/div[1]/select")), prCategory);

			//product name
			sendKeys(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/div[2]/input")), prodName);

			//description
			sendKeys(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/div[3]/input")), prDesc);

			// image url
			sendKeys(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/div[4]/input")), prURL);

			// product price
			sendKeys(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/div[5]/input")), prPrice);

			click(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/button")));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// Click OK on alert
			click(driver, driver.findElement(By.xpath("/html/body/div[2]/div/div[3]/div/button")));

		}catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.error("Add to gallery failed");
		}
	}

	static void addNewProduct(WebDriver driver, String prodName, String prDesc, String prURL, String prCategory, String prPrice){
		try {
			driver.navigate().to("http://localhost:8081/admin/product/add");

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			selectByVisibleText(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/div[1]/select")), prCategory);

			//product name
			sendKeys(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/div[2]/input")), prodName);

			//description
			sendKeys(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/div[3]/input")), prDesc);

			// image url
			sendKeys(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/div[4]/input")), prURL);

			// product price
			sendKeys(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/div[5]/input")), prPrice);

			click(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/button")));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// Click OK on alert
			click(driver, driver.findElement(By.xpath("/html/body/div[2]/div/div[3]/div/button")));

		}catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.error("Add to gallery failed");
		}
	}

	static void addToGallery(WebDriver driver) {
		try {
			driver.navigate().to("http://localhost:8081/admin/gallery");

			click(driver, driver.findElement(By.xpath("//*[@id=\"add-image\"]")));

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			sendKeys(driver, driver.findElement(By.id("myfile")), new File("src/main/resources/dummy.png").getAbsolutePath());
			click(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/button")));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.error("Add to gallery failed");
		}
	}

	static void addNewCategory(WebDriver driver, String catName, String catDesc, String catURL) {
		try {
			driver.navigate().to("http://localhost:8081/admin/category");
			driver.navigate().to("http://localhost:8081/admin/category/add");

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			//product name
			sendKeys(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/div[1]/input")), catName);

			//description
			sendKeys(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/div[2]/input")), catDesc);
			// image url
			sendKeys(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/div[3]/input")), catURL);

			click(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/button")));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			click(driver, driver.findElement(By.xpath("/html/body/div[2]/div/div[3]/div/button")));

		}catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.error("Add to gallery failed");
		}
	}

	static void editCategory(WebDriver driver, String categoryURL, String catName, String catDesc, String catURL) {
		try {
			driver.navigate().to(categoryURL);

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			//product name
			sendKeys(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/div[1]/input")), catName);

			//description
			sendKeys(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/div[2]/input")), catDesc);
			// image url
			sendKeys(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/div[3]/input")), catURL);

			click(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[2]/form/button")));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			click(driver, driver.findElement(By.xpath("/html/body/div[2]/div/div[3]/div/button")));

		}catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.error("Add to gallery failed");
		}
	}



	static void addProductToCart(WebDriver driver, String productURL){
		try {
			driver.navigate().to(productURL);

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			click(driver, driver.findElement(By.id("wishlist-button")));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			click(driver, driver.findElement(By.id("add-to-cart-button")));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}


			click(driver, driver.findElement(By.xpath("/html/body/div[2]/div/div[3]/div/button")));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			click(driver, driver.findElement(By.id("show-cart-button")));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			click(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[3]/button")));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			click(driver, driver.findElement(By.id("proceed-to-checkout")));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.error("Add to gallery failed");
		}
	}

	static void removeProductFromCart(WebDriver driver){
		try {
			driver.navigate().to("http://localhost:8081/cart");

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			click(driver, driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[2]/div[3]/div/a")));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}



		}catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.error("Add to gallery failed");
		}
	}


}
