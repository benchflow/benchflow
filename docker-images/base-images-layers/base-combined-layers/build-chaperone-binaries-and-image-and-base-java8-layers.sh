#/bin/bash
# Combine the build of Chaperone binaries and Chaperone Image and Base + Java8 Layers


# TODO: remove, when the code become stable
set -xv

# Combine the build of Chaperone binaries and Chaperone Image and Base Layer
cd $WERCKER_SOURCE_DIR/docker-images/base-images-layers/base-combined-layers/
source build-chaperone-binaries-and-image-and-base-layer.sh

# Add the Base Image Base Java8 Layer
cd $WERCKER_SOURCE_DIR/docker-images/base-images-layers/base-java8
source build-image.sh