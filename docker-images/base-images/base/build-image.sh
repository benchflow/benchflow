#/bin/bash
# This is the base base image of all the images used in benchflow

# TODO: remove, when the code become stable
set -xv	

# Remove sample application from chaperone.d
rm /apps/chaperone.d/200-userapp.conf
rm /apps/bin/sample_app

# create the directory that is going to accomodate the app
mkdir -p /app
# enable chaperone to work on the following directories
chown -R runapps: /apps /app