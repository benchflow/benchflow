#/bin/bash
# Build and Configure the BenchFlow Experiment Manager Service

# TODO: remove, when the code become stable
set -xv

cat >/usr/local/bin/env <<EOL
if [ "$#" -eq 0 ]; then
  /usr/bin/env
elif [ "$1" == "--null" ]; then
  /usr/bin/env | tr '\n' '\000'
else
  /usr/bin/env "$@"
fi
EOL

# copy service 
cp target/benchflow-experiment-manager.jar /app/

# copy service configuration
cp configuration.yml /app/

# copy resources (experiment configurations)
cp -a services/300-experiment-manager.conf /apps/chaperone.d/300-experiment-manager.conf

cp -a services/envcp/config.tpl /app/config.tpl