#!/bin/bash

url=http://localhost:9090/pattern
numMessages=100
patterns=(retryViaNackDLQPattern retryViaNackPattern retryTopicPattern stopOnErrorPattern ignoreErrorPattern)

for i in "${patterns[@]}"
do
  echo "Running Pattern: $i [Bad Messages Scenario]"
	curl -s "$url/$i?numMessages=$numMessages&errorDurationMillis=10&errorOnValues=13,42" > ~/Downloads/$i-BadMessages.json
	wait
done

for i in "${patterns[@]}"
do
  echo "Running Pattern: $i [External Outage Scenario]"
  curl -s "$url/$i?numMessages=$numMessages&errorDurationMillis=15000&errorOnValues=11" > ~/Downloads/$i-ExternalOutage.json
  wait
done

for i in "${patterns[@]}"
do
  echo "Running Pattern: $i [Network Issues Scenario]"
  curl -s "$url/$i?numMessages=$numMessages&errorDurationMillis=3000&errorOnCounts=9,23" > ~/Downloads/$i-NetworkIssues.json
  wait
done



