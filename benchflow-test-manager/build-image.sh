#/bin/bash
# Build and Configure the BenchFlow Experiment Manager Service

# TODO: remove, when the code become stable
set -xv

# copy service
cp target/benchflow-test-manager.jar /app/

# # copy service configuration
cp configuration.yml /app/

# # copy resources (experiment configurations)
cp -a services/300-test-manager.conf /apps/chaperone.d/300-test-manager.conf

cp -a services/envcp/config.tpl /app/config.tpl