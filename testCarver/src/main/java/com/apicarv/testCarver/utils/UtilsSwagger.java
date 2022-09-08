package com.apicarv.testCarver.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;

public class UtilsSwagger {
    private static final Logger LOG = LoggerFactory.getLogger(UtilsSwagger.class);

    /**
     *
     * @param openAPIData
     * @return
     * @throws JsonProcessingException
     */
    public static OpenAPI readOpenAPISpecFromString(String openAPIData) throws JsonProcessingException {
//        ObjectMapper objectMapper = Yaml.mapper();
//        YAMLFactory factory  = (YAMLFactory) objectMapper.getFactory();
//        factory.configure(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID, false);
//        return objectMapper.readerFor(OpenAPI.class).readValue(openAPIData);
        SwaggerParseResult parseResult = new OpenAPIParser().readContents(openAPIData, new ArrayList<>(), new ParseOptions());
        return parseResult.getOpenAPI();
    }

    public static OpenAPI readOpenAPISpecFromFile(String location) throws JsonProcessingException {
//        ObjectMapper objectMapper = Yaml.mapper();
//        YAMLFactory factory  = (YAMLFactory) objectMapper.getFactory();
//        factory.configure(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID, false);
//        return objectMapper.readerFor(OpenAPI.class).readValue(openAPIData);
        SwaggerParseResult parseResult = new OpenAPIParser().readLocation(location, new ArrayList<>(), new ParseOptions());
        return parseResult.getOpenAPI();
    }

    /**
     * Write yaml value string.
     *
     * @param openAPI
     *            the open api
     * @return the string
     * @throws JsonProcessingException
     *             the json processing exception
     */
    public static String stringifyYaml(OpenAPI openAPI) throws JsonProcessingException {
        String result;
        ObjectMapper objectMapper = Yaml.mapper();
        YAMLFactory factory = (YAMLFactory) objectMapper.getFactory();
        factory.configure(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID, false);
        result = objectMapper.writerFor(OpenAPI.class).writeValueAsString(openAPI);
        return result;
    }

    public static String jsonifyOpenAPI(OpenAPI openAPI) throws JsonProcessingException {
        ObjectMapper objectMapper = Json.mapper();
        String result = objectMapper.writerFor(OpenAPI.class).writeValueAsString(openAPI);
        return result;
    }

    public static OpenAPI getGroundTruthSpec(Settings.SUBJECT subject) {
        try{
            String yamlPath = Paths.get("src", "main", "resources", "webapps", subject.name(), "openapi.yml").toAbsolutePath().toString();
            return readOpenAPISpecFromFile(yamlPath);
        }catch (Exception ex){
            LOG.error("Error getting ground truth for subject {}", subject);
            return null;
        }
    }
}
