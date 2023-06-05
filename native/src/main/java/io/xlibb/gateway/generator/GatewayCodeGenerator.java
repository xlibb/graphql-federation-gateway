package io.xlibb.gateway.generator;

import graphql.schema.GraphQLSchema;
import io.xlibb.gateway.GatewayProject;
import io.xlibb.gateway.exception.GatewayGenerationException;
import io.xlibb.gateway.exception.ValidationException;
import io.xlibb.gateway.generator.common.CommonUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.xlibb.gateway.generator.common.Constants.GATEWAY_PROJECT_TEMPLATE_DIRECTORY;
import static io.xlibb.gateway.generator.common.Constants.QUERY_PLAN_FILE_NAME;
import static io.xlibb.gateway.generator.common.Constants.SERVICE_FILE_NAME;
import static io.xlibb.gateway.generator.common.Constants.TYPES_FILE_NAME;

/**
 * Class to generated source code for the gateway.
 */
public class GatewayCodeGenerator {
    private static final String[] GATEWAY_PROJECT_TEMPLATE_FILES = {
            "Ballerina.toml",
            "resolver.bal",
            "utils.bal",
            "records.bal",
            "queryFieldClassifier.bal"
    };

    public static File generateGatewayJar(GatewayProject project) throws GatewayGenerationException {
        try {
            copyTemplateFiles(project.getTempDir());
            generateBalSources(project);

            // Delete partial files
            try {
                deletePartialFiles(project.getTempDir());
            } catch (IOException ignored) {

            }
            //Generating the executable
            CommonUtils.getCompiledBallerinaProject(project.getTempDir(),
                    project.getOutputPath(), project.getName() + "-gateway");
        } catch (GatewayGenerationException | IOException | ValidationException e) {
            throw new GatewayGenerationException(e.getMessage());
        }
        return new File(project.getOutputPath().toString() + "/" + project.getName() + "-gateway.jar");
    }

    public static void copyTemplateFiles(Path targetPath) throws GatewayGenerationException, IOException {
        ClassLoader classLoader = GatewayCodeGenerator.class.getClassLoader();
        for (String fileName : GATEWAY_PROJECT_TEMPLATE_FILES) {
            InputStream inputStream = classLoader.getResourceAsStream(
                    GATEWAY_PROJECT_TEMPLATE_DIRECTORY + "/" + fileName);

            checkInputStream(inputStream);
            String resource = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            Path path = targetPath.resolve(fileName);
            try (PrintWriter writer = new PrintWriter(path.toString(), StandardCharsets.UTF_8)) {
                writer.print(resource);
            } catch (IOException e) {
                throw new GatewayGenerationException("Error while copying the template files.");
            }
        }
    }

    private static void checkInputStream(InputStream inputStream) throws GatewayGenerationException {
        if (inputStream == null) {
            throw new GatewayGenerationException("Error while copying the template files.");
        }
    }

    private static void deletePartialFiles(Path directoryPath) throws IOException {
        for (Path path : Files.walk(directoryPath)
                .filter(path -> path.toString().endsWith(".partial")).toArray(Path[]::new)) {
            Files.delete(path);
        }
    }

    private static void generateBalSources(GatewayProject project)
            throws GatewayGenerationException, IOException, ValidationException {
        GraphQLSchema graphQLSchema = project.getSchema();

        writeSourceToFile(new GatewayTypeGenerator(graphQLSchema).generateSrc(), TYPES_FILE_NAME,
                project.getTempDir());
        writeSourceToFile(new GatewayQueryPlanGenerator(graphQLSchema).generateSrc(), QUERY_PLAN_FILE_NAME,
                project.getTempDir());
        writeSourceToFile(new GatewayServiceGenerator(project).generateSrc(), SERVICE_FILE_NAME,
                project.getTempDir());
    }

    private static void writeSourceToFile(String content, String filename, Path targetPath) throws IOException {
        Path path = targetPath.resolve(filename);
        try (PrintWriter writer = new PrintWriter(path.toString(), StandardCharsets.UTF_8)) {
            writer.print(content);
        } catch (IOException e) {
            throw new IOException("Error while writing the generated source to the file.");
        }
    }

}
