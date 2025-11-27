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
            case STRING:
                yield "StringAttributeHelper";
            case BINARY:
                yield "zzzzzz";
            case NUMBER :
                String javaType = getJavaType();
                if ("Long".equals(javaType)){
                    yield "LongAttributeHelper";
                } else if ("Integer".equals(javaType)){
                    yield "IntegerAttributeHelper";
                } else {
                    throw new IllegalArgumentException(javaType+" is not suitable for NUMBER");
                }
        };
    }

    public String getHelper() {
        return "_" + getName() + "__helper";
    }
}
