package net.gencsoy.odm.expandedmodel;

import net.gencsoy.odm.inputmodel.DynamoAttribute;

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

    public String getHelperClass() {
        return switch (getType()) {
            case STRING -> "StringAttributeHelper";
            case BINARY -> "zzzzzz";
            case NUMBER -> "LongAttributeHelper";
        };
    }

    public String getHelper() {
        return "_" + getName() + "__helper";
    }
}
