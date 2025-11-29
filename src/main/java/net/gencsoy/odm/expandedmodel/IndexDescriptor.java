package net.gencsoy.odm.expandedmodel;

import net.gencsoy.odm.inputmodel.DynamoAttribute;

import java.util.ArrayList;
import java.util.List;

public class IndexDescriptor {
    private final ArrayList<DynamoAttribute> allAttributes;
    private List<DynamoAttribute> keyAttributes = new ArrayList<>();
    private DynamoAttribute queryAttribute;
    private DynamoAttribute keyAttribute;
    private String indexName;

    IndexDescriptor(List<DynamoAttribute> allAttributes) {
        this.allAttributes = new ArrayList<>(allAttributes);
    }

    public List<DynamoAttribute> getKeyAttributes() {
        return keyAttributes;
    }

    public void setKeyAttributes(List<DynamoAttribute> keyAttributes) {
        this.keyAttributes = keyAttributes;
    }

    public List<DynamoAttribute> getQueryAttributes() {
        List<DynamoAttribute> list = new ArrayList<>(allAttributes);
        list.removeAll(getKeyAttributes());
        return List.copyOf(list);
    }

    public DynamoAttribute getQueryAttribute() {
        return queryAttribute;
    }

    public void setQueryAttribute(DynamoAttribute queryAttribute) {
        this.queryAttribute = queryAttribute;
    }

    public DynamoAttribute getKeyAttribute() {
        return keyAttribute;
    }

    public void setKeyAttribute(DynamoAttribute keyAttribute) {
        this.keyAttribute = keyAttribute;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
}
