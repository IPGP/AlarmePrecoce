default: help

help:
	@echo "EarlyWarning Docker image"
	@echo "    build: Builds a new Docker image from the Dockerfile"
	@echo "    run: Starts a new EarlyWarning container with the EarlyWarning app"
	@echo "    bash: Starts a new EarlyWarning container, and logs into a session (debugging purposes)"
	@echo "    rm: Removes all Docker containers running"

# Run the EarlyWarning app
run:
	./run.sh

# Build the Docker image
build:
	docker build . -t earlywarning

# Remove all existing Docker containers
rm:
	docker rm -f `docker ps -aq`

# Connects to a virtual terminal on an EarlyWarning Docker container
bash:
	./bash.sh