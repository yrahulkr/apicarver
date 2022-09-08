package com.apicarv.testCarver.utils;

import java.io.File;

import com.apicarv.testCarver.uitestrunner.TestRunResult;

public class Settings {

	public static final boolean VERBOSE = false;
	public static final int PIXEL_DENSITY = 1;
	public static TestRunResult currResult;
	public static boolean aspectActive = false;
	public static String outputDir = "out";

	public static final String sep = File.separator;
	public static final boolean LOG_NETWORK = true;

	/* file extensions. */
	public static String PNG_EXT = ".png";
	public static String HTML_EXT = ".html";
	public static String JAVA_EXT = ".java";
	public static String JSON_EXT = ".json";
	public static final String DEV_TOOLS_OUTPUT_JSON = "devToolsOutput.json";
	public static final String UI_ACTIONS_LOG = "uiActionsLog.json";
	public static final String CRAWL_EVENTS_LOG = "crawlEventsLog.json";

	public static final String PROBE_EVENTS = "probeEvents.json";
	public static final String RESULT_RESPONSES = "resultResponses.json";

	// API Seq Generator Outputs
	public static final String COMBINED_EVENTS_JSON = "combined_generatedEvents.json";
	public static final String GEN_EVENTS_JSON = "generatedEvents.json";
	public static final String ClEANED_GEN_EVENTS_JSON = "cleaned_generatedEvents.json";
	public static final String FILTERED_GEN_EVENTS_JSON = "filtered_generatedEvents.json";
	public static final String GEN_PIPELINE_CONFIG = "pipeline_config.json";

	// API Runner Outputs
	public static final String REPORT_HTML = "report.html";
	public static final String PAYLOAD_DIR = "responses";

	// API Miner Outputs
	public static final String TEST_RUN_RESULT = "uiTest_runResult.json";
	public static final String OAS_EXPORT_FILE = "oas";
	public static boolean DRIVER_LISTENER = true;
	public static final String API_TREE_FILE = "apiTree.xml";
	public static final String API_GRAPH_FILE = "apiGraph.json";
	public static final String URL_MAP_JSON = "urlMap.json";
	public static final String API_RAW_GRAPH_FILE = "apiGraph_raw.json";
	public static final String DOCKER_LOCATION = "src/main/resources/webapps";
	public static final String DOCKER_SCRIPT = "run-docker.sh";
	public static final String DOCKER_STOP_SCRIPT = "stop-docker.sh";

	// OAS GENERATION
	public static final boolean NO_HEADERS_SWAGGER = true;
	public static final String COV_DIR = "cov";
	public static final String PROBED_API_RAW_GRAPH_FILE = "probe_" + API_RAW_GRAPH_FILE;
	public static final String PROBED_API_GRAPH_FILE = "probe_" + API_GRAPH_FILE;
	public static final String PROBED_URL_MAP_JSON = "probe_" + URL_MAP_JSON;
	public static final String PROBED_OAS_EXPORT_FILE = "probe_" + OAS_EXPORT_FILE;;

	public static final boolean DISABLE_HTML_OUTPUT = false;
	public static final boolean RA_INCLUDE_ARRAY_ELEMENTS = false;

	public static enum SUBJECT {
		petclinic, dummy, xwiki, parabank, booker, jawa, realworld, tmf, shopizer, medical, mdh, ecomm
	}
}
