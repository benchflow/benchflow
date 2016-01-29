#!/bin/bash
# Handle Docker Hub intra repo dependencies among Dockerfile stored in the same GitHub repository and part
# of the same automated build repository on DokerHub. The current script is meant to be executed in a CI environment
# 
# Problem description:
#   Docker Hub still does not support the definition of dependencies among Dockerfile stored in the same GitHub repo (https://groups.google.com/forum/#!topic/docker-user/Vqt8-bitfuo)
#	BenchFlow base-images (https://github.com/benchflow/docker-images) declare dependencies each other and we need to enforce a specific build order
#   
# Solution:
#	This script trigger updates on DockerHub and waits for the dependencies to be built before triggering the build of the dependant images
# 
# NOTE:
#	- Currently this script only works for https://github.com/benchflow/docker-images as it is the only repository
#	  with intra repository dependencies, currently. TODO: as soon as we need it for another repo, abstract the way
#     in which it is possible to define the dependencies
#	- Currently declared dependencies (the names refer to the folder names here https://github.com/benchflow/docker-images/tree/dev/base-images):
#		- all the images depend on: base
#		- base-envconsul-java7 and base-envconsul-java8 depend on: base-envconsul
#	- The echo commands are informative, but they also deal with the Travis behaviour described in the following: https://docs.travis-ci.com/user/common-build-problems/#My-builds-are-timing-out (output timeout)
# TODO:
#	- Refactoring, refactoring, refactoring!
set -e

# install dependencies (JSON parser)
sudo wget https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64 -O /usr/bin/jq
sudo chmod +x /usr/bin/jq

# Min waiting time of 5min (300s) before a new trig get accepted by the Docker Hub (https://docs.docker.com/v1.7/docker/docker-hub/builds/#remote-build-triggers)
min_wait=300

#################################################### FUNCTIONS ####################################################

# We rely on the (currently undocumented) API and status codes discussed on the following issue: https://github.com/badges/shields/issues/241
# Structure of the query to the Docker Hub:
#	- We GET the build history of the REPO
#	- We check for the status of the builds for the given tag and wait until we have all the images correclty built (status 10)
#		- NOTE: the API returns the latest builds of the given tag, that is why we need to check for the status. An improvemement
#				would be to check just for the status of the latest build in the order of the given tag
function wait_build {
	export DOCKER_HUB_TAG_NAME=$1
	# count the time in seconds spent in this function, to adjust the waiting time before triggering the next build on the Docker Hub
	local wait_time=0
	while :
	do
	  sleep 60 #wait one minute
	  wait_time=`expr $wait_time + 60`
	  res=$(curl -sS -X GET https://hub.docker.com/v2/repositories/benchflow/base-images/buildhistory/ | /usr/bin/jq --arg DOCKER_HUB_TAG_NAME $DOCKER_HUB_TAG_NAME '.results[] | select(.dockertag_name == $DOCKER_HUB_TAG_NAME) | select(.status != 10)')
	  if [[ -z "$res" ]]; then
	    break
	  fi
	done

	echo $wait_time
}

# TODO: check the response and retry in case of errors
function trigger_docker_hub_build {

	# wait for the time remaining for the min_wait time before triggering the build
	if [[ "$2" ]]; then
		echo "set $2"
		wait_remaining=`expr $min_wait - $2`
		echo "wait_remaining $wait_remaining"

		if [ "$wait_remaining" -gt "0" ]; then
			echo "sleep $wait_remaining"
			sleep $wait_remaining
		fi
	fi

	curl -H "Content-Type: application/json" --data '{"docker_tag": "'$1'"}' -X POST $DOCKER_HUB_TRIGGER
}
#################################################### FUNCTIONS ####################################################

# Trigger the build of all the images without dependencies (using the assigned docker_tags)
echo "Triggering dev build"
trigger_docker_hub_build dev

# Wait for the build
waited=$(wait_build dev)

# Trigger the builds and wait for the build in dependency order
echo "Triggering minio-client_dev build"
trigger_docker_hub_build minio-client_dev $waited
waited=$(wait_build minio-client_dev)
echo "Triggering envconsul_dev build"
trigger_docker_hub_build envconsul_dev $waited
waited=$(wait_build envconsul_dev)

# Trigger leaf builds in the dependency tree
echo "Triggering envconsul-java7_dev build"
trigger_docker_hub_build envconsul-java7_dev $waited
echo "Triggering envconsul-java8_dev build"
trigger_docker_hub_build envconsul-java8_dev 0
