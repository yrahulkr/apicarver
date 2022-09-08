package com.apicarv.testCarver.uitestrunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.apicarv.testCarver.utils.UtilsMiner;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apicarv.testCarver.Main;
import com.apicarv.testCarver.apirecorder.CrawlStateLogger;
import com.apicarv.testCarver.apirecorder.GenPipeLine;
import com.apicarv.testCarver.apirecorder.LogEntry;
import com.apicarv.testCarver.apirecorder.NetworkEvent;
import com.apicarv.testCarver.apirecorder.NetworkEventParser;
import com.apicarv.testCarver.apirecorder.ResponseEvent;
import com.apicarv.testCarver.apirecorder.UIActionLogger;
import com.apicarv.testCarver.apirunner.APIResponse;
import com.apicarv.testCarver.apirunner.APIResponse.Status;
import com.apicarv.testCarver.utils.Settings;
import com.apicarv.testCarver.utils.Settings.SUBJECT;
import com.apicarv.testCarver.utils.WorkDirManager;
import com.apicarv.testCarver.utils.WorkDirManager.DirType;

public class APIMiner {
	private static final Logger LOG = LoggerFactory.getLogger(APIMiner.class);

	private SUBJECT app;
	private MiningMode miningMode;

	private int runTime;

	public enum MiningMode{
		CRAWLING, MANUAL, JUNIT
	}
	
	WorkDirManager workDirManager;

	public WorkDirManager getWorkDirManager() {
		return workDirManager;
	}

	/**
	 * The runTime parameter is required for tests that use Crawljax.
	 * @param app
	 * @param miningMode
	 * @param runTime
	 */
	public APIMiner(SUBJECT app, MiningMode miningMode, int runTime) {
		this.app = app;
		this.miningMode = miningMode;
		if(miningMode == MiningMode.CRAWLING) {
			Settings.DRIVER_LISTENER = false;
			this.runTime = runTime;
			if(runTime>0){
				UtilsMiner.CRAWL_TIME = runTime;
			}
		}
	}

	public List<LogEntry> mineAPI() {
		long start = System.currentTimeMillis();

		workDirManager = new WorkDirManager(app, app.name(), DirType.MINE);
		
		if(app!=null) {
			Main.resetApp(app, true, null);
		}
		
		NetworkEventParser eventParser = NetworkEventParser.getInstance();

		Settings.currResult = new TestRunResult(miningMode, runTime);
		try {
			Optional<Description> failure = UtilsRunner.executeTests(app);
			if (failure.isPresent()) {
				Settings.currResult.setFailed(true);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Settings.currResult.setFailed(true);
			TestFailureDetail detail = new TestFailureDetail("overall", ex.toString(), ex.toString());
			Settings.currResult.addTestResult(detail);
		}
		long end = System.currentTimeMillis();
		Settings.currResult.setDuration(end - start);
		
		List<APIResponse> loggedAPIResponses = buildAPIResponses(eventParser.getLogEntries());
		
		GenPipeLine pipeline = new GenPipeLine(true, true, true, false, GenPipeLine.FILTER_TYPE.inclusion);
		
		workDirManager.exportResultResponses(loggedAPIResponses);

		workDirManager.exportMinedAPI(eventParser.getLogEntries());
		if(Settings.DRIVER_LISTENER) {
			workDirManager.exportUIActions(UIActionLogger.getInstance().getUiActions());
		}
		else {
			workDirManager.exportCrawlEvents(CrawlStateLogger.getInstance().getEventList());
		}
		workDirManager.exportTestRunResult(Settings.currResult);
		
		String covFile = workDirManager.getCovFile();
		if(app!=null) {
			Main.resetApp(app, true, covFile);
		}
		
		return eventParser.getLogEntries();

	}
	
	private List<APIResponse> buildAPIResponses(List<LogEntry> allEvents) {
		List<APIResponse> combinedEvents = new ArrayList<>();

		Map<String, List<LogEntry>> requestMap = new HashMap<>();
		// Map<String, List<NetworkEvent>> responseMap = new HashMap<>();

		for (LogEntry event : allEvents) {
			if(event == null) {
				continue;
			}
			if (!requestMap.containsKey(event.getRequestId())) {
				List<LogEntry> newRequestList = new ArrayList<LogEntry>();
				requestMap.put(event.getRequestId(), newRequestList);
			}

			requestMap.get(event.getRequestId()).add(event);
		}

		AtomicInteger atomicId = new AtomicInteger(0);
		for (String requestId : requestMap.keySet()) {
			List<LogEntry> eventsForRequest = requestMap.get(requestId);

			List<APIResponse> mainEvents = extractMainEvents(eventsForRequest, atomicId);
			if(mainEvents!=null)
				combinedEvents.addAll(mainEvents);
		}

		return combinedEvents;
	}
	private static AtomicInteger ID = new AtomicInteger(0);

	private List<APIResponse> extractMainEvents(List<LogEntry> eventsForRequest, AtomicInteger atomicInt) {

		if (eventsForRequest == null || eventsForRequest.isEmpty()) {
			LOG.error("Invalid param : {}. Provide non empty list of events containing same requestId",
					eventsForRequest);
			return null;
		}

		List<APIResponse> mainEvents = new ArrayList<APIResponse>();

		
		for (LogEntry event : eventsForRequest) {
			if (event.getClazz() == NetworkEvent.EventClazz.RequestWillBeSent) {

				Set<String> urls = mainEvents.stream().map(mainEvent->mainEvent.getRequest().getRequestUrl().toLowerCase()).collect(Collectors.toSet());
				if(!urls.contains(event.getRequestUrl().toLowerCase())){
					NetworkEvent currRequest = event.clone(ID.getAndIncrement());
					APIResponse currentMain = new APIResponse(atomicInt.getAndIncrement());
					currentMain.setStatus(Status.SUCCESS);
					currentMain.setRequest(currRequest);
					mainEvents.add(currentMain);
				}
				/*
				  
				boolean addNew = false;
				if (mainEvents.isEmpty()) {
					NetworkEvent currRequest = event.clone();
					currentMain.setRequest(currRequest);
					
					addNew = true;
				} else {
					for (APIResponse mainEvent : mainEvents) {
						if (!event.getRequestUrl().equalsIgnoreCase(mainEvent.getRequest().getRequestUrl())) {
							// TODO: Need to check if cloning works properly.
							// We need to make sure the original objects stay without getting modified by
							// these ops
							NetworkEvent currRequest = event.clone();
							currentMain.setRequest(currRequest);
							
							addNew = true;
						}
					}
				}
				
				if (addNew)
					mainEvents.add(currentMain);*/
			}
		}
		
		
		
		
		if (mainEvents.size() == 0) {
			LOG.error("No main events recorded for : {}", eventsForRequest.get(0).getRequestId());
			return null;
		}
		
		for(NetworkEvent event: eventsForRequest) {
			if (event.getClazz() == NetworkEvent.EventClazz.ResponseReceived) {
				NetworkEvent response = event.clone(ID.getAndIncrement());
				ResponseEvent responseEvent = new ResponseEvent(ID.getAndIncrement());
				responseEvent.setRequestUrl(response.getRequestUrl());
				responseEvent.setClazz(event.getClazz());
				responseEvent.setMethod(event.getMethod());
				responseEvent.setHeaders(response.getHeaders());
				responseEvent.setPostData(response.getPostData());
				responseEvent.setMessage(response.getMessage());
				responseEvent.setResourceType(event.getResourceType());
				try {
					responseEvent.setStatus(Integer.parseInt(response.getMessage()));
				}catch(Exception ex) {
					LOG.error("Cannot parse server response code {}", responseEvent.getRequestId());
				}
				for(APIResponse apiResponse: mainEvents) {
					if(apiResponse.getRequest().getRequestUrl().equalsIgnoreCase(response.getRequestUrl())) {
						apiResponse.setResponse(responseEvent);
						break;
					}
				}
			}
		}

		if (mainEvents.size() > 1) {
			return combineMultiRequests(eventsForRequest, mainEvents);
		}

		APIResponse currentMain = mainEvents.get(0);

		for (NetworkEvent event : eventsForRequest) {

			if (event.getClazz() == NetworkEvent.EventClazz.RequestWillBeSentExtraInfo) {
				try {
					currentMain.getRequest().getHeaders().addAll(event.getHeaders());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			
			if (event.getClazz() == NetworkEvent.EventClazz.ResponseReceivedExtraInfo) {
				try {
					currentMain.getResponse().getHeaders().addAll(event.getHeaders());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
			if(event.getClazz() == NetworkEvent.EventClazz.LoadingFailed) {
				currentMain.setMessage(event.getMessage());
				currentMain.setStatus(Status.FAILURE);
			}
			
			if(event.getClazz() == NetworkEvent.EventClazz.LoadingFinished && event.getData()!=null) {
				workDirManager.exportPayLoad(currentMain, event.getData());
			}
		}

		return mainEvents;
	}

	private List<APIResponse> combineMultiRequests(List<LogEntry> eventsForRequest,
			List<APIResponse> mainEvents) {
		// Heuristic - Adding extra Headers in the same order as main events
		// TODO: perform dependency analysis to figure out the relationships between
		// request and extra headers

		List<NetworkEvent> extraInfoEvents = new ArrayList<>();
		List<NetworkEvent> extraResponseInfoEvents = new ArrayList<>();
		List<NetworkEvent> loadingEvents = new ArrayList<>();

		for (NetworkEvent event : eventsForRequest) {
			if (event.getClazz() == NetworkEvent.EventClazz.RequestWillBeSentExtraInfo) {
				extraInfoEvents.add(event);
			}
			if (event.getClazz() == NetworkEvent.EventClazz.ResponseReceivedExtraInfo) {
				extraResponseInfoEvents.add(event);
			}
			if(event.getClazz() == NetworkEvent.EventClazz.LoadingFinished) {
				loadingEvents.add(event);
			}
		}
		
		for(int i=0; i< mainEvents.size(); i++) {
			if(loadingEvents.size() > i){
				workDirManager.exportPayLoad(mainEvents.get(i), loadingEvents.get(i).getData());
			}
			else if(loadingEvents.size() > 0) {
				workDirManager.exportPayLoad(mainEvents.get(i), loadingEvents.get(0).getData());
			}
		}

		boolean useIndices = true;

		if (mainEvents.size() != extraInfoEvents.size() 
				|| mainEvents.size() != extraResponseInfoEvents.size()) {
			useIndices = false;
		}

		if (useIndices) {
			LOG.info("Using INDEX-BASED_MERGE for multi-request {}", mainEvents.get(0).getRequest().getRequestId());
			for (int i = 0; i < mainEvents.size(); i++) {
				try {
					mainEvents.get(i).getRequest().getHeaders().addAll(extraInfoEvents.get(i).getHeaders());
					mainEvents.get(i).getResponse().getHeaders().addAll(extraResponseInfoEvents.get(i).getHeaders());
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} else {
			LOG.info("Using ALL-HEADER_MERGE for multi-request {}", mainEvents.get(0).getRequest().getRequestId());
			for (APIResponse currentMain : mainEvents) {
				for (NetworkEvent event : extraInfoEvents) {
					try {
						currentMain.getRequest().getHeaders().addAll(event.getHeaders());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				for (NetworkEvent event : extraResponseInfoEvents) {
					if(currentMain.getResponse() == null) {
						continue;
					}
					try {
						currentMain.getResponse().getHeaders().addAll(event.getHeaders());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		/*
		// Set Mime Type
		for(NetworkEvent event: eventsForRequest) {
			if(event.getClazz() == EventClazz.ResponseReceived) {
				for(NetworkEvent mainEvent: mainEvents) {
					if(mainEvent.getRequestUrl().equalsIgnoreCase(event.getRequestUrl())) {
						mainEvent.setResourceType(event.getResourceType());
					}
				}
			}
		}*/
		
		
		return mainEvents;
	}

	
	public static void main(String args[]) {
		int runTime = 1;
		APIMiner miner = new APIMiner(Main.getSubject("xwiki"), MiningMode.JUNIT, runTime);
		miner.mineAPI();
	}
}
