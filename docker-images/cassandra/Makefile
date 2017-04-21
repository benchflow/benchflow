NAME = benchflow/cassandra
VERSION = dev

.PHONY: all build 

all: build

# download_schema:
# 	mkdir -p ./data
# 	wget -O ./data/database.cql https://github.com/benchflow/benchflow/raw/dev/database_schemas/cassandra/benchflow.cql

build:
	docker build -t $(NAME):$(VERSION) -f Dockerfile .

build_container_local:
	docker build -t $(NAME):$(VERSION) -f Dockerfile.test .