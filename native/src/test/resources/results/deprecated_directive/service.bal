import ballerina/graphql;
import ballerina/log;

final graphql:Client PRODUCTS_CLIENT = check new graphql:Client("http://localhost:9091");

isolated function getClient(string clientName) returns graphql:Client {
    match clientName {
        "products" => {
            return PRODUCTS_CLIENT;
        }
        _ => {
            panic error("Client not found");
        }
    }
}

configurable int PORT = 9000;

isolated service on new graphql:Listener(PORT) {
    isolated function init() {
        log:printInfo(string `💃 Server ready at port: ${PORT}`);
    }

    # # Deprecated
    # No longer supported
    @deprecated
    isolated resource function get products(graphql:Field 'field, graphql:Context context) returns Product[]|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, PRODUCTS);
        string fieldString = classifier.getFieldString();
        UnresolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("products", fieldString);
        productsResponse|graphql:ClientError response = PRODUCTS_CLIENT->execute(queryString);
        Product[]? result = null;
        graphql:ErrorDetail[] errors = [];
        if response is graphql:ClientError {
            appendUnableToResolveErrorDetail(errors, 'field);
        } else {
            result = response.data.products;
            appendErrorDetailsFromResponse(errors, response?.errors);
        }
        Resolver resolver = new (queryPlan, result.toJson(), "Product", propertiesNotResolved, ["products"], errors);
        json finalResult = resolver.getResult();
        addErrorsToGraphqlContext(context, errors);
        return finalResult.cloneWithType();
    }

    isolated resource function get product(graphql:Field 'field, graphql:Context context, string id) returns Product?|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, PRODUCTS);
        string fieldString = classifier.getFieldString();
        UnresolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("product", fieldString, {"id": getParamAsString(id)});
        productResponse|graphql:ClientError response = PRODUCTS_CLIENT->execute(queryString);
        map<json> result = {"id": id};
        graphql:ErrorDetail[] errors = [];
        if response is graphql:ClientError {
            appendUnableToResolveErrorDetail(errors, 'field);
        } else {
            mergeToResultJson(result, <map<json>>response.data.product.toJson());
            appendErrorDetailsFromResponse(errors, response?.errors);
        }
        Resolver resolver = new (queryPlan, result.toJson(), "Product", propertiesNotResolved, ["product"], errors);
        json finalResult = resolver.getResult();
        addErrorsToGraphqlContext(context, errors);
        return finalResult.cloneWithType();
    }

    # # Deprecated
    # `person` will be removed in the future
    @deprecated
    isolated resource function get person(graphql:Field 'field, graphql:Context context, string? id) returns string?|error {
        string queryString = wrapwithQuery("person", (), {"id": getParamAsString(id)});
        personResponse|graphql:ClientError response = PRODUCTS_CLIENT->execute(queryString);
        if response is graphql:ClientError {
            return error("Unable to resolve : person");
        }
        return response.data.person;
    }
    # # Deprecated
    # No longer supported
    @deprecated
    isolated remote function addProduct(graphql:Field 'field, graphql:Context context, string? name) returns Product?|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, PRODUCTS);
        string fieldString = classifier.getFieldString();
        UnresolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithMutation("addProduct", fieldString, {"name": getParamAsString(name)});
        addProductResponse|graphql:ClientError response = PRODUCTS_CLIENT->execute(queryString);
        if response is graphql:ClientError {
            return error("Unable to perform the operation");
        }
        Product? result = response.data.addProduct;
        graphql:ErrorDetail[] errors = [];
        appendErrorDetailsFromResponse(errors, response?.errors);
        Resolver resolver = new (queryPlan, result.toJson(), "Product", propertiesNotResolved, ["addProduct"], errors);
        json finalResult = resolver.getResult();
        addErrorsToGraphqlContext(context, errors);
        return finalResult.cloneWithType();
    }

    # # Deprecated
    # `addPerson` will be removed in the future
    @deprecated
    isolated remote function addPerson(graphql:Field 'field, graphql:Context context, string? id) returns string|error {
        string queryString = wrapwithMutation("addPerson", (), {"id": getParamAsString(id)});
        addPersonResponse|graphql:ClientError response = PRODUCTS_CLIENT->execute(queryString);
        if response is graphql:ClientError {
            return error("Unable to resolve : addPerson");
        }
        return response.data.addPerson;
    }
}
