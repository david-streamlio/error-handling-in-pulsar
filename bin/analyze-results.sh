#!/bin/bash


if [ $# -eq 0 ]; then
  echo "Usage: $0 file_name"
  exit 1
fi

file_name=$1

echo "Analyzing file $file_file_name"
echo "Processed messages"
jq -r '.processedMessages[].contents' "$file_name" | wc -l
jq -r '.processedMessages[].contents' "$file_name"

echo "Reprocessed Messages"
jq '.processingCounts[] | select(.processCount > 2)' "$file_name"

echo "Dead letter queue"
jq '.dlqMessages[].contents' "$file_name"


