import os.path

# APPS = ['medical']
APPS = ['petclinic', 'parabank', 'realworld', 'booker', 'jawa', 'medical', 'ecomm']
DOCKER_LOCATION = os.path.abspath('../src/main/resources/webapps')
RUN_CARVER_COMMAND = ['java', '-Xmx8G', '-Xss1G', '-cp', 'target/testCarver-0.0.1-SNAPSHOT-jar-with-dependencies.jar',
                'com.apicarv.testCarver.Main']
RUN_SCHEMATHESIS_COMMAND = [os.path.abspath('venv/bin/st'), 'run']
RESTATS_PATH = os.path.abspath('../../restats')
RUN_RESTATS_COMMAND = [os.path.abspath('venv/bin/python'),
                       os.path.join(RESTATS_PATH, 'app.py')]
JACOCO_MERGE_COMMAND = ['java', '-jar', os.path.abspath('libs/org.jacoco.cli-0.8.8-nodeps.jar'), 'merge']
JACOCO_REPORT_COMMAND = ['java', '-jar', os.path.abspath('libs/org.jacoco.cli-0.8.8-nodeps.jar'), 'report']

CASETTE_YAML = "cassette.yaml"
RESULT_RESPONSES_JSON = "resultResponses.json"
PROBER_RESPONSES_JSON = "allPrresultResponses.json"

INFERRED_YAML = "oas.yaml"
PROBER_YAML = "probe_oas.yaml"
INFERRED_JSON = "oas.json"
PROBER_JSON = "probe_oas.json"
ENHANCED_YAML = "openAPI_enhanced.yaml"

SCHEMATHESIS_OUTPUT = "schemathesis"
SCHEMATHESIS_CARVER = SCHEMATHESIS_OUTPUT + "_carver"
SCHEMATHESIS_PROBER = SCHEMATHESIS_OUTPUT + "_prober"

EVOMASTER_OUTPUT = "evomaster"


RESTATS_OUT_DIR = "reports"

COV_XML = "cov.xml"
COV_CARVER_XML = "covcarver.xml"
COV_PROBER_XML = "covprober.xml"

COV_JAWA_XML = "app.xml"
COV_JAWA_CARVER_XML = "appcarver.xml"
COV_JAWA_PROBER_XML = "appprober.xml"
COV_PARABANK_CARVER_XML = "jacococarver.xml"
COV_PARABANK_PROBER_XML = "jacocoprober.xml"

STATUS_SUCCESSFUL = "successful"
STATUS_NO_OUTPUT = "noOutput"
STATUS_STRAY_TERMINATED = "strayProcessTerminated_OutputObtained"
STATUS_SKIPPED = "skipped"
STATUS_ERRORED = "errored"

def getHostURL(appName):
    if appName == "petclinic":
        return "http://localhost:9966/petclinic/api"

    if appName == "parabank":
        return "http://localhost:8080/parabank-3.0.0-SNAPSHOT/services/"

    if appName == "realworld":
        return "http://localhost:3000/api"

    if appName == "booker":
        return "http://localhost:8080"

    if appName == "jawa":
        return "http://localhost:8080"

    if appName == "shopizer":
        return "http://localhost:8080/api"

    if appName == "medical":
        return "http://localhost:8080"

    if appName == "ecomm":
        return "http://localhost:8080/api"

    return None


NYC_REPORT = "index.html"
NYC_CARVER_REPORT = "indexcarver.html"
NYC_PROBER_REPORT = "indexprober.html"

COV_EXEC = "*.exec"


BOOKER_MODULES = ["assets", "auth", "booking", "branding", "message", "report", "room"]
