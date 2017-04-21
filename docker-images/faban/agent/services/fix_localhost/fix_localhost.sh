#!/bin/bash
set -e

#Get the actual container IP on the network

#If on TUTUM, get the IP of the container on the TUTUM network
if [[ $TUTUM_IP_ADDRESS ]]; then 
	#strips the trailing /N+
	TUTUM_IP_ADDRESS=`echo $TUTUM_IP_ADDRESS | sed -r 's/\/([0-9]+)//'`
	ip=$TUTUM_IP_ADDRESS
#If on RANCHER, get the IP of the container on the RANCHER network. 
#This is a custom ENV variable, it seems there are no variables set by Rancher to identify if
#a container is running in the Rancher infrastructure
elif [[ $IS_RANCHER ]]; then
	#Reference: https://forums.rancher.com/t/get-container-rancher-ip-inside-a-container/357
	#TODO: improve using metadata service (http://docs.rancher.com/rancher/latest/en/rancher-services/metadata-service/)
	ip=`ip addr | grep inet | grep 10.42 | tail -1 | awk '{print $2}' | awk -F\/ '{print $1}'`
#Else we get the IP of the current container
#If using network --net="host" we pass the actual IP of the host
elif [[ $HOST_IP ]]; then
	ip=$HOST_IP
#Else we get the IP of the current container
else
	ip=`ifconfig eth0 | grep "inet addr:" | cut -d : -f 2 | cut -d " " -f 1`
fi
#Update the localhost to the actual container IP on the network
#We cannot direclty edit /ets/hosts with sed: https://github.com/smdahlen/vagrant-hostmanager/issues/136
sed -r 's/127.0.0.1(([[:space:]]\+)|\t)localhost/'$ip' localhost/g' /etc/hosts > /etc/hosts.new
#If on TUTUM or RANCHER, we also need to update the local IP of the container
if [[ $TUTUM_CONTAINER_HOSTNAME ]]; then
	CONTAINER_LOCAL_IP=`ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}'`
    sed -ri 's/'$CONTAINER_LOCAL_IP'(([[:space:]]\+)|\t)'$TUTUM_CONTAINER_HOSTNAME'/'$ip' '$TUTUM_CONTAINER_HOSTNAME'/g' /etc/hosts.new
elif [[ $IS_RANCHER ]]; then
	CONTAINER_LOCAL_IP=`ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}'`
	#we use the HOSTNAME to identify the CONTAINER_NAME. This assumes that on Rancher at the deployment
	#time, one select to set "Use the container name" as value for the hostname setting
	#TODO: a better way would be to using metadata service (http://docs.rancher.com/rancher/latest/en/rancher-services/metadata-service/)
    sed -ri 's/'$CONTAINER_LOCAL_IP'(([[:space:]]\+)|\t)'$HOSTNAME'/'$ip' '$HOSTNAME'/g' /etc/hosts.new
fi

cat /etc/hosts.new > /etc/hosts
rm /etc/hosts.new