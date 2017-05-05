#/bin/bash
# Build and Configure the BenchFlow Experiment Manager Service

# TODO: remove, when the code become stable
set -xv

CLIENT_HOME=/app
# if this is called "PIP_VERSION", pip explodes with "ValueError: invalid truth value '<VERSION>'"
PYTHON_PIP_VERSION=9.0.1

#TODO: keep it updated while changing the client
#Installing client dependencies
# Install pip (Source: https://github.com/docker-library/python/blob/12db3f7b07f9704719657a0652357a3ae4cdc1c1/2.7/alpine/Dockerfile)
apk --update add --no-cache python3 zip make curl bash wget ca-certificates
curl -fSL 'https://bootstrap.pypa.io/get-pip.py' | python3
pip install --no-cache-dir --upgrade pip==$PYTHON_PIP_VERSION
rm -rf /var/cache/apk/*

#TODO: keep it updated while changing the client
mkdir $CLIENT_HOME

#Copy BenchFlow
cp benchflow.py $CLIENT_HOME/benchflow.py
cp setup.py $CLIENT_HOME/setup.py

# Python application dependencies
pip3 install $CLIENT_HOME

# Clean up
apk del --purge curl
rm -rf /var/cache/apk/*
rm -rf $CLIENT_HOME/setup.py