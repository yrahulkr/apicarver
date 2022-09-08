import datetime
import glob
import os
import xml.etree.ElementTree as ETree
from bs4 import BeautifulSoup

import constants
# import utilsRun
from subprocess import check_call, CalledProcessError


# combine schemathesis(vanilla) coverage with carver and prober coverage
import utilsRun
from runGeneratedTests import getExistingCrawl


def generateCombinedCoverage(stCov, toMerge, mergedFileName, classFolder):
    execFile = os.path.splitext(os.path.abspath(stCov))[0] + mergedFileName + ".exec"
    if os.path.exists(execFile):
        print("generation already done. Skipping {}".format(execFile))
        return constants.STATUS_SKIPPED
    mergeCommand = constants.JACOCO_MERGE_COMMAND.copy()
    mergeCommand.append(os.path.abspath(stCov))
    mergeCommand.append(os.path.abspath(toMerge))
    mergeCommand.append("--destfile")
    mergeCommand.append(execFile)
    try:
        check_call(mergeCommand)
    except Exception as ex:
        print("Could not merge files? ")
        print(ex)

    reportCommand = constants.JACOCO_REPORT_COMMAND.copy()
    reportCommand.append('--xml')
    xmlFile = os.path.splitext(os.path.abspath(stCov))[0] + mergedFileName + ".xml"
    reportCommand.append(xmlFile)
    reportCommand.append(execFile)
    reportCommand.append('--classfiles')
    reportCommand.append(classFolder)
    try:
        check_call(reportCommand)
    except Exception as ex:
        print("Could not generate report? ")
        print(ex)
        return constants.STATUS_ERRORED

    return constants.STATUS_SUCCESSFUL


def parseJacocoResults(xmlFile):
    try:
        print(xmlFile)
        eTree = ETree.parse(xmlFile)
        branchResult = None
        instrResult = None
        try:
            branchNode = eTree.findall("counter[@type='BRANCH']")[0]
            print(branchNode)
            branchResult = {"type": "branch", "missed": int(branchNode.attrib['missed']),
                        "covered": int(branchNode.attrib['covered'])
            , "total": int(branchNode.attrib['missed']) + int(branchNode.attrib['covered'])}
            print(branchResult)
        except Exception as ex1:
            print("Exception getting branch result for {}".format(xmlFile))
            print(ex1)

        try:
            instNode = eTree.findall("counter[@type='INSTRUCTION']")[0]
            instrResult = {"type": "instruction", "missed": int(instNode.attrib['missed']),
                       "covered": int(instNode.attrib['covered'])
            , "total": int(instNode.attrib['missed']) + int(instNode.attrib['covered'])}

            print(instrResult)
        except Exception as ex2:
            print("Exception getting Instruction result for {}".format(xmlFile))
            print(ex2)
        return {"branch": branchResult, "instruction": instrResult}
    except Exception as ex:
        print(ex)
        print("Error getting coverage for {}".format("xmlFile"))
        return None

def parseNYCResults(htmlFile):
    try:
        with open(htmlFile) as html:
            soup = BeautifulSoup(html, 'html.parser')
            try:
                rowElem = soup.find_all('td', attrs={"data-value": "app"})[0].parent
            except Exception as ex:
                print("Unable to get coverage for {}".format(htmlFile))
                return None
            branchNode = rowElem.find_all('td', 'abs')[1].get_text().split("/")
            instNode = rowElem.find_all('td', 'abs')[0].get_text().split("/")

            branchResult = {"type": "branch", "missed": int(branchNode[1]) - int(branchNode[0]),
                            "covered": int(branchNode[0])
                , "total": int(branchNode[1])}
            instrResult = {"type": "instruction", "missed": int(instNode[1]) - int(instNode[0]),
                           "covered": int(instNode[0])
                , "total": int(instNode[1])}

            print(branchResult)
            print(instrResult)
            return {"branch": branchResult, "instruction": instrResult}
    except Exception as ex:
        print("Exception getting NYC coverage data for {}", htmlFile)
        print(ex)
        return None

def getRawCovFiles(appName):
    appOutput = os.path.abspath(os.path.join("..", "out", appName))

    if not os.path.exists(appOutput):
        print("no output folder for {}".format(appName))
        return None
    if appName == "realworld":
        carverCov = glob.glob(appOutput + "/*/run/*/cov*/raw/")
        proberCov = glob.glob(appOutput + "/*/oas/*/allPr_cov*/raw/")
        stCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov/raw/")
        emCov = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov/raw/")
        stCarverCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_CARVER + "/*/cov/raw/")
        stProberCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_PROBER + "/*/cov/raw/")

    # elif appName == "jawa":
    #     carverCov = glob.glob(appOutput + "/*/run/*/cov*/" + constants.COV_EXEC)
    #     proberCov = glob.glob(appOutput + "/*/oas/*/allPr_cov*/" + constants.COV_EXEC)
    #     stCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov/" + constants.COV_EXEC)
    #     stCarverCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_CARVER + "/*/cov/" + constants.COV_EXEC)
    #     stProberCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_PROBER + "/*/cov/" + constants.COV_EXEC)
    elif appName == "booker":
        carverCov = glob.glob(appOutput + "/*/run/*/cov*/")
        proberCov = glob.glob(appOutput + "/*/oas/*/allPr_cov*/")
        stCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov*/")
        emCov = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov*/")
        stCarverCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_CARVER + "/*/cov*/")
        stProberCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_PROBER + "/*/cov*/")
    else:
        carverCov = glob.glob(appOutput + "/*/run/*/cov*/" + constants.COV_EXEC)
        proberCov = glob.glob(appOutput + "/*/oas/*/allPr_cov*/" + constants.COV_EXEC)
        stCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov/" + constants.COV_EXEC)
        emCov = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov/" + constants.COV_EXEC)
        stCarverCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_CARVER + "/*/cov/" + constants.COV_EXEC)
        stProberCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_PROBER + "/*/cov/" + constants.COV_EXEC)
    return {"carverCov": carverCov,
            "proberCov": proberCov,
            "stCov": stCov,
            "emCov": emCov,
            "stCarver": stCarverCov,
            "stProber": stProberCov}

def getCovFiles(appName):
    appOutput = os.path.abspath(os.path.join("..", "out", appName))
    if not os.path.exists(appOutput):
        print("no output folder for {}".format(appName))
        return None
    try:
        print(getExistingCrawl(appName, "HYBRID", -1.0, 30, ALL_CRAWLS = os.path.abspath(os.path.join("..", "crawlOut"))))
        crawlOutput, file = os.path.split(
            getExistingCrawl(appName, "HYBRID", -1.0, 30, ALL_CRAWLS = os.path.abspath(os.path.join("..", "crawlOut")))
            ['existingValidCrawls'][0])
    except Exception as ex:
        print(ex)
        print("Cannot get Crawl folder")
        crawlOutput = None
        uiTestCov = None


    if appName == "realworld":
        uiCov = glob.glob(appOutput + "/*/cov*/lcov-report/" + constants.NYC_REPORT)
        if crawlOutput is not None:
            uiTestCov = glob.glob(crawlOutput + "/test-results/0/nyc_output/lcov-report/" + constants.NYC_REPORT)
        carverCov = glob.glob(appOutput + "/*/run/*/cov*/lcov-report/" + constants.NYC_REPORT)
        proberCov = glob.glob(appOutput + "/*/oas/*/allPr_cov*/lcov-report/" + constants.NYC_REPORT)
        stCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov/lcov-report/" + constants.NYC_REPORT)
        stCarverMerge = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov/lcov-report/" + constants.NYC_CARVER_REPORT)
        stProberMerge = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov/lcov-report/" + constants.NYC_PROBER_REPORT)
        emCov = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov/lcov-report/" + constants.NYC_REPORT)
        emCarverMerge = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov/lcov-report/" + constants.NYC_CARVER_REPORT)
        emProberMerge = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov/lcov-report/" + constants.NYC_PROBER_REPORT)
        stCarverCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_CARVER + "/*/cov/lcov-report/" + constants.NYC_REPORT)
        stProberCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_PROBER + "/*/cov/lcov-report/" + constants.NYC_REPORT)
    elif appName == "jawa" or appName == "medical":
        uiCov = glob.glob(appOutput + "/*/cov*/" + constants.COV_JAWA_XML)
        if crawlOutput is not None:
            uiTestCov = glob.glob(crawlOutput + "/test-results/0/cov/" + constants.COV_JAWA_XML)
        carverCov = glob.glob(appOutput + "/*/run/*/cov*/" + constants.COV_JAWA_XML)
        proberCov = glob.glob(appOutput + "/*/oas/*/allPr_cov*/" + constants.COV_JAWA_XML)
        stCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov/" + constants.COV_JAWA_XML)
        stCarverMerge = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov/" + constants.COV_JAWA_CARVER_XML)
        stProberMerge = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov/" + constants.COV_JAWA_PROBER_XML)
        emCov = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov/" + constants.COV_JAWA_XML)
        emCarverMerge = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov/" + constants.COV_JAWA_CARVER_XML)
        emProberMerge = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov/" + constants.COV_JAWA_PROBER_XML)
        stCarverCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_CARVER + "/*/cov/" + constants.COV_JAWA_XML)
        stProberCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_PROBER + "/*/cov/" + constants.COV_JAWA_XML)
    elif appName == "parabank":
        uiCov = glob.glob(appOutput + "/*/cov*/" + constants.COV_XML)
        if crawlOutput is not None:
            uiTestCov = glob.glob(crawlOutput + "/test-results/0/cov/" + constants.COV_XML)
        carverCov = glob.glob(appOutput + "/*/run/*/cov*/" + constants.COV_XML)
        proberCov = glob.glob(appOutput + "/*/oas/*/allPr_cov*/" + constants.COV_XML)
        stCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov/" + constants.COV_XML)
        stCarverMerge = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov/" + constants.COV_PARABANK_CARVER_XML)
        stProberMerge = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov/" + constants.COV_PARABANK_PROBER_XML)
        emCov = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov/" + constants.COV_XML)
        emCarverMerge = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov/" + constants.COV_PARABANK_CARVER_XML)
        emProberMerge = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov/" + constants.COV_PARABANK_PROBER_XML)
        stCarverCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_CARVER + "/*/cov/" + constants.COV_XML)
        stProberCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_PROBER + "/*/cov/" + constants.COV_XML)
    elif appName == "booker":
        uiCov = glob.glob(appOutput + "/*/cov*/")
        if crawlOutput is not None:
            uiTestCov = glob.glob(crawlOutput + "/test-results/0/cov/")
        carverCov = glob.glob(appOutput + "/*/run/*/cov*/")
        proberCov = glob.glob(appOutput + "/*/oas/*/allPr_cov*/")
        stCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov*/")
        stCarverMerge = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov*/")
        stProberMerge = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov*/")
        emCov = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov*/")
        emCarverMerge = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov*/")
        emProberMerge = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov*/")
        stCarverCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_CARVER + "/*/cov*/")
        stProberCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_PROBER + "/*/cov*/")
    else:
        uiCov = glob.glob(appOutput + "/*/cov*/" + constants.COV_XML)
        if crawlOutput is not None:
            uiTestCov = glob.glob(crawlOutput + "/test-results/0/cov/" + constants.COV_XML)
        carverCov = glob.glob(appOutput + "/*/run/*/cov*/" + constants.COV_XML)
        proberCov = glob.glob(appOutput + "/*/oas/*/allPr_cov*/" + constants.COV_XML)
        stCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov/" + constants.COV_XML)
        stCarverMerge = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov/" + constants.COV_CARVER_XML)
        stProberMerge = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_OUTPUT + "/*/cov/" + constants.COV_PROBER_XML)
        emCov = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov/" + constants.COV_XML)
        emCarverMerge = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov/" + constants.COV_CARVER_XML)
        emProberMerge = glob.glob(appOutput + "/" + constants.EVOMASTER_OUTPUT + "/*/cov/" + constants.COV_PROBER_XML)
        stCarverCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_CARVER + "/*/cov/" + constants.COV_XML)
        stProberCov = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_PROBER + "/*/cov/" + constants.COV_XML)
    return {
            "uiCov": uiCov,
            "uiTestCov": uiTestCov,
            "carverCov": carverCov,
            "proberCov": proberCov,
            "stCov": stCov,
            "stCarverMerge": stCarverMerge,
            "stProberMerge": stProberMerge,
            "emCov": emCov,
            "emCarverMerge": emCarverMerge,
            "emProberMerge": emProberMerge,
            "stCarver": stCarverCov,
            "stProber": stProberCov
    }

def getResultForCovFile(covRecords, appName, tool):
    if appName == "realworld":
        result = parseNYCResults(covRecords)
    elif appName == "booker":
        if tool == "stCarverMerge" or tool == "emCarverMerge":
            pattern = "carver"
        elif tool == "stProberMerge" or tool == "emProberMerge":
            pattern = "prober"
        else:
            pattern = ""
        result = mergeCovFiles(covRecords, pattern)
    else:
        result = parseJacocoResults(covRecords)

    return result

def getAppResults(appName):
    results = []
    errors = []
    covFiles = getCovFiles(appName)
    print(covFiles)
    if covFiles is None:
        print("Ignoring App because no Coverage Data available.")
        status = constants.STATUS_SKIPPED
        return [{"app": appName, "status": status, "message": "No coverage data available"}]

    for key in covFiles.keys():
        if key in ["uiCov", "uiTestCov", "carverCov", "proberCov"]:
            try:
                covRecords = covFiles[key][0]
            except Exception as ex:
                print(ex)
                continue

            if covRecords is not None:
                result = getResultForCovFile(covRecords=covRecords, appName=appName, tool=key)
                if result is None:
                    result = {}
                    result['error'] = "error"
                    result['app'] = appName
                    result['tool'] = key
                    result['file'] = covRecords
                    errors.append(result)
                else:
                    result['branch']['app'] = appName
                    result['branch']['tool'] = key
                    result['branch']['file'] = covRecords
                    results.append(result['branch'])
                    result['instruction']['app'] = appName
                    result['instruction']['tool'] = key
                    result['instruction']['file'] = covRecords
                    results.append(result['instruction'])
        else:
            try:
                covRecordEntries = covFiles[key]
            except Exception as ex:
                print(ex)
                continue
            toolResults = []

            avgResult = {"branch": {"type": "branch", "missed": 0,"covered": 0, "total": 0}, "instruction": {"type": "instruction", "missed": 0,"covered": 0, "total": 0}}
            for runIndex in range(len(covRecordEntries)):
                covRecords = covRecordEntries[runIndex]
                result = getResultForCovFile(covRecords=covRecords, appName=appName, tool=key)
                if result is None:
                    result = {}
                    result['error'] = "error"
                    result['app'] = appName
                    result['tool'] = key+str(runIndex)
                    result['file'] = covRecords
                    errors.append(result)
                else:
                    avgResult['branch']['covered'] += result['branch']['covered']
                    avgResult['branch']['missed'] +=  result['branch']['missed']
                    avgResult['branch']['total'] += result['branch']['total']
                    result['branch']['app'] = appName
                    result['branch']['tool'] = key+str(runIndex)
                    result['branch']['file'] = covRecords
                    toolResults.append(result['branch'])

                    avgResult['instruction']['covered'] += result['instruction']['covered']
                    avgResult['instruction']['missed'] += result['instruction']['missed']
                    avgResult['instruction']['total'] += result['instruction']['total']
                    result['instruction']['app'] = appName
                    result['instruction']['tool'] = key+str(runIndex)
                    result['instruction']['file'] = covRecords
                    toolResults.append(result['instruction'])
            if toolResults is not None and len(toolResults) >0:
                avgResult['branch']['app'] = appName
                avgResult['branch']['tool'] = key
                avgResult['branch']['file'] = "Aggregate" + "_" + appName + "_" + key
                results.append(avgResult['branch'])
                avgResult['instruction']['app'] = appName
                avgResult['instruction']['tool'] = key
                avgResult['instruction']['file'] = "Aggregate" + "_" + appName + "_" + key
                results.append(avgResult['instruction'])
                results.extend(toolResults)
    return results


def getAllResults():
    totalResults = []
    for app in constants.APPS:
        results = getAppResults(app)
        if results is not None:
            totalResults.extend(results)
    return totalResults

def parseNYCTests():
    parseNYCResults("/TestCarving/testCarver/out/realworld/schemathesis_prober/cov/lcov-report/index.html")


def mergeCovFiles(covFolder, pattern=""):
    # covFiles = glob.glob(covFolder + pattern)
    # print(covFiles)
    finalResult = {"branch": {"type": "branch", "missed": 0, "covered":0, "total":0},
                   "instruction": {"type": "instruction", "missed": 0, "covered":0, "total":0}}
    # for covFile in covFiles:
    for module in constants.BOOKER_MODULES:
        covFile = os.path.join(os.path.abspath(covFolder), module + pattern + ".xml")
        results = parseJacocoResults(covFile)
        if results is not None:
            if "branch" in results and results["branch"] is not None:
                finalResult["branch"]["missed"] += results["branch"]["missed"]
                finalResult["branch"]["covered"] += results["branch"]["covered"]
                finalResult["branch"]["total"] += results["branch"]["total"]
            if "instruction" in results and results["instruction"] is not None:
                finalResult["instruction"]["missed"] += results["instruction"]["missed"]
                finalResult["instruction"]["covered"] += results["instruction"]["covered"]
                finalResult["instruction"]["total"] += results["instruction"]["total"]

    return finalResult
def mergeCovFilesTests():
    finalResult = mergeCovFiles("/TestCarving/testCarver/out/booker")
    print(finalResult)


def generateCombinedCoverageForApp(appName, TOOLS):
    succeeded = []
    errored = []
    skipped = []
    rawCovFiles = getRawCovFiles(appName)
    if appName == "petclinic" or appName == "parabank" or appName == "jawa" or appName == "ecomm" or appName == "medical" or appName == "shopizer":
        # These apps have a single module
        classFiles = os.path.abspath(os.path.join("..", "src", "main", "resources", "webapps", appName, "target", "classes"))
        carverCov = rawCovFiles["carverCov"]
        proberCov = rawCovFiles["proberCov"]
        for tool in rawCovFiles.keys():
            if (tool in ["carverCov", "proberCov", "uiCov", "uiTestCov", "stProberCov"]) or (tool not in TOOLS):
                print("Skipping merge {} : {}".format(appName, tool))
                continue
            for rawCov in rawCovFiles[tool]:
                stCov = rawCov
                carverMerge = carverCov[0]
                proberMerge = proberCov[0]
                status = generateCombinedCoverage(stCov=stCov, toMerge=carverMerge, mergedFileName="carver", classFolder=classFiles)
                if status == constants.STATUS_SKIPPED:
                    skipped.append({"st": stCov, "toMerge": "carver"})
                elif status == constants.STATUS_ERRORED:
                    errored.append({"st": stCov, "toMerge": "carver"})
                elif status == constants.STATUS_SUCCESSFUL:
                    succeeded.append({"st": stCov, "toMerge": "carver"})

                status = generateCombinedCoverage(stCov=stCov, toMerge=proberMerge, mergedFileName="prober", classFolder=classFiles)
                if status == constants.STATUS_SKIPPED:
                    skipped.append({"st": stCov, "toMerge": "prober"})
                elif status == constants.STATUS_ERRORED:
                    errored.append({"st": stCov, "toMerge": "prober"})
                elif status == constants.STATUS_SUCCESSFUL:
                    succeeded.append({"st": stCov, "toMerge": "prober"})

    if appName == "booker":
        modules = constants.BOOKER_MODULES
        for module in modules:
            classFiles = os.path.abspath(os.path.join("..", "src", "main", "resources", "webapps", appName, "target", module))
            carverCov = os.path.join(rawCovFiles["carverCov"][0], module + ".exec")
            proberCov = os.path.join(rawCovFiles["proberCov"][0], module + ".exec")
            for tool in rawCovFiles.keys():
                if (tool in ["carverCov", "proberCov", "uiCov", "uiTestCov", "stProberCov"]) or (tool not in TOOLS):
                    continue
                for rawCov in rawCovFiles[tool]:
                    stCov = os.path.join(rawCov, module + ".exec")
                    carverMerge = carverCov
                    proberMerge = proberCov


                    status = generateCombinedCoverage(stCov=stCov, toMerge=carverMerge, mergedFileName="carver", classFolder=classFiles)
                    if status == constants.STATUS_SKIPPED:
                        skipped.append({"st": stCov, "toMerge": "carver"})
                    elif status == constants.STATUS_ERRORED:
                        errored.append({"st": stCov, "toMerge": "carver"})
                    elif status == constants.STATUS_SUCCESSFUL:
                        succeeded.append({"st": stCov, "toMerge": "carver"})

                    status = generateCombinedCoverage(stCov=stCov, toMerge=proberMerge, mergedFileName="prober", classFolder=classFiles)
                    if status == constants.STATUS_SKIPPED:
                        skipped.append({"st": stCov, "toMerge": "prober"})
                    elif status == constants.STATUS_ERRORED:
                        errored.append({"st": stCov, "toMerge": "prober"})
                    elif status == constants.STATUS_SUCCESSFUL:
                        succeeded.append({"st": stCov, "toMerge": "prober"})
    print(succeeded)
    print(skipped)
    print(errored)
    print("succeeded {}, errored {}, skipped {}".format(len(succeeded), len(errored), len(skipped)))
    return succeeded, errored, skipped

def combineCovTest():
    stCov = "/TestCarving/testCarver/out/ecomm/schemathesis/0/cov/cov.exec"
    toMerge = "/TestCarving/testCarver/out/ecomm/20220824_195922/oas/20220824_200840/allPr_cov_20220824_201447/cov.exec"
    mergedFileName = "Prober"
    classFiles = "/TestCarving/testCarver/src/main/resources/webapps/ecomm/target/classes"
    generateCombinedCoverage(stCov=stCov, toMerge=toMerge, mergedFileName=mergedFileName, classFolder=classFiles)


ALL_TOOLS = ["uiCov", "uiTestCov", "carverCov", "proberCov", "stCov",
             "stCarverMerge", "stProberMerge", "emCov", "emCarverMerge", "emProberMerge", "stCarver", "stProber"]

if __name__ == "__main__":
    # parseJacocoResults("coverage/cov/cov.xml")
    # results = getAppResults("booker")
    # print(results)
    # parseNYCTests()
    # mergeCovFilesTests()

    # combineCovTest()

    # generateCombinedCoverageForApp("petclinic", TOOLS=["emCov"])
    # generateCombinedCoverageForApp("parabank", TOOLS=["emCov"])
    # generateCombinedCoverageForApp("booker", TOOLS=["emCov"])
    # generateCombinedCoverageForApp("medical", TOOLS=["emCov"])
    # generateCombinedCoverageForApp("ecomm", TOOLS=["emCov"])
    # # generateCombinedCoverageForApp("realworld", TOOLS=["emCov"])
    # generateCombinedCoverageForApp("jawa", TOOLS=["emCov"])
    # generateCombinedCoverageForApp("medical")
    #
    totalResults = getAllResults()
    print(totalResults)
    totalResults = [result for result in totalResults if 'status' not in result.keys()]
    # utilsRun.exportJson(jsonData=totalResults, file="../results/cov_"+datetime.datetime.now().strftime("%Y%m%d-%H%M%S")+".json")
    utilsRun.writeCSV_Dict(totalResults[0].keys(), csvRows=totalResults,dst="../results/cov_"+datetime.datetime.now().strftime("%Y%m%d-%H%M%S")+".csv")
