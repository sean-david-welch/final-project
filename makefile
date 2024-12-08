tailwind:
	tailwindcss -i ./src/main/resources/static/styles/index.css -o ./src/main/resources/static/styles/output.css

# Makefile for managing environment variables and EB deployment

# Load environment variables from .env file
include .env
export $(shell sed 's/=.*//' .env)

# Default environment name from EB config
EB_ENV = budgetai-env

.PHONY: help init deploy update-env list-env check-status wait-ready use-env

help:
	@echo "Available commands:"
	@echo "  make init         - Initialize EB environment with variables from .env"
	@echo "  make deploy      - Deploy application to EB"
	@echo "  make update-env  - Update EB environment variables from .env"
	@echo "  make list-env    - List current EB environment variables"
	@echo "  make check-status- Check current EB environment status"
	@echo "  make wait-ready  - Wait until environment is ready"
	@echo "  make use-env     - Set default environment"

init:
	eb init budgetai --platform "docker" --region eu-west-1

# Set default environment
use-env:
	eb use $(EB_ENV)

# Check environment status
check-status:
	@eb status -e $(EB_ENV)

# Wait until environment is ready
wait-ready:
	@echo "Waiting for environment to be ready..."
	@while [ "$$(eb status -e $(EB_ENV) | grep "Status:" | awk '{print $$2}')" != "Ready" ]; do \
		echo "Environment is not ready. Current status: $$(eb status -e $(EB_ENV) | grep "Status:" | awk '{print $$2}')"; \
		sleep 30; \
	done
	@echo "Environment is ready!"

# Deploy application to Elastic Beanstalk
deploy:
	make wait-ready
	eb deploy -e $(EB_ENV)

# Update EB environment variables from .env file
update-env:
	make wait-ready
	eb setenv -e $(EB_ENV) \
		PORT=8080 \
		ENVIRONMENT=production \
		OPEN_AI="$(OPEN_AI)" \
		AWS_ACCESS="$(AWS_ACCESS)" \
		AWS_SECRET="$(AWS_SECRET)" \
		JWT_AUDIENCE="$(JWT_AUDIENCE)" \
		JWT_ISSUER="$(JWT_ISSUER)" \
		JWT_REALM="$(JWT_REALM)" \
		JWT_SECRET="$(JWT_SECRET)" \
		ADMIN_EMAIL="$(ADMIN_EMAIL)"

# List current environment variables in EB
list-env:
	eb printenv -e $(EB_ENV)