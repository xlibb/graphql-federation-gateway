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

package io.xlibb.gateway.graphql;

import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.errors.SchemaProblem;
import io.xlibb.gateway.exception.ValidationException;
import io.xlibb.gateway.generator.GatewayCodeGenerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Class to hold the utility methods.
 * */
public class Utils {

    private static final String[] KEYWORDS = new String[] {"abort", "aborted", "abstract", "all", "annotation",
            "any", "anydata", "boolean", "break", "byte", "catch", "channel", "check", "checkpanic", "client",
            "committed", "const", "continue", "decimal", "else", "error", "external", "fail", "final", "finally",
            "float", "flush", "fork", "function", "future", "handle", "if", "import", "in", "int", "is", "join",
            "json", "listener", "lock", "match", "new", "object", "OBJECT_INIT", "onretry", "parameter", "panic",
            "private", "public", "record", "remote", "resource", "retries", "retry", "return", "returns", "service",
            "source", "start", "stream", "string", "table", "transaction", "try", "type", "typedesc", "typeof",
            "trap", "throw", "wait", "while", "with", "worker", "var", "version", "xml", "xmlns", "BOOLEAN_LITERAL",
            "NULL_LITERAL", "ascending", "descending", "foreach", "map", "group", "from", "default", "field",
            "limit", "as", "on", "isolated", "readonly", "distinct", "where", "select", "do", "transactional"
            , "commit", "enum", "base16", "base64", "rollback", "configurable", "class", "module", "never",
            "outer", "order", "null", "key", "let", "by", "equals"};

    private static final String[] TYPES = new String[] {"int", "any", "anydata", "boolean", "byte", "float", "int",
            "json", "string", "table", "var", "xml"};

    public static final List<String> BAL_KEYWORDS = Collections.unmodifiableList(Arrays.asList(KEYWORDS));
    public static final List<String> BAL_TYPES = Collections.unmodifiableList(Arrays.asList(TYPES));
    public static final String GRAPHQL_ID_TYPE = "ID";
    public static final String GRAPHQL_STRING_TYPE = "String";
    public static final String GRAPHQL_INT_TYPE = "Int";
    public static final String GRAPHQL_FLOAT_TYPE = "Float";
    public static final String GRAPHQL_BOOLEAN_TYPE = "Boolean";
    public static final String BALLERINA_STRING_TYPE = "string";
    public static final String BALLERINA_INT_TYPE = "int";
    public static final String BALLERINA_FLOAT_TYPE = "float";
    public static final String BALLERINA_BOOLEAN_TYPE = "boolean";
    public static final String BALLERINA_ANYDATA_TYPE = "anydata";

    public static final String ESCAPE_PATTERN = "([\\[\\]\\\\?!<>@#&~`*\\-=^+();:\\/\\_{}\\s|.$])";

    public static GraphQLSchema getGraphqlSchema(String schema) throws ValidationException {
        try {
            SchemaParser schemaParser = new SchemaParser();
            TypeDefinitionRegistry typeRegistry;
            typeRegistry = schemaParser.parse(schema);

            // TODO: Find an alternative way for define custom scalar types
            GraphQLScalarType joinFieldSet = ExtendedScalars.newAliasedScalar("join__FieldSet")
                    .aliasedScalar(Scalars.GraphQLString).build();
            GraphQLScalarType linkImport = ExtendedScalars.newAliasedScalar("link__Import")
                    .aliasedScalar(Scalars.GraphQLString).build();

            return new SchemaGenerator().makeExecutableSchema(typeRegistry,
                    RuntimeWiring.newRuntimeWiring().scalar(joinFieldSet).scalar(linkImport).build());
        } catch (SchemaProblem e) {
            throw new ValidationException(GatewayCodeGenerator.ERROR_INVALID_SCHEMA);
        }
    }

    /**
     * This method will escape special characters used in method names and identifiers.
     *
     * @param identifier identifier or method name
     * @return escaped string
     */
    public static String escapeIdentifier(String identifier) {

        if (identifier.matches("\\b[0-9]*\\b")) {
            return "'" + identifier;
        } else if (!identifier.matches("\\b[_a-zA-Z][_a-zA-Z0-9]*\\b") ||
                BAL_KEYWORDS.stream().anyMatch(identifier::equals)) {

            // TODO: Remove this `if`. Refer - https://github.com/ballerina-platform/ballerina-lang/issues/23045
            if (identifier.equals("error")) {
                identifier = "_error";
            } else {
                identifier = identifier.replaceAll(ESCAPE_PATTERN, "\\\\$1");
                if (identifier.endsWith("?")) {
                    if (identifier.charAt(identifier.length() - 2) == '\\') {
                        StringBuilder stringBuilder = new StringBuilder(identifier);
                        stringBuilder.deleteCharAt(identifier.length() - 2);
                        identifier = stringBuilder.toString();
                    }
                    if (BAL_KEYWORDS.stream().anyMatch(
                            Optional.ofNullable(identifier).filter(sStr -> sStr.length() != 0)
                                    .map(sStr -> sStr.substring(0, sStr.length() - 1)).orElse(identifier)::equals)) {
                        identifier = "'" + identifier;
                    } else {
                        return identifier;
                    }
                } else {
                    identifier = "'" + identifier;
                }
            }
        }
        return identifier;
    }

    /**
     * Checks whether a given GraphQL scalar type name is a primitive scalar type.
     *
     * @param graphqlTypeName the GraphQL scalar type name
     * @return whether a given GraphQL scalar type name is a primitive scalar type
     */
    public static Boolean isPrimitiveScalarType(String graphqlTypeName) {
        boolean isPrimitiveScalarType;
        switch (graphqlTypeName) {
            case GRAPHQL_ID_TYPE:
            case GRAPHQL_STRING_TYPE:
            case GRAPHQL_INT_TYPE:
            case GRAPHQL_FLOAT_TYPE:
            case GRAPHQL_BOOLEAN_TYPE:
                isPrimitiveScalarType = true;
                break;
            default:
                isPrimitiveScalarType = false;
        }
        return isPrimitiveScalarType;
    }

    /**
     * Checks whether a given GraphQL type name is a custom scalar type.
     *
     * @param graphQLSchema   the object instance of the GraphQL schema (SDL)
     * @param graphqlTypeName the GraphQL scalar type name
     * @return whether a given GraphQL scalar type name is a primitive scalar type
     */
    public static Boolean isCustomScalarType(GraphQLSchema graphQLSchema, String graphqlTypeName) {
        return SpecReader.getCustomScalarTypeNames(graphQLSchema).contains(graphqlTypeName);
    }

    /**
     * Gets the Ballerina type name for a given GraphQL type name.
     *
     * @param graphQLSchema   the object instance of the GraphQL schema (SDL)
     * @param graphqlTypeName the GraphQL scalar type name
     * @return the Ballerina type name for a given GraphQL scalar type name
     */
    public static String getBallerinaTypeName(GraphQLSchema graphQLSchema, String graphqlTypeName) {
        String ballerinaTypeName;
        if (isCustomScalarType(graphQLSchema, graphqlTypeName)) {
            ballerinaTypeName = BALLERINA_ANYDATA_TYPE;
        } else if (isEnumType(graphQLSchema, graphqlTypeName)) {
            ballerinaTypeName = BALLERINA_STRING_TYPE;
        } else {
            switch (graphqlTypeName) {
                case GRAPHQL_ID_TYPE:
                case GRAPHQL_STRING_TYPE:
                    ballerinaTypeName = BALLERINA_STRING_TYPE;
                    break;
                case GRAPHQL_INT_TYPE:
                    ballerinaTypeName = BALLERINA_INT_TYPE;
                    break;
                case GRAPHQL_FLOAT_TYPE:
                    ballerinaTypeName = BALLERINA_FLOAT_TYPE;
                    break;
                case GRAPHQL_BOOLEAN_TYPE:
                    ballerinaTypeName = BALLERINA_BOOLEAN_TYPE;
                    break;
                default:
                    ballerinaTypeName = graphqlTypeName;
            }
        }
        return ballerinaTypeName;
    }

    /**
     * Checks whether a given GraphQL type name is an enum type.
     *
     * @param graphQLSchema   the object instance of the GraphQL schema (SDL)
     * @param graphqlTypeName the GraphQL scalar type name
     * @return whether a given GraphQL scalar type name is a primitive scalar type
     */
    public static Boolean isEnumType(GraphQLSchema graphQLSchema, String graphqlTypeName) {
        return SpecReader.getEnumTypeNames(graphQLSchema).contains(graphqlTypeName);
    }
}
