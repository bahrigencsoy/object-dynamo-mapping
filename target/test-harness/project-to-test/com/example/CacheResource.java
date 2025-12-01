package com.example;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.lib.*;

public class CacheResource implements Comparable<CacheResource> {

    private final String key;
    private final byte[] data;
    private final String uniqueId;
    private final Map<String, String> properties;
    private final java.time.Instant creationTime;

    private transient DynamoDbClient _client;

    CacheResource(Builder b) {
        this.key = b.key;
        this.data = b.data;
        this.uniqueId = b.uniqueId;
        this.properties = b.properties;
        this.creationTime = b.creationTime;
    }

    public String key() {
        return key;
    }

    public byte[] data() {
        return data;
    }

    public String uniqueId() {
        return uniqueId;
    }

    public Map<String, String> properties() {
        return properties;
    }

    public java.time.Instant creationTime() {
        return creationTime;
    }

    void _setClient(DynamoDbClient client) {
        this._client = client;
    }

    public Mutator mutator() {
        var map = new HashMap<String, AttributeValue>();
        CacheResource._cache_item_key__helper.contributeToMap(map, key);;
        return new Mutator(_client, map);
    }

    public static class Mutator {

        private final List<FieldMutator> mutators = new ArrayList<>();
        private final DynamoDbClient client;
        private final Map<String, AttributeValue> key;

        Mutator(DynamoDbClient client, Map<String, AttributeValue> key) {
            this.client = client;
            this.key = Map.copyOf(key);
        }

        public GenericMutator<String, Mutator> key() {
            var mutator = new GenericMutator<>("cache_item_key", this, _cache_item_key__helper);
            mutators.add(mutator);
            return mutator;
        }
        public GenericMutator<byte[], Mutator> data() {
            var mutator = new GenericMutator<>("cached_data", this, _cached_data__helper);
            mutators.add(mutator);
            return mutator;
        }
        public GenericMutator<String, Mutator> uniqueId() {
            var mutator = new GenericMutator<>("cached_item_unique_id", this, _cached_item_unique_id__helper);
            mutators.add(mutator);
            return mutator;
        }
        public GenericMutator<Map<String, String>, Mutator> properties() {
            var mutator = new GenericMutator<>("cache_data_props", this, _cache_data_props__helper);
            mutators.add(mutator);
            return mutator;
        }
        public GenericMutator<java.time.Instant, Mutator> creationTime() {
            var mutator = new GenericMutator<>("creation_time", this, _creation_time__helper);
            mutators.add(mutator);
            return mutator;
        }

        public CacheResource commit() {
            AtomicInteger counter = new AtomicInteger();
            List<String> setExpressions = new ArrayList<>();
            List<String> removeExpressions = new ArrayList<>();
            Map<String, String> expressionAttributeNames = new HashMap<>();
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            for (var mutator : mutators) {
                mutator.appendUpdateExpression(counter, setExpressions, removeExpressions, expressionAttributeNames,
                        expressionAttributeValues);
            }
            StringBuilder updateExpression = new StringBuilder();
            if (!setExpressions.isEmpty()) {
                updateExpression.append("SET ");
                updateExpression.append(String.join(",", setExpressions));
                updateExpression.append(" ");
            }
            if (!removeExpressions.isEmpty()) {
                updateExpression.append("REMOVE ");
                updateExpression.append(String.join(",", removeExpressions));
                updateExpression.append(" ");
            }
            var updateItemRequest = UpdateItemRequest.builder().key(key).tableName("cached_resource_odm_test")
                    .updateExpression(updateExpression.toString()).expressionAttributeNames(expressionAttributeNames)
                    .expressionAttributeValues(expressionAttributeValues).returnValues(ReturnValue.ALL_NEW).build();
            UpdateItemResponse response = client.updateItem(updateItemRequest);
            Map<String, AttributeValue> map = response.attributes();
            CacheResource.Builder builder = CacheResource.builder();

            if (map.containsKey("cache_item_key")) {
                builder.key(CacheResource._cache_item_key__helper.extractFromMap(map));
            }
            if (map.containsKey("cached_data")) {
                builder.data(CacheResource._cached_data__helper.extractFromMap(map));
            }
            if (map.containsKey("cached_item_unique_id")) {
                builder.uniqueId(CacheResource._cached_item_unique_id__helper.extractFromMap(map));
            }
            if (map.containsKey("cache_data_props")) {
                builder.properties(CacheResource._cache_data_props__helper.extractFromMap(map));
            }
            if (map.containsKey("creation_time")) {
                builder.creationTime(CacheResource._creation_time__helper.extractFromMap(map));
            }

            var obj = builder.build();
            obj._setClient(client);
            return obj;
        }

        public void delete() {
            DeleteItemRequest request = DeleteItemRequest.builder().tableName("cached_resource_odm_test").key(key)
                    .build();
            var response = client.deleteItem(request);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        if (o == this)
            return true;
        CacheResource other = (CacheResource) o;
        return Objects.equals(key, other.key) && Objects.equals(data, other.data)
                && Objects.equals(uniqueId, other.uniqueId) && Objects.equals(properties, other.properties)
                && Objects.equals(creationTime, other.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, data, uniqueId, properties, creationTime);
    }

    @Override
    public int compareTo(CacheResource o) {
        int c;

        if (this.key == null && o.key == null)
            return 0;
        if (this.key == null)
            return 1;
        if (o.key == null)
            return -1;
        if ((c = this.key.compareTo(o.key)) != 0) {
            return c;
        }
        if (this.uniqueId == null && o.uniqueId == null)
            return 0;
        if (this.uniqueId == null)
            return 1;
        if (o.uniqueId == null)
            return -1;
        if ((c = this.uniqueId.compareTo(o.uniqueId)) != 0) {
            return c;
        }

        return 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CacheResource{");

        sb.append("key='").append(key).append('\'');
        sb.append(", data='").append(data).append('\'');
        sb.append(", uniqueId='").append(uniqueId).append('\'');
        sb.append(", properties='").append(properties).append('\'');
        sb.append(", creationTime='").append(creationTime).append('\'');

        sb.append('}');
        return sb.toString();
    }

    static final lib.AttributeHelper<String> _cache_item_key__helper = new lib.StringAttributeHelper("cache_item_key");
    static final lib.AttributeHelper<byte[]> _cached_data__helper = new lib.Base64ArrayAttributeHelper("cached_data");
    static final lib.AttributeHelper<String> _cached_item_unique_id__helper = new lib.StringAttributeHelper(
            "cached_item_unique_id");
    static final lib.AttributeHelper<Map<String, String>> _cache_data_props__helper = new lib.StringMapHelper(
            "cache_data_props");
    static final lib.AttributeHelper<java.time.Instant> _creation_time__helper = new lib.InstantAttributeHelper(
            "creation_time");

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String key;
        private byte[] data;
        private String uniqueId;
        private Map<String, String> properties;
        private java.time.Instant creationTime;

        private Builder() {
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }
        public Builder data(byte[] data) {
            this.data = data;
            return this;
        }
        public Builder uniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
            return this;
        }
        public Builder properties(Map<String, String> properties) {
            this.properties = properties;
            return this;
        }
        public Builder creationTime(java.time.Instant creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public CacheResource build() {
            return new CacheResource(this);
        }
    }

}