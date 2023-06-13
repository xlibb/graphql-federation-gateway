import ballerina/jballerina.java;
import ballerina/io;

configurable string supergraphPath = "";
configurable string outputPath = "";
configurable int port = 9090;

public function main() {
    string gatewayFilePath = generateGateway(supergraphPath, outputPath, port.toString());
    io:println(gatewayFilePath);
}

isolated function generateGateway(string supergraphPath, string outputPath, string port) returns string = @java:Method {
    'class: "io.xlibb.gateway.generator.GatewayCodeGenerator"
} external;

