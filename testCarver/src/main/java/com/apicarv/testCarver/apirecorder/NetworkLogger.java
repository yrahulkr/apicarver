package com.apicarv.testCarver.apirecorder;

import java.util.Optional;

import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v101.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkLogger {
	private static final Logger logger = LoggerFactory.getLogger(NetworkLogger.class);

	public static void addNetworkListeners(DevTools chromeDevTools) {
		NetworkEventParser networkLogs = NetworkEventParser.getInstance(); 
		chromeDevTools.createSession();

		chromeDevTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
		chromeDevTools.addListener(Network.requestWillBeSent(), entry -> {
			networkLogs.parseEvent(entry, chromeDevTools);
			logger.debug("Request URI : " + entry.getRequest().getUrl() + "\n" + " With method : "
					+ entry.getRequest().getMethod() + "\n" + " With method : " + entry.getRequest().getHeaders()
					+ "\n");
			entry.getRequest().getMethod();
		});
		chromeDevTools.addListener(Network.requestWillBeSentExtraInfo(), entry -> {
			networkLogs.parseEvent(entry, chromeDevTools);
			logger.debug(
					"Request URI : " + entry.getRequestId() + "\n" + " With method : " + entry.getHeaders() + "\n");
			entry.getRequestId();
		});

		chromeDevTools.addListener(Network.responseReceived(), entry -> {
			networkLogs.parseEvent(entry, chromeDevTools);
			logger.debug("Response URI : " + entry.getResponse().getUrl() + "\n" + " With method : "
					+ entry.getResponse().getHeadersText() + "\n");
			entry.getResponse().getHeadersText();
		});
		chromeDevTools.addListener(Network.responseReceivedExtraInfo(), entry -> {
			networkLogs.parseEvent(entry, chromeDevTools);
			logger.debug("Response URI : " + entry.getRequestId() + "\n" + " With method : "
					+ entry.getHeadersText() + "\n");
			entry.getHeadersText();
		});
		
		chromeDevTools.addListener(Network.loadingFinished(), entry -> {
			networkLogs.parseEvent(entry, chromeDevTools);
		});
		
		chromeDevTools.addListener(Network.loadingFailed(), entry -> {
			networkLogs.parseEvent(entry, chromeDevTools);
		});
		
		/*chromeDevTools.addListener(Network.dataReceived(), entry -> {
			networkLogs.parseEvent(entry, chromeDevTools);
		});*/
	}
}
