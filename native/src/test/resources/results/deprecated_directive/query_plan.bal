public const string PRODUCTS = "products";
public final readonly & table<QueryPlanEntry> key(typename) queryPlan = table [{typename: "Product", keys: {"products": "id"}, fields: table [
            {name: "price", 'type: "Float", 'client: PRODUCTS},
            {name: "name", 'type: "String", 'client: PRODUCTS},
            {name: "description", 'type: "String", 'client: PRODUCTS}
        ]}];
