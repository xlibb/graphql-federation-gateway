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
import io.xlibb.gateway.exception.GatewayGenerationException;

import java.util.List;

import static io.xlibb.gateway.generator.common.CommonUtils.getClientFromFieldDefinition;
import static io.xlibb.gateway.generator.common.CommonUtils.getTypeFromFieldDefinition;

/**
 * Class to hold data related to a graphql type field.
 */
public class FieldData {
    private final String fieldName;
    private final String type;
    private final String client;


    FieldData(String fieldName, FieldDefinition fieldDefinition,
              List<GraphQLAppliedDirective> joinTypeDirectivesOnParent)
            throws GatewayGenerationException {
        this.fieldName = fieldName;
        this.type = getTypeFromFieldDefinition(fieldDefinition);
        this.client = getClientFromFieldDefinition(fieldDefinition, joinTypeDirectivesOnParent);
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getType() {
        return type;
    }

    public String getClient() {
        return client;
    }

    public boolean isID() {
        return this.type.equals("ID");
    }

}
