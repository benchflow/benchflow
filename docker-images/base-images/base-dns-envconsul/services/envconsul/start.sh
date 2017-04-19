#!/bin/bash
set -e

# Handle the once flag (the empty string keeps the default one).
once=""
if [ "$ENVCONSUL_ONCE" ]; then
    var="ENVCONSUL_ONCE"
	val="${!var}"
	if [ "$val" = true ] ; then
		once="-once"
	fi
fi  

exec /usr/bin/envconsul -consul=${ENVCONSUL_CONSUL} -config=$ENVCONSUL_CONFIG/envconsul-config.hcl $once "$@"