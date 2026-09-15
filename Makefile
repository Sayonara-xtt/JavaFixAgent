.PHONY: help status

help:
	@echo "JavaFix Agent - Phase 1 repository skeleton"
	@echo "Available targets:"
	@echo "  make status  Show the current Git working-tree state"

status:
	@git status --short --branch

