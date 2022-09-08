import csv
import json
import os
import subprocess
from datetime import datetime
from enum import Enum
from subprocess import check_call, CalledProcessError, Popen
from time import sleep

import psutil

from constants import DOCKER_LOCATION, STATUS_SUCCESSFUL


def getDockerName(appName):
	return appName


def restartDockerVersion(appName):
	# if version == None:
	# 	restartDocker(getDockerName(appName))
	# 	return
	# dockerList = getDockerList(version)
	# print(dockerList[appName])
	restartDocker(getDockerName(appName=appName))


def restartDocker(dockerName, SLEEPTIME=30):

	stopDocker = [os.path.join(DOCKER_LOCATION, dockerName, 'stop-docker.sh')]

	try:
		check_call(stopDocker)
	except CalledProcessError as ex:
		print("Could not stop docker docker? ")
		print(ex)

	startDocker = [os.path.join(DOCKER_LOCATION, dockerName, 'run-docker.sh')]
	try:
		check_call(startDocker)
		sleep(SLEEPTIME)
	except CalledProcessError as ex:
		print("No matching processes Found for docker? ")
		print(ex)

class MODE(Enum):
	CARVER = "carver"
	ST = "schemathesis"


def cleanup(mode, appName=None, outputDir = None):

	if mode is MODE.CARVER:
		killChromeDriverCommand = ['killall', 'chromedriver']
		try:
			check_call(killChromeDriverCommand)
		except CalledProcessError as ex:
			print("No matching processes Found for chromedriver? ")
			print(ex)

		killGoogleChromeCommand = ['killall', 'chrome']

		try:
			check_call(killGoogleChromeCommand)
		except CalledProcessError as ex:
			print("No matching processes Found for Google Chrome? ")
			print(ex)

	if appName is None:
		print("No appName provided. Not resetting Docker")
		return

	dockerName = getDockerName(appName)
	if not dockerName is None:
		stopDocker = [os.path.join(DOCKER_LOCATION, dockerName, 'stop-docker.sh')]
		if outputDir is not None:
			stopDocker.append(outputDir)
		try:
			check_call(stopDocker)
		except CalledProcessError as ex:
			print("Could not stop docker docker? ")
			print(ex)


def kill_process(pid):
	try:
		proc = psutil.Process(pid)
		print("Killing", proc.name())
		proc.kill()
	except psutil.NoSuchProcess as ex:
		print("No Such Process : {0}".format(pid))


def monitorProcess(proc, runtime=30, timeStep=30, timeout=200, crawljaxOutputPath=None, existing=-1):
	done = False
	timeDone = 0
	graceTime = 60
	status = None
	while not done:
		poll = proc.poll()
		if poll == None:
			print("process still running {0}/{1}".format(str(timeDone), str(runtime * 60)))
			sleep(timeStep)
			timeDone += timeStep
		else:
			done = True
			status = STATUS_SUCCESSFUL
			break

	return status


def changeDirectory(path):
	try:
		os.chdir(path)
		return True
	except OSError as ex:
		print("Could not change director")
		print(ex)
		return False


def startProcess(command, outputPath="output_crawljax_" + str(datetime.now().strftime("%Y%m%d-%H%M%S")) + ".log",
				 changeDir=None,
				 DEBUG=False):
	changed = False
	current = os.getcwd()
	try:
		if changeDir is not None:
			changed = changeDirectory(changeDir)

		if DEBUG:
			process = Popen(command)
			return process
		else:
			print("outputtting log to {0}".format(outputPath))
			with open(outputPath, 'w') as outputFile:
				proc = Popen(command, stderr=subprocess.STDOUT, stdout=outputFile)
				print("Started {0} with PID {1}".format(command, proc.pid))
				return proc
	except Exception as ex:
		print(ex)
		print("Exception try to run {0} : ".format(command))
	finally:
		if changed:
			changeDirectory(current)


def exportJson(file, jsonData):
	with open(file, "w") as write_file:
		json.dump(jsonData, write_file)

def writeCSV_Dict(csvFields, csvRows, dst):
	# print(csvRows)
	with open(dst, 'w') as csvfile:
		writer = csv.DictWriter(csvfile, fieldnames=csvFields)
		writer.writeheader()

		for row in csvRows:
			writer.writerow(row)

def writeCSV(rows, dest):
	with open(dest, 'w') as csvFile:
		writer = csv.writer(csvFile, rows)
		for row in rows:
			writer.writerow(row)
			writer.writerow(row)

def importJson(jsonFile):
	try:
		with open(jsonFile, encoding='utf-8') as data_file:
			data = json.loads(data_file.read())
			return data
	except Exception as ex:
		print("Exception occured while importing json from : " + jsonFile)
		print(ex)
		return None

if __name__=="__main__":
	cleanup(MODE.ST, appName="realworld", outputDir="../out/testProbe/cov")