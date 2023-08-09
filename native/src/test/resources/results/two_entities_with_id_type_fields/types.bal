import ballerina/graphql;

public type Astronaut record {|
    Mission?[]? missions?;
    string? name?;
    string id?;
|};

public type Mission record {|
    string? endDate?;
    string id?;
    string designation?;
    string? startDate?;
    Astronaut?[]? crew?;
|};

public type astronautResponse record {
    graphql:ErrorDetail[] errors?;
    record {|Astronaut astronaut;|} data;
};

public type astronautsResponse record {
    graphql:ErrorDetail[] errors?;
    record {|Astronaut?[] astronauts;|} data;
};

public type missionResponse record {
    graphql:ErrorDetail[] errors?;
    record {|Mission mission;|} data;
};

public type missionsResponse record {
    graphql:ErrorDetail[] errors?;
    record {|Mission?[] missions;|} data;
};
