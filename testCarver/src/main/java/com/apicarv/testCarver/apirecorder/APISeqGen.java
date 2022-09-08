package com.apicarv.testCarver.apirecorder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apicarv.testCarver.utils.Settings.SUBJECT;
import com.apicarv.testCarver.utils.UtilsJson;
import com.apicarv.testCarver.utils.WorkDirManager;
import com.apicarv.testCarver.utils.WorkDirManager.DirType;


/*
 * Class to process raw API logs and create a runnable list of API requests
 * 
 * PIPELINE MODULES - CLEAN_HEADERS, EXCLUDE_RESOURCES
 *  
 */

public class APISeqGen {

	/**
	 * Reference for MIME types :
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types
	 */

	private static final Logger LOG = LoggerFactory.getLogger(APISeqGen.class);
	private final SUBJECT subject;

	private WorkDirManager workDirManager;
	
	private GenPipeLine genPipeLine= null;
	
	public APISeqGen(SUBJECT subject, String minedFolder, GenPipeLine genPipeLine) {
		this.subject = subject;
		this.setWorkDirManager(new WorkDirManager(subject, minedFolder, DirType.GEN));
		this.genPipeLine = genPipeLine;
	}

	public static void main(String args[]) throws IOException {
		SUBJECT subject = SUBJECT.parabank;
		String app = "20220426_144511";
		APISeqGen seqGen = new APISeqGen(subject, app, new GenPipeLine(true, true, true, true, GenPipeLine.FILTER_TYPE.inclusion));
		seqGen.generateAPISeq();
//		String genPath = "out/petclinic-20220324_012519/gen/20220324_013603";
//		APIRunner runner = new APIRunner(subject, app, genPath);
//		runner.runGeneratedSeq();
		/*try {
			runner.runEvents(genRequests);
			runner.getWorkdirManager().exportResultResponses(runner.getResultResponses());
		} catch (IOException e) {
			LOG.error("Error Generating report {}", e.getMessage());
//			e.printStackTrace();
		}*/
	}

	private List<NetworkEvent> cleanRequests(List<NetworkEvent> rawRequests) {
		for (NetworkEvent rawRequest : rawRequests) {
			List<Header> newSet = new ArrayList<>();
			for (Header header : rawRequest.getHeaders()) {
				for (String ignored : genPipeLine.getIgnoredHeaders()) {
					if (!header.getName().equalsIgnoreCase(ignored)) {
						newSet.add(header);
					}
				}
			}
			rawRequest.setHeaders(newSet);
		}

		return rawRequests;
	}
	
	public List<NetworkEvent> applyFilters(List<NetworkEvent> returnEvents){
		
		
		if(genPipeLine.isExcludeResources()) {
			returnEvents = excludeResourceRequests(returnEvents);
			if(genPipeLine.isExportIntermediate()) {
				workDirManager.exportNonResourceRequests(returnEvents);
			}
		}
		
		if(genPipeLine.isCleanHeaders()) {
			returnEvents = cleanRequests(returnEvents);
			if(genPipeLine.isExportIntermediate()) {
				workDirManager.exportCleanHeaderRequests(returnEvents);
			}
		}
		
		return returnEvents;
	}

	public List<NetworkEvent> generateAPISeq() {
		List<LogEntry> allEvents = UtilsJson.importNetworkEventLog(workDirManager.getMinedAPIJson());
		List<NetworkEvent> returnEvents = null;
		if(genPipeLine.isCombineEvents()) {
			returnEvents = combineRequestLogs(allEvents);
			if(genPipeLine.isExportIntermediate()) {
				workDirManager.exportCombinedRequests(returnEvents);
			}
		}
		else {
			LOG.error("CombineEvents has been set to false. Nothing to do!!");
			return null;
		}
		returnEvents = applyFilters(returnEvents);
		
		
		workDirManager.exportSeqGenOutput(genPipeLine, returnEvents);
		return returnEvents;
	}
	private static AtomicInteger ID = new AtomicInteger(0);

	private List<NetworkEvent> extractMainEvents(List<LogEntry> eventsForRequest, Map<Integer, Long> orderingMap) {

		if (eventsForRequest == null || eventsForRequest.isEmpty()) {
			LOG.error("Invalid param : {}. Provide non empty list of events containing same requestId",
					eventsForRequest);
			return null;
		}

		List<NetworkEvent> mainEvents = new ArrayList<NetworkEvent>();

		NetworkEvent currentMain = null;
		for (LogEntry event : eventsForRequest) {
			if (event.getClazz() == NetworkEvent.EventClazz.RequestWillBeSent) {
				boolean addNew = false;
				if (mainEvents.isEmpty()) {
					currentMain = event.clone(ID.getAndIncrement());
					addNew = true;
				} else {
					for (NetworkEvent mainEvent : mainEvents) {
						if (!event.getRequestUrl().equalsIgnoreCase(mainEvent.getRequestUrl())) {
							// TODO: Need to check if cloning works properly.
							// We need to make sure the original objects stay without getting modified by
							// these ops
							currentMain = event.clone(ID.getAndIncrement());
							addNew = true;
						}
					}
				}
				if (addNew) {
					orderingMap.put(currentMain.getId(), event.getTimeStamp());
					mainEvents.add(currentMain);
				}
			}
		}

		if (mainEvents.size() == 0) {
			LOG.error("No main events recorded for : {}", eventsForRequest.get(0).getRequestId());
			return null;
		}

		if (mainEvents.size() > 1) {
			return combineMultiRequests(eventsForRequest, mainEvents);
		}

		currentMain = mainEvents.get(0);
		if (currentMain == null) {
			System.err.println("No main event found: " + eventsForRequest.get(0).getRequestId());
			return null;
		}

		for (NetworkEvent event : eventsForRequest) {

			if (event.getClazz() == NetworkEvent.EventClazz.RequestWillBeSentExtraInfo) {
				try {
					currentMain.getHeaders().addAll(event.getHeaders());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			if (event.getClazz() == NetworkEvent.EventClazz.ResponseReceived) {
				currentMain.setResourceType(event.getResourceType());
			}
		}
		

		return mainEvents;
	}

	private List<NetworkEvent> combineMultiRequests(List<LogEntry> eventsForRequest,
			List<NetworkEvent> mainEvents) {
		// Heuristic - Adding extra Headers in the same order as main events
		// TODO: perform dependency analysis to figure out the relationships between
		// request and extra headers

		List<NetworkEvent> extraInfoEvents = new ArrayList<>();
		for (NetworkEvent event : eventsForRequest) {
			if (event.getClazz() == NetworkEvent.EventClazz.RequestWillBeSentExtraInfo) {
				extraInfoEvents.add(event);
			}
		}

		boolean useIndices = true;

		if (mainEvents.size() != extraInfoEvents.size()) {
			useIndices = false;
		}

		if (useIndices) {
			LOG.info("Using INDEX-BASED_MERGE for multi-request {}", mainEvents.get(0).getRequestId());
			for (int i = 0; i < mainEvents.size(); i++) {
				try {
					mainEvents.get(i).getHeaders().addAll(extraInfoEvents.get(i).getHeaders());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} else {
			LOG.info("Using ALL-HEADER_MERGE for multi-request {}", mainEvents.get(0).getRequestId());
			for (NetworkEvent currentMain : mainEvents) {
				for (NetworkEvent event : eventsForRequest) {

					if (event.getClazz() == NetworkEvent.EventClazz.RequestWillBeSentExtraInfo) {
						if (currentMain != null) {
							try {
								currentMain.getHeaders().addAll(event.getHeaders());
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						} else {
							System.err.println("No current main event : " + event.getRequestId());
						}
					}
				}
			}
		}
		
		// Set Mime Type
		for(NetworkEvent event: eventsForRequest) {
			if(event.getClazz() == NetworkEvent.EventClazz.ResponseReceived) {
				for(NetworkEvent mainEvent: mainEvents) {
					if(mainEvent.getRequestUrl().equalsIgnoreCase(event.getRequestUrl())) {
						mainEvent.setResourceType(event.getResourceType());
					}
				}
			}
		}
		
		
		return mainEvents;
	}

	private List<NetworkEvent> combineRequestLogs(List<LogEntry> allEvents) {
		List<NetworkEvent> combinedEvents = new ArrayList<>();

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
		
		Map<Integer, Long> orderingMap = new HashMap<>();


		for (String requestId : requestMap.keySet()) {
			List<LogEntry> eventsForRequest = requestMap.get(requestId);
			List<NetworkEvent> mainEvents = extractMainEvents(eventsForRequest, orderingMap);
			if(mainEvents!=null)
				combinedEvents.addAll(mainEvents);
		}
		
		Collections.sort(combinedEvents, new Comparator<NetworkEvent>() {

			@Override
			public int compare(NetworkEvent o1, NetworkEvent o2) {
				if(orderingMap.get(o1.getId()) - orderingMap.get(o2.getId()) > 0) {
					return 1;
				}
				if(orderingMap.get(o1.getId()) - orderingMap.get(o2.getId()) < 0) {
					return -1;
				}
				else {
					return 0;
				}
			}
		});;
		
		return combinedEvents;
	}
	
	
	public static boolean isExcludedResource(NetworkEvent request, GenPipeLine genPipeLine) {
		
		String mimeType = request.getResourceType();
		if (mimeType == null || mimeType.trim().isEmpty()) {
			// Requests like OPTIONS do not have a mimetype
			return false;
		}
		
		if(genPipeLine.getFilterType() == GenPipeLine.FILTER_TYPE.exclusion) {

			List<String> excludedResources = genPipeLine.getExcludedResources();
			
			String[] mimeTypeParts = mimeType.split("/");
			String mainType = null;
			String subType = null;
			if (mimeTypeParts != null) {
				if (mimeTypeParts.length > 0) {
					mainType = mimeTypeParts[0];
				}
				if (mimeTypeParts.length > 1) {
					subType = mimeTypeParts[1];
				}
			}

			if (excludedResources.contains(mimeType) || (mainType != null && excludedResources.contains(mainType))
					|| (subType != null && excludedResources.contains(subType))) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			List<String> includedResources = genPipeLine.getIncludedResources();
			
			String[] mimeTypeParts = mimeType.split("/");
			String mainType = null;
			String subType = null;
			if (mimeTypeParts != null) {
				if (mimeTypeParts.length > 0) {
					mainType = mimeTypeParts[0];
				}
				if (mimeTypeParts.length > 1) {
					subType = mimeTypeParts[1];
				}
			}

			if (includedResources.contains(mimeType) || (mainType != null && includedResources.contains(mainType))
					|| (subType != null && includedResources.contains(subType))) {
				return false;
			}
			else {
				return true;
			}
		}
		
	}

	/**
	 * Removes the requests that pertain to excluded mime types
	 * @param apiRequests
	 * @return filtered list of requests
	 */
	public List<NetworkEvent> excludeResourceRequests(List<NetworkEvent> apiRequests) {
		List<NetworkEvent> requestsToExclude = new ArrayList<>();
		for (NetworkEvent request : apiRequests) {
			if(request.method != NetworkEvent.MethodClazz.GET){
				// Cannot filter non get requests reliably. Unlikely to have resource requests that are not GET
				continue;
			}
			if(isExcludedResource(request, genPipeLine)) {
				requestsToExclude.add(request);
			}
		}
		
		apiRequests.removeAll(requestsToExclude);
		return apiRequests;
	}

	public WorkDirManager getWorkDirManager() {
		return workDirManager;
	}

	public void setWorkDirManager(WorkDirManager workDirManager) {
		this.workDirManager = workDirManager;
	}
}
