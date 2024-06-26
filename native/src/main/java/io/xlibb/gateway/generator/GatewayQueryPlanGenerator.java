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

package io.xlibb.gateway.generator;

import graphql.language.EnumValue;
import graphql.language.StringValue;
import graphql.schema.GraphQLAppliedDirective;
import graphql.schema.GraphQLAppliedDirectiveArgument;
import graphql.schema.GraphQLSchema;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.xlibb.gateway.exception.GatewayGenerationException;
import io.xlibb.gateway.exception.ValidationException;
import io.xlibb.gateway.graphql.SpecReader;
import io.xlibb.gateway.graphql.components.FieldData;
import io.xlibb.gateway.graphql.components.JoinGraph;
import io.xlibb.gateway.graphql.components.SchemaTypes;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createIntersectionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createKeySpecifierNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModuleVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSpecificFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTableConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTableTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.BITWISE_AND_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FINAL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.GT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.KEY_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.LT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.READONLY_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.READONLY_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TABLE_KEYWORD;
import static io.xlibb.gateway.generator.CommonUtils.ARGUMENT_GRAPH;
import static io.xlibb.gateway.generator.CommonUtils.ARGUMENT_KEY;
import static io.xlibb.gateway.generator.CommonUtils.CLIENT_NAME_DECLARATION;
import static io.xlibb.gateway.generator.CommonUtils.CLIENT_NAME_PLACEHOLDER;
import static io.xlibb.gateway.generator.CommonUtils.CLIENT_NAME_VALUE_PLACEHOLDER;
import static io.xlibb.gateway.generator.CommonUtils.DIRECTIVE_JOIN_TYPE;
import static io.xlibb.gateway.generator.CommonUtils.getJoinGraphs;

/**
 * Class to generate the query plan for the gateway.
 */
public class GatewayQueryPlanGenerator {

    private final GraphQLSchema graphQLSchema;
    private final Map<String, JoinGraph> joinGraphs;
    private final SchemaTypes schemaTypes;

    public GatewayQueryPlanGenerator(GraphQLSchema graphQLSchema) throws GatewayGenerationException,
                                                                         ValidationException {
        this.graphQLSchema = graphQLSchema;
        this.joinGraphs = getJoinGraphs(graphQLSchema);
        this.schemaTypes = new SchemaTypes(graphQLSchema);
    }

    public String generateSrc() throws GatewayGenerationException {
        try {
            SyntaxTree syntaxTree = generateSyntaxTree();
            return Formatter.format(syntaxTree).toString();
        } catch (ValidationException | FormatterException e) {
            throw new GatewayGenerationException("Error while generating the gateway types");
        }
    }

    private SyntaxTree generateSyntaxTree() throws ValidationException {
        List<ModuleMemberDeclarationNode> nodeList = new LinkedList<>();
        NodeList<ImportDeclarationNode> importsList = createEmptyNodeList();

        addClientConstantDeclarations(nodeList);
        addQueryPlanTableNode(nodeList);

        NodeList<ModuleMemberDeclarationNode> members = createNodeList(nodeList.toArray(
                new ModuleMemberDeclarationNode[0]));

        ModulePartNode modulePartNode = createModulePartNode(
                importsList,
                members,
                createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private void addQueryPlanTableNode(List<ModuleMemberDeclarationNode> nodeList) {
        ModuleMemberDeclarationNode tableNode = createModuleVariableDeclarationNode(
                null,
                createToken(PUBLIC_KEYWORD),
                createNodeList(createToken(FINAL_KEYWORD)),
                createTypedBindingPatternNode(
                        createIntersectionTypeDescriptorNode(
                                createBuiltinSimpleNameReferenceNode(READONLY_TYPE_DESC, createToken(READONLY_KEYWORD)),
                                createToken(BITWISE_AND_TOKEN),
                                createTableTypeDescriptorNode(
                                        createToken(TABLE_KEYWORD),
                                        createTypeParameterNode(
                                                createToken(LT_TOKEN),
                                                createSimpleNameReferenceNode(
                                                        createIdentifierToken("QueryPlanEntry",
                                                                createEmptyMinutiaeList(),
                                                                createEmptyMinutiaeList()
                                                        )

                                                ),
                                                createToken(GT_TOKEN)
                                        ),
                                        createKeySpecifierNode(
                                                createToken(KEY_KEYWORD),
                                                createToken(OPEN_PAREN_TOKEN),
                                                createSeparatedNodeList(createIdentifierToken("typename")),
                                                createToken(CLOSE_PAREN_TOKEN)
                                        )
                                )
                        ),
                        createCaptureBindingPatternNode(createIdentifierToken("queryPlan"))
                ),
                createToken(EQUAL_TOKEN),
                createTableConstructorExpressionNode(
                        createToken(TABLE_KEYWORD),
                        null,
                        createToken(OPEN_BRACKET_TOKEN),
                        getTableRows(),
                        createToken(CLOSE_BRACKET_TOKEN)
                ),
                createToken(SEMICOLON_TOKEN)
        );
        nodeList.add(tableNode);
    }

    private SeparatedNodeList<Node> getTableRows() {
        List<Node> nodeList = new ArrayList<>();
        List<String> names = CommonUtils.getCustomDefinedObjectTypeNames(graphQLSchema);

        int namesLength = names.size();
        int i = 0;
        for (String name : names) {
            MappingConstructorExpressionNode entry = createMappingConstructorExpressionNode(
                    createToken(OPEN_BRACE_TOKEN),
                    getTableEntry(name),
                    createToken(CLOSE_BRACE_TOKEN)
            );

            nodeList.add(entry);
            i += 1;
            if (i < namesLength) {
                nodeList.add(createToken(COMMA_TOKEN));
            }
        }
        return createSeparatedNodeList(nodeList);
    }

    private SeparatedNodeList<MappingFieldNode> getTableEntry(String name) {
        List<Node> nodeList = new ArrayList<>();
        nodeList.add(
                createSpecificFieldNode(
                        null,
                        createIdentifierToken("typename"),
                        createToken(COLON_TOKEN),
                        createBasicLiteralNode(
                                STRING_LITERAL,
                                createLiteralValueToken(
                                        STRING_LITERAL_TOKEN,
                                        "\"" + name + "\"",
                                        createEmptyMinutiaeList(),
                                        createEmptyMinutiaeList()
                                )
                        )
                )
        );
        nodeList.add(createToken(COMMA_TOKEN));

        Map<String, String> keys = getKeys(name);
        Node keyNode = createSpecificFieldNode(
                null,
                createIdentifierToken("keys"),
                createToken(COLON_TOKEN),
                createMappingConstructorExpressionNode(
                        createToken(OPEN_BRACE_TOKEN),
                        getKeysNodeList(keys),
                        createToken(CLOSE_BRACE_TOKEN)
                )
        );
        nodeList.add(keyNode);
        nodeList.add(createToken(COMMA_TOKEN));
        Node fieldNode = createSpecificFieldNode(
                null,
                createIdentifierToken("fields"),
                createToken(COLON_TOKEN),
                createTableConstructorExpressionNode(
                        createToken(TABLE_KEYWORD),
                        null,
                        createToken(OPEN_BRACKET_TOKEN),
                        getFieldTableRows(name),
                        createToken(CLOSE_BRACKET_TOKEN)
                )
        );
        nodeList.add(fieldNode);
        return createSeparatedNodeList(nodeList);
    }

    private SeparatedNodeList<MappingFieldNode> getKeysNodeList(Map<String, String> keys) {
        List<Node> nodeList = new ArrayList<>();

        int keysLength = keys.size();
        int i = 0;

        for (Map.Entry<String, String> entry : keys.entrySet()) {
            nodeList.add(
                    createSpecificFieldNode(
                            null,
                            createIdentifierToken("\"" + entry.getKey() + "\""),
                            createToken(COLON_TOKEN),
                            createBasicLiteralNode(
                                    STRING_LITERAL,
                                    createLiteralValueToken(
                                            STRING_LITERAL_TOKEN,
                                            "\"" + entry.getValue() + "\"",
                                            createEmptyMinutiaeList(),
                                            createEmptyMinutiaeList()
                                    )
                            )
                    )
            );
            i += 1;
            if (i < keysLength) {
                nodeList.add(createToken(COMMA_TOKEN));
            }
        }

        return createSeparatedNodeList(nodeList);
    }

    private Map<String, String> getKeys(String name) {
        Map<String, String> keys = new HashMap<>();
        List<GraphQLAppliedDirective> directives = SpecReader.getObjectTypeDirectives(this.graphQLSchema, name);

        for (GraphQLAppliedDirective directive : directives) {
            if (directive.getName().equals(DIRECTIVE_JOIN_TYPE)) {
                try {
                    String graph = getGraphOfJoinTypeArgument(directive);
                    String key = getKeyOfJoinTypeArgument(name, directive);
                    keys.put(graph, key);
                } catch (GatewayGenerationException | ValidationException ignored) {

                }
            }
        }
        return keys;
    }

    private String getGraphOfJoinTypeArgument(GraphQLAppliedDirective directive)
            throws GatewayGenerationException, ValidationException {
        for (GraphQLAppliedDirectiveArgument argument : directive.getArguments()) {
            if (argument.getName().equals(ARGUMENT_GRAPH)) {
                Object value = argument.getArgumentValue().getValue();
                if (value == null) {
                    throw new ValidationException("graph argument value is null");
                }
                String graphEnumName =
                        ((EnumValue) value).getName();
                return this.joinGraphs.get(graphEnumName).getName();
            }
        }
        throw new GatewayGenerationException("No graph argument found in @join__type directive");
    }

    private String getKeyOfJoinTypeArgument(String name, GraphQLAppliedDirective directive)
            throws GatewayGenerationException {
        for (GraphQLAppliedDirectiveArgument argument : directive.getArguments()) {
                if (argument.getName().equals(ARGUMENT_KEY)) {
                        Object argumentValue = argument.getArgumentValue().getValue();
                        if (argumentValue == null) {
                                break;
                        } 
                        return ((StringValue) argumentValue).getValue();
                }
        }

        for (FieldData field : schemaTypes.getFieldsOfType(name)) {
                if (field.isID()) {
                        return field.getFieldName();
                }
        }

        throw new GatewayGenerationException("No key argument found in @join__type directive");
    }

    private SeparatedNodeList<Node> getFieldTableRows(String name) {
        List<Node> nodeList = new ArrayList<>();
        List<FieldData> fields = schemaTypes.getFieldsOfType(name);

        int fieldsLength = fields.size();
        int i = 0;

        for (FieldData field : fields) {
            if (field.isID()) {
                continue;
            }
            MappingConstructorExpressionNode entryNode = createMappingConstructorExpressionNode(
                    createToken(OPEN_BRACE_TOKEN),
                    getFieldTableEntry(field),
                    createToken(CLOSE_BRACE_TOKEN)
            );
            nodeList.add(entryNode);
            i += 1;
            if (i < fieldsLength) {
                nodeList.add(createToken(COMMA_TOKEN));
            }
        }
        return createSeparatedNodeList(nodeList);
    }

    private SeparatedNodeList<MappingFieldNode> getFieldTableEntry(FieldData data) {
        List<Node> fieldNodeList = new ArrayList<>();
        fieldNodeList.add(
                createSpecificFieldNode(
                        null,
                        createIdentifierToken("name"),
                        createToken(COLON_TOKEN),
                        createBasicLiteralNode(
                                STRING_LITERAL,
                                createLiteralValueToken(
                                        STRING_LITERAL_TOKEN,
                                        "\"" + data.getFieldName() + "\"",
                                        createEmptyMinutiaeList(),
                                        createEmptyMinutiaeList()
                                )
                        )
                )
        );
        fieldNodeList.add(createToken(COMMA_TOKEN));
        fieldNodeList.add(
                createSpecificFieldNode(
                        null,
                        createIdentifierToken("'type"),
                        createToken(COLON_TOKEN),
                        createBasicLiteralNode(
                                STRING_LITERAL,
                                createLiteralValueToken(
                                        STRING_LITERAL_TOKEN,
                                        "\"" + data.getType() + "\"",
                                        createEmptyMinutiaeList(),
                                        createEmptyMinutiaeList()
                                )
                        )
                )
        );
        fieldNodeList.add(createToken(COMMA_TOKEN));
        fieldNodeList.add(
                createSpecificFieldNode(
                        null,
                        createIdentifierToken("'client"),
                        createToken(COLON_TOKEN),
                        createBasicLiteralNode(
                                STRING_LITERAL,
                                createLiteralValueToken(
                                        STRING_LITERAL_TOKEN,
                                        data.getClient(),
                                        createEmptyMinutiaeList(),
                                        createEmptyMinutiaeList()
                                )
                        )
                )
        );
        return createSeparatedNodeList(fieldNodeList);
    }

    private void addClientConstantDeclarations(List<ModuleMemberDeclarationNode> nodeList) throws ValidationException {
        for (Map.Entry<String, JoinGraph> entry :
                getJoinGraphs(this.graphQLSchema).entrySet()) {
            nodeList.add(NodeParser.parseModuleMemberDeclaration(
                    CLIENT_NAME_DECLARATION.replace(CLIENT_NAME_PLACEHOLDER, entry.getKey())
                            .replace(CLIENT_NAME_VALUE_PLACEHOLDER, entry.getValue().getName())
            ));
        }
    }
}
