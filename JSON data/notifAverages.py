import sys
import json

with open(sys.argv[1]) as json_data:
	data = json.load(json_data)
	json_data.close()

UPPER_LIMIT = 105
pre = []
post = []

for reading in data:
	preHeartRate = float(reading['pre'])
	postHeartRate = float(reading['post'])

	if preHeartRate > UPPER_LIMIT or postHeartRate > UPPER_LIMIT:
		continue

	#if int(preHeartRate * 1000) == int(postHeartRate * 1000):
	#	continue

	pre.append(preHeartRate)
	post.append(postHeartRate)

preAv = (sum(pre) / len(pre))
postAv = (sum(post) / len(post))
print "Pre-notification average: %.3f" % preAv
print "Post-notification average: %.3f" % postAv
print "Difference: %.3f" % (postAv - preAv)
print "Number of samples: %d" % len(pre)