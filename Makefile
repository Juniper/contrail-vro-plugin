MVN_OPT=-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true
TOOLS=tools

ifneq (,$(findstring dev,$(MAKECMDGOALS)))
    DEPS=deps
endif

.PHONY: deps generateds contrail-api-client contrail-java-api contrail-java-api-install
generateds:
	$(TOOLS)/update.sh https://github.com/Juniper/contrail-generateDS.git generateds

contrail-api-client:
	$(TOOLS)/update.sh https://github.com/Juniper/contrail-api-client.git contrail-api-client

contrail-java-api:
	$(TOOLS)/update.sh https://github.com/Juniper/contrail-java-api.git contrail-java-api

deps: generateds contrail-api-client contrail-java-api

contrail-java-api-install: $(DEPS)
	cd contrail-java-api && mvn install

dist/o11nplugin-contrail.vmoapp: contrail-java-api-install
	mvn install -Drepo.host=$(REPO_HOST) -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true

.PHONY: dev build test clean

vmoapp: dist/o11nplugin-contrail.vmoapp

dev: vmoapp

build: vmoapp

test: build

clean:
	mvn clean
