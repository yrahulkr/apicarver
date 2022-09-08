package com.apicarv.testCarver.openAPIGenerator;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.apicarv.testCarver.utils.*;
import com.apicarv.testCarver.utils.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.crawljax.util.XPathHelper;
import com.apicarv.testCarver.apirecorder.LogEntry;
import com.apicarv.testCarver.apirecorder.NetworkEvent;
import com.apicarv.testCarver.apirecorder.NetworkEvent.EventClazz;
import com.apicarv.testCarver.apirecorder.ResponseEvent;
import com.apicarv.testCarver.apirunner.APIResponse;
import com.apicarv.testCarver.utils.WorkDirManager.DirType;

import fj.Show;
import fj.data.Tree;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;

public class APITree {
	
	private static final String REQUEST_ID_ATTR = "id";
	private static final String EVENT_ATTR = "event";
	private static final String AFTER_ATTR = "after";
	private static final String BEFORE_ATTR = "before";
	private static final String VISITS_ATTR = "visits";
	private static final String PAYLOADS_ATTR = "payloads";
	private static final String CONTENT_TYPE_ATTR = "class";
	private static final String VARIABLE_ATTR = "variable";
	private WorkDirManager workDirManager;
	private Paths paths;
	private String runFolder;

	private List<APIResponse> apiResponses;

	Tree<String> root;

	private List<LogEntry> logEntries;

	public APITree(OpenAPI openAPI) {
		this.paths = openAPI.getPaths();
		this.apiResponses = null;
		root = Tree.node("root", fj.data.List.nil());
	}

	public APITree(List<APIResponse> apiResponses, List<LogEntry> logEntries) {
		this.apiResponses = apiResponses;
		this.logEntries = logEntries;
		this.paths = null;
	}

	public APITree(Settings.SUBJECT subject, String minedFolder, String runPath) {
		this.setWorkDirManager(new WorkDirManager(subject, minedFolder, DirType.OAS, runPath));
		this.runFolder = runPath;
		apiResponses = this.getWorkDirManager().getRunResults();
		logEntries = UtilsJson.importNetworkEventLog(this.getWorkDirManager().getMinedAPIJson());
	}

	void addPathToTree(String path) {
		if (path == null || path.isEmpty()) {
			System.out.printf("Invalid path %s \n", path);
			return;
		}
		String[] pathSplit = path.split("/");
		if (pathSplit.length == 0) {
			System.out.printf("could not split path %s \n", pathSplit.toString());
			return;
		}
		Tree parent = root;
		for (int index = 0; index < pathSplit.length; index++) {
			if (!pathSplit[index].equalsIgnoreCase((String) parent.root())) {
				System.out.printf("Something wrong %s and %s are not the same \n", pathSplit[index], parent.root());
			}

		}
	}

	public Document getAPITreeFromYaml() {
		Document apiTree;
		try {
			apiTree = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Node root = apiTree.createElement("root");
			apiTree.appendChild(root);

		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}

		for (String path : paths.keySet()) {
			String[] pathSplit = path.split("/");
			Node currentNode = (Node) apiTree.getDocumentElement();
			for (String pathItem : pathSplit) {
				if (pathItem.trim().isEmpty()) {
					continue;
				}
				String pathItemMod = UtilsAPIRunner.getNormalizedPathString(pathItem);

				Node targetNode = XPathHelper.getNodeFromSpecificParent(currentNode, pathItemMod + "[1]");
				if (targetNode == null) {
					try {
						// Create the node in the tree
						targetNode = apiTree.createElement(pathItemMod);
						currentNode.appendChild(targetNode);
					} catch (Exception ex) {
						System.out.println(pathItem + "  :  " + pathItemMod);
						ex.printStackTrace();
					}
				}
				currentNode = targetNode;
			}
		}

		return apiTree;
	}
	

	public Document getAPITree() {

		Document apiTree;
		try {
			apiTree = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Node root = apiTree.createElement("root");
			apiTree.appendChild(root);

		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
		Map<String, List<LogEntry>> requestMap = new HashMap<>();
		// Map<String, List<NetworkEvent>> responseMap = new HashMap<>();

		for (LogEntry event : logEntries) {
			if(event == null) {
				continue;
			}
			if (!requestMap.containsKey(event.getRequestId())) {
				List<LogEntry> newRequestList = new ArrayList<LogEntry>();
				requestMap.put(event.getRequestId(), newRequestList);
			}

			requestMap.get(event.getRequestId()).add(event);
		}

		for (APIResponse api : apiResponses) {
			NetworkEvent request = api.getRequest();
			ResponseEvent response = api.getResponse();
			URL urlObj = null;
			try {
				urlObj = new URL(request.getRequestUrl());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			String path = urlObj.getPath();
			if (path.endsWith("/") && path.length() > 1) {
				path = path.substring(0, path.length() - 1);
			}
			String[] pathSplit = path.split("/");
			Node currentNode = (Node) apiTree.getDocumentElement();
			for (int index = 0; index < pathSplit.length; index++) {
				String pathItem = pathSplit[index];

				if (pathItem.trim().isEmpty()) {
					continue;
				}

				String pathItemMod = UtilsAPIRunner.getNormalizedPathString(pathItem);

				Node targetNode = XPathHelper.getNodeFromSpecificParent(currentNode, pathItemMod + "[1]");
				
				if (targetNode == null) {
					try {
						// Create the node in the tree
						targetNode = apiTree.createElement(pathItemMod);
						currentNode.appendChild(targetNode);
						if (index == pathSplit.length - 1) {
							// last node add request id and response data
							Node idAttr = apiTree.createAttribute(REQUEST_ID_ATTR);
							idAttr.setNodeValue(request.getRequestId());
							targetNode.getAttributes().setNamedItem(idAttr);
							
							Node classAttr = apiTree.createAttribute(CONTENT_TYPE_ATTR);
							if(response!=null) {
								classAttr.setNodeValue(response.getResourceType());
							}
							else {
								classAttr.setNodeValue(request.getResourceType());
							}
							targetNode.getAttributes().setNamedItem(classAttr);
							List<LogEntry> requestLogs = requestMap.get(request.getRequestId());
							for(LogEntry requestLog: requestLogs) {
								if(requestLog.getClazz() == EventClazz.RequestWillBeSent){
									Node before = apiTree.createAttribute(BEFORE_ATTR);
									before.setNodeValue("" + requestLog.getCrawlStateBefore());
									targetNode.getAttributes().setNamedItem(before);
									Node after = apiTree.createAttribute(AFTER_ATTR);
									after.setNodeValue("" + requestLog.getCrawlStateAfter());
									targetNode.getAttributes().setNamedItem(after);
									Node action = apiTree.createAttribute(EVENT_ATTR);
									action.setNodeValue(requestLog.getEvent());
									targetNode.getAttributes().setNamedItem(action);
								}
							}
						}
						
					} catch (Exception ex) {
						System.out.println(pathItem + "  :  " + pathItemMod);
						ex.printStackTrace();
					}
				}
				if (index == pathSplit.length - 1) {
					// Increase visit count
					if(targetNode.getAttributes().getNamedItem(VISITS_ATTR) == null) {
						Node visitCount = apiTree.createAttribute(VISITS_ATTR);
						visitCount.setNodeValue(""+1);
						targetNode.getAttributes().setNamedItem(visitCount);
					}
					else {
						int visitCount =  Integer.parseInt(targetNode.getAttributes().getNamedItem(VISITS_ATTR).getNodeValue());
						visitCount = visitCount + 1;
						targetNode.getAttributes().getNamedItem(VISITS_ATTR).setNodeValue("" + visitCount);
					}
					
					// Add payload ids
					if(targetNode.getAttributes().getNamedItem(PAYLOADS_ATTR) == null) {
						Node payloads = apiTree.createAttribute(PAYLOADS_ATTR);
						payloads.setNodeValue(""+api.getId());
						targetNode.getAttributes().setNamedItem(payloads);
					}
					else {
						String payloads =  targetNode.getAttributes().getNamedItem(PAYLOADS_ATTR).getNodeValue();
						payloads = payloads + "_" + api.getId();
						targetNode.getAttributes().getNamedItem(PAYLOADS_ATTR).setNodeValue(payloads);
					}
				}
				currentNode = targetNode;
			}
		}
		return apiTree;
	}
	
	public String getPayload(Node node) {
		try{
			String payloads = node.getAttributes().getNamedItem(PAYLOADS_ATTR).getNodeValue();
			int payload = Integer.parseInt(payloads.split("_")[0]);
			return this.workDirManager.getPayload(payload);
		}catch(Exception ex) {
			return null;
		}
	}
	
	/**
	 * Modifies the nodes within provided document instance
	 * @param apiTree
	 * @return
	 */
	public Document getAPITreeWithVariables(Document apiTree) {
		List<Node> merged = new ArrayList<>(); 
		merged.add(apiTree.getDocumentElement());
		detectChildVariables(merged);
		return apiTree;
	}

	// Assuming the leaf and intermediate nodes are the same variable 
	/* for example: 
	 * 		merge(a/b1 , a/b2) -> a/{b}
	 * 		=>	merge(a/b1/c/d1, a/b1/c/d2) -> a/{b}/c/d1, a/{b}/c/d2
	 */
	public void detectChildVariables(List<Node> mergedNodes) {
		// gather siblings at each level
		List<Node> leafChildren = new ArrayList<>();
		List<Node> nonLeafChildren = new ArrayList<>();
		for(Node node: mergedNodes) {
			NodeList childNodes = node.getChildNodes();
			for(int i=0; i<childNodes.getLength();i++) {
				Node childNode = childNodes.item(i);
				if(childNode.hasAttributes() && childNode.getAttributes().getNamedItem(REQUEST_ID_ATTR) != null) {
					// leaf node
					leafChildren.add(childNode);
				}
				else {
					nonLeafChildren.add(childNode);
				}
			}
		}
		
		List<List<Node>> merged = mergeSiblings(leafChildren);
		
		for(Node nonLeaf: nonLeafChildren) {
			List<Node> nonLeafList = new ArrayList<>();
			nonLeafList.add(nonLeaf);
			merged.add(nonLeafList);
		}
		
		for(List<Node> mergedItems: merged) {
			detectChildVariables(mergedItems);
		}
	}
	
	
	AtomicInteger nextVar = new AtomicInteger(0);

	public List<List<Node>> mergeSiblings(List<Node> leafChildren) {
		List<List<Node>> merged = new ArrayList<List<Node>>();
		List<Node> unMappedList = new ArrayList<Node>();
		unMappedList.addAll(leafChildren);
		
		for(Node leafNode: leafChildren) {
			if(!unMappedList.contains(leafNode)) {
				continue;
			}
			
			int variable = nextVar.getAndIncrement();
			
			List<Node> similar = new ArrayList<Node>();
			similar.add(leafNode);
			
			for(Node unMapped: unMappedList) {
				if(leafNode.isSameNode(unMapped)) {
					continue;
				}
				try {
					if(similarNode(leafNode, unMapped)) {
						Node varAttr = unMapped.getOwnerDocument().createAttribute(VARIABLE_ATTR);
						varAttr.setNodeValue(""+variable);
						unMapped.getAttributes().setNamedItem(varAttr);
						similar.add(unMapped);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(similar.size() > 1) {
				Node varAttr = leafNode.getOwnerDocument().createAttribute(VARIABLE_ATTR);
				varAttr.setNodeValue(""+variable);
				leafNode.getAttributes().setNamedItem(varAttr);
			}
			merged.add(similar);
			for(Node similarNode: similar) {
				unMappedList.remove(similarNode);
			}
		}
		return merged;
	}

	private boolean similarNode(Node leafNode, Node leafNode2) throws Exception{
		
		String type1 = leafNode.getAttributes().getNamedItem(CONTENT_TYPE_ATTR).getNodeValue();
		String type2 = leafNode.getAttributes().getNamedItem(CONTENT_TYPE_ATTR).getNodeValue();
		
		if(!type1.equalsIgnoreCase(type2)) {
			return false;
		}
		
		String payload1 = getPayload(leafNode);
		String payload2 = getPayload(leafNode2);
		
		return ResponseComparisonUtils.comparePayloads(payload1, payload2, type1);
	}
	

	public static void main(final String[] args) {
		fj.data.List<Integer> a = fj.data.List.list(1, 2, 3).map(i -> i + 42);
		Show.listShow(Show.intShow).println(a); // [43,44,45]

		fj.data.List<Tree<String>> forest = fj.data.List.nil();
		forest = forest.cons(Tree.node("admin", fj.data.List.nil()));
		Show.listShow(Show.treeShow(Show.stringShow)).print(forest);
		Tree<String> tree = Tree.node("petclinic", forest);

		Show.treeShow(Show.stringShow).print(tree);
		// combined into a single line
		// listShow(intShow).println(list(1, 2, 3).map(i -> i + 42)); // [43,44,45]

		/*
		 * { OpenAPI openAPI = new OpenAPIV3Parser().read(
		 * "/Users/apicarv/git/TestCarving/testCarver/out/petclinic-20211110_213335/oas/20211110_213843/oas.yaml"
		 * ); APITree apiTree = new APITree(openAPI); // apiTree.getVariableList();
		 * 
		 * Document docTree = apiTree.getAPITreeFromYaml(); }
		 */

		{
			String devOutput = "/Users/apicarv/git/TestCarving/testCarver/out/petclinic-20220208_014522/devToolsOutput.json";
			String jsonFile = "/Users/apicarv/git/TestCarving/testCarver/out/petclinic-20220208_014522/run/20220208_014628/resultResponses.json";

			/*List<APIResponse> apiResponses = UtilsJson.importAPIResponses(jsonFile);
			List<LogEntry> logEntries = UtilsJson.importNetworkEventLog(devOutput);

			APITree apiTree = new APITree(apiResponses, logEntries);
			Document docTree = apiTree.getAPITree();*/
			
			APITree apiTree = new APITree(Settings.SUBJECT.petclinic,"20220211_002937", "/Users/apicarv/git/TestCarving/testCarver/out/petclinic-20220211_002937/run/20220211_003444");
			Document docTree = apiTree.getAPITree();
			
			apiTree.getAPITreeWithVariables(docTree);
			
			WorkDirManager workDirManager = new WorkDirManager(Settings.SUBJECT.dummy, "test", DirType.OAS);
			try {
				workDirManager.exportAPITree(docTree);
			} catch (TransformerException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public WorkDirManager getWorkDirManager() {
		return workDirManager;
	}

	public void setWorkDirManager(WorkDirManager workDirManager) {
		this.workDirManager = workDirManager;
	}
}
