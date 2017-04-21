FROM spotify/kafka:latest

MAINTAINER Vincenzo FERME <info@vincenzoferme.it>


COPY bin/initialize-entrypoint.sh /initialize-entrypoint.sh
RUN chmod +x /initialize-entrypoint.sh

# Supervisor config
ADD supervisor/kafka-init.conf /etc/supervisor/conf.d/