package tests.booker;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.OnUrlFirstLoadPlugin;

public class bookerManualPlugin implements OnUrlFirstLoadPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(bookerManualPlugin.class.getName());

	@Override
	public void onUrlFirstLoad(CrawlerContext context) {
		WebDriver driver = context.getBrowser().getWebDriver();

		runManualTest(driver);
	}

	public static void runManualTest(WebDriver driver) {
		bookRoom(driver, 2, 4);

		bookRoom(driver, 5, 7);

		login(driver);

		createReport(driver, 8, 10);


		createReport(driver, 21, 25);

		logout(driver);
	}

	static void logout(WebDriver driver) {
		try {
			driver.navigate().to("http://localhost:8080/#/admin/");
			driver.findElement(By.linkText("Logout")).click();
		}catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.error("Error loggin out");
		}
	}
	
	static void bookRoom(WebDriver driver, int firstIndex, int secondIndex) {
		try {
			driver.navigate().to("http://localhost:8080/#/");
			driver.findElement(By.className("openBooking")).click();
			Thread.sleep(2000);

			WebElement bookButton = driver.findElement(By.xpath("//*[@id=\"root\"]/div[2]/div/div[5]/div/div[2]/div[3]/button[2]"));
//			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", bookButton);
			
			handleCalendarDrag(driver, firstIndex, secondIndex, 120);
			Thread.sleep(2000);
			
			driver.findElement(By.name("firstname")).sendKeys("John");
			driver.findElement(By.name("lastname")).sendKeys("Doherty");
			driver.findElement(By.name("phone")).sendKeys("123123123123");
			driver.findElement(By.name("email")).sendKeys("email@email.com");
			
			bookButton.click();
			Thread.sleep(2000);
			
		}catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.error("Error bookign room for {} {}", firstIndex, secondIndex);
		}
	}

	static void createReport(WebDriver driver, int firstIndex, int secondIndex) {
		try {
			driver.navigate().to("http://localhost:8080/#/admin/report");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			handleCalendarDrag(driver, firstIndex, secondIndex, 100);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			driver.findElement(By.name("firstname")).sendKeys("jd1");
			driver.findElement(By.name("lastname")).sendKeys("dj1");
			new Select(driver.findElement(By.id("roomid"))).selectByValue("1");
			driver.findElement(By.xpath("/html/body/div[4]/div/div/div[4]/div/button[2]")).click();;
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.error(" report failed");
		}
	}

	static void login(WebDriver driver) {
		try {
			driver.navigate().to("http://localhost:8080/");
			driver.findElement(By.xpath("//*[@id=\"collapseBanner\"]/div/div[3]/div[2]/button")).click();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			driver.navigate().to("http://localhost:8080/#/admin");
			driver.findElement(By.id("username")).sendKeys("admin");
			driver.findElement(By.id("password")).sendKeys("password");
			driver.findElement(By.id("doLogin")).click();
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
	
	public static void dragAndDropElement(WebElement dragFrom, WebElement dragTo, int yOffset)
			throws Exception {
			        //Setup robot
			        Robot robot = new Robot();
			        robot.setAutoDelay(200);

			        //Fullscreen page so selenium coordinates work
			      /*  robot.keyPress(KeyEvent.VK_META);
			        robot.delay(200);
			        robot.keyPress(KeyEvent.VK_CONTROL);
			        robot.keyPress(KeyEvent.VK_F);
			        robot.delay(500);
			        robot.keyRelease(KeyEvent.VK_F);
			        robot.keyRelease(KeyEvent.VK_CONTROL);
			        robot.keyRelease(KeyEvent.VK_META);
*/
			        Thread.sleep(2000);

			        //Get size of elements
			        org.openqa.selenium.Dimension fromSize = dragFrom.getSize();
			        org.openqa.selenium.Dimension toSize = dragTo.getSize();
			        LOGGER.info("sizes {} - {}", toSize, fromSize);
			        //Get centre distance
			        int xCentreFrom = fromSize.width / 2;
			        int yCentreFrom = fromSize.height / 2;
			        int xCentreTo = toSize.width / 2;
			        int yCentreTo = toSize.height / 2;

			        //Get x and y of WebElement to drag to
			        org.openqa.selenium.Point toLocation1 = dragTo.getLocation();
			        org.openqa.selenium.Point fromLocation1 = dragFrom.getLocation();
			        
			        org.openqa.selenium.Point toLocation = ((Locatable)dragTo).getCoordinates().inViewPort();
			        org.openqa.selenium.Point fromLocation = ((Locatable)dragFrom).getCoordinates().inViewPort();
			        
			        LOGGER.info("coordinates {} - {}", toLocation, fromLocation);

			        LOGGER.info("locatable coordinates {} - {}", toLocation1, fromLocation1);
			        //Make Mouse coordinate centre of element
			        toLocation.x += xCentreTo;
			        toLocation.y += yCentreTo + yOffset;
			        fromLocation.x += xCentreFrom;
			        fromLocation.y += yCentreFrom + yOffset;

			        //Move mouse to drag from location
			        robot.mouseMove(fromLocation.x, fromLocation.y);
			        robot.delay(1000);
			        //Click and drag
			        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			        robot.delay(1000);

			        //Drag events require more than one movement to register
			        //Just appearing at destination doesn't work so move halfway first
			        robot.mouseMove(((toLocation.x - fromLocation.x) / 2) + fromLocation.x, ((toLocation.y
			- fromLocation.y) / 2) + fromLocation.y);

			        //Move to final position
			        robot.mouseMove(toLocation.x, toLocation.y);

			        //Drop
			        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			        robot.delay(1000);
			        
		 
	}
	
	
	static void handleCalendarDrag(WebDriver driver, int firstIndex, int secondIndex, int yOffset) throws Exception{
		System.out.println("handling calendar drag");
		
		List<WebElement> draggableCells = driver.findElements(By.className("rbc-day-bg"));
		/*Random random = new Random();
		int firstIndex = random.nextInt(draggableCells.size());
		
		int secondIndex = random.nextInt(draggableCells.size());
		
		while(firstIndex==secondIndex) {
			secondIndex = random.nextInt(draggableCells.size());
		}*/
		/*int firstIndex = 2;
		int secondIndex = 3;
		*/
		WebElement a = draggableCells.get(firstIndex);
		WebElement b = draggableCells.get(secondIndex);
/*		Actions action = new Actions(driver);
//		action.clickAndHold(firstElement).moveToElement(secondElement)
//		.release(secondElement).build().perform();
//		action.dragAndDrop(firstElement, secondElement).perform();
		int x1 = ((Locatable)a).getCoordinates().inViewPort().x;
		int y1 = a.getLocation().y;
		int x2 = b.getLocation().x;
        int y2 = b.getLocation().y;
		
        
		action.moveToElement(a)
        .pause(Duration.ofSeconds(1))
        .clickAndHold(a)
        .pause(Duration.ofSeconds(1))
        .moveByOffset(x, y)
        .moveToElement(b)
        .moveByOffset(x,y)
        .pause(Duration.ofSeconds(1))
        .release().build().perform();
		action.dragAndDrop(a, b).release().perform();
//		action.dragAndDropBy(a, x2-x1, y2-y1).release().perform();;
//		 Robot robot = new Robot();
//		    robot.keyPress(KeyEvent.VK_ESCAPE);
//		    robot.keyRelease(KeyEvent.VK_ESCAPE);
		JavascriptExecutor js = (JavascriptExecutor)driver;
				js.executeScript("function createEvent(typeOfEvent) {\n" + "var event =document.createEvent(\"CustomEvent\");\n"
				                    + "event.initCustomEvent(typeOfEvent,true, true, null);\n" + "event.dataTransfer = {\n" + "data: {},\n"
				                    + "setData: function (key, value) {\n" + "this.data[key] = value;\n" + "},\n"
				                    + "getData: function (key) {\n" + "return this.data[key];\n" + "}\n" + "};\n" + "return event;\n"
				                    + "}\n" + "\n" + "function dispatchEvent(element, event,transferData) {\n"
				                    + "if (transferData !== undefined) {\n" + "event.dataTransfer = transferData;\n" + "}\n"
				                    + "if (element.dispatchEvent) {\n" + "element.dispatchEvent(event);\n"
				                    + "} else if (element.fireEvent) {\n" + "element.fireEvent(\"on\" + event.type, event);\n" + "}\n"
				                    + "}\n" + "\n" + "function simulateHTML5DragAndDrop(element, destination) {\n"
				                    + "var dragStartEvent =createEvent('dragstart');\n" + "dispatchEvent(element, dragStartEvent);\n"
				                    + "var dropEvent = createEvent('drop');\n"
				                    + "dispatchEvent(destination, dropEvent,dragStartEvent.dataTransfer);\n"
				                    + "var dragEndEvent = createEvent('dragend');\n"
				                    + "dispatchEvent(element, dragEndEvent,dropEvent.dataTransfer);\n" + "}\n" + "\n"
				                    + "var source = arguments[0];\n" + "var destination = arguments[1];\n"
				                    + "simulateHTML5DragAndDrop(source,destination);", a, b);
				*/
		LOGGER.info("Drag drop done between {} and {}", a.getLocation(), b.getLocation());

		dragAndDropElement(a, b, yOffset);
	}


}
