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
1. Download the `graphql_federation_gateway.jar` from the release files. 

2. Run the following command
```bash
java -jar -D"ballerina.home=<ballerina-installation-path>/distributions/ballerina-<version>" ./graphql_federation_gateway.jar -CsupergraphPath=<path-to-supergraph-schema> -CoutputPath=<output-path> -Cport=<port>
```

3. Run the generated gateway executable `jar` with

```bash
java -jar <executable-file>
```

## Build from the source.

### Setup the prerequisites
1.  Download and install Java SE Development Kit (JDK) version 11 (from one of the following locations).

    - [Oracle](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)

    - [OpenJDK](https://adoptopenjdk.net/)

      > **Note:** Set the JAVA_HOME environment variable to the path name of the directory into which you installed JDK.

2.  Export your Github Personal access token with the read package permissions as follows.

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
./gradlew clean build -test
```

4. Publish `jar` artifact to the local `.m2` repository:
```bash
./gradlew clean build publishToMavenLocal
```
