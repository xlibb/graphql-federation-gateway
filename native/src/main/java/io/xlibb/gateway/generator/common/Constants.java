package io.xlibb.gateway.generator.common;

/**
 * Class to store constants used in gateway generation.
 */
public class Constants {
    public static final String SERVICE_FILE_NAME = "service.bal";
    public static final String QUERY_PLAN_FILE_NAME = "query_plan.bal";
    public static final String TYPES_FILE_NAME = "types.bal";
    public static final String QUERY_PLACEHOLDER = "@\\{query}";
    public static final String FUNCTION_PARAM_PLACEHOLDER = "@\\{params}";
    public static final String RESPONSE_TYPE_PLACEHOLDER = "@\\{responseType}";
    public static final String BASIC_RESPONSE_TYPE_PLACEHOLDER = "@\\{basicResponseType}";
    public static final String CLIENT_NAME_PLACEHOLDER = "@\\{clientName}";
    public static final String CLIENT_NAME_VALUE_PLACEHOLDER = "@\\{clientNameValue}";
    public static final String QUERY_ARGS_PLACEHOLDER = "@\\{queryArgs}";
    public static final String URL_PLACEHOLDER = "@\\{url}";
    public static final String RESOURCE_FUNCTIONS_PLACEHOLDER = "@\\{resourceFunctions}";
    public static final String MATCH_CLIENT_STATEMENTS_PLACEHOLDER = "@\\{matchClientStatements}";
    public static final String OUTPUT_PATH_PLACEHOLDER = "@\\{outputPath}";
    public static final String SCHEMA_FILENAME_PLACEHOLDER = "@\\{schemaPath}";
    // Constants for the gateway service generation.
    public static final String CONFIGURABLE_PORT_STATEMENT = "configurable int PORT = 9000;";
    public static final String BALLERINA_GRAPHQL_IMPORT_STATEMENT = "import ballerina/graphql;";
    public static final String BALLERINA_LOG_IMPORT_STATEMENT = "import ballerina/log;";
    public static final String GRAPHQL_CLIENT_DECLARATION_STATEMENT =
            "final graphql:Client " + CLIENT_NAME_PLACEHOLDER +
                    "_CLIENT = check new graphql:Client(\"" + URL_PLACEHOLDER + "\");";

    // Constants for the gateway query plan generation.
    public static final String CLIENT_NAME_DECLARATION = "public const string " + CLIENT_NAME_PLACEHOLDER
            + " = \"" + CLIENT_NAME_VALUE_PLACEHOLDER + "\";";
    public static final String MATCH_CLIENT_STATEMENT_TEMPLATE =
            "\"" + CLIENT_NAME_VALUE_PLACEHOLDER + "\" => {return " + CLIENT_NAME_PLACEHOLDER + "_CLIENT;}";

    // File names for templates
    public static final String RESOURCE_FUNCTION_TEMPLATE_FILE = "resource_function.bal.partial";
    public static final String SCALAR_RETURN_TYPE_RESOURCE_FUNCTION_TEMPLATE_FILE =
            "scalar_return_type_resource_function.bal.partial";
    public static final String REMOTE_FUNCTION_TEMPLATE_FILE = "remote_function.bal.partial";
    public static final String SCALAR_RETURN_TYPE_REMOTE_FUNCTION_TEMPLATE_FILE =
            "scalar_return_type_remote_function.bal.partial";
    public static final String GET_CLIENT_FUNCTION_TEMPLATE_FILE = "get_client_function.bal.partial";
    public static final String SERVICE_DECLARATION_TEMPLATE_FILE = "service_declaration.bal.partial";
    public static final String GATEWAY_PROJECT_TEMPLATE_DIRECTORY = "gateway";
    public static final String GATEWAY_TEMPLATE_FILES_DIRECTORY = "gateway_templates";

    // Error messages
    public static final String ERROR_INVALID_SUPERGRAPH_FILE_PATH = "Given supergraph file path is invalid";
    public static final String ERROR_INVALID_OUTPUT_PATH = "Given output path is invalid";
    public static final String ERROR_OUTPUT_PATH_NOT_WRITABLE = "Given out path is not writable";
    public static final String ERROR_INVALID_SCHEMA = "Error occurred while parsing the GraphQL schema";
}
