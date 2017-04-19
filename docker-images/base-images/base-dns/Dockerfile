FROM benchflow/base-images:dev

MAINTAINER Vincenzo FERME <info@vincenzoferme.it>

# Solve the DNS resolution problem of Alpine, when the contaienr is run on infrastructures like Tutum
#  - Overview of the problem: https://github.com/smebberson/docker-alpine/tree/master/alpine-base#dns
#  - Some more details about the problem: https://github.com/sillelien/base-alpine#dns-and-the-alpine-resolvconf-problem
# Source of the fix: https://github.com/smebberson/docker-alpine/blob/master/alpine-base/Dockerfile
# Add commonly used packages
ENV GODNSMASQ_VERSION=0.9.8 GO_DNSMASQ_LOG_FILE="/var/log/go-dnsmasq/go-dnsmasq.log"

RUN apk add --update bind-tools && \
    rm -rf /var/cache/apk/*

RUN apk add --update curl && \
    curl -sSL https://github.com/janeczku/go-dnsmasq/releases/download/${GODNSMASQ_VERSION}/go-dnsmasq-min_linux-amd64 -o /bin/go-dnsmasq && \
    chmod +x /bin/go-dnsmasq && \
    mkdir -p `dirname $GO_DNSMASQ_LOG_FILE` && \
    apk del curl && \
    rm -rf /var/cache/apk/*

COPY ./services/010-resolver.conf /apps/chaperone.d/010-resolver.conf