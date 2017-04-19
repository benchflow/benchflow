#/bin/bash
# Cleanup Wercker build related code so that the built image is ready to be delivered
# on the Docker Registry

# TODO: remove, when the code become stable
set -xv	

# Determine if we are in a CI environment (e.g., the Wercker Cloud) or not
if [ "$CI" = true ]; then
	# Reference for the - before rm: https://superuser.com/a/523510
	-rm -Rf /report /pipeline
else
	echo 'Skipping step, because not needed for local execution'
fi