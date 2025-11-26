package net.gencsoy.odm.expandedmodel;

import net.gencsoy.odm.inputmodel.DynamoAttribute;
import net.gencsoy.odm.inputmodel.DynamoColumnType;

public class ExtendedDynamoAttribute extends DynamoAttribute {

    public String getJavaType() {
        String superType = super.getJavaType();
        if (superType == null) {
            return switch (getType()) {
                case STRING -> "String";
                case BINARY -> "byte[]";
                case NUMBER -> "Long";
            };
        } else {
            return superType;
        }
    }

    public String getBuilder() {
        return switch (getType()) {
            case STRING -> "s";
            case BINARY -> "xxxxxx";
            case NUMBER -> "n";
        };
    }

    public String getConverter() {
        return switch (getType()) {
            case STRING -> "__s";
            case BINARY -> "zzzzzz";
            case NUMBER -> "__l";
        };
    }
}
