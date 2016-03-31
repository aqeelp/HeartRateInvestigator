#!/bin/bash

adb pull /storage/emulated/0/aqeelp_heartrate/heartRateData.json
awk -f fixFormatting.awk heartRateData.json > $1
python averages.py $1 
rm heartRateData.json