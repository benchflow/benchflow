FROM benchflow/base-images:dns-envconsul-java8_dev

MAINTAINER Vincenzo FERME <info@vincenzoferme.it>

ENV DATA_MANAGER_VERSION dev

ADD ./target/universal/data-manager-${DATA_MANAGER_VERSION}.tgz /app/

COPY ./services/300-data-manager.conf /apps/chaperone.d/300-data-manager.conf

EXPOSE 8080
