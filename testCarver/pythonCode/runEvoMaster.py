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

        results = runAlgo(app, RUNTIME=RUNTIME)

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
        gtYaml = glob.glob(crawljaxOutputPath + "/" + constants.EVOMASTER_OUTPUT + "/" + CASETTE_YAML)
        return {"path": crawljaxOutputPath, "existingValidCrawls": gtYaml}

    return {"path": None, "gtYaml": gtYaml}

def getSwaggerUrl(appName):
    if appName == "petclinic":
        return "http://localhost:9966/petclinic/v3/api-docs"
    elif appName == "parabank":
        return "http://localhost:8080/parabank-3.0.0-SNAPSHOT/services/bank/swagger.yaml"
    elif appName == "realworld":
        return "http://localhost:3000/api"
    elif appName == "booker":
        return {"booking": "http://localhost:3000/booking/v3/api-docs/booking-api",
                "branding" : "http://localhost:3002/branding/v3/api-docs/branding-api",
                "message": "http://localhost:3006/message/v3/api-docs/message-api",
                "report": "http://localhost:3005/report/v3/api-docs/report-api",
                "room": "http://localhost:3001/room/v3/api-docs/room-api",
                "auth": "http://localhost:3004/auth/v3/api-docs/auth-api"
                }
    elif appName == "jawa":
        return "http://localhost:8080/v2/api-docs"
    elif appName == "ecomm":
        return "http://localhost:8080/api/v2/api-docs"
    elif appName == "medical":
        return "http://localhost:8080/v2/api-docs"
    elif appName == "shopizer":
        return "http://localhost:8080/v2/api-docs"

RUN_EVOMASTER_COMMAND = ['java', '-jar', './libs/evomaster.jar', '--blackBox', 'true']

def buildEvoMasterCommand(outputDir, baseURL, maxTime, targetURL=None):
    command = RUN_EVOMASTER_COMMAND.copy()
    command.append("--bbSwaggerUrl")
    command.append(baseURL)
    command.append('--outputFormat')
    command.append('JAVA_JUNIT_4')
    if not os.path.exists(outputDir):
        os.makedirs(outputDir)
    command.append('--outputFolder')
    command.append(outputDir)
    command.append('--maxTime')
    command.append(maxTime)
    if targetURL is not None:
        command.append('--bbTargetUrl')
        command.append(targetURL)
    return command


def runAlgo(appName, RUNTIME=60,
            logFile=os.path.join("../logs", "evomaster_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log"),
            rerun=False, EVOMASTER_OUTPUT="evomaster"):
    maxTime= str(RUNTIME) + "m"
    results = []
    commands = []
    # For GroundTruth OpenAPI
    srcPath = os.path.join("..", "src", "main", "resources", "webapps", appName)
    openApiPath = os.path.join(srcPath, "openapi.yml")

    for runIndex in range(1):
        curr_commands = []
        outputDir = os.path.join("..", "out", appName, EVOMASTER_OUTPUT, str(runIndex))
        if appName == "parabank":
            # No online swagger available
            command = buildEvoMasterCommand(outputDir=outputDir, baseURL=getSwaggerUrl(appName), maxTime=maxTime, targetURL=constants.getHostURL(appName))
            curr_commands.append(command)
        elif appName == "booker":
            baseURLs=getSwaggerUrl(appName)
            for key in baseURLs.keys():
                 curr_commands.append(buildEvoMasterCommand(outputDir=os.path.join(outputDir, key), baseURL=baseURLs[key], maxTime=str(round(RUNTIME/6)+1)+'m'))

        else:
            command = buildEvoMasterCommand(outputDir=outputDir, baseURL=getSwaggerUrl(appName), maxTime=maxTime)
            curr_commands.append(command)

        if (not rerun) and os.path.exists(os.path.join(outputDir, constants.CASETTE_YAML)):
            # There is a previous execution and rerun is disabled
            results.append({"command": curr_commands, "status": STATUS_SKIPPED, "message": "previous execution data exists"})
        else:
            commands.append({"command":curr_commands, "outputDir": outputDir})

        if not DRY_RUN:
            SLEEPTIME = 30
            if appName=="shopizer":
                SLEEPTIME= 120

            restartDocker(appName, SLEEPTIME)

        for command in curr_commands:
            if DRY_RUN:
                results.append({"command": command, "status": STATUS_SUCCESSFUL, "message": "DRYRUN"})
                continue

            print("sending command {0}".format(command))
            proc = startProcess(command, logFile, changeDir=None)
            if proc == None:
                print("Ignoring error command.")
                results.append({"command": command, "status": STATUS_ERRORED, "message": "Command could not be executed"})
                continue
            status = monitorProcess(proc, timeStep=30)
            print("Done : {0}".format(command))


            results.append({"command": command, "status": STATUS_SUCCESSFUL, "message": "Succesful"})

        if not DRY_RUN:
            cleanup(MODE.ST, appName, os.path.join(outputDir, "cov"))

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


DRY_RUN = False
excludeApps = ['tmf', 'mdh']

if __name__ == "__main__":
    print("hello")
    # getExistingTest()
    runAllApps(RUNTIME=2)
