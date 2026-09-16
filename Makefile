.PHONY: help status java-test

help:
	@echo "JavaFix Agent - Phase 2 Spring Boot baseline"
	@echo "Available targets:"
	@echo "  make status     Show the current Git working-tree state"
	@echo "  make java-test  Run the shop-service test suite"

status:
	@git status --short --branch

java-test:
	@./shop-service/mvnw test -f shop-service/pom.xml
