#!/bin/sh

while(true)
do
	echo "--- net ---"
	date '+%Y-%m-%dT%H:%M:%S%Z'
	ip --json -s -s link show
	sleep 10
done
