import os
import shutil
from datetime import datetime
# from globalNames import FILTER, THRESHOLD_SETS, DB_SETS, APPS, isDockerized, DOCKER_LOCATION, isNd3App, getHostNames, \
#	ALGOS, getDockerName, getDockerList, getURLList
import glob

from constants import APPS, RUN_CARVER_COMMAND, STATUS_SUCCESSFUL, STATUS_SKIPPED, STATUS_ERRORED
from utilsRun import restartDocker, cleanup, monitorProcess, changeDirectory, startProcess


######################## REGRESSION UTILS ##################


######################## ######## ##################


def runAllApps(RUNTIME=30):
    succesful = []
    unsuccesful = []
    skipped = []

    for app in APPS:
        if app in excludeApps:
            continue
        status, command = runAlgo(app, RUNTIME)

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


def getExistingCarverRun(appName, ALL_CRAWLS=os.path.join(os.path.abspath(".."), "out")):
    existingValidCrawls = []
    crawljaxOutputPath = os.path.abspath(os.path.join(ALL_CRAWLS, appName))
    if os.path.exists(crawljaxOutputPath):
        existingValidCrawls = glob.glob(crawljaxOutputPath + "/*/uiTest_runResult.json")
        return {"path": crawljaxOutputPath, "existingValidCrawls": existingValidCrawls}

    return {"path": None, "existingValidCrawls": existingValidCrawls}


def runAlgo(appName, runtime,
            logFile=os.path.join("logs", "carverLog_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log"),
            rerun=False):
    command = RUN_CARVER_COMMAND.copy()

    command.append(appName)
    command.append(str(runtime))

    # host = "localhost"
    # if(isDockerized(appName)):
    # 	host = "192.168.99.101"

    existingCrawlData = getExistingCarverRun(appName)
    existingValidCrawls = existingCrawlData['existingValidCrawls']
    crawljaxOutputPath = existingCrawlData['path']

    if (not rerun):
        if crawljaxOutputPath is not None and os.path.exists(crawljaxOutputPath):
            if len(existingValidCrawls) == 0:
                # shutil.rmtree(crawljaxOutputPath)
                print("No existing output. Continuing to run")
            else:
                print("Ignoring run because a crawl already exists.")
                print("Call with rerun=True for creating a new crawl with the same configuration")
                status = STATUS_SKIPPED
                return status, command

    if DRY_RUN:
        status = STATUS_SUCCESSFUL
        return status, command
    #
    # if isDockerized(appName):
    # 	# restartDocker(appName)
    # 	restartDocker(getDockerName(appName))

    print("sending command {0}".format(command))
    proc = startProcess(command, logFile, changeDir="..")
    if proc == None:
        print("Ignoring error command.")
        status = STATUS_ERRORED
        return status, command

    timeout = 200

    status = monitorProcess(proc, runtime, timeout=timeout, crawljaxOutputPath=crawljaxOutputPath,
                            existing=len(existingValidCrawls))
    print("Done : {0}".format(command))

    cleanup(appName)

    return status, command


###########################################################################
##Tests ############
###########################################################################
def CleanupTest():
    cleanup()
    print("cleanup tested")


def RestartDockerTest():
    restartDocker("dimeshift")


def ChangeDirTest():
    current = os.getcwd();
    print(os.getcwd())
    changeDirectory("..")
    print(os.getcwd())
    changeDirectory(current)
    print(os.getcwd())


def GetExistingTest():
    for app in APPS:
        print(getExistingCarverRun(app))


###########################################################################
## Main Code ############
###########################################################################

DRY_RUN = False
excludeApps = ['tmf', 'mdh']

if __name__ == "__main__":
    print("hello")
    # testGetExisting()
    runAllApps(30)
