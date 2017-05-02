#!/bin/bash
set -e

BENCHFLOW_SERVERS_LIST_ALIASES=${BENCHFLOW_SERVERS_LIST_ALIASES}

#TODO: find a way to make possible to keep \n we have in this script, when used in a chaperone tpl file. Currenlty chaperone streeps away the \n.
#       For this reasone we use #=# (with = that is a character forbidden in ENV) so that then we can replace it with \n

while IFS=',' read -ra ADDR; do
      for i in "${ADDR[@]}"; do
      	  #Server name
      	  name="BENCHFLOW_SERVER_"${i^^}"_NAME"
          echo $name": \""${!name}"\"#=#"

          #Server public IP
          public_ip="BENCHFLOW_SERVER_"${i^^}"_PUBLICIP"
          echo $public_ip": \""${!public_ip}"\"#=#"

          #Server private IP
          private_ip="BENCHFLOW_SERVER_"${i^^}"_PRIVATEIP"
          echo $private_ip": \""${!private_ip}"\"#=#"

          #Server hostname
          hostname="BENCHFLOW_SERVER_"${i^^}"_HOSTNAME"
          echo $hostname": \""${!hostname}"\"#=#"

          #Server tags
          tags="BENCHFLOW_SERVER_"${i^^}"_TAGS"
          echo $tags": ["${!tags}"]#=#"
      done
done <<< "$BENCHFLOW_SERVERS_LIST_ALIASES"