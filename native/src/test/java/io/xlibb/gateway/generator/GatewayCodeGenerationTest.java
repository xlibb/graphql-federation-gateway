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

import graphql.schema.GraphQLSchema;
import io.xlibb.gateway.GatewayProject;
import io.xlibb.gateway.exception.GatewayGenerationException;
import io.xlibb.gateway.exception.ValidationException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class to test code generation related to gateway.
 */
public class GatewayCodeGenerationTest extends GraphqlTest {
    private final Path expectedResources = this.resourceDir.resolve(Paths.get(
            "results"));

    @Test(description = "Test query plan generation for gateway", dataProvider =
            "GatewayQueryPlanGenerationDataProvider")
    public void testQueryPlanGeneration(String supergraphFileName)
            throws ValidationException, IOException, GatewayGenerationException {
        GraphQLSchema graphQLSchema =  GatewayTestUtils.getGatewayProject(supergraphFileName, tmpDir).getSchema();
        String generatedSrc = (new GatewayQueryPlanGenerator(graphQLSchema)).generateSrc();
        String expectedSrc = Files.readString(expectedResources.resolve(
                Paths.get(supergraphFileName, "query_plan.bal")));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }

    @DataProvider(name = "GatewayQueryPlanGenerationDataProvider")
    public Object[][] getGatewayQueryPlanGenerationTestData() {
        return new Object[][] {
                {"two_entities"},
                {"two_entities_with_id_type_fields"},
                {"three_entities"}
        };
    }

    @Test(description = "Test service generation for gateway", dataProvider = "serviceGenerationDataProvider")
    public void testGatewayServiceGeneration(String supergraphFileName)
            throws ValidationException, IOException, GatewayGenerationException {

        GatewayProject project = GatewayTestUtils.getGatewayProject(supergraphFileName, tmpDir);
        String generatedSrc = (new GatewayServiceGenerator(project)).generateSrc();
        String expectedSrc = Files.readString(expectedResources.resolve(
                Paths.get(supergraphFileName, "service.bal")));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }

    @DataProvider(name = "serviceGenerationDataProvider")
    public Object[][] getServiceGenerationDataProvider() {
        return new Object[][] {
                {"two_entities"},
                {"two_entities_with_id_type_fields"},
                {"three_entities"}
        };
    }

    @Test(description = "Test gateway types generation", dataProvider = "GatewayTypeGenerationDataProvider")
    public void testGatewayTypeGeneration(String supergraphFileName)
            throws IOException, ValidationException, GatewayGenerationException {
        GatewayProject project = GatewayTestUtils.getGatewayProject(supergraphFileName, tmpDir);
        GraphQLSchema graphQLSchema = project.getSchema();
        String generatedSrc = (new GatewayTypeGenerator(graphQLSchema)).generateSrc();
        String expectedSrc = Files.readString(expectedResources.resolve(
                Paths.get(supergraphFileName, "types.bal")));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }

    @DataProvider(name = "GatewayTypeGenerationDataProvider")
    public Object[][] getGatewayTypeGenerationTestData() {
        return new Object[][] {
                {"two_entities"},
                {"two_entities_with_id_type_fields"},
                {"three_entities"}
        };
    }
}
