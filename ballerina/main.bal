import ballerina/jballerina.java;
import ballerina/io;

configurable string supergraphPath = "";
configurable string outputPath = "";

public function main() {
    string gatewayCode = generateGateway(supergraphPath, outputPath);
    io:println(gatewayCode);
}

isolated function generateGateway(string supergraphPath, string outputPath) returns string = @java:Method {
    'class: "io.xlibb.gateway.generator.GatewayCodeGenerator"
} external;

