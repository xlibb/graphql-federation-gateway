import ballerina/graphql;
import ballerina/log;

final graphql:Client MISSIONS_CLIENT = check new graphql:Client("http://localhost:5002");
final graphql:Client ASTRONAUTS_CLIENT = check new graphql:Client("http://localhost:5001");

isolated function getClient(string clientName) returns graphql:Client {
    match clientName {
        "missions" => {
            return MISSIONS_CLIENT;
        }
        "astronauts" => {
            return ASTRONAUTS_CLIENT;
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

    isolated resource function get astronauts(graphql:Field 'field, graphql:Context context) returns Astronaut[]|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, ASTRONAUTS);
        string fieldString = classifier.getFieldString();
        UnresolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("astronauts", fieldString);
        astronautsResponse|graphql:ClientError response = ASTRONAUTS_CLIENT->execute(queryString);
        Astronaut[]? result = null;
        graphql:ErrorDetail[] errors = [];
        if response is graphql:ClientError {
            appendUnableToResolveErrorDetail(errors, 'field);
        } else {
            result = response.data.astronauts;
            appendErrorDetailsFromResponse(errors, response?.errors);
        }
        Resolver resolver = new (queryPlan, result.toJson(), "Astronaut", propertiesNotResolved, ["astronauts"], errors);
        json finalResult = resolver.getResult();
        addErrorsToGraphqlContext(context, errors);
        return finalResult.cloneWithType();
    }

    isolated resource function get astronaut(graphql:Field 'field, graphql:Context context, int id) returns Astronaut?|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, ASTRONAUTS);
        string fieldString = classifier.getFieldString();
        UnresolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("astronaut", fieldString, {"id": getParamAsString(id)});
        astronautResponse|graphql:ClientError response = ASTRONAUTS_CLIENT->execute(queryString);
        map<json> result = {"id": id};
        graphql:ErrorDetail[] errors = [];
        if response is graphql:ClientError {
            appendUnableToResolveErrorDetail(errors, 'field);
        } else {
            mergeToResultJson(result, <map<json>>response.data.astronaut.toJson());
            appendErrorDetailsFromResponse(errors, response?.errors);
        }
        Resolver resolver = new (queryPlan, result.toJson(), "Astronaut", propertiesNotResolved, ["astronaut"], errors);
        json finalResult = resolver.getResult();
        addErrorsToGraphqlContext(context, errors);
        return finalResult.cloneWithType();
    }

    isolated resource function get serviceName(graphql:Field 'field, graphql:Context context) returns string|error {
        string queryString = wrapwithQuery("serviceName", ());
        serviceNameResponse|graphql:ClientError response = ASTRONAUTS_CLIENT->execute(queryString);
        if response is graphql:ClientError {
            return error("Unable to resolve : serviceName");
        }
        return response.data.serviceName;
    }
    isolated resource function get isExist(graphql:Field 'field, graphql:Context context, string name) returns boolean|error {
        string queryString = wrapwithQuery("isExist", (), {"name": getParamAsString(name)});
        isExistResponse|graphql:ClientError response = ASTRONAUTS_CLIENT->execute(queryString);
        if response is graphql:ClientError {
            return error("Unable to resolve : isExist");
        }
        return response.data.isExist;
    }
    isolated resource function get missions(graphql:Field 'field, graphql:Context context) returns Mission[]|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, MISSIONS);
        string fieldString = classifier.getFieldString();
        UnresolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("missions", fieldString);
        missionsResponse|graphql:ClientError response = MISSIONS_CLIENT->execute(queryString);
        Mission[]? result = null;
        graphql:ErrorDetail[] errors = [];
        if response is graphql:ClientError {
            appendUnableToResolveErrorDetail(errors, 'field);
        } else {
            result = response.data.missions;
            appendErrorDetailsFromResponse(errors, response?.errors);
        }
        Resolver resolver = new (queryPlan, result.toJson(), "Mission", propertiesNotResolved, ["missions"], errors);
        json finalResult = resolver.getResult();
        addErrorsToGraphqlContext(context, errors);
        return finalResult.cloneWithType();
    }

    isolated resource function get mission(graphql:Field 'field, graphql:Context context, int id) returns Mission|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, MISSIONS);
        string fieldString = classifier.getFieldString();
        UnresolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("mission", fieldString, {"id": getParamAsString(id)});
        missionResponse|graphql:ClientError response = MISSIONS_CLIENT->execute(queryString);
        map<json> result = {"id": id};
        graphql:ErrorDetail[] errors = [];
        if response is graphql:ClientError {
            appendUnableToResolveErrorDetail(errors, 'field);
        } else {
            mergeToResultJson(result, <map<json>>response.data.mission.toJson());
            appendErrorDetailsFromResponse(errors, response?.errors);
        }
        Resolver resolver = new (queryPlan, result.toJson(), "Mission", propertiesNotResolved, ["mission"], errors);
        json finalResult = resolver.getResult();
        addErrorsToGraphqlContext(context, errors);
        return finalResult.cloneWithType();
    }

    isolated remote function addMission(graphql:Field 'field, graphql:Context context, MissionInput missionInput) returns Mission|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, MISSIONS);
        string fieldString = classifier.getFieldString();
        UnresolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithMutation("addMission", fieldString, {"missionInput": getParamAsString(missionInput)});
        addMissionResponse|graphql:ClientError response = MISSIONS_CLIENT->execute(queryString);
        if response is graphql:ClientError {
            return error("Unable to perform the operation");
        }
        Mission result = response.data.addMission;
        graphql:ErrorDetail[] errors = [];
        appendErrorDetailsFromResponse(errors, response?.errors);
        Resolver resolver = new (queryPlan, result.toJson(), "Mission", propertiesNotResolved, ["addMission"], errors);
        json finalResult = resolver.getResult();
        addErrorsToGraphqlContext(context, errors);
        return finalResult.cloneWithType();
    }

    isolated remote function setServiceName(graphql:Field 'field, graphql:Context context, string name) returns string|error {
        string queryString = wrapwithMutation("setServiceName", (), {"name": getParamAsString(name)});
        setServiceNameResponse|graphql:ClientError response = ASTRONAUTS_CLIENT->execute(queryString);
        if response is graphql:ClientError {
            return error("Unable to resolve : setServiceName");
        }
        return response.data.setServiceName;
    }
}
