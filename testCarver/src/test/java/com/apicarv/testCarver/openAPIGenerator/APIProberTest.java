package com.apicarv.testCarver.openAPIGenerator;

import com.apicarv.testCarver.utils.Settings;
import org.junit.Test;

public class APIProberTest {

    @Test
    public void getSimilarEvents() {
        String minedFolder = "mdh-20220610_151241";
        String runFolder = "20220613_144858";
        String runPath = "/Users/apicarv/git/TestCarving/testCarver/out/" + minedFolder + "/run/" + runFolder;
        Settings.SUBJECT subject = Settings.SUBJECT.mdh;
        String baseURL = "http://localhost:9448/mdh-tss/rest";
        APIGraph apiGraph = new APIGraph(subject, minedFolder, runPath);
        apiGraph.buildAPIGraph();
        apiGraph.pruneGraph();
        APIProber prober = new APIProber(apiGraph.getApiResponses(), Settings.SUBJECT.mdh, baseURL, minedFolder, runFolder, null);
        prober.getSimilarEvents(apiGraph);
    }
}
