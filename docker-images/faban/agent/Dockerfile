FROM benchflow/base-images:dns-java7_dev

MAINTAINER Vincenzo FERME <info@vincenzoferme.it>

ENV RELEASE_VERSION v-dev
ENV FABAN_VERSION dev
#Faban will be installed in the ${FABAN_ROOT}/faban directory
ENV FABAN_ROOT /app/
ENV FABAN_HOME ${FABAN_ROOT}faban

#Add faban and its dependencies (procps)
RUN apk --update add wget procps && \
	wget -q --no-check-certificate -O /tmp/faban.tar.gz https://github.com/benchflow/faban/releases/download/${RELEASE_VERSION}/faban-agent-${FABAN_VERSION}.tar.gz && \
	mkdir -p ${FABAN_ROOT}/ && \
	tar -xzvf /tmp/faban.tar.gz -C ${FABAN_ROOT}/ && \
	apk del --purge wget && \
    rm -rf /var/cache/apk/* /tmp/* /var/tmp/* *.gz

# Setup tools and paths of system's executables as expected by Faban
RUN ln -s /usr/bin/awk /bin/awk
    
#Update the startup script to keep the Faban Agent in foreground
COPY ./bin/agent ${FABAN_HOME}/bin/agent
RUN chmod +x ${FABAN_HOME}/bin/agent

#TEST: remove, enable FINE log level
COPY ./config/logging.properties ${FABAN_HOME}/config/logging.properties

COPY ./services/fix_localhost/fix_localhost.sh /fix_localhost.sh
RUN chmod +x /fix_localhost.sh

COPY ./services/005-fix-localhost.conf /apps/chaperone.d/005-fix-localhost.conf
COPY ./services/300-faban-agent.conf /apps/chaperone.d/300-faban-agent.conf

#TODO: decide which of the following volumes and ports are necessary for the agent, and disable the ones that are not

#Tomcat logs
VOLUME ${FABAN_HOME}/master/logs
#Tomcat conf
VOLUME ${FABAN_HOME}/master/conf
#Faban output
VOLUME ${FABAN_HOME}/output
#Faban benchmarks
VOLUME ${FABAN_HOME}/benchmarks
#Faban config
VOLUME ${FABAN_HOME}/config
#Faban logs
VOLUME ${FABAN_HOME}/logs
#Faban services
VOLUME ${FABAN_HOME}/services

#Faban uses ports in the 9980 - 9999 range. Currently these are the list of ports and their functionality:
#9980 - Faban HTTP server
EXPOSE 9980
#9981 - Agent bootstrap
EXPOSE 9981
#9984 - Faban HTTP server shutdown
EXPOSE 9984
#9985 - Faban HTTP server coyote connector
EXPOSE 9985
#9988 - Runtime stats (currently not enabled by Faban)
#9998 - RMI registry
EXPOSE 9998
#9999 - Faban logging
EXPOSE 9999
