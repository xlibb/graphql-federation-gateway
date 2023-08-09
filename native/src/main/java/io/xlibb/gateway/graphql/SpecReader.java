package io.xlibb.gateway.graphql;
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

import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.GraphQLAppliedDirective;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import io.xlibb.gateway.exception.ValidationException;
import io.xlibb.gateway.graphql.components.FieldType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.xlibb.gateway.graphql.Utils.getBallerinaTypeName;

/**
 * Class includes the methods to read the GraphQL schema (SDL).
 */
public class SpecReader {
    /**
     * Get the object type names from the GraphQL schema.
     *
     * @param graphQLSchema the instance of the Graphql schema file
     * @return the list of the object type names
     */
    public static List<String> getObjectTypeNames(GraphQLSchema graphQLSchema) {
        List<String> objectTypeNames = new ArrayList<>();
        for (GraphQLNamedType graphQLNamedType : graphQLSchema.getAllTypesAsList()) {
            if (graphQLNamedType instanceof GraphQLObjectType && !graphQLNamedType.getName().startsWith("__")) {
                objectTypeNames.add(graphQLNamedType.getName());
            }
        }
        return objectTypeNames;
    }

    /**
     * Get the directives applied on the object type name from the GraphQL schema.
     *
     * @param graphQLSchema  the instance of the Graphql schema file
     * @param objectTypeName the object type name
     * @return the object type directives
     */
    public static List<GraphQLAppliedDirective> getObjectTypeDirectives(GraphQLSchema graphQLSchema,
                                                                        String objectTypeName) {
        List<GraphQLAppliedDirective> objectTypeDirectives = new ArrayList<>();
        if (graphQLSchema.getType(objectTypeName) instanceof GraphQLObjectType) {
            GraphQLObjectType objectType =
                    ((GraphQLObjectType) graphQLSchema.getType(objectTypeName));
            if (objectType != null) {
                objectTypeDirectives = objectType.getAppliedDirectives();
            }
        }
        return objectTypeDirectives;
    }

    /**
     * Gets the representation of Ballerina field type for a given GraphQL field type.
     *
     * @param graphQLSchema the object instance of the GraphQL schema (SDL)
     * @param type          the field type
     * @return the string representation of Ballerina type for a given GraphQL field type
     */
    public static FieldType getFieldType(GraphQLSchema graphQLSchema, Type<?> type) {
        FieldType fieldType = new FieldType();
        if (type instanceof TypeName) {
            fieldType.setName(getBallerinaTypeName(graphQLSchema, ((TypeName) type).getName()));
            fieldType.setTokens("?");
        }
        if (type instanceof NonNullType) {
            if (((NonNullType) type).getType() instanceof TypeName) {
                fieldType.setName(getBallerinaTypeName(graphQLSchema,
                        ((TypeName) ((NonNullType) type).getType()).getName()));
                fieldType.setTokens("");
            }
            if (((NonNullType) type).getType() instanceof ListType) {
                if (((ListType) ((NonNullType) type).getType()).getType() instanceof TypeName) {
                    fieldType.setName(getBallerinaTypeName(graphQLSchema,
                            ((TypeName) ((ListType) ((NonNullType) type).getType()).getType()).getName()));
                    fieldType.setTokens("?[]");
                }
                if (((ListType) ((NonNullType) type).getType()).getType() instanceof NonNullType) {
                    if (((NonNullType) ((ListType) ((NonNullType) type).getType()).getType())
                            .getType() instanceof TypeName) {
                        fieldType.setName(getBallerinaTypeName(graphQLSchema,
                                ((TypeName) ((NonNullType) ((ListType) ((NonNullType) type).getType()).getType())
                                        .getType()).getName()));
                        fieldType.setTokens("[]");
                    }
                }
            }
        }
        if (type instanceof ListType) {
            if (((ListType) type).getType() instanceof TypeName) {
                fieldType.setName(getBallerinaTypeName(graphQLSchema,
                        ((TypeName) ((ListType) type).getType()).getName()));
                fieldType.setTokens("?[]?");
            }
            if (((ListType) type).getType() instanceof NonNullType) {
                if (((NonNullType) ((ListType) type).getType()).getType() instanceof TypeName) {
                    fieldType.setName(getBallerinaTypeName(graphQLSchema,
                            ((TypeName) ((NonNullType) ((ListType) type).getType()).getType()).getName()));
                    fieldType.setTokens("[]?");
                }
            }
        }
        return fieldType;
    }

    /**
     * Get the custom scalar type names from the GraphQL schema.
     *
     * @param graphQLSchema the instance of the Graphql schema file
     * @return the list of the custom scalar type names
     */
    public static List<String> getCustomScalarTypeNames(GraphQLSchema graphQLSchema) {
        List<String> scalarTypeNames = new ArrayList<>();
        for (GraphQLNamedType graphQLNamedType : graphQLSchema.getAllTypesAsList()) {
            if (graphQLNamedType instanceof GraphQLScalarType && !graphQLNamedType.getName().startsWith("__")
                    && !Utils.isPrimitiveScalarType(graphQLNamedType.getName())) {
                scalarTypeNames.add(graphQLNamedType.getName());
            }
        }
        return scalarTypeNames;
    }

    public static Map<String, FieldDefinition> getObjectTypeFieldDefinitionMap(GraphQLSchema graphQLSchema,
                                                                               String objectTypeName) {
        Map<String, FieldDefinition> objectTypeFieldsMap = new HashMap<>();
        if (graphQLSchema.getType(objectTypeName) instanceof GraphQLObjectType) {
            GraphQLObjectType objectType =
                    ((GraphQLObjectType) graphQLSchema.getType(objectTypeName));
            if (objectType != null) {
                for (GraphQLFieldDefinition field : objectType.getFields()) {
                    objectTypeFieldsMap.put(Utils.escapeIdentifier(field.getName()),
                            field.getDefinition());
                }
            }
        }
        return objectTypeFieldsMap;
    }

    /**
     * Get the enum type names from the GraphQL schema.
     *
     * @param graphQLSchema the instance of the Graphql schema file
     * @return the list of the enum type names
     */
    public static List<String> getEnumTypeNames(GraphQLSchema graphQLSchema) {
        List<String> enumTypeNames = new ArrayList<>();
        for (GraphQLNamedType graphQLNamedType : graphQLSchema.getAllTypesAsList()) {
            if (graphQLNamedType instanceof GraphQLEnumType && !graphQLNamedType.getName().startsWith("__")) {
                enumTypeNames.add(graphQLNamedType.getName());
            }
        }
        return enumTypeNames;
    }

    /**
     * Get the input object type names from the GraphQL schema.
     *
     * @param graphQLSchema the instance of the Graphql schema file
     * @return the list of the input object type names
     */
    public static List<String> getInputObjectTypeNames(GraphQLSchema graphQLSchema) {
        List<String> inputObjectTypeNames = new ArrayList<>();
        for (GraphQLNamedType graphQLNamedType : graphQLSchema.getAllTypesAsList()) {
            if (graphQLNamedType instanceof GraphQLInputObjectType) {
                inputObjectTypeNames.add(graphQLNamedType.getName());
            }
        }
        return inputObjectTypeNames;
    }

    /**
     * Get the input object type fields map based on the input object type name from the GraphQL schema.
     *
     * @param graphQLSchema       the instance of the Graphql schema file
     * @param inputObjectTypeName the input object type name
     * @return the input object type fields map
     */
    public static Map<String, FieldType> getInputTypeFieldsMap(GraphQLSchema graphQLSchema,
                                                               String inputObjectTypeName) throws ValidationException {
        Map<String, FieldType> inputTypeFieldsMap = new HashMap<>();
        if (graphQLSchema.getType(inputObjectTypeName) instanceof GraphQLInputObjectType) {
            GraphQLInputObjectType inputObjectType =
                    ((GraphQLInputObjectType) graphQLSchema.getType(inputObjectTypeName));
            if (inputObjectType != null) {
                for (GraphQLInputObjectField field : inputObjectType.getFields()) {
                    InputValueDefinition inputValueDefinition = field.getDefinition();
                    if (inputValueDefinition == null) {
                        throw new ValidationException("Field definition cannot be null");
                    }
                    inputTypeFieldsMap.put(Utils.escapeIdentifier(field.getName()),
                            SpecReader.getFieldType(graphQLSchema, inputValueDefinition.getType()));

                }
            }
        }
        return inputTypeFieldsMap;
    }
}
