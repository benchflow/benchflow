NAME = benchflow/base-images:dns-envconsul
# will follow the envconsul version
VERSION = dev

.PHONY: all build

all: build

build:
	docker build -t $(NAME)_$(VERSION) -f Dockerfile .

test:
	docker run -ti --rm -e "ENVCONSUL_CONSUL=demo.consul.io:80" \
	-e "ENVCONSUL_PREFIXES==\{path = \"minio\" format = \"MINIO_{{ key }}\"\},\{path = \"benchflow\" format = \"BENCHFLOW_{{ key }}\"\}" \
	$(NAME)_$(VERSION)