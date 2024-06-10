/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org).
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

import graphql.schema.GraphQLSchema;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.xlibb.gateway.GatewayProject;
import io.xlibb.gateway.exception.GatewayGenerationException;
import io.xlibb.gateway.exception.ValidationException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.xlibb.gateway.generator.GatewayCodeGenerator.generateGateway;

/**
 * Class to test code generation related to gateway.
 */
public class GatewayCodeGenerationTest extends GraphqlTest {
    private final Path expectedResources = this.resourceDir.resolve(Paths.get(
            "results"));

    @Test(description = "Test query plan generation for gateway", dataProvider =
            "GatewayGenerationDataProvider")
    public void testQueryPlanGeneration(String supergraphFileName)
            throws ValidationException, IOException, GatewayGenerationException {
        GraphQLSchema graphQLSchema = GatewayTestUtils.getGatewayProject(supergraphFileName, tmpDir).getSchema();
        String generatedSrc = (new GatewayQueryPlanGenerator(graphQLSchema)).generateSrc();
        String expectedSrc = GatewayTestUtils.readWithLF(expectedResources.resolve(
                Paths.get(supergraphFileName, "query_plan.bal")));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }

    @Test(description = "Test service generation for gateway", dataProvider = "GatewayGenerationDataProvider")
    public void testGatewayServiceGeneration(String supergraphFileName)
            throws ValidationException, IOException, GatewayGenerationException {

        GatewayProject project = GatewayTestUtils.getGatewayProject(supergraphFileName, tmpDir);
        String generatedSrc = (new GatewayServiceGenerator(project)).generateSrc();
        String expectedSrc = GatewayTestUtils.readWithLF(expectedResources.resolve(
                Paths.get(supergraphFileName, "service.bal")));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }

    @Test(description = "Test gateway types generation", dataProvider = "GatewayGenerationDataProvider")
    public void testGatewayTypeGeneration(String supergraphFileName)
            throws IOException, ValidationException, GatewayGenerationException {
        GatewayProject project = GatewayTestUtils.getGatewayProject(supergraphFileName, tmpDir);
        GraphQLSchema graphQLSchema = project.getSchema();
        String generatedSrc = (new GatewayTypeGenerator(graphQLSchema)).generateSrc();
        String expectedSrc = GatewayTestUtils.readWithLF(expectedResources.resolve(
                Paths.get(supergraphFileName, "types.bal")));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }

    @Test(description = "Test generate gateway function", dataProvider = "GatewayGenerationDataProvider")
    public void testGenerateGatewayFunction(String supergraphFileName) {
        String schemaPath = GatewayTestUtils.SCHEMA_RESOURCE_DIR.resolve(supergraphFileName + ".graphql")
                .toAbsolutePath().toString();
        BString gatewayFilePath = generateGateway(StringUtils.fromString(schemaPath),
                StringUtils.fromString(tmpDir.toString()), StringUtils.fromString("9000"));
        Assert.assertEquals(gatewayFilePath.getValue(), "success");
    }

    @Test(description = "Test generate gateway function with invalid arguments", dataProvider =
            "InvalidArgumentsDataProvider")
    public void testGenerateGatewayFunctionWithInvalidArguments(String supergraphFileName, String outputPath,
                                                                String expected) {
        String schemaPath = GatewayTestUtils.SCHEMA_RESOURCE_DIR.resolve(supergraphFileName + ".graphql")
                .toAbsolutePath().toString();
        BString gatewayFilePath = generateGateway(StringUtils.fromString(schemaPath),
                StringUtils.fromString(outputPath), StringUtils.fromString("9000"));
        Assert.assertEquals(gatewayFilePath.getValue(), expected);
    }

    @DataProvider(name = "GatewayGenerationDataProvider")
    public Object[][] getGatewayTypeGenerationTestData() {
        return new Object[][]{
                {"two_entities"},
                {"two_entities_with_id_type_fields"},
                {"three_entities"},
                {"deprecated_directive"}
        };
    }

    @DataProvider(name = "InvalidArgumentsDataProvider")
    public Object[][] getInvalidArgumentsTestData() {
        String tempPath = tmpDir.toAbsolutePath().toString();
        return new Object[][]{
                {"invalid_schema_path", tempPath, GatewayCodeGenerator.ERROR_INVALID_SUPERGRAPH_FILE_PATH},
                {"two_entities", "invalid_output_path", GatewayCodeGenerator.ERROR_INVALID_OUTPUT_PATH},
                {"invalid/missing_directive_definitions", tempPath, GatewayCodeGenerator.ERROR_INVALID_SCHEMA},
                {"invalid/missing_query_type", tempPath, GatewayCodeGenerator.ERROR_INVALID_SCHEMA}
        };
    }

    @Test(groups = {"invalid_permission"}, description = "Test output path is not writable", enabled = false)
    public void testReadOnlyOutputPath() throws IOException {
        String supergraph = "two_entities";
        String schemaPath = GatewayTestUtils.SCHEMA_RESOURCE_DIR.resolve(supergraph + ".graphql")
                .toAbsolutePath().toString();
        Path readOnlyPath = tmpDir.toAbsolutePath().resolve("readonly_folder");
        Files.createDirectory(readOnlyPath);
        File file = new File(readOnlyPath.toString());
        file.setReadOnly();
        BString output = generateGateway(StringUtils.fromString(schemaPath),
                StringUtils.fromString(readOnlyPath.toString()), StringUtils.fromString("9000"));
        Assert.assertEquals(output.getValue(), GatewayCodeGenerator.ERROR_OUTPUT_PATH_NOT_WRITABLE);
    }

}
