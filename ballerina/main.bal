import ballerina/jballerina.java;
import ballerina/io;

configurable string supergraphPath = "";
configurable string outputPath = "";

public function main() {
    string gatewayFilePath = generateGateway(supergraphPath, outputPath);
    io:println(gatewayFilePath);
}

isolated function generateGateway(string supergraphPath, string outputPath) returns string = @java:Method {
    'class: "io.xlibb.gateway.generator.GatewayCodeGenerator"
} external;

