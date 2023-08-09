# GraphQL Federation Gateway
[![Build](https://github.com/Ishad-M-I-M/graphql-federation-gateway/actions/workflows/pull_request.yml/badge.svg)](https://github.com/Ishad-M-I-M/graphql-federation-gateway/actions/workflows/pull_request.yml/badge.svg)
[![codecov](https://codecov.io/gh/Ishad-M-I-M/graphql-federation-gateway/branch/main/graph/badge.svg?token=hLnziNmccQ)](https://codecov.io/gh/Ishad-M-I-M/graphql-federation-gateway)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/Ishad-M-I-M/graphql-federation-gateway.svg)](https://github.com/Ishad-M-I-M/graphql-federation-gateway/commits/master)
[![Github issues](https://img.shields.io/github/issues/Ishad-M-I-M/graphql-federation-gateway.svg?label=Open%20Issues)](https://github.com/Ishad-M-I-M/graphql-federation-gateway)

A Graphql Federation Gateway implemented using Ballerina as the underline technology.
This will generate a gateway executable for a given supergraph schema. 

## Using the Gateway

### Prerequisites
1.  Download and install Java SE Development Kit (JDK) version 11 (from one of the following locations).

    - [Oracle](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)

    - [OpenJDK](https://adoptopenjdk.net/)

      > **Note:** Set the JAVA_HOME environment variable to the path name of the directory into which you installed JDK.

2. Download and install [Ballerina](https://ballerina.io/downloads/)

### Steps to use the gateway.
1. Download [gateway.sh](https://github.com/Ishad-M-I-M/graphql-federation-gateway/releases/download/v0.1.0/gateway.sh)

2. To start the gateway run the following command in the terminal.

```bash
./gateway.sh -s <supergraphPath> -p <port>
```

- `supergraphPath` is a mandatory argument.
- If the port is not provided the default port `9090` will be used.

### Try out the example
1. Navigate into `examples/astronauts_missions_example` directory. There's two federated graphql services and a supergraph schema.
2. In terminal execute `./gateway.sh -s supergraph.graphql` to start the gateway.
3. In terminal execute `bal run` inside both `astronauts_service` and `missions_service` directories to start the subgraph services.
3. Navigate into `astronuats_service` directory and execute `bal run` in the terminal to start the `astronauts_service` subgraph service.
4. Navigate into `missions_service` directory and execute `bal run` in the terminal to start the `missions_service` subgraph service.
5. Try out the following query in the graphql client.

```graphql
query {
    astronauts {
        id
        name
        missions {
            id
            designation
        }
    }
}
```

## Build from the source.

### Setup the prerequisites
1.  Download and install Java SE Development Kit (JDK) version 11 (from one of the following locations).

    - [Oracle](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)

    - [OpenJDK](https://adoptopenjdk.net/)

      > **Note:** Set the JAVA_HOME environment variable to the path name of the directory into which you installed JDK.

2.  Export your Github personal access token with the read package permissions as follows.

          export packageUser=<Username>
          export packagePAT=<Personal access token>

### Build the source

Execute the commands below to build from the source.
> **Note:** When running the build with test in Windows use `-Pdisable=invalid-permission` to skip the Windows incompatible test cases.

1. To build the project:
```bash
./gradlew clean build
```

2. To run the tests
```bash
./gradlew clean test
```

3. To build the project without tests:
```bash
./gradlew clean build -x test
```

4. Publish `jar` artifact to the local `.m2` repository:
```bash
./gradlew clean build publishToMavenLocal
```
