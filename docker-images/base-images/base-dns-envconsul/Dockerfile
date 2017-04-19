FROM benchflow/base-images:dns_dev

MAINTAINER Vincenzo FERME <info@vincenzoferme.it>

ENV ENVCONSUL_VERSION_NUMBER 0.6.1

RUN apk --update add wget bash unzip && \
    wget -q --no-check-certificate -O /tmp/consul.zip https://releases.hashicorp.com/envconsul/${ENVCONSUL_VERSION_NUMBER}/envconsul_${ENVCONSUL_VERSION_NUMBER}_linux_amd64.zip && \
    unzip -d /usr/bin/ /tmp/consul.zip && \
    apk del --purge wget unzip && \
    rm -rf /var/cache/apk/* /tmp/* /usr/bin/envconsul_${ENVCONSUL_VERSION_NUMBER}_linux_amd64/ /var/tmp/* *.zip 
  
ENV ENVCONSUL_CONFIG /envconsul
  
COPY ./services/envconsul/config/envconsul-config.hcl ${ENVCONSUL_CONFIG}/envconsul-config.hcl
COPY ./services/envconsul/configure.sh /envconsul/configure.sh
RUN chmod +x /envconsul/configure.sh
COPY ./services/envconsul/start.sh /envconsul/start.sh
RUN chmod +x /envconsul/start.sh
COPY ./services/envcp/update.sh /envcp/update.sh
RUN chmod +x /envcp/update.sh

COPY ./services/100-envconsul-configure.conf /apps/chaperone.d/100-envconsul-configure.conf
COPY ./services/200-envconsul-envcp.conf /apps/chaperone.d/200-envconsul-envcp.conf

# copy the default configuration file in the folder where envcp expects it
# The name is config.tlp because it is not possible to use something like default.tlp
# because envcp uses rstrip() [https://github.com/garywiz/chaperone/blob/129514b525b4b8acf50ff01c5be827e4b31d7b01/chaperone/exec/envcp.py#L86] and "default.tpl".rstrip(".tpl") ==> 'defau'
COPY ./config/config.tpl /app/config.tpl

# enable chaperone to work on the following directories
RUN chown -R runapps: /envconsul