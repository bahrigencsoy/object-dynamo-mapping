package net.gencsoy.odm.inputmodel;

import java.util.ArrayList;
import java.util.List;

public class DynamoTable {
    private String name;
    private DynamoAttribute partitionKey;
    private DynamoAttribute sortKey;
    private List<DynamoItem> items = new ArrayList<>();

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
