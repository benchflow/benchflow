#!/bin/bash
set -e

#echo "Starting Kafka to initialize it..."

#/usr/share/zookeeper/bin/zkServer.sh start
#pid_zk=$!
#/usr/bin/start-kafka.sh start &
#pid_ka=$!

echo "Waiting for Kafka to be ready..."

sleep 20

echo "Initialising the topics..."

#TODO: make a script taking care of a file to take care of the topics definition centralized in a file that should be placed in https://github.com/benchflow/benchflow, and with better partition and replication-factor conf
#	   moreover we should try to have these initializations at build time, if feasible
$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper 127.0.0.1:2181 --topic mysql --partitions 1 --replication-factor 1
sleep 1
$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper 127.0.0.1:2181 --topic zip --partitions 1 --replication-factor 1
sleep 1
$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper 127.0.0.1:2181 --topic stats --partitions 1 --replication-factor 1
sleep 1
$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper 127.0.0.1:2181 --topic logs --partitions 1 --replication-factor 1
sleep 1
$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper 127.0.0.1:2181 --topic properties --partitions 1 --replication-factor 1
sleep 1
$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper 127.0.0.1:2181 --topic faban --partitions 1 --replication-factor 1

#PGID_KA=$(ps -o pgid= $pid_ka | grep -o [0-9]*)
#PGID_ZK=$(ps -o pgid= $pid_zk | grep -o [0-9]*)

#echo "Killing Kafka..."

#kill -9 -$PGID_KA
#kill -9 -$PGID_ZK

#echo "Killed Kafka..."

echo "Kafka initialised..."

# enable other commands to run
#exec "$@"