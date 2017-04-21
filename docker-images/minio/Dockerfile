# Source: https://github.com/Zenithar/minio-server/blob/master/Dockerfile
# Updated to get a more recent Minio version
# TODO, if not https://github.com/benchflow/docker-images/issues/13, then optimize the image and refer to the same release of alpine as the other images
FROM sdurrheimer/alpine-glibc

MAINTAINER Vincenzo FERME <info@vincenzoferme.it>

ENV MINIO_SERVER_VERSION RELEASE.2016-07-13T21-46-05Z
ENV MINIO_CLIENT_VERSION RELEASE.2016-07-13T22-00-39Z
# ENV GOSU_VERSION 1.9
ENV MINIO_SERVER_CONF_FOLDER /home/minio/.minio
ENV MINIO_CLIENT_CONF_FOLDER /root/.mc/

# Source https://hub.docker.com/r/minio/minio/~/dockerfile/
ENV ALLOW_CONTAINER_ROOT=1

# TODO, if not https://github.com/benchflow/docker-images/issues/13, then optimize the image
ADD https://dl.minio.io/server/minio/release/linux-amd64/archive/minio.${MINIO_SERVER_VERSION} /usr/bin/minio
# ADD https://github.com/tianon/gosu/releases/download/${GOSU_VERSION}/gosu-amd64 /usr/bin/gosu
COPY bin/entrypoint.sh .

#TODO: at some point we should optimize this, now we are just adding bash for more easly use the scripts.
#      the same applies to sed, wget
#TODO: cleanup
RUN apk --update add bash sed wget

RUN chmod +x /usr/bin/minio \
    # && chmod +x /usr/bin/gosu \
    && chmod +x /entrypoint.sh \
    && addgroup minio \
    && adduser -s /bin/false -G minio -S -D minio

COPY config/minio_server_config.json $MINIO_SERVER_CONF_FOLDER/config.json

COPY config/minio_client_config.json $MINIO_CLIENT_CONF_FOLDER/config.json

COPY bin/docker-entrypoint.sh /docker-entrypoint.sh

COPY bin/initialize-entrypoint.sh /initialize-entrypoint.sh

RUN chmod +x /docker-entrypoint.sh /initialize-entrypoint.sh

VOLUME      ["/benchflow"]

ENTRYPOINT  ["/docker-entrypoint.sh"]
EXPOSE      9000
CMD         ["server","/benchflow"]