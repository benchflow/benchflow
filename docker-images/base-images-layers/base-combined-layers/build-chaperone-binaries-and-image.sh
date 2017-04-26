#/bin/bash
# Combine the build of Chaperone binaries and Chaperone Image


# TODO: remove, when the code become stable
set -xv

# Set the working folder, so we can refer to files in that context (here so that we can quickly copy the code in other pipelines)
cd $WERCKER_SOURCE_DIR/docker-images/base-images-layers/chaperone/setup
# Build the Chaperone Binaries
source create-binaries.sh
          
# Add the Base Image Chaperone Layer
cd $WERCKER_SOURCE_DIR/docker-images/base-images-layers/chaperone/setup
# Binaries folder ($WERCKER_OUTPUT_DIR)
export BINARIES_FOLDER=$WERCKER_SOURCE_DIR/docker-images/base-images-layers/chaperone/setup/lib
source install.sh