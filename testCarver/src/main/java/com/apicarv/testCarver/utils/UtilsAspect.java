package com.apicarv.testCarver.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.apicarv.testCarver.apirecorder.AspectLogEntry;
import com.apicarv.testCarver.apirecorder.NetworkEvent;
import org.apache.commons.io.FileUtils;
import org.aspectj.lang.JoinPoint;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import com.assertthat.selenium_shutterbug.core.Shutterbug;
import com.assertthat.selenium_shutterbug.utils.web.ScrollStrategy;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.shooting.SimpleShootingStrategy;
import ru.yandex.qatools.ashot.shooting.ViewportPastingDecorator;



public class UtilsAspect {

	/**
	 * return an identifier for the statement in the form <testname>-<line> from a
	 * joinPoint of type WebElement
	 * 
	 * @param joinPoint
	 * @return String
	 */
	public static String getStatementNameFromJoinPoint(JoinPoint joinPoint) {

		String name = "";

		name = joinPoint.getStaticPart().getSourceLocation().getFileName().replace(".java", "");
		name = name.concat("_");
		name = name.concat(Integer.toString(joinPoint.getStaticPart().getSourceLocation().getLine()));

		return name;
	}

	/**
	 * return the statement line from a joinPoint of type WebElement
	 * 
	 * @param joinPoint
	 * @return int
	 */
	public static int getStatementLineFromJoinPoint(JoinPoint joinPoint) {
		return joinPoint.getStaticPart().getSourceLocation().getLine();
	}

	/**
	 * creates a directory in the project workspace
	 * 
	 * @param joinPoint
	 * @return int
	 */
	public static void createTestFolder(String path) {

		File theDir = new File(path);
		if (!theDir.exists()) {

			if (Settings.VERBOSE)
				System.out.print("[LOG]\tcreating directory " + path + "...");

			boolean result = theDir.mkdirs();
			if (result) {
				if (Settings.VERBOSE)
					System.out.println("done");
			} else {
				if (Settings.VERBOSE)
					System.out.print("failed!");
				System.exit(1);
			}
		}

	}
	

	/**
	 * save an HTML file of the a WebDriver instance
	 * 
	 * @param d
	 * @param filePath
	 */
	public static void saveDOM(WebDriver d, String filePath) {

//		try {
			saveDom(d.getPageSource(), filePath);
//			FileUtils.writeStringToFile(new File(filePath), d.getPageSource());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

	}
	

	private static void saveDom(String dom, String htmlPath) {

		try {
			FileUtils.writeStringToFile(new File(htmlPath), dom);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save rendered webpage path = where to save the html file
	 */
	public static File saveHTMLPage(String urlString, String path) throws IOException {

		File savedHTML = new File(path);

		/* necessary to avoid garbage. */
		if (savedHTML.exists()) {
			FileUtils.deleteDirectory(savedHTML);
		}

		/* wget to save html page. */
		Runtime runtime = Runtime.getRuntime();
		Process p = runtime.exec("/usr/local/bin/wget -p -k -E -nd -P " + path + " " + urlString);

		try {
			p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return savedHTML;
	}

	public static void registerEventAfter(WebDriver driver, String stateName, String imagePath, String htmlPath) {
		/*StateVertex newState = TraceSession.getInstance().newState(stateName);
		saveDom(newState.getDom(), htmlPath);
		if(newState instanceof HybridStateVertexImpl) {
			saveImage(((HybridStateVertexImpl)newState).getImage(), imagePath);
		}
		else {*/
			saveScreenshot(driver, imagePath);
//		}
	}


	private static void saveImage(BufferedImage image, String imagePath) {
		try {
			ImageIO.write(image, "PNG", new File(imagePath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void registerEventBefore(String statementName, WebElement elem) {
//		TraceSession.getInstance().recordEvent(statementName, elem);
		System.out.println("Event Before");
	}
	
	public static void registerNav(String stateName) {
//		TraceSession.getInstance().newNavState(stateName);
		System.out.println("Navigation Event");
	}
	
	public static List<NetworkEvent> getNetworkLogs(WebDriver d, LogEntries allLogs){
		   // "Network.response", "Network.request", or "Network.webSocket"
		List<NetworkEvent> returnList = new ArrayList<>();
		Gson gson = new Gson();
	    for(LogEntry logEntry : allLogs) {
//	    	System.out.println(logEntry.toJson().get("message"));
	    	TypeToken listType = new TypeToken<LinkedTreeMap<String, Object>>() {};
	    	LinkedTreeMap<String, Object> logJson = gson.fromJson(logEntry.getMessage(), listType.getType());
	    	LinkedTreeMap<String, Object> logMethod = (LinkedTreeMap<String, Object>)logJson.get("message");
		    LinkedTreeMap<String, Object> params = (LinkedTreeMap<String, Object>)logMethod.get("params");
		    String requestMethod = null;
	    	boolean networkEntry = false;
	    	
//	    	System.out.println(key + ":" + logMethod.get(key));
			String method = (String)logMethod.get("method");
			String urlString = null;
			String requestString = null;
			String dataResponse = null;
			
			switch (method.toLowerCase()) {
//			case "network.datareceived":
			case "network.responsereceived":
				networkEntry= true;
				requestString  = "response";
				urlString = "url";
				break;
			case "network.requestwillbesent":
				networkEntry= true;
				requestString = "request";
				urlString = "url";
				try {
					requestMethod = (String) ((LinkedTreeMap<String, Object>)params.get(requestString)).get("method");
					if(requestMethod.equalsIgnoreCase("post")) {
						/*String request_id = (String)params.get("requestId");
						System.out.println(request_id);
						Map<String, Object> parms = new HashMap<>();
						parms.put("requestId", request_id);
						dataResponse = ((ChromeDriver)d).executeCdpCommand("Network.getRequestPostData", parms);*/
						dataResponse = (String) ((LinkedTreeMap<String, Object>)params.get(requestString)).get("postData");
					}
				}catch(Exception ex) {
					ex.printStackTrace();
				}
				break;
			case "network.websocketcreated":
				networkEntry = true;
				requestString = null;
				urlString = "url";
				break;
			default:
				break;
			}
	    			
			
			if(networkEntry) {
				String request_id = (String)params.get("requestId");
				Map<String, Object> response = null;
				String requestUrl = null;
				try {
//				    ["params"]["requestId"]
					response = ((LinkedTreeMap<String, Object>)params.get(requestString));
				    requestUrl = (String)response.get(urlString);
//					("Network.getResponseBody", {"requestId": request_id}))
				}
				catch(Exception ex) {
					ex.printStackTrace();
					Map<String, Object> parms = new HashMap<>();
					parms.put("requestId", request_id);

					response = ((ChromeDriver)d).executeCdpCommand("Network.getResponseBody", parms);
					requestUrl = response.toString();
				}
				
				
				
				NetworkEvent newLog = new AspectLogEntry(NetworkEvent.getMethodClazz(method), requestUrl, response);
				newLog.setData(dataResponse);
				returnList.add(newLog);
	    	}

	    	
	    }
//		        log = json.loads(entry["message"])["message"]
//		        if (
//		                "Network.response" in log["method"]
//		                or "Network.request" in log["method"]
//		                or "Network.webSocket" in log["method"]
//		        ):
//		            yield log
		return returnList;

	}

	public static void saveLogs(WebDriver d, String htmlPath) {
		LogEntries allLogs = d.manage().logs().get(LogType.PERFORMANCE);
		List<NetworkEvent> networkLogs = UtilsAspect.getNetworkLogs(d, allLogs);
		
		try {
			Map<String, Object> parms = new HashMap<>();
			Map<String, Object> cookies = ((ChromeDriver)d).executeCdpCommand("Network.getAllCookies", parms);
//			System.out.println(cookies);
			NetworkEvent cookieLog = new AspectLogEntry(NetworkEvent.getMethodClazz("Cookies"), "", cookies);
			networkLogs.add(cookieLog);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		Gson gson = new Gson();
		
		FileWriter writer;
		try {
			writer = new FileWriter(new File(htmlPath));
			writer.write(gson.toJson(networkLogs));
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static BufferedImage getScreenShotAsBufferedImage(WebDriver driver, int pixelDensity, int scrollTime) throws Exception {

		if (pixelDensity != -1) {
			// BufferedImage img = Shutterbug.shootPage(getWebDriver(),
			// ScrollStrategy.WHOLE_PAGE_CHROME,true).getImage();
			BufferedImage img = Shutterbug
					.shootPage(driver, ScrollStrategy.BOTH_DIRECTIONS, scrollTime, true)
					.getImage();
			BufferedImage resizedImage = new BufferedImage(img.getWidth() / pixelDensity,
					img.getHeight() / pixelDensity, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = resizedImage.createGraphics();
			g.drawImage(img, 0, 0, img.getWidth() / pixelDensity,
					img.getHeight() / pixelDensity,
					Color.WHITE, null);
			g.dispose();
			return resizedImage;
		}

		try {
			ShootingStrategy pasting =
					new ViewportPastingDecorator(new SimpleShootingStrategy())
							.withScrollTimeout(scrollTime);
			return pasting.getScreenshot(driver);

		} catch (IllegalStateException e) {
			Thread.currentThread().interrupt();
			throw new Exception(e);
		}
	}
	
	public static void saveScreenshot(WebDriver browser, String screenshotBefore) {
		try {
			BufferedImage image = getScreenShotAsBufferedImage(browser, Settings.PIXEL_DENSITY, 500);
			ImageIO.write(image, "PNG", new File(screenshotBefore));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
