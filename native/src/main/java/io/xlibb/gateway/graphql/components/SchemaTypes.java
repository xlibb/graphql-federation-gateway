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

package io.xlibb.gateway.graphql.components;

import graphql.language.FieldDefinition;
import graphql.schema.GraphQLAppliedDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import io.xlibb.gateway.exception.GatewayGenerationException;
import io.xlibb.gateway.generator.CommonUtils;
import io.xlibb.gateway.graphql.SpecReader;
import io.xlibb.gateway.graphql.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class to hold data related to graphql types in a given graphql schema.
 */
public class SchemaTypes {
    private final Map<String, List<FieldData>> fieldDataMap;

    public SchemaTypes(GraphQLSchema graphQLSchema) throws GatewayGenerationException {
        List<String> names = CommonUtils.getCustomDefinedObjectTypeNames(graphQLSchema);

        this.fieldDataMap = new HashMap<>();
        for (String name : names) {
            fieldDataMap.put(name, getFieldsOfType(name, graphQLSchema));
        }
    }

    /**
     * Return the list of fields of the given type.
     *
     * @param name Type name
     * @return List of fields
     */
    public List<FieldData> getFieldsOfType(String name) {
        return fieldDataMap.get(name);
    }

    /**
     * Return the list of fields of the given type.
     *
     * @param typeName      Type name
     * @param graphQLSchema GraphQL schema
     * @return List of fields
     */
    private List<FieldData> getFieldsOfType(String typeName, GraphQLSchema graphQLSchema)
            throws GatewayGenerationException {
        List<FieldData> fields = new ArrayList<>();

        List<GraphQLAppliedDirective> joinTypeDirectives =
                SpecReader.getObjectTypeDirectives(graphQLSchema, typeName).stream().filter(
                        directive -> directive.getName().equals(CommonUtils.DIRECTIVE_JOIN_TYPE)
                ).collect(Collectors.toList());
        for (Map.Entry<String, FieldDefinition> entry :
                SpecReader.getObjectTypeFieldDefinitionMap(graphQLSchema, typeName).entrySet()) {
            FieldData field = new FieldData(entry.getKey(), entry.getValue(), joinTypeDirectives);
            if (field.getClient() != null) {
                fields.add(field);
            }
        }
        return fields;
    }

    /**
     * Get the object type fields map based on the input object type name from the GraphQL schema.
     *
     * @param graphQLSchema  the instance of the Graphql schema file
     * @param objectTypeName the object type name
     * @return the object type fields map
     */
    public static Map<String, FieldType> getObjectTypeFieldsMap(GraphQLSchema graphQLSchema, String objectTypeName) {
        Map<String, FieldType> objectTypeFieldsMap = new HashMap<>();
        if (graphQLSchema.getType(objectTypeName) instanceof GraphQLObjectType) {
            GraphQLObjectType objectType =
                    ((GraphQLObjectType) graphQLSchema.getType(objectTypeName));
            if (objectType != null) {
                for (GraphQLFieldDefinition field : objectType.getFields()) {
                    FieldDefinition fieldDefinition = field.getDefinition();
                    if (fieldDefinition == null) {
                        continue;
                    }
                    objectTypeFieldsMap.put(Utils.escapeIdentifier(field.getName()),
                            SpecReader.getFieldType(graphQLSchema, fieldDefinition.getType()));
                }
            }
        }
        return objectTypeFieldsMap;
    }
}
