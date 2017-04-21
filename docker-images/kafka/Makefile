NAME = benchflow/kafka
VERSION = dev

.PHONY: all build 

all: build

build:
	docker build -t $(NAME):$(VERSION) -f Dockerfile .

test:
	docker run -ti --rm --net="host" -e "AUTO_CREATE_TOPICS=true" -e "ADVERTISED_HOST=10.0.2.15" -e "ADVERTISED_PORT=9092" -p 2181:2181 -p 9092:9092 $(NAME):$(VERSION)