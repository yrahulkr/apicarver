from pathlib import Path
import sys
import json

import core.pairing as pairing
import core.statistic as stat
import utils.parsers as par

def callOptionMethod(confDict):

	modules = confDict['modules']

	# Extract data from specification (needed to parse pairs)
	specDict = par.extractSpecificationData(conf['specification'])
	# Pop the base paths of the API
	bases = specDict.pop('bases')

	if modules == 'dataCollection':
		paths = list(specDict.keys())
		pairing.generatePairs(confDict, paths, bases, dbFile=confDict['dbPath'])

	elif modules == 'statistics':
		stat.generateStats(specDict, dbfile=confDict['dbPath'], dest=confDict['reportsDir'])

	elif modules == 'carver':
		paths = list(specDict.keys())
		pairing.generatePairs(confDict, paths, bases, dbFile=confDict['dbPath'])
		stat.generateStats(specDict, dbfile=confDict['dbPath'], dest=confDict['reportsDir'])

	elif modules == 'all':
		paths = list(specDict.keys())
		pairing.generatePairs(confDict, paths, bases, dbFile=confDict['dbPath'])
		stat.generateStats(specDict, dbfile=confDict['dbPath'], dest=confDict['reportsDir'])
		pairing.generatePairs(confDict, paths, bases, dbFile=confDict['cassetteDbPath'], schemathesis=True)
		stat.generateStats(specDict, dbfile=confDict['dbPath'], dest=confDict['cassetteReportsDir'])

	elif modules == 'specCompare':
		specDict = par.extractSpecificationData(conf['specification'])
		# paths = list(specDict.keys())
		# pairing.generateSpecPairs(confDict, paths, bases)
		# stat.generateStats(specDict, confDict)
		# pairing.compareSpecs(specDict, confDict)
		pairing.compareSpecsNew(confDict)

	elif modules == "schemathesis":
		paths = list(specDict.keys())
		pairing.generatePairs(confDict, paths, bases, dbFile=confDict['cassetteDbPath'], schemathesis=True)
		stat.generateStats(specDict, dbfile=confDict['cassetteDbPath'], dest=confDict['cassetteReports'])

	elif modules == "merge":
		paths = list(specDict.keys())
		pairing.generatePairs(confDict, paths, bases, dbFile=confDict['mergeDb'], schemathesis=True)
		pairing.generatePairs(confDict, paths, bases, dbFile=confDict['mergeDb'])
		stat.generateStats(specDict, dbfile=confDict['mergeDb'], dest=confDict['mergeReports'])

	else:
		raise Exception('Wrong module. [pair/statistics/all]')


if __name__ == '__main__':

	try:
		cFilePath = sys.argv[1]
	except:
		# cFilePath = "petclinic2/config.json"
		cFilePath = "/Users/apicarv/git/TestCarving/testCarver/out/jawa/20220711_025641/oas/20220711_032415/probe_oas_json_conf.json"

	# Read configuration file
	with open(cFilePath) as j:
		conf = json.load(j)

	for k in conf:
		conf[k] = conf[k][:-1] if conf[k][-1] == '/' else conf[k]

	callOptionMethod(conf)

'''	verbose = conf['verbose']

	# Extract data from specification (needed to parse pairs)
	specDict = par.extractSpecificationData(conf['specification'])
	# Pop the base paths of the API
	bases = specDict.pop('bases')
	paths = list(specDict.keys())

	if conf['option'] == 'pair':	
		pairing.generatePairs(conf, paths, bases)

	elif conf['option'] == 'statistics':
		stat.generateStats(specDict, conf['reportDirPath'], conf['dbFilePath'])

	else:
		raise Exception('Not implemented. WIP.')'''
