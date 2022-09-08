import json
import os
from urllib.parse import urlsplit, parse_qs

import ruamel.yaml

import constants
import runRestats

import matplotlib.pyplot as plt


def parsePostData(postData):
    params = {}
    if postData is None:
        print("Cannot parse None")
        return None
    if type(postData) is dict and "string" in postData.keys():
        postData = postData["string"]
    try:
        split = postData.split(sep="&")
        for splitItem in split:
            if splitItem is None or len(splitItem.strip()) == 0:
                continue

            paramItemSplit = splitItem.split(sep="=")

            if len(paramItemSplit) >= 2:
                name = paramItemSplit[0]
                value = "".join(paramItemSplit[1:])
            elif len(paramItemSplit) == 1:
                name = paramItemSplit[0]
                value = ''
            else:
                continue

            params[name] = value
    except:
        print("cannot parse {}".format(postData))

    return params



def yamlResponse2Dict(yamlResult):
    request = yamlResult["request"]
    response = yamlResult["response"]

    if 'body' in request:
        postData = parsePostData(request['body'])
    else:
        postData = {}

    parameters = []
    path = urlsplit(request['uri'])[2]

    queryParam = parse_qs(urlsplit(request["uri"])[3])

    # Add query parameters in the parameters dictionary
    for k, v in queryParam.items():
        parameters.append({'in': 'query', 'name': k, 'value': v[0]})

    for header in request["headers"]:
        parameters.append({'in': 'header', 'name': header, 'value': request["headers"][header]})

    requestDict = {
        'method': request["method"].lower(),
        'url': request["uri"],
        'version': "HTTP 1.0",
        'path': path,
        'parameters': parameters,
        'body': postData
    }

    if response is not None:
        status = response["status"]["code"]
        message = response["status"]["message"]
        responseParams = []
        for header in response["headers"]:
            parameters.append({'in': 'header', 'name': header, 'value': response["headers"][header]})
        # body = response["body"]
        body = ''

        responseDict = {
            'status': status,
            'message': message,
            'parameters': responseParams,
            'body': body
        }

    else:
        responseDict = {}
    return {"request": requestDict, "response":responseDict}

def addCassetteEntries(yamlResponses):
    requestResponses = []
    statusList = []
    for yamlResponse in yamlResponses:
        # print(yamlResponse)
        requestResponse = yamlResponse2Dict(yamlResponse)
        if requestResponse is not None:
            requestResponses.append(requestResponse)

    for requestResponse in requestResponses:
        request = requestResponse['request']
        response = requestResponse['response']
        if response is not None and "status" in response.keys():
            statusEntry = response["status"]
            if isinstance(statusEntry, str):
                status = int(statusEntry)
            elif type(statusEntry) is dict and "code" in statusEntry.keys():
                status = int(statusEntry["code"])
        statusList.append(status)
    print("Total" + str(len(statusList)))
    print("Error client (4xx) - " + str(len([status for status in statusList if status>=400 and status <500])))
    print("Success (2xx) - " + str(len([status for status in statusList if status>=200 and status <400])))
    print("Error server (5xx) - " + str(len([status for status in statusList if status>=500])))
    # print()
    return statusList


def getStatsListFromCassette(yamlResponsesPath):
    try:
        with open(yamlResponsesPath) as yamlFile:
            yaml = ruamel.yaml.YAML(typ='safe')
            data = yaml.load(yamlFile)
            yamlResponses = json.loads(json.dumps(data))
            statusList = addCassetteEntries(yamlResponses["http_interactions"])
            return statusList
    except Exception as ex:
        print(ex)
        print("Unable to parse yaml from {}".format(yamlResponsesPath))
        return None

def getAppData(appName):
    print(appName)
    statusDict = {}
    toolOutputs = runRestats.getExistingOutput(appName)
    stOutput = toolOutputs["stOutput"][0]
    if stOutput is not None:
        statusList = getStatsListFromCassette(stOutput)
        if statusList is not None:
            statusDict["stOutput"] = statusList

    stCarver = toolOutputs["stCarver"][0]
    if stCarver is not None:
        statusList = getStatsListFromCassette(stCarver)
        if statusList is not None:
            statusDict["stCarver"] = statusList

    stProber = toolOutputs["stProber"][0]
    if stProber is not None:
        statusList = getStatsListFromCassette(stProber)
        if statusList is not None:
            statusDict["stProber"] = statusList
    return statusDict


def getGraphData(statusList):
    graphData = []
    dataType = []
    y = 0
    for status in statusList:
        if status>=400 and status<500:
            graphData.append(y)
            dataType.append(0)
        elif status>=200 and status<400:
            y = y+1
            graphData.append(y)
            dataType.append(1)
        elif status>=500:
            y = y+1
            graphData.append(y)
            dataType.append(2)

    return {"y_data" : graphData, "type": dataType}

def plotApp(appName, ax):
    graphDataList = []
    statuses = getAppData(appName)
    print(statuses.keys())
    for tool in statuses.keys():
        statusList = statuses[tool]
        graphData = getGraphData(statusList)
        graphData["tool"] = tool
        graphDataList.append(graphData)
        markers_on = [i for i, j in enumerate(graphData["type"]) if j == 2]
        ax.plot(graphData["y_data"], '-D', markevery=markers_on, label=tool)

    ax.legend()

def exampleFigure():
    fig, ax = plt.subplots(nrows=1, ncols=len(constants.APPS), squeeze=False)
    fig.set_size_inches(20,4)
    for index in range(len(constants.APPS)):
        ax[0][index].plot(range(10), '-D', markevery=[2,4], label=str(index))
        ax[0][index].set_title(constants.APPS[index])

    plt.show()


def plotAllApps():
    fig, ax = plt.subplots(ncols=len(constants.APPS), nrows=1, squeeze=False)
    fig.set_size_inches(20,4)
    for index in range(len(constants.APPS)):
        plotApp(constants.APPS[index], ax[0][index])
        ax[0][index].set_title(constants.APPS[index])

    plt.show()

print('hello')

if __name__ == "__main__":
    # exampleFigure()
    plotAllApps()