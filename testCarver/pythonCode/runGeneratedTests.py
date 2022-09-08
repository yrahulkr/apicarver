import glob
import os
from datetime import datetime, timedelta

import constants
from constants import APPS, STATUS_SUCCESSFUL, STATUS_ERRORED
from utilsRun import restartDocker, startProcess, monitorProcess, getDockerName, cleanup, MODE, exportJson

# BASE_COMMAND_HYBRID = ['sh', 'runTests.sh']
BASE_COMMAND = ['sh', 'runTests.sh']


# BASE_COMMAND=['java', '-jar', '/art-fork_icseBranch/crawljax/examples/target/crawljax-examples-3.7-SNAPSHOT-jar-with-dependencies.jar']



def executeTestsDummy(appName, algo, crawl, url=None,
				 logFile=os.path.join("logs", "testRunLog_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log"),
				 testResultsFolder=None):
	try:
		status = saveTestRunInfo(crawl=crawl, url=url,
							 dockerName=getDockerName(appName),
							 testResultsFolder=testResultsFolder,
							 version=APP_VERSION)
	except Exception as ex:
		print(ex)
		print("Exception saving test run info")
		status = False

def executeTests(appName, algo, crawl, url=None,
				 logFile=os.path.join("logs", "testRunLog_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log"),
				 testResultsFolder=None):

	command = BASE_COMMAND.copy()

	command.append(crawl)

	# if url is not None:
	# 	command.append(url)

	if appName in ["petclinic", "booker", "medical", "ecomm"]:
		command.append(appName)

	if DRY_RUN:
		status = STATUS_SUCCESSFUL
		return status, command

	restartDocker(appName)

	startTime = datetime.now()
	proc = startProcess(command, logFile, changeDir=None, DEBUG=False)
	if proc == None:
		print("Ignoring error command.")
		status = STATUS_ERRORED
		return status, command

	timeout = 200

	status = monitorProcess(proc, 6 * 60, timeStep=5)
	print("Done : {0}".format(command))
	endTime = datetime.now()

	testDuration = (endTime - startTime)/timedelta(milliseconds=1)

	try:
		status = saveTestRunInfo(crawl=crawl, url=url,
								 dockerName=getDockerName(appName),
								 testResultsFolder=testResultsFolder,
								 version=APP_VERSION, duration = testDuration)
	except Exception as ex:
		print(ex)
		print("Exception saving test run info")
		status = False


	cleanup(MODE.CARVER, appName=appName, outputDir=testResultsFolder)
	return status, command


def saveTestRunInfo(crawl,url, dockerName=None, testResultsFolder=None, version=None, duration = None):
	if version is None:
		version=APP_VERSION

	testRunInfo = {'version': version, 'url': url, 'docker':dockerName, 'duration': duration}
	testRunInfoFile = os.path.join(testResultsFolder, 'testRunInfo.json')

	if testResultsFolder == None:
		testResultsFolder = os.path.join(crawl, 'test-results', '0')
		print("Assuming test results folder {0}".format(testResultsFolder))

	if not os.path.exists(testResultsFolder):
		print("Test results folder not found {0}".format(testResultsFolder))
		print("Error: Test Run not successful!!")
		return False

	if os.path.exists(testRunInfoFile):
		print("Error: Test run file already exists at {0}".format(testRunInfo))
		return False
	else:
		print(testRunInfo)
		if not DRY_RUN:
			exportJson(testRunInfoFile, testRunInfo)
		return True

def getTestRun(crawl):
	returnList = []
	testResultsFolder = os.path.join(crawl, "test-results")
	if os.path.exists(testResultsFolder):
		testRunList = os.listdir(testResultsFolder)
		print("Found test runs {0}".format(testRunList))
		for testRun in testRunList:
			if testRun == '.DS_Store':
				continue
			returnList.append(os.path.join(testResultsFolder, testRun))
		return returnList
	return []

def runTests(crawl, rerun=False):
	split = os.path.split(os.path.split(os.path.split(crawl)[0])[0])
	appName = os.path.split(split[0])[1]
	runInfo = split[1]
	print(appName)
	print(runInfo)
	testRuns = getTestRun(crawl)
	if len(testRuns) > 0:
		if not rerun:
			return False
	else:

		status, command = executeTests(
			appName, "HYBRID", crawl,
			url=None,
			logFile=os.path.join(crawl, "testRun_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log"),
			testResultsFolder=os.path.join(crawl,'test-results', str(len(testRuns))))
		print(command)
		print(status)
		return True


def runAllTests(crawls, rerun=False):
	success = []
	skipped = []
	for crawl in crawls:
		status = runTests(crawl, rerun)
		if status:
			success.append(crawl)
		else:
			skipped.append(crawl)

	print("succeeded {0}: {1}".format(len(success), success))
	print("skipped {0}: {1}".format(len(skipped), skipped))
	return success, skipped


def getHostNames():
	return ["localhost"]


def getExistingCrawl(appName, algo, threshold, runtime, ALL_CRAWLS = os.path.join(os.path.abspath(".."), "out")):
	existingValidCrawls = []
	hostNames = getHostNames()
	for host in hostNames:
		crawlFolderName = appName + "_" + algo + "_" + str(float(threshold))+ "_" + str(runtime) + "mins"
		crawljaxOutputPath = os.path.abspath(os.path.join(ALL_CRAWLS, appName, crawlFolderName, host))
		if os.path.exists(crawljaxOutputPath):
			existingValidCrawls = glob.glob(crawljaxOutputPath + "/crawl*/result.json")
			return {"path": crawljaxOutputPath, "existingValidCrawls": existingValidCrawls}

	return {"path": None, "existingValidCrawls": existingValidCrawls}


def getCrawlsToAnalyze(crawlPath=None,app=None, host=None, runtime = 5, bestCrawls = False):
	if crawlPath==None:
		crawlPath = os.path.join(".","out")

	crawlMap = {}
	returnCrawls = []
	missingCrawls = []
	for appName in APPS:
		if app!=None and app!=appName:
			continue

		algoStr = "HYBRID"

		threshold = "-1.0"
		existingCrawlData = getExistingCrawl(appName, algoStr, threshold, runtime, ALL_CRAWLS = crawlPath)
		existingValidCrawls = existingCrawlData['existingValidCrawls']
		crawljaxOutputPath = existingCrawlData['path']
		print(existingCrawlData)

		if crawljaxOutputPath is None or len(existingValidCrawls) == 0:

			crawlFolderName = appName + "_" + algoStr + "_" + str(float(threshold))+ "_" + str(runtime) + "mins"
			crawljaxOutputPath = os.path.abspath(os.path.join(crawlPath, appName, crawlFolderName))
			missingCrawls.append(crawljaxOutputPath)

		for validCrawl in existingValidCrawls:
			if validCrawl not in returnCrawls:
				path,file = os.path.split(validCrawl)
				returnCrawls.append(path)
				crawlMap[path] = appName


	print(len(returnCrawls))
	return returnCrawls, crawlMap, missingCrawls

# APPS=["medical"]

DRY_RUN = False
APP_VERSION = -1
if __name__ == "__main__":
	# testCleanup()
	# testGetThresholds()
	# testRestartDocker()
	# testChangeDir()
	# testGetBestThresholds()
	returnCrawls, crawlMap, missingCrawls = getCrawlsToAnalyze(crawlPath="../crawlOut", app=None, host="localhost",
															   runtime=30, bestCrawls=True)
	print(returnCrawls)

	print(crawlMap)

	print("Missing")
	print(missingCrawls)

	# executeTestsDummy("petclinic", "HYBRID", "/TestCarving/crawlOut/petclinic/petclinic_HYBRID_-1.0_30mins/localhost/crawl0",
	# 				  None)
	runAllTests(returnCrawls, rerun=False)
	# addTestRunInfos(returnCrawls, app_version=APP_VERSION)
