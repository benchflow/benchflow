NAME = benchflow/faban:agent
# will follow the envconsul version
VERSION = dev

.PHONY: all build 

all: build

build:
	docker build -t $(NAME)_$(VERSION) -f Dockerfile .