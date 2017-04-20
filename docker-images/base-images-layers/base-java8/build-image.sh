#/bin/bash
# This is the base base image of all the images used in benchflow

# TODO: remove, when the code become stable
set -xv

# test:
# 	docker run -ti --rm -e "ENVCONSUL_CONSUL=demo.consul.io:80" $(NAME)_$(VERSION)

# Source: https://github.com/garywiz/chaperone-docker/blob/master/alpinejava/Dockerfile.tpl and https://github.com/anapsix/docker-alpine-java/blob/master/8/jdk/Dockerfile

cp ./services/020-java.conf /apps/chaperone.d/020-java.conf

# Based upon frolvlad/alpine-oraclejdk8 and many others.  Thank you all.
# Especially andyshinn, who made all this possible by solving the glibc
# issues (https://github.com/gliderlabs/docker-alpine/issues/11).

# Install Java requirements

JAVA_VERSION=8
JAVA_UPDATE=131
JAVA_BUILD=11
JAVA_HOME=/usr/lib/jvm/java${JAVA_VERSION}
GLIBC_VERSION=2.25-r0

mkdir -p /setup-java
TMP=/setup-java/tmp
mkdir -p $TMP

# Here we use several hacks collected from https://github.com/gliderlabs/docker-alpine/issues/11:
# 1. install GLibc (which is not the cleanest solution at all)
# 2. hotfix /etc/nsswitch.conf, which is apperently required by glibc and is not used in Alpine Linux

apk add --update wget ca-certificates

cd $TMP
wget --no-check-certificate --progress=dot:mega \
 "https://github.com/andyshinn/alpine-pkg-glibc/releases/download/${GLIBC_VERSION}/glibc-${GLIBC_VERSION}.apk" \
 "https://github.com/andyshinn/alpine-pkg-glibc/releases/download/${GLIBC_VERSION}/glibc-bin-${GLIBC_VERSION}.apk"

apk add --allow-untrusted glibc-${GLIBC_VERSION}.apk glibc-bin-${GLIBC_VERSION}.apk

/usr/glibc/usr/bin/ldconfig /lib /usr/glibc/usr/lib

echo 'hosts: files mdns4_minimal [NOTFOUND=return] dns mdns4' >> /etc/nsswitch.conf

wget --no-check-certificate --progress=dot:mega --header "Cookie: oraclelicense=accept-securebackup-cookie;" \
        "http://download.oracle.com/otn-pub/java/jdk/${JAVA_VERSION}u${JAVA_UPDATE}-b${JAVA_BUILD}/jdk-${JAVA_VERSION}u${JAVA_UPDATE}-linux-x64.tar.gz"

tar xzf "jdk-${JAVA_VERSION}u${JAVA_UPDATE}-linux-x64.tar.gz"

mkdir -p /usr/lib/jvm
mv "$TMP/jdk1.${JAVA_VERSION}.0_${JAVA_UPDATE}" $JAVA_HOME

ln -s $JAVA_HOME/bin/java /usr/bin/java
ln -s $JAVA_HOME/bin/javac /usr/bin/javac

# Clean up things we don't need
rm -rf $JAVA_HOME/*src.zip
rm -rf $JAVA_HOME/lib/missioncontrol \
       $JAVA_HOME/lib/visualvm \
       $JAVA_HOME/lib/*javafx* \
       $JAVA_HOME/jre/lib/plugin.jar \
       $JAVA_HOME/jre/lib/ext/jfxrt.jar \
       $JAVA_HOME/jre/bin/javaws \
       $JAVA_HOME/jre/lib/javaws.jar \
       $JAVA_HOME/jre/lib/desktop \
       $JAVA_HOME/jre/plugin \
       $JAVA_HOME/jre/lib/deploy* \
       $JAVA_HOME/jre/lib/*javafx* \
       $JAVA_HOME/jre/lib/*jfx* \
       $JAVA_HOME/jre/lib/amd64/libdecora_sse.so \
       $JAVA_HOME/jre/lib/amd64/libprism_*.so \
       $JAVA_HOME/jre/lib/amd64/libfxplugins.so \
       $JAVA_HOME/jre/lib/amd64/libglass.so \
       $JAVA_HOME/jre/lib/amd64/libgstreamer-lite.so \
       $JAVA_HOME/jre/lib/amd64/libjavafx*.so \
       $JAVA_HOME/jre/lib/amd64/libjfx*.so \
       $JAVA_HOME/jre/bin/jjs \
       $JAVA_HOME/jre/bin/keytool \
       $JAVA_HOME/jre/bin/orbd \
       $JAVA_HOME/jre/bin/pack200 \
       $JAVA_HOME/jre/bin/policytool \
       $JAVA_HOME/jre/bin/rmid \
       $JAVA_HOME/jre/bin/rmiregistry \
       $JAVA_HOME/jre/bin/servertool \
       $JAVA_HOME/jre/bin/tnameserv \
       $JAVA_HOME/jre/bin/unpack200 \
       $JAVA_HOME/jre/lib/ext/nashorn.jar \
       $JAVA_HOME/jre/lib/jfr.jar \
       $JAVA_HOME/jre/lib/jfr \
       $JAVA_HOME/jre/lib/oblique-fonts

apk del ca-certificates

# Do final cleanups
rm -rf /setup-java
rm -rf /tmp/* /var/tmp/* /var/cache/apk/*
rm -f `find /apps -name '*~'`

echo done.