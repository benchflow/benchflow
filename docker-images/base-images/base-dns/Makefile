NAME = benchflow/base-images:dns
VERSION = dev

.PHONY: all build

all: build

build:
	docker build -t $(NAME)_$(VERSION) -f Dockerfile .

test:
	docker run -ti --rm $(NAME)_$(VERSION)