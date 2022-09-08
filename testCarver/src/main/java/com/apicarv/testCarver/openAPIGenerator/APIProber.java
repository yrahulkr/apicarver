package com.apicarv.testCarver.openAPIGenerator;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.apicarv.testCarver.utils.Settings;
import com.apicarv.testCarver.utils.UtilsOASGen;
import com.apicarv.testCarver.utils.WorkDirManager;
import com.apicarv.testCarver.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Sets;
import com.apicarv.testCarver.Main;
import com.apicarv.testCarver.apirecorder.LogEntry;
import com.apicarv.testCarver.apirecorder.NetworkEvent;
import com.apicarv.testCarver.apirecorder.NetworkEvent.EventClazz;
import com.apicarv.testCarver.apirecorder.NetworkEvent.MethodClazz;
import com.apicarv.testCarver.apirunner.APIResponse;
import com.apicarv.testCarver.apirunner.APIResponse.CheckPointType;
import com.apicarv.testCarver.apirunner.APIResponse.Status;
import com.apicarv.testCarver.apirunner.APIRunner;
import com.apicarv.testCarver.openAPIGenerator.ProbeEvent.ProbeType;
import com.apicarv.testCarver.utils.Settings.SUBJECT;
import com.apicarv.testCarver.utils.WorkDirManager.DirType;

import io.swagger.v3.oas.models.OpenAPI;

public class APIProber {
	private static final Logger LOG = LoggerFactory.getLogger(APIProber.class);

	List<APIResponse> apiResponses;

	private SUBJECT subject;

//	private List<APIResponse> newResponses = new ArrayList<>();

	private String minedFolder;

	private String runFolder;
	private String baseUrl;

	/**
	 *
	 * @param apiResponses
	 * @param subject
	 * @param baseUrl
	 * @param minedFolder
	 * @param runFolder
	 * @param workDirManager
	 */
	public APIProber(List<APIResponse> apiResponses, SUBJECT subject, String baseUrl, String minedFolder, String runFolder, WorkDirManager workDirManager) {
		this.apiResponses = apiResponses;
		this.subject = subject;
		this.baseUrl = baseUrl;
		this.minedFolder = minedFolder;
		this.runFolder = runFolder;
		this.workDirManager = workDirManager;
	}
	
	public APIResponse getResponseForId(String requestId) {
		for(APIResponse response: apiResponses) {
			if(response.getRequest().getRequestId().equalsIgnoreCase(requestId) 
					&& response.getRequest().getMethod() == MethodClazz.GET) {
				return response;
			}
		}
		return null;
	}
	
	AtomicInteger PROBE_ID = new AtomicInteger(0);

	private ArrayList<ProbeEvent> executedProbeEvents;
	
	public ProbeEvent getSimilarRequest(URLNode existingNode, URLNode probeNode) {
		String URL = buildSimilarURL(probeNode, existingNode);
		NetworkEvent existing = null;
		try {
			existing = getResponseForId(existingNode.getRequestId()).getRequest();
		}catch(NullPointerException ex) {
			LOG.error("NetworkEvent for given Request Id {} not found", existingNode.getRequestId());
		}
		
		if(existing == null) {
			return null;
		}
	

		ProbeEvent similarPr = new ProbeEvent(PROBE_ID.getAndIncrement(), existingNode);
		existing.transferFields(similarPr);
		similarPr.setRequestUrl(URL);
		similarPr.setClazz(EventClazz.Probe);
		return similarPr;
	}
	
	public List<ProbeEvent> getSimilarEvents(APIGraph apiGraph) {
		List<ProbeEvent> availableEvents = new ArrayList<ProbeEvent>();
		// Get request to execute
		DirectedAcyclicGraph<URLNode, DefaultEdge> graph = apiGraph.getGraph();
		for(URLNode urlNode: graph.vertexSet()) {
			if(graph.incomingEdgesOf(urlNode).size() > 1) {
				/*
				 *  When there are multiple incoming edges to a vertex, 
				 *  we consider the ancestors to be possible path variable instances
				*/
				Set<URLNode> possiblySimilarSet = graph.incomingEdgesOf(urlNode).stream().map(edge->graph.getEdgeSource(edge)).collect(Collectors.toSet());
					
				
				// See if any of the nodes are leaf nodes
				boolean hasLeafNode = possiblySimilarSet.stream().anyMatch(node -> node.isLeaf);
				if(hasLeafNode) {
					// Atleast one of the ancestors had a response from server. So try Request for every ancestory without a response.
					// PruneGraph would not have detected these nodes because they are not leaf nodes
					LOG.info("Node {} has ancestors that are leaf nodes", urlNode);
					URLNode leafNode = possiblySimilarSet.stream().filter(node -> node.isLeaf).findFirst().get();
					Set<URLNode> nonLeafNodes = possiblySimilarSet.stream().filter(node -> node.isLeaf).collect(Collectors.toSet());
					for(URLNode nonLeafNode: nonLeafNodes) {
						
//						NetworkEvent existing = getResponseForId(leafNode.getRequestId()).getRequest();
//						
//						String URL = existing.getRequestUrl().substring(0, existing.getRequestUrl().lastIndexOf("/")+1) + nonLeafNode.pathItem;
						
						ProbeEvent similar = getSimilarRequest(leafNode, nonLeafNode);
						if(similar!=null){
							similar.setProbeType(ProbeType.MDI2L);
							availableEvents.add(similar);
						}
					}
				}
			}
		}
		return availableEvents;
	}
	
	
	/**
	 * To be called after I2L probes are done and the graph is updated.
	 * @return
	 */
	public List<ProbeEvent> getMDBpEvents(APIGraph apiGraph){
		List<ProbeEvent> availableEvents = new ArrayList<ProbeEvent>();
		// Get request to execute
		DirectedAcyclicGraph<URLNode, DefaultEdge> graph = apiGraph.getGraph();
		for(URLNode urlNode: graph.vertexSet()) {
			if(graph.incomingEdgesOf(urlNode).size() > 1) {
				/*
				 *  When there are multiple incoming edges to a vertex, 
				 *  we consider the ancestors to be possible path variable instances
				*/
				Set<URLNode> possiblySimilarSet = graph.incomingEdgesOf(urlNode).stream().map(edge->graph.getEdgeSource(edge)).collect(Collectors.toSet());
				
				// Missing Edges- Check if adding edges would bring subtree similarity for ancestors
				// 1. compute combined leafnode set
				
				Map<URLNode, Set<URLNode>> mappedLeafDescendants = possiblySimilarSet.stream().
						collect(Collectors.toMap
								(ancestor -> ancestor, 
										ancestor -> graph.outgoingEdgesOf(ancestor).stream().map(edge->graph.getEdgeTarget(edge)).filter(node->node.isLeaf)
										.collect(Collectors.toSet())));
								
//								graph.getDescendants(ancestor).stream().filter(node -> node.isLeaf)
//								.collect(Collectors.toSet())));
				
				LOG.info(" ancestor size - {}  key size - {} ", possiblySimilarSet.size(), mappedLeafDescendants.size());
				
				List<ProbeEvent> probeEvents = findMissingEdgesToCompleteBiPt(mappedLeafDescendants);
				availableEvents.addAll(probeEvents);
			}
		}
		return availableEvents;
	}
	
	public List<ProbeEvent> findMissingEdgesToCompleteBiPt(Map<URLNode, Set<URLNode>> adjacencyMap) {
		List<ProbeEvent> probeEvents = new ArrayList<ProbeEvent>();
		Map<String, URLNode> nameToNodeMap = new HashMap<String, URLNode>();
		Set<String> part1 = new HashSet<>();
		Set<String> part2 = new HashSet<>();
		
		for(URLNode part1Node: adjacencyMap.keySet()) {
			String part1Name = part1Node.pathIndex + "-" + part1Node.pathItem;
			part1.add(part1Name);
			nameToNodeMap.put(part1Name, part1Node);
			for(URLNode part2Node: adjacencyMap.get(part1Node)) {
				String part2Name = part2Node.pathIndex + "-" + UtilsOASGen.parseURLNode(part2Node);
				part2.add(part2Name);
				nameToNodeMap.put(part2Name, part2Node);
			}
		}
		LOG.info("Bipartite Graph - ");
		LOG.info("Part1 - {}", part1);
		LOG.info("Part2 : {}", part2);
		
		for(URLNode part1Node: adjacencyMap.keySet()) {
			String part1Name = part1Node.pathIndex + "-" + part1Node.pathItem;
			Set<String> part2Names = adjacencyMap.get(part1Node).stream().map(node -> node.pathIndex + "-"+ UtilsOASGen.parseURLNode(node))
					.collect(Collectors.toSet());
			Set<String> difference = Sets.difference(part2, part2Names);
			LOG.info("missing {} - {}", part1Name, difference);
			for(String diff: difference) {
				// Add a probe for each missing edge
				URLNode diffNode = nameToNodeMap.get(diff);
//				String newURL = buildSimilarURL(part1Node, diffNode);
				ProbeEvent similarRequest = getSimilarRequest(diffNode, part1Node);
				probeEvents.add(similarRequest);
			}
		}
		
		return probeEvents;
	}
	
	private String buildIntermediateURL(URLNode intermediate, String originalUrl) {
		
		String newURL = null;
		
		try {
			URL originalURL = new URL(originalUrl);
			String newPath = intermediate.getParentPath() + "/" + intermediate.getPathItem();
			newURL = new URL(originalURL.getProtocol(), originalURL.getHost(), originalURL.getPort(), newPath).toString();			
			LOG.info("Built URL from {} to {}", originalURL.toString(), newURL);
		}catch(MalformedURLException e) {
			e.printStackTrace();
		}
		
		return newURL;
	}

	private String buildSimilarURL(URLNode probeNode, URLNode nodeWithURL) {
		String newURL = null;
		try {
			URL originalURL = new URL(getResponseForId(nodeWithURL.getRequestId()).getRequest().getRequestUrl());
			String originalPath = originalURL.getPath();
			String[] split = originalPath.split("/");
			split[probeNode.pathIndex] = probeNode.pathItem;
			String newPath = StringUtils.join(split, "/");
			if(originalURL.getQuery()!=null) {
				newURL = new URL(originalURL.getProtocol(), originalURL.getHost(), originalURL.getPort(), newPath + "?" + originalURL.getQuery()).toString();
			}
			else {
				newURL = new URL(originalURL.getProtocol(), originalURL.getHost(), originalURL.getPort(), newPath).toString();
			}
			LOG.info("Changed URL from {} to {}", originalURL.toString(), newURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (NullPointerException npe){
			npe.printStackTrace();
		}
		return newURL;
	}
	
	AtomicInteger RESPONSE_ID;

	private WorkDirManager workDirManager;

	/**
	 * Builds graph with original requests while making GET requests for intermediate nodes
	 * @param httpClient
	 * @param cookieCache
	 * @param graphBuildOutput 
	 * @return
	 */
	public APIGraph buildGraphWithItermediate(CloseableHttpClient httpClient, Map<String, String> cookieCache, List<APIResponse> graphBuildOutput) {
		
		LOG.info("Building graph by expansion");
//		List<APIResponse> originalResponses  = apiGraph.getApiResponses();
		/*int maxId = apiGraph.getApiResponses().stream().max(new Comparator<APIResponse>() {


			@Override
			public int compare(APIResponse o1, APIResponse o2) {
				return o1.getId() - o2.getId();
			}
		}).get().getId();*/
		
		Set<String> probeList = new HashSet<>();
		Set<String> existingList =  apiResponses.stream().filter(original-> ((original.getRequest().getMethod()==MethodClazz.GET) && (original.getStatus() == Status.SUCCESS )))
				.map(original->UtilsOASGen.getOnlyURL(original.getRequest().getRequestUrl())).collect(Collectors.toSet());
		
		LOG.info("Existing list of URLs");
		LOG.info("{}", existingList);
		
		APIGraph newGraph = new APIGraph(subject, graphBuildOutput);
		
//		APIGraph newGraph = new APIGraph(minedFolder, runFolder);

		for(APIResponse originalResponse: apiResponses) {
			APIResponse newResponse = new APIResponse(RESPONSE_ID.incrementAndGet());
			LOG.info("Executing original response {} with new Id {}", originalResponse.getRequest().getRequestUrl(), newResponse.getId());
			Status status = executeNormalEvent(httpClient, originalResponse.getRequest(), newResponse, cookieCache);
			switch(status) {
			case SKIPPED:
				LOG.warn("Skipped Original Event {}", originalResponse.getId());
				break;
			case FAILURE:
				LOG.error("Failed to execute orignal event {}", originalResponse.getId());
				break;
			case SUCCESS:
				if(newResponse.getResponse().getData()!=null) {
					workDirManager.exportPayLoad(newResponse, newResponse.getResponse().getData());
				}
				break;
				
			default:
				break;
			}
			graphBuildOutput.add(newResponse);
			List<URLNode> graphPath = addResponseToGraph(newResponse, newGraph);
			
			if(graphPath!=null) {
				LOG.info("Expanding intermediate nodes for graphpath of {}", newResponse.getId());
				// Its a REST API call
				for(URLNode intermediate: graphPath) {
					if(!intermediate.isLeaf) {
						String probeURL = buildIntermediateURL(intermediate, originalResponse.getRequest().getRequestUrl());
						if(existingList.contains(probeURL)) {
							LOG.info("Skipping probe because the url already exists in original list {} ", probeURL);
							continue;
						}
						if(probeList.contains(probeURL)) {
							LOG.info("Skipping probe because the probe was already executed earlier {} ", probeURL);
							continue;
						}
						else {
							probeList.add(probeURL);
						}
						
						LOG.info("Found intermediate node that is not leaf. Creating probe");
						
						APIResponse probeResponse = new APIResponse(RESPONSE_ID.incrementAndGet());
						ProbeEvent newProbe = new ProbeEvent(PROBE_ID.getAndIncrement(), null);
						newProbe.setProbeType(ProbeType.MDI2L);
						newProbe.setRequestId("probe" + ProbeType.MDI2L.name() + newProbe.getId());
						List<Header> newHeaders = new ArrayList<>();
						
						for(Header existing: originalResponse.getRequest().getHeaders()) {
							newHeaders.add(new BasicHeader(existing.getName(), existing.getValue()));
						}
						newProbe.setClazz(EventClazz.Probe);
						newProbe.setMethod(MethodClazz.GET);
						newProbe.setRequestUrl(probeURL);
						
 						newProbe.setHeaders(newHeaders);
 						

						Status probeStatus = executeProbeEvent(httpClient, newProbe, probeResponse, cookieCache);
						
						switch(probeStatus) {
						case SKIPPED:
							LOG.warn("Skipped probe event {}", newProbe.getId());
							break;
						case FAILURE:
							LOG.error("Failed to execute probe event {}", newProbe.getId());
							break;
						case SUCCESS:
							LOG.warn("probeEvent {} : code - {}", newProbe.getRequestUrl(), probeResponse.getResponse().getStatus());

							if(probeResponse.getResponse().getData()!=null) {
								LOG.info("Exporting payload for probe {}", newProbe.getId());
								workDirManager.exportPayLoad(probeResponse, probeResponse.getResponse().getData());
							}
							
							addResponseToGraph(probeResponse, newGraph);
							break;
							
						default:
							break;
						}
						graphBuildOutput.add(probeResponse);
					}
				}
			}
			
//			executeProbeEvent(httpClient, probeEvent, returnResponse, cookieCache);
		}
		
		LOG.info("Probe URLs tried {}", probeList);
		LOG.info("Existing URLs {}", existingList);
		return newGraph;
	}
	
	/**
	 * Main function
	 * @return
	 */
	public List<APIResponse> expandGraph() {
		
		int maxId = apiResponses.stream().max(new Comparator<APIResponse>() {
			@Override
			public int compare(APIResponse o1, APIResponse o2) {
				return o1.getId() - o2.getId();
			}
		}).get().getId();
		
		RESPONSE_ID = new AtomicInteger(maxId);
		
		Map<String, String> cookieCache = new HashMap<>();
//		List<APIResponse> graphBuildOutput = new ArrayList<>();
		executedProbeEvents = new ArrayList<ProbeEvent>();

		CloseableHttpClient httpClient = null;
		try {
			httpClient = APIRunner.getCloseableHttpClient();
		} catch (NoSuchAlgorithmException | KeyStoreException| KeyManagementException e) {
			LOG.error("Error creating HTTP Client");
			e.printStackTrace();
			return null;
		}

		APIGraph newGraph = new APIGraph(subject, minedFolder, runFolder);

		newGraph.buildAPIGraph();
		newGraph.pruneGraph();

//		if(subject!=null)
//			Main.resetApp(subject, true, null);


//		APIGraph newGraph = buildGraphWithItermediate(httpClient, cookieCache, graphBuildOutput);
		
//		if(subject!=null)
//			Main.resetApp(subject, false, workDirManager.getCovFile());
		
		
//		newGraph.pruneGraph();

		List<ProbeEvent> allProbes = new ArrayList<>();

		List<ProbeEvent> intermediateEvents = getIntermediateEvents(newGraph);
		allProbes.addAll(intermediateEvents);

		List<ProbeEvent> similarEvents = getSimilarEvents(newGraph);
		allProbes.addAll(similarEvents);

		List<ProbeEvent> mdbpList = getMDBpEvents(newGraph);
		allProbes.addAll(mdbpList);

		List<ProbeEvent> responseAnalysis = buildProbesUsingResponseAnalysis(apiResponses, this.baseUrl);
		allProbes.addAll(responseAnalysis);

		workDirManager.exportProbeEvents(allProbes, "getPr");

		List<NetworkEvent> orderedEvents = scheduleProbes(allProbes, apiResponses, ProbeScheduler.GRAPH_DEP);

		if(subject!=null)
			Main.resetApp(subject, true, null);

		// Do not save bad status probes. saving all events leads to Heapspace error
		List<APIResponse> responsesWithGetProbes = executeEventSequenceWithProbes(orderedEvents, cookieCache, httpClient);
		workDirManager.exportResultResponses(responsesWithGetProbes, "getPr", Settings.DISABLE_HTML_OUTPUT);

		responsesWithGetProbes = minifyAPISequence(responsesWithGetProbes);
		workDirManager.exportResultResponses(responsesWithGetProbes, "getPrMin", Settings.DISABLE_HTML_OUTPUT);



		if(subject!=null)
			Main.resetApp(subject, false, workDirManager.getCovFile());


		List<ProbeEvent> missingOpList = findMissingOperations(responsesWithGetProbes, new ArrayList<>());
		orderedEvents = scheduleProbes(missingOpList, responsesWithGetProbes, ProbeScheduler.GRAPH_DEP);

//		allProbes.addAll(missingOpList);
//		List<APIResponse> responsesWithProbes = orderProbes(graphBuildOutput, allProbes);
		
		if(subject!=null)
			Main.resetApp(subject, true, null);
		
		List<APIResponse> responsesWithAllProbes = executeEventSequenceWithProbes(orderedEvents, cookieCache, httpClient);
		
		if(subject!=null)
			Main.resetApp(subject, false, workDirManager.getCovFile("allPr"));

		allProbes.addAll(missingOpList);
		workDirManager.exportProbeEvents(allProbes, "allPr");
		
		workDirManager.exportProbeEvents(executedProbeEvents, "executed");

		workDirManager.exportResultResponses(responsesWithAllProbes, "allPr", Settings.DISABLE_HTML_OUTPUT);

		List<APIResponse> minified = minifyAPISequence(responsesWithAllProbes);

		try {
			httpClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return minified;
	}

	public static List<APIResponse> minifyAPISequence(List<APIResponse> responsesWithGetProbes) {
		List<APIResponse> returnResponses = new ArrayList<>();
		List<APIResponse> successfulResponses = responsesWithGetProbes.stream()
				.filter(
						apiResponse ->
								!(
									apiResponse.getRequest().getRequestId().startsWith("probe")
										&& (
											apiResponse.getResponse()==null
											|| !UtilsOASGen.isGoodServerStatus(apiResponse.getResponse().getStatus())
										)
								)
				)
				.collect(Collectors.toList());

		// Retain only one instance of a successful probe (multiple instances happen because of checkpoint scheduling)
		Set<String> successfulProbes = new HashSet<>();
		for(APIResponse apiResponse: successfulResponses){
			if(!apiResponse.getRequest().getRequestId().startsWith("probe")){
				// If a request is not a probe, add it regardless of the server status
				returnResponses.add(apiResponse);
				continue;
			}
			if(!successfulProbes.contains(apiResponse.getRequest().getRequestId())){
				successfulProbes.add(apiResponse.getRequest().getRequestId());
				returnResponses.add(apiResponse);
				LOG.info("Added probe to the successful sequence");
			}
			else{
				LOG.info("Skipping instance because it already exists in the return list");
			}
		}

		return returnResponses;
	}

	private List<ProbeEvent> getIntermediateEvents(APIGraph graph) {
		List<ProbeEvent> probeEvents = new ArrayList<>();
		Set<String> probeList = new HashSet<>();
		Set<String> existingList =  apiResponses.stream().filter(original-> ((original.getRequest().getMethod()==MethodClazz.GET) && (original.getStatus() == Status.SUCCESS )))
				.map(original->UtilsOASGen.getOnlyURL(original.getRequest().getRequestUrl())).collect(Collectors.toSet());

		for(APIResponse originalResponse: apiResponses) {
			if(!originalResponse.getRequest().getRequestUrl().startsWith(this.baseUrl)){
				LOG.info("Ignoring intermediate probes for {} because it is outside API domain {}", originalResponse.getRequest().getRequestUrl(), this.baseUrl);
				continue;
			}
			List<URLNode> graphPath = graph.apiPathMap.get(originalResponse.getId());
			if(graphPath!=null) {
				LOG.info("Expanding intermediate nodes for graphpath of {}", originalResponse.getId());
				// Its a REST API call
				for(URLNode intermediate: graphPath) {
					if(!intermediate.isLeaf) {
						String probeURL = buildIntermediateURL(intermediate, originalResponse.getRequest().getRequestUrl());
						if(existingList.contains(probeURL)) {
							LOG.info("Skipping probe because the url already exists in original list {} ", probeURL);
							continue;
						}
						if(probeList.contains(probeURL)) {
							LOG.info("Skipping probe because the probe was already executed earlier {} ", probeURL);
							continue;
						}
						else {
							probeList.add(probeURL);
						}

						LOG.info("Found intermediate node that is not leaf. Creating probe");

						ProbeEvent newProbe = new ProbeEvent(PROBE_ID.getAndIncrement(), null);
						newProbe.setProbeType(ProbeType.MDI2L);
						newProbe.setRequestId("probe" + ProbeType.MDI2L.name() + newProbe.getId());
						List<Header> newHeaders = new ArrayList<>();

						for(Header existing: originalResponse.getRequest().getHeaders()) {
							newHeaders.add(new BasicHeader(existing.getName(), existing.getValue()));
						}
						newProbe.setClazz(EventClazz.Probe);
						newProbe.setMethod(MethodClazz.GET);
						newProbe.setRequestUrl(probeURL);

						newProbe.setHeaders(newHeaders);

						probeEvents.add(newProbe);
					}
				}
			}
		}
		return probeEvents;
	}

	private List<APIResponse> executeEventSequenceWithProbes( List<NetworkEvent> eventSequence, Map<String, String> cookieCache, CloseableHttpClient httpClient) {

		LOG.info("Executing event sequence with {} requests", eventSequence.size());
		List<APIResponse> newResponses = new ArrayList<>();

		Set<String> succeededProbes = new HashSet<>();
		int failedProbes = 0;
		int skippedProbes = 0;
		int progress = 0;
		for(NetworkEvent event: eventSequence) {
			LOG.info("Progress : {}/{}", progress, eventSequence.size());
			progress +=1;
			APIResponse newResponse = new APIResponse(RESPONSE_ID.incrementAndGet());
			APIResponse.Status status = null;

			if(event instanceof ProbeEvent) {

				if(succeededProbes.contains(event.getRequestId())){
					skippedProbes += 1;
					LOG.info("Skipping probe {} because a successful instance already exists ", event.getRequestId());
					continue;
				}
				status = executeProbeEvent(httpClient, (ProbeEvent)event, newResponse, cookieCache);
				if(newResponse.getResponse()!=null && UtilsOASGen.isGoodServerStatus(newResponse.getResponse().getStatus())){
					succeededProbes.add(event.getRequestId());
				}
				else{
					failedProbes += 1;
					LOG.info("No further processing of failed probe {}", event.getRequestId());
					continue;
				}
			}
			else {
				status = executeNormalEvent(httpClient, event, newResponse, cookieCache);
			}
			
			switch(status) {
			case SKIPPED:
				LOG.warn("Skipped event {}", event.getId());
				break;
			case FAILURE:
				LOG.error("Failed to execute event {}", event.getId());
				break;
			case SUCCESS:
				LOG.warn("Event {} : code - {}", event.getRequestUrl(), newResponse.getResponse().getStatus());

				if(newResponse.getResponse().getData()!=null) {
					workDirManager.exportPayLoad(newResponse, newResponse.getResponse().getData());
				}
				
//				addResponseToGraph(newResponse, newGraph);
				break;
				
			default:
				break;
			}
			newResponses.add(newResponse);
		}
		LOG.info("Succeeded probes {}", succeededProbes.size());
		LOG.info("Failed probes {}", failedProbes);
		LOG.info("Skipped probes {}", skippedProbes);

		return newResponses;
	}

	private List<URLNode> addResponseToGraph(APIResponse newResponse, APIGraph graph) {
		
		if(UtilsOASGen.getPathFromURL(newResponse.getRequest().getRequestUrl())==null) {
			LOG.info("Ignoring API {} because URL path is null {}", newResponse.getId(), newResponse.getRequest().getRequestUrl());
			return null;
		}
		
		// parse only GET rquests
		if(newResponse.getRequest().getMethod() != MethodClazz.GET) {
			LOG.info("Ignoring API {} because it is not GET", newResponse.getId());
			return null;
		}
		
		if(newResponse.getResponse().getResourceType() == null || !( newResponse.getResponse().getResourceType().contains("json") || newResponse.getResponse().getResourceType().contains("xml"))){
			LOG.info("Ignoring API {} because resourceType is {}", newResponse.getId(), newResponse.getResponse().getResourceType());
			return null;
		}
		
		
		int status = newResponse.getResponse().getStatus();
		 
		if(status >= 400 && status < 500 ) {
			// status code is 4xx - bad request
			LOG.info("The API call is a bad request {}", newResponse.getRequest().getRequestUrl());
			return null;
		}
		
		if(status >= 500 && status < 600) {
			// Status code 5xx - server error
			LOG.info("The API call caused server error {}", newResponse.getRequest().getRequestUrl());
		}
		
		Map<String, List<LogEntry>> requestMap = new HashMap<>();
		return graph.parseAPIResponse(newResponse, requestMap , newResponse.getResponse().getData());
	}

	/**
	 * Uses provided "existingResponses" parameter for analysis. If not provided, defaults to apiResponses, which is generated by APIRunner
	 * Finds missing operations in known paths or other probes (they are GET probes)
	 * Known operations : GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD, TRACE
	 * @return
	 */
	public List<ProbeEvent> findMissingOperations(List<APIResponse> existingResponses, List<ProbeEvent> availableProbes){

		// If Graph expansion has not been called, then fill new responses with old ones
		if(existingResponses == null && apiResponses!=null){
			existingResponses = new ArrayList<>();
			existingResponses.addAll(apiResponses);
		}
		
		Map<String, Set<NetworkEvent.MethodClazz>> opMap = new HashMap<>();
		Map<String, List<NetworkEvent>> pathMap = new HashMap<>();
		Map<String, List<NetworkEvent>> postDataMap = new HashMap<>();
		
		List<NetworkEvent> allRequests = new ArrayList<>();
		allRequests.addAll(
				existingResponses.stream()
						.filter(apiResponse -> (
										(apiResponse.getStatus() == Status.SUCCESS)
										&& apiResponse.getResponse()!=null
										&& UtilsOASGen.isGoodServerStatus(apiResponse.getResponse().getStatus())
								)) // Use only apiresponses with good server status (200-399)
						.filter(apiResponse -> UtilsOASGen.isRestAPI(apiResponse))
						.map(apiResponse -> apiResponse.getRequest()).collect(Collectors.toSet()));
		allRequests.addAll(availableProbes);
//		for(APIResponse response: existingResponses) {
//			String path = null;
//			path = UtilsOASGen.getOnlyURL(response.getRequest().getRequestUrl());
//			if(path == null) {
//				LOG.error("NULL path for APIResponse : ", response.getId());
//				continue;
//			}
//			if(!pathMap.containsKey(path)) {
//				pathMap.put(path, new ArrayList<>());
//			}
//			pathMap.get(path).add(response.getRequest());
//
//			if(!opMap.containsKey(path)) {
//				opMap.put(path, new HashSet<NetworkEvent.MethodClazz>());
//			}
//			opMap.get(path).add(response.getRequest().getMethod());
//
//			if(response.getRequest().getPostData()!=null && !response.getRequest().getPostData().isEmpty()) {
//				if(!postDataMap.containsKey(path)) {
//					postDataMap.put(path, new ArrayList<>());
//				}
//				postDataMap.get(path).add(response.getRequest());
//			}
//		}

		for(NetworkEvent request: allRequests){
			String path = null;
			path = UtilsOASGen.getOnlyURL(request.getRequestUrl());
			if(path == null) {
				LOG.error("NULL path for : {} ", request);
				continue;
			}
			if(!pathMap.containsKey(path)) {
				pathMap.put(path, new ArrayList<>());
			}
			pathMap.get(path).add(request);

			if(!opMap.containsKey(path)){
				opMap.put(path, new HashSet<>());
			}
			opMap.get(path).add(request.getMethod());

			if(request.getPostData()!=null && !request.getPostData().isEmpty()) {
				if(!postDataMap.containsKey(path)) {
					postDataMap.put(path, new ArrayList<>());
				}
				postDataMap.get(path).add(request);
			}
		}
		
		List<ProbeEvent> missingOpProbes = new ArrayList<>();
		
		for(String path: opMap.keySet()){
			Set<MethodClazz> availableOps = opMap.get(path);
			Set<MethodClazz> all = Sets.newHashSet(MethodClazz.values());
			all.remove(MethodClazz.UNKNOWN);
			all.remove(MethodClazz.PATCH);
			all.remove(MethodClazz.TRACE);
			Set<MethodClazz> missingOps = Sets.difference(all, availableOps);
			
			LOG.info("missing {} {}", path, missingOps);
			
			for(MethodClazz missingOp: missingOps) {
				
				switch(missingOp) {
				case POST:
				case PUT:
					//Imitate the other if one is available. Needs postdata
					// Can I use the response of GET for sending POST/PUT?
//					LOG.info("Yet to handle PUT/POST");
					if(postDataMap.get(path) == null) {
						LOG.info("Skipping probe {} - {}. No post data available.", missingOp, path);
						continue;
					}
					for(NetworkEvent existing: postDataMap.get(path)) {
						String url = existing.getRequestUrl();
						String postData = existing.getPostData();
						List<Header> headers = existing.getHeaders();
						ProbeType probeType = ProbeType.MOP;
						ProbeEvent newProbe = buildProbeEvent(missingOp, url, headers, postData, probeType);
						missingOpProbes.add(newProbe);
						LOG.info("Added {}", newProbe);
					}
					break;
				default:
//					for(NetworkEvent existing: pathMap.get(path)) {
					NetworkEvent existing = pathMap.get(path).get(0);
					String url = existing.getRequestUrl();
					List<Header> headers = existing.getHeaders();
					ProbeType probeType = ProbeType.MOP;
					ProbeEvent newProbe = buildProbeEvent(missingOp, url, headers, null, probeType);
					missingOpProbes.add(newProbe);
					LOG.info("Added {}", newProbe);
//					}
					break;
				}
			}
			
//			for(MethodClazz operation: opMap.get(path));
		}
			
		
		LOG.info("{}", opMap);
		LOG.info("{}", missingOpProbes.size());
		
		return missingOpProbes;
	}

	/**
	 * 
	 * @param missingOp
	 * @param url
	 * @param headers
	 * @param postData
	 * @param probeType
	 * @return
	 */
	private ProbeEvent buildProbeEvent(MethodClazz missingOp, String url, List<Header> headers, String postData, ProbeType probeType) {
		ProbeEvent newProbe = new ProbeEvent(PROBE_ID.incrementAndGet(), null);
		newProbe.setClazz(EventClazz.Probe);
		newProbe.setMethod(missingOp);
		newProbe.setProbeType(probeType);
		newProbe.setRequestUrl(url);
		newProbe.setRequestId("probe" + probeType.name() + newProbe.getId());

//					APIResponse probeResponse = new APIResponse(RESPONSE_ID.incrementAndGet());
		List<Header> newHeaders = new ArrayList<>();
		
		for(Header existingHead: headers) {
			newHeaders.add(new BasicHeader(existingHead.getName(), existingHead.getValue()));
		}					
		newProbe.setHeaders(newHeaders);
		if(postData!=null) {
			newProbe.setPostData(postData);
		}
		return newProbe;
	}
	
	

	public static void main(final String[] args) {
//		String minedFolder = "parabank-20220503_064854";
// 		String runFolder = "/Users/apicarv/git/TestCarving/testCarver/out/parabank-20220503_064854/run/20220503_070049";
//		String baseURL = "http://localhost:8080/parabank-3.0.0-SNAPSHOT/services_proxy/bank";

//		String minedFolder = "petclinic";
//		String runFolder = "/Users/apicarv/git/TestCarving/testCarver/out/petclinic/run/20220502_172405";
//		SUBJECT subject = SUBJECT.petclinic;
//		APIGraph apiTree = new APIGraph(minedFolder, runFolder);
//		String baseURL = "http://localhost:9966/petclinic/api";

		String minedFolder = "20220826_005052";
		String runFolder = "20220826_005835";
		SUBJECT subject = SUBJECT.shopizer;
		String baseURL = "http://localhost:8080";
		String runPath = "/Users/apicarv/git/TestCarving/testCarver/out/" + subject.name() + "/" +  minedFolder + "/run/" + runFolder;

		APIGraph apiTree = new APIGraph(subject, minedFolder, runPath);


//		DirectedAcyclicGraph<URLNode, DefaultEdge> graph= apiTree.getAPIGraph();
//		
//		apiTree.pruneGraph();
		
		WorkDirManager workDirManager = new WorkDirManager(subject, "testProbe", DirType.OAS);

//		
		APIProber prober = new APIProber(apiTree.getApiResponses(), subject, baseURL
				, minedFolder, runPath, workDirManager);
		

		List<APIResponse> resultResponses = prober.expandGraph();
		
		workDirManager.exportResultResponses(resultResponses);
		
		APIGraph newGraph = new APIGraph(subject, apiTree.getMinedFolder(), workDirManager.getOutputDir());
		
		DirectedAcyclicGraph<URLNode, DefaultEdge> graph = newGraph.buildAPIGraph();
		newGraph.pruneGraph();
		HashMap<String, String> urlMap = newGraph.extractPathsFromGraph();

		try {
			workDirManager.exportURLMap(urlMap, Settings.PROBED_URL_MAP_JSON);

			workDirManager.exportAPIGraph(graph, Settings.PROBED_API_RAW_GRAPH_FILE, Settings.PROBED_API_GRAPH_FILE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SwaggerGenerator newSwagger = new SwaggerGenerator(subject, apiTree.getMinedFolder(), workDirManager.getOutputDir(), baseURL);
		try {
			OpenAPI openAPI = newSwagger.getOpenAPI(newSwagger.getHostUrl(), newGraph.getApiResponses());
			workDirManager.exportOAS(openAPI, Settings.PROBED_OAS_EXPORT_FILE);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 
	public enum ProbeScheduler{
		/**
		 * send all probes after each checkpoint
		 * Skip DELETE checkpoints
		 */ 
		 CHECKPOINT, 
		 
		 /**
		  * Compute dependence on graph and send valid probes for graph state. Check the first time a node is created in graph for which the current probe URL matches. 
		  * Probe paths that don't exist in the graph are sent at the end. 
		  * DELETE requests are skipped in original set
		  */
		 GRAPH_DEP,
		 
		 /**
		  * Send all probes only before exisitng probes
		  */
		 BEFORE_GRAPH,
		 
		 /**
		  * Send all probes after completing exisitng probes
		  */
		 AFTER_GRAPH, ALL
		 
	}
	
	/**
	 * 
	 * @param probesToRun
	 * @param existingResponses
	 * @param probeScheduler
	 * @return
	 * 
	 * Assumptions: 1. Presence of graph node indicates presence of resource if it is in spec. For example: if P(a/b) exists, then P(a) exists if valid end-point.
	 * 				2. POST -> Only capable of adding nodes/subgraphs to the graph
	 * 				3. DELETE -> Only capable of deleting nodes/subgraphs to the graph
	 * 				4. PUT -> Can do both add/delete nodes/subgraphs to the graph
	 * 				5. Cookie -> Will invalidate or validate (change responses from server) for API calls.				
	 */
	public List<NetworkEvent> scheduleProbes(List<ProbeEvent> probesToRun, List<APIResponse> existingResponses, ProbeScheduler probeScheduler){
	
		List<NetworkEvent> eventSequence = new ArrayList<NetworkEvent>();
		List<NetworkEvent> existingEvents = existingResponses.stream().map(apiResponse -> apiResponse.getRequest()).collect(Collectors.toList());
		
		switch(probeScheduler){
			case BEFORE_GRAPH:
				eventSequence.addAll(probesToRun);
				eventSequence.addAll(existingEvents);
				break;
			case AFTER_GRAPH:
				eventSequence.addAll(existingEvents);
				eventSequence.addAll(probesToRun);
				break;
			case GRAPH_DEP:
				inferCheckPoints(existingResponses);
				eventSequence = getGraphDepProbeSequence(probesToRun, existingResponses);
				break;
			case CHECKPOINT:
				inferCheckPoints(existingResponses);
				eventSequence = getCheckPointDepSequence(probesToRun, existingResponses);
				break;
			case ALL:
				LOG.info("Yet to handle ALL option. Choose something else");
				break;
			default:
				break;
		}
		
		return eventSequence;
	}

	private void inferCheckPoints(List<APIResponse> responsesToParse){

		for(APIResponse apiResponse: responsesToParse){
			if(apiResponse.getCheckPoint()!=null){
				continue;
			}
			NetworkEvent request = apiResponse.getRequest();
			switch (request.getMethod()) {
				case DELETE:
				case POST:
				case PUT:
					apiResponse.addCheckPoint(CheckPointType.OPERATION);
					break;
				default:
					break;
			}

			for (Header header : apiResponse.getResponse().getHeaders()) {
				if (header.getName().equalsIgnoreCase("set-cookie")) {
					apiResponse.addCheckPoint(CheckPointType.COOKIE);
					break;
				}
			}
		}
	}
	
/**
 * 
 * @param probesToRun
 * @param existingResponses - should have the exact order in which they should be executed
 * @return
 */
	private List<NetworkEvent> getGraphDepProbeSequence(List<ProbeEvent> probesToRun,
			List<APIResponse> existingResponses) {
		
		APIGraph newGraph = new APIGraph(subject, existingResponses);
		
//		Collections.sort(existingResponses, (o1, o2) -> o1.getId() - o2.getId());
		
		DirectedAcyclicGraph<URLNode, DefaultEdge> tree = newGraph.buildAPIGraph();
		
		List<ProbeEvent> GETPOSTProbes = probesToRun.stream().filter(probeEvent -> probeEvent.getMethod()!=MethodClazz.PUT && probeEvent.getMethod()!=MethodClazz.DELETE)
				.collect(Collectors.toList());
		
		List<ProbeEvent> GETProbes = probesToRun.stream().filter(probeEvent -> probeEvent.getMethod()!=MethodClazz.POST && probeEvent.getMethod()!=MethodClazz.PUT && probeEvent.getMethod()!=MethodClazz.DELETE)
				.collect(Collectors.toList());
		
		List<ProbeEvent> DELETEProbes =  probesToRun.stream().filter(probeEvent -> probeEvent.getMethod() == MethodClazz.DELETE)
				.collect(Collectors.toList());
		
		List<ProbeEvent> PUTProbes = probesToRun.stream().filter(probeEvent -> probeEvent.getMethod() == MethodClazz.PUT)
				.collect(Collectors.toList());
		
		List<ProbeEvent> POSTProbes = probesToRun.stream().filter(probeEvent -> probeEvent.getMethod() == MethodClazz.POST)
				.collect(Collectors.toList());
		
		List<APIResponse> GETExisting = existingResponses.stream().filter(existingResp -> existingResp.getRequest().getMethod()!=MethodClazz.POST && existingResp.getRequest().getMethod()!=MethodClazz.PUT && existingResp.getRequest().getMethod()!=MethodClazz.DELETE)
				.collect(Collectors.toList());
		
		List<APIResponse> DELETEExisting =  existingResponses.stream().filter(existingResp -> existingResp.getRequest().getMethod() == MethodClazz.DELETE)
				.collect(Collectors.toList());
		
		List<APIResponse> PUTExisting = existingResponses.stream().filter(existingResp -> existingResp.getRequest().getMethod() == MethodClazz.PUT)
				.collect(Collectors.toList());
		
		List<APIResponse> POSTExisting = existingResponses.stream().filter(existingResp -> existingResp.getRequest().getMethod() == MethodClazz.POST)
				.collect(Collectors.toList());
		
		// How to schedule POST, PUT and DELETE events??
		HashMap<Integer, List<ProbeEvent>> location = new HashMap<>();
		location.put(new Integer(-1), new ArrayList<>());
		
		
		Set<String> allUrls = apiResponses.stream().filter(apiResponse -> apiResponse.getRequest().getMethod()==MethodClazz.GET)
				.map(apiResponse -> UtilsOASGen.getPathFromURL(apiResponse.getRequest().getRequestUrl())).collect(Collectors.toSet());
		
		LOG.info("All urls -> {}", allUrls);
		
		for(ProbeEvent probeEvent: GETPOSTProbes) {
			String 	probePath = UtilsOASGen.getPathFromURL(probeEvent.getRequestUrl());

//			try {
////				probePath = new URL(probeEvent.getRequestUrl()).getPath();
//			}catch(MalformedURLException ex) {
//				ex.printStackTrace();
//				LOG.error("The probeEvent has an invalid path {}", probeEvent.getRequestUrl());
//				continue;
//			}
			
			// Only look for full paths (Intermediate nodes have already been checked during  BuildGraphWithIntermediate) -> But do we ignore the requests other than GET like OPTIONS that do not have a valid path in the graph?
			/*Boolean pathExists = allUrls.contains(probePath);
			if(!pathExists) {
				//The path is not added to graph (PUT/POST)?
				boolean foundPath = false;
				for(APIResponse existing: apiResponses) {

					if(UtilsOASGen.getPathFromURL(existing.getRequest().getRequestUrl()).equalsIgnoreCase(probePath)) {
						if(!location.containsKey(existing.getId())) {
							location.put(new Integer(existing.getId()), new ArrayList<ProbeEvent>());
						}
						location.get(existing.getId()).add(probeEvent);
						foundPath = true;
						break;
					}
				}
				if(!foundPath){
					location.get(-1).add(probeEvent);
				}
			}
			else {*/
				URLNode node = newGraph.getTargetNode(probePath);
				
				if(node ==null) {
					boolean foundPath = false;
					for(APIResponse existing: apiResponses) {

						if(UtilsOASGen.getPathFromURL(existing.getRequest().getRequestUrl()).equalsIgnoreCase(probePath)) {
							if(!location.containsKey(existing.getId())) {
								location.put(new Integer(existing.getId()), new ArrayList<ProbeEvent>());
							}
							location.get(existing.getId()).add(probeEvent);
							foundPath = true;
							break;
						}
					}
					// No Graph dep - add them before and after all checkpoints
					if(!foundPath){
						location.get(-1).add(probeEvent);
					}
				}
				else{
					// All the times node appeared in a path
				
					List<Integer> visits = node.getVisits();
					// All the time node resource has been requested
					List<Integer> requests = node.getPayloads();
					
					
					// Lets try after first request
					
					int minId = -1;
					
					if(requests.size()>0)
						minId = requests.stream().min((o1, o2) -> o1-o2).get();
					else
						minId = visits.stream().min((o1,o2) -> o1-o2).get();
					
					if(!location.containsKey(minId)) {
						location.put(new Integer(minId), new ArrayList<ProbeEvent>());
					}
					location.get(minId).add(probeEvent);
				}
//			}
			
		}
		
		LOG.info("Unsorted probe events - {} ", location.get(-1).size());
		
		List<NetworkEvent> returnEvents = new ArrayList<>();

		boolean addedUnsorted = false; // When there are no checkpoints, lets add them after everything
		
		for(APIResponse existingResponse: existingResponses) {
			
			// Delay delete events
			if(existingResponse.getRequest().getMethod() == MethodClazz.DELETE) {
				continue;
			}

			if(
					((existingResponse.getCheckPoint() != null && existingResponse.getCheckPoint() != CheckPointType.NONE))
					&& (existingResponse!=null && existingResponse.getResponse()!=null && UtilsOASGen.isGoodServerStatus(existingResponse.getResponse().getStatus()))
			) {
				//Add unsorted events at every checkpoint
				returnEvents.addAll(location.get(-1));
				addedUnsorted = true;
			}
			
			returnEvents.add(existingResponse.getRequest());
			if(location.containsKey(existingResponse.getId())) {
				returnEvents.addAll(location.get(existingResponse.getId()));
			}
		}

//		if(!addedUnsorted){
		/*
		Add unsorted probes at the end so that the probes are sent before and after every checkpoint
		 */
			returnEvents.addAll(location.get(-1));
//		}
//		returnEvents.addAll(POSTProbes);
		returnEvents.addAll(PUTProbes);
		returnEvents.addAll(DELETEProbes);
		returnEvents.addAll(DELETEExisting.stream().map(apiResponse -> apiResponse.getRequest()).collect(Collectors.toList()));

		LOG.info("Size of events : {}, unsorted probes {}", returnEvents.size(), location.get(-1).size());
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return returnEvents;
	}
	
	
	/**
	 * Computes missing nodes in the tree (nodes are dynamic resource URLs)
	 * @param existingResponses
	 * @return
	 */
	public List<ProbeEvent> buildProbesUsingResponseAnalysis(List<APIResponse> existingResponses, String baseUrl){
		
		HashMap<String, ProbeEvent> probesForUrl = new HashMap<>();
		Set<String> knownUrls = existingResponses.stream().map(apiResponse-> UtilsOASGen.getOnlyURL(apiResponse.getRequest().getRequestUrl())).collect(Collectors.toSet());

		APIGraph newGraph = new APIGraph(subject, existingResponses);
		
		Collections.sort(existingResponses, (o1, o2) -> o1.getId() - o2.getId());
		
		Map<Integer, APIResponse> responseMapping = existingResponses.stream().collect(Collectors.toMap(apiResponse->apiResponse.getId(), apiResponse->apiResponse));
		
		DirectedAcyclicGraph<URLNode, DefaultEdge> tree = newGraph.buildAPIGraph();

		for(URLNode node: tree.vertexSet()) {
			if(node.getPayloads().isEmpty()) {
				// Analyze only nodes that have responses.
				continue;
			}
			analyzeResponseSubtree(responseMapping, node, baseUrl, probesForUrl, knownUrls);
		}
		
		return probesForUrl.values().stream().collect(Collectors.toList());
	}

	/**
	 * Computes URLs based on known responses and builds ProbeEvents for URLs that are not available already.
	 * @param responseMapping
	 * @param node
	 * @param baseUrl
	 * @param probesForUrl
	 * @param knownUrls
	 */
	private void analyzeResponseSubtree(Map<Integer, APIResponse> responseMapping, URLNode node, String baseUrl, HashMap<String, ProbeEvent> probesForUrl, Set<String> knownUrls) {
		
		List<APIResponse> responsesForNode = node.getPayloads().stream().map(id -> responseMapping.get(id)).collect(Collectors.toList());
		
		if(baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length()-1);
		}
		
		for(APIResponse response: responsesForNode) {
			String nodeUrl = UtilsOASGen.getOnlyURL(response.getRequest().getRequestUrl());
			if(!nodeUrl.startsWith(baseUrl)){
				LOG.info("Ignoring node url {} because it is not in API domain {}", nodeUrl, baseUrl);
				continue;
			}
			Set<String> builtPathsForResponse = UtilsOASGen.buildUrlUsingAPIResponseData(response);
			if(builtPathsForResponse == null){
				LOG.info("Could not build paths for APIResponse {}", response);
				continue;
			}
			for(String builtPath: builtPathsForResponse) {
				String baseUrlStr = baseUrl + "/" + builtPath;
				if(!knownUrls.contains(baseUrlStr) && !probesForUrl.containsKey(baseUrlStr)) {
					probesForUrl.put(baseUrlStr, buildProbeEvent(MethodClazz.GET, baseUrlStr, response.getRequest().getHeaders(), null, ProbeType.RA));
				}

				String nodeUrlStr = nodeUrl + "/" + builtPath;
				if(!knownUrls.contains(nodeUrlStr) && !probesForUrl.containsKey(nodeUrlStr)){
					probesForUrl.put(nodeUrlStr, buildProbeEvent(MethodClazz.GET, nodeUrlStr, response.getRequest().getHeaders(), null, ProbeType.RA));
				}
			}
		}
	}

	/**
	 * Uses checkpoints in the original sequence to determine placement of probes in the final sequence
	 * @param probesToRun
	 * @param existingResponses
	 * @return
	 */
	private List<NetworkEvent> getCheckPointDepSequence(List<ProbeEvent> probesToRun,
			List<APIResponse> existingResponses) {
		List<ProbeEvent> GETProbes = probesToRun.stream().filter(probeEvent -> probeEvent.getMethod()!=MethodClazz.POST && probeEvent.getMethod()!=MethodClazz.PUT && probeEvent.getMethod()!=MethodClazz.DELETE)
				.collect(Collectors.toList());
		
		List<ProbeEvent> DELETEProbes =  probesToRun.stream().filter(probeEvent -> probeEvent.getMethod() == MethodClazz.DELETE)
				.collect(Collectors.toList());
		
		List<ProbeEvent> PUTProbes = probesToRun.stream().filter(probeEvent -> probeEvent.getMethod() == MethodClazz.PUT)
				.collect(Collectors.toList());
		
		List<ProbeEvent> POSTProbes = probesToRun.stream().filter(probeEvent -> probeEvent.getMethod() == MethodClazz.POST)
				.collect(Collectors.toList());
		
		List<NetworkEvent> GETExisting = existingResponses.stream().filter(existingResp -> existingResp.getRequest().getMethod()!=MethodClazz.POST && existingResp.getRequest().getMethod()!=MethodClazz.PUT && existingResp.getRequest().getMethod()!=MethodClazz.DELETE)
				.map(existingResp -> existingResp.getRequest()).collect(Collectors.toList());
		
		List<NetworkEvent> DELETEExisting =  existingResponses.stream().filter(existingResp -> existingResp.getRequest().getMethod() == MethodClazz.DELETE)
				.map(existingResp -> existingResp.getRequest()).collect(Collectors.toList());
		
		List<NetworkEvent> PUTExisting = existingResponses.stream().filter(existingResp -> existingResp.getRequest().getMethod() == MethodClazz.PUT)
				.map(existingResp -> existingResp.getRequest()).collect(Collectors.toList());
		
		List<NetworkEvent> POSTExisting = existingResponses.stream().filter(existingResp -> existingResp.getRequest().getMethod() == MethodClazz.POST)
				.map(existingResp -> existingResp.getRequest()).collect(Collectors.toList());
	
		
		HashMap<Integer, List<ProbeEvent>> location = new HashMap<>();
		
		List<NetworkEvent> eventSeq = new ArrayList<>();
		
		for(APIResponse existing: existingResponses) {
			switch(existing.getRequest().getMethod()) {
				case DELETE:
					existing.addCheckPoint(CheckPointType.OPERATION);
					continue;
				case PUT:
				case POST:
					existing.addCheckPoint(CheckPointType.OPERATION);
					eventSeq.addAll(POSTProbes);
					eventSeq.addAll(GETProbes);
					break;
				default:
					break;
			}
			
			if(existing.getResponse() != null && existing.getResponse().getHeaders() != null) {
				for(Header header: existing.getResponse().getHeaders()) {
					if (header.getName().equalsIgnoreCase("set-cookie")) {
						existing.addCheckPoint(CheckPointType.COOKIE);
						eventSeq.addAll(POSTProbes);
						eventSeq.addAll(GETProbes);
					}
				}
			}
			
			eventSeq.add(existing.getRequest()); // Delete should be skipped
		}
		eventSeq.addAll(PUTProbes); // Could affect delete. Probably delete probes should be called separately altogether
		eventSeq.addAll(DELETEProbes);
		eventSeq.addAll(DELETEExisting);
		
		
		return null;
	}
	
	public Status executeNormalEvent(CloseableHttpClient httpClient, NetworkEvent networkEvent, APIResponse returnResponse, Map<String, String> cookieCache) {
		boolean ignoreHeaders = this.subject==SUBJECT.tmf? true: false;
		LOG.info("Executing normal event {} with Id {}", networkEvent.getRequestUrl(), networkEvent.getId());

		Status status = APIRunner.executeRequest(httpClient, networkEvent, returnResponse, cookieCache, ignoreHeaders);
		return status;
	}

	public Status executeProbeEvent(CloseableHttpClient httpClient, ProbeEvent probeEvent, APIResponse returnResponse, Map<String, String> cookieCache) {
		LOG.info("Executing probe event {} with Id {}", probeEvent.getRequestUrl(), probeEvent.getId());

		executedProbeEvents.add(probeEvent);

		boolean ignoreHeaders = this.subject==SUBJECT.tmf? true: false;
		Status status = APIRunner.executeRequest(httpClient, probeEvent, returnResponse, cookieCache, ignoreHeaders);
		
		probeEvent.setProbeStatus(status);
		return status;
	}
	
}
