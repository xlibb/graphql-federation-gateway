import ballerina/graphql;

// Prepare query string to resolve by reference.
isolated function wrapWithEntityRepresentation(string typename, map<json>[] fieldsRequiredToFetch, string fieldQuery) returns string {
    string[] representations = [];
    foreach var entry in fieldsRequiredToFetch {
        string keyValueString = getKeyValueString(entry);
        representations.push(string `{ __typename: "${typename}", ${keyValueString} }`);
    }
    return string `query{
        _entities(
            representations: [${",".'join(...representations)}]
        ) {
            ... on ${typename} {
                ${fieldQuery}
            }
        }
    }`;
}

// Prepare key value string.
isolated function getKeyValueString(map<json> fieldMap) returns string {
    string keyValueString = "";
    foreach var [key, value] in fieldMap.entries() {
        if value is map<json> {
            keyValueString = keyValueString + string `${key}: { ${getKeyValueString(value)} } `;
        } else {
            keyValueString = keyValueString + string `${key}: ${getParamAsString(value)} `;
        }
    }
    return keyValueString;
}

// Prepare query string to resolve by query.
isolated function wrapwithQuery(string root, string? fieldQuery = (), map<string>? args = ()) returns string {
    if args is () {
        if fieldQuery is () {
            return string `query
                {
                    ${root}
                }`;
        }
        return string `query
            {
                ${root}{
                ${fieldQuery}
            }
        }`;
    } else {
        string[] argsList = [];
        foreach var [key, value] in args.entries() {
            argsList.push(string `${key}: ${value}`);
        }
        if fieldQuery is () {
            return string `query
                {
                    ${root}(${string:'join(", ", ...argsList)})
                }`;
        }
        return string `query
            {
                ${root}(${string:'join(", ", ...argsList)}){
                ${fieldQuery}
            }
        }`;
    }
}

isolated function wrapwithMutation(string root, string? fieldQuery = (), map<string>? args = ()) returns string {
    if args is () {
        if fieldQuery is () {
            return string `mutation
                {
                    ${root}
                }`;
        }
        return string `mutation
            {
                ${root}{
                ${fieldQuery}
            }
        }`;
    } else {
        string[] argsList = [];
        foreach var [key, value] in args.entries() {
            argsList.push(string `${key}: ${value}`);
        }
        if fieldQuery is () {
            return string `mutation
                {
                    ${root}(${string:'join(", ", ...argsList)})
                }`;
        }
        return string `mutation
            {
                ${root}(${string:'join(", ", ...argsList)}){
                ${fieldQuery}
            }
        }`;
    }
}

isolated function convertPathToStringArray((string|int)[] path) returns string[] {
    return path.'map(isolated function(string|int element) returns string {
        return element is int ? "@" : element;
    });
}

isolated function getOfType(graphql:__Type schemaType) returns graphql:__Type {
    graphql:__Type? ofType = schemaType?.ofType;
    if ofType is () {
        return schemaType;
    } else {
        return getOfType(ofType);
    }
}

isolated function getParamAsString(anydata param) returns string {
    if param is string {
        return "\"" + param + "\"";
    } else if param.toJson() is map<json> {
        map<json> paramMap = <map<json>>param.toJson();
        string[] paramList = [];
        foreach var [key, value] in paramMap.entries() {
            if value is () {
                continue;
            }
            paramList.push(string `${key}: ${getParamAsString(value)}`);
        }
        return string `{${string:'join(", ", ...paramList)}}`;
    } else {
        return param.toString();
    }
}

isolated function addErrorsToGraphqlContext(graphql:Context context, graphql:ErrorDetail|graphql:ErrorDetail[] errors) {
    if errors is graphql:ErrorDetail {
        graphql:__addError(context, errors);
    } else {
        foreach graphql:ErrorDetail e in errors {
            graphql:__addError(context, e);
        }
    }
}

isolated function appendUnableToResolveErrorDetail(graphql:ErrorDetail[] errors,
        graphql:Field 'field) {
    errors.push({
        message: "Unable to resolve " + 'field.getName(),
        path: 'field.getPath()
    });
}

isolated function appendErrorDetailsFromResponse(graphql:ErrorDetail[] errors, graphql:ErrorDetail[]? responseErrors) {
    if responseErrors is () {
        return;
    }
    foreach graphql:ErrorDetail e in responseErrors {
        errors.push({
            message: e.message,
            path: e.path
        });
    }
}

isolated function mergeToResultJson(map<json> resultJson, map<json> responseJson) {
    foreach var [key, value] in responseJson.entries() {
        if resultJson[key] is map<json> {
            mergeToResultJson(<map<json>>resultJson[key], <map<json>>value);
        } else {
            resultJson[key] = value;
        }
    }
}
