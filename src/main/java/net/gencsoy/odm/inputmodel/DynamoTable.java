package net.gencsoy.odm.inputmodel;

import java.util.List;

public class DynamoTable {
    private String tableName;
    private DynamoAttribute partitionKey;
    private DynamoAttribute sortKey;
    private List<DynamoItem> items;

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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<DynamoItem> getItems() {
        return items;
    }

    public void setItems(List<DynamoItem> items) {
        this.items = items;
    }
}
