#/bin/bash
# Combine the build of Chaperone binaries and Chaperone Image and Base + DNS Layers


# TODO: remove, when the code become stable
set -xv

# Combine the build of Chaperone binaries and Chaperone Image and Base Layer
source build-chaperone-binaries-and-image-and-base-layer.sh

# Add the Base Image Base DNS Layer
cd $WERCKER_SOURCE_DIR/docker-images/base-images-layers/base-dns
source build-image.sh