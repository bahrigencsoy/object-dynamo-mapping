package net.gencsoy.odm.inputmodel;

import java.util.List;

public class DynamoTable {
    private String tableName;
    private String partitionKey;
    private DynamoColumnType partitionKeyType;
    private String sortKey;
    private DynamoColumnType sortKeyType;
    private List<DynamoItem> items;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public DynamoColumnType getPartitionKeyType() {
        return partitionKeyType;
    }

    public void setPartitionKeyType(DynamoColumnType partitionKeyType) {
        this.partitionKeyType = partitionKeyType;
    }

    public String getSortKey() {
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    public DynamoColumnType getSortKeyType() {
        return sortKeyType;
    }

    public void setSortKeyType(DynamoColumnType sortKeyType) {
        this.sortKeyType = sortKeyType;
    }

    public List<DynamoItem> getItems() {
        return items;
    }

    public void setItems(List<DynamoItem> items) {
        this.items = items;
    }
}
