package com.apicarv.testCarver.apirecorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v101.network.Network;
import org.openqa.selenium.devtools.v101.network.Network.GetResponseBodyResponse;
import org.openqa.selenium.devtools.v101.network.model.DataReceived;
import org.openqa.selenium.devtools.v101.network.model.LoadingFailed;
import org.openqa.selenium.devtools.v101.network.model.LoadingFinished;
import org.openqa.selenium.devtools.v101.network.model.RequestWillBeSent;
import org.openqa.selenium.devtools.v101.network.model.RequestWillBeSentExtraInfo;
import org.openqa.selenium.devtools.v101.network.model.ResponseReceived;
import org.openqa.selenium.devtools.v101.network.model.ResponseReceivedExtraInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apicarv.testCarver.apirecorder.NetworkEvent.EventClazz;
import com.apicarv.testCarver.apirecorder.NetworkEvent.MethodClazz;
import com.apicarv.testCarver.utils.Settings;

public class NetworkEventParser {
	private static final Logger LOG = LoggerFactory.getLogger(NetworkEventParser.class);

	List<LogEntry> logEntries;

	List<LogEntry> logEntriesToUpdate;

	public List<LogEntry> getLogEntries() {
		return logEntries;
	}

	private static NetworkEventParser currentInstance;

	public static NetworkEventParser getInstance() {
		if (currentInstance == null)
			currentInstance = new NetworkEventParser();
		return currentInstance;
	}

	private NetworkEventParser() {
		logEntries = new ArrayList<LogEntry>();
		logEntriesToUpdate = new ArrayList<>();
	}

	public void newCrawlEvent() {
		synchronized (CrawlStateLogger.getInstance()) {
			synchronized (logEntriesToUpdate) {
				for (LogEntry toUpdate : this.logEntriesToUpdate) {
					toUpdate.setCrawlStateAfter(CrawlStateLogger.getInstance().getLatestEvent().getCrawlStateAfter());
					toUpdate.setEvent(CrawlStateLogger.getInstance().getLatestEvent().getEvent());
					toUpdate.setCandidateGroup(CrawlStateLogger.getInstance().getLatestEvent().getCandidateGroup());
				}

				logEntriesToUpdate = new ArrayList<>();
			}
		}
	}

	private static AtomicInteger ID = new AtomicInteger(0);

	public void parseEvent(Object event, DevTools chromeDevTools) {
		LogEntry logEntry = new LogEntry(ID.getAndIncrement());

		if (Settings.DRIVER_LISTENER) {
			// Set UI Action
			UIAction before = UIActionLogger.getInstance().getLatestBeforeAction();
			UIAction after = UIActionLogger.getInstance().getLatestAfterAction();

			logEntry.setUiActionBefore(before);
			logEntry.setUiActionAfter(after);

		} else {
			synchronized (CrawlStateLogger.getInstance()) {
				// System.out.println("setting" +
				// CrawlStateLogger.getInstance().getCrawlStateBefore());
				logEntry.setCrawlStateBefore(CrawlStateLogger.getInstance().getLatestEvent().getCrawlStateAfter());
			}
			synchronized (logEntriesToUpdate) {
				logEntriesToUpdate.add(logEntry);
			}
		}

		String clazz = event.getClass().getSimpleName();
		EventClazz clazzE = NetworkEvent.getEventClazz(clazz);
		logEntry.setClazz(clazzE);
		switch (clazzE) {
		case ResponseReceived:
			parseResponseReceived((ResponseReceived) event, logEntry);
			break;
		case ResponseReceivedExtraInfo:
			parseResponseReceivedExtraInfo((ResponseReceivedExtraInfo) event, logEntry);
			break;
		case RequestWillBeSent:
			parseRequestWillBeSent((RequestWillBeSent) event, logEntry);
			break;
		case RequestWillBeSentExtraInfo:
			parseRequestWillBeSentExtraInfo((RequestWillBeSentExtraInfo) event, logEntry);
			break;
		case DataReceived:
			parseDataReceived((DataReceived) event, logEntry);
			break;
		case LoadingFailed:
			parseLoadingFailed((LoadingFailed) event, logEntry);
			break;
		case LoadingFinished:
			parseLoadingFinished((LoadingFinished) event, logEntry, chromeDevTools);
			break;
		}
		// System.out.println(logEntry.getCrawlStateBefore());
		logEntries.add(logEntry);
	}

	private void parseLoadingFinished(LoadingFinished event, LogEntry logEntry, DevTools chromeDevTools) {
		logEntry.setRequestId(event.getRequestId().toString());
		try {
			GetResponseBodyResponse responseBody = chromeDevTools.send(Network.getResponseBody(event.getRequestId()));
			logEntry.setData(responseBody.getBody());
			LOG.info("Response body found for {}", event.getRequestId());
		} catch (DevToolsException de) {
			LOG.error("No response body for {} ", event.getRequestId());
		}
	}

	private void parseLoadingFailed(LoadingFailed event, LogEntry logEntry) {
		logEntry.setRequestId(event.getRequestId().toString());
		logEntry.setMessage(event.getErrorText());
	}

	private void parseDataReceived(DataReceived event, LogEntry logEntry) {
		logEntry.setRequestId(event.getRequestId().toString());
	}

	public void parseResponseReceived(ResponseReceived event, LogEntry logEntry) {
		setHeaders(logEntry, event.getResponse().getHeaders().entrySet());

		String requestUrl = event.getResponse().getUrl();
		String method = (String) event.getResponse().getHeaders().get("method");
		logEntry.setRequestUrl(requestUrl);
		logEntry.setMethod(NetworkEvent.getMethodClazz(method));
		String requestId = event.getRequestId().toString();
		logEntry.setRequestId(requestId);
		logEntry.setResourceType(event.getResponse().getMimeType());

		logEntry.setMonoTimeStamp(event.getTimestamp());
		logEntry.setMessage(event.getResponse().getStatus() + "");
	}

	public void parseResponseReceivedExtraInfo(ResponseReceivedExtraInfo event, LogEntry logEntry) {
		setHeaders(logEntry, event.getHeaders().entrySet());

		String requestUrl = "";
		String method = "";
		logEntry.setRequestUrl(requestUrl);
		logEntry.setMethod(NetworkEvent.getMethodClazz(method));
		String requestId = event.getRequestId().toString();
		logEntry.setRequestId(requestId);
		// logEntry.setTimeStamp(event.getTimestamp());

	}

	public void parseRequestWillBeSent(RequestWillBeSent event, LogEntry logEntry) {
		setHeaders(logEntry, event.getRequest().getHeaders().entrySet());

		String requestUrl = event.getRequest().getUrl();
		MethodClazz method = NetworkEvent.getMethodClazz(event.getRequest().getMethod());
		try {
			if ((method == MethodClazz.POST || method == MethodClazz.PUT || method == MethodClazz.PATCH)
					&& event.getRequest().getHasPostData().get()) {
				// logEntry.setPostData(event.getRequest().getPostDataEntries().get());
				String data = event.getRequest().getPostData().get();
				logEntry.setPostData(data);
				// logEntry.setData(data);
			}
		} catch (Exception ex) {
			// ex.printStackTrace();
			LOG.info("Error getting post data");
		}
		logEntry.setRequestUrl(requestUrl);
		logEntry.setMethod(method);
		String requestId = event.getRequestId().toString();
		logEntry.setRequestId(requestId);
		logEntry.setResourceType(event.getRequest().getMixedContentType().toString());
		logEntry.setMonoTimeStamp(event.getTimestamp());
	}

	private void setHeaders(LogEntry logEntry, Set<Entry<String, Object>> response) {
		List<Header> retResponse = new ArrayList<>();
		for (Entry<String, Object> entry : response) {
			LOG.debug(entry.toString());
			retResponse.add(new BasicHeader(entry.getKey(), (String) entry.getValue()));
		}
		logEntry.setHeaders(retResponse);
	}

	public void parseRequestWillBeSentExtraInfo(RequestWillBeSentExtraInfo event, LogEntry logEntry) {
		setHeaders(logEntry, event.getHeaders().entrySet());
		String requestUrl = "";
		String method = "";
		logEntry.setRequestUrl(requestUrl);
		logEntry.setMethod(MethodClazz.UNKNOWN);
		String requestId = event.getRequestId().toString();
		logEntry.setRequestId(requestId);
		// logEntry.setTimeStamp(event.getTimestamp());
	}

}
