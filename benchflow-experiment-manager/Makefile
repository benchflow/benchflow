REPONAME = experiments-manager
DOCKERIMAGENAME = benchflow/$(REPONAME)
VERSION = dev
JAVA_VERSION_FOR_COMPILATION = (^|/)java-8-oracle($|\s)
UNAME = $(shell uname)

DBNAME = mysql

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
	mvn -U package

build_release: find_java
	mvn -U package

install: find_java
	mvn -U package

test: find_java
	mvn -U test

build_container:
	docker build -t $(DOCKERIMAGENAME):$(VERSION) -f Dockerfile .

build_container_local: find_java
	mvn -U package
	docker build -t $(DOCKERIMAGENAME):$(VERSION) -f Dockerfile.test .
	rm target/benchflow-$(REPONAME).jar

test_container_local:
	docker run -d -p $(DB_PORT):3306 --name $(DBNAME) -e "MYSQL_ALLOW_EMPTY_PASSWORD=yes" -e "MYSQL_USER=root" $(DBNAME):latest
	docker run -ti --rm \
	-e "ENVCONSUL_CONSUL=$(ENVCONSUL_CONSUL)" \
	-e "FABAN_ADDRESS=$(FABAN_ADDRESS)" \
	-e "DRIVERS_MAKER_ADDRESS=$(DRIVERS_MAKER_ADDRESS)" \
	-e "DB_USER=$(DB_USER)" -e "DB_PASSWORD=$(DB_PASSWORD)" -e "DB_HOST=$(DBNAME):(DB_PORT)" \
	-e "DB_PORT=$(DB_PORT)" -e "DB_NAME=$(DB_NAME)" \
	-e "MINIO_ADDRESS=$(MINIO_ADDRESS)" \
	-p 8080:8080 --link=$(DBNAME) --name $(REPONAME) $(DOCKERIMAGENAME):$(VERSION)

rm_container_local:
	docker rm -f -v $(DBNAME)
	docker rm -f -v $(REPONAME)