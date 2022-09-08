package com.apicarv.testCarver.openAPIGenerator;

import com.apicarv.testCarver.apirunner.APIResponse;
import com.apicarv.testCarver.utils.Settings;
import com.apicarv.testCarver.utils.UtilsJson;
import com.apicarv.testCarver.utils.UtilsSwagger;
import io.swagger.models.Swagger;
import io.swagger.parser.OpenAPIParser;
import io.swagger.parser.SwaggerParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class OASCombinerTests {
    @Test
    public void testEnhance() throws IOException {
        Settings.SUBJECT subject = Settings.SUBJECT.petclinic;
        String basePath = "/Users/apicarv/git/TestCarving/testCarver/out/";
        String responsesFolder = "20220709_225833";
        String type = "run";
        String minedFolder = "20220709_220701";

        String responsesPath = Paths.get(basePath, subject.name(), minedFolder, type, responsesFolder).toString();
        String knownResponsesJson = Paths.get(responsesPath, "resultResponses.json").toString();

        //                "/Users/apicarv/git/TestCarving/testCarver/out/ecomm/20220824_195922/run/20220824_200516/resultResponses.json";
        //                "/Users/apicarv/git/TestCarving/testCarver/out/medical/20220802_023330/oas/20220802_211249/resultResponses.json";

        List<APIResponse> knownResponses = UtilsJson.importAPIResponses(knownResponsesJson);
        OpenAPI originalAPI = UtilsSwagger.getGroundTruthSpec(subject);
        OpenAPI enhanced = OASCombiner.enhanceSpec(originalAPI, knownResponses);
        String swagger = UtilsSwagger.stringifyYaml(enhanced);
        System.out.println(swagger);
        String oasFile = Paths.get(responsesPath, "openAPI_enhanced.yaml").toString();
        File oasFileInstance = new File(oasFile);
//        if(oasFileInstance.exists()){
//            Files.copy(Paths.get(responsesPath, "openAPI_enhanced.yaml"), Paths.get(responsesPath, "openAPI_enhanced_old.yaml"), StandardCopyOption.REPLACE_EXISTING);
//        }
//        UtilsJson.exportFile(oasFile, swagger);

//        WorkDirManager workDirManager = new WorkDirManager(Settings.SUBJECT.medical, minedFolder,
//                WorkDirManager.DirType.OAS,  "out/medical/20220802_023330/oas/20220802_211249");
//        workDirManager.exportOAS(swagger, "openAPI_enhanced.yaml");
    }



//    @Test
//    public void testQueryParamExamples() throws IOException {
//        String original = "src/main/resources/webapps/petclinic/openapi.yml";
//        ProberTests proberTests = new ProberTests();
//        List<APIResponse> knownResponses = new ArrayList<>();
//
//        knownResponses.add(proberTests.getAPIResponse(0, "GET", "http://localhost:9966/petclinic/api/owners/1", ""));
//        OpenAPI originalAPI = UtilsSwagger.readOpenAPISpecFromString(Files.readFile(new File(original)));
//        OpenAPI enhanced = OASCombiner.enhanceSpec(originalAPI, knownResponses);
//
//        String swagger = UtilsSwagger.stringifyYaml(enhanced);
//        System.out.println(swagger);
//    }

    @Test
    public void testSwaggerParser() throws IOException{
        String original = "src/main/resources/webapps/parabank/openapi.yml";
        Swagger swagger = new SwaggerParser().read(original);
        System.out.println(swagger.getInfo());
        SwaggerParseResult parseResult = new OpenAPIParser().readLocation(original, new ArrayList<>(), new ParseOptions());
        OpenAPI openAPI1 = parseResult.getOpenAPI();
        System.out.println(openAPI1.getInfo());
        OpenAPI openAPI2 = UtilsSwagger.getGroundTruthSpec(Settings.SUBJECT.parabank);
        System.out.println(openAPI2.getInfo());
    }
}
