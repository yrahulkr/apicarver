package com.apicarv.testCarver.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import com.apicarv.testCarver.apirecorder.NetworkEvent;
import com.apicarv.testCarver.apirunner.APIResponse;
import com.apicarv.testCarver.openAPIGenerator.URLNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.crawljax.util.DomUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

public class UtilsOASGen {
	private static final Logger LOG = LoggerFactory.getLogger(UtilsOASGen.class);

	/**
	 * Returns names based on the leaf variable inferred by comparing responses
	 * @param node
	 * @return
	 */
	public static String parseURLNode(URLNode node) {
		String pathItem;
		if(node.getVar() >= 0) {
			pathItem = "{var" + node.getVar() + "}";
		}
		else {
			pathItem = node.getPathItem();
		}
		return pathItem;
	}

	public static String getPathFromURL(String URL) {
		URL urlObj = null;
		try {
			urlObj = new URL(URL);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		String path = urlObj.getPath();
		if (path.endsWith("/") && path.length() > 1) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	public static String getOnlyURL(String url) {
		try {
			URL original = new URL(url);
			return new URL(original.getProtocol(), original.getHost(), original.getPort(), original.getPath()).toString();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
	
	public static void parsingJsonObject(JsonNode objectNode, Set<String> returnSet) {

		Iterator<Entry<String, JsonNode>> iter = objectNode.fields();
		
		while(iter.hasNext()) {
			Entry<String, JsonNode> field = iter.next();
			
			String propName = field.getKey();
			returnSet.add(propName);
			switch(field.getValue().getNodeType()) {
				case ARRAY:
					JsonNode propVal = field.getValue();
					Set<String> childSet = new HashSet<>();
					buildUrlUsingJsonObject(propVal, childSet);

					for(String childSetString: childSet) {
						returnSet.add(propName + "/" + childSetString);
						returnSet.add(childSetString);
					}
					break;
				case OBJECT:
					propVal = field.getValue();
					childSet = new HashSet<>();
					buildUrlUsingJsonObject(propVal, childSet);
					for(String childSetString: childSet) {
						returnSet.add(propName + "/" + childSetString);
						returnSet.add(childSetString);
					}
					break;
				default:
					String propValue = field.getValue().asText();
					if(propValue.matches("\\S+")) { // Ignore values with spaces
						returnSet.add(propValue);
						returnSet.add(propName + "/" + propValue);
					}
					break;
				}
		}
	}
	
	public static void buildUrlUsingJsonObject(JsonNode node, Set<String> returnSet){
		
		
		
//			if(parseTree.getNodeType() == JsonNodeType.STRING ) {
//				List<NameValuePair> dataEntries = UtilsAPIRunner.getPostData(data);
//				for (NameValuePair pair : dataEntries) {
//					returnList.add(pair.getName());
//					returnList.add(pair.getValue());
//					returnList.add(pair.getName()+"/"+pair.getValue());
//					returnList.add(pair.getValue() + "/" + pair.getName());
//				}
//			}
		if (node.getNodeType() == JsonNodeType.OBJECT) {
			parsingJsonObject(node, returnSet);
		}
		else if(node.getNodeType() == JsonNodeType.ARRAY) {
			/*
			Limitting the number of probes by removing duplicates in response structures
			 */
			int size = 0;
			if(Settings.RA_INCLUDE_ARRAY_ELEMENTS){
				size = node.size();
			}
			else{
				size = (node!=null && node.size()>=1)?1:0;
			}
			for(int i=0; i< size; i++) {
				Set<String> childSet = new HashSet<>();
				parsingJsonObject(node.get(i), childSet);
				returnSet.addAll(childSet);
			}
		}
		else {
			returnSet.add(node.asText());
		}
		
	}
	
	public static void buildUrlUsingXmlObject(Node node, Set<String> returnSet) {
		returnSet.add(node.getNodeName());
		
		org.w3c.dom.NodeList children = node.getChildNodes();
		
		if(children.getLength() == 0) {
			//Leaf node
			if(!node.getNodeValue().matches("\\S+")){
				returnSet.add(node.getNodeValue());
				returnSet.add(node.getNodeName() + "/" + node.getNodeValue());
				returnSet.add(node.getNodeValue() + "/" + node.getNodeValue());
			}
			return;
		}
		

		for(int i =0; i< children.getLength(); i++) {
			Set<String> childSet = new HashSet<>();
			buildUrlUsingXmlObject(children.item(i), childSet);
			for(String childSetString: childSet) {
				returnSet.add(node.getNodeName() + "/" + childSetString);
				returnSet.add(childSetString);
			}
		}
	}
	
	public static Set<String> buildUrlUsingAPIResponseData(APIResponse apiResponse) {

		if(apiResponse == null || apiResponse.getRequest() == null || apiResponse.getResponse() == null
				|| apiResponse.getRequest().getResourceType() == null){
			LOG.info("Invalid APIResponse. Cannot build URL for {}", apiResponse);
			return null;
		}

		NetworkEvent request = apiResponse.getRequest();
		String data = apiResponse.getResponse().getData();
		if(data == null) {
			LOG.info("Data is null for {}", apiResponse);
			return null;
		}

		
		Set<String> returnList = new HashSet<>();
		
		if(request.getResourceType().contains("json")) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode parseTree = null;
			try {
				parseTree = mapper.readTree(data);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

			buildUrlUsingJsonObject(parseTree, returnList);	
		}
		
		else if(request.getResourceType().contains("xml")) {
			try {
				org.w3c.dom.Document doc1 = DomUtils.asDocument(data);
				buildUrlUsingXmlObject(doc1, returnList);
			} catch (IOException e) {
				LOG.error("Cannot parse post data {} as xml", data );
			}
		}
		
		return returnList;
	}

	/**
	 * Only handles apiResponses with valid response and status (200-399)
	 * @param apiResponse
	 * @return
	 */
    public static boolean isRestAPI(APIResponse apiResponse) {
		if(apiResponse.getRequest().getMethod() != NetworkEvent.MethodClazz.GET){
			// The resourceType may not matter
			return true;
		}
		String resourceType = apiResponse.getResponse().getResourceType();
		if(resourceType == null){
			LOG.error("resourcetype is {}", resourceType);
			return false;
		}
		return resourceType.toLowerCase().contains("json") || resourceType.toLowerCase().contains("xml");
    }

	public static boolean isGoodServerStatus(int status) {
		return status>=200 && status<400;
	}

	/**
	 * Even internal error should be counted as a good server status for oas generation
	 * @param status
	 * @return
	 */
	public static boolean isValidServerStatus(int status) {
		return (status>=200 && status<400) || (status>=500 && status<600);
	}

	public static String removeBaseFromPath(String path, String base) {
		if (!path.startsWith(base)){
			LOG.error("The path {} does not start with {}", path, base);
			return null;
		}
		return path.substring(base.length());
	}
}
