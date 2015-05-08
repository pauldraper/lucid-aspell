SHELL := /bin/bash -e

.PHONY: default
default: all

.PHONY: package
package:
	sbt native:package

.PHONY: test
test:
	sbt test

.PHONY: all
all: compile test

.PHONY: stage
stage:
	sbt +native:stage

.PHONY: publish
publish:
	sbt +native:publish

.PHONY: publish-signed
publish-signed:
	@echo GPG_AGENT_INFO: $(value GPG_AGENT_INFO)
	@echo SONATYPE_USERNAME: $(value SONATYPE_USERNAME)
	sbt +native:publish-signed

.PHONY: cleanpackage
cleanpackage:
	sbt clean compile "test" package

.PHONY: %
%:
	sbt $*
