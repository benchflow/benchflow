FROM benchflow/base-images:dns-envconsul-java8_dev

MAINTAINER Vincenzo FERME <info@vincenzoferme.it>

ENV DATA_MANAGER_VERSION v-dev

# Get benchflow-data-manager
RUN wget -q --no-check-certificate -O /app/benchflow-data-manager.jar https://github.com/benchflow/data-manager/releases/download/$DATA_MANAGER_VERSION/benchflow-data-manager.jar

COPY ./conf/prod.conf /app/

COPY ./services/300-data-manager.conf /apps/chaperone.d/300-data-manager.conf

EXPOSE 8080