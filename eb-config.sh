#!/bin/bash

# Load .env file
set -a
source .env
set +a

# Create eb extensions directory if it doesn't exist
mkdir -p .ebextensions

# Use envsubst to replace variables
envsubst < .ebextensions/options.config.template > .ebextensions/options.config