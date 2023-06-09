import ballerina/graphql;
import ballerina/log;

final graphql:Client REVIEWS_CLIENT = check new graphql:Client("http://localhost:4002");
final graphql:Client PRODUCT_CLIENT = check new graphql:Client("http://localhost:4001");

isolated function getClient(string clientName) returns graphql:Client {
    match clientName {
        "reviews" => {
            return REVIEWS_CLIENT;
        }
        "product" => {
            return PRODUCT_CLIENT;
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

    isolated resource function get product(graphql:Field 'field, graphql:Context context, string id) returns Product?|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, PRODUCT);
        string fieldString = classifier.getFieldString();
        UnresolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("product", fieldString, {"id": getParamAsString(id)});
        productResponse|graphql:ClientError response = PRODUCT_CLIENT->execute(queryString);
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

    isolated resource function get products(graphql:Field 'field, graphql:Context context) returns Product[]|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, PRODUCT);
        string fieldString = classifier.getFieldString();
        UnresolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("products", fieldString);
        productsResponse|graphql:ClientError response = PRODUCT_CLIENT->execute(queryString);
        Product[] result = [];
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

    isolated resource function get reviews(graphql:Field 'field, graphql:Context context, string productId) returns Review[]|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, REVIEWS);
        string fieldString = classifier.getFieldString();
        UnresolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("reviews", fieldString, {"productId": getParamAsString(productId)});
        reviewsResponse|graphql:ClientError response = REVIEWS_CLIENT->execute(queryString);
        Review[] result = [];
        graphql:ErrorDetail[] errors = [];
        if response is graphql:ClientError {
            appendUnableToResolveErrorDetail(errors, 'field);
        } else {
            result = response.data.reviews;
            appendErrorDetailsFromResponse(errors, response?.errors);
        }
        Resolver resolver = new (queryPlan, result.toJson(), "Review", propertiesNotResolved, ["reviews"], errors);
        json finalResult = resolver.getResult();
        addErrorsToGraphqlContext(context, errors);
        return finalResult.cloneWithType();
    }

}
