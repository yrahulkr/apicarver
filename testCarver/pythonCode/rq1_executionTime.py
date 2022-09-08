import glob
import os.path
from datetime import datetime

import utilsRun
from constants import APPS
from coverageStats import getCovFiles
from runCarver import getExistingCarverRun
from runGeneratedTests import getCrawlsToAnalyze, getExistingCrawl
from utilsRun import importJson


def findAllOutputs(ALL_CRAWLS="../crawlOut"):
    allOutputs = {}
    for appName in APPS:
        try:
            # print(appName)
            algoStr = "HYBRID"
            threshold = "-1.0"
            existingCrawlData = getExistingCrawl(appName, algoStr, threshold, 30, ALL_CRAWLS = ALL_CRAWLS)
            existingValidCrawls = existingCrawlData['existingValidCrawls']
            crawljaxOutputPath = existingCrawlData['path']
            # print(existingValidCrawls[0])
            existingCarverData = getExistingCarverRun(appName)
            existingValidCarverOutputs = existingCarverData['existingValidCrawls']
            carverOutputPath = existingCarverData['path']
            # print(existingValidCarverOutputs[0])
            outputs = {"carver": existingValidCarverOutputs[0], "crawler": existingValidCrawls[0], "success": True}
        except Exception as ex:
            outputs = {"success": False, "message": "error finding outputs"}
            print(ex)
        allOutputs[appName] = outputs

    return allOutputs

def getExecutionTime(outputs):
    duration = {}
    validCrawl = outputs['crawler']
    validCarve = outputs['carver']
    try:
        crawlPath, file = os.path.split(validCrawl)
        testExecutionResultFile = glob.glob(crawlPath + "/test-results/0/testRunInfo.json")[0]
        executionData = importJson(jsonFile=testExecutionResultFile)
        executionTime = executionData['duration']
        # print("Crawler time {}".format(executionTime))
        # print("Crawler time {}".format(int(executionTime)))
        duration['crawler'] = int(executionTime)
    except Exception as ex:
        print(ex)
        print("Exception getting UI test execution data")
        duration['crawler'] = None
    try:
        carvePath, file = os.path.split(validCarve)
        carveResultFile = glob.glob(carvePath + "/run/*/resultResponses.json")[0]
        carverResults = importJson(carveResultFile)
        executionTime = 0
        for apiResult in carverResults:
            executionTime += apiResult['duration']
        # print("Carver time {}".format(executionTime))
        duration['carver'] = executionTime
    except Exception as ex:
        print(ex)
        print("Unable to find carver execution time")
        duration['carver'] = None
    return duration

def getCoverageData(app):
    print(getCovFiles(app))

if __name__ == "__main__":
    allOutputs = findAllOutputs()
    print(allOutputs)
    durations = []
    coverages = []
    for app in APPS:
        if allOutputs[app]['success']:
            duration = getExecutionTime(allOutputs[app])
            duration['app'] = app
            durations.append(duration)
        else:
            print("Cannot get results for {}".format(app))
            duration = {'app': app, 'result' : "error"}

    print(durations)
    utilsRun.writeCSV_Dict(durations[0].keys(), csvRows=durations,dst="../results/durations_"+datetime.now().strftime("%Y%m%d-%H%M%S")+".csv")
    # getCoverageData("petclinic")