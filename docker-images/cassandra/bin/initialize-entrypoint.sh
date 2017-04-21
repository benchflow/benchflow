#!/bin/bash
set -e

if [ "${CASSANDRA_INITIALIZE}" = true ] ; then
    echo "Starting Casandra to initialize it..."
	cassandra -R
	echo "Waiting for Cassandra to be ready..."
	sleep 20
	echo "Initialising the database..."
	cqlsh -f /app/data/benchflow.cql
	echo "Killing Cassandra..."
	pkill -f cassandra
	sleep 20
	echo "Cassandra database initialised..."
fi

# execute the base image entrypoint in the context of the current shell
source /docker-entrypoint.sh