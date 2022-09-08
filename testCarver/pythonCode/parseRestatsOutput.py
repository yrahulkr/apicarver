import os
from datetime import datetime

import constants
import runRestats
import utilsRun


def fetchRestatsOutputDir(appName):
    returnDict = {}
    toolOutputs = runRestats.getExistingOutput(appName)
    if toolOutputs is None:
        print("No restats results found {}".format(appName))
        return None
    for toolOutput in toolOutputs.keys():
        if len(toolOutputs[toolOutput]) == 0:
            print(toolOutput + " : No output" )
            continue
        returnDict[toolOutput] = []
        if len(toolOutputs[toolOutput]) == 1:
            outputDir = toolOutputs[toolOutput][0]
            print(toolOutput + " : " + outputDir + " : " + str(os.path.exists(outputDir)))
            if toolOutput == "carverMerge" :
                reportsDir = os.path.splitext(outputDir)[0] + "carver" + "_reports"
            elif toolOutput == "proberMerge":
                reportsDir = os.path.splitext(outputDir)[0] + "prober" + "_reports"
            elif toolOutput in ["inferredJson", "proberJson"]:
                reportsDir = os.path.splitext(outputDir)[0] + "_json_reports"
            else:
                reportsDir = os.path.splitext(outputDir)[0] + "_reports"
            print(reportsDir + " " + str(os.path.exists(reportsDir)))
            if os.path.exists(reportsDir):
                returnDict[toolOutput].append(reportsDir)
            continue

        for runIndex in range(len(toolOutputs[toolOutput])):
            outputDir = toolOutputs[toolOutput][runIndex]
            print(toolOutput + str(runIndex) + " : " + outputDir + " : " + str(os.path.exists(outputDir)))
            if toolOutput == "carverMerge" :
                reportsDir = os.path.splitext(outputDir)[0] + "carver" + "_reports"
            elif toolOutput == "proberMerge":
                reportsDir = os.path.splitext(outputDir)[0] + "prober" + "_reports"
            else:
                reportsDir = os.path.splitext(outputDir)[0] + "_reports"
            print(reportsDir + " " + str(os.path.exists(reportsDir)))
            if os.path.exists(reportsDir):
                returnDict[toolOutput].append(reportsDir)
    return returnDict


def getOASCoverageStats(toolKey, reportsDir):
    statsFile = os.path.join(reportsDir, "stats.json")
    if not os.path.exists(statsFile):
        print("Stats not available at {}", statsFile)
        return None
    statsJson = utilsRun.importJson(jsonFile=statsFile)
    return {"tool": toolKey,
            "pathTotal": statsJson["pathCoverage"]["raw"]["documented"],
            "pathTested": statsJson["pathCoverage"]["raw"]["documentedAndTested"],
            "pathRate": statsJson["pathCoverage"]["rate"],
            "operationTotal": statsJson["operationCoverage"]["raw"]["documented"],
            "operationTested": statsJson["operationCoverage"]["raw"]["documentedAndTested"],
            "operationRate": statsJson["operationCoverage"]["rate"],
            "statusClassTotal": statsJson["statusClassCoverage"]["raw"]["documented"],
            "statusClassTested": statsJson["statusClassCoverage"]["raw"]["documentedAndTested"],
            "statusClassRate": statsJson["statusClassCoverage"]["rate"],
            "statusTotal": statsJson["statusCoverage"]["raw"]["documented"],
            "statusTested": statsJson["statusCoverage"]["raw"]["documentedAndTested"],
            "statusRate": statsJson["statusCoverage"]["rate"],
            "responseTypeTotal": statsJson["responseTypeCoverage"]["raw"]["documented"],
            "responseTypeTested": statsJson["responseTypeCoverage"]["raw"]["documentedAndTested"],
            "responseTypeRate": statsJson["responseTypeCoverage"]["rate"],
            "requestTypeTotal": statsJson["requestTypeCoverage"]["raw"]["documented"],
            "requestTypeTested": statsJson["requestTypeCoverage"]["raw"]["documentedAndTested"],
            "requestTypeRate": statsJson["requestTypeCoverage"]["rate"],
            "parameterTotal": statsJson["parameterCoverage"]["raw"]["documented"],
            "parameterTested": statsJson["parameterCoverage"]["raw"]["documentedAndTested"],
            "parameterRate": statsJson["parameterCoverage"]["rate"]
            }


def getOASCompStats(toolKey, reportsDir):
    statsFile = os.path.join(reportsDir, "stats.json")
    if not os.path.exists(statsFile):
        print("Stats not available at {}", statsFile)
        return None
    statsJson = utilsRun.importJson(jsonFile=statsFile)

    # return {"tool": toolKey,
    #         "pathPr": statsJson["pathPr"],
    #         "pathRe": statsJson["pathRe"],
    #         "operationPr": statsJson["operationPr"],
    #         "operationRe": statsJson["operationRe"]
    #         }

    if toolKey in ["inferredYaml", "proberYaml"]:
        return {"tool": toolKey,
            "paths_matched": statsJson["path"]["matched"],
            "paths_unmatched": statsJson["path"]["unmatched"],
            "paths_matched_unique": statsJson["path"]["matched_unique"],
            "paths_original": statsJson["path"]["gt"],
            "op_matched": statsJson["op"]["matched"],
            "op_unmatched": statsJson["op"]["unmatched"],
            "op_matched_unique": statsJson["op"]["matched_unique"],
            "op_original": statsJson["op"]["gt"],
            "params_matched": -1,
            "params_fp": -1,
            "params_original": -1
            }
    else:
        return {"tool": toolKey,
            "paths_matched": statsJson["path"]["matched"],
            "paths_unmatched": statsJson["path"]["unmatched"],
            "paths_matched_unique": statsJson["path"]["matched_unique"],
            "paths_original": statsJson["path"]["gt"],
            "op_matched": statsJson["op"]["matched"],
            "op_unmatched": statsJson["op"]["unmatched"],
            "op_matched_unique": statsJson["op"]["matched_unique"],
            "op_original": statsJson["op"]["gt"],
            "params_matched": statsJson["var"]["matched"],
            "params_fp": statsJson["var"]["fp"],
            "params_original": statsJson["var"]["gt"]
            }


def getRestatsResults(appName):
    results = []
    oasResults = []
    outputs = fetchRestatsOutputDir(appName)
    if outputs is None:
        print("No outputs found for {}".format(appName))
        return None, None
    for output in outputs.keys():
        if not (output == "carverRecords" or output == "uiRecords" or output == "proberRecords"):
            avgResult = {"tool": output,
                     "pathTotal": 0,
                     "pathTested": 0,
                     "pathRate": 0,
                     "operationTotal": 0,
                     "operationTested": 0,
                     "operationRate": 0,
                     "statusClassTotal": 0,
                     "statusClassTested": 0,
                     "statusClassRate": 0,
                     "statusTotal": 0,
                     "statusTested": 0,
                     "statusRate": 0,
                     "responseTypeTotal": 0,
                     "responseTypeTested": 0,
                     "responseTypeRate": 0,
                     "requestTypeTotal": 0,
                     "requestTypeTested": 0,
                     "requestTypeRate": 0,
                     "parameterTotal": 0,
                     "parameterTested": 0,
                     "parameterRate": 0,
                         "app":appName
                     }

        for runIndex in range(len(outputs[output])):
            if output in ["inferredYaml", "proberYaml", "inferredJson", "proberJson"]:
                result = getOASCompStats(output, outputs[output][runIndex])
                if result is not None:
                    result['app'] = appName
                    result['file'] = outputs[output][runIndex]
                    oasResults.append(result)
            else:
                if output == "carverRecords" or output == "uiRecords" or output == "proberRecords":
                    result = getOASCoverageStats(output, outputs[output][runIndex])
                    if result is not None:
                        result['app'] = appName
                        result['file'] = outputs[output][runIndex]
                        results.append(result)
                else:
                    result = getOASCoverageStats(output + str(runIndex), outputs[output][runIndex])

                    if result is not None:
                        result['app'] = appName
                        result['file'] = outputs[output][runIndex]
                        results.append(result)
                        avgResult["pathTotal"] += result["pathTotal"]
                        avgResult["pathTested"] +=result["pathTested"]
                        avgResult["pathRate"]
                        avgResult["operationTotal"] +=result["operationTotal"]
                        avgResult["operationTested"] +=result["operationTested"]
                        avgResult["operationRate"]
                        avgResult["statusClassTotal"] +=result["statusClassTotal"]
                        avgResult["statusClassTested"] +=result["statusClassTested"]
                        avgResult["statusClassRate"]
                        avgResult["statusTotal"] +=result["statusTotal"]
                        avgResult["statusTested"] +=result["statusTested"]
                        avgResult["statusRate"]
                        avgResult["responseTypeTotal"] +=result["responseTypeTotal"]
                        avgResult["responseTypeTested"] +=result["responseTypeTested"]
                        avgResult["responseTypeRate"]
                        avgResult["requestTypeTotal"] +=result["requestTypeTotal"]
                        avgResult["requestTypeTested"] +=result["requestTypeTested"]
                        avgResult["requestTypeRate"]
                        avgResult["parameterTotal"] +=result["parameterTotal"]
                        avgResult["parameterTested"] +=result["parameterTested"]
                        avgResult["parameterRate"]

        if not (output == "carverRecords" or output == "uiRecords" or output == "proberRecords"):
            try:
                avgResult["pathRate"] = avgResult["pathTested"]/avgResult["pathTotal"]
            except:
                print("err")

            try:
                avgResult["operationRate"] = avgResult["operationTested"]/avgResult["operationTotal"]
            except:
                print("err")
            try:
                avgResult["statusClassRate"]= avgResult["statusClassTested"]/avgResult["statusClassTotal"]
            except:
                print("err")

            try:
                avgResult["statusRate"] = avgResult["statusTested"]/avgResult["statusTotal"]
            except:
                print("err")

            try:
                avgResult["responseTypeRate"] = avgResult["responseTypeTested"]/avgResult["responseTypeTotal"]
            except:
                print("err")
            try:
                avgResult["requestTypeRate"]  = avgResult["requestTypeTested"]/avgResult["requestTypeTotal"]
            except:
                print("err")

            try:
                avgResult["parameterRate"] = avgResult["parameterRate"]/avgResult["parameterTotal"]
            except:
                print("err")

            results.append(avgResult)

    return results, oasResults

def getAllRestatsResults():
    totalResults = []
    totalOASResults = []
    for app in constants.APPS:
        results, oasResults = getRestatsResults(app)
        if results is not None:
            totalResults.extend(results)
        if oasResults is not None:
            totalOASResults.extend(oasResults)
    print(totalResults)
    print(totalOASResults)
    return totalResults, totalOASResults


if __name__ == "__main__":
    # returnDict = fetchRestatsOutputDir("petclinic")
    # print(returnDict)
    # results = getRestatsResults("petclinic")
    # print(results)

    totalResults, totalOASResults = getAllRestatsResults()
    utilsRun.writeCSV_Dict(totalResults[0].keys(), csvRows=totalResults,dst="../results/apiTestCov_"+datetime.now().strftime("%Y%m%d-%H%M%S")+".csv")
    utilsRun.writeCSV_Dict(totalOASResults[0].keys(), csvRows=totalOASResults,dst="../results/specCompare_"+datetime.now().strftime("%Y%m%d-%H%M%S")+".csv")
