---
layout: default
title: Shopping Cart
---

# Shopping Cart

TODO: Explain Shopping Cart example itself

Look at Readme in Portals repository  
scripts/deployment/README.md


# VLDB 2023 Tutorial Helper Script

## Introduction

This script is designed to assist with the installation and setup of the VLDB 2023 tutorial environment. It provides several options to manage the tutorial's installation, configuration, and execution processes.

## Notes

- The script is tested on Ubuntu 20.04 LTS. Running it on other platforms may require adjustments.
- The installation of required packages is experimental and requires root access. Use it with caution.
- The script uses Minikube as a local Kubernetes replacement.

## Download

You can download the script using the links below:

- [Download the vldb-helper.sh script](assets-demo/vldb-helper-shopping-cart.sh)


## Usage

To use the script, place it into the root directory of the portals repository and execute it from the command line with one of the following options:

./vldb-helper-shopping-cart.sh OPTION


Replace `OPTION` with one of the following:

- `-help` or `-h`: Display help and usage instructions.
- `-install` or `-i`: Install the required packages (requires root access, experimental).
- `-setup` or `-s`: Setup the environment (start Minikube, build and load Docker images).
- `-run_example` or `-r`: Run the example (start the portals server and shopping cart client).
- `-all` or `-a`: Setup the environment and run the example (does not install required packages) and perform the teardown afterward.
- `-teardown` or `-t`: Teardown the example (delete the portals server deployment).

## Options

### -help / -h

This option displays help and usage instructions for the script.

### -install / -i

Use this option to install the required packages. Note that this process requires root access and is experimental. The script will check for and install the following packages if they are not already installed:

- Docker
- Kubernetes (kubectl)
- Minikube
- SBT (Scala Build Tool)
- Scala

### -setup / -s

This option sets up the environment for the VLDB 2023 tutorial. It includes the following steps:

1. Starting Minikube (if not already running).
2. Setting up the Minikube Docker environment.
3. Building and loading the required Docker images for the tutorial.

### -run_example / -r

Use this option to run the example of the VLDB 2023 tutorial. It performs the following tasks:

1. Starts the portals server using the Kubernetes deployment defined in `scripts/deployment/Kubernetes/Deployment.yaml`.
2. Waits for the server pod to be ready.
3. Sets up port forwarding to the portals server.
4. Starts the shopping cart client and logs its output to `client_output.log`.
5. Stops the port forwarding after the client completes its execution.
6. Prints the server log to `output_server.log`.

### -all / -a

This option combines the setup and run_example options without installing the required packages. It sets up the environment and runs the tutorial example. Afterward, it performs the teardown by deleting the portals server deployment.

### -teardown / -t

Use this option to tear down the tutorial example. It deletes the portals server deployment, effectively removing the running example.

