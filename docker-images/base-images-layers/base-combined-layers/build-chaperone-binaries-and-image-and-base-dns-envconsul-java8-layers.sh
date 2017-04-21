#/bin/bash
# Combine the build of Chaperone binaries and Chaperone Image and Base + DNS + EnvConsul + Java8 Layers


# TODO: remove, when the code become stable
set -xv

# Combine the build of Chaperone binaries and Chaperone Image and Base Layer
source build-chaperone-binaries-and-image-and-base-layer.sh

# Add the Base Image Base DNS Layer
cd $WERCKER_SOURCE_DIR/docker-images/base-images-layers/base-dns
source build-image.sh

# Add the Base Image Base Envconsul Layer
cd $WERCKER_SOURCE_DIR/docker-images/base-images-layers/base-envconsul
source build-image.sh

# Add the Base Image Base Java8 Layer
cd $WERCKER_SOURCE_DIR/docker-images/base-images-layers/base-java8
source build-image.sh