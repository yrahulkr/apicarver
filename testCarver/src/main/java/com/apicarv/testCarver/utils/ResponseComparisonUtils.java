package com.apicarv.testCarver.utils;

import java.io.IOException;
import java.util.Comparator;

import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

import com.crawljax.stateabstractions.dom.RTED.LblTree;
import com.crawljax.stateabstractions.dom.RTED.RTEDUtils;
import com.crawljax.stateabstractions.dom.RTED.RTED_InfoTree_Opt;
import com.crawljax.util.DomUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseComparisonUtils {
	private static LblTree getDomTree(String dom1) throws IOException {

		org.w3c.dom.Document doc1 = DomUtils.asDocument(dom1);

		LblTree domTree = null;

		DocumentTraversal traversal = (DocumentTraversal) doc1;
		TreeWalker walker = traversal.createTreeWalker(doc1,
				NodeFilter.SHOW_ELEMENT, null, true);
		domTree = createTree(walker);

		return domTree;
	}

	private static LblTree createTree(TreeWalker walker) {
		Node parent = walker.getCurrentNode();
		LblTree node = new LblTree(parent.getNodeName(), -1); // treeID = -1
		for (Node n = walker.firstChild(); n != null; n = walker.nextSibling()) {
			node.add(createTree(walker));
		}
		walker.setCurrentNode(parent);
		return node;
	}

	public static double getRobustTreeEditDistance(String xml1, String xml2) {

		LblTree domTree1 = null, domTree2 = null;
		
		try {
			domTree1 = getDomTree(xml1);
			domTree2 = getDomTree(xml2);
		} catch (IOException e) {
			e.printStackTrace();
		}

		double DD = 0.0;
		RTED_InfoTree_Opt rted;
		double ted;

		rted = new RTED_InfoTree_Opt(1, 1, 1);

		// compute tree edit distance
		rted.init(domTree1, domTree2);

		int maxSize = Math.max(domTree1.getNodeCount(), domTree2.getNodeCount());

		rted.computeOptimalStrategy();
		ted = rted.nonNormalizedTreeDist();
		ted /= (double) maxSize;

		DD = ted;
		return DD;
	}
	
	static boolean compareXml(String payload1, String payload2) {
		return getRobustTreeEditDistance(payload1, payload2) == 0? true: false;
	}

	static boolean compareJson(String payload1, String payload2) throws JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Comparator<JsonNode> comparator = new Comparator<JsonNode>() {
			@Override
			public int compare(JsonNode o1, JsonNode o2) {
				if(o1.getNodeType() == o2.getNodeType()) {
					return 0;
				}
				return 1;
			}
		};
		return mapper.readTree(payload1).equals(comparator, mapper.readTree(payload2));
//		return false;
	}

	static boolean compareText(String payload1, String payload2) {
		return payload1.equalsIgnoreCase(payload2);
	}

	private static boolean compareDOM(String payload1, String payload2) throws IOException {
		return RTEDUtils.getRobustTreeEditDistance(payload1, payload2) == 0? true: false;
	}
	
	public static boolean comparePayloads(String payload1, String payload2, String format) throws IOException {
		if(format.toLowerCase().contains("html")) {
			return compareDOM(payload1, payload2);
		}
		
		if(format.toLowerCase().contains("text")) {
			return compareText(payload1, payload2);
		}
		
		if(format.toLowerCase().contains("json")){
			return compareJson(payload1, payload2);
		}
		
		if(format.toLowerCase().contains("xml")) {
			return compareXml(payload1, payload2);
		}

		System.out.println("Unknown payload format");
		
		return false;
	}

}
