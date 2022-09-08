import glob
import os

# print(os.path.splitext("../a/b/c.json")[0])
# carverRecords = "../a/c.json"
#
# dir = os.path.pathsep.join(os.path.split(carverRecords)[0:len(os.path.split(carverRecords))-1])
# print(dir)
import constants
import coverageStats
from constants import RESULT_RESPONSES_JSON

# print(glob.glob( "/TestCarving/testCarver/out/petclinic/schemathesis/*/cov/" + constants.COV_XML))

print((coverageStats.getRawCovFiles("realworld")))
