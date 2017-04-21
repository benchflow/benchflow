FROM benchflow/base-images:dns-envconsul_dev

MAINTAINER Vincenzo FERME <info@vincenzoferme.it>

# Source: https://github.com/garywiz/chaperone-docker/blob/master/alpinejava/Dockerfile.tpl and https://github.com/anapsix/docker-alpine-java/blob/master/7/jdk/Dockerfile

COPY ./services/java/install.sh /setup-java/install.sh
RUN chmod +x /setup-java/install.sh
RUN /setup-java/install.sh