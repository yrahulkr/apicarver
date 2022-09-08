import os
from pathlib import Path
import re
import json

from ruamel import yaml

import utils
import utils.parsers as parsers
import utils.dbmanager as dbm
import ruamel.yaml


def addSpecToDB(pair):
	#####################
	#### POPULATE DB ####

	pathID = dbm.getPathID(pair['request']['path'])
	method = pair['request']['method']

	# Add query/form parameters to the db
	for p in pair['request']['parameters']:
		paramID = dbm.getParameterID(pathID, method, p['name'])
		dbm.addParameterValue(paramID, p['value'])

	# # Add body parameters to the db
	# for p, v in pair['request']['data']['requestBody'].items():
	# 	paramID = dbm.getParameterID(pathID, method, p)
	# 	dbm.addParameterValue(paramID, v)

	# Sometimes some responses can be empty. Just avoid to add it to the db
	if pair['response'] != {}:

		# Add response parameters to the db
		isResponseAdded = False
		for p in pair['response']['parameters']:
			if p['name'] == 'Content-Type':
				dbm.addResponse(pathID, method, pair['response']['status'], p['value'])
				isResponseAdded = True
				break
		# Content type is not mandatory, even thought it should be in the message
		# If it is absent, it is assumed ad 'application/octet-stream'
		if not isResponseAdded:
			dbm.addResponse(pathID, method, pair['response']['status'], 'application/octet-stream')

def addPairToDB(pair):
	#####################
	#### POPULATE DB ####

	pathID = dbm.getPathID(pair['request']['path'])
	method = pair['request']['method']

	# Add query/form parameters to the db
	for p in pair['request']['parameters']:
		paramID = dbm.getParameterID(pathID, method, p['name'])
		dbm.addParameterValue(paramID, p['value'])

	# Add body parameters to the db
	for p, v in pair['request']['body'].items():
		paramID = dbm.getParameterID(pathID, method, p)
		dbm.addParameterValue(paramID, v)

	# Sometimes some responses can be empty. Just avoid to add it to the db
	if pair['response'] != {}:

		# Add response parameters to the db
		isResponseAdded = False
		for p in pair['response']['parameters']:
			if p['name'] == 'Content-Type':
				dbm.addResponse(pathID, method, pair['response']['status'], p['value'])
				isResponseAdded = True
				break
		# Content type is not mandatory, even thought it should be in the message
		# If it is absent, it is assumed ad 'application/octet-stream'
		if not isResponseAdded:
			dbm.addResponse(pathID, method, pair['response']['status'], 'application/octet-stream')


def addSourceEntries(source, paths_re, pathsInSpec):
	unmatched = []  # unmatched requests/responses
	matched = []
	prev_number = ''  # previous file number
	pair = {}  # pair map for easy json writing

	# Gets every entry in the directory, and keeps only files
	files_in_source = (entry for entry in Path(source).iterdir() if entry.is_file())
	files_in_source = sorted(files_in_source)

	for file in files_in_source:

		# print('pair number: ', prev_number)

		if prev_number == '':
			prev_number = file.name.split('-')[0]
			request = parsers.RawHTTPRequest2Dict(file)
			pair['pairNumber'] = prev_number

			# To check if a path matches one in the spec
			match = False
			# print('actual path: ', request['path'])

			# replace the path extracted from the request with the specification matching one
			for (r, path) in zip(paths_re, pathsInSpec):
				# print('re:', r, 'path:', path)

				if (r.match(request['path'])):
					match = True
					request['path'] = path
					break

			# x = input()
			pair['request'] = request

		elif prev_number == file.name.split('-')[0]:
			response = parsers.RawHTTPResponse2Dict(file)
			pair['response'] = response

			# parsers.pair2json(pair, prev_number, dest)

			# If there is no match with the API specification paths
			# The path is ignored and not counted in the statistics.
			if not match:
				unmatched.append(pair['request']['path'])
				prev_number = ''
				pair.clear()
				continue
			else:
				matched.append(pair['request']['path'])

			addPairToDB(pair)

			prev_number = ''
			pair.clear()

		else:
			unmatched.append(file.name)

	return {"unmatched": unmatched, "pair": pair}


def addInferredSpecEntries(inferredDict, paths_re, pathsInSpec):
	unmatched = []  # unmatched requests/responses
	matched = []
	pair = {}  # pair map for easy json writing

	builtPaths = []
	buildPairs = []
	for inferredPath in inferredDict.keys():
		for method in inferredDict[inferredPath].keys():
			parameters = inferredDict[inferredPath][method]["parameters"].keys()
			paramList = []
			buildPath = inferredPath.replace("{", "")
			buildPath = buildPath.replace("}", "")
			if len(parameters)>0:
				buildPath = buildPath[:-1] + '?' if buildPath[-1] == '/' else buildPath + '?'
			for parameter in parameters:
				paramList.append({"name": parameter, "value": "xyz"})
				buildPath += parameter + "=xyz"

			builtPaths.append(buildPath)
			request = {"method": method, "buildPath": buildPath, "parameters": paramList}

			buildPair = {"request": request, "response": {}}
			buildPairs.append(buildPair)

	print(builtPaths)

	for buildPair in buildPairs:
		request = buildPair['request']
		response = buildPair['response']
		buildPath = request['buildPath']

		match = False
		for (r, path) in zip(paths_re, pathsInSpec):
			# print('re:', r, 'path:', path)

			if r.match(buildPath):
				match = True
				request['path'] = path
				break

		pair['request'] = request
		pair['response'] = response

		if not match:
			unmatched.append(buildPath)
			pair.clear()
			continue

		matched.append(pair['request']['path'])
		addSpecToDB(pair)

		pair.clear()

	return {"unmatched": unmatched, "matched": matched}


def addCassetteEntries(yamlResponses, paths_re, pathsInSpec):
	unmatched = []  # unmatched requests/responses
	matched = []
	pair = {}  # pair map for easy json writing
	for yamlResponse in yamlResponses:
		print(yamlResponse)
		requestResponse = parsers.yamlResponse2Dict(yamlResponse)
		request = requestResponse['request']
		response = requestResponse['response']

		match = False
		# print('actual path: ', request['path'])

		# replace the path extracted from the request with the specification matching one
		for (r, path) in zip(paths_re, pathsInSpec):
			# print('re:', r, 'path:', path)

			if (r.match(request['path'])):
				match = True
				request['path'] = path
				break

		# x = input()
		pair['request'] = request
		pair['response'] = response

		if not match:
			unmatched.append(pair['request']['path'])
			pair.clear()
			continue

		matched.append(pair['request']['path'])
		addPairToDB(pair)

		pair.clear()

	return {"unmatched": unmatched, "matched": matched}


def addJsonEntries(jsonResponses, paths_re, pathsInSpec):
	unmatched = []  # unmatched requests/responses
	matched = []
	pair = {}  # pair map for easy json writing

	for jsonResponse in jsonResponses:
		requestResponse = parsers.JsonResponse2Dict(jsonResponse)
		request = requestResponse["request"]
		response = requestResponse["response"]

		# To check if a path matches one in the spec
		match = False
		# print('actual path: ', request['path'])

		# replace the path extracted from the request with the specification matching one
		for (r, path) in zip(paths_re, pathsInSpec):
			# print('re:', r, 'path:', path)

			if (r.match(request['path'])):
				match = True
				request['path'] = path
				break

		# x = input()
		pair['request'] = request
		pair['response'] = response

		if not match:
			unmatched.append(pair['request']['path'])
			pair.clear()
			continue

		matched.append(pair['request']['path'])
		addPairToDB(pair)

		pair.clear()

	return {"unmatched": unmatched, "matched": matched}


def generatePairs(confDict, pathsInSpec, basesInSpec, dbFile, schemathesis=False):
	if schemathesis:
		yamlResponsesPath = confDict['cassette']
		with open(yamlResponsesPath) as yamlFile:
			yaml = ruamel.yaml.YAML(typ='safe')
			data = yaml.load(yamlFile)
			yamlResponses = json.loads(json.dumps(data))
		source = None
		jsonResponses = None

	elif "results" in confDict:
		jsonResponsesPath = confDict['results']
		with open(jsonResponsesPath) as jsonFile:
			jsonResponses = json.load(jsonFile)
		source = None
		yamlResponses = None
	else:
		jsonResponses = None
		yamlResponses = None
		source = confDict['dumpsDir']

	# dest = confDict['pairsDir']
	# dbFile = confDict['dbPath']

	'''
	Sorting paths in the specification to try to avoid path collision:
	"/user/{id}" and "/user/auth" have a collision because the second
	can be matched by the regex of the first. With a sorted list, the order is
	inverted, so the first regex matching should be the right one.
	'''
	pathsInSpec.sort()

	'''
	Have to be sure that every resource from every possible server is taken
	in consideration.
	'''
	regPaths = []
	for i in range(len(pathsInSpec)):

		suffix = ''
		actualPath = pathsInSpec[i]
		actualPath = actualPath.replace('*', '.*')
		# print('path:', pathsInSpec[i], 'actualPath:', actualPath)

		for b in basesInSpec:
			suffix = suffix + '(' + b.replace('/', '/') + ')|'

		regPaths.append('(' + suffix[:-1] + ')' + actualPath)

	'''
	From every path in the specification extract a regular expression
	for pattern matching with the actual paths found in the requests.
	'''
	paths_re = [re.sub('\{{1}[^{}}]*\}{1}', '[^/]+', x) for x in regPaths]
	paths_re = [x + '?$' if x[-1] == '/' else x + '/?$' for x in paths_re]
	paths_re = [re.compile(x) for x in paths_re]

	#####################
	#### POPULATE DB ####
	dbm.create_connection(dbFile)
	dbm.createTables()
	####     END     ####
	#####################

	if yamlResponses is not None:
		print("Adding YAML responses to DB")
		addCassetteEntries(yamlResponses['http_interactions'], paths_re, pathsInSpec)

	if jsonResponses is not None:
		print("Adding json responses to DB")
		addJsonEntries(jsonResponses, paths_re, pathsInSpec)

	if source is not None:
		print("Adding source dump to DB")
		addSourceEntries(source, paths_re, pathsInSpec)

	#####################
	#### POPULATE DB ####
	# dbm.getValues()
	dbm.closeAndCommit()


def compareParams(matchDict, fpDict, pathsInSpec, basesInSpec, inferredBases):
	if basesInSpec is not None and len(basesInSpec)>0:
		base = basesInSpec[0]
	else:
		base = ""

	if inferredBases is not None and len(inferredBases)>0:
		infBase = inferredBases[0]
	else:
		infBase = ""


	allVars = []
	for path in pathsInSpec:
		path = base+path
		pathSplit = path.split("/")
		for i in range(len(pathSplit)):
			splitElem = pathSplit[i]
			if splitElem.startswith("{"):
				allVars.append(path+":"+splitElem)

	matchedVars = []
	fpVars = []
	for matchedPath in matchDict:
		print(matchedPath)
		specPath = base + matchDict[matchedPath]
		infPath = infBase + matchedPath
		if matchedPath in fpDict:
			infPath = fpDict[matchedPath]
		specPathSplit = specPath.split("/")
		infPathSplit = infPath.split("/")
		if len(specPathSplit)!= len(infPathSplit):
			print("Something is wrong")
			continue
		for pathIndex in range(len(specPathSplit)):
			if specPathSplit[pathIndex].startswith("{"):
				if infPathSplit[pathIndex].startswith("{"):
					matchedVar = specPath + ":" + specPathSplit[pathIndex]
					if matchedVar not in matchedVars:
						matchedVars.append(matchedVar)
			else:
				if infPathSplit[pathIndex].startswith("{"):
					fpVars.append(infPath + ":" + infPathSplit[pathIndex])

	print(matchedVars)
	print(fpVars)
	print(allVars)
	return matchedVars, fpVars, allVars



def compareSpecsNew(confDict):
	original = confDict['specification']
	if "inferred" in confDict:
		inferredSpec = confDict['inferred']
		# inferredDict = parsers.extractSpecificationData(inferredSpec)
	else:
		print("Add the key 'inferred' in config file for the spec to be compared")
		return
	originalSpec = json.load(open(original))
	if 'openapi' in originalSpec.keys():
		parsedOriginal = parsers.parseOpenAPI3(originalSpec)
	else:
		parsedOriginal = parsers.parseSwagger2(originalSpec)

	paths = parsedOriginal

	inferredSpec = json.load(open(inferredSpec))
	if 'openapi' in inferredSpec.keys():
		parsedInf = parsers.parseOpenAPI3(inferredSpec)
	else:
		parsedInf = parsers.parseSwagger2(inferredSpec)

	paths_inf = parsedInf

	compareSpecs_base_new(oldSpec=parsedOriginal, inferredDict=parsedInf, confDict=confDict, inferredSpec=inferredSpec)

def compareSpecs(oldSpec, confDict):
	if "inferred" in confDict:
		inferredSpec = confDict['inferred']
		inferredDict = parsers.extractSpecificationData(inferredSpec)
		compareSpecs_base(oldSpec, inferredSpec, confDict)
	else:
		print("Add the key 'inferred' in config file for the spec to be compared")
		return

def compareSpecs_base_new(oldSpec, inferredDict, confDict, inferredSpec):


	pathVarFP = 0
	path_TP = 0
	path_TN = 0
	path_FP = []
	path_FN = 0

	op_TP = 0
	op_TN = 0
	op_FP = []
	op_FN = 0
	inferredBases = inferredDict.pop('bases')
	inferredPaths = list(inferredDict.keys())
	dbFile = confDict['specDbPath']


	'''
	Sorting paths in the specification to try to avoid path collision:
	"/user/{id}" and "/user/auth" have a collision because the second
	can be matched by the regex of the first. With a sorted list, the order is
	inverted, so the first regex matching should be the right one.
	'''
	basesInSpec = oldSpec.pop('bases')
	pathsInSpec = list(oldSpec.keys())
	pathsInSpec.sort()

	'''
	Have to be sure that every resource from every possible server is taken
	in consideration.
	'''
	regPaths = []
	concPaths = []
	for i in range(len(pathsInSpec)):

		suffix = ''
		actualPath = pathsInSpec[i]
		actualPath = actualPath.replace('*', '.*')
		# print('path:', pathsInSpec[i], 'actualPath:', actualPath)

		for b in basesInSpec:
			suffix = suffix + '(' + b.replace('/', '/') + ')|'

		regPaths.append('(' + suffix[:-1] + ')' + actualPath)

	'''
	From every path in the specification extract a regular expression
	for pattern matching with the actual paths found in the requests.
	'''
	paths_re = [re.sub('\{{1}[^{}}]*\}{1}', '[^/]+', x) for x in regPaths]
	paths_re = [x + '?$' if x[-1] == '/' else x + '/?$' for x in paths_re]
	paths_re = [re.compile(x) for x in paths_re]

	print(paths_re)



	opDict = {}
	pairDict = {}
	fpDict = {}
	print("inferred Paths: {}".format(inferredPaths))
	for inferredPath in inferredPaths:
		inferredPathWhole = inferredBases[0] + inferredPath
		buildPath = inferredPathWhole.replace("{", "")
		buildPath = buildPath.replace("}", "")
		print(buildPath)
		match = False
		for (r, path) in zip(paths_re, pathsInSpec):
			print('re:', r, 'path:', path)
			if r.match(buildPath):
				match = True
				inferredOperations = inferredDict[inferredPath].keys()
				originalOperations = oldSpec[path].keys()
				print(inferredOperations)
				print(originalOperations)
				for opKey in inferredOperations:
					if opKey in originalOperations:
						opDict[inferredPath+"-"+opKey] = path+"-"+opKey
					else:
						op_FP.append(inferredPath+"-"+opKey)
				pairDict[inferredPath] = path
				print(pairDict)
				break
		if not match:
			# build concrete paths from examples and match
			concMatch = False
			try:
				concMatches = []
				specElem = inferredSpec['paths'][inferredPath]['parameters']
				buildPaths = []
				if specElem is not None and len(specElem) >0:

					for exampleIndex in range(len(specElem[0]['examples'].values())):
						inferredPathWhole = inferredBases[0] + inferredPath
						buildPath = inferredPathWhole
						for parameter in specElem:
							print(parameter['examples'])
							print(parameter['examples'].values())
							buildPath = buildPath.replace('{'+parameter['name'] +'}', list(parameter['examples'].values())[exampleIndex]['value'])
						if buildPath in buildPaths:
							continue
						else:
							buildPaths.append(buildPath)

						for (r, path) in zip(paths_re, pathsInSpec):
							print('re:', r, 'path:', path)
							if r.match(buildPath):
								if path in concMatches:
									break
								concMatch = True
								concMatches.append(path)
								fpDict[buildPath] = inferredPath
								inferredOperations = inferredDict[inferredPath].keys()
								originalOperations = oldSpec[path].keys()
								print(inferredOperations)
								print(originalOperations)
								for opKey in inferredOperations:
									if opKey in originalOperations:
										opDict[buildPath+"-"+opKey] = path+"-"+opKey
									else:
										op_FP.append(buildPath+"-"+opKey)
								pairDict[buildPath] = path
								print(pairDict)
								break
			except Exception as ex:
				print(ex)
			if not concMatch:
				path_FP.append(inferredPath + "-" + str(inferredDict[inferredPath].keys()))
				inferredOperations = inferredDict[inferredPath].keys()
				for opKey in inferredOperations:
					op_FP.append(inferredPath+"-"+opKey)

	print(pairDict)
	print(fpDict)
	numVars = 0
	matchedVars, fpVars, allVars = compareParams(pairDict, fpDict, pathsInSpec, basesInSpec, inferredBases)

	covered = set(pairDict.values())
	coverage = len(covered)/len(pathsInSpec)
	precision = len(covered)/len(pairDict.keys())
	print(covered)
	print(pathsInSpec)
	print("coverage : {}, precision : {}".format(coverage, precision))

	opCovered = set(opDict.values())
	totalOperations = 0
	for path in oldSpec.keys():
		totalOperations+=len(oldSpec[path].keys())

	operationPr= len(opCovered)/len(opDict.keys())
	operationRe= len(opCovered)/totalOperations

	# covDict = {"pathPr": precision, "pathRe": coverage, "operationPr": operationPr, "operationRe": operationRe}
	covDict = {
		"path":{
			"matched": len(pairDict.keys()),
			"unmatched": len(path_FP),
			"matched_unique": len(covered),
			"gt": len(pathsInSpec),
			"fp": path_FP
		},
		"op":{
			"matched": len(opDict.keys()),
			"unmatched": len(op_FP),
			"matched_unique": len(opCovered),
			"gt": totalOperations,
			"fp": op_FP
		},
		"var":{
			"matched": len(matchedVars),
			"fp": len(fpVars),
			"gt": len(allVars)
		}
	}

	print(covDict)
	with open(confDict["specReports"] + '/stats.json', 'w+') as out:
		json.dump(covDict, out, indent='\t')
		print('Metrics and statistics computed successfully. Reports are available at', confDict["specReports"])

	return covDict


def compareSpecs_base(oldSpec, inferredDict, confDict):


	pathVarFP = 0
	path_TP = 0
	path_TN = 0
	path_FP = []
	path_FN = 0

	op_TP = 0
	op_TN = 0
	op_FP = []
	op_FN = 0
	inferredBases = inferredDict.pop('bases')
	inferredPaths = list(inferredDict.keys())
	dbFile = confDict['specDbPath']


	'''
	Sorting paths in the specification to try to avoid path collision:
	"/user/{id}" and "/user/auth" have a collision because the second
	can be matched by the regex of the first. With a sorted list, the order is
	inverted, so the first regex matching should be the right one.
	'''
	basesInSpec = oldSpec.pop('bases')
	pathsInSpec = list(oldSpec.keys())
	pathsInSpec.sort()

	'''
	Have to be sure that every resource from every possible server is taken
	in consideration.
	'''
	regPaths = []
	concPaths = []
	for i in range(len(pathsInSpec)):

		suffix = ''
		actualPath = pathsInSpec[i]
		actualPath = actualPath.replace('*', '.*')
		# print('path:', pathsInSpec[i], 'actualPath:', actualPath)

		for b in basesInSpec:
			suffix = suffix + '(' + b.replace('/', '/') + ')|'

		regPaths.append('(' + suffix[:-1] + ')' + actualPath)

	'''
	From every path in the specification extract a regular expression
	for pattern matching with the actual paths found in the requests.
	'''
	paths_re = [re.sub('\{{1}[^{}}]*\}{1}', '[^/]+', x) for x in regPaths]
	paths_re = [x + '?$' if x[-1] == '/' else x + '/?$' for x in paths_re]
	paths_re = [re.compile(x) for x in paths_re]

	print(paths_re)



	opDict = {}
	pairDict = {}
	print("inferred Paths: {}".format(inferredPaths))
	for inferredPath in inferredPaths:

		buildPath = inferredPath.replace("{", "")
		buildPath = buildPath.replace("}", "")
		print(buildPath)
		match = False
		for (r, path) in zip(paths_re, pathsInSpec):
			print('re:', r, 'path:', path)
			if r.match(buildPath):
				match = True
				inferredOperations = inferredDict[inferredPath].keys()
				originalOperations = oldSpec[path].keys()
				print(inferredOperations)
				print(originalOperations)
				for opKey in inferredOperations:
					if opKey in originalOperations:
						opDict[inferredPath+"-"+opKey] = path+"-"+opKey
					else:
						op_FP.append(inferredPath+"-"+opKey)
				pairDict[inferredPath] = path
				print(pairDict)
				break
		if not match:
			path_FP.append(inferredPath + "-" + str(inferredDict[inferredPath].keys()))

	print(pairDict)
	numVars = 0

	covered = set(pairDict.values())
	coverage = len(covered)/len(pathsInSpec)
	precision = len(covered)/len(pairDict.keys())
	print(covered)
	print(pathsInSpec)
	print("coverage : {}, precision : {}".format(coverage, precision))

	opCovered = set(opDict.values())
	totalOperations = 0
	for path in oldSpec.keys():
		totalOperations+=len(oldSpec[path].keys())

	operationPr= len(opCovered)/len(opDict.keys())
	operationRe= len(opCovered)/totalOperations

	# covDict = {"pathPr": precision, "pathRe": coverage, "operationPr": operationPr, "operationRe": operationRe}
	covDict = {
				"path":{
					"matched": len(pairDict.keys()),
					"unmatched": len(path_FP),
					"matched_unique": len(covered),
					"gt": len(pathsInSpec),
					"fp": path_FP
				},
			  	"op":{
					"matched": len(opDict.keys()),
					"unmatched": len(op_FP),
					"matched_unique": len(opCovered),
					"gt": totalOperations,
					"fp": op_FP
			  	}
	}

	print(covDict)
	with open(confDict["specReports"] + '/stats.json', 'w+') as out:
		json.dump(covDict, out, indent='\t')
		print('Metrics and statistics computed successfully. Reports are available at', confDict["specReports"])

	return covDict

def generateSpecPairs(confDict, pathsInSpec, basesInSpec):
	if "inferred" in confDict:
		inferredSpec = confDict['inferred']
		inferredDict = parsers.extractSpecificationData(inferredSpec)
	else:
		print("Add the key 'inferred' in config file for the spec to be compare")
		return

	inferredBases = inferredDict.pop('bases')
	inferredPaths = inferredDict.keys()
	dbFile = confDict['specDbPath']


	'''
	Sorting paths in the specification to try to avoid path collision:
	"/user/{id}" and "/user/auth" have a collision because the second
	can be matched by the regex of the first. With a sorted list, the order is
	inverted, so the first regex matching should be the right one.
	'''
	pathsInSpec.sort()

	'''
	Have to be sure that every resource from every possible server is taken
	in consideration.
	'''
	regPaths = []
	for i in range(len(pathsInSpec)):

		suffix = ''
		actualPath = pathsInSpec[i]
		actualPath = actualPath.replace('*', '.*')
		# print('path:', pathsInSpec[i], 'actualPath:', actualPath)

		for b in basesInSpec:
			suffix = suffix + '(' + b.replace('/', '/') + ')|'

		regPaths.append('(' + suffix[:-1] + ')' + actualPath)

	'''
	From every path in the specification extract a regular expression
	for pattern matching with the actual paths found in the requests.
	'''
	paths_re = [re.sub('\{{1}[^{}}]*\}{1}', '[^/]+', x) for x in regPaths]
	paths_re = [x + '?$' if x[-1] == '/' else x + '/?$' for x in paths_re]
	paths_re = [re.compile(x) for x in paths_re]

	print(paths_re)

	#####################
	#### POPULATE DB ####
	dbm.create_connection(dbFile)
	dbm.createTables()
	####     END     ####
	#####################

	if inferredPaths is not None:
		print("Adding json responses to DB")
		addInferredSpecEntries(inferredDict, paths_re, pathsInSpec)

	#####################
	#### POPULATE DB ####
	# dbm.getValues()
	dbm.closeAndCommit()

####     END     ####
#####################

if __name__=="__main__":
	with open("/Users/apicarv/git/TestCarving/testCarver/out/parabank/20220710_035630/oas/20220710_043329/oas_conf.json") as j:
	# with open("/Users/apicarv/git/TestCarving/testCarver/out/booker/20220711_144103/oas/20220711_151840/oas_conf.json") as j:
		conf = json.load(j)

	for k in conf:
		conf[k] = conf[k][:-1] if conf[k][-1] == '/' else conf[k]

	specDict = utils.parsers.extractSpecificationData(conf['specification'])


	compareSpecs(specDict, conf)
