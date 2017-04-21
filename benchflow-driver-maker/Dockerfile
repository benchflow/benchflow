FROM benchflow/base-images:dns-envconsul-java8_dev

MAINTAINER Vincenzo FERME <info@vincenzoferme.it>

ENV JAVA_HOME /usr/lib/jvm/java8

ENV FABAN_DRIVERS_MAKER_VERSION v-dev
ENV RELEASE_VERSION v-dev
ENV FABAN_VERSION dev
ENV FABAN_ROOT /app/
ENV FABAN_HOME ${FABAN_ROOT}faban

ENV CLIENT_VERSION v-dev
ENV SUT_LIBRARIES_VERSION v-dev
ENV SUT_PLUGINS_VERSION v-dev

ENV GENERATION_RESOURCES_ROOT /app/drivers
ENV LIBRARIES_ROOT ${GENERATION_RESOURCES_ROOT}/libraries
ENV PLUGINS_ROOT ${GENERATION_RESOURCES_ROOT}/plugins
ENV TEMPLATES_ROOT ${GENERATION_RESOURCES_ROOT}/templates
ENV BENCHFLOW_SERVICES_ROOT /app/benchflow-services
ENV BENCHFLOW_COLLECTORS_ROOT ${BENCHFLOW_SERVICES_ROOT}/collectors
ENV BENCHFLOW_MONITORS_ROOT ${BENCHFLOW_SERVICES_ROOT}/monitors

# Get benchflow-drivers-maker
RUN wget -q --no-check-certificate -O /app/benchflow-drivers-maker.jar https://github.com/benchflow/drivers-maker/releases/download/$FABAN_DRIVERS_MAKER_VERSION/benchflow-drivers-maker.jar

COPY configuration.yml /app/

# copy driver skeleton
COPY ./application/src/main/resources/app/drivers/templates /app/drivers/templates/

# copy cloud.benchflow.driversmaker.generation package classes
COPY ./application/src/main/java/cloud/benchflow/driversmaker/generation /app/application/src/main/java/cloud/benchflow/driversmaker/generation/

# Get benchflow-drivers-maker dependencies
RUN apk --update add wget tar && \
	wget -q --no-check-certificate -O /tmp/faban.tar.gz https://github.com/benchflow/faban/releases/download/${RELEASE_VERSION}/faban-kit-${FABAN_VERSION}.tar.gz && \
	mkdir -p ${FABAN_ROOT}/ && \
	tar -xzvf /tmp/faban.tar.gz -C ${FABAN_ROOT}/ && \
	# Unpack faban master
	mkdir -p ${FABAN_HOME}/master/webapps/faban && \
	cd ${FABAN_HOME}/master/webapps/faban && \
	JAVA=`readlink -f $(which java) | sed 's|/bin/java$||'` && \
	# Unjar faban.war
	$JAVA/bin/jar xvf ../faban.war && \
	# Remove unused Faban assets
	rm -f ${FABAN_HOME}/*.* && \
	rm -rf ${FABAN_HOME}/benchmarks ${FABAN_HOME}/bin ${FABAN_HOME}/config ${FABAN_HOME}/legal \
	       ${FABAN_HOME}/logs ${FABAN_HOME}/output ${FABAN_HOME}/resources ${FABAN_HOME}/samples ${FABAN_HOME}/services && \
	find ${FABAN_HOME}/master/ -not -path "/app/faban/master/" -not -path "${FABAN_HOME}/master/webapps" -not -path "${FABAN_HOME}/master/webapps/faban" -not -path "${FABAN_HOME}/master/webapps/faban/WEB-INF"  -not -path "${FABAN_HOME}/master/webapps/faban/WEB-INF/classes*" | xargs rm -rf && \
    # Get sut-libraries
    mkdir -p ${LIBRARIES_ROOT} && \
    wget -q --no-check-certificate -O - https://github.com/benchflow/sut-libraries/archive/$SUT_LIBRARIES_VERSION.tar.gz \
    | tar xz --strip-components=1 -C ${LIBRARIES_ROOT} sut-libraries-$SUT_LIBRARIES_VERSION && \
    # Get sut-plugins
    mkdir -p ${PLUGINS_ROOT} && \
    wget -q --no-check-certificate -O - https://github.com/benchflow/sut-plugins/archive/$SUT_PLUGINS_VERSION.tar.gz \
    | tar xz --strip-components=1 -C ${PLUGINS_ROOT} sut-plugins-$SUT_PLUGINS_VERSION && \
    # Download monitors library into driver skeleton
    wget -q --no-check-certificate -O ${TEMPLATES_ROOT}/skeleton/benchmark/lib/benchflow-monitors-driver-library.jar \
    http://github.com/simonedavico/monitors/releases/download/${RELEASE_VERSION}/benchflow-monitors-driver-library.jar && \
    cp ${TEMPLATES_ROOT}/skeleton/benchmark/lib/benchflow-monitors-driver-library.jar ${TEMPLATES_ROOT}/skeleton/benchmark/build/lib/ && \
    # Download monitors release, extract deployment descriptors, move them to the right place, and delete the rest
    mkdir -p /tmp/monitors-deployment-descriptors && \
    mkdir -p ${BENCHFLOW_MONITORS_ROOT} && \
    wget -q --no-check-certificate -O /tmp/monitors-deployment-descriptors/v-dev.tar.gz https://github.com/benchflow/monitors/archive/v-dev.tar.gz && \
    tar -xzf /tmp/monitors-deployment-descriptors/v-dev.tar.gz -C /tmp/monitors-deployment-descriptors/ --wildcards --no-anchored '*.monitor.yml' && \
    find /tmp/monitors-deployment-descriptors/ -name '*.monitor.yml' -type f -exec mv -i {} ${BENCHFLOW_MONITORS_ROOT} \; && \
    rm -rf /tmp/monitors-deployment-descriptors/ && \
    # Download collectors release, extract deployment descriptors, move them to the right place, and delete the rest
    mkdir -p /tmp/collectors-deployment-descriptors && \
    mkdir -p ${BENCHFLOW_COLLECTORS_ROOT} && \
    wget -q --no-check-certificate -O /tmp/collectors-deployment-descriptors/v-dev.tar.gz https://github.com/benchflow/collectors/archive/v-dev.tar.gz && \
    tar -xzf /tmp/collectors-deployment-descriptors/v-dev.tar.gz -C /tmp/collectors-deployment-descriptors/ --wildcards --no-anchored '*.collector.yml' && \
    find /tmp/collectors-deployment-descriptors/ -name '*.collector.yml' -type f -exec mv -i {} ${BENCHFLOW_COLLECTORS_ROOT} \; && \
    rm -rf /tmp/collectors-deployment-descriptors/ && \
    # Cleanup
	apk del --purge tar && \
    rm -rf /var/cache/apk/* /tmp/* /var/tmp/* *.gz


# Install Ant (Source: https://hub.docker.com/r/webratio/ant/~/dockerfile/)
ENV ANT_VERSION 1.9.4
RUN mkdir -p /opt/ant && \
    wget -q --no-check-certificate -O /apache-ant-${ANT_VERSION}-bin.tar.gz http://archive.apache.org/dist/ant/binaries/apache-ant-${ANT_VERSION}-bin.tar.gz && \
    tar -xzf /apache-ant-${ANT_VERSION}-bin.tar.gz && \
    mv /apache-ant-${ANT_VERSION} /opt/ant && \
    rm /apache-ant-${ANT_VERSION}-bin.tar.gz && \
    apk del --purge wget
ENV ANT_HOME /opt/ant
ENV PATH ${PATH}:/opt/ant/bin


COPY ./services/envcp/config.tpl /app/config.tpl
COPY ./services/envcp/add_servers_info.sh /app/add_servers_info.sh
RUN chmod +x /app/add_servers_info.sh

COPY ./services/300-drivers-maker.conf /apps/chaperone.d/300-drivers-maker.conf

EXPOSE 8080
