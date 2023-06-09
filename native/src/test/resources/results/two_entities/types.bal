import ballerina/graphql;

public type Astronaut record {|
    Mission[] missions?;
    string name?;
    int id?;
|};

public type Mission record {|
    string? endDate?;
    int id?;
    string designation?;
    string? startDate?;
    Astronaut[] crew?;
|};

public type MissionInput record {|
    int[] crewIds;
    string? endDate;
    string designation;
    string? startDate;
|};

public type astronautsResponse record {
    graphql:ErrorDetail[] errors?;
    record {|Astronaut[] astronauts;|} data;
};

public type astronautResponse record {
    graphql:ErrorDetail[] errors?;
    record {|Astronaut astronaut;|} data;
};

public type serviceNameResponse record {
    graphql:ErrorDetail[] errors?;
    record {|string serviceName;|} data;
};

public type isExistResponse record {
    graphql:ErrorDetail[] errors?;
    record {|boolean isExist;|} data;
};

public type missionsResponse record {
    graphql:ErrorDetail[] errors?;
    record {|Mission[] missions;|} data;
};

public type missionResponse record {
    graphql:ErrorDetail[] errors?;
    record {|Mission mission;|} data;
};

public type addMissionResponse record {
    graphql:ErrorDetail[] errors?;
    record {|Mission addMission;|} data;
};

public type setServiceNameResponse record {
    graphql:ErrorDetail[] errors?;
    record {|string setServiceName;|} data;
};
