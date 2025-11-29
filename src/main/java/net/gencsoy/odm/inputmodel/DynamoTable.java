package net.gencsoy.odm.inputmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamoTable {
    private String name;
    private String javaClass;
    private DynamoIndex primaryKey;
    private List<DynamoAttribute> attributes = new ArrayList<>();
    private List<DynamoIndex> indexes = new ArrayList<>();

    private DynamoAttribute partitionKey;
    private DynamoAttribute sortKey;
    private List<DynamoItem> items = new ArrayList<>();
    private Map<String, DynamoAttribute> localSecondaryIndexes = new HashMap<>();
    private Map<String, DynamoAttribute> globalSecondaryIndexes = new HashMap<>();

    public String getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(String javaClass) {
        this.javaClass = javaClass;
    }

    public List<DynamoAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<DynamoAttribute> attributes) {
        this.attributes = attributes;
    }

    public List<DynamoIndex> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<DynamoIndex> indexes) {
        this.indexes = indexes;
    }




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

    public DynamoIndex getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(DynamoIndex primaryKey) {
        this.primaryKey = primaryKey;
    }
}
