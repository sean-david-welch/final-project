tailwind:
	tailwindcss -i ./src/main/resources/static/styles/index.css -o ./src/main/resources/static/styles/output.css

include .env
export $(shell sed 's/=.*//' .env)

# Default environment name from EB config
EB_ENV = budgetai-env

.PHONY: help init deploy update-env list-env

help:
	@echo "Available commands:"
	@echo "  make init         - Initialize EB environment with variables from .env"
	@echo "  make deploy      - Deploy application to EB"
	@echo "  make update-env  - Update EB environment variables from .env"
	@echo "  make list-env    - List current EB environment variables"

init:
	eb init budgetai --platform "docker" --region eu-west-1

# Deploy application to Elastic Beanstalk
deploy:
	eb deploy $(EB_ENV)

# Update EB environment variables from .env file
update-env:
	eb setenv \
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
	eb printenv $(EB_ENV)