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

package io.xlibb.gateway.graphql.components;

import graphql.language.Directive;
import graphql.language.EnumValueDefinition;
import graphql.language.StringValue;
import graphql.schema.GraphQLEnumValueDefinition;
import io.xlibb.gateway.exception.ValidationException;

/**
 * Class to hold the information about join__Graph Enum.
 */
public class JoinGraph {
    private final String name;
    private final String url;

    public JoinGraph(GraphQLEnumValueDefinition element) throws ValidationException {
        EnumValueDefinition definition = element.getDefinition();
        if (definition == null) {
            throw new ValidationException("Enum value definition cannot be null");
        }
        var node = definition.getChildren().get(0);
        this.name = ((StringValue) ((Directive) node).getArgument("name").getValue()).getValue();
        this.url = ((StringValue) ((Directive) node).getArgument("url").getValue()).getValue();
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
