package com.apicarv.testCarver.openAPIGenerator;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Collectors;

import com.apicarv.testCarver.apirunner.APIResponse;
import com.apicarv.testCarver.utils.Settings;
import com.apicarv.testCarver.utils.UtilsJson;
import com.apicarv.testCarver.utils.UtilsSwagger;
import com.apicarv.testCarver.utils.WorkDirManager;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.v3.oas.models.OpenAPI;

public class SwaggerTests {
	
	public static String exampleAPIResponse = "[{" + 
			"    \"id\": 26,\n" + 
			"    \"status\": \"SUCCESS\",\n" + 
			"    \"request\":\n" + 
			"    {\n" + 
			"        \"requestId\": \"E2F641C833AECCD9B60C12BDE2309878\",\n" + 
			"        \"clazz\": \"RequestWillBeSent\",\n" + 
			"        \"method\": \"GET\",\n" + 
			"        \"requestUrl\": \"http://localhost:8080/vets/1\",\n" + 
			"        \"headers\": [\n" + 
			"        {\n" + 
			"            \"name\": \"Upgrade-Insecure-Requests\",\n" + 
			"            \"value\": \"1\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"User-Agent\",\n" + 
			"            \"value\": \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) HeadlessChrome/95.0.4638.54 Safari/537.36\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Referer\",\n" + 
			"            \"value\": \"http://localhost:8080/vets.html/?page=2\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Host\",\n" + 
			"            \"value\": \"localhost:8080\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Connection\",\n" + 
			"            \"value\": \"keep-alive\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Upgrade-Insecure-Requests\",\n" + 
			"            \"value\": \"1\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"User-Agent\",\n" + 
			"            \"value\": \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) HeadlessChrome/95.0.4638.54 Safari/537.36\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Accept\",\n" + 
			"            \"value\": \"text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Sec-Fetch-Site\",\n" + 
			"            \"value\": \"same-origin\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Sec-Fetch-Mode\",\n" + 
			"            \"value\": \"navigate\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Sec-Fetch-User\",\n" + 
			"            \"value\": \"?1\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Sec-Fetch-Dest\",\n" + 
			"            \"value\": \"document\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Referer\",\n" + 
			"            \"value\": \"http://localhost:8080/vets.html/?page=2\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Accept-Encoding\",\n" + 
			"            \"value\": \"gzip, deflate, br\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Accept-Language\",\n" + 
			"            \"value\": \"en-US\"\n" + 
			"        }],\n" + 
			"        \"status\": 0,\n" + 
			"        \"resourceType\": \"text/html\"\n" + 
			"    },\n" + 
			"    \"response\":\n" + 
			"    {\n" + 
			"        \"headers\": [\n" + 
			"        {\n" + 
			"            \"name\": \"Vary\",\n" + 
			"            \"value\": \"Origin\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Vary\",\n" + 
			"            \"value\": \"Access-Control-Request-Method\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Vary\",\n" + 
			"            \"value\": \"Access-Control-Request-Headers\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Content-Type\",\n" + 
			"            \"value\": \"text/html;charset=UTF-8\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Content-Language\",\n" + 
			"            \"value\": \"en-US\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Transfer-Encoding\",\n" + 
			"            \"value\": \"chunked\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Date\",\n" + 
			"            \"value\": \"Wed, 27 Oct 2021 01:48:56 GMT\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Keep-Alive\",\n" + 
			"            \"value\": \"timeout=60\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"name\": \"Connection\",\n" + 
			"            \"value\": \"keep-alive\"\n" + 
			"        }],\n" + 
			"        \"status\": 404,\n" + 
			"        \"resourceType\": \"Content-Type: text/html;charset=UTF-8\"\n" + 
			"    },\n" + 
			"    \"duration\": 16\n" + 
			"}]";
	
	@Test
	public void test() throws MalformedURLException, JsonProcessingException {
		SwaggerGenerator gen = new SwaggerGenerator(Settings.SUBJECT.dummy, "dummy", "dummy", "http://localhost:8080");
		List<APIResponse> apiResponses = UtilsJson.importAPIResponsesFromString(exampleAPIResponse);
		OpenAPI openAPI = gen.getOpenAPI("http://localhost:8080", apiResponses);
		System.out.println(UtilsSwagger.stringifyYaml(openAPI));
	}
	
	
	@Test
	public void test_regex() {
		String regex = "\\{var[0-9]+\\}";
		
		assertTrue("{var2}".matches(regex));
		
	
		int variable = Integer.parseInt("{var3}".substring(4, "{var3}".indexOf('}')));
		assertTrue(variable==3);
	}


	@Test
	public void generateOASWithAPIResponses(){
		List<APIResponse> apiResponses = UtilsJson.importAPIResponses("src/test/resources/resultResponses.json");
		apiResponses = 	apiResponses.stream().filter(apiResponse -> apiResponse.getId()<1000).collect(Collectors.toList());
		WorkDirManager workDirManager_old = new WorkDirManager(Settings.SUBJECT.dummy, "src/test/resources", WorkDirManager.DirType.OAS, "src/test/resources");
		for(APIResponse response: apiResponses){
			try {
				String payload = workDirManager_old.getPayload(response.getId());
				response.getResponse().setData(payload);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		WorkDirManager workDirManager = new WorkDirManager(Settings.SUBJECT.dummy, "testProbe", WorkDirManager.DirType.OAS);
		String baseURL = "http://localhost:9966/petclinic/api";
		SwaggerGenerator gen = new SwaggerGenerator(Settings.SUBJECT.petclinic, baseURL, apiResponses, workDirManager);
		try {
			OpenAPI openAPI = gen.getOpenAPI(gen.getHostUrl(), gen.getApiResponses());
			workDirManager.exportOAS(openAPI, Settings.PROBED_OAS_EXPORT_FILE);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
