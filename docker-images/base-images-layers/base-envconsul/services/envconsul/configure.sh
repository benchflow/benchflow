#!/bin/bash
set -e

# Exit if the required ENVCONSUL_CONSUL it is not defined
if [ -z "$ENVCONSUL_CONSUL" ]; then
    echo "Need to set ENVCONSUL_CONSUL"
    exit 1
fi  

# Configure envconsul using environment variables

for hcl in \
		sanitize \
		upcase \
		pristine \
; do
		var="ENVCONSUL_${hcl^^}"
		val="${!var}"
		
		if [ "$val" ]; then
			sed -ri 's/^(# )?('"$hcl"' =).*/\2 '"$val"'/' "$ENVCONSUL_CONFIG/envconsul-config.hcl"
		fi
done

for hcl in \
		max_stale \
		token \
		wait \
		splay \
		kill_signal \
		retry \
		log_level \
; do
		var="ENVCONSUL_${hcl^^}"
		val="${!var}"
		
		if [ "$val" ]; then
			sed -ri 's/^(# )?('"$hcl"' =).*/\2 '\""$val"\"'/' "$ENVCONSUL_CONFIG/envconsul-config.hcl"
		fi
done

#Handle prefixes
#Get the environment variable specifying the prefixes
var="ENVCONSUL_PREFIXES"
val="${!var}"

numIteration=0

if [ "$val" ]; then
	#Iterate over the array of prefixes splitting by the , separator

	set -f # turn off globbing
	IFS=$','

	for prefix in $val
	do

		#Replace the default prefix with the first one specified in the enviornment variable
		if (( $numIteration == 0 )) ; then
		    sed -ri 's/^(# )?('"prefix"').*/\2 '"$prefix"'/' "$ENVCONSUL_CONFIG/envconsul-config.hcl"
		#Append the other prefixes to the configuration file
		else
			prefix="$(echo $prefix | sed "s@\\\\@@g")"
		    echo -e "\nprefix =" $prefix >> $ENVCONSUL_CONFIG/envconsul-config.hcl
		fi

		numIteration=$((numIteration+1))

	done

	unset IFS
	set +f

fi
