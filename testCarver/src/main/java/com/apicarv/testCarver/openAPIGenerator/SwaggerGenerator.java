package com.apicarv.testCarver.openAPIGenerator;

import com.apicarv.testCarver.utils.Settings;
import com.apicarv.testCarver.utils.UtilsAPIRunner;
import com.apicarv.testCarver.utils.UtilsOASGen;
import com.apicarv.testCarver.utils.WorkDirManager;
import com.beust.jcommander.internal.Lists;
import com.crawljax.util.DomUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.base.Functions;
import com.apicarv.testCarver.Main;
import com.apicarv.testCarver.apirecorder.NetworkEvent;
import com.apicarv.testCarver.apirecorder.ResponseEvent;
import com.apicarv.testCarver.apirunner.APIResponse;
import com.apicarv.testCarver.utils.*;
import com.apicarv.testCarver.utils.Settings.SUBJECT;
import com.apicarv.testCarver.utils.WorkDirManager.DirType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class SwaggerGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(SwaggerGenerator.class);

	private String hostUrl;
	private List<APIResponse> apiResponses;

	public String getHostUrl() {
		return hostUrl;
	}

	public void setHostUrl(String hostUrl) {
		this.hostUrl = hostUrl;
	}

	private WorkDirManager workDirManager;

	private HashMap<String, List<PathItem>> pathItemMap;
	
	private APIGraph apiGraph;
	private SUBJECT subject;

	private String minedFolder;

	private String runPath;

	public SwaggerGenerator(SUBJECT subject, String minedFolder, String runPath, String hostUrl) {
		this.subject = subject;
		this.hostUrl = hostUrl;
		this.minedFolder = minedFolder;
		this.runPath = runPath;
		apiGraph = new APIGraph(subject, minedFolder, runPath);
		workDirManager = new WorkDirManager(subject, minedFolder, DirType.OAS, runPath);
		apiResponses = apiGraph.getApiResponses();
	}

	/**
	 *
	 * @param subject
	 * @param apiResponses
	 * @param workDirManager
	 */
	public SwaggerGenerator(SUBJECT subject, String hostUrl, List<APIResponse> apiResponses, WorkDirManager workDirManager){
		this.subject = subject;
		this.hostUrl = hostUrl;
		this.apiResponses = apiResponses;
		this.apiGraph = new APIGraph(subject, apiResponses);
		this.workDirManager = workDirManager;
//		try {
//			DirectedAcyclicGraph<URLNode, DefaultEdge> graph = apiGraph.buildAPIGraph();
//			apiGraph.pruneGraph();
//			HashMap<String, String> urlMap = apiGraph.extractPathsFromGraph();
//			workDirManager.exportURLMap(urlMap, Settings.URL_MAP_JSON);
//			workDirManager.exportAPIGraph(graph, Settings.API_RAW_GRAPH_FILE, Settings.API_GRAPH_FILE);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	public void loadPayloads(List<APIResponse> apiResponses, boolean prober){
		for(APIResponse response: apiResponses){
			try {
				String payload = null;
				if(prober){
					payload = workDirManager.getProberPayload(response.getId());
				}
				else{
					payload = workDirManager.getPayload(response.getId());
				}
//				if(payload == null){
//					throw new RuntimeException("Cannot get payloads");
//				}
				response.getResponse().setData(payload);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	public void rerunSwaggerGeneration() throws MalformedURLException, JsonProcessingException {
		loadPayloads(apiResponses, false);
		DirectedAcyclicGraph<URLNode, DefaultEdge> graph = apiGraph.buildAPIGraph();
		apiGraph.pruneGraph();
//		HashMap<String, String> urlMap = apiGraph.getComputedURLMap();
		HashMap<String, String> urlMap = apiGraph.extractPathsFromGraph();

		try {
			workDirManager.exportURLMap(urlMap, Settings.URL_MAP_JSON);

			workDirManager.exportAPIGraph(graph, Settings.API_RAW_GRAPH_FILE, Settings.API_GRAPH_FILE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OpenAPI openAPI = getOpenAPI(hostUrl, apiResponses);
//		System.out.println(openAPI);
		workDirManager.exportOAS(openAPI, Settings.OAS_EXPORT_FILE);


		List<APIResponse> resultResponses = workDirManager.getProberRunResults();
		loadPayloads(resultResponses, true);
//		apiGraph.pruneGraph();
//
//		apiGraph.extractPathsFromGraph();


		APIGraph newGraph = new APIGraph(subject, resultResponses);

		DirectedAcyclicGraph<URLNode, DefaultEdge> newGraphTree = newGraph.buildAPIGraph();
		newGraph.pruneGraph();
		urlMap = newGraph.extractPathsFromGraph();

		try {
			workDirManager.exportURLMap(urlMap, Settings.PROBED_URL_MAP_JSON);

			workDirManager.exportAPIGraph(newGraphTree, Settings.PROBED_API_RAW_GRAPH_FILE, Settings.PROBED_API_GRAPH_FILE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//
//		List<APIResponse> allResponses = new ArrayList<>();
//		allResponses.addAll(apiResponses);
//		allResponses.addAll(resultResponses);
//

		this.apiGraph = newGraph;

		OpenAPI openAPI_withProbe = getOpenAPI(hostUrl, resultResponses);

		workDirManager.exportOAS(openAPI_withProbe, Settings.PROBED_OAS_EXPORT_FILE);

	}


	public WorkDirManager getWorkDirManager() {
		return workDirManager;
	}

	public void setWorkDirManager(WorkDirManager workDirManager) {
		this.workDirManager = workDirManager;
	}





	/**
	 * 
	 * @param api
	 * @return
	 * @throws MalformedURLException
	 */
	public static PathItem getPathItemForRequest(APIResponse api) throws MalformedURLException, NullPointerException {
		NetworkEvent request = api.getRequest();
		ResponseEvent response = api.getResponse();
		PathItem item = new PathItem();
		Operation op = new Operation();

		try {
			ApiResponses responses = getResponseSpec(request.getRequestId(), response);
			op.setResponses(responses);
		} catch (Exception ex) {
			LOG.error("Error getting Response spec for {} {}", api, response);
			ex.printStackTrace();
		}

		List<Parameter> params = getParamSpecForRequest(request);

		op.setParameters(params);

		switch (request.getMethod()) {
		case GET:
			item.setGet(op);
			break;
		case POST:
			item.setPost(op);
			break;
		case PUT:
			item.setPut(op);
			break;
		case DELETE:
			item.setDelete(op);
			break;
		case PATCH:
			item.setPatch(op);
			break;
		case OPTIONS:
			item.setOptions(op);
			break;
		case HEAD:
			item.setHead(op);
			break;
		case TRACE:
			item.setTrace(op);
			break;
		default:
			item = null;
			break;
		}
		
		if(item!=null) {
			String postData = request.getPostData();
			RequestBody requestBody = new RequestBody();

			if (postData != null && !postData.isEmpty()) {
				requestBody.setRequired(true);
				Content content = getContentForPostData(request, postData);
				requestBody.setContent(content);
				op.setRequestBody(requestBody);
			}
		}

		return item;
	}

	private static List<Parameter> getParamSpecForRequest(NetworkEvent request) throws MalformedURLException {
		List<Parameter> params = new ArrayList<Parameter>();

		/* Set Query Parameters */
		String query = (new URL(request.getRequestUrl())).getQuery();
		LOG.info("Query string for event {}", query);
		if (query != null) {
			List<NameValuePair> queryPairs = UtilsAPIRunner.getPostData(query);
			for (NameValuePair queryPair : queryPairs) {
				Parameter param = new QueryParameter();
				Schema paramSchema = new StringSchema();
				param.setSchema(paramSchema);
				param.setRequired(true);
				param.setName(queryPair.getName());

				Map<String, Example> examples = new HashMap<>();
				Example example = new Example();
				example.setValue(queryPair.getName() + "=" + queryPair.getValue());
				examples.put(request.getRequestId(), example);
				param.setExamples(examples);

				params.add(param);
			}
		}

		/* Set Header Parameters */
		if (request.getHeaders() != null) {
			for (Header header : request.getHeaders()) {

				if (header.getName().equalsIgnoreCase("cookie")) {
					/* Set Cookie Parameters */
					List<NameValuePair> cookies = UtilsAPIRunner.getCookiePairs(header.getValue());
					for (NameValuePair queryPair : cookies) {
						Parameter param = new CookieParameter();
						Schema paramSchema = new StringSchema();
						param.setSchema(paramSchema);
						param.setRequired(true);
						param.setName(queryPair.getName());
						// param.setExample(queryPair.getName() + "=" + queryPair.getValue());

						Map<String, Example> examples = new HashMap<>();
						Example example = new Example();
						example.setValue(queryPair.getName() + "=" + queryPair.getValue());
						examples.put(request.getRequestId(), example);
						param.setExamples(examples);

						params.add(param);
					}
				} else {
					// Ignore other headers unless specified
					if(Settings.NO_HEADERS_SWAGGER) {
						continue;
					}
					Parameter param = new HeaderParameter();
					Schema paramSchema = new StringSchema();
					param.setSchema(paramSchema);
					param.setRequired(false);
					param.setName(header.getName());
					// param.setExample(header.getName() + "=" + header.getValue());

					Map<String, Example> examples = new HashMap<>();
					Example example = new Example();
					example.setValue(header.getName() + "=" + header.getValue());
					examples.put(request.getRequestId(), example);
					param.setExamples(examples);

					params.add(param);
				}
			}
		}
		return params;
	}

	private static ApiResponses getResponseSpec(String requestId, ResponseEvent response) {
		/* Set Response */

		ApiResponses responses = new ApiResponses();
		ApiResponse resp = new ApiResponse();
		resp.setDescription("recorded response for request id " + response.getRequestId());
		for (Header header : response.getHeaders()) {
			if(Settings.NO_HEADERS_SWAGGER && !header.getName().equalsIgnoreCase("cookie")) {
				continue;
			}
			io.swagger.v3.oas.models.headers.Header modelHeader = new io.swagger.v3.oas.models.headers.Header();
			modelHeader.setSchema(new StringSchema());
			resp.addHeaderObject(header.getName(), modelHeader);
		}
		Content respContent = new Content();
		MediaType respMediaType = new MediaType();
		Schema respSchema = new StringSchema();
		respMediaType.setSchema(respSchema);
		
		String resourceType = response.getResourceType();
		if(resourceType == null) {
			resourceType = "";
		}
		
		respContent.addMediaType(resourceType, respMediaType);
		// Removing content for requests other than post or put
		resp.setContent(respContent);

		responses.addApiResponse(response.getStatus() + "", resp);
		return responses;
	}
	
	private static Content getContentForPostData(NetworkEvent request, String postData) {
		Content content = new Content();
		
		MediaType mediaType = new MediaType();
		ObjectSchema schema = new ObjectSchema();
		mediaType.setSchema(schema);
		Map<String, Example> examples = new HashMap<>();
		Example example = new Example();
		example.setValue(postData);
		examples.put(request.getRequestId(), example);
		mediaType.setExamples(examples);
		// Determine post data type 
		List<NameValuePair> postDataEntries = null;
		boolean schemaAdded = false;
		for(Header header : request.getHeaders()) {
			if (header.getName().equalsIgnoreCase("content-type")) {
				if(header.getValue().contains("json")) {
					try {
						ObjectMapper mapper = new ObjectMapper();
						JsonNode parseTree = mapper.readTree(postData);
						if(parseTree.getNodeType() == JsonNodeType.STRING ) {
							postDataEntries = UtilsAPIRunner.getPostData(postData);
							for (NameValuePair pair : postDataEntries) {
								schema.addProperties(pair.getName(), new StringSchema());
							}
						}
						else if (parseTree.getNodeType() == JsonNodeType.OBJECT) {
							Iterator<Entry<String, JsonNode>> iter = parseTree.fields();
							while(iter.hasNext()) {
								Entry<String, JsonNode> field = iter.next();
								
								String propName = field.getKey();
								Schema propSchema = null;
								
								switch(field.getValue().getNodeType()) {
								case BOOLEAN:
									propSchema = new BooleanSchema();
									break;
								case NUMBER:
									propSchema = new IntegerSchema();
									break;
								case OBJECT:
									propSchema = new ObjectSchema();
									break;
								default:
									propSchema = new StringSchema();
								}
								
								schema.addProperties(propName, propSchema);
/*								Map<String, Example> propExamples = new HashMap<>();
								Example propExample = new Example();
								example.setValue(field.getValue());
								examples.put(request.getRequestId(), example);
*/							}
							
						}
					}catch(Exception ex) {
						LOG.error("Cannot parse post data {} as json", postData );
					}
					schemaAdded = true;
					break;
				}
				else if(header.getValue().contains("xml")) {
					try {
						org.w3c.dom.Document doc1 = DomUtils.asDocument(postData);
						org.w3c.dom.NodeList children = doc1.getChildNodes();
						for(int i =0; i< children.getLength(); i++) {
							Node child = children.item(i);
							schema.addProperties(child.getNodeName(), new StringSchema());
						}
					} catch (IOException e) {
						LOG.error("Cannot parse post data {} as xml", postData );
					}
					schemaAdded = true;
					break;
				}
				
			}
		}
		if (!schemaAdded) {
				postDataEntries = UtilsAPIRunner.getPostData(postData);
		}
		content.addMediaType(com.google.common.net.MediaType.FORM_DATA.toString(), mediaType);
		
		return content;
	}

	/**
	 * Only Merged pathItems that have the same path; merges parameters, postData
	 * etc;
	 * 
	 * @param path
	 * @param pathItems
	 * @return
	 */
	public PathItem getMergedPathItem(String path, List<PathItem> pathItems) {
		if (pathItems == null || pathItems.size() == 0) {
			return null;
		}
		LOG.info("Merging {} PathItems for {}", pathItems.size(), path);

		PathItem mergedItem = new PathItem();

		for (PathItem pathItem : pathItems) {
			// TODO: yet to handle PUT, PATCH, TRACE
			if (pathItem.getGet() != null) {
				if (mergedItem.getGet() == null) {
					mergedItem.setGet(pathItem.getGet());
					continue;
				}
				Operation mergedGet = mergePaths(mergedItem.getGet(), pathItem.getGet(), false);
				pathItem.setGet(mergedGet);

			}
			if (pathItem.getOptions() != null) {
				if (mergedItem.getOptions() == null) {
					mergedItem.setOptions(pathItem.getOptions());
					continue;
				}
				Operation mergedOptions = mergePaths(mergedItem.getOptions(), pathItem.getOptions(), false);
				pathItem.setOptions(mergedOptions);

			}
			if (pathItem.getDelete() != null) {
				if (mergedItem.getDelete() == null) {
					mergedItem.setDelete(pathItem.getDelete());
					continue;
				}
				Operation mergedOptions = mergePaths(mergedItem.getDelete(), pathItem.getDelete(), false);
				pathItem.setDelete(mergedOptions);

			}
			if (pathItem.getHead() != null) {
				if (mergedItem.getHead() == null) {
					mergedItem.setHead(pathItem.getHead());
					continue;
				}
				Operation mergedHead = mergePaths(mergedItem.getHead(), pathItem.getHead(), false);
				pathItem.setHead(mergedHead);

			}
			if (pathItem.getTrace() != null) {
				if (mergedItem.getTrace() == null) {
					mergedItem.setTrace(pathItem.getTrace());
					continue;
				}
				Operation mergedGet = mergePaths(mergedItem.getTrace(), pathItem.getTrace(), false);
				pathItem.setTrace(mergedGet);

			}
			if (pathItem.getPatch() != null) {
				if (mergedItem.getPatch() == null) {
					mergedItem.setPatch(pathItem.getPatch());
					continue;
				}
				Operation mergedGet = mergePaths(mergedItem.getPatch(), pathItem.getPatch(), false);
				pathItem.setPatch(mergedGet);

			}
			if (pathItem.getPost() != null) {
				if (mergedItem.getPost() == null) {
					mergedItem.setPost(pathItem.getPost());
					continue;
				}

				Operation mergedPost = mergePaths(mergedItem.getPost(), pathItem.getPost(), true);
				pathItem.setPost(mergedPost);

			}
			if (pathItem.getPut() != null) {
				if (mergedItem.getPut() == null) {
					mergedItem.setPut(pathItem.getPut());
					continue;
				}

				Operation mergedPost = mergePaths(mergedItem.getPut(), pathItem.getPut(), true);
				pathItem.setPut(mergedPost);

			}

		}

		String regex = "\\{var[0-9]+\\}";
		String[] splitPath = path.split("/");
		for(String splitItem: splitPath) {
			if(splitItem.matches(regex)) {
				splitItem = splitItem.substring(4, splitItem.indexOf('}'));
				int variable = Integer.parseInt(splitItem);
				List<String> examples = apiGraph.getVarExamples().get(variable);
				Parameter pathParam = new PathParameter();
				Schema paramSchema = new StringSchema();
				pathParam.setSchema(paramSchema);
				pathParam.setRequired(true);
				pathParam.setName("var" + variable + "");
				Map<String, Example> examplesMap = new HashMap<String, Example>();
				for(int i = 0; i<examples.size(); i++) {					
					Example example = new Example();
					example.setValue(examples.get(i));
					examplesMap.put("" + i, example);
				}
				pathParam.setExamples(examplesMap);
				mergedItem.addParametersItem(pathParam);
			}
		}
		
		return mergedItem;

	}

	/*
	 * private Operation mergeGetPaths(Operation get, Operation get2) { if (get ==
	 * null && get2 != null) { return get2; } if (get != null && get2 == null) {
	 * return get; }
	 * 
	 * Operation merged = get; // TODO: merge parameters, examples, responses from
	 * get2
	 * 
	 * Parameter Merging Map<String, Parameter> parameterMap =
	 * merged.getParameters().stream(). collect(Collectors.toSet()).stream().
	 * collect(Collectors.toMap(Parameter::getName, Functions.identity()));;
	 * 
	 * for(Parameter param: get2.getParameters()) {
	 * if(!parameterMap.containsKey(param.getName())) {
	 * parameterMap.put(param.getName(), param); } else { try {
	 * parameterMap.get(param.getName()).getExamples().putAll(param.getExamples());
	 * }catch(Exception ex) { LOG.error("Error mergin params {} {}",
	 * parameterMap.get(param.getName()), param); } } }
	 * 
	 * merged.setParameters(Lists.newArrayList(parameterMap.values()));
	 * 
	 * Response Merging merged.getResponses().putAll(get2.getResponses());
	 * 
	 * 
	 * return merged; }
	 */

	/**
	 * TODO: make parameters optional when we find multiple requests with and
	 * without them
	 * 
	 * @param op1
	 * @param op2
	 * @return
	 */
	private Operation mergePaths(Operation op1, Operation op2, boolean hasRequestBody) {

		Operation merged;

		if (op1 == null || op2 == null) {
			if (op1 == null && op2 != null) {

				merged = op2;
			} else if (op1 != null && op2 == null) {
				merged = op1;
			} else {
				return null;
			}

			// Remove duplicate parameters
			Map<String, Parameter> parameterMap = merged.getParameters().stream().collect(Collectors.toSet()).stream()
					.collect(Collectors.toMap(Parameter::getName, Functions.identity()));
			;

			merged.setParameters(Lists.newArrayList(parameterMap.values()));
		} else {
			merged = op1;

			Map<String, Parameter> parameterMap = merged.getParameters().stream().collect(Collectors.toSet()).stream()
					.collect(Collectors.toMap(Parameter::getName, Functions.identity()));
			;

			for (Parameter param : op2.getParameters()) {
				if (!parameterMap.containsKey(param.getName())) {
					parameterMap.put(param.getName(), param);
				} else {
					try {
						parameterMap.get(param.getName()).getExamples().putAll(param.getExamples());
					} catch (Exception ex) {
						LOG.error("Error mergin params {} {}", parameterMap.get(param.getName()), param);
					}
				}
			}

			merged.setParameters(Lists.newArrayList(parameterMap.values()));

			/* Response Merging */
			try {
				if(merged!=null && merged.getResponses()!=null) {
					merged.getResponses().putAll(op2.getResponses());
				}
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		}

		if (hasRequestBody) {
			if (merged.getRequestBody() == null) {
				if (op2.getRequestBody() != null) {
					merged.setRequestBody(op2.getRequestBody());
				}
			} else {
				if (op2.getRequestBody() != null) {
					// Both operations have request body. Have to merge
					try {
						RequestBody mergedRB = mergeRequestBodies(merged.getRequestBody(), op2.getRequestBody());
						merged.setRequestBody(mergedRB);
					}catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}

		return merged;
	}

	/**
	 * TODO: make request body optional if we find counter examples
	 * 
	 * @param requestBody
	 * @param requestBody2
	 * @return
	 */
	private RequestBody mergeRequestBodies(RequestBody requestBody, RequestBody requestBody2) throws NullPointerException {
		RequestBody merged = requestBody;
		merged.setRequired(true);
		Content content = merged.getContent();
		MediaType mediaType = content.get(com.google.common.net.MediaType.FORM_DATA.toString());
		Schema schema = mediaType.getSchema();
		Map<String, Schema> properties1 = schema.getProperties();
		Map<String, Example> examples = mediaType.getExamples();

		Content content2 = requestBody2.getContent();
		MediaType mediaType2 = content2.get(com.google.common.net.MediaType.FORM_DATA.toString());
		Schema schema2 = mediaType2.getSchema();
		Map<String, Schema> properties2 = schema2.getProperties();
		Map<String, Example> examples2 = mediaType2.getExamples();

		for (String propertyName : properties2.keySet()) {
			schema.addProperties(propertyName, properties2.get(propertyName));
		}

		for (String exampleId : examples2.keySet()) {
			examples.put(exampleId, examples2.get(exampleId));
		}

		merged.setContent(content);

		return merged;
	}
	

	public OpenAPI getOpenAPI(String hostUrl, List<APIResponse> apiResponses) throws MalformedURLException {
		this.pathItemMap = new HashMap<>();
		HashMap<String, List<APIResponse>> apiResponseHashMap = new HashMap<>();
		OpenAPI openAPI = new OpenAPI();
		Server serversItem = new Server();
		serversItem.setUrl(hostUrl);
		openAPI.addServersItem(serversItem);
		Paths paths = new Paths();
		openAPI.setPaths(paths);

		/* Set Info Object */
		Info info = new Info();
		info.setTitle("Auto-generated OpenAPI for " + hostUrl);
		info.setDescription(
				"Auto-generated OpenAPI for " + hostUrl + "\n" + "Generated from " + apiResponses.size() + "api calls");
		info.setVersion("0");
		openAPI.setInfo(info);

		for (APIResponse api : apiResponses) {

			if(api.getStatus() != APIResponse.Status.SUCCESS) {
				continue;
			} 
			
			NetworkEvent request = api.getRequest();
			URL urlObj = new URL(request.getRequestUrl());
			String path = urlObj.getPath();
			if (path.endsWith("/") && path.length() > 1) {
				path = path.substring(0, path.length() - 1);
			}
			if(apiGraph!=null && apiGraph.getComputedURLMap()!=null) {
				if(apiGraph.getComputedURLMap().containsKey(path)) {
					path = apiGraph.getComputedURLMap().get(path);
				}
			}

			String shortPath = UtilsOASGen.removeBaseFromPath(path, UtilsOASGen.getPathFromURL(hostUrl));
			/*String server = urlObj.getHost();
			if (!server.equalsIgnoreCase((new URL(hostUrl).getHost()))) {
				continue;
			}*/
			if(!request.getRequestUrl().startsWith(hostUrl)) {
				continue;
			}
			try {
				PathItem pathItem = getPathItemForRequest(api);
				if (!pathItemMap.containsKey(shortPath)) {
					pathItemMap.put(shortPath, new ArrayList<>());
				}
				pathItemMap.get(shortPath).add(pathItem);
				if(!apiResponseHashMap.containsKey(shortPath)){
					apiResponseHashMap.put(shortPath, new ArrayList<>());
				}
				apiResponseHashMap.get(shortPath).add(api);
			} catch (Exception ex) {
				LOG.error("Error getting path Item for {}", api);
				ex.printStackTrace();
			}
		}
		
		for (String path : pathItemMap.keySet()) {
			if(apiResponseHashMap.containsKey(path)){
				boolean containsValidStatus = false;
				boolean hasGoodResponse = false;
				for(APIResponse apiResponse: apiResponseHashMap.get(path)){
					if(apiResponse.getRequest().getMethod() != NetworkEvent.MethodClazz.GET && apiResponse.getRequest().getMethod() != NetworkEvent.MethodClazz.POST && apiResponse.getRequest().getMethod() != NetworkEvent.MethodClazz.PUT && apiResponse.getRequest().getMethod() != NetworkEvent.MethodClazz.DELETE){
						continue;
					}
					if(apiResponse.getResponse()!=null && UtilsOASGen.isValidServerStatus(apiResponse.getResponse().getStatus())){
						containsValidStatus = true;
						break;
					}
				}

				for(APIResponse apiResponse: apiResponseHashMap.get(path)){
					if(apiResponse.getRequest().getMethod() != NetworkEvent.MethodClazz.GET){
						if(apiResponse.getRequest().getMethod() == NetworkEvent.MethodClazz.POST || apiResponse.getRequest().getMethod() ==NetworkEvent.MethodClazz.PUT || apiResponse.getRequest().getMethod() == NetworkEvent.MethodClazz.DELETE){
							hasGoodResponse = true;
							break;
						}
						continue;
					}
					if(apiResponse.getResponse().getResourceType() == null || !( apiResponse.getResponse().getResourceType().contains("json") || apiResponse.getResponse().getResourceType().contains("xml"))){
//						LOG.info("Ignoring API {} because resourceType is {}", apiResponse.getId(), apiResponse.getResponse().getResourceType());
						continue;
					}
					else{
						hasGoodResponse = true;
						break;
					}
				}

				if(!containsValidStatus || !hasGoodResponse){
					// No need to add paths that have no valid responses
					continue;
				}
			}
			PathItem merged = getMergedPathItem(path, pathItemMap.get(path));
			if (merged != null) {
				paths.addPathItem(path, merged);
				LOG.info("Added Merged PathItem for {}", path);
			}
		}
		
		return openAPI;
	}

	public static void main(String args[]) {
		String minedFolder = "20220711_025641";
		String runFolder = "20220711_032114";
		SUBJECT subject = SUBJECT.jawa;
		String baseURL = Main.getBaseUrl(subject);


//		String minedFolder = "booker-20220606_170523";
//		String runFolder = "20220606_174308";
//		SUBJECT subject = SUBJECT.booker;
//		String baseURL = "http://localhost:8080";

//		String minedFolder = "mdh-20220610_151241";
//		String runFolder = "20220613_144858";
//		SUBJECT subject = SUBJECT.mdh;
//		String baseURL = "http://localhost:8080";

		String runPath = "/Users/apicarv/git/TestCarving/testCarver/out/" + subject.name() + File.separator +  minedFolder + "/run/" + runFolder;
		SwaggerGenerator swaggerGen = new SwaggerGenerator(subject, minedFolder, runPath, baseURL);
		
		try {
			OpenAPI openAPI = swaggerGen.getOpenAPI();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public OpenAPI getOpenAPI() throws MalformedURLException, JsonProcessingException {
//		if(this.apiResponses == null){
//			apiResponses = this.workDirManager.getRunResults();
//		}
		DirectedAcyclicGraph<URLNode, DefaultEdge> graph = apiGraph.buildAPIGraph();
		apiGraph.pruneGraph();
//		HashMap<String, String> urlMap = apiGraph.getComputedURLMap();
		HashMap<String, String> urlMap = apiGraph.extractPathsFromGraph();

		try {
			workDirManager.exportURLMap(urlMap, Settings.URL_MAP_JSON);

			workDirManager.exportAPIGraph(graph, Settings.API_RAW_GRAPH_FILE, Settings.API_GRAPH_FILE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OpenAPI openAPI = getOpenAPI(hostUrl, apiResponses);
//		System.out.println(openAPI);
		workDirManager.exportOAS(openAPI, Settings.OAS_EXPORT_FILE);
		
		APIProber prober = new APIProber(apiResponses, subject, hostUrl, minedFolder, runPath, workDirManager);
		
//		WorkDirManager workDirManager = new WorkDirManager("testProbe", DirType.OAS);

		List<APIResponse> resultResponses = prober.expandGraph();
//		apiGraph.pruneGraph();
//
//		apiGraph.extractPathsFromGraph();


		APIGraph newGraph = new APIGraph(subject, resultResponses);

		DirectedAcyclicGraph<URLNode, DefaultEdge> newGraphTree = newGraph.buildAPIGraph();
		newGraph.pruneGraph();
		urlMap = newGraph.extractPathsFromGraph();

		try {
			workDirManager.exportURLMap(urlMap, Settings.PROBED_URL_MAP_JSON);

			workDirManager.exportAPIGraph(newGraphTree, Settings.PROBED_API_RAW_GRAPH_FILE, Settings.PROBED_API_GRAPH_FILE);
			workDirManager.exportResultResponses(resultResponses);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//
//		List<APIResponse> allResponses = new ArrayList<>();
//		allResponses.addAll(apiResponses);
//		allResponses.addAll(resultResponses);
//

		this.apiGraph = newGraph;
		
		OpenAPI openAPI_withProbe = getOpenAPI(hostUrl, resultResponses);

		workDirManager.exportOAS(openAPI_withProbe, Settings.PROBED_OAS_EXPORT_FILE);
		
		return openAPI_withProbe;
	}

	public List<APIResponse> getApiResponses() {
		return apiResponses;
	}
}
