#! /bin/sh
# Source: https://github.com/Zenithar/minio-server/blob/master/Dockerfile
# Already in initialize-entrypoint.sh
# chown -R minio:minio /benchflow
# /usr/bin/gosu minio /usr/bin/minio $@
minio $@