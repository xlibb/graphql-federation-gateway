#!/bin/bash

# Function to display script usage
display_usage() {
  echo "Usage: $0 [--supergraphPath <supergraphPath>] [--outputPath <outputPath>] [--port <port>]"
}

# Check if Ballerina is installed
if ! command -v bal &>/dev/null; then
  echo "Error: Ballerina is not installed."
  exit 1
fi

jar_file="./graphql_federation_gateway.jar"
jar_url="https://github.com/Ishad-M-I-M/graphql-federation-gateway/releases/download/v0.1.0/graphql_federation_gateway.jar"

if [[ ! -f $jar_file ]]; then
  wget -O $jar_file $jar_url
fi

# Parse command-line arguments
while [[ $# -gt 0 ]]; do
  case "$1" in
    -s|--supergraphPath)
      supergraphPath=$2
      shift 2
      ;;
    -o|--outputPath)
      outputPath=$2
      shift 2
      ;;
    -p|--port)
      port=$2
      shift 2
      ;;
    *)
      echo "Invalid option: $1"
      display_usage
      exit 1
      ;;
  esac
done

# Set default values if arguments are not provided
outputPath=${outputPath:-.}
port=${port:-9090}

# Execute the jar with the provided input values
(
  bal run "${jar_file}" \
    -CsupergraphPath="${supergraphPath}" \
    -CoutputPath="${outputPath}" \
    -Cport="${port}"
) &

pid=$!
spin='-\|/'
i=0

# Display animation while the last command is running
while kill -0 $pid 2>/dev/null; do
  i=$(( (i+1) % 4 ))
  printf "\r%s Generating gateway..." "${spin:$i:1}"
  sleep 0.1
done
