FROM benchflow/envconsul:v0.6.0_serverjre-8

MAINTAINER Vincenzo FERME <info@vincenzoferme.it>

ENV BENCHFLOW_EXPERIMENTS_MANAGER_VERSION 0.0.1
ENV MAVEN_VERSION 3.3.3

#Install maven
RUN curl -sSL http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share && \
    mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven && \
    ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

#TODO: complete it by adding the actual service (both using local folder and the git repository)

#Using local folder
COPY . /var/tmp/dropwizard/

WORKDIR /var/tmp/dropwizard/

#Build the project  

#TODO cache the packages: https://keyholesoftware.com/2015/01/05/caching-for-maven-docker-builds/
#RUN mvn verify clean --fail-never -Dmaven.test.skip=true -DskipTests
#TODO: enable the tests when they are ready to be executed correclty
RUN mvn package -Dmaven.test.skip=true -DskipTests

RUN mkdir -p /opt/dropwizard

#Export the builded project  
RUN cp ./configuration.yml /opt/dropwizard/ && \
	cp ./target/benchflow-experiments-manager-${BENCHFLOW_EXPERIMENTS_MANAGER_VERSION}.jar /opt/dropwizard/benchflow-experiments-manager.jar  

#TODO: understand why I get - rm: can't remove '/var/tmp/dropwizard/api': Directory not empty, rm: can't remove '/var/tmp/dropwizard': Directory not empty 
#Then enable: /var/tmp/*
#Clean up
RUN apk del --purge curl && \
	mvn clean && \
    rm -Rf /var/cache/apk/* *.gz /usr/share/maven /usr/bin/mvn

WORKDIR /opt/dropwizard

EXPOSE 8080

CMD ["java", "-jar", "-Done-jar.silent=true", "benchflow-experiments-manager.jar", "server", "configuration.yml"]