#/bin/bash
# Combine the build of Chaperone binaries and Chaperone Image and Base Layer


# TODO: remove, when the code become stable
set -xv

# Combine the build of Chaperone binaries and Chaperone Image
source build-chaperone-binaries-and-image.sh

# Add the Base Image Base Layer
cd $WERCKER_SOURCE_DIR/docker-images/base-images-layers/base
source build-image.sh