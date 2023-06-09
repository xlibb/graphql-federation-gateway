import ballerina/graphql;

public type Category record {|
    string id?;
    string title?;
|};

public type Product record {|
    Review[] reviews?;
    int price?;
    string description?;
    string id?;
    string title?;
    Category category?;
|};

public type Review record {|
    string author?;
    float rating?;
    string comment?;
    string id?;
|};

public type productResponse record {
    graphql:ErrorDetail[] errors?;
    record {|Product product;|} data;
};

public type productsResponse record {
    graphql:ErrorDetail[] errors?;
    record {|Product[] products;|} data;
};

public type reviewsResponse record {
    graphql:ErrorDetail[] errors?;
    record {|Review[] reviews;|} data;
};
