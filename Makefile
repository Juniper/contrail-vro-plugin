#
# Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
#

GENERATEDS_DIR = generateds
API_CLIENT_DIR = contrail-api-client
JAVA_API_DIR = contrail-java-api

SCRIPT_DIR = etc

MVN = mvn -Drepo.host=$(REPO_HOST) -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true

ifneq (,$(findstring dev,$(MAKECMDGOALS)))
    DEPS = deps
endif

.PHONY: deps generateds contrail-api-client contrail-java-api contrail-java-api-install
generateds:
	$(SCRIPT_DIR)/update-repo.sh https://github.com/Juniper/contrail-generateDS.git $(GENERATEDS_DIR)

contrail-api-client:
	$(SCRIPT_DIR)/update-repo.sh https://github.com/Juniper/contrail-api-client.git $(API_CLIENT_DIR)

contrail-java-api:
	$(SCRIPT_DIR)/update-repo.sh https://github.com/Juniper/contrail-java-api.git $(JAVA_API_DIR)

deps: generateds contrail-api-client contrail-java-api

contrail-java-api-install: $(DEPS)
	cd $(JAVA_API_DIR) && mvn install

dist/o11nplugin-contrail.vmoapp: contrail-java-api-install
	$(MVN) install

.PHONY: dev build test clean

vmoapp: dist/o11nplugin-contrail.vmoapp

dev: vmoapp

build: vmoapp

test: build

clean:
	$(MVN) clean
