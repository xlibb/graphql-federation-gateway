package io.xlibb.gateway.graphql.components;

import graphql.language.FieldDefinition;
import graphql.schema.GraphQLAppliedDirective;
import io.xlibb.gateway.exception.GatewayGenerationException;

import java.util.List;

import static io.xlibb.gateway.generator.common.CommonUtils.getClientFromFieldDefinition;
import static io.xlibb.gateway.generator.common.CommonUtils.getTypeFromFieldDefinition;

/**
 * Class to hold data related to a graphql type field.
 */
public class FieldData {
    private final String fieldName;
    private final String type;
    private final String client;


    FieldData(String fieldName, FieldDefinition fieldDefinition,
              List<GraphQLAppliedDirective> joinTypeDirectivesOnParent)
            throws GatewayGenerationException {
        this.fieldName = fieldName;
        this.type = getTypeFromFieldDefinition(fieldDefinition);
        this.client = getClientFromFieldDefinition(fieldDefinition, joinTypeDirectivesOnParent);
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getType() {
        return type;
    }

    public String getClient() {
        return client;
    }

    public boolean isID() {
        return this.type.equals("ID");
    }

}
