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
                case BOOLEAN -> "Boolean";
                case X_STRINGMAP -> "Map<String, String>";
                case X_INSTANT -> "java.time.Instant";
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
                yield "Base64ArrayAttributeHelper";
            case NUMBER:
                String javaType = getJavaType();
                if ("Long".equals(javaType)) {
                    yield "LongAttributeHelper";
                } else if ("Integer".equals(javaType)) {
                    yield "IntegerAttributeHelper";
                } else {
                    throw new IllegalArgumentException(javaType + " is not suitable for NUMBER");
                }
            case BOOLEAN:
                yield "BooleanAttributeHelper";
            case X_STRINGMAP:
                yield "StringMapHelper";
            case X_INSTANT:
                yield "InstantAttributeHelper";
        };
    }

    public String getHelper() {
        return "_" + getName() + "__helper";
    }

    public String getMutatorClass() {
        return switch (getType()){
            case X_STRINGMAP -> "StringMapMutator<Mutator>";
            default -> "GenericMutator<%s, Mutator>".formatted(getJavaType());
        };
    }
}
