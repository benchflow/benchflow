#/bin/bash
# This is the base base image of all the images used in benchflow

# TODO: remove, when the code become stable
set -xv

# test:
# 	docker run -ti --rm -e "ENVCONSUL_CONSUL=demo.consul.io:80" $(NAME)_$(VERSION)

# Source: https://github.com/garywiz/chaperone-docker/blob/master/alpinejava/Dockerfile.tpl and https://github.com/anapsix/docker-alpine-java/blob/master/8/jdk/Dockerfile

cp ./services/java/install.sh /setup-java/install.sh
chmod +x /setup-java/install.sh
./setup-java/install.sh