/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org). All Rights Reserved.
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

package io.xlibb.gateway.generator;

import graphql.language.Argument;
import graphql.language.BooleanValue;
import graphql.language.Directive;
import graphql.language.EnumValue;
import graphql.language.FieldDefinition;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.StringValue;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.language.Value;
import graphql.schema.GraphQLAppliedDirective;
import graphql.schema.GraphQLAppliedDirectiveArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLType;
import io.xlibb.gateway.exception.GatewayGenerationException;
import io.xlibb.gateway.exception.ValidationException;
import io.xlibb.gateway.graphql.SpecReader;
import io.xlibb.gateway.graphql.components.JoinGraph;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Common utility functions used inside the package.
 */
public class CommonUtils {
    public static final String TYPE_QUERY = "Query";
    public static final String TYPE_MUTATION = "Mutation";
    public static final String TYPE_SUBSCRIPTION = "Subscription";

    public static final String GRAHQL_TYPE_INT = "Int";
    public static final String GRAHQL_TYPE_FLOAT = "Float";
    public static final String GRAHQL_TYPE_BOOLEAN = "Boolean";

    public static final String BALLERINA_TYPE_INT = "int";
    public static final String BALLERINA_TYPE_FLOAT = "float";
    public static final String BALLERINA_TYPE_BOOLEAN = "boolean";
    public static final String BALLERINA_TYPE_STRING = "string";

    public static final String ARGUMENT_GRAPH = "graph";
    public static final String ARGUMENT_EXTERNAL = "external";
    public static final String ARGUMENT_KEY = "key";

    public static final String DIRECTIVE_JOIN_FIELD = "join__field";
    public static final String DIRECTIVE_JOIN_TYPE = "join__type";

    public static final String TYPE_JOIN_GRAPH = "join__Graph";

    public static final String BALLERINA_GRAPHQL_IMPORT_STATEMENT = "import ballerina/graphql;";
    public static final String GATEWAY_TEMPLATE_FILES_DIRECTORY = "gateway_templates";

    public static final String CLIENT_NAME_PLACEHOLDER = "@\\{clientName}";
    public static final String CLIENT_NAME_VALUE_PLACEHOLDER = "@\\{clientNameValue}";
    public static final String CLIENT_NAME_DECLARATION = "public const string " + CLIENT_NAME_PLACEHOLDER
            + " = \"" + CLIENT_NAME_VALUE_PLACEHOLDER + "\";";

    /**
     * Return the list of custom defined object type names in the GraphQL schema.
     *
     * @param graphQLSchema GraphQL schema
     * @return List of custom defined object type names
     */
    public static List<String> getCustomDefinedObjectTypeNames(GraphQLSchema graphQLSchema) {
        return SpecReader.getObjectTypeNames(graphQLSchema).stream()
                .filter(name -> name != null
                        && !name.isEmpty() && !name.equals(TYPE_QUERY)
                        && !name.equals(TYPE_MUTATION)
                        && !name.equals(TYPE_SUBSCRIPTION)).collect(Collectors.toList());
    }

    /**
     * Return list of query types.
     *
     * @param graphQLSchema GraphQL schema
     * @return List of query types
     */
    public static List<GraphQLSchemaElement> getQueryTypes(GraphQLSchema graphQLSchema) {
        // Schema validation will fail if Query type is null.
        return graphQLSchema.getQueryType().getChildren().stream().filter(
                child -> child instanceof GraphQLFieldDefinition).collect(Collectors.toList());
    }

    /**
     * Return list of mutation types.
     *
     * @param graphQLSchema GraphQL schema
     * @return List of mutation types
     */
    public static List<GraphQLSchemaElement> getMutationTypes(GraphQLSchema graphQLSchema) {
        if (graphQLSchema.getMutationType() == null) {
            return new ArrayList<>();
        }
        return graphQLSchema.getMutationType().getChildren().stream().filter(
                child -> child instanceof GraphQLFieldDefinition).collect(Collectors.toList());
    }

    /**
     * Return the type name of the GraphQL type.
     *
     * @param graphQLType GraphQL type
     * @return Type name
     * @throws GatewayGenerationException if the type is not supported
     */
    public static String getTypeFromGraphQLType(GraphQLType graphQLType) throws GatewayGenerationException {
        if (graphQLType instanceof GraphQLObjectType) {
            return ((GraphQLObjectType) graphQLType).getName() + "?";
        } else if (graphQLType instanceof GraphQLList) {
            return getTypeFromGraphQLType(((GraphQLList) graphQLType).getOriginalWrappedType()) + "[]?";
        } else if (graphQLType instanceof GraphQLNonNull) {
            return getNonNullTypeFromGraphQLType(((GraphQLNonNull) graphQLType).getOriginalWrappedType());
        } else if (graphQLType instanceof GraphQLScalarType) {
            return getBallerinaTypeName(((GraphQLScalarType) graphQLType).getName()) + "?";
        } else {
            throw new GatewayGenerationException("Unsupported type: " + graphQLType);
        }
    }

    public static String getNonNullTypeFromGraphQLType(GraphQLType graphQLType) throws GatewayGenerationException {
        if (graphQLType instanceof GraphQLObjectType) {
            return ((GraphQLObjectType) graphQLType).getName();
        } else if (graphQLType instanceof GraphQLList) {
            return getTypeFromGraphQLType(((GraphQLList) graphQLType).getOriginalWrappedType()) + "[]";
        } else if (graphQLType instanceof GraphQLScalarType) {
            return getBallerinaTypeName(((GraphQLScalarType) graphQLType).getName());
        } else if (graphQLType instanceof GraphQLNonNull) {
            return getNonNullTypeFromGraphQLType(((GraphQLNonNull) graphQLType).getOriginalWrappedType());
        } else {
            throw new GatewayGenerationException("Unsupported type: " + graphQLType);
        }
    }

    /**
     * Return the type name of the GraphQL type.
     *
     * @param definition GraphQL field definition
     * @return Type name
     */
    public static String getTypeFromFieldDefinition(FieldDefinition definition) throws GatewayGenerationException {
        return getTypeNameFromType(definition.getType());
    }

    private static String getTypeNameFromType(Type<?> type) throws GatewayGenerationException {
        if (type instanceof NonNullType) {
            return getTypeNameFromType(((NonNullType) type).getType());
        } else if (type instanceof ListType) {
            return getTypeNameFromType(((ListType) type).getType());
        } else if (type instanceof TypeName) {
            return ((TypeName) type).getName();
        } else {
            throw new GatewayGenerationException("Unsupported type: " + type);
        }
    }

    public static String getClientFromFieldDefinition(FieldDefinition definition,
                                                      List<GraphQLAppliedDirective> joinTypeDirectivesOnParent) {
        for (Directive directive : definition.getDirectives()) {
            if (directive.getName().equals(DIRECTIVE_JOIN_FIELD)) {
                String graph = null;
                Boolean external = null;
                for (Argument argument : directive.getArguments()) {
                    if (argument.getName().equals(ARGUMENT_GRAPH)) {
                        graph = ((EnumValue) argument.getValue()).getName();
                    } else if (argument.getName().equals(ARGUMENT_EXTERNAL)) {
                        external = ((BooleanValue) argument.getValue()).isValue();
                    }
                }

                if (graph != null && (external == null || !external)) {
                    return graph;
                }
            }
        }

        if (joinTypeDirectivesOnParent.size() == 1) {
            for (GraphQLAppliedDirectiveArgument argument : joinTypeDirectivesOnParent.get(0).getArguments()) {
                Object value = argument.getArgumentValue().getValue();
                if (argument.getName().equals(ARGUMENT_GRAPH) && value instanceof EnumValue) {
                    return ((EnumValue) value).getName();
                }
            }
        }

        return null;
    }

    /**
     * Return the type name of the GraphQL type without the array brackets.
     *
     * @param queryType GraphQL type
     * @return Type name
     * @throws GatewayGenerationException if the type is not supported
     */
    public static String getBasicTypeNameFromGraphQLType(GraphQLType queryType) throws GatewayGenerationException {
        if (queryType instanceof GraphQLScalarType) {
            return getBallerinaTypeName(((GraphQLScalarType) queryType).getName());
        } else if (queryType instanceof GraphQLObjectType) {
            return ((GraphQLObjectType) queryType).getName();
        } else if (queryType instanceof GraphQLList) {
            return getBasicTypeNameFromGraphQLType(((GraphQLList) queryType).getOriginalWrappedType());
        } else if (queryType instanceof GraphQLNonNull) {
            return getBasicTypeNameFromGraphQLType(((GraphQLNonNull) queryType).getOriginalWrappedType());
        } else {
            throw new GatewayGenerationException("Unsupported type: " + queryType);
        }
    }

    /**
     * Return whether the GraphQL type is a scalar type.
     *
     * @param queryType GraphQL type
     * @return Whether the GraphQL type is a scalar type
     */
    public static Boolean isScalarType(GraphQLType queryType) {
        if (queryType instanceof GraphQLScalarType) {
            return true;
        } else if (queryType instanceof GraphQLNonNull) {
            return isScalarType(((GraphQLNonNull) queryType).getOriginalWrappedType());
        } else if (queryType instanceof GraphQLList) {
            return isScalarType(((GraphQLList) queryType).getOriginalWrappedType());
        }
        return false;
    }

    /**
     * Return whether the GraphQL type is a list type.
     *
     * @param queryType GraphQL type
     * @return Whether the GraphQL type is a list type
     */
    public static Boolean isListType(GraphQLType queryType) {
        if (queryType instanceof GraphQLList) {
            return true;
        } else if (queryType instanceof GraphQLNonNull) {
            return isListType(((GraphQLNonNull) queryType).getOriginalWrappedType());
        }
        return false;
    }

    /**
     * Return whether the GraphQL type is an object type.
     *
     * @param queryType GraphQL type
     * @return Whether the GraphQL type is an object type
     */
    public static Boolean isObjectType(GraphQLType queryType) {
        if (queryType instanceof GraphQLObjectType) {
            return true;
        } else if (queryType instanceof GraphQLNonNull) {
            return isObjectType(((GraphQLNonNull) queryType).getOriginalWrappedType());
        }
        return false;
    }

    /**
     * Return map of join graphs in the GraphQL schema as Enum value as the key and a JoinGraph object as the value.
     *
     * @param graphQLSchema GraphQL schema
     * @return Map of join graphs
     */
    public static Map<String, JoinGraph> getJoinGraphs(GraphQLSchema graphQLSchema) throws ValidationException {
        Map<String, JoinGraph> joinGraphs = new HashMap<>();
        GraphQLType joinGraph = graphQLSchema.getType(TYPE_JOIN_GRAPH);
        if (joinGraph != null) {
            for (GraphQLEnumValueDefinition element : ((GraphQLEnumType) joinGraph).getValues()) {
                joinGraphs.put(element.getName(), new JoinGraph(element));
            }
        }
        return joinGraphs;
    }

    /**
     * Gets the path of the  file after copying it to the specified tmpDir. Used to read resources.
     *
     * @param tmpDir   Temporary directory
     * @param filename Name of the file
     * @return Path to file in the temporary directory created
     * @throws IOException When failed to get the gateway_templates/resource_function_body.bal.partial file
     *                     from resources
     */
    public static Path getResourceTemplateFilePath(Path tmpDir, String filename) throws IOException {
        Path path = null;
        ClassLoader classLoader = CommonUtils.class.getClassLoader();
        InputStream inputStream =
                classLoader.getResourceAsStream(GATEWAY_TEMPLATE_FILES_DIRECTORY + "/" + filename);
        if (inputStream != null) {
            String resource = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            path = tmpDir.resolve(filename);
            try (PrintWriter writer = new PrintWriter(path.toString(), StandardCharsets.UTF_8)) {
                writer.print(resource);
            }
        }
        return path;
    }

    public static String getValue(Value<?> value) throws GatewayGenerationException {
        if (value instanceof IntValue) {
            return ((IntValue) value).getValue().toString();
        } else if (value instanceof StringValue) {
            return "\"" + ((StringValue) value).getValue() + "\"";
        } else if (value instanceof BooleanValue) {
            return ((BooleanValue) value).isValue() ? "true" : "false";
        } else if (value instanceof FloatValue) {
            return ((FloatValue) value).getValue().toString();
        } else {
            throw new GatewayGenerationException("Unsupported value: " + value);
        }
    }

    public static String getBallerinaTypeName(String type) {
        switch (type) {
            case GRAHQL_TYPE_INT:
                return BALLERINA_TYPE_INT;
            case GRAHQL_TYPE_BOOLEAN:
                return BALLERINA_TYPE_BOOLEAN;
            case GRAHQL_TYPE_FLOAT:
                return BALLERINA_TYPE_FLOAT;
            default:
                return BALLERINA_TYPE_STRING;
        }
    }

}
