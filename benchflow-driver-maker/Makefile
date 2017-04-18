REPONAME = drivers-maker
DOCKERIMAGENAME = benchflow/$(REPONAME)
VERSION = dev
JAVA_VERSION_FOR_COMPILATION = (^|/)java-8-oracle($|\s)
UNAME = $(shell uname)

find_java:
ifeq ($(UNAME), Darwin)
	$(eval JAVA_HOME := $(shell /usr/libexec/java_home))
else ifeq ($(UNAME),Linux)
ifndef TRAVIS
	$(eval JAVA_HOME := $(shell update-java-alternatives -l | cut -d' ' -f3 | egrep '$(JAVA_VERSION_FOR_COMPILATION)'))
endif 
endif

.PHONY: all build_release

all: build_release

clean:
	mvn clean

build: find_java
	mvn package

build_release: find_java
	mvn package

install: find_java
	mvn validate
	mvn package

test: find_java
	mvn test -B

build_container:
	docker build -t $(DOCKERIMAGENAME):$(VERSION) -f Dockerfile .

build_container_local: find_java
	mvn package
	docker build -t $(DOCKERIMAGENAME):$(VERSION) -f Dockerfile.ci .
	rm target/benchflow-$(REPONAME).jar

test_container_local:
	docker run -ti --rm -e "ENVCONSUL_CONSUL=$(ENVCONSUL_CONSUL)" \
	-e "MINIO_ADDRESS=$(MINIO_ADDRESS)" \
	-p 8080:8080 --name $(REPONAME) $(DOCKERIMAGENAME):$(VERSION)

rm_container_local:
	docker rm -f -v $(REPONAME)
