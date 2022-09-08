package com.apicarv.testCarver.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.transform.TransformerException;

import com.apicarv.testCarver.apirunner.APIResponse;
import com.apicarv.testCarver.openAPIGenerator.ProbeEvent;
import com.apicarv.testCarver.openAPIGenerator.URLNode;
import com.apicarv.testCarver.report.ReportGenerator;
import com.apicarv.testCarver.uitestrunner.TestRunResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.io.FileUtils;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.ComponentAttributeProvider;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.DefaultAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.crawljax.util.DomUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.apicarv.testCarver.apirecorder.CrawlEvent;
import com.apicarv.testCarver.apirecorder.GenPipeLine;
import com.apicarv.testCarver.apirecorder.LogEntry;
import com.apicarv.testCarver.apirecorder.NetworkEvent;
import com.apicarv.testCarver.apirecorder.UIAction;

public class WorkDirManager {

	private static final Logger LOG = LoggerFactory.getLogger(WorkDirManager.class);

	public static final String DEFAULT_APP_OUT = "default";

	private String outputDir = null;

	private Settings.SUBJECT subject = null;
	private String minedFolder = null;

	private String genPath = null;

	private boolean prepared = false;

	private DirType dirType = null;



	public enum DirType {
		MINE, GEN, RUN, OAS
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public DirType getDirType() {
		return dirType;
	}

	public void setDirType(DirType dirType) {
		this.dirType = dirType;
	}

	/**
	 * To be used for dirTypes MINE, GEN
	 *
	 * @param app
	 * @param minedFolder
	 * @param dirType
	 */
	public WorkDirManager(Settings.SUBJECT app, String minedFolder, DirType dirType) {
		this.subject = app;
		this.minedFolder = minedFolder;
		this.setDirType(dirType);
		String dateString = getDateString();
		if (minedFolder == null || minedFolder.trim().isEmpty()) {
			minedFolder = DEFAULT_APP_OUT;
		}
		if (dirType == DirType.MINE) {
			outputDir = Paths.get(Settings.outputDir, app.name(), dateString).toString();
		} else {
			outputDir = Paths.get(Settings.outputDir, app.name(), minedFolder, dirType.name().toLowerCase(), dateString).toString();
		}
	}

	/**
	 * To be used for dirType RUN, OAS, API Tree
	 *
	 * @param app
	 * @param minedFolder
	 * @param dirType
	 * @param genPath
	 */
	public WorkDirManager(Settings.SUBJECT app, String minedFolder, DirType dirType, String genPath) {
		this.subject = app;
		this.minedFolder = minedFolder;
		this.genPath = genPath;
		this.setDirType(dirType);
		String dateString = getDateString();
		if (minedFolder == null || minedFolder.trim().isEmpty()) {
			minedFolder = DEFAULT_APP_OUT;
		}
		outputDir = Paths.get(Settings.outputDir, app.name(), minedFolder, dirType.name().toLowerCase(), dateString).toString();
	}

	/**
	 *  Only to be used when the output directory already exists
	 * @param app
	 * @param minedFolder
	 * @param runFolder
	 * @param dirType
	 * @param oasPath
	 */
	public WorkDirManager(Settings.SUBJECT app, DirType dirType, String minedFolder, String runFolder, String oasPath){
		this.subject = app;
		this.minedFolder = minedFolder;
		this.genPath = runFolder;
		this.dirType = dirType;
		this.outputDir = oasPath;
	}

	public static String getDateString() {
		return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	}

	public boolean prepareOutputDir() {
		File outputFolder = new File(outputDir);

		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
			LOG.info("Created output directory {}", outputDir);
		} else {
			if (!outputFolder.isDirectory()) {
				LOG.error("The output path is not a directory {}", outputDir);
				return false;
			}
			if (outputFolder.list() != null && outputFolder.list().length == 0) {
				LOG.error("The output directory is not empty {}", outputFolder);
				return false;
			}
		}
		
		if(dirType == DirType.RUN || dirType == DirType.MINE || dirType == DirType.OAS) {
			File payloadFolder = new File(outputFolder, Settings.PAYLOAD_DIR);
			if(!payloadFolder.exists()) {
				payloadFolder.mkdirs();
				LOG.info("Created payload directory {}", payloadFolder);
			}
		}
		prepared = true;
		return prepared;
	}

	public List<APIResponse> getProberRunResults() {
		if(dirType == DirType.OAS){
			String apiJson = Paths.get(outputDir, Settings.RESULT_RESPONSES).toString();
			return UtilsJson.importAPIResponses(apiJson);
		}
		else{
			LOG.error("Workdir is not of type OAS. prober results not available");
			return null;
		}
	}


	public void exportResultResponses(List<APIResponse> apiResponses){
		exportResultResponses(apiResponses, "", Settings.DISABLE_HTML_OUTPUT);
	}

	public void exportResultResponses(List<APIResponse> resultResponses, String fileName, boolean disableHTML) {
		if (!prepared) {
			prepareOutputDir();
		}

		Gson gson = new GsonBuilder().disableHtmlEscaping().create();

		String htmlPath = Paths.get(outputDir, fileName + Settings.RESULT_RESPONSES).toString();

		String json = gson.toJson(resultResponses);
		UtilsJson.exportFile(htmlPath, json);
		
		String reportFile = new File(outputDir, fileName + Settings.REPORT_HTML).getAbsolutePath();

		if(!disableHTML) {
			ReportGenerator reportGen = new ReportGenerator(reportFile);
			try {
				reportGen.generateReport(resultResponses);
			} catch (IOException e) {
				LOG.error("Error Generating Report");
				e.printStackTrace();
			}
		}
	}

	public String getMinedAPIJson() {
		String minedAPIJson = Paths.get(Settings.outputDir, subject.name(), minedFolder, Settings.DEV_TOOLS_OUTPUT_JSON).toString();
		return minedAPIJson;
	}

	public void exportCombinedRequests(List<NetworkEvent> requestsToExport) {
		if (!prepared) {
			prepareOutputDir();
		}
		String combinedJson = Paths.get(outputDir, Settings.COMBINED_EVENTS_JSON).toString();
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		UtilsJson.exportFile(combinedJson, gson.toJson(requestsToExport));
	}

	public void exportNonResourceRequests(List<NetworkEvent> requestsToExport) {
		if (!prepared) {
			prepareOutputDir();
		}
		String filteredJson = Paths.get(outputDir, Settings.FILTERED_GEN_EVENTS_JSON).toString();
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		UtilsJson.exportFile(filteredJson, gson.toJson(requestsToExport));
	}

	public void exportCleanHeaderRequests(List<NetworkEvent> requestsToExport) {
		if (!prepared) {
			prepareOutputDir();
		}
		String cleanedJson = Paths.get(outputDir, Settings.ClEANED_GEN_EVENTS_JSON).toString();
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		UtilsJson.exportFile(cleanedJson, gson.toJson(requestsToExport));
	}

	public void exportGenRequests(List<NetworkEvent> requestsToExport) {
		if (!prepared) {
			prepareOutputDir();
		}
		String genJson = Paths.get(outputDir, Settings.GEN_EVENTS_JSON).toString();
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		UtilsJson.exportFile(genJson, gson.toJson(requestsToExport));
	}

	public void exportSeqGenOutput(GenPipeLine genPipeLine, List<NetworkEvent> requestsToExport) {
		if (!prepared) {
			prepareOutputDir();
		}
		exportGenRequests(requestsToExport);
		exportPipelineConfig(genPipeLine);
	}

	private void exportPipelineConfig(GenPipeLine genPipeLine) {
		if (!prepared) {
			prepareOutputDir();
		}
		String configJson = Paths.get(outputDir, Settings.GEN_PIPELINE_CONFIG).toString();
		UtilsJson.exportFile(configJson, (new Gson()).toJson(genPipeLine));
	}

	public List<NetworkEvent> getGeneratedEvents() {
//		String genJson = Paths.get(Settings.outputDir, subject.name(), minedFolder, DirType.GEN.name().toLowerCase(), genPath, Settings.GEN_EVENTS_JSON).toString();
		String genJson = Paths.get(genPath, Settings.GEN_EVENTS_JSON).toString();

		List<NetworkEvent> importedEvents = UtilsJson.importGeneratedEvents(genJson);

		return importedEvents;
	}

	public List<NetworkEvent> getMinedEvents() {
		String genJson = Paths.get(genPath, Settings.COMBINED_EVENTS_JSON).toString();

		List<NetworkEvent> importedEvents = UtilsJson.importGeneratedEvents(genJson);

		return importedEvents;
	}


	public void exportPayLoad(APIResponse response, String payload) {
		if (!prepared) {
			prepareOutputDir();
		}
		String genJson = Paths
				.get(outputDir, Settings.PAYLOAD_DIR, response.getId() + "_" + response.getRequest().getRequestId())
				.toString();
		UtilsJson.exportFile(genJson, payload);
//		UtilsJson.exportFile(genJson+".html", payload);
	}
	

	
	/*public void exportPayLoad(LogEntry event) {
		if(event.getData()==null || event.getData().isEmpty()) {
			return;
		}
		if (!prepared) {
			prepareOutputDir();
		}
		String genJson = Paths
				.get(outputDir, Settings.PAYLOAD_DIR, event.getId() + "_" + event.getRequestId())
				.toString();
		UtilsJson.exportFile(genJson, event.getData());
	}*/

	/**
	 * Save UI Test RUn result while mining API
	 * 
	 * @param currResult
	 */
	public void exportTestRunResult(TestRunResult currResult) {
		if (!prepared) {
			prepareOutputDir();
		}
		String genJson = Paths.get(outputDir, Settings.TEST_RUN_RESULT).toString();
		UtilsJson.exportFile(genJson, (new Gson()).toJson(currResult));
	}

	/**
	 * Exports Mined API from UI Test suite. Needs to be called separately after
	 * mining API.
	 * 
	 * @param list
	 */
	public void exportMinedAPI(List<LogEntry> list) {
		if (!prepared) {
			prepareOutputDir();
		}
		String htmlPath = Paths.get(outputDir, Settings.DEV_TOOLS_OUTPUT_JSON).toString();

		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String json = gson.toJson(list);

		UtilsJson.exportFile(htmlPath, json);
		/*
		for(LogEntry log: list) {
			exportPayLoad(log);
		}*/
	}

	/**
	 * Uses the runFolder path provided during workdir creation
	 * @return
	 */
	public List<APIResponse> getRunResults() {
		String apiJson = Paths.get(genPath, Settings.RESULT_RESPONSES).toString();
		return UtilsJson.importAPIResponses(apiJson);
	}

	/**
	 * filename without extension. it will export both json and yaml format
	 * @param openAPI
	 * @param fileName
	 */
	public void exportOAS(OpenAPI openAPI, String fileName) throws JsonProcessingException {
		if (!prepared) {
			prepareOutputDir();
		}
		String oasFile = Paths.get(outputDir, fileName).toString();
		String json = UtilsSwagger.jsonifyOpenAPI(openAPI);
		String yaml = UtilsSwagger.stringifyYaml(openAPI);
		UtilsJson.exportFile(oasFile+".json", json);
		UtilsJson.exportFile(oasFile + ".yaml", yaml);
	}

	public void exportUIActions(List<UIAction> uiActions) {
		if (!prepared) {
			prepareOutputDir();
		}
		String htmlPath = Paths.get(outputDir, Settings.UI_ACTIONS_LOG).toString();

		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String json = gson.toJson(uiActions);

		UtilsJson.exportFile(htmlPath, json);
	}

	public void exportAPITree(Document apiTree) throws TransformerException, IOException {
		if (!prepared) {
			prepareOutputDir();
		}
		String oasFile = Paths.get(outputDir, Settings.API_TREE_FILE).toString();
		DomUtils.writeDocumentToFile(apiTree, oasFile ,"xml", 2);
	}

	public void exportCrawlEvents(List<CrawlEvent> crawlEvents) {
		if (!prepared) {
			prepareOutputDir();
		}
		String htmlPath = Paths.get(outputDir, Settings.CRAWL_EVENTS_LOG).toString();

		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String json = gson.toJson(crawlEvents);

		UtilsJson.exportFile(htmlPath, json);
	}

	public String getPayload(int payload) throws IOException {
		Path payloadsDir = Paths.get(genPath, Settings.PAYLOAD_DIR);
		List<Path> files = Files.list(payloadsDir).collect(Collectors.toList());
		for(Path file: files) {
			int fileNum = Integer.parseInt(file.getFileName().toString().split("_")[0]);
			if(fileNum == payload) {
				return FileUtils.readFileToString(file.toFile());
			}
		}
		return null;
	}

	public String getProberPayload(int payload) throws IOException {
		if(dirType != DirType.OAS){
			LOG.error("Workdir is not of type OAS. prober payloads not available");
			return null;
		}
		Path payloadsDir = Paths.get(outputDir, Settings.PAYLOAD_DIR);
		List<Path> files = Files.list(payloadsDir).collect(Collectors.toList());
		for(Path file: files) {
			int fileNum = Integer.parseInt(file.getFileName().toString().split("_")[0]);
			if(fileNum == payload) {
				return FileUtils.readFileToString(file.toFile());
			}
		}
		return null;
	}
	
	public void exportPng(String fileDot){
	    try {
	        File f = new File(fileDot); 
	        String arg1 = f.getAbsolutePath(); 
	        String arg2 = arg1 + ".png"; 
	        String[] c = {"/usr/local/bin/dot", "-Tpng", arg1, "-o", arg2};
	        System.out.println(c);
	        Process p = Runtime.getRuntime().exec(c); 
	        int err = p.waitFor(); 
	    }
	    catch(IOException e1) {
	        System.out.println(e1);
	    }
	    catch(InterruptedException e2) {
	        System.out.println(e2);
	    }
	}

	public void exportAPIGraph(DirectedAcyclicGraph<URLNode, DefaultEdge> graph, String rawGraphFile, String graphFile) throws IOException {
		if (!prepared) {
			prepareOutputDir();
		}
		
		String verticesFile = Paths.get(outputDir, rawGraphFile).toString();
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("vertices", graph.vertexSet());
		jsonMap.put("edges", graph.edgeSet());
		String json = gson.toJson(jsonMap);
		UtilsJson.exportFile(verticesFile, json);
		
		String oasFile = Paths.get(outputDir, graphFile).toString();
		ComponentNameProvider<URLNode> vertexIDProvider = new ComponentNameProvider<URLNode>() {

			@Override
			public String getName(URLNode arg0) {
				// TODO Auto-generated method stub
				return "" + arg0.getId();
			}
		};
		
		ComponentNameProvider<URLNode> vertexLabelProvider = new ComponentNameProvider<URLNode>() {

			@Override
			public String getName(URLNode arg0) {
				String name  = "";
				if(arg0.getVar()>=0) {
					name += "var" + arg0.getVar() + "-";
				}
				name += arg0.getPathItem();
				return name;
			}
		};;
		
		ComponentNameProvider<DefaultEdge> edgeLabelProvider = new ComponentNameProvider<DefaultEdge>() {

			@Override
			public String getName(DefaultEdge arg0) {
				return "";
			}
		};
		
		ComponentAttributeProvider<URLNode> vertexAttributeProvider = new ComponentAttributeProvider<URLNode>() {

			@Override
			public Map<String, Attribute> getComponentAttributes(URLNode arg0) {
				if(arg0.isLeaf && arg0.getMethod()!=null) {
					Map<String, Attribute> attrs = new HashMap<>();
					switch(arg0.getMethod()) {
					case POST:
					case PUT:
						attrs.put("shape", DefaultAttribute.createAttribute("triangle") );
						break;
					case GET:
					case PATCH:
					case TRACE:
					case OPTIONS:
					case DELETE:
					case HEAD:
						attrs.put("shape", DefaultAttribute.createAttribute("square") );
						break;
					case UNKNOWN:
					default:
						attrs.put("shape", DefaultAttribute.createAttribute("circle") );
					}
					if(arg0.getPayloadToCompare()!=null) {
						attrs.put("color", DefaultAttribute.createAttribute("green") );
					}
					else {
						attrs.put("color", DefaultAttribute.createAttribute("red") );
					}
					
					return attrs;
				}
				
				return null;
			}
		};
		
		ComponentAttributeProvider<DefaultEdge> edgeAttributeProvider = new ComponentAttributeProvider<DefaultEdge>() {

			@Override
			public Map<String, Attribute> getComponentAttributes(DefaultEdge arg0) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		
		DOTExporter<URLNode, DefaultEdge> dotExporter = new DOTExporter<URLNode, DefaultEdge>(vertexIDProvider, vertexLabelProvider, edgeLabelProvider, vertexAttributeProvider, edgeAttributeProvider);
		dotExporter.exportGraph(graph, new FileWriter(new File(oasFile)));
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exportPng(new File(oasFile).getAbsolutePath());
	}

	public void exportURLMap(HashMap<String, String> urlMap, String urlMapName) {
		if (!prepared) {
			prepareOutputDir();
		}
		String urlMapFile = Paths.get(outputDir, urlMapName).toString();

		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String json = gson.toJson(urlMap);
		
		UtilsJson.exportFile(urlMapFile, json);

	}

	public void exportProbeEvents(List<ProbeEvent> probeEvents, String fileName) {
		if (!prepared) {
			prepareOutputDir();
		}
		if(fileName == null ){
			fileName = "";
		}

		String htmlPath = Paths.get(outputDir, fileName + Settings.PROBE_EVENTS).toString();

		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String json = gson.toJson(probeEvents);

		UtilsJson.exportFile(htmlPath, json);
	}

	public String getCovFile(String name) {
		if(!prepared) {
			prepareOutputDir();
		}
		if( name==null || name.trim().isEmpty()){
			name = "";
		}
		else{
			name = name.trim() + "_";
		}
		String covFile = Paths.get(outputDir, name + Settings.COV_DIR + "_" + getDateString()).toAbsolutePath().toString();
		return covFile;
	}

	public String getCovFile(){
		return getCovFile("");
	}
}
