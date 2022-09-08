package com.apicarv.testCarver;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import com.apicarv.testCarver.apirecorder.APISeqGen;
import com.apicarv.testCarver.apirecorder.GenPipeLine;
import com.apicarv.testCarver.apirecorder.LogEntry;
import com.apicarv.testCarver.apirecorder.NetworkEvent;
import com.apicarv.testCarver.apirunner.APIRunner;
import com.apicarv.testCarver.openAPIGenerator.OASCombiner;
import com.apicarv.testCarver.openAPIGenerator.SwaggerGenerator;
import com.apicarv.testCarver.uitestrunner.APIMiner;
import com.apicarv.testCarver.utils.UtilsSwagger;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.apicarv.testCarver.utils.Settings.SUBJECT;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	
	public static SUBJECT getSubject(String app) {
		switch(app) {
		case "petclinic":
			return SUBJECT.petclinic;
		case "parabank":
			return SUBJECT.parabank;
		case "realworld":
			return SUBJECT.realworld;
		case "booker":
			return SUBJECT.booker;
		case "jawa":
			return SUBJECT.jawa;
		case "medical":
			return SUBJECT.medical;
		case "ecomm":
			return SUBJECT.ecomm;
		case "dummy":
			return SUBJECT.dummy;
			default:
				return null;
		}
	}

	public static String getBaseUrl(SUBJECT subject){
		switch(subject){
			case parabank:
				return "http://localhost:8080/parabank-3.0.0-SNAPSHOT/services_proxy/bank";
			case booker:
				return "http://localhost:8080";
			case petclinic:
				return "http://localhost:9966/petclinic/api";
			case realworld:
				return "http://localhost:3000/api";
			case jawa:
				return "http://localhost:8080/api";
			case medical:
				return "http://localhost:8080";
			case ecomm:
				return "http://localhost:8080/api";
			default:
				return null;
		}
	}
	
	
	public static void resetApp(SUBJECT subject, boolean restart, String file) {
		if (subject == null) {
			LOG.error("Not a valid subject : {}", subject);
			return;
		}
		
		switch (subject) {
		case petclinic:
			tests.petclinic.ResetAppState.reset(file, restart);
			break;
		case parabank:
			tests.parabank.ResetAppState.reset(file, restart);
			break;
		case realworld:
			tests.realworld.ResetAppState.reset(file, restart);
			break;
		case booker:
			tests.booker.ResetAppState.reset(file, restart);
			break;
		case jawa:
			tests.jawa.ResetAppState.reset(file, restart);
			break;
		case medical:
			tests.medical.ResetAppState.reset(file, restart);
			break;
		case ecomm:
			tests.ecomm.ResetAppState.reset(file, restart);
			break;
		default:
			LOG.error("Could not reset {}", subject);
			return;
		}
	}



	public static void main(String args[]) throws URISyntaxException {

//		String app = "petclinic";
		
		if(args.length < 2) {
			LOG.info("Usage : app runtime(mins) minedFolder<optional> genPath<optional> runPath<optional>");
			System.exit(-1);
		}
		
		String app = args[0];
		
		SUBJECT subject = getSubject(app);

		int runTime = -1;
		try{
			runTime = Integer.parseInt(args[1]);
		}catch(NumberFormatException ex){
			LOG.error("Provide a valid integer as runtime. {} not a valid argument ", args[1]);
			LOG.error("Usage : app runtime(mins) minedFolder<optional> genPath<optional> runPath<optional>");
			System.exit(-1);
		}

		if(runTime <=0){
			LOG.error("Provide a positive integer as runtime!! {} not a valid argument", args[1]);
		}
		
		String hostUrl = getBaseUrl(subject);
//				args[1];
//				"http://localhost:8080";
		
		//petclinic-20211024_140703
		String minedFolder;

		if(args.length >= 3) {
			minedFolder = args[2];
		}
		else {
			APIMiner miner = new APIMiner(subject, APIMiner.MiningMode.CRAWLING, runTime);

			List<LogEntry> minedAPI = miner.mineAPI();
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			minedFolder = (new File(miner.getWorkDirManager().getOutputDir())).getName();
		}
		
		String genPath;
		List<NetworkEvent> genRequests;
		if(args.length >= 4) {
			genPath = args[3];
			
		}
		else {
			GenPipeLine genPipeLine = new GenPipeLine(true, true, true, true, GenPipeLine.FILTER_TYPE.inclusion);
			
			
			APISeqGen seqGen = new APISeqGen(subject, minedFolder, genPipeLine);

			genRequests = seqGen.generateAPISeq();
			
			genPath = new File(seqGen.getWorkDirManager().getOutputDir()).getAbsolutePath();
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		String runPath;
		if(args.length >= 5) {
			runPath = args[4];
		}
		else {
			APIRunner runner = new APIRunner(subject, minedFolder, genPath);
			try {
//				runner.runEvents(genRequests);
				runner.runGeneratedSeq();
				runner.getWorkdirManager().exportResultResponses(runner.getResultResponses());
			} catch (IOException e) {
				LOG.error("Error Generating report {}", e.getMessage());
				// e.printStackTrace();
			}
			runPath = new File(runner.getWorkdirManager().getOutputDir()).getAbsolutePath();

			try {
				OpenAPI original = UtilsSwagger.getGroundTruthSpec(subject);
				if(original == null){
					LOG.error("Groundtruth Spec not available for {}", subject);
				}
				OpenAPI enhanced = OASCombiner.enhanceSpec(original, runner.getResultResponses());
//				runner.getWorkdirManager().exportOAS(UtilsSwagger.stringifyYaml(enhanced), "openAPI_enhanced.yaml");
				runner.getWorkdirManager().exportOAS(enhanced, "openAPI_enhanced");
			}catch(Exception ex){
				ex.printStackTrace();
				LOG.error("ERROR enhancing spec {}", ex.getMessage());
			}
		}
		
		long start = System.currentTimeMillis();
		SwaggerGenerator swaggerGen = new SwaggerGenerator(subject, minedFolder, runPath, hostUrl);
		try {
			swaggerGen.getOpenAPI();
//			swaggerGen.getOpenAPI(hostUrl, runner.getResultResponses());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		LOG.info("Time taken for Swagger Generation {} millis", (end-start));

		try {
			OpenAPI original = UtilsSwagger.getGroundTruthSpec(subject);
			if(original == null){
				LOG.error("Groundtruth Spec not available for {}", subject);
			}
			// Use the final APIresponses generated by Prober. It will be a minified sequence
			OpenAPI enhanced = OASCombiner.enhanceSpec(original, swaggerGen.getWorkDirManager().getRunResults());
//			swaggerGen.getWorkDirManager().exportOAS(UtilsSwagger.stringifyYaml(enhanced), "openAPI_enhanced.yaml");
			swaggerGen.getWorkDirManager().exportOAS(enhanced, "openAPI_enhanced");
		}catch(Exception ex){
			ex.printStackTrace();
			LOG.error("ERROR enhancing spec {}", ex.getMessage());
		}

		/*APITree apiTree = new APITree(minedFolder, runPath);
		Document docTree = apiTree.getAPITree();
		try {
			apiTree.getWorkDirManager().exportAPITree(docTree);
		} catch (TransformerException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
