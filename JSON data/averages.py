import sys
import json

with open(sys.argv[1]) as json_data:
	data = json.load(json_data)
	json_data.close()

UPPER_LIMIT = 105
packages = []
entries = []
totalHeartRates = []

for reading in data:
	package = reading['package_name']
	heartRate = int(reading['heart_rate'])
	if heartRate > UPPER_LIMIT:
		continue

	try:
		index = packages.index(package)
		entries[index] = entries[index] + 1
		totalHeartRates[index] = totalHeartRates[index] + heartRate
	except:
		packages.append(package)
		entries.append(1)
		totalHeartRates.append(heartRate)

for package in packages:
	index = packages.index(package)
	print "%s: %d (readings: %d)" % (package, totalHeartRates[index] / entries[index], entries[index])