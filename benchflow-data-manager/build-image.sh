#/bin/bash
# Build and Configure the BenchFlow Data Manager Service

# TODO: remove, when the code become stable
set -xv

# copy service
cp target/benchflow-data-manager.jar /app/

# # copy service configuration
cp configuration.yml /app/

# # copy resources (experiment configurations)
cp -a services/300-data-manager.conf /apps/chaperone.d/300-data-manager.conf

cp -a services/envcp/config.tpl /app/config.tpl