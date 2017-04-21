#!/bin/bash
set -e

#TODO: find a way to make possible to keep \n in envcp when using a script in the tpl file. Currenlty chaperone streeps away the \n.
#       For this reasone we use "#=#" or "#=# " (with = that is a character forbidden in ENV) so that then we can replace it with \n
/usr/local/bin/envcp -v -a --shell-enable - < /app/config.tpl | sed -e $'s/#=#\( \)\{0,1\}/\\n/g' > /app/config.yml
echo -e "INFO ["`date "+%Y-%m-%d %H:%M:%S"`"] envcp: Configuration updated from Consul"
# avoid the process to die, so that it can monitored by envconsul
trap 'exit 1' SIGINT SIGQUIT SIGTERM
while :; do
  sleep 1m
done