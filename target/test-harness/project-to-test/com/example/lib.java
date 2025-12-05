package com.example;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

class lib {

    interface AttributeHelper<T> {
        String attributeName();

        default void contributeToMap(Map<String, AttributeValue> map, T value) {
            map.put(attributeName(), build(AttributeValue.builder(), value).build());
        }

        default T extractFromMap(Map<String, AttributeValue> map) {
            return extract(map.get(attributeName()));
        }

        AttributeValue.Builder build(AttributeValue.Builder builder, T value);

        T extract(AttributeValue value);
    }

    static abstract class BaseAttributeHelper<T> implements AttributeHelper<T> {
        private final String attributeName;

        BaseAttributeHelper(String attributeName) {
            this.attributeName = attributeName;
        }

        @Override
        public final String attributeName() {
            return attributeName;
        }
    }

    static class StringAttributeHelper extends BaseAttributeHelper<String> {

        public StringAttributeHelper(String attributeName) {
            super(attributeName);
        }

        @Override
        public AttributeValue.Builder build(AttributeValue.Builder builder, String value) {
            return builder.s(value);
        }

        @Override
        public String extract(AttributeValue value) {
            return value.s();
        }
    }

    static class LongAttributeHelper extends BaseAttributeHelper<Long> {

        public LongAttributeHelper(String attributeName) {
            super(attributeName);
        }

        @Override
        public AttributeValue.Builder build(AttributeValue.Builder builder, Long value) {
            return builder.n(Long.toString(value));
        }

        @Override
        public Long extract(AttributeValue value) {
            return Long.parseLong(value.n());
        }
    }

    static class IntegerAttributeHelper extends BaseAttributeHelper<Integer> {

        public IntegerAttributeHelper(String attributeName) {
            super(attributeName);
        }

        @Override
        public AttributeValue.Builder build(AttributeValue.Builder builder, Integer value) {
            return builder.n(Integer.toString(value));
        }

        @Override
        public Integer extract(AttributeValue value) {
            return Integer.parseInt(value.n());
        }
    }

    static class Base64ArrayAttributeHelper extends BaseAttributeHelper<byte[]> {

        public Base64ArrayAttributeHelper(String attributeName) {
            super(attributeName);
        }

        @Override
        public AttributeValue.Builder build(AttributeValue.Builder builder, byte[] value) {
            return builder.b(SdkBytes.fromByteArrayUnsafe(value));
        }

        @Override
        public byte[] extract(AttributeValue value) {
            return value.b().asByteArrayUnsafe();
        }
    }

    static class StringMapHelper extends BaseAttributeHelper<Map<String, String>> {

        StringMapHelper(String attributeName) {
            super(attributeName);
        }

        @Override
        public AttributeValue.Builder build(AttributeValue.Builder builder, Map<String, String> value) {
            Map<String, AttributeValue> dynamoMap = new TreeMap<>();
            for (Map.Entry<String, String> e : value.entrySet()) {
                dynamoMap.put(e.getKey(), AttributeValue.builder().s(e.getValue()).build());
            }
            return builder.m(dynamoMap);
        }

        @Override
        public Map<String, String> extract(AttributeValue value) {
            if (value.hasM()) {
                return value.m().entrySet().stream()
                        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> entry.getValue().s()));
            } else {
                return Map.of();
            }
        }
    }

    static class InstantAttributeHelper extends BaseAttributeHelper<java.time.Instant> {
        public InstantAttributeHelper(String attributeName) {
            super(attributeName);
        }

        @Override
        public AttributeValue.Builder build(AttributeValue.Builder builder, java.time.Instant value) {
            return builder.n(Long.toString(value.toEpochMilli()));
        }

        @Override
        public java.time.Instant extract(AttributeValue value) {
            return java.time.Instant.ofEpochMilli(Long.parseLong(value.n()));
        }
    }

    public static class GenericMutator<T, P> extends lib.FieldMutator {

        private final String attribute;
        private final P parent;
        private final AttributeHelper<T> helper;
        private boolean delete = false;
        private T value;
        private T increment;

        GenericMutator(String attribute, P parent, AttributeHelper<T> helper) {
            this.parent = parent;
            this.attribute = attribute;
            this.helper = helper;
        }

        public P setValue(T value) {
            this.value = value;
            return parent;
        }

        public P delete() {
            this.delete = true;
            return parent;
        }

        public P increment(T by) {
            this.increment = by;
            return parent;
        }

        @Override
        void appendUpdateExpression(AtomicInteger counter, List<String> setExpressions, List<String> removeExpressions,
                Map<String, String> expressionAttributeNames, Map<String, AttributeValue> expressionAttributeValues) {
            if (delete) {
                int deleteCounter = counter.getAndIncrement();
                removeExpressions.add("#delete" + deleteCounter);
                expressionAttributeNames.put("#delete" + deleteCounter, attribute);
            }
            if (value != null) {
                int updateCounter = counter.getAndIncrement();
                setExpressions.add(String.format("#sk%s = :sv%s", updateCounter, updateCounter));
                expressionAttributeNames.put("#sk" + updateCounter, attribute);
                AttributeValue.Builder builder = AttributeValue.builder();
                builder = helper.build(builder, value);
                expressionAttributeValues.put(":sv" + updateCounter, builder.build());
            }
            if (this.increment != null) {
                int incrementCounter = counter.getAndIncrement();
                setExpressions.add(
                        String.format("#ik%s = #ik%s + :iv%s", incrementCounter, incrementCounter, incrementCounter));
                expressionAttributeNames.put("#ik" + incrementCounter, attribute);
                AttributeValue.Builder builder = AttributeValue.builder();
                builder = helper.build(builder, increment);
                expressionAttributeValues.put(":iv" + incrementCounter, builder.build());
            }
        }
    }

    public static class GenericQuery<T, P> {

        private final String attribute;
        private final P parent;
        private final AttributeHelper<T> helper;
        private final Map<String, Condition> conditionMap;

        GenericQuery(String attribute, P parent, AttributeHelper<T> helper, Map<String, Condition> conditionMap) {
            this.parent = parent;
            this.attribute = attribute;
            this.helper = helper;
            this.conditionMap = conditionMap;
        }

        private P singleValueComparison(ComparisonOperator op, T value) {
            AttributeValue.Builder builder = AttributeValue.builder();
            helper.build(builder, value);
            conditionMap.put(attribute,
                    Condition.builder().comparisonOperator(op).attributeValueList(List.of(builder.build())).build());
            return parent;
        }

        public P eq(T value) {
            return singleValueComparison(ComparisonOperator.EQ, value);
        }

        public P ne(T value) {
            return singleValueComparison(ComparisonOperator.NE, value);
        }

        public P isNull() {
            conditionMap.put(attribute, Condition.builder().comparisonOperator(ComparisonOperator.NULL).build());
            return parent;
        }

        public P isNotNull() {
            conditionMap.put(attribute, Condition.builder().comparisonOperator(ComparisonOperator.NOT_NULL).build());
            return parent;
        }

        public P beginsWith(T value) {
            return singleValueComparison(ComparisonOperator.BEGINS_WITH, value);
        }
    }

    abstract static class FieldMutator {
        abstract void appendUpdateExpression(AtomicInteger counter, List<String> setExpressions,
                List<String> removeExpressions, Map<String, String> expressionAttributeNames,
                Map<String, AttributeValue> expressionAttributeValues);
    }
}