import glob
import os.path
from datetime import datetime
from enum import Enum

import constants
import utilsRun
from constants import APPS, STATUS_ERRORED, STATUS_SUCCESSFUL, STATUS_SKIPPED, RUN_RESTATS_COMMAND, \
    RESULT_RESPONSES_JSON, SCHEMATHESIS_OUTPUT, CASETTE_YAML, PROBER_RESPONSES_JSON, INFERRED_YAML, PROBER_YAML, \
    RESTATS_PATH, INFERRED_JSON, PROBER_JSON
from utilsRun import monitorProcess, startProcess, restartDocker


class RUN_MODE(Enum):
    merge = "merge"
    carver = "carver"
    schemathesis = "schemathesis"
    specCompare = "specCompare"


def buildRestatsConfig(run_mode, groundTruthYaml, inferredYaml=None, inferredJson=None, carverRecords=None, stRecords=None, mergeName=None):
    if (groundTruthYaml is None) or (not os.path.exists(groundTruthYaml)):
        print("Error. Provide a valid groundtruth openapi yaml")
        return None

    confDict = {}

    confDict["specification"] = os.path.abspath(groundTruthYaml)

    if run_mode == RUN_MODE.carver:
        if (carverRecords is None) or (not os.path.exists(carverRecords)):
            print("Error. Provide a valid resultResponses for carver")
            return None
        confDict["modules"] = "carver"
        confDict["results"] = os.path.abspath(carverRecords)
        # carverDir = os.path.pathsep.join(os.path.split(carverRecords)[0:len(os.path.split(carverRecords)) - 1])
        reportsDir = os.path.splitext(carverRecords)[0] + "_reports"
        confDict["reportsDir"] = reportsDir
        if not os.path.exists(reportsDir):
            os.makedirs(reportsDir)
        confDict["dbPath"] = os.path.splitext(carverRecords)[0] + ".sqlite"

    elif run_mode == RUN_MODE.specCompare:
        # if (inferredYaml is None) or (not os.path.exists(inferredYaml)):
        #     print("Error. Provide a valid yaml for spec comparison")
        #     return None
        if inferredYaml is not None:
            confDict["modules"] = "specCompare"
            confDict["inferred"] = os.path.abspath(inferredYaml)
            # inferredDir = os.path.pathsep.join(os.path.split(inferredYaml)[0:len(os.path.split(inferredYaml)) - 1])
            reportsDir = os.path.splitext(inferredYaml)[0] + "_reports"
            confDict["specReports"] = reportsDir
            if not os.path.exists(reportsDir):
                os.makedirs(reportsDir)
            confDict["specDbPath"] = os.path.splitext(inferredYaml)[0] + ".sqlite"
        if inferredJson  is not None:
            confDict["modules"] = "specCompare"
            confDict["inferred"] = os.path.abspath(inferredJson)
            # inferredDir = os.path.pathsep.join(os.path.split(inferredYaml)[0:len(os.path.split(inferredYaml)) - 1])
            reportsDir = os.path.splitext(inferredJson)[0] + "_json_reports"
            confDict["specReports"] = reportsDir
            if not os.path.exists(reportsDir):
                os.makedirs(reportsDir)
            confDict["specDbPath"] = os.path.splitext(inferredJson)[0] + "_json.sqlite"

    elif run_mode == RUN_MODE.schemathesis:
        if (stRecords is None) or (not os.path.exists(stRecords)):
            print("Error. Provide a valid Cassette for schemathesis")
            return None
        confDict["modules"] = "schemathesis"
        confDict["cassette"] = stRecords
        confDict["cassetteReports"] = os.path.abspath(stRecords)
        # cassetteDir = os.path.pathsep.join(os.path.split(stRecords)[0:len(os.path.split(stRecords)) - 1])
        reportsDir = os.path.splitext(stRecords)[0] + "_reports"
        confDict["cassetteReports"] = reportsDir
        if not os.path.exists(reportsDir):
            os.makedirs(reportsDir)
        confDict["cassetteDbPath"] = os.path.splitext(stRecords)[0] + ".sqlite"

    elif run_mode == RUN_MODE.merge:
        if (stRecords is None) or (not os.path.exists(stRecords)):
            print("Error. Provide a valid Cassette for schemathesis")
            return None
        if (carverRecords is None) or (not os.path.exists(carverRecords)):
            print("Error. Provide a valid resultResponses for carver")
            return None
        if mergeName is None:
            print("Error. Provide a valid merge name")
            return None
        confDict["modules"] = "merge"
        confDict["cassette"] = stRecords
        confDict["results"] = carverRecords
        reportsDir = os.path.splitext(stRecords)[0] + mergeName + "_reports"
        confDict["mergeReports"] = reportsDir
        if not os.path.exists(reportsDir):
            os.makedirs(reportsDir)
        confDict["mergeDb"] = os.path.splitext(stRecords)[0] + mergeName + ".sqlite"

    # confDict = {
    #     "modules": "specCompare",
    #     "specification": "petclinic2/openapi.json",
    #     "reportsDir": "petclinic2/reports",
    #     "cassetteReports": "petclinic2/reports_cassette",
    #     "dbPath": "petclinic2/database.sqlite",
    #     "cassetteDbPath": "petclinic2/database_cassette.sqlite",
    #     "results": "petclinic2/probe_resultResponses.json",
    #     "inferred": "petclinic2/probe_oas.yaml",
    #     "specDbPath": "petclinic2/database_spec.sqlite",
    #     "specReports": "petclinic2/reports_spec",
    #     "cassette": "petclinic2/casette.yaml"
    # }

    return confDict


def runAllApps(TOOLS=[], RUNTIME=30):
    if len(TOOLS) == 0:
        TOOLS = ALL_TOOLS
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

        results = runAlgo(app, TOOLS)
        for result in results:
            if result["status"] == STATUS_SUCCESSFUL:
                succesful.append(result["command"])
            elif result["status"] == STATUS_SKIPPED:
                skipped.append(result["command"])
            elif result["status"] == STATUS_ERRORED:
                unsuccesful.append(result["command"])

    print("succesful : {0}".format(str(len(succesful))))
    print(succesful)
    print("skipped : {0}".format(str(len(skipped))))
    print(skipped)
    print("unsuccesful  : {0}".format(str(len(unsuccesful))))
    print(unsuccesful)
    if DRY_RUN:
        print("Predicted run time : " + str(RUNTIME * len(succesful)))


def getExistingOutput(appName, ALL_CRAWLS=os.path.join(os.path.abspath(".."), "out")):
    appOutput = os.path.abspath(os.path.join(ALL_CRAWLS, appName))
    if not os.path.exists(appOutput):
        print("no output folder for {}".format(appName))
        return None
    uiRecords = glob.glob(appOutput + "/*/" + RESULT_RESPONSES_JSON)
    carverRecords = glob.glob(appOutput + "/*/run/*/" + RESULT_RESPONSES_JSON)
    proberRecords = glob.glob(appOutput + "/*/oas/*/" + PROBER_RESPONSES_JSON)
    inferredYaml = glob.glob(appOutput + "/*/oas/*/" + INFERRED_YAML)
    proberYaml = glob.glob(appOutput + "/*/oas/*/" + PROBER_YAML)
    inferredJson = glob.glob(appOutput + "/*/oas/*/" + INFERRED_JSON)
    proberJson = glob.glob(appOutput + "/*/oas/*/" + PROBER_JSON)
    stOutput = glob.glob(appOutput + "/" + SCHEMATHESIS_OUTPUT + "/*/" + CASETTE_YAML)
    carverMerge = glob.glob(appOutput + "/" + SCHEMATHESIS_OUTPUT + "/*/" + CASETTE_YAML)
    proberMerge = glob.glob(appOutput + "/" + SCHEMATHESIS_OUTPUT + "/*/" + CASETTE_YAML)
    stCarver = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_CARVER + "/*/" + CASETTE_YAML)
    stProber = glob.glob(appOutput + "/" + constants.SCHEMATHESIS_PROBER + "/*/" + CASETTE_YAML)

    return {
        "uiRecords": uiRecords,
        "carverRecords": carverRecords,
        "proberRecords": proberRecords,
        "inferredYaml": inferredYaml,
        "proberYaml": proberYaml,
        "inferredJson": inferredJson,
        "proberJson": proberJson,
        "stOutput": stOutput,
        "carverMerge": carverMerge,
        "proberMerge": proberMerge,
        "stCarver": stCarver,
        "stProber": stProber
    }


def runAlgo(appName, tools=[]):
    if len(tools) == 0:
        tools = ALL_TOOLS
    results = []
    existingOutput = getExistingOutput(appName)
    print(existingOutput)
    if existingOutput is None:
        print("Ignoring run because no output exists.")
        status = STATUS_SKIPPED
        return status, appName

    srcPath = os.path.join("..", "src", "main", "resources", "webapps", appName)
    # openApiPath = os.path.join(srcPath, "openapi.yml")
    openApiPath = os.path.join(srcPath, "openapi.json")

    # Carver Results
    try:
        carverRecords = existingOutput['carverRecords'][0]
    except Exception as ex:
        carverRecords = None
        print(ex)

    if carverRecords is not None and ("carverRecords" in tools):
        config = buildRestatsConfig(run_mode=RUN_MODE.carver, groundTruthYaml=openApiPath,
                                    carverRecords=carverRecords)
        confPath = os.path.splitext(carverRecords)[0] + "_conf.json"
        utilsRun.exportJson(confPath, config)
        command = RUN_RESTATS_COMMAND.copy()
        command.append(confPath)
        logFile = os.path.abspath(
            os.path.join("../logs", "carverResults_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log"))
        print("sending command {0}".format(command))
        proc = startProcess(command, logFile, changeDir=RESTATS_PATH)
        if proc == None:
            print("Ignoring error command.")
            status = STATUS_ERRORED
            results.append({"name": "carverResults", "status": status, "command": command})

        timeout = 200
        status = monitorProcess(proc, runtime=30, timeStep=10)
        print("Done : {0}".format(command))

        results.append({"name": "carverResults", "status": status, "command": command})

    # Prober Carver Results
    try:
        proberRecords = existingOutput['proberRecords'][0]
    except Exception as ex:
        proberRecords = None
        print(ex)

    if proberRecords is not None and ("proberRecords" in tools):
        config = buildRestatsConfig(run_mode=RUN_MODE.carver, groundTruthYaml=openApiPath,
                                    carverRecords=proberRecords)
        confPath = os.path.splitext(proberRecords)[0] + "_conf.json"
        utilsRun.exportJson(confPath, config)
        command = RUN_RESTATS_COMMAND.copy()
        command.append(confPath)

        logFile = os.path.abspath(
            os.path.join("../logs", "proberResults_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log"))
        print("sending command {0}".format(command))
        proc = startProcess(command, logFile, changeDir=RESTATS_PATH)
        if proc == None:
            print("Ignoring error command.")
            status = STATUS_ERRORED
            results.append({"name": "proberResults", "status": status, "command": command})

        timeout = 200
        status = monitorProcess(proc, runtime=30, timeStep=10)
        print("Done : {0}".format(command))

        results.append({"name": "proberResults", "status": status, "command": command})

    # InferredYaml Results
    try:
        inferredYaml = existingOutput['inferredYaml'][0]
    except Exception as ex:
        inferredYaml = None
        print(ex)

    if inferredYaml is not None and ("inferredYaml" in tools):
        config = buildRestatsConfig(run_mode=RUN_MODE.specCompare, groundTruthYaml=openApiPath,
                                    inferredYaml=inferredYaml)
        confPath = os.path.splitext(inferredYaml)[0] + "_conf.json"
        utilsRun.exportJson(confPath, config)
        command = RUN_RESTATS_COMMAND.copy()
        command.append(confPath)

        logFile = os.path.abspath(
            os.path.join("../logs", "inferredYaml_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log"))
        print("sending command {0}".format(command))
        proc = startProcess(command, logFile, changeDir=RESTATS_PATH)
        if proc == None:
            print("Ignoring error command.")
            status = STATUS_ERRORED
            results.append({"name": "inferredYaml", "status": status, "command": command})

        timeout = 200
        status = monitorProcess(proc, runtime=30, timeStep=1)
        print("Done : {0}".format(command))

        results.append({"name": "inferredYaml", "status": status, "command": command})

    # Inferred Json Results
    try:
        inferredJson = existingOutput['inferredJson'][0]
    except Exception as ex:
        inferredJson = None
        print(ex)

    if inferredJson is not None and ("inferredJson" in tools):
        config = buildRestatsConfig(run_mode=RUN_MODE.specCompare, groundTruthYaml=openApiPath,
                                    inferredJson=inferredJson)
        confPath = os.path.splitext(inferredJson)[0] + "_json_conf.json"
        utilsRun.exportJson(confPath, config)
        command = RUN_RESTATS_COMMAND.copy()
        command.append(confPath)

        logFile = os.path.abspath(
            os.path.join("../logs", "inferredJson_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log"))
        print("sending command {0}".format(command))
        proc = startProcess(command, logFile, changeDir=RESTATS_PATH)
        if proc == None:
            print("Ignoring error command.")
            status = STATUS_ERRORED
            results.append({"name": "inferredJson", "status": status, "command": command})

        timeout = 200
        status = monitorProcess(proc, runtime=30, timeStep=1)
        print("Done : {0}".format(command))

        results.append({"name": "inferredJson", "status": status, "command": command})

    # Prober Json Results
    try:
        proberJson = existingOutput['proberJson'][0]
    except Exception as ex:
        proberJson = None
        print(ex)

    if proberJson is not None and ("proberJson" in tools):
        config = buildRestatsConfig(run_mode=RUN_MODE.specCompare, groundTruthYaml=openApiPath,
                                    inferredJson=proberJson)
        confPath = os.path.splitext(proberJson)[0] + "_json_conf.json"
        utilsRun.exportJson(confPath, config)
        command = RUN_RESTATS_COMMAND.copy()
        command.append(confPath)

        logFile = os.path.abspath(
            os.path.join("../logs", "proberJson_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log"))
        print("sending command {0}".format(command))
        proc = startProcess(command, logFile, changeDir=RESTATS_PATH)
        if proc == None:
            print("Ignoring error command.")
            status = STATUS_ERRORED
            results.append({"name": "proberJson", "status": status, "command": command})

        timeout = 200
        status = monitorProcess(proc, runtime=30, timeStep=1)
        print("Done : {0}".format(command))

        results.append({"name": "proberJson", "status": status, "command": command})

    # ProberYaml Results
    try:
        proberYaml = existingOutput['proberYaml'][0]
    except Exception as ex:
        proberYaml = None
        print(ex)

    if proberYaml is not None and ("proberYaml" in tools):
        config = buildRestatsConfig(run_mode=RUN_MODE.specCompare, groundTruthYaml=openApiPath,
                                    inferredYaml=proberYaml)
        confPath = os.path.splitext(proberYaml)[0] + "_conf.json"
        utilsRun.exportJson(confPath, config)
        command = RUN_RESTATS_COMMAND.copy()
        command.append(confPath)

        logFile = os.path.abspath(
            os.path.join("../logs", "proberYaml_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log"))
        print("sending command {0}".format(command))
        proc = startProcess(command, logFile, changeDir=RESTATS_PATH)
        if proc == None:
            print("Ignoring error command.")
            status = STATUS_ERRORED
            results.append({"name": "proberYaml", "status": status, "command": command})

        timeout = 200
        status = monitorProcess(proc, runtime=30, timeStep=1)
        print("Done : {0}".format(command))

        results.append({"name": "proberYaml", "status": status, "command": command})

    # schemathesis Results

    try:
        stOutputEntries = existingOutput['stOutput']
    except Exception as ex:
        stOutputEntries = None
        print(ex)

    if stOutputEntries is not None and ("stOutput" in tools):
        for runIndex in range(len(stOutputEntries)):
            stOutput = stOutputEntries[runIndex]
            config = buildRestatsConfig(run_mode=RUN_MODE.schemathesis, groundTruthYaml=openApiPath,
                                        stRecords=stOutput)
            confPath = os.path.splitext(stOutput)[0] + "_conf.json"
            utilsRun.exportJson(confPath, config)
            command = RUN_RESTATS_COMMAND.copy()
            command.append(confPath)

            logFile = os.path.abspath(
                os.path.join("../logs", "stOutput_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log"))
            print("sending command {0}".format(command))
            proc = startProcess(command, logFile, changeDir=RESTATS_PATH)
            if proc == None:
                print("Ignoring error command.")
                status = STATUS_ERRORED
                results.append({"name": "stOutput"+str(runIndex), "status": status, "command": command})

            status = monitorProcess(proc, runtime=30, timeStep=10)
            print("Done : {0}".format(command))
            results.append({"name": "stOutput"+str(runIndex), "status": status, "command": command})

    try:
        carverMergeEntries = existingOutput['carverMerge']
        carverRecords=existingOutput['carverRecords'][0]
    except Exception as ex:
        carverMergeEntries = None
        print(ex)

    if carverMergeEntries is not None and ("carverMerge" in tools) and (carverRecords is not None):
        for runIndex in range(len(carverMergeEntries)):
            carverMerge = carverMergeEntries[runIndex]
            mergeName = "carver"
            config = buildRestatsConfig(run_mode=RUN_MODE.merge, groundTruthYaml=openApiPath,
                                        stRecords=carverMerge, carverRecords=carverRecords, mergeName=mergeName)
            confPath = os.path.splitext(carverMerge)[0] + mergeName + "_conf.json"
            utilsRun.exportJson(confPath, config)
            command = RUN_RESTATS_COMMAND.copy()
            command.append(confPath)

            logFile = os.path.abspath(
                os.path.join("../logs", "carverMerge_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log"))
            print("sending command {0}".format(command))
            proc = startProcess(command, logFile, changeDir=RESTATS_PATH)
            if proc == None:
                print("Ignoring error command.")
                status = STATUS_ERRORED
                results.append({"name": "carverMerge"+str(runIndex), "status": status, "command": command})

            status = monitorProcess(proc, runtime=30, timeStep=10)
            print("Done : {0}".format(command))
            results.append({"name": "carverMerge"+str(runIndex), "status": status, "command": command})

    try:
        proberMergeEntries = existingOutput['proberMerge']
        proberRecords=existingOutput['proberRecords'][0]
    except Exception as ex:
        proberMergeEntries = None
        print(ex)

    if proberMergeEntries is not None and ("proberMerge" in tools) and (proberRecords is not None):
        for runIndex in range(len(proberMergeEntries)):
            proberMerge = proberMergeEntries[runIndex]
            mergeName = "prober"
            config = buildRestatsConfig(run_mode=RUN_MODE.merge, groundTruthYaml=openApiPath,
                                        stRecords=proberMerge, carverRecords=proberRecords, mergeName=mergeName)
            confPath = os.path.splitext(proberMerge)[0] + mergeName + "_conf.json"
            utilsRun.exportJson(confPath, config)
            command = RUN_RESTATS_COMMAND.copy()
            command.append(confPath)

            logFile = os.path.abspath(
                os.path.join("../logs", "proberMerge_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log"))
            print("sending command {0}".format(command))
            proc = startProcess(command, logFile, changeDir=RESTATS_PATH)
            if proc == None:
                print("Ignoring error command.")
                status = STATUS_ERRORED
                results.append({"name": "proberMerge"+str(runIndex), "status": status, "command": command})

            status = monitorProcess(proc, runtime=30, timeStep=10)
            print("Done : {0}".format(command))
            results.append({"name": "proberMerge"+str(runIndex), "status": status, "command": command})

    try:
        stCarverEntries = existingOutput['stCarver']
    except Exception as ex:
        stCarverEntries = None
        print(ex)

    if stCarverEntries is not None and ("stCarver" in tools):
        for runIndex in range(len(stCarverEntries)):
            stCarver = stCarverEntries[runIndex]
            config = buildRestatsConfig(run_mode=RUN_MODE.schemathesis, groundTruthYaml=openApiPath,
                                        stRecords=stCarver)
            confPath = os.path.splitext(stCarver)[0] + "_conf.json"
            utilsRun.exportJson(confPath, config)
            command = RUN_RESTATS_COMMAND.copy()
            command.append(confPath)

            logFile = os.path.abspath(
                os.path.join("../logs", "stCarver_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log"))
            print("sending command {0}".format(command))
            proc = startProcess(command, logFile, changeDir=RESTATS_PATH)
            if proc == None:
                print("Ignoring error command.")
                status = STATUS_ERRORED
                results.append({"name": "stCarver"+str(runIndex), "status": status, "command": command})

            status = monitorProcess(proc, runtime=30, timeStep=10)
            print("Done : {0}".format(command))
            results.append({"name": "stCarver"+str(runIndex), "status": status, "command": command})

    try:
        stProberEntries = existingOutput['stProber']
    except Exception as ex:
        stProberEntries = None
        print(ex)

    if stProberEntries is not None and ("stProber" in tools):
        for runIndex in range(len(stProberEntries)):
            stProber = stProberEntries[runIndex]
            config = buildRestatsConfig(run_mode=RUN_MODE.schemathesis, groundTruthYaml=openApiPath,
                                        stRecords=stProber)
            confPath = os.path.splitext(stProber)[0] + "_conf.json"
            utilsRun.exportJson(confPath, config)
            command = RUN_RESTATS_COMMAND.copy()
            command.append(confPath)

            logFile = os.path.abspath(
                os.path.join("../logs", "stProber_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log"))
            print("sending command {0}".format(command))
            proc = startProcess(command, logFile, changeDir=RESTATS_PATH)
            if proc == None:
                print("Ignoring error command.")
                status = STATUS_ERRORED
                results.append({"name": "stProber"+str(runIndex), "status": status, "command": command})

            status = monitorProcess(proc, runtime=30, timeStep=10)
            print("Done : {0}".format(command))
            results.append({"name": "stProber"+str(runIndex), "status": status, "command": command})

    return results


def getExistingTest():
    for app in APPS:
        print(getExistingOutput(app))

# APPS = ["ecomm"]
DRY_RUN = True
excludeApps = ['tmf', 'mdh']
ALL_TOOLS = ["uiRecords", "carverRecords", "proberRecords", "inferredYaml", "proberYaml","inferredJson", "proberJson",  "stOutput", "carverMerge", "proberMerge", "stCarver", "stProber"]

if __name__ == "__main__":
    print("hello")
    # getExistingTest()
    runAllApps(['inferredJson', 'proberJson'])
    #Can specify paritcular app and tool
        # ["uiRecords", "carverMerge", "proberMerge", "stOutput", "stCarver", "stProber"])
    # runAlgo("petclinic", ["inferredJson", "proberJson"])
    # runAlgo("parabank")
    # runAlgo("jawa", ["proberJson"])
