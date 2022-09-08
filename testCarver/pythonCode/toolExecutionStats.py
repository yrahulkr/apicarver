import glob
import os

from constants import APPS
from plotCoverage import getAppData
from runCarver import getExistingCarverRun
from utilsRun import importJson, writeCSV, writeCSV_Dict
from urllib.parse import urlparse

def findResultResponses():
    allOutputs = {}
    for appName in APPS:
        carverOutput = None
        outputs = {}
        allOutputs[appName] = outputs
        try:
            existingCarverData = getExistingCarverRun(appName)
            carverOutput, fileName = os.path.split(existingCarverData['existingValidCrawls'][0])
            outputs["carver"] = carverOutput
            outputs["success"] =  True
        except Exception as ex:
            outputs["success"] =  False
            outputs["message"] =  "error finding outputs"
            print(ex)
            continue

        if carverOutput is None:
            outputs["success"] =  False
            outputs["message"] =  "error finding outputs"
            print("carver output not found for {}".format(appName))
            continue

        try:
            outputs["recordedRequests"] = glob.glob(carverOutput+ "/gen/*/combined_generatedEvents.json")[0]
            outputs["filteredRequests"] = glob.glob(carverOutput+ "/gen/*/generatedEvents.json")[0]
            outputs["carverResultFile"] = glob.glob(carverOutput+ "/run/*/resultResponses.json")[0]
            outputs["proberResultFile"] = glob.glob(carverOutput+ "/oas/*/resultResponses.json")[0]
            executedProbes = glob.glob(carverOutput+ "/oas/*/crawlEventsLog.json")
            if len(executedProbes) == 0:
                executedProbes = glob.glob(carverOutput+ "/oas/*/executedprobeEvents.json")

            outputs["executedProbes"] = executedProbes[0]

        except Exception as ex:
            print("Exception getting output files inside carver output directory")
            print(ex)

    return allOutputs

def parseAPIResults(resultsJson):
    succeeded = 0
    failed = 0
    status2xx = 0
    status3xx = 0
    status4xx = 0
    status5xx = 0
    statusOther = 0
    triedUrls = []
    succeededUrls = []
    executionTime = 0
    for result in resultsJson:
        executionTime += result['duration']
        try:
            url = urlparse(result['request']['requestUrl'])
            path = url.path
            if path not in triedUrls:
                triedUrls.append(path)
            status = result['response']['status']
            if status >=200 and status <300:
                status2xx += 1
                if path not in succeededUrls:
                    succeededUrls.append(path)
            elif status >=300 and status <400:
                status3xx +=1
                if path not in succeededUrls:
                    succeededUrls.append(path)
            elif status >=400 and status <500:
                status4xx +=1
            elif status >=500 and status <600:
                status5xx += 1
                if path not in succeededUrls:
                    succeededUrls.append(path)
            else:
                statusOther += 1
            succeeded += 1
        except:
            failed += 1

    return {"succeeded": succeeded, "failed": failed,
            "status2xx": status2xx, "status3xx":status3xx, "status4xx": status4xx, "status5xx": status5xx, "statusOther": statusOther, "goodStatus": (status2xx + status3xx + status5xx + statusOther),
            "triedUrls": len(triedUrls), "succeededUrls": len(succeededUrls),
            "duration": executionTime}


def parseProbeEvents(probeEvents, proberResults):
    I2L = []
    MD2BiP = []
    RA = []
    MOP = []
    other = []
    unknown = []
    total = []
    succeeded = []
    opCheckPoints = 0
    cookieCheckPoints = 0
    bothCheckPoints = 0

    for proberResult in proberResults:
        if proberResult['request']['clazz'] == 'Probe' and proberResult['status'] == 'SUCCESS' and not (proberResult['response']['status'] >=400 and proberResult['response']['status'] <500):
            succeeded.append(proberResult['request']['requestId'])
        if proberResult['checkPoint'] == "OPERATION":
            opCheckPoints += 1
        elif proberResult['checkPoint'] == 'COOKIE':
            cookieCheckPoints += 1
        elif proberResult['checkPoint'] == 'BOTH':
            bothCheckPoints += 1

    for probeEvent in probeEvents:

        total.append(probeEvent['requestId'])
        if "probeType" not in probeEvent:
            print(probeEvent)
            unknown.append(probeEvent['requestId'])
            continue
        probeType = probeEvent["probeType"]
        if probeType == "MOP":
            MOP.append(probeEvent['requestId'])
        elif probeType == "RA":
            RA.append(probeEvent['requestId'])
        elif probeType == "MDI2L":
            I2L.append(probeEvent['requestId'])
        else:
            other.append(probeEvent['requestId'])

    return {"exec_I2L": len(I2L), "exec_RA": len(RA), "exec_MOP": len(MOP), "exec_other": len(other), "exec_BiP": len(unknown), "exec_total": len(probeEvents), "exec_succeededProbes": len(succeeded),
            "gen_I2L": len(set(I2L)), "gen_RA": len(set(RA)), "gen_MOP": len(set(MOP)), "gen_other": len(set(other)), "gen_BiP": len(set(unknown)),"gen_succeededProbes": len(set(succeeded)), "gen_total": len(set(total)),
            "checkPoints_operation": opCheckPoints, "checkPoints_cookie": cookieCheckPoints, "checkPoints_both": bothCheckPoints}


def getToolExecutionData(allResults):
    allOutputs = findResultResponses()
    for key in allOutputs:
        print(key)
        recordedRequests = len(importJson(allOutputs[key]["recordedRequests"]))
        filteredRequests = len(importJson(allOutputs[key]["filteredRequests"]))
        filteringResults = {"recorded": recordedRequests, "filtered": filteredRequests}
        filteringResults['app'] = key
        carverResults = parseAPIResults(importJson(allOutputs[key]["carverResultFile"]))
        carverResults["app"] = key
        carverResults["tool"] = "carver"
        proberResults = parseAPIResults(importJson(allOutputs[key]["proberResultFile"]))
        proberResults["tool"] = "prober"
        proberResults["app"] = key

        probingResults = parseProbeEvents(importJson(allOutputs[key]["executedProbes"]), importJson(allOutputs[key]["proberResultFile"]))
        probingResults["app"] = key
        appResult = {"app": key, "filtering": filteringResults, "carver": carverResults, "prober": proberResults, "probingResults": probingResults}
        allResults['filtering'].append(filteringResults)
        allResults['execution'].append(carverResults)
        allResults['execution'].append(proberResults)
        allResults['probing'].append(probingResults)
    return allResults

def estParseProbes():
    probeEvents = importJson("/TestCarving/testCarver/out/ecomm/20220827_151530/oas/20220827_155323/executedprobeEvents.json")
    probingOutput = parseProbeEvents(probeEvents=probeEvents)
    print(probingOutput)

if __name__=="__main__":
    # allOutputs = findResultResponses()
    # print(allOutputs)
    # estParseProbes()
    allResults = {'filtering': [], 'execution': [], 'probing': []}
    results = getToolExecutionData(allResults)
    print(results)
    writeCSV_Dict(csvFields=allResults['filtering'][0].keys(), csvRows=allResults['filtering'], dst="../results/filtering.csv")
    writeCSV_Dict(csvFields=allResults['execution'][0].keys(), csvRows=allResults['execution'], dst="../results/execution.csv")
    writeCSV_Dict(csvFields=allResults['probing'][0].keys(), csvRows=allResults['probing'], dst="../results/probing.csv")



