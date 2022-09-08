package com.apicarv.testCarver.openAPIGenerator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.apicarv.testCarver.utils.Settings;
import org.junit.Test;

public class ProberTestsMdBip {
	@Test
	public void testMdBip()  {

//		String minedFolder = "parabank-20220426_144511";
//		String runFolder = "/Users/apicarv/git/TestCarving/testCarver/out/parabank-20220426_144511/run/20220426_182634";
//		APIGraph apiGraph = new APIGraph(minedFolder, runFolder);

		String minedFolder = "mdh-20220610_151241";
		String runFolder = "20220613_144858";
		String runPath = "/Users/apicarv/git/TestCarving/testCarver/out/" + minedFolder + "/run/" + runFolder;
		Settings.SUBJECT subject = Settings.SUBJECT.mdh;
		String baseURL = "http://localhost:8080";
		APIGraph apiGraph = new APIGraph(subject, minedFolder, runPath);

//		DirectedAcyclicGraph<URLNode, DefaultEdge> graph= apiTree.getAPIGraph();
//		
//		apiTree.pruneGraph();
		
		apiGraph.buildAPIGraph();
		apiGraph.pruneGraph();
		
		Set<String> endpoints = apiGraph.getApiResponses().stream().filter(apiResponse->apiResponse.getResponse().getResourceType().contains("json")).map(apiResponse-> apiResponse.getId()+"-"+apiResponse.getRequest().getRequestUrl()).collect(Collectors.toSet());
		System.out.printf("%s \n", endpoints);
		
		Set<URLNode> leafNodes = apiGraph.graph.vertexSet().stream().filter(vertex->vertex.isLeaf).collect(Collectors.toSet());
		
		System.out.printf("%s \n", leafNodes);
		
		Set<URLNode> hasResponses  = apiGraph.graph.vertexSet().stream().filter(vertex->vertex.payloadToCompare!=null).collect(Collectors.toSet());
		System.out.printf("%s \n", hasResponses);
		
		APIProber prober = new APIProber(apiGraph.getApiResponses(), Settings.SUBJECT.parabank, "http://localhost:8080/parabank-3.0.0-SNAPSHOT/services_proxy/bank", minedFolder, runFolder, null);
		
		List<ProbeEvent> probes = prober.getMDBpEvents(apiGraph);
		
	}

}
