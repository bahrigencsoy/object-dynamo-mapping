package net.gencsoy.odm.inputmodel;

import java.util.ArrayList;
import java.util.List;

public class DynamoTable {
    private String name;
    private String javaClass;
    private DynamoIndex primaryIndex;
    private List<DynamoAttribute> attributes = new ArrayList<>();
    private List<DynamoIndex> indexes = new ArrayList<>();

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

    public String getName() {
        return name;
    }

    public void setName(String tableName) {
        this.name = tableName;
    }

    public DynamoIndex getPrimaryIndex() {
        return primaryIndex;
    }

    public void setPrimaryIndex(DynamoIndex primaryKey) {
        this.primaryIndex = primaryKey;
    }
}
