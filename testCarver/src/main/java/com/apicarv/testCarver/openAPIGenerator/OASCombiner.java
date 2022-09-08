package com.apicarv.testCarver.openAPIGenerator;

import com.beust.jcommander.internal.Lists;
import com.apicarv.testCarver.apirecorder.NetworkEvent;
import com.apicarv.testCarver.apirunner.APIResponse;
import com.apicarv.testCarver.utils.UtilsAPIRunner;
import com.apicarv.testCarver.utils.UtilsOASGen;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import org.apache.http.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * To combine two OpenAPI specs
 *
 */
public class OASCombiner {
    private static final Logger LOG = LoggerFactory.getLogger(OASCombiner.class);

    /**
     * combine paths from two specs
     * @param spec1
     * @param spec2
     * @return
     */
    public static OpenAPI combineSpecs(OpenAPI spec1, OpenAPI spec2){
        OpenAPI combined = new OpenAPI();

        return combined;
    }

    /**
     * Add examples to known specification
     * @param original
     * @param knownResponses
     * @return
     */
    public static OpenAPI enhanceSpec(OpenAPI original, List<APIResponse> knownResponses){

        System.out.println(original.getServers());
//        System.out.println(original.getInfo());
        System.out.println(knownResponses.size());
        OpenAPI enhanced = original;

        Paths paths = original.getPaths();
        List<String> pathList = Lists.newArrayList(paths.keySet());
        List<String> bases = new ArrayList<>();
        for(Server server: original.getServers()){
            bases.add(server.getUrl());
        }

        if(bases.isEmpty()){
            bases.add("");
        }

        /* From restats:
            pathsInSpec.sort()
            paths_re = [re.sub('\{{1}[^{}}]*\}{1}', '[^/]+', x) for x in regPaths]
	        paths_re = [x + '?$' if x[-1] == '/' else x + '/?$' for x in paths_re]
	        paths_re = [re.compile(x) for x in paths_re]
         */


        Collections.sort(pathList);
        List<String> regexList = new ArrayList<>();

        Map<String, String> regexPathItems = new HashMap<>();
        Map<String, String> regexBase = new HashMap<>();

        for(String path: pathList){
            for(String base: bases) {
                if(base.endsWith("/")){
                    base = base.substring(0, base.length()-1);
                }
                if(!path.startsWith("/")){
                    path = "/" + path;
                }
                System.out.println(base + path);
                String regex = base + path;
                try {
                    regex = new URL(regex).getPath();
                } catch (MalformedURLException e) {
                    LOG.error("Path {} is not full URL", base+path);
                    regex = base+path;
                }

                regex = regex.replaceAll("\\{[^\\}]*\\}", "[^/]+");
                regexList.add(regex);
                System.out.println(base + "-" + regex);
                regexBase.put(regex, base);
                regexPathItems.put(regex, path);
            }
        }

        for(APIResponse knownResponse: knownResponses){
            if(knownResponse.getRequest() == null || knownResponse.getRequest().getRequestUrl() == null){
                continue;
            }
            if(knownResponse.getResponse()==null || !UtilsOASGen.isGoodServerStatus(knownResponse.getResponse().getStatus())){
                continue;
            }
            if(knownResponse.getRequest().getMethod() == NetworkEvent.MethodClazz.OPTIONS
                    || knownResponse.getRequest().getMethod() == NetworkEvent.MethodClazz.UNKNOWN
                    || knownResponse.getRequest().getMethod() == NetworkEvent.MethodClazz.TRACE
                    || knownResponse.getRequest().getMethod() == NetworkEvent.MethodClazz.HEAD
            ){
//                System.out.println("Ignoring ");
                continue;
            }
            String url = knownResponse.getRequest().getRequestUrl();
            String path = null;
            try {
                path = new URL(url).getPath();
            } catch (MalformedURLException e) {
                LOG.error("skipping response {} because url is not valid", knownResponse.getId());
                continue;
            }
            String matchingRegex = null;
            for(String regex: regexList){
                if(path.matches(regex)){
                    matchingRegex = regex;
                    System.out.println(knownResponse.getRequest().getMethod() + "-" + url + " : " + regex);
                    break;
                }
            }
            if(matchingRegex == null){
                // No matching regex found for path.
                LOG.info("No regex for {}", url);
                continue;
            }
            try {
                PathItem pathItem = SwaggerGenerator.getPathItemForRequest(knownResponse);
                List<Parameter> inferredParams = pathItem.getParameters();

                PathItem originalPathItem = paths.get(regexPathItems.get(matchingRegex));
                List<Parameter> paramsInPath = originalPathItem.getParameters();

                List<Parameter> paramsInOp = null;

                switch (knownResponse.getRequest().getMethod()){
                    case GET:
                        paramsInOp = originalPathItem.getGet().getParameters();
                        break;
                    case PUT:
                        paramsInOp = originalPathItem.getPut().getParameters();
                        break;
                    case POST:
                        paramsInOp = originalPathItem.getPost().getParameters();
                        break;
                    case DELETE:
                        paramsInOp =  originalPathItem.getDelete().getParameters();
                        break;
                    case OPTIONS:
                        paramsInOp = originalPathItem.getOptions().getParameters();
                        break;
                    case HEAD:
                        paramsInOp = originalPathItem.getHead().getParameters();
                        break;
                    case TRACE:
                        paramsInOp = originalPathItem.getTrace().getParameters();
                        break;
                    case PATCH:
                        paramsInOp = originalPathItem.getPatch().getParameters();
                        break;
                    default:
                        break;
                }

                List<Parameter> specParams = new ArrayList<>();
                if(paramsInPath!=null)
                    specParams.addAll(paramsInPath);
                if(paramsInOp!=null)
                    specParams.addAll(paramsInOp);

                if(specParams == null){
                    LOG.info("No parameters for PathItem {}", regexPathItems.get(matchingRegex));
                    continue;
                }
                // Match Parameters. Add examples for each parameter
                // Verify that the parameters are correct.
                for(Parameter specParam: specParams){
                    switch(specParam.getIn().toLowerCase()){
                        case "path":
                            //Required. Get the path variable example from url
                            String name = specParam.getName();
                            String pathUrl = regexBase.get(matchingRegex) + regexPathItems.get(matchingRegex);
                            List<String> split = Lists.newArrayList(pathUrl.split("/"));
                            int position = split.indexOf("{" + name + "}");
                            String exampleValue = url.split("/")[position];
                            LOG.info("Example - {} : {}", name, exampleValue);
                            if(specParam.getExamples() == null){
                                specParam.setExamples(new HashMap<>());
                            }
                            Example example = new Example();
                            example.setValue(exampleValue);
                            specParam.getExamples().put(knownResponse.getRequest().getRequestId(), example);
                            break;
                        case "query":
                            name = specParam.getName();
                            exampleValue = null;
                            List<NameValuePair> queryPairs = UtilsAPIRunner.getPostData(new URL(url).getQuery());
                            for(NameValuePair pair: queryPairs){
                                if(pair.getName().equalsIgnoreCase(name)){
                                    exampleValue = pair.getValue();
                                    LOG.info("Example - {} : {}", name, exampleValue);
                                    break;
                                }
                            }
                            if(exampleValue!=null){
                                if(specParam.getExamples() == null){
                                    specParam.setExamples(new HashMap<>());
                                }
                                example = new Example();
                                example.setValue(exampleValue);
                                specParam.getExamples().put(knownResponse.getRequest().getRequestId(), example);
                            }
                            break;
                        default:
                            break;
                    }
                }


            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

//        enhanced.setInfo(original.getInfo());
//        enhanced.getInfo().setDescription("Enhanced using " + knownResponses.size() + "responses" + original.getInfo().getDescription());

        return enhanced;
    }
}
