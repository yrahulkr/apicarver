package com.apicarv.testCarver.openAPIGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.KShortestSimplePaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Strings;
import com.apicarv.testCarver.apirecorder.LogEntry;
import com.apicarv.testCarver.apirecorder.NetworkEvent;
import com.apicarv.testCarver.apirecorder.NetworkEvent.EventClazz;
import com.apicarv.testCarver.apirecorder.NetworkEvent.MethodClazz;
import com.apicarv.testCarver.apirecorder.ResponseEvent;
import com.apicarv.testCarver.apirunner.APIResponse;
import com.apicarv.testCarver.utils.ResponseComparisonUtils;
import com.apicarv.testCarver.utils.Settings;
import com.apicarv.testCarver.utils.UtilsJson;
import com.apicarv.testCarver.utils.UtilsOASGen;
import com.apicarv.testCarver.utils.WorkDirManager;
import com.apicarv.testCarver.utils.WorkDirManager.DirType;

public class APIGraph {
	private static final Logger LOG = LoggerFactory.getLogger(APIGraph.class);
	private final Settings.SUBJECT subject;
	private String minedFolder;
	public APIGraph(Settings.SUBJECT subject, String minedFolder, String runPath) {
		this.subject = subject;
		this.setMinedFolder(minedFolder);
		this.runFolder = runPath;
		this.workDirManager = new WorkDirManager(subject, minedFolder, DirType.OAS, runFolder);
		apiResponses = workDirManager.getRunResults();
		logEntries = UtilsJson.importNetworkEventLog(workDirManager.getMinedAPIJson());
		this.rootNode = new URLNode("root");
		this.graph.addVertex(rootNode);
	}
	
	/**
	 * Create APIGraph with live APIResponses
	 *
	 * @param subject
	 * @param apiResponses
	 */
	public APIGraph(Settings.SUBJECT subject, List<APIResponse> apiResponses) {
		this.subject = subject;
		this.apiResponses = apiResponses;
		this.rootNode = new URLNode("root");
		this.graph.addVertex(rootNode);
	}
	
	
	DirectedAcyclicGraph<URLNode, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
	private WorkDirManager workDirManager;
	
	
	
	public WorkDirManager getWorkDirManager() {
		return workDirManager;
	}

	public void setWorkDirManager(WorkDirManager workDirManager) {
		this.workDirManager = workDirManager;
	}

	private String runFolder;

	
	HashMap<Integer, List<URLNode>> apiPathMap = new HashMap<>();

	private List<APIResponse> apiResponses;

	public List<APIResponse> getApiResponses() {
		return apiResponses;
	}
	
	private List<LogEntry> logEntries;
	private URLNode rootNode;
	
	private HashMap<String, String> computedURLMap = null;
	private Map<Integer, List<String>> varExamples  = new HashMap<>();

	public Map<Integer, List<String>> getVarExamples() {
		return varExamples;
	}

	public HashMap<String, String> getComputedURLMap() {
		if(computedURLMap == null) {
			getPathMapFromGraph();
		}
		return computedURLMap;
	}

	public void setComputedURLMap(HashMap<String, String> computedURLMap) {
		this.computedURLMap = computedURLMap;
	}

	
	
	public List<URLNode> parseAPIResponse(APIResponse api, Map<String, List<LogEntry>> requestMap, String payloadToCompare) {
		LOG.info("parsing {} - {}", api.getId(), api.getRequest().getRequestUrl());
		List<URLNode> graphPath = new ArrayList<>();
		
		NetworkEvent request = api.getRequest();
		ResponseEvent response = api.getResponse();
		String fullURL = request.getRequestUrl();
		String path = UtilsOASGen.getPathFromURL(fullURL);
		
		
		String[] pathSplit = path.split("/");
		
		URLNode currentNode = this.rootNode;
		for (int index = 0; index < pathSplit.length; index++) {
			String pathItem = pathSplit[index];
			
			LOG.debug("---------------------------------------Starting : %s - %s ----------------------------------------\n", pathItem, path);
			
			for(URLNode node: graph.vertexSet()) {
				LOG.debug("{}-{},", node.getId(), node.getPathItem());
			}

			if (pathItem.trim().isEmpty()) {
				continue;
			}

			// String pathItemMod = UtilsString.getGraphNode(pathItem, index);
			String parentPath = Strings.join("/", Arrays.copyOf(pathSplit, index));
			
			URLNode targetNode = new URLNode(ID.incrementAndGet(), pathItem, index, parentPath);
			if (index == pathSplit.length - 1) {
				// last node add request id and response data
				String requestId = request.getRequestId();
				targetNode.requestId = requestId;
				targetNode.setMethod(api.getRequest().getMethod());
				targetNode.isLeaf = true;
				String mediaType = "";
				if (response != null) {
					mediaType = (response.getResourceType());
				} else {
					mediaType = (request.getResourceType());
				}
				targetNode.responseType = mediaType;

				List<LogEntry> requestLogs = requestMap.get(request.getRequestId());
				if(requestLogs!=null) {
					for (LogEntry requestLog : requestLogs) {
						if (requestLog.getClazz() == EventClazz.RequestWillBeSent) {
							int stateBfore = requestLog.getCrawlStateBefore();
							targetNode.stateBefore = stateBfore;
	
							int stateAfter = requestLog.getCrawlStateAfter();
							targetNode.stateAfter = stateAfter;
	
							String event = requestLog.getEvent();
							targetNode.event = event;
						}
					}
				}
				targetNode.payloadToCompare = payloadToCompare;
				/*
				 try {
					String payloadToCompare = this.workDirManager.getPayload(api.getId());
					targetNode.payloadToCompare = payloadToCompare;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					targetNode.payloadToCompare = null;
				}
				 */
			}
			
			boolean added = false;
			if (graph.addVertex(targetNode)) {
				LOG.debug("added: " + pathItem + " - " + parentPath);
				added = true;
				graph.addEdge(currentNode, targetNode);
			} else {
				LOG.debug("not added: " + pathItem + " - " + parentPath);
				for (URLNode vertex : graph.vertexSet()) {
					if (vertex.equals(targetNode)) {
						graph.addEdge(currentNode, vertex);
						targetNode = vertex;
						break;
					}
				}
			}

			if (index == pathSplit.length - 1) {
				

				// Add payload ids
				targetNode.payloads.add(api.getId());
				
				if(targetNode.payloadToCompare==null) {
					targetNode.payloadToCompare = payloadToCompare;
				}
				if(!targetNode.isLeaf) {
					targetNode.isLeaf = true;
				}
				if(targetNode.responseType == null) {
					String mediaType = "";
					if (response != null) {
						mediaType = (response.getResourceType());
					} else {
						mediaType = (request.getResourceType());
					}
					targetNode.responseType = mediaType;
				}
				
				List<LogEntry> requestLogs = requestMap.get(request.getRequestId());
				if(requestLogs!=null) {
	
					for (LogEntry requestLog : requestLogs) {
						if (requestLog.getClazz() == EventClazz.RequestWillBeSent) {
							int candidateGroup = requestLog.getCandidateGroup();
							targetNode.candidateGroups.add(candidateGroup);
							break;
						}
					}
				}
			}
			graphPath.add(targetNode);
			
			// Increase visit count

			targetNode.visits.add(api.getId());
			
			currentNode = targetNode;
		}
		
		
		
		apiPathMap.put(api.getId(), graphPath);
		return graphPath;
	}
	AtomicInteger ID = new AtomicInteger(0);

	
	/**
	 * Gets payload from workdirmanager when available. Otherwise uses paylaod from the APIResponse
	 * @param requestMap
	 * @return
	 */
	public DirectedAcyclicGraph<URLNode, DefaultEdge> buildAPIGraph(Map<String, List<LogEntry>> requestMap) {

		// parse requests with valid response/payloads first
		List<APIResponse> noPayload = new ArrayList<APIResponse>();
		for (APIResponse api : apiResponses) {
			if(UtilsOASGen.getPathFromURL(api.getRequest().getRequestUrl())==null || api.getResponse() == null) {
				LOG.info("Invalid entry {}", api);
				continue;
			}
			
			// parse only GET rquests
			if(api.getRequest().getMethod() != MethodClazz.GET) {
				LOG.info("Ignoring API {} because it is not GET", api.getId());
				continue;
			}
			
			if(api.getResponse().getResourceType() == null || !( api.getResponse().getResourceType().contains("json") || api.getResponse().getResourceType().contains("xml"))){
				LOG.info("Ignoring API {} because resourceType is {}", api.getId(), api.getResponse().getResourceType());
				continue;
			}
			
			if(api.getResponse().getStatus() >= 400) {
				LOG.info("Ignoring API {} because it has status code {}", api.getId(), api.getResponse().getStatus());
				continue;
			}
			
			String payloadToCompare = api.getResponse().getData();
			if(payloadToCompare == null) {
				if(workDirManager != null) {
					try {
						payloadToCompare = this.workDirManager.getPayload(api.getId());
						if(payloadToCompare == null) {
							// No payload to compare for now. 
							noPayload.add(api);
							continue;
						}
						else{
							api.getResponse().setData(payloadToCompare);
						}
					} catch (IOException e) {
						e.printStackTrace();
						noPayload.add(api);
						continue;
					}
				}else {
					// LIVE APIResposne doesn't have a payload. 
					noPayload.add(api);
					continue;
				}
			}
			
			parseAPIResponse(api, requestMap, payloadToCompare);
		}
		
		for(APIResponse api: noPayload) {
			
			// parse only GET rquests
			if(api.getRequest().getMethod() != MethodClazz.GET) {
				LOG.info("Ignoring API {} because it is not GET", api.getId());
				continue;
			}
			
			// Adding no payload apis after the ones with payloads.
			parseAPIResponse(api, requestMap, null);
		}
		return graph;
	}

	public DirectedAcyclicGraph<URLNode, DefaultEdge> buildAPIGraph() {

		Map<String, List<LogEntry>> requestMap = new HashMap<>();
		// Map<String, List<NetworkEvent>> responseMap = new HashMap<>();
		
		
		if(logEntries!=null) {
			for (LogEntry event : logEntries) {
				if (event == null) {
					continue;
				}
				if (!requestMap.containsKey(event.getRequestId())) {
					List<LogEntry> newRequestList = new ArrayList<LogEntry>();
					requestMap.put(event.getRequestId(), newRequestList);
				}
	
				requestMap.get(event.getRequestId()).add(event);
			}
		}

		return buildAPIGraph(requestMap);
	}
	
	
	AtomicInteger nextVar = new AtomicInteger(0);

	
	/**
	 * 1. Differnt intermediate names, but same leaf node (already dome during graph construction)
	 *  - intermediate path parameters
	 * 2. Different name + same path index
	 * 	- Leaf path parameters
	 * 3. Mixed variables (have response like leaf node but also have children)
	 * - intermediate/ leaf parameters 
	 */
	public void pruneGraph() {
		List<URLNode> unmergedLeafNodes = new ArrayList<URLNode>();
		unmergedLeafNodes.addAll(graph.vertexSet());
		int maxHeight = 0;
		
		/*Comparator heightComparator = new Comparator<URLNode>() {

			@Override
			public int compare(URLNode o1, URLNode o2) {
				return o1.pathIndex-o2.pathIndex;
			}
		};
		*/
				
		for(URLNode unmerged: unmergedLeafNodes) {
			if(unmerged.pathIndex > maxHeight) {
				maxHeight = unmerged.pathIndex;
			}
		}
		for(int i=0; i<maxHeight+1; i++) {
			List<URLNode> toMerge = new ArrayList<URLNode>();
			for(URLNode unmerged: unmergedLeafNodes) {
				if(unmerged.pathIndex == i) {
					toMerge.add(unmerged);
				}
			}
			mergeSiblings(toMerge);
		}
	}
	
	/**
	 * return true if atleast one of the candidates used to trigger this API is common for the two nodes
	 * @param leafNode
	 * @param leafNode2
	 * @return
	 */
	private boolean haveCommonUIEvent(URLNode leafNode, URLNode leafNode2) {
		for(Integer group: leafNode.candidateGroups) {
			if(group < 0) {
				continue;
			}
			for(Integer group2: leafNode2.candidateGroups) {
				if(group == group2) {
					LOG.info("same candidate group {} - {},{}", group, leafNode.getParentPath()+"/"+leafNode.getPathItem(), leafNode2.getParentPath()+"/"+leafNode2.getPathItem());
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean similarNode(URLNode leafNode, URLNode leafNode2) throws Exception{
		
		String type1 = leafNode.getType();
		String type2 = leafNode2.getType();
		
		if(type1 == null || type2 == null || !type1.equalsIgnoreCase(type2)) {
			return false;
		}
		
		String payload1 = leafNode.getPayloadToCompare();
		String payload2 = leafNode2.getPayloadToCompare();
		
		if(payload1 == null || payload2 == null) {
			return false;
		}
		
		boolean responseCompare = ResponseComparisonUtils.comparePayloads(payload1, payload2, type1);
		
//		boolean candidateGroup = true;
		
		
		
		return responseCompare;
	}
	
	public List<List<URLNode>> mergeSiblings(List<URLNode> leafChildren) {
		List<List<URLNode>> merged = new ArrayList<List<URLNode>>();
		List<URLNode> unMappedList = new ArrayList<URLNode>();
		unMappedList.addAll(leafChildren);
		
		for(URLNode leafNode: leafChildren) {
			if(!unMappedList.contains(leafNode)) {
				continue;
			}
			
			int variable = nextVar.getAndIncrement();
			
			List<URLNode> similar = new ArrayList<URLNode>();
			similar.add(leafNode);
			
			for(URLNode unMapped: unMappedList) {
				if(leafNode.equals(unMapped)) {
					continue;
				}
				try {
					if(similarNode(leafNode, unMapped)) {
						unMapped.setVar(variable);
						similar.add(unMapped);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(similar.size() > 1) {
				leafNode.setVar(variable);
				varExamples.put(variable, new ArrayList<String>());
				for(URLNode similarNode: similar) {
					varExamples.get(variable).add(similarNode.getPathItem());
				}
			}
			merged.add(similar);
			for(URLNode similarNode: similar) {
				unMappedList.remove(similarNode);
			}
		}
		return merged;
	}
	
	public String parseGraphPath(List<URLNode> graphPath) {
		if(graphPath == null) {
			return null;
		}
		StringBuffer pathBuffer = new StringBuffer();
		for(URLNode node: graphPath) {
			pathBuffer.append("/");
			String pathItem = UtilsOASGen.parseURLNode(node);
			pathBuffer.append(pathItem);
		}
		return pathBuffer.toString();
	}
	
	/**
	 * Uses parseURLNode - the leaf variable inferred by comparing responses
	 * @param graphPath
	 * @return
	 */
	public String parseGraphPath(GraphPath<URLNode, DefaultEdge> graphPath) {
		if(graphPath == null) {
			return null;
		}
		StringBuffer pathBuffer = new StringBuffer();
		
		for(URLNode pathNode: graphPath.getVertexList()) {
			if(pathNode.getPathItem().equalsIgnoreCase("root")) {
				continue;
			}
			pathBuffer.append("/");
			String pathItem = UtilsOASGen.parseURLNode(pathNode);
			pathBuffer.append(pathItem);
		}
		return pathBuffer.toString();
	}

	public HashMap<URLNode, List<GraphPath<URLNode, DefaultEdge>>> getAllPossiblePaths(List<URLNode> nodes) {
		final HashMap<URLNode, List<GraphPath<URLNode, DefaultEdge>>> results = new HashMap<URLNode, List<GraphPath<URLNode, DefaultEdge>>>();
		final KShortestSimplePaths<URLNode, DefaultEdge> kPaths =
				new KShortestSimplePaths<>(this.graph,
						Integer.MAX_VALUE);

//		for (URLNode state : getDeepStates(rootNode)) { // not needed because this.graph is acyclic
		for(URLNode state: nodes) {
			List<GraphPath<URLNode, DefaultEdge>> paths =
					kPaths.getPaths(rootNode, state, Integer.MAX_VALUE);
			results.put(state, paths);
		}

		return results;
	}
	
	public HashMap<String, String> extractPathsFromGraph(){
		computedURLMap  = new HashMap<>();
		
		List<URLNode> leafNodes = new ArrayList<>();
		Map<String, List<URLNode>> leafNodeMap = new HashMap<>();
		
		for(URLNode node: graph.vertexSet()) {
			if(node.getPayloadToCompare()!=null) {
				leafNodes.add(node);
				String rep = UtilsOASGen.parseURLNode(node)+":"+node.getPathIndex();
				if(!leafNodeMap.containsKey(rep)) {
					leafNodeMap.put(rep, new ArrayList<>());
				}
				leafNodeMap.get(rep).add(node);
			}
		}
		
		HashMap<URLNode, List<GraphPath<URLNode, DefaultEdge>>> graphPaths = getAllPossiblePaths(leafNodes);

		for(String rep: leafNodeMap.keySet()) {
			String mergedPath;
			List<GraphPath<URLNode, DefaultEdge>> graphPathsCurr = new ArrayList<>();
			for(URLNode node: leafNodeMap.get(rep)) {
				if(!graphPaths.containsKey(node)) {
					LOG.warn("Graph paths not found for leaf node {}", node.getParentPath()+"/" + node.getPathItem());
					continue;
				}
				graphPathsCurr.addAll(graphPaths.get(node));
			}
			if(graphPathsCurr.size()>1) {
				mergedPath = getMergedPath(graphPathsCurr, rep.split(":")[0]);
			}
			else {
				mergedPath = parseGraphPath(graphPathsCurr.get(0));
			}
			for(URLNode node: leafNodeMap.get(rep)) {
				// The leaf node has more than one path
				for(Integer payload: node.getPayloads()) {
					String original = UtilsOASGen.getPathFromURL(getAPIURL(payload));
					computedURLMap.put(original, mergedPath);
				}
			}
		}
		/*for(URLNode node: graphPaths.keySet()) {
			String mergedPath;
			if(graphPaths.get(node).size()>1) {
				mergedPath = getMergedPath(graphPaths.get(node), node.getPathItem());
			}
			else {
				mergedPath = parseGraphPath(graphPaths.get(node).get(0));
			}
			// The leaf node has more than one path
			for(Integer payload: node.getPayloads()) {
				String original = getPathFromURL(getAPIURL(payload));
				computedURLMap.put(original, mergedPath);
			}
		}*/
				
		return computedURLMap;
	}
	
	private String getAPIURL(Integer payload) {
		for(APIResponse api: apiResponses) {
			if(api.getId() == payload) {
				return api.getRequest().getRequestUrl();
			}
		}
		return null;
	}

	/**
	 * Infers path parameters only based on multiple paths leading to the same leaf node
	 * @param list
	 * @param pathItemLast
	 * @return
	 */
	private String getMergedPath(List<GraphPath<URLNode, DefaultEdge>> list, String pathItemLast) {
		StringBuffer mergedPath = new StringBuffer();
			
		for(int pathIndex = 1; pathIndex < list.get(0).getLength(); pathIndex++) {
			String pathItem = null;
			int varStr = -1;
			for(GraphPath<URLNode, DefaultEdge> path: list) {
				String thisPathItem = path.getVertexList().get(pathIndex).getPathItem();
				if(pathItem == null) {
					pathItem = thisPathItem;
				}
				else { 
					if(!thisPathItem.equalsIgnoreCase(pathItem) ) {
						if(varStr<0) {
							varStr = nextVar.getAndIncrement(); 
							pathItem = "{var" + varStr + "}";
						}
						if(!varExamples.containsKey(varStr)) {
							varExamples.put(varStr, new ArrayList<String>());
						}
						varExamples.get(varStr).add(thisPathItem);
					}
				}
			}
			mergedPath.append("/");
			mergedPath.append(pathItem);
		}
		
		mergedPath.append("/");
		mergedPath.append(pathItemLast);
		
		return mergedPath.toString();
	}

	
	/**
	 * Are two intermediate nodes similar?
	 * 1. Use incoming and outgoing edges similarity
	 * 2. Predict missing incoming/outgoing edges 
	 * @return
	 */
	public void detectIntermediateVariables() {
		// bipartite partition every every level and find matching
		
	}
	
	public HashMap<String, String> getPathMapFromGraph(){
		computedURLMap  = new HashMap<>();
		
		for(APIResponse api: apiResponses) {
			
			String path = UtilsOASGen.getPathFromURL(api.getRequest().getRequestUrl());
			
			if(path==null) {
				LOG.warn("path of url is null : id-{}", api.getId());
				continue;
			}
			String graphPath = parseGraphPath(apiPathMap.get(api.getId()));
			if(graphPath == null) {
				LOG.warn("graph path is null for {}", path);
				continue;
			}
			
			if(computedURLMap.containsKey(path)) {
				if(!graphPath.equalsIgnoreCase(computedURLMap.get(path))) {
					LOG.warn("Found two graph paths for same URL path {} - {}", computedURLMap.get(path), graphPath);
				}
			}
			else {
				computedURLMap.put(path, graphPath);
			}
		}
		
		return computedURLMap;
	}
	
	
	
	public static void main(final String[] args) {
		APIGraph apiTree = new APIGraph(Settings.SUBJECT.petclinic, "20220324_012519", "/Users/apicarv/git/TestCarving/testCarver/out/petclinic-20220324_012519/run/20220324_090041");
		DirectedAcyclicGraph<URLNode, DefaultEdge> graph= apiTree.buildAPIGraph();
		apiTree.pruneGraph();
		apiTree.extractPathsFromGraph();
		
		WorkDirManager workDirManager = new WorkDirManager(Settings.SUBJECT.dummy, "testDAG", DirType.OAS);
		try {
			workDirManager.exportAPIGraph(graph, Settings.API_RAW_GRAPH_FILE, Settings.API_GRAPH_FILE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getMinedFolder() {
		// TODO Auto-generated method stub
		return minedFolder;
	}

	public void setMinedFolder(String minedFolder) {
		this.minedFolder = minedFolder;
	}

	public String getRunFolder() {
		// TODO Auto-generated method stub
		return runFolder;
	}

	public DirectedAcyclicGraph<URLNode, DefaultEdge> getGraph() {
		return graph;
	}

	

	public URLNode getTargetNode(String probePath) {
		String[] pathSplit = probePath.split("/");
		String pathItem = null;
		String parentPath = null;
		if(pathSplit.length == 0){
			pathItem = "";
			parentPath = "";
		}
		else{
			pathItem = pathSplit[pathSplit.length-1];
			parentPath = Strings.join("/", Arrays.copyOf(pathSplit, pathSplit.length-1));
		}
		

		// String pathItemMod = UtilsString.getGraphNode(pathItem, index);

		URLNode targetNode = new URLNode(ID.incrementAndGet(), pathItem, pathSplit.length-1, parentPath);
		
		for(URLNode vertex: graph.vertexSet()) {
			if(vertex.equals(targetNode)) {
				return vertex;
			}
		}
		return null;
	}
}

/*private List<URLNode> getDeepStates(URLNode state) {
final List<URLNode> deepStates = new ArrayList<>();

traverse(Sets.newHashSet(), deepStates, state);

return deepStates;
}

private void traverse(Set<Integer> visitedStates, List<URLNode> deepStates,
	URLNode state) {
visitedStates.add(state.getId());

Set<URLNode> outgoingSet = getOutgoingStates(state);

if ((outgoingSet == null) || outgoingSet.isEmpty()) {
	deepStates.add(state);
} else {
	if (cyclic(visitedStates, outgoingSet)) {
		deepStates.add(state);
	} else {
		for (URLNode st : outgoingSet) {
			if (!visitedStates.contains(st.getId())) {
				traverse(visitedStates, deepStates, st);
			}
		}
	}
}
}

private boolean cyclic(Set<Integer> visitedStates, Set<URLNode> outgoingSet) {
int i = 0;

for (URLNode state : outgoingSet) {
	if (visitedStates.contains(state.getId())) {
		i++;
	}
}

return i == outgoingSet.size();
}

public ImmutableSet<URLNode> getOutgoingStates(URLNode stateVertex) {
final Set<URLNode> result = new HashSet<>();

for (URLNode c : graph.getDescendants(stateVertex)) {
	result.add(c);
}

return ImmutableSet.copyOf(result);
}*/

