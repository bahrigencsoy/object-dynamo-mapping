package net.gencsoy.odm.inputmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamoTable {
    private String name;
    private DynamoAttribute partitionKey;
    private DynamoAttribute sortKey;
    private List<DynamoItem> items = new ArrayList<>();
    private Map<String, DynamoAttribute> localSecondaryIndexes = new HashMap<>();
    private Map<String, DynamoAttribute> globalSecondaryIndexes = new HashMap<>();

    public Map<String, DynamoAttribute> getLocalSecondaryIndexes() {
        return localSecondaryIndexes;
    }

    public void setLocalSecondaryIndexes(Map<String, DynamoAttribute> localSecondaryIndexes) {
        this.localSecondaryIndexes = localSecondaryIndexes;
    }

    public Map<String, DynamoAttribute> getGlobalSecondaryIndexes() {
        return globalSecondaryIndexes;
    }

    public void setGlobalSecondaryIndexes(Map<String, DynamoAttribute> globalSecondaryIndexes) {
        this.globalSecondaryIndexes = globalSecondaryIndexes;
    }

    public DynamoAttribute getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(DynamoAttribute partitionKey) {
        this.partitionKey = partitionKey;
    }

    public DynamoAttribute getSortKey() {
        return sortKey;
    }

    public void setSortKey(DynamoAttribute sortKey) {
        this.sortKey = sortKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String tableName) {
        this.name = tableName;
    }

    public List<DynamoItem> getItems() {
        return items;
    }

    public void setItems(List<DynamoItem> items) {
        this.items = items;
    }
}
