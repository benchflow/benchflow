#/bin/bash
# This is the base base image of all the images used in benchflow

# TODO: remove, when the code become stable
set -xv

# test:
# 	docker run -ti --rm -e "ENVCONSUL_CONSUL=demo.consul.io:80" \
# 	-e "ENVCONSUL_PREFIXES==\{path = \"minio\" format = \"MINIO_{{ key }}\"\},\{path = \"benchflow\" format = \"BENCHFLOW_{{ key }}\"\}" \
# 	$(NAME)_$(VERSION)

ENVCONSUL_VERSION_NUMBER=0.6.1

apk --update add wget bash unzip
wget -q --no-check-certificate -O /tmp/consul.zip https://releases.hashicorp.com/envconsul/${ENVCONSUL_VERSION_NUMBER}/envconsul_${ENVCONSUL_VERSION_NUMBER}_linux_amd64.zip
unzip -d /usr/bin/ /tmp/consul.zip
apk del --purge wget unzip
rm -rf /var/cache/apk/* /tmp/* /usr/bin/envconsul_${ENVCONSUL_VERSION_NUMBER}_linux_amd64/ /var/tmp/* *.zip 
  
ENVCONSUL_CONFIG=/envconsul
  
cp ./services/envconsul/config/envconsul-config.hcl ${ENVCONSUL_CONFIG}/envconsul-config.hcl
cp ./services/envconsul/configure.sh /envconsul/configure.sh
chmod +x /envconsul/configure.sh
cp ./services/envconsul/start.sh /envconsul/start.sh
chmod +x /envconsul/start.sh
cp ./services/envcp/update.sh /envcp/update.sh
chmod +x /envcp/update.sh

cp ./services/100-envconsul-configure.conf /apps/chaperone.d/100-envconsul-configure.conf
cp ./services/200-envconsul-envcp.conf /apps/chaperone.d/200-envconsul-envcp.conf

# copy the default configuration file in the folder where envcp expects it
# The name is config.tlp because it is not possible to use something like default.tlp
# because envcp uses rstrip() [https://github.com/garywiz/chaperone/blob/129514b525b4b8acf50ff01c5be827e4b31d7b01/chaperone/exec/envcp.py#L86] and "default.tpl".rstrip(".tpl") ==> 'defau'
cp ./config/config.tpl /app/config.tpl

# enable chaperone to work on the following directories
chown -R runapps: /envconsul