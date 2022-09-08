package com.apicarv.testCarver.openAPIGenerator;

import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.apicarv.testCarver.apirecorder.NetworkEvent;
import com.apicarv.testCarver.apirunner.APIResponse;
import com.apicarv.testCarver.utils.UtilsJson;
import com.apicarv.testCarver.utils.UtilsOASGen;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.gson.Gson;

@RunWith(MockitoJUnitRunner.class)

public class ProberTests {
	@Mock 
	APIGraph apiGraph;

	String responseJson = "[{id:1}]";
	
	String apiResponseFormat = "{\n"
			+ "        \"duration\": 0,\n"
			+ "        \"id\": %(id),\n"
			+ "        \"request\": {\n"
			+ "            \"clazz\": \"RequestWillBeSent\",\n"
			+ "            \"headers\": [\n"
			+ "               \n"
			+ "            ],\n"
			+ "            \"id\": 0,\n"
			+ "            \"method\": \"%(method)\",\n"
			+ "            \"requestId\": \"req1\",\n"
			+ "            \"requestUrl\": \"%(url)\",\n"
			+ "            \"resourceType\": \"application/json\",\n"
			+ "			   \"postData\": \"%(postData)\"\n"
			+ "        },\n"
			+ "        \"response\": {\n"
			+ "            \"clazz\": \"ResponseReceived\",\n"
			+ "            \"headers\": [\n"
			+ "                \n"
			+ "            ],\n"
			+ "            \"id\": 0,\n"
			+ "            \"message\": \"200\",\n"
			+ "            \"method\": \"UNKNOWN\",\n"
			+ "            \"requestUrl\": \"%(url)\",\n"
			+ "            \"resourceType\": \"application/json\",\n"
			+ "            \"status\": 200\n"
			+ "        },\n"
			+ "        \"status\": \"SUCCESS\"\n"
			+ "    }";

	String probeFormat = "{" +
			"\"probeType\":\"MDI2L\"," +
			"\"probeStatus\":\"SUCCESS\"," +
			"\"priorityScore\":0," +
			"\"id\":%(id)," +
			"\"requestId\":\"probe0\"," +
			"\"clazz\":\"Probe\"," +
			"\"method\":\"%(method)\"," +
			"\"requestUrl\":\"%(url)\"" +
			",\"headers\":[]," +
			"\"postData\": \"%(postData)\"" +
			"}" ;
	APIResponse getAPIResponse(int id, String method, String url, String postData) {
		String[] search = {"%(method)", "%(id)", "%(url)", "%(postData)"}; 
		String[] arguments = {method, ""+id, url, postData};
		String jsonString =  StringUtils.replaceEach(apiResponseFormat, search , arguments);
		return (new Gson()).fromJson(jsonString, APIResponse.class);
	}


	ProbeEvent getProbe(int id, String method, String url, String postData) {
		String[] search = {"%(method)", "%(id)", "%(url)", "%(postData)"};
		String[] arguments = {method, ""+id, url, postData};
		String jsonString =  StringUtils.replaceEach(probeFormat, search , arguments);
		return (new Gson()).fromJson(jsonString, ProbeEvent.class);
	}
	
	@Test
	public void testGetMissingOperations() {
		List<APIResponse> responses = new ArrayList<>();
		
		responses.add(getAPIResponse(0, "GET", "http://localhost:9292/api/static1", ""));
		responses.add(getAPIResponse(0, "POST", "http://localhost:9292/api/static1", "c=ds"));
		responses.add(getAPIResponse(1, "GET", "http://localhost:9292/api/static1/dynamic1", ""));
		responses.add(getAPIResponse(2, "POST", "http://localhost:9292/api/static2", "a=b&x=z"));
		APIProber prober = new APIProber(responses, null, "http://localhost:9292/api", null, null, null);
		List<ProbeEvent> missing = prober.findMissingOperations(responses, new ArrayList<>());
		System.out.println(missing);
	} 
 
	
	@Test 
	public void testProbeScheduling() {
		List<APIResponse> responses = new ArrayList<>();
		responses.add(getAPIResponse(0, "GET", "http://localhost:9292/api/static1", ""));
		responses.add(getAPIResponse(1, "GET", "http://localhost:9292/api/static1/dynamic1", ""));
		responses.add(getAPIResponse(2, "POST", "http://localhost:9292/api/static2", "a=b&x=z"));
		
		APIProber prober  = new APIProber(responses, null, "http://localhost:9292/api", null, null, null);
//		prober.expandGraph();
		List<ProbeEvent> missing = prober.findMissingOperations(responses, new ArrayList<>());
		
		Mockito.when(prober.executeNormalEvent(any(), any(NetworkEvent.class), any(), any())).then(setObject());
		
//		Mockito.doReturn(APIResponse.Status.SUCCESS).when(prober).executeProbeEvent(any(), any(), any(), any());
//		Mockito.doReturn(APIResponse.Status.SUCCESS).when(prober).executeNormalEvent(any(), any(), any(), any());
		
		List<NetworkEvent> eventSeq = prober.scheduleProbes(missing, responses, APIProber.ProbeScheduler.GRAPH_DEP);
		System.out.println(eventSeq);
	}

	@Test
	public void testProbeScheduling2() {
		List<APIResponse> responses = new ArrayList<>();
		String[] originalPaths = {"/users/user1/info",
				"/users/user2/info",
				"/users/user1/follow",
				"/users/user2",
				"/tags",
				"/articles",
				"/articles/2",
				"/articles/2/comments"};
		String[] methods = {
				"GET", "GET", "POST", "GET", "GET", "POST", "GET", "GET"
		};

		String[] probePaths = {
				//"/users",
				"/users/user1",
				//"/users/user2/follow",
				//"/articles/1",
				//"/articles/1/comments",
				"/tags/1",
				//"/tags/2",
				//"/tags/3"
		};

		List<ProbeEvent> probes = new ArrayList<>();
		for(int i=0; i<originalPaths.length; i++){
			responses.add(getAPIResponse(i, methods[i], "http://localhost" + originalPaths[i], ""));
		}

		System.out.println(responses);

		for(int i=0; i<probePaths.length; i++){
			probes.add(getProbe(i, "GET", "http://localhost"+probePaths[i], ""));
		}


		APIProber prober  = new APIProber(responses, null, "http://localhost", null, null, null);

		List<NetworkEvent> eventSeq = prober.scheduleProbes(probes, responses, APIProber.ProbeScheduler.GRAPH_DEP);
		System.out.println(eventSeq);
	}


	private Answer<?> setObject() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Test
	public void testResponseAnalysis() {
		APIResponse newResponse = getAPIResponse(0, "GET", "http://localhost:9292/api/static1", "");
		newResponse.getResponse().setData("{\"firstName\":\"David\",\"lastName\":\"Schroeder\",\"address\":\"2749 Blackhawk Trail\",\"city\":\"Madison\",\"telephone\":\"6085559435\",\"id\":9,\"pets\":[{\"name\":\"Freddy\",\"birthDate\":\"2010-03-09\",\"type\":{\"name\":\"bird\",\"id\":5},\"id\":11,\"ownerId\":9,\"visits\":[]}]}");
		System.out.println(newResponse.getResponse().getData());
		Set<String> returnList = UtilsOASGen.buildUrlUsingAPIResponseData(newResponse);
		System.out.println(returnList);
	}
	
	@Test
	public void testGetResponseProbes() {
		List<APIResponse> responses = new ArrayList<>();
		APIResponse newResponse = getAPIResponse(0, "GET", "http://localhost:9292/api/static1", "");
		newResponse.getResponse().setData("{\"firstName\":\"David\",\"lastName\":\"Schroeder\",\"address\":\"2749 Blackhawk Trail\",\"city\":\"Madison\",\"telephone\":\"6085559435\",\"id\":9,\"pets\":[{\"name\":\"Freddy\",\"birthDate\":\"2010-03-09\",\"type\":{\"name\":\"bird\",\"id\":5},\"id\":11,\"ownerId\":9,\"visits\":[]}]}");
		responses.add(newResponse);
		
		APIProber prober  = new APIProber(responses, null, "http://localhost:9292/api", null, null, null);

		List<ProbeEvent> probes = prober.buildProbesUsingResponseAnalysis(responses, "http://localhost:9292/api/");
		
		System.out.println(probes);
	}

	@Test
	public void testMinifySequence(){
		List<APIResponse> responsesWithGetProbes = UtilsJson.importAPIResponses("/Users/apicarv/git/TestCarving/testCarver/out/shopizer/20220716_051937/oas/20220716_053932/getPrresultResponses.json");
		for(APIResponse apiResponse: responsesWithGetProbes){
			try{
				boolean probe = !(
								apiResponse.getRequest().getRequestId().startsWith("probe")
										&&
										(apiResponse.getResponse()==null || !UtilsOASGen.isGoodServerStatus(apiResponse.getResponse().getStatus()))
						);
				System.out.println(apiResponse + " : " + probe);

			}catch(Exception ex){
				ex.printStackTrace();
			}
			if(apiResponse.getResponse() == null){
				System.out.println(apiResponse);
			}
		}
		APIProber.minifyAPISequence(responsesWithGetProbes);
	}

}
