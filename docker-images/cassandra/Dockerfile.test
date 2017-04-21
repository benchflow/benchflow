FROM cassandra:3.7

MAINTAINER Vincenzo FERME <info@vincenzoferme.it>

# create the directory that is going to accomodate the app (data and configuration)
RUN mkdir -p /app
RUN mkdir -p /app/data
# TODO: this is going to be moved. Refer to https://github.com/benchflow/docker-images/issues/15
COPY data/benchflow.cql /app/data/benchflow.cql

COPY bin/initialize-entrypoint.sh /app/initialize-entrypoint.sh
RUN chmod +x /app/initialize-entrypoint.sh

# copy custom configurations
COPY conf/cassandra.yaml /etc/cassandra/cassandra.yaml

ENTRYPOINT ["/app/initialize-entrypoint.sh"]

# it seems that if we override the entrypoint then wen also need to define again the default command
CMD ["cassandra","-f"]