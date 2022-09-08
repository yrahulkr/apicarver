import glob
import os
import shutil
from datetime import datetime

import constants
from constants import RUN_SCHEMATHESIS_COMMAND, APPS, STATUS_SUCCESSFUL, STATUS_SKIPPED, STATUS_ERRORED, CASETTE_YAML, \
    SCHEMATHESIS_OUTPUT
from utilsRun import monitorProcess, cleanup, startProcess, restartDocker, MODE


def runAllApps(RUNTIME=30):
    succesful = []
    unsuccesful = []
    skipped = []

    for app in APPS:
        if app in excludeApps:
            continue

        baseURL = constants.getHostURL(app)
        if baseURL is None:
            skipped.append(app)
            continue

        results = runAlgo(app, baseURL)

        for result in results:
            status = result["status"]
            command = result["command"]
            if status == STATUS_SUCCESSFUL:
                succesful.append(command)
            elif status == STATUS_SKIPPED:
                skipped.append(command)
            elif status == STATUS_ERRORED:
                unsuccesful.append(command)

    print("succesful : {0}".format(str(len(succesful))))
    print(succesful)
    print("skipped : {0}".format(str(len(skipped))))
    print(skipped)
    print("unsuccesful  : {0}".format(str(len(unsuccesful))))
    print(unsuccesful)
    if DRY_RUN:
        print("Predicted run time : " + str(RUNTIME * len(succesful)))


def getExistingRuns(appName, ALL_CRAWLS=os.path.join(os.path.abspath(".."), "out")):
    gtYaml = []
    crawljaxOutputPath = os.path.abspath(os.path.join(ALL_CRAWLS, appName))
    if os.path.exists(crawljaxOutputPath):
        gtYaml = glob.glob(crawljaxOutputPath + "/" + SCHEMATHESIS_OUTPUT + "/" + CASETTE_YAML)
        carverYaml = glob.glob(crawljaxOutputPath + "/" + constants.SCHEMATHESIS_CARVER + "/" + CASETTE_YAML)
        proberYaml = glob.glob(crawljaxOutputPath + "/" + constants.SCHEMATHESIS_PROBER + "/" + CASETTE_YAML)
    return {"path": crawljaxOutputPath, "existingValidCrawls": gtYaml}

    return {"path": None, "gtYaml": gtYaml, "carverYaml": carverYaml, "proberYaml": proberYaml}




def buildSchemathesisCommand(outputDir, openApiPath, baseURL):
    command = RUN_SCHEMATHESIS_COMMAND.copy()
    command.append("--cassette-path")
    if not os.path.exists(outputDir):
        os.makedirs(outputDir)
    command.append(os.path.join(outputDir, CASETTE_YAML))
    command.append(openApiPath)

    command.append("--base-url")
    command.append(baseURL)
    command.append('--hypothesis-max-examples')
    command.append('1000')
    return command


def getEnhancedYaml(appName):
    appOutput = os.path.abspath(os.path.join("../out", appName))
    if not os.path.exists(appOutput):
        print("no output folder for {}".format(appName))
        return None
    carverYaml = glob.glob(appOutput + "/*/run/*/" + constants.ENHANCED_YAML)
    proberYaml = glob.glob(appOutput + "/*/oas/*/" + constants.ENHANCED_YAML)
    return {"carverYaml": carverYaml, "proberYaml": proberYaml}


def runAlgo(appName, baseURL,
            logFile=os.path.join("../logs", "schemaThesis_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log"),
            rerun=False):
    results = []
    commands = []
    # For GroundTruth OpenAPI
    srcPath = os.path.join("..", "src", "main", "resources", "webapps", appName)
    openApiPath = os.path.join(srcPath, "openapi.yml")
    enhancedYaml = getEnhancedYaml(appName)

    for runIndex in range(1):
        outputDir = os.path.join("..", "out", appName, SCHEMATHESIS_OUTPUT, str(runIndex))
        command_gtYaml = buildSchemathesisCommand(outputDir=outputDir, openApiPath=openApiPath, baseURL=baseURL)
    
        if (not rerun) and os.path.exists(os.path.join(outputDir, constants.CASETTE_YAML)):
            # There is a previous execution and rerun is disabled
            results.append({"command": command_gtYaml, "status": STATUS_SKIPPED, "message": "previous execution data exists"})
        else:
            commands.append({"command":command_gtYaml, "outputDir":outputDir})
    
        if (enhancedYaml is not None) and len(enhancedYaml['carverYaml']) > 0:
            # For Carver Enhanced OpenAPI
            outputDir = os.path.join("..", "out", appName, constants.SCHEMATHESIS_CARVER, str(runIndex))
            openApiPath = enhancedYaml['carverYaml'][0]
            command_carverYaml = buildSchemathesisCommand(outputDir=outputDir, openApiPath=openApiPath, baseURL=baseURL)
            if (not rerun) and os.path.exists(os.path.join(outputDir, constants.CASETTE_YAML)):
                # There is a previous execution and rerun is disabled
                results.append({"command": command_carverYaml, "status": STATUS_SKIPPED, "message": "previous execution data exists"})
            else:
                commands.append({"command":command_carverYaml, "outputDir":outputDir})
    
        if (enhancedYaml is not None) and len(enhancedYaml['proberYaml']) > 0:
            # For Carver Enhanced OpenAPI
            outputDir = os.path.join("..", "out", appName, constants.SCHEMATHESIS_PROBER, str(runIndex))
            openApiPath = enhancedYaml['proberYaml'][0]
            command_proberYaml = buildSchemathesisCommand(outputDir=outputDir, openApiPath=openApiPath, baseURL=baseURL)
            if (not rerun) and os.path.exists(os.path.join(outputDir, constants.CASETTE_YAML)):
                # There is a previous execution and rerun is disabled
                results.append({"command": command_proberYaml, "status": STATUS_SKIPPED, "message": "previous execution data exists"})
            else:
                commands.append({"command":command_proberYaml, "outputDir":outputDir})

    for command in commands:
        if DRY_RUN:
            results.append({"command": command["command"], "status": STATUS_SUCCESSFUL, "message": "DRYRUN"})
            continue
        SLEEPTIME = 30
        if appName == "shopizer":
            SLEEPTIME = 120
        restartDocker(appName, SLEEPTIME)
        print("sending command {0}".format(command["command"]))
        proc = startProcess(command["command"], logFile, changeDir=None)
        if proc == None:
            print("Ignoring error command.")
            results.append({"command": command["command"], "status": STATUS_ERRORED, "message": "Command could not be executed"})
            continue
        status = monitorProcess(proc, timeStep=30)
        print("Done : {0}".format(command["command"]))

        cleanup(MODE.ST, appName, os.path.join(command["outputDir"], "cov"))
        results.append({"command": command["command"], "status": STATUS_SUCCESSFUL, "message": "Succesful"})


# if DRY_RUN:
    #     status = STATUS_SUCCESSFUL
    #     return results
    #
    # if isDockerized(appName):
    # 	restartDocker(getDockerName(appName))



    return results


def getExistingTest():
    for app in APPS:
        print(getExistingRuns(app))


DRY_RUN = Falser
excludeApps = ['tmf', 'mdh', 'shopizer']

if __name__ == "__main__":
    print("hello")
    # getExistingTest()
    runAllApps()
