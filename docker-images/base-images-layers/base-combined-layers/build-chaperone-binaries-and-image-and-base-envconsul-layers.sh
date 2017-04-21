#/bin/bash
# Combine the build of Chaperone binaries and Chaperone Image and Base + EnvConsul Layers


# TODO: remove, when the code become stable
set -xv

# Combine the build of Chaperone binaries and Chaperone Image and Base Layer
cd $WERCKER_SOURCE_DIR/docker-images/base-images-layers/base-combined-layers/
source build-chaperone-binaries-and-image-and-base-layer.sh

# Add the Base Image Base Envconsul Layer
cd $WERCKER_SOURCE_DIR/docker-images/base-images-layers/base-envconsul
source build-image.sh