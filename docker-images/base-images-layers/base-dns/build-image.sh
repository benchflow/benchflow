#/bin/bash
# This is the base base image of all the images used in benchflow

# TODO: remove, when the code become stable
set -xv

# TODO: move the log file in the chaperone apps directory, so that it is handled there
# and also exposed on the log
# Solve the DNS resolution problem of Alpine, when the contaienr is run on infrastructures like Tutum
#  - Overview of the problem: https://github.com/smebberson/docker-alpine/tree/master/alpine-base#dns
#  - Some more details about the problem: https://github.com/sillelien/base-alpine#dns-and-the-alpine-resolvconf-problem
# Source of the fix: https://github.com/smebberson/docker-alpine/blob/master/alpine-base/Dockerfile
# Add commonly used packages
GODNSMASQ_VERSION=1.0.7 GO_DNSMASQ_LOG_FILE="/var/log/go-dnsmasq/go-dnsmasq.log"

apk add --update bind-tools
rm -rf /var/cache/apk/*

apk add --update curl
curl -sSL https://github.com/janeczku/go-dnsmasq/releases/download/${GODNSMASQ_VERSION}/go-dnsmasq-min_linux-amd64 -o /bin/go-dnsmasq
chmod +x /bin/go-dnsmasq
mkdir -p `dirname $GO_DNSMASQ_LOG_FILE`
apk del curl
rm -rf /var/cache/apk/*

cp -a services/010-resolver.conf /apps/chaperone.d/010-resolver.conf