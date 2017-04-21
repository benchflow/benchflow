#!/bin/bash
set -e

# escaping forward slash for sed
MINIO_ACCESS_KEY_ESCAPED=$(echo ${MINIO_ACCESS_KEY//\//\\\/})
MINIO_SECRET_KEY_ESCAPED=$(echo ${MINIO_SECRET_KEY//\//\\\/})

sed -i 's/$MINIO_ACCESS_KEY/'${MINIO_ACCESS_KEY_ESCAPED}'/' "$MINIO_SERVER_CONF_FOLDER/config.json"
sed -i 's/$MINIO_SECRET_KEY/'${MINIO_SECRET_KEY_ESCAPED}'/' "$MINIO_SERVER_CONF_FOLDER/config.json"
sed -i 's/$MINIO_ACCESS_KEY/'${MINIO_ACCESS_KEY_ESCAPED}'/' "$MINIO_CLIENT_CONF_FOLDER/config.json"
sed -i 's/$MINIO_SECRET_KEY/'${MINIO_SECRET_KEY_ESCAPED}'/' "$MINIO_CLIENT_CONF_FOLDER/config.json"

# execute the initialize-entrypoint in the context of this shell
source /initialize-entrypoint.sh $@