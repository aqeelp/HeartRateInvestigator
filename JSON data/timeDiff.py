import sys
import json
import time

"Sun Apr 03 10:33:42 EDT 2016"

with open(sys.argv[1]) as json_data:
	data = json.load(json_data)
	json_data.close()

UPPER_LIMIT = 105
pre = []
post = []
prev = None

for reading in data:
	if prev == None:
		prev = time.mktime(time.strptime(reading['time'], "%a %b %d %H:%M:%S %Z %Y"))
	else:
		thisTime = time.mktime(time.strptime(reading['time'], "%a %b %d %H:%M:%S %Z %Y"))
		diff = thisTime - prev
		prev = thisTime
		if diff < 50000 and diff > 300:
			preHeartRate = float(reading['pre'])
			postHeartRate = float(reading['post'])
			if preHeartRate > UPPER_LIMIT or postHeartRate > UPPER_LIMIT:
				continue

			if int(preHeartRate * 1000) == int(postHeartRate * 1000):
				continue
			pre.append(preHeartRate)
			post.append(postHeartRate)

preAv = (sum(pre) / len(pre))
postAv = (sum(post) / len(post))
print "Pre-notification average: %.3f" % preAv
print "Post-notification average: %.3f" % postAv
print "Difference: %.3f" % (postAv - preAv)
print "Number of samples: %d" % len(pre)
