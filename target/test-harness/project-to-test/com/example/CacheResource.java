package com.example;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.lib.*;

public class CacheResource implements Comparable<CacheResource>, java.io.Serializable {
    private static final long serialVersionUID = -4607284148900335639L;

    private final String key;
    private final byte[] data;
    private final String uniqueId;
    private final Map<String, String> properties;
    private final Map<String, List<String>> extendedProperties;
    private final java.time.Instant creationTime;

    private transient DynamoDbClient _client;
    private transient String _tableName;

    CacheResource(Builder b) {
        this.key = b.key;
        this.data = b.data;
        this.uniqueId = b.uniqueId;
        this.properties = b.properties;
        this.extendedProperties = b.extendedProperties;
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

    public Map<String, List<String>> extendedProperties() {
        return extendedProperties;
    }

    public java.time.Instant creationTime() {
        return creationTime;
    }

    void _setClient(DynamoDbClient client) {
        this._client = client;
    }

    void _setTableName(String tableName) {
        this._tableName = Objects.requireNonNull(tableName);
    }

    public Mutator mutator() {
        var map = new HashMap<String, AttributeValue>();
        CacheResource._cache_item_key__helper.contributeToMap(map, key);;
        return new Mutator(_client, map, _tableName);
    }

    public static class Mutator {

        private final List<FieldMutator> mutators = new ArrayList<>();
        private final DynamoDbClient client;
        private final String tableName;
        private final Map<String, AttributeValue> key;

        Mutator(DynamoDbClient client, Map<String, AttributeValue> key, String tableName) {
            this.client = client;
            this.key = Map.copyOf(key);
            this.tableName = Objects.requireNonNull(tableName);
        }

        public GenericMutator<String, Mutator> key() {
            var mutator = new GenericMutator<String, Mutator>("cache_item_key", this, _cache_item_key__helper);
            mutators.add(mutator);
            return mutator;
        }
        public GenericMutator<byte[], Mutator> data() {
            var mutator = new GenericMutator<byte[], Mutator>("cached_data", this, _cached_data__helper);
            mutators.add(mutator);
            return mutator;
        }
        public GenericMutator<String, Mutator> uniqueId() {
            var mutator = new GenericMutator<String, Mutator>("cached_item_unique_id", this,
                    _cached_item_unique_id__helper);
            mutators.add(mutator);
            return mutator;
        }
        public StringMapMutator<Mutator> properties() {
            var mutator = new StringMapMutator<Mutator>("cache_data_props", this, _cache_data_props__helper);
            mutators.add(mutator);
            return mutator;
        }
        public StringMultimapMutator<Mutator> extendedProperties() {
            var mutator = new StringMultimapMutator<Mutator>("cache_data_extended_props", this,
                    _cache_data_extended_props__helper);
            mutators.add(mutator);
            return mutator;
        }
        public GenericMutator<java.time.Instant, Mutator> creationTime() {
            var mutator = new GenericMutator<java.time.Instant, Mutator>("creation_time", this, _creation_time__helper);
            mutators.add(mutator);
            return mutator;
        }

        public CacheResource commit() {
            AtomicInteger counter = new AtomicInteger();
            List<String> setExpressions = new ArrayList<>();
            List<String> removeExpressions = new ArrayList<>();
            List<String> conditionExpressions = new ArrayList<>();
            Map<String, String> expressionAttributeNames = new HashMap<>();
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            for (var mutator : mutators) {
                mutator.appendUpdateExpression(counter, setExpressions, removeExpressions, conditionExpressions,
                        expressionAttributeNames, expressionAttributeValues);
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
            var updateItemRequestBuilder = UpdateItemRequest.builder().key(key).tableName(tableName)
                    .updateExpression(updateExpression.toString()).expressionAttributeNames(expressionAttributeNames)
                    .returnValues(ReturnValue.ALL_NEW);
            if (!expressionAttributeValues.isEmpty()) {
                updateItemRequestBuilder.expressionAttributeValues(expressionAttributeValues);
            }
            if (!conditionExpressions.isEmpty()) {
                updateItemRequestBuilder.conditionExpression(String.join(" AND ", conditionExpressions));
            }
            UpdateItemResponse response = client.updateItem(updateItemRequestBuilder.build());
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
            if (map.containsKey("cache_data_extended_props")) {
                builder.extendedProperties(CacheResource._cache_data_extended_props__helper.extractFromMap(map));
            }
            if (map.containsKey("creation_time")) {
                builder.creationTime(CacheResource._creation_time__helper.extractFromMap(map));
            }

            var obj = builder.build();
            obj._setClient(client);
            obj._setTableName(tableName);
            return obj;
        }

        public void delete() {
            DeleteItemRequest request = DeleteItemRequest.builder().tableName(tableName).key(key).build();
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
                && Objects.equals(extendedProperties, other.extendedProperties)
                && Objects.equals(creationTime, other.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, data, uniqueId, properties, extendedProperties, creationTime);
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
        sb.append(", extendedProperties='").append(extendedProperties).append('\'');
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
    static final lib.AttributeHelper<Map<String, List<String>>> _cache_data_extended_props__helper = new lib.StringMultimapHelper(
            "cache_data_extended_props");
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
        private Map<String, List<String>> extendedProperties;
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
        public Builder extendedProperties(Map<String, List<String>> extendedProperties) {
            this.extendedProperties = extendedProperties;
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