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

    isolated resource function get astronauts(graphql:Field 'field) returns Astronaut[]|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, ASTRONAUTS);
        string fieldString = classifier.getFieldString();
        UnresolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("astronauts", fieldString);
        astronautsResponse response = check ASTRONAUTS_CLIENT->execute(queryString);
        Astronaut[] result = response.data.astronauts;
        Resolver resolver = new (queryPlan, result.toJson(), "Astronaut", propertiesNotResolved, ["astronauts"]);
        json|error finalResult = resolver.getResult();
        if finalResult is error {
            return finalResult;
        } else {
            return finalResult.cloneWithType();
        }
    }
    isolated resource function get astronaut(graphql:Field 'field, int id) returns Astronaut?|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, ASTRONAUTS);
        string fieldString = classifier.getFieldString();
        UnresolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("astronaut", fieldString, {"id": getParamAsString(id)});
        astronautResponse response = check ASTRONAUTS_CLIENT->execute(queryString);
        Astronaut? result = response.data.astronaut;
        Resolver resolver = new (queryPlan, result.toJson(), "Astronaut", propertiesNotResolved, ["astronaut"]);
        json|error finalResult = resolver.getResult();
        if finalResult is error {
            return finalResult;
        } else {
            return finalResult.cloneWithType();
        }
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
    isolated resource function get missions(graphql:Field 'field) returns Mission[]|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, MISSIONS);
        string fieldString = classifier.getFieldString();
        UnresolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("missions", fieldString);
        missionsResponse response = check MISSIONS_CLIENT->execute(queryString);
        Mission[] result = response.data.missions;
        Resolver resolver = new (queryPlan, result.toJson(), "Mission", propertiesNotResolved, ["missions"]);
        json|error finalResult = resolver.getResult();
        if finalResult is error {
            return finalResult;
        } else {
            return finalResult.cloneWithType();
        }
    }
    isolated resource function get mission(graphql:Field 'field, int id) returns Mission|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, MISSIONS);
        string fieldString = classifier.getFieldString();
        UnresolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("mission", fieldString, {"id": getParamAsString(id)});
        missionResponse response = check MISSIONS_CLIENT->execute(queryString);
        Mission result = response.data.mission;
        Resolver resolver = new (queryPlan, result.toJson(), "Mission", propertiesNotResolved, ["mission"]);
        json|error finalResult = resolver.getResult();
        if finalResult is error {
            return finalResult;
        } else {
            return finalResult.cloneWithType();
        }
    }
    isolated remote function addMission(graphql:Field 'field, MissionInput missionInput) returns Mission|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, MISSIONS);
        string fieldString = classifier.getFieldString();
        UnresolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithMutation("addMission", fieldString, {"missionInput": getParamAsString(missionInput)});
        addMissionResponse response = check MISSIONS_CLIENT->execute(queryString);
        Mission result = response.data.addMission;
        Resolver resolver = new (queryPlan, result.toJson(), "Mission", propertiesNotResolved, ["addMission"]);
        json|error finalResult = resolver.getResult();
        if finalResult is error {
            return finalResult;
        } else {
            return finalResult.cloneWithType();
        }
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