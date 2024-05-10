/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.xlibb.gateway;

import graphql.schema.GraphQLSchema;
import io.xlibb.gateway.exception.ValidationException;
import io.xlibb.gateway.graphql.Utils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class to represent GraphQL federation gateway generation project.
 */
public class GatewayProject {
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayProject.class);
    private final String name;
    private final Path tempDir;
    private final Path outputPath;
    private final GraphQLSchema schema;
    private int port = 9000;

    public GatewayProject(String name, String schemaPath, String outputPath) throws IOException, ValidationException {
        this.name = name;
        this.outputPath = Path.of(outputPath);
        String schemaFileContent = Files.readString(Path.of(schemaPath));
        this.schema = Utils.getGraphqlSchema(schemaFileContent);
        tempDir = Files.createTempDirectory(".gateway-tmp" + System.nanoTime());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileUtils.deleteDirectory(tempDir.toFile());
            } catch (IOException ex) {
                LOGGER.error("Unable to delete the temporary directory : " + tempDir, ex);
            }
        }));
    }

    public GatewayProject(String name, String schemaPath, String outputPath, int port) throws ValidationException,
            IOException {
        this(name, schemaPath, outputPath);
        this.port = port;
    }

    public GatewayProject(String name, Path schemaPath, Path outputPath) throws IOException, ValidationException {
        this.name = name;
        this.outputPath = outputPath;
        String schemaFileContent = Files.readString(schemaPath);
        this.schema = Utils.getGraphqlSchema(schemaFileContent);
        tempDir = Files.createTempDirectory(".gateway-tmp" + System.nanoTime());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileUtils.deleteDirectory(tempDir.toFile());
            } catch (IOException ex) {
                LOGGER.error("Unable to delete the temporary directory : " + tempDir, ex);
            }
        }));
    }

    public String getName() {
        return name;
    }

    public Path getTempDir() {
        return tempDir;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public GraphQLSchema getSchema() {
        return schema;
    }

    public int getPort() {
        return port;
    }
}
