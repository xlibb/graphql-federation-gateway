/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.xlibb.gateway.generator;

import io.xlibb.gateway.GatewayProject;
import io.xlibb.gateway.exception.GatewayGenerationException;
import io.xlibb.gateway.exception.ValidationException;
import io.xlibb.gateway.generator.common.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Utility class for gateway tests.
 */
public class GatewayTestUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayTestUtils.class);
    private static final Path sampleRequestResourceDir =
            Path.of("src", "test", "resources", "sample_request_responses", "requests").toAbsolutePath();
    private static final Path sampleResponseResourceDir =
            Path.of("src", "test", "resources", "sample_request_responses", "responses").toAbsolutePath();
    public static final Path SCHEMA_RESOURCE_DIR =
            Path.of("src", "test", "resources", "supergraph_schemas").toAbsolutePath();

    public static File getBallerinaExecutableJar(Path projectDir, Path tmpDir)
            throws GatewayGenerationException {
        return CommonUtils.getCompiledBallerinaProject(projectDir.toAbsolutePath(), tmpDir,
                projectDir.getFileName().toString());
    }

    public static void waitTillUrlIsAvailable(Process process, String url) throws IOException {
        URL urlObj = new URL(url);
        HttpURLConnection connection;
        boolean available = false;
        do {
            try {
                Thread.sleep(1000);
                connection = (HttpURLConnection) urlObj.openConnection();
                connection.getResponseCode();
                available = true;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ConnectException ignored) {
            }

        } while (!available && process.isAlive());

        if (!process.isAlive()) {
            throw new RuntimeException("Process terminated before the url is available");
        }
    }

    public static void deleteDirectory(Path tmpDir) throws IOException {
        Files.walk(tmpDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                });
    }

    public static String getRequestContent(String filename) throws IOException {
        String content = Files.readString(sampleRequestResourceDir.resolve(filename + ".graphql")).trim();
        content = content.replace("\"", "\\\"");
        if (content.startsWith("query")) {
            content = content.replace("query", "").trim();
            content = content.substring(1, content.length() - 1);
        }
        return content;
    }

    public static String getResponseContent(String filename) throws IOException {
        return replaceWhiteSpacesAndNewLines(Files.readString(sampleResponseResourceDir.resolve(filename + ".json")));
    }

    public static String getGraphqlQueryResponse(String graphqlUrl, String query) throws IOException {
        return replaceWhiteSpacesAndNewLines(getGraphqlResponse(graphqlUrl,
                ("{\"query\":\"{" + query + "}\"}").getBytes()));
    }

    public static String getGraphqlMutationResponse(String grapqlUrl, String query) throws IOException {
        return replaceWhiteSpacesAndNewLines(getGraphqlResponse(grapqlUrl,
                ("{\"query\":\"" + query + "\"}").getBytes()));
    }

    private static String getGraphqlResponse(String grapqlUrl, byte[] body) throws IOException {
        URL url = new URL(grapqlUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.getOutputStream().write(body);

        // read and assert the response from the server
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        return response.toString();
    }

    public static void copyFilesToTarget(Path[] files, Path targetDir) throws IOException {
        for (Path file : files) {
            Files.copy(file, targetDir.resolve(file.getFileName()));
        }
    }

    public static void copyFilesToTarget(File[] files, Path targetDir) throws IOException {
        for (File file : files) {
            Path path = file.toPath();
            Files.copy(path, targetDir.resolve(path.getFileName()));
        }
    }

    public static GatewayProject getGatewayProject(String schemaFileName, Path tmpDir)
            throws IOException, ValidationException {
        GatewayProject project = new GatewayProject("test",
                SCHEMA_RESOURCE_DIR.resolve(schemaFileName + ".graphql").toString(), tmpDir.toString());
        return project;
    }

    public static String getCorrespondingFolderName(String schemaFileName) {
        char firstChar = schemaFileName.charAt(0);
        return Character.toLowerCase(firstChar) + schemaFileName.substring(1);
    }

    public static String replaceWhiteSpacesAndNewLines(String str) {
        return str.replaceAll("\\s+", "").replaceAll("\\n+", "");
    }
}
