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

import graphql.language.EnumValue;
import graphql.language.InputValueDefinition;
import graphql.schema.GraphQLAppliedDirective;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLSchemaElement;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.xlibb.gateway.GatewayProject;
import io.xlibb.gateway.exception.GatewayGenerationException;
import io.xlibb.gateway.exception.ValidationException;
import io.xlibb.gateway.generator.common.CommonUtils;
import io.xlibb.gateway.graphql.SpecReader;
import io.xlibb.gateway.graphql.components.FieldType;
import io.xlibb.gateway.graphql.components.JoinGraph;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.xlibb.gateway.generator.common.CommonUtils.getJoinGraphs;
import static io.xlibb.gateway.generator.common.CommonUtils.getResourceTemplateFilePath;
import static io.xlibb.gateway.generator.common.Constants.BALLERINA_GRAPHQL_IMPORT_STATEMENT;
import static io.xlibb.gateway.generator.common.Constants.BALLERINA_LOG_IMPORT_STATEMENT;
import static io.xlibb.gateway.generator.common.Constants.BASIC_RESPONSE_TYPE_PLACEHOLDER;
import static io.xlibb.gateway.generator.common.Constants.CLIENT_NAME_PLACEHOLDER;
import static io.xlibb.gateway.generator.common.Constants.CLIENT_NAME_VALUE_PLACEHOLDER;
import static io.xlibb.gateway.generator.common.Constants.CONFIGURABLE_PORT_STATEMENT;
import static io.xlibb.gateway.generator.common.Constants.FUNCTION_PARAM_PLACEHOLDER;
import static io.xlibb.gateway.generator.common.Constants.GET_CLIENT_FUNCTION_TEMPLATE_FILE;
import static io.xlibb.gateway.generator.common.Constants.GRAPHQL_CLIENT_DECLARATION_STATEMENT;
import static io.xlibb.gateway.generator.common.Constants.MATCH_CLIENT_STATEMENTS_PLACEHOLDER;
import static io.xlibb.gateway.generator.common.Constants.MATCH_CLIENT_STATEMENT_TEMPLATE;
import static io.xlibb.gateway.generator.common.Constants.QUERY_ARGS_PLACEHOLDER;
import static io.xlibb.gateway.generator.common.Constants.QUERY_PLACEHOLDER;
import static io.xlibb.gateway.generator.common.Constants.REMOTE_FUNCTION_TEMPLATE_FILE;
import static io.xlibb.gateway.generator.common.Constants.RESOURCE_FUNCTIONS_PLACEHOLDER;
import static io.xlibb.gateway.generator.common.Constants.RESOURCE_FUNCTION_TEMPLATE_FILE;
import static io.xlibb.gateway.generator.common.Constants.RESPONSE_TYPE_PLACEHOLDER;
import static io.xlibb.gateway.generator.common.Constants.SERVICE_DECLARATION_TEMPLATE_FILE;
import static io.xlibb.gateway.generator.common.Constants.URL_PLACEHOLDER;

enum FunctionType {
    QUERY,
    MUTATION
};

/**
 * Class to generate service code for the gateway.
 */
public class GatewayServiceGenerator {
    private final GatewayProject project;
    private final Map<String, JoinGraph> joinGraphs;

    public GatewayServiceGenerator(GatewayProject project) throws ValidationException {
        this.project = project;
        joinGraphs = getJoinGraphs(project.getSchema());
    }

    public String generateSrc() throws GatewayGenerationException {
        try {
            SyntaxTree syntaxTree = generateSyntaxTree();
            return Formatter.format(syntaxTree).toString();
        } catch (IOException | FormatterException e) {
            throw new GatewayGenerationException("Error while generating the gateway services");
        }
    }

    private SyntaxTree generateSyntaxTree() throws GatewayGenerationException, IOException {
        NodeList<ImportDeclarationNode> importsList = createNodeList(
                NodeParser.parseImportDeclaration(BALLERINA_GRAPHQL_IMPORT_STATEMENT),
                NodeParser.parseImportDeclaration(BALLERINA_LOG_IMPORT_STATEMENT)
        );

        List<ModuleMemberDeclarationNode> nodes = new ArrayList<>(getClientDeclarations());
        nodes.add(getGetClientFunction());
        nodes.add(NodeParser.parseModuleMemberDeclaration(CONFIGURABLE_PORT_STATEMENT));
        nodes.add(getServiceDeclaration());

        NodeList<ModuleMemberDeclarationNode> members = createNodeList(
                nodes.toArray(
                        new ModuleMemberDeclarationNode[0])
        );

        ModulePartNode modulePartNode = createModulePartNode(
                importsList,
                members,
                createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private ModuleMemberDeclarationNode getServiceDeclaration() throws GatewayGenerationException, IOException {
        String resourceFunctions = String.join(System.lineSeparator(), getServiceFunctions());
        String serviceTemplate = Files.readString(getResourceTemplateFilePath(project.getTempDir(),
                SERVICE_DECLARATION_TEMPLATE_FILE)).replaceAll(RESOURCE_FUNCTIONS_PLACEHOLDER, resourceFunctions);
        return NodeParser.parseModuleMemberDeclaration(serviceTemplate);
    }

    private List<String> getServiceFunctions() throws GatewayGenerationException, IOException {
        List<String> resourceFunctions = new ArrayList<>();
        for (GraphQLSchemaElement graphQLObjectType : CommonUtils.getQueryTypes(project.getSchema())) {
            resourceFunctions.add(getServiceFunction(FunctionType.QUERY, graphQLObjectType));
        }
        for (GraphQLSchemaElement graphQLObjectType : CommonUtils.getMutationTypes(project.getSchema())) {
            resourceFunctions.add(getServiceFunction(FunctionType.MUTATION, graphQLObjectType));
        }
        return resourceFunctions;
    }

    private String getServiceFunction(FunctionType functionType, GraphQLSchemaElement graphQLSchemaElement)
            throws IOException, GatewayGenerationException {
        String template;
        String type;
        if (functionType == FunctionType.QUERY) {
            template = Files.readString(getResourceTemplateFilePath(project.getTempDir(),
                    RESOURCE_FUNCTION_TEMPLATE_FILE));
            type = "Query";
        } else if (functionType == FunctionType.MUTATION) {
            template = Files.readString(getResourceTemplateFilePath(project.getTempDir(),
                    REMOTE_FUNCTION_TEMPLATE_FILE));
            type = "Mutation";
        } else {
            throw new GatewayGenerationException("Unsupported function type");
        }
        return template.replaceAll(QUERY_PLACEHOLDER,
                        ((GraphQLFieldDefinition) graphQLSchemaElement).getName())
                .replaceAll(FUNCTION_PARAM_PLACEHOLDER,
                        getArgumentString(graphQLSchemaElement))
                .replaceAll(RESPONSE_TYPE_PLACEHOLDER,
                        CommonUtils.getTypeNameFromGraphQLType(
                                ((GraphQLFieldDefinition) graphQLSchemaElement).getType()))
                .replaceAll(BASIC_RESPONSE_TYPE_PLACEHOLDER,
                        CommonUtils.getBasicTypeNameFromGraphQLType(
                                ((GraphQLFieldDefinition) graphQLSchemaElement).getType()))
                .replaceAll(CLIENT_NAME_PLACEHOLDER,
                        getClientNameFromFieldDefinition((GraphQLFieldDefinition) graphQLSchemaElement, type))
                .replaceAll(QUERY_ARGS_PLACEHOLDER, getQueryArguments(graphQLSchemaElement));
    }

    private ModuleMemberDeclarationNode getGetClientFunction()
            throws IOException {
        List<String> matchClientCases = new ArrayList<>();
        for (Map.Entry<String, JoinGraph> entry : joinGraphs.entrySet()) {
            matchClientCases.add(
                    MATCH_CLIENT_STATEMENT_TEMPLATE
                            .replace(CLIENT_NAME_PLACEHOLDER, entry.getKey())
                            .replace(CLIENT_NAME_VALUE_PLACEHOLDER, entry.getValue().getName())
            );
        }
        String functionTemplate = Files.readString(getResourceTemplateFilePath(project.getTempDir(),
                GET_CLIENT_FUNCTION_TEMPLATE_FILE));
        functionTemplate = functionTemplate.replaceAll(
                MATCH_CLIENT_STATEMENTS_PLACEHOLDER,
                String.join(System.lineSeparator(), matchClientCases)
        );

        return NodeParser.parseModuleMemberDeclaration(functionTemplate);
    }

    private List<ModuleMemberDeclarationNode> getClientDeclarations() {
        List<ModuleMemberDeclarationNode> nodes = new ArrayList<>();
        for (Map.Entry<String, JoinGraph> entry : joinGraphs.entrySet()) {
            String key = entry.getKey();
            JoinGraph value = entry.getValue();
            nodes.add(
                    NodeParser.parseModuleMemberDeclaration(
                            GRAPHQL_CLIENT_DECLARATION_STATEMENT.replace(CLIENT_NAME_PLACEHOLDER, key)
                                    .replace(URL_PLACEHOLDER, value.getUrl())
                    )
            );
        }
        return nodes;
    }

    private String getClientNameFromFieldDefinition(GraphQLFieldDefinition graphQLFieldDefinition, String parentType)
            throws GatewayGenerationException {
        for (GraphQLAppliedDirective directive : graphQLFieldDefinition.getAppliedDirectives()) {
            Object value = directive.getArgument("graph").getArgumentValue().getValue();
            if (directive.getName().equals("join__field") && value instanceof EnumValue) {
                return ((EnumValue) value).getName();
            }
        }

        List<GraphQLAppliedDirective> appliedDirectivesOnParent =
                SpecReader.getObjectTypeDirectives(project.getSchema(), parentType);
        for (GraphQLAppliedDirective directive : appliedDirectivesOnParent) {
            Object value = directive.getArgument("graph").getArgumentValue().getValue();
            if (directive.getName().equals("join__type") && value instanceof EnumValue) {
                return ((EnumValue) value).getName();
            }
        }

        throw new GatewayGenerationException("No client name found: " + graphQLFieldDefinition.getName());
    }

    private String getArgumentString(GraphQLSchemaElement graphQLObjectType) throws GatewayGenerationException {
        StringBuilder arguments = new StringBuilder();
        for (GraphQLArgument argument : ((GraphQLFieldDefinition) graphQLObjectType).getArguments()) {
            arguments.append(", ");
            InputValueDefinition inputValueDefinition = argument.getDefinition();
            if (inputValueDefinition != null) {
                FieldType fieldType = SpecReader.getFieldType(project.getSchema(),
                        inputValueDefinition.getType());
                if (inputValueDefinition.getDefaultValue() != null) {
                    arguments.append(fieldType.getName()).append(fieldType.getTokens()).append(" ")
                            .append(argument.getName()).append(" = ")
                            .append(CommonUtils.getValue(inputValueDefinition.getDefaultValue()));
                } else {
                    arguments.append(fieldType.getName()).append(fieldType.getTokens()).append(" ")
                            .append(argument.getName());
                }
            }
        }
        return arguments.toString();
    }

    private String getQueryArguments(GraphQLSchemaElement graphQLObjectType) {
        StringBuilder argumentString = new StringBuilder();
        List<GraphQLArgument> arguments = ((GraphQLFieldDefinition) graphQLObjectType).getArguments();
        if (arguments.size() > 0) {
            argumentString.append(", ");
            argumentString.append("{");
            argumentString.append(getQueryArgumentList(arguments));
            argumentString.append("}");
        }
        return argumentString.toString();
    }

    private String getQueryArgumentList(List<GraphQLArgument> arguments) {
        StringBuilder argumentList = new StringBuilder();
        int size = arguments.size();
        int count = 0;
        for (GraphQLArgument argument : arguments) {
            argumentList.append("\"").append(argument.getName()).append("\": getParamAsString(")
                    .append(argument.getName()).append(")");

            if (size < ++count) {
                argumentList.append(", ");
            }
        }
        return argumentList.toString();
    }
}
