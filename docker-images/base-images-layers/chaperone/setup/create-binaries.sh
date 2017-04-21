#!/bin/bash

# It runs a separate container, builds the binary packages, then includees them in a root-extractable
# bundle.  Since this is the same architecture as used by the image build, all should be compatible.

# Obtain UID of the mounted volume so we don't copy as root
# uid=`ls -l / | awk '/setup$/{print $3}'`
# adduser -D -u $uid -s /bin/sh usetup


# When integrated in Wercker, we can use the following instead:
# Install all development tools...
    # See: https://github.com/ytet5uy4/step-apk-add
    # - ytet5uy4/apk-add@0.1.2:
    #     name: Install all development tools
    #     update: "true"
    #     cache: "true"
    #     packages: python3-dev gcc bash git musl-dev
echo Install all development tools...
apk add --update python3-dev gcc bash git musl-dev

# Output directory root (it is the directory where we cwd on the Wercker step calling this script)
output=$(pwd)

# Bit of a hack to make sure prctl.h is found under alpine
cd /usr/include
mkdir linux
cd linux
ln -s /usr/include/sys/prctl.h

echo Build binary versions of needed modules...
mkdir /build
git clone https://github.com/dvarrazzo/py-setproctitle.git
cd py-setproctitle
python3 setup.py bdist

echo Copy them to our shared mount bin...
cd dist
# su usetup -c 'mkdir -p /setup/lib; cp -v setproctitle-*.gz /setup/lib/setproctitle-install.tar.gz'
[ -d $output/lib ] && rm -Rf $output/lib
mkdir -p $output/lib
cp -v setproctitle-*.gz $output/lib/setproctitle-install.tar.gz
