package com.apicarv.testCarver.apirunner;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.apicarv.testCarver.apirecorder.NetworkEvent;
import com.apicarv.testCarver.apirecorder.ResponseEvent;
import com.apicarv.testCarver.utils.UtilsAPIRunner;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apicarv.testCarver.Main;
import com.apicarv.testCarver.utils.Settings.SUBJECT;
import com.apicarv.testCarver.utils.WorkDirManager;
import com.apicarv.testCarver.utils.WorkDirManager.DirType;

public class APIRunner {
	private static final Logger LOG = LoggerFactory.getLogger(APIRunner.class);

	Map<String, String> cookieCache = new HashMap<>();

	List<String> succeeded = new ArrayList<>();
	List<String> failed = new ArrayList<>();
	List<String> skipped = new ArrayList<>();

	List<APIResponse> resultResponses = null;
	WorkDirManager manager = null;

	String genPath = null;

	SUBJECT app = null;
	
	String minedFolder = null;

	private RunConfig runConfig = new RunConfig();

	/**
	 * 
	 * @param genPath
	 */
	public APIRunner(String genPath) {
		this.app = SUBJECT.dummy;
		this.minedFolder = "default";
		this.genPath = genPath;
		this.manager = new WorkDirManager(app, minedFolder, DirType.RUN, genPath);
	}

	/**
	 * 
	 * @param app
	 * @param minedFolder
	 * @param genPath
	 */
	public APIRunner(SUBJECT app, String minedFolder, String genPath) {
		this.app = app;
		this.minedFolder = minedFolder;
		this.genPath = genPath;
		this.manager = new WorkDirManager(app, minedFolder, DirType.RUN, genPath);
	}

	public List<String> getSucceeded() {
		return succeeded;
	}

	public List<String> getSkipped() {
		return skipped;
	}

	public List<APIResponse> getResultResponses() {
		return resultResponses;
	}


	public static void main(String args[]) {

		SUBJECT app = Main.getSubject(args[0]);
		String minedFolder = args[1];
		String genFolder = args[2];
		String genPath = Paths.get("/Users/apicarv/git/TestCarving/testCarver/out" , app.name() , minedFolder, "gen", genFolder).toString();
		APIRunner runner = new APIRunner(app, minedFolder, genPath);
		
		try {
			// String jsonFile = Settings.outputDir + Settings.sep +
			// Settings.DEV_TOOLS_OUTPUT_JSON;

//			runner.runGeneratedSeq();
			runner.runOriginalSeq();
			runner.getWorkdirManager().exportResultResponses(runner.getResultResponses());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * public static void getResponseBody(String url) { //
	 * given().when().get(url).then().log() // .all(); // // //
	 * given().queryParam("j_username","admin") //
	 * .queryParam("j_password","password") //
	 * .queryParam("form_token","u003dTkWdWbeuPHQ2R2NfPlANVw") //
	 * .when().get(url).then().log().body();
	 * 
	 * 
	 * given().queryParam("JSESSIONID", "EB00B9854A6672C54814A4ACC07EB599") //
	 * .queryParam("j_password","password") //
	 * .queryParam("form_token","u003dTkWdWbeuPHQ2R2NfPlANVw")
	 * .when().get("http://localhost:8080/bin/view/Main").then().log().body();
	 * 
	 * }
	 */

	public void runGeneratedSeq() throws IOException {
		if(app!=null) {
			Main.resetApp(app, true, null);
		}
		
		List<NetworkEvent> eventsToRun = manager.getGeneratedEvents();
		resultResponses = runEvents(eventsToRun);
		manager.exportResultResponses(resultResponses);
		String covFile = this.getWorkdirManager().getCovFile();
		if(app!=null) {
			Main.resetApp(app, false, covFile);
		}
		
	}

	public void runOriginalSeq() throws IOException {
		if(app!=null) {
			Main.resetApp(app, true, null);
		}

		List<NetworkEvent> eventsToRun = manager.getMinedEvents();

		resultResponses = runEvents(eventsToRun);
		manager.exportResultResponses(resultResponses);
		String covFile = this.getWorkdirManager().getCovFile();
		if(app!=null) {
			Main.resetApp(app, false, covFile);
		}

	}

	public static APIResponse.Status executeRequest(CloseableHttpClient httpClient, NetworkEvent logEntry, APIResponse returnResponse, Map<String, String> cookieCache, boolean ignoreHeaders) {
		long start = System.currentTimeMillis();

		returnResponse.setRequest(logEntry);
		returnResponse.setResponse(null);

		if (logEntry.getRequestUrl() == null) {
			System.out.println("Skipping empty url : " + logEntry);
			returnResponse.setStatus(APIResponse.Status.SKIPPED);
			returnResponse.setMessage("Empty URL");
//			addSkipped(logEntry.getRequestId());
			return APIResponse.Status.SKIPPED;
		}

		if (logEntry.getClazz() != NetworkEvent.EventClazz.RequestWillBeSent && logEntry.getClazz()!= NetworkEvent.EventClazz.Probe) {
			returnResponse.setStatus(APIResponse.Status.SKIPPED);
			returnResponse.setMessage("Only RequestWillBeSent or Probe handled ");
//			addSkipped(logEntry.getRequestId());
			return APIResponse.Status.SKIPPED;
		}
		try {
			HttpUriRequest request = null;
			switch (logEntry.getMethod()) {
			case GET:
				request = new HttpGet(logEntry.getRequestUrl());
				break;
			case DELETE:
				returnResponse.addCheckPoint(APIResponse.CheckPointType.OPERATION);
				request = new HttpDelete(logEntry.getRequestUrl());
				break;
			case POST:
				returnResponse.addCheckPoint(APIResponse.CheckPointType.OPERATION);
				request = new HttpPost(logEntry.getRequestUrl());
				new ArrayList<Header>();
				if (logEntry.getPostData() != null && !logEntry.getPostData().trim().isEmpty()) {
					// List<NameValuePair> params = getPostData(logEntry.getData());
					// ((HttpPost) request).setEntity(new UrlEncodedFormEntity(params));
					// ((HttpPost) request).setEntity(new
					// ByteArrayEntity(logEntry.getPostData().get(0).getBytes().get().getBytes()));
					// ((HttpPost)request).setEntity(new
					// ByteArrayEntity("j_username=admin&j_password=password".getBytes()));
					((HttpPost) request).setEntity(new StringEntity(logEntry.getPostData()));
				}
				break;
			case PUT:
				returnResponse.addCheckPoint(APIResponse.CheckPointType.OPERATION);
				request = new HttpPut(logEntry.getRequestUrl());
				new ArrayList<Header>();
				if (logEntry.getPostData() != null && !logEntry.getPostData().trim().isEmpty()) {
					// List<NameValuePair> params = getPostData(logEntry.getData());
					// ((HttpPost) request).setEntity(new UrlEncodedFormEntity(params));
					// ((HttpPost) request).setEntity(new
					// ByteArrayEntity(logEntry.getPostData().get(0).getBytes().get().getBytes()));
					// ((HttpPost)request).setEntity(new
					// ByteArrayEntity("j_username=admin&j_password=password".getBytes()));
					((HttpPut) request).setEntity(new StringEntity(logEntry.getPostData()));
				}
				break;
			case PATCH:
				request = new HttpPatch(logEntry.getRequestUrl());
				new ArrayList<Header>();
				if (logEntry.getPostData() != null && !logEntry.getPostData().trim().isEmpty()) {
					// List<NameValuePair> params = getPostData(logEntry.getData());
					// ((HttpPost) request).setEntity(new UrlEncodedFormEntity(params));
					// ((HttpPost) request).setEntity(new
					// ByteArrayEntity(logEntry.getPostData().get(0).getBytes().get().getBytes()));
					// ((HttpPost)request).setEntity(new
					// ByteArrayEntity("j_username=admin&j_password=password".getBytes()));
					((HttpPatch) request).setEntity(new StringEntity(logEntry.getPostData()));
				}
				break;
			case TRACE:
				request = new HttpTrace(logEntry.getRequestUrl());
				break;
			case OPTIONS:
				request = new HttpOptions(logEntry.getRequestUrl());
				break;
			case HEAD:
				request = new HttpHead(logEntry.getRequestUrl());
				break;
			default:
				break;
			}

			if (request == null) {
				// || !(request instanceof HttpPost)) {
				// }
				System.out.println("Skipping entry because method is unknown : " + logEntry);
				returnResponse.setStatus(APIResponse.Status.FAILURE);
				returnResponse.setMessage("Unknown Method : " + logEntry.getMethod());
			}

			for (Header key : logEntry.getHeaders()) {
				if (key.getName().equalsIgnoreCase("cookie")) {
					try {
						request.addHeader(buildCookieHeaderWithCache(key, cookieCache));
					} catch (Exception ex) {
						LOG.error("Exception updating cookie {} ", ex.getMessage());
						request.addHeader(key);
					}
				} else {
					if(!ignoreHeaders){
						request.addHeader(key);
					}
				}
			}

			LOG.info("Executing request {}", request);

			CloseableHttpResponse response = httpClient.execute(request);

			/*switch (logEntry.getMethod()) {
			case "get":
				response = httpClient.execute((HttpGet) request);
				break;
			case "post":
				response = httpClient.execute((HttpPost) request);
				break;
			default:
				break;
			}*/

			try {

				// Get HttpResponse Status
				// System.out.println(response.getAllHeaders());
				// System.out.println(response.getProtocolVersion()); // HTTP/1.1
				// System.out.println(response.getStatusLine().getStatusCode()); // 200
				// System.out.println(response.getStatusLine().getReasonPhrase()); // OK
				// System.out.println(response.getStatusLine().toString()); // HTTP/1.1 200 OK

				ResponseEvent responseLog = new ResponseEvent(0);

				HttpEntity entity = response.getEntity();
				
				if (entity != null) {
					// System.out.println(response.getHeaders("Set-Cookie").length);
					// for(Header cookieHeader: response.getHeaders("Set-Cookie")) {
					// System.out.println(cookieHeader.toString());
					// }
					// return it as a String
					try {
						String result = EntityUtils.toString(entity);
						// System.out.println(result);
	//					responseLog.setData(DomUtils.getStrippedHTML(result));
						responseLog.setData(result);
						responseLog.setResourceType(entity.getContentType().toString());
					}catch(Exception e) {
						LOG.error("Exception parsing response entity {}", entity);
					}
					/*
					if(runConfig.isExportPayload()) {
						manager.exportPayLoad(returnResponse, result);
					}*/
				}
				// Map<String, Object> responseHeaders = new HashMap<>();
				// System.out.println(response.getAllHeaders().length);
				// for (Header header : response.getAllHeaders()) {
				// System.out.println(header.toString());
				// responseHeaders.put(header.getName(), header.getValue());
				// }
				// System.out.println(responseHeaders.size());
				List<Header> responseHeaders = new ArrayList<>();
				for (Header responseHeader : response.getAllHeaders()) {
					responseHeaders.add(new BasicHeader(responseHeader.getName(), responseHeader.getValue()));
				}

				boolean cookieUpdated = updateCookieCache(responseHeaders, cookieCache);
				
				if(cookieUpdated) {
					returnResponse.addCheckPoint(APIResponse.CheckPointType.COOKIE);
				}
				responseLog.setHeaders(responseHeaders);
				responseLog.setStatus(response.getStatusLine().getStatusCode());
				responseLog.setMessage(response.getStatusLine().getReasonPhrase());
				returnResponse.setResponse(responseLog);
				returnResponse.setStatus(APIResponse.Status.SUCCESS);
//				addSucceeded(logEntry.getRequestId());
				return APIResponse.Status.SUCCESS;
			} finally {
				response.close();
			}
		} catch (Exception ex) {
			returnResponse.setStatus(APIResponse.Status.FAILURE);
			returnResponse.setMessage(ex.getMessage());
//			addFailed(logEntry.getRequestId());
			return APIResponse.Status.FAILURE;
		}finally{
			long end = System.currentTimeMillis();
			returnResponse.setDuration(end-start);
		}

	}

	private static boolean updateCookieCache(List<Header> responseHeaders, Map<String, String> cookieCache) {
		boolean updated = false;
		for (Header header : responseHeaders) {
			if (header.getName().equalsIgnoreCase("set-cookie")) {
				List<String> newCookieStrings = UtilsAPIRunner.splitCookie(header.getValue());
				for (String newCookie : newCookieStrings) {
					if (!newCookie.contains("=")) {
						LOG.info("Ignoring cookie : {}", newCookie);
						continue;
					}
					String[] newCookieSplit = newCookie.split("=");
					if (newCookieSplit.length != 2) {
						LOG.error("Unknown Cookie Format : {}", newCookie);
						continue;
					}
					cookieCache.put(newCookieSplit[0].trim(), newCookieSplit[1].trim());
					LOG.info("Updated Cookie : {}", newCookie);
					updated = true;
				}
			}
		}
		return updated;
	}

	private static Header buildCookieHeaderWithCache(Header header, Map<String, String> cookieCache) {

		LOG.info("Creating new cookie string for : {}", header.getValue());
		Header newCookieHeader = null;
		List<String> cookieStrings = UtilsAPIRunner.splitCookie(header.getValue());

		StringBuffer newCookieValue = new StringBuffer();

		for (String cookie : cookieStrings) {
			if (!cookie.contains("=")) {
				LOG.info("Ignoring cookie : {}", cookie);
				continue;
			}
			String[] cookieSplit = cookie.split("=");
			if (cookieSplit.length != 2) {
				LOG.error("Unknown Cookie Format : {}", cookie);
				continue;
			}

			String cookieCacheVal = null;
			if (cookieCache.containsKey(cookieSplit[0].trim())) {
				cookieCacheVal = cookieCache.get(cookieSplit[0].trim());
				LOG.info("Updating  value for {} using cache {}", cookieSplit[0].trim(), cookieCacheVal);
			} else {
				LOG.info("No update found for {} in cache", cookieSplit[0]);
				cookieCacheVal = cookieSplit[1].trim();
			}

			String newCookieString = cookieSplit[0].trim() + "=" + cookieCacheVal;

			LOG.info("Changed Cookie {} -> {}", cookie, newCookieString);

			newCookieValue.append(newCookieString);
			newCookieValue.append(";");
		}

		newCookieHeader = new BasicHeader("Cookie", newCookieValue.toString());

		LOG.info("Returning new cookie header with value: {}", newCookieValue.toString());

		return newCookieHeader;
	}


	public List<APIResponse> runEvents(List<NetworkEvent> events) throws IOException {

		resultResponses = new ArrayList<>();

		if (events == null || events.isEmpty()) {
			System.err.println("Invalid argument: " + events + " Provide non empty list of events to execute");
			return null;
		}

		boolean ignoreHeaders = this.app==SUBJECT.tmf? true: false;

		List<NetworkEvent> logEntries = events;
		// new ArrayList<>();
		// logEntries.add(allLogEntries.get(0));

		CloseableHttpClient httpClient = null;
		try {
			httpClient = getCloseableHttpClient();
		} catch (NoSuchAlgorithmException | KeyStoreException| KeyManagementException e) {
			LOG.error("Error creating HTTP Client");
			e.printStackTrace();
			return null;
		}
		try {
			for (int i = 0; i < logEntries.size(); i++) {
				/*
				 * ID for the APIResponse object is the same as index of the API being run
				 */
				NetworkEvent logEntry = logEntries.get(i);
				APIResponse returnResponse = new APIResponse(i);
				resultResponses.add(returnResponse);
				APIResponse.Status status = executeRequest(httpClient, logEntry, returnResponse, cookieCache, ignoreHeaders);
				switch(status) {
				case SKIPPED:
					addSkipped(logEntry.getRequestId());
					break;
				case FAILURE:
					addFailed(logEntry.getRequestId());
					break;
				case SUCCESS:
					addSucceeded(logEntry.getRequestId());

					if(runConfig.isExportPayload() && returnResponse.response.getData()!=null) {
						manager.exportPayLoad(returnResponse, returnResponse.response.getData());
					}
					break;
					
				default:
					break;
				}
				
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			LOG.error("Exception while executing log entries");
		} finally {
			httpClient.close();
		}

		LOG.info("Failed API {}", failed);
		LOG.info("Succeeded API {}", succeeded);
		LOG.info("Total {}", succeeded.size());
		return resultResponses;
	}

	public static CloseableHttpClient getCloseableHttpClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		CloseableHttpClient httpClient = HttpClients
				.custom()
				.setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
				.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
				.build();
		return httpClient;
	}

	public void addSucceeded(String requestId) {
		succeeded.add(requestId);
	}

	public void addSkipped(String requestId) {
		skipped.add(requestId);
	}

	public void addFailed(String requestId) {
		failed.add(requestId);
	}

	public WorkDirManager getWorkdirManager() {
		return manager;
	}

}
