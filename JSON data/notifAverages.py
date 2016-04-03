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

	pre.append(preHeartRate)
	post.append(postHeartRate)

print "Pre-notification average: %.3f" % (sum(pre) / len(pre))
print "Post-notification average: %.3f" % (sum(post) / len(post))