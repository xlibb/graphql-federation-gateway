import ballerina/jballerina.java;
import ballerina/file;
import ballerina/io;

configurable string supergraphPath = "";
configurable string outputPath = ".";
configurable int port = 9000;

public function main() returns error? {
    if supergraphPath == ""  {
        io:println("Required parameters are not provided. Please provide supergraphPath");
        return;
    }
    string absoluteSupergraphPath = check file:getAbsolutePath(supergraphPath);
    string absoluteOutputPath = check file:getAbsolutePath(outputPath);
    string gatewayFilePath = generateGateway(absoluteSupergraphPath, absoluteOutputPath, port.toString());
    io:println(gatewayFilePath);
}

isolated function generateGateway(string supergraphPath, string outputPath, string port) returns string = @java:Method {
    'class: "io.xlibb.gateway.generator.GatewayCodeGenerator"
} external;

