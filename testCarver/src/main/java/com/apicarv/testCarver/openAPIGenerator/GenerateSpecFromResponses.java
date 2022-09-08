package com.apicarv.testCarver.openAPIGenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.apicarv.testCarver.Main;
import com.apicarv.testCarver.apirunner.APIResponse;
import com.apicarv.testCarver.utils.Settings;
import com.apicarv.testCarver.utils.UtilsJson;
import com.apicarv.testCarver.utils.WorkDirManager;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.List;

public class GenerateSpecFromResponses {

    public static void main(String args[]) throws MalformedURLException, JsonProcessingException {
        String app = args[0];

        Settings.SUBJECT subject = Main.getSubject(app);

        String minedFolder = args[1];

        String runFolder = args[2];

        String oasFolder = args[3];

        String runPath = "/Users/apicarv/git/TestCarving/testCarver/out/" + subject.name() + File.separator +  minedFolder + "/run/" + runFolder;
        String oasPath = "/Users/apicarv/git/TestCarving/testCarver/out/" + subject.name() + File.separator +  minedFolder + "/oas/" + oasFolder;

        List<APIResponse> resultResponses = UtilsJson.importAPIResponses(Paths.get(runPath, Settings.RESULT_RESPONSES).toString());

        WorkDirManager workDirManager = new WorkDirManager( subject, WorkDirManager.DirType.OAS, minedFolder, runPath, oasPath);
        SwaggerGenerator generator = new SwaggerGenerator(subject, Main.getBaseUrl(subject), resultResponses, workDirManager);

        generator.rerunSwaggerGeneration();

    }

    @Test
    public void testSwaggerGen() throws MalformedURLException, JsonProcessingException {
        GenerateSpecFromResponses.main(new String[]{"booker", "20220711_144103", "20220711_151446", "20220711_151840"});
        GenerateSpecFromResponses.main(new String[]{"ecomm", "20220827_151530", "20220827_154951", "20220827_155323"});
        GenerateSpecFromResponses.main(new String[]{"jawa", "20220711_025641", "20220711_032114", "20220711_032415"});
        GenerateSpecFromResponses.main(new String[]{"medical", "20220827_125716", "20220827_130733", "20220827_131043"});
        GenerateSpecFromResponses.main(new String[]{"parabank", "20220710_035630", "20220710_042938", "20220710_043329"});
        GenerateSpecFromResponses.main(new String[]{"petclinic", "20220709_220701", "20220709_225833", "20220709_230035"});
        GenerateSpecFromResponses.main(new String[]{"realworld", "20220711_005952", "20220711_011334", "20220711_011717"});
    }

}

