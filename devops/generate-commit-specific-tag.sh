#/bin/bash
# Generate the tag used to deploy commit specific version

# TODO: remove, when the code become stable
set -xv	

export CUSTOM_VERSION_TAG=${WERCKER_GIT_BRANCH}"_"${WERCKER_GIT_COMMIT:0:6}