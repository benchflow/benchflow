JAVA_HOME = "/opt/jdk"

.PHONY: all build_release 

all: build_release

# clean:
# 	JAVA_HOME=$(JAVA_HOME) ant clean

run_local: 
	JAVA_HOME=$(JAVA_HOME) ant run

build: build_release

build_release:
	JAVA_HOME=$(JAVA_HOME) ant build