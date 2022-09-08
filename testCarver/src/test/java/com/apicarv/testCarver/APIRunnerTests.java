package com.apicarv.testCarver;

import java.io.IOException;
import java.util.List;

import com.apicarv.testCarver.apirecorder.NetworkEvent;
import com.apicarv.testCarver.apirunner.APIRunner;
import com.apicarv.testCarver.utils.Settings;
import com.apicarv.testCarver.utils.UtilsJson;
import org.junit.Test;

public class APIRunnerTests {
	@Test
	public void TestCookieCache() throws IOException {
		String jsonFile = Settings.outputDir + Settings.sep
				 + Settings.ClEANED_GEN_EVENTS_JSON;

		List<NetworkEvent> cleanedEvents = UtilsJson.importGeneratedEvents(jsonFile);
		
		APIRunner runner = new APIRunner("out/default/gen/default");
		
		runner.runEvents(cleanedEvents);
		
	}
}
