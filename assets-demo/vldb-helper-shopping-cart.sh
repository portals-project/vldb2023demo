#!/bin/bash
# display all the commands



# This script is used to help with the installation of the VLDB 2023 tutorial
# It will install the required packages and setup the environment

# ./vldb-helper-shopping-cart.sh -help / -h display help
# ./vldb-helper-shopping-cart.sh -install / -i install the required packages [root required] (warning: experimental)
# ./vldb-helper-shopping-cart.sh -setup / -s setup the environment
# ./vldb-helper-shopping-cart.sh -run_example / -r run the example
# ./vldb-helper-shopping-cart.sh -all / -a setup the environment and run the example (not installing the required packages)
# ./vldb-helper-shopping-cart.sh -teardown / -t teardown the example

# list of required packages
# minikube (or other kubernetes cluster)
# kubectl
# docker
# sbt
# scala

# This script is tested on Ubuntu 20.04 LTS

# Check if the user has provided any arguments
if [ $# -eq 0 ]
  then
    echo "No arguments supplied"
    echo "Please use -help / -h for help"
    exit
fi

# Check if the user has provided more than one argument
if [ $# -gt 1 ]
  then
    echo "Too many arguments supplied"
    echo "Please use -help / -h for help"
    exit
fi

# Check if the user has provided the correct argument

# Define an array of valid options
valid_options=("-help" "-h" "-install" "-i" "-setup" "-s" "-run_example" "-r" "-all" "-a" "-teardown" "-t")

# Check if the provided argument is in the array of valid options
if [[ ! " ${valid_options[@]} " =~ " $1 " ]]; then
  echo "Invalid argument supplied"
  echo "Please use -help / -h for help"
  exit 1
fi


# display help
if [ $1 == "-help" ] || [ $1 == "-h" ]
  then
    echo "This script is used to help with the installation of the VLDB 2023 tutorial"
    echo "It will install the required packages and setup the environment"
    echo ""
    echo "./vldb-helper-shopping-cart.sh -help / -h display help"
    echo "./vldb-helper-shopping-cart.sh -install / -i install the required packages (warning: experimental)"
    echo "./vldb-helper-shopping-cart.sh -setup / -s setup the environment"
    echo "./vldb-helper-shopping-cart.sh -run_example / -r run the example"
    echo "./vldb-helper-shopping-cart.sh -teardown / -t teardown the example"
    echo "./vldb-helper-shopping-cart.sh -all / -a setup the environment and run the example (not installing the required packages) + teardown"
    echo ""
    echo "This script is tested on Ubuntu 20.04 LTS"
    exit
fi

# install the required packages
# not included in -a / -all since the installation is experimental
if [ $1 == "-install" ] || [ $1 == "-i" ]
  then
    echo "For installation, please run the script as root"
    echo "Checking for root access"
    if [ "$EUID" -ne 0 ]
      then 
      echo "ERROR: Installation is not possible without root access"
      exit
    fi
    echo "Installing the required packages"
    # check if packages are already installed
    if [ $(dpkg-query -W -f='${Status}' docker 2>/dev/null | grep -c "ok installed") -eq 0 ]
      then
        # https://docs.docker.com/engine/install/ubuntu/
        echo "Installing dependencies for docker"
        sudo apt-get update
        sudo apt install apt-transport-https ca-certificates curl software-properties-common
        echo "adding docker's official GPG key"
        sudo install -m 0755 -d /etc/apt/keyrings
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
        sudo chmod a+r /etc/apt/keyrings/docker.gpgecho "setting up the stable repository"
        echo \
          "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
          "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | \
          sudo tee /etc/apt/sources.list.d/docker.list > /dev/nullsudo apt update
        sudo apt-get update
        sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
        # TODO: check if docker is installed correctly
        echo "INFO: Please verify that docker is installed correctly"
    fi
    if ! command -v kubectl &>/dev/null; then
      echo "Installing kubectl"
      # https://kubernetes.io/docs/tasks/tools/install-kubectl-linux/
      # Check for x86_64
      if [ "$(uname -m)" == "x86_64" ]; then
        curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
      fi
      # Check for arm64
      if [ "$(uname -m)" == "aarch64" ]; then
        curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/arm64/kubectl"
      fi
      sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

      # Check if kubectl is in the PATH before displaying the message
      if command -v kubectl &>/dev/null; then
        echo "INFO: kubectl installed successfully!"
      else
        echo "ERROR: Installation of kubectl failed."
      fi
    else
      echo "INFO: kubectl is already installed."
    fi

    if [ $(dpkg-query -W -f='${Status}' minikube 2>/dev/null | grep -c "ok installed") -eq 0 ]
      then
        # https://minikube.sigs.k8s.io/docs/start/
        echo "Installing minikube"
        # check for x86_64
        if [ $(uname -m) == "x86_64" ]
          then
            curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube_latest_amd64.deb
            sudo dpkg -i minikube_latest_amd64.deb
        fi
        # check for arm64
        if [ $(uname -m) == "aarch64" ]
          then
            curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube_latest_arm64.deb
            sudo dpkg -i minikube_latest_arm64.deb
        fi
    else
      echo "INFO: minikube is already installed."
    fi
    if [ $(dpkg-query -W -f='${Status}' sbt 2>/dev/null | grep -c "ok installed") -eq 0 ]
      then
        echo "Installing Sbt"
        # https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html
        sudp apt-get update
        sudo apt-get install apt-transport-https curl gnupg -yqq
        echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | sudo tee /etc/apt/sources.list.d/sbt.list
        echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | sudo tee /etc/apt/sources.list.d/sbt_old.list
        curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo -H gpg --no-default-keyring --keyring gnupg-ring:/etc/apt/trusted.gpg.d/scalasbt-release.gpg --import
        sudo chmod 644 /etc/apt/trusted.gpg.d/scalasbt-release.gpg
        sudo apt-get update
        sudo apt-get install sbt
    else
      echo "INFO: sbt is already installed."
    fi
    if [ $(dpkg-query -W -f='${Status}' scala 2>/dev/null | grep -c "ok installed") -eq 0 ]
      then
        echo "Installing Scala"
        # https://docs.scala-lang.org/getting-started/index.html
        # check for x86_64
        if [ $(uname -m) == "x86_64" ]
          then
            curl -fL https://github.com/coursier/coursier/releases/latest/download/cs-x86_64-pc-linux.gz | gzip -d > cs && chmod +x cs && ./cs setup
        fi
        # check for arm64
        if [ $(uname -m) == "aarch64" ]
          then
            curl -fL https://github.com/VirtusLab/coursier-m1/releases/latest/download/cs-aarch64-pc-linux.gz | gzip -d > cs && chmod +x cs && ./cs setup
        fi
        echo "You may need to restart your terminal, log out, or reboot in order for the changes to take effect."
  else
    echo "INFO: Scala is already installed."
  fi
    echo "Done"
fi

# setup minikube
if [ $1 == "-setup" ] || [ $1 == "-s" ] || [ $1 == "-all" ] || [ $1 == "-a" ]
  then
    # minikube setup
    echo "Setting up minikube"
    echo "Starting minikube"
    minikube start
    echo "Setting up minikube docker env"
    eval $(minikube docker-env)

    # build and load the docker images
    echo "Building the shopping cart client docker image"
    # docker build -f scripts/deployment/docker/Dockerfile-ShoppingCartClient . -t shoppingcart-client
    echo "Building the portals server docker image"
    # docker build -f scripts/deployment/docker/Dockerfile-Server . -t portals-server
    echo "Loading the server image into minikube"
    minikube image load portals-server
  
    echo "Done"
fi

# run the example
if [ $1 == "-run_example" ] || [ $1 == "-r" ] || [ $1 == "-all" ] || [ $1 == "-a" ]
  then
    echo "Running the example"
    echo "Starting the portals server"
    kubectl apply -f scripts/deployment/Kubernetes/Deployment.yaml

    # wait for the server to start
    echo "Waiting for the server to start"
    kubectl wait --for=condition=Ready pod -l app=portals --timeout=120s

    # minikube port forwarding
    echo "Setting up port forwarding"
    kubectl port-forward $(kubectl get pod -l app=portals -o jsonpath="{.items[0].metadata.name}") 8080:8080  &
    port_forward_pid=$!
    
    # start the client log into output.log
    echo "Starting the shopping cart client"
    docker run --network host test-client 2>&1 | tee client_output.log

    # stop the port forwarding
    echo "Stopping port forwarding"
    kill $port_forward_pid

    # print log of server to output_server.log
    echo "Printing the server log"
    kubectl logs $(kubectl get pod -l app=portals -o jsonpath="{.items[0].metadata.name}") > output_server.log


    # done
    echo "Done"
fi

# teardown
if [ $1 == "-teardown" ] || [ $1 == "-t" ] || [ $1 == "-all" ] || [ $1 == "-a" ]
  then
    echo "Tearing down the example"
    echo "Deleting the portals server"
    kubectl delete -f scripts/deployment/Kubernetes/Deployment.yaml
    echo "Done"
fi

