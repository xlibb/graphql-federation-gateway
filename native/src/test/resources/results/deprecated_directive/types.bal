import ballerina/graphql;

public type Product record {|
    float price?;
    string name?;
    string description?;
    string id?;
|};

public type productsResponse record {
    graphql:ErrorDetail[] errors?;
    record {|Product[] products;|} data;
};

public type productResponse record {
    graphql:ErrorDetail[] errors?;
    record {|Product product;|} data;
};

public type personResponse record {
    graphql:ErrorDetail[] errors?;
    record {|string person;|} data;
};

public type addProductResponse record {
    graphql:ErrorDetail[] errors?;
    record {|Product addProduct;|} data;
};

public type addPersonResponse record {
    graphql:ErrorDetail[] errors?;
    record {|string addPerson;|} data;
};
