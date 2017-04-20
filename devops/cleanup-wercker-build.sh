#/bin/bash
# Cleanup Wercker build related code so that the built image is ready to be delivered
# on the Docker Registry

# TODO: remove, when the code become stable
set -xv	

# Determine if we are in a CI environment (for now the Wercker Cloud) or not
# Although Wercker suggests to use the CI or WERCKER variables (http://devcenter.wercker.com/docs/environment-variables/available-env-vars)
# , those are true also locally, somehow. WERCKER_STARTED_BY is only set in the Wercker Cloud
if [ -z ${WERCKER_STARTED_BY+x} ]; then
	# Reference for the - before rm: https://superuser.com/a/523510 (It does not work)
	rm -Rf /report /pipeline
else
	echo 'Skipping step, because not needed for local execution'
fi