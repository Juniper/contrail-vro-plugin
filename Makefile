#
# Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
#

SB_ROOT = ..
JAVA_API_DIR = $(SB_ROOT)/java-api

OUTPUT_FILE = o11nplugin-contrail.dar

MVN = mvn -Drepo.host=$(REPO_HOST)

.PHONY: repo init sync package build test clean
.ONESHELL:

$(SB_ROOT)/repo:
	cd ..
	curl https://storage.googleapis.com/git-repo-downloads/repo > repo
	chmod a+x ./repo

repo: $(SB_ROOT)/repo

init: repo
	cd $(SB_ROOT)
	./repo init -u https://github.com/Juniper/contrail-vnc

sync:
	cd $(SB_ROOT)
	./repo sync

java-api-install:
	cd $(JAVA_API_DIR)
	mvn install

dist/$(OUTPUT_FILE): java-api-install
	$(MVN) install

package: dist/$(OUTPUT_FILE)

build: package

test: package

clean:
	$(MVN) clean
