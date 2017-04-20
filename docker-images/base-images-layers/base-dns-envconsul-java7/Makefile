NAME = benchflow/base-images:dns-envconsul-java7
# will follow the envconsul version
VERSION = dev

.PHONY: all build

all: build

build:
	docker build -t $(NAME)_$(VERSION) -f Dockerfile .

test:
	docker run -ti --rm -e "ENVCONSUL_CONSUL=demo.consul.io:80" $(NAME)_$(VERSION)