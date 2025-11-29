package net.gencsoy.odm.expandedmodel;

import net.gencsoy.odm.inputmodel.DynamoAttribute;
import net.gencsoy.odm.inputmodel.DynamoItem;

import java.util.ArrayList;
import java.util.List;

public class IndexDescriptor {
    private List<DynamoAttribute> keyAttributes = new ArrayList<>();
    private List<DynamoAttribute> equalityAttributes = new ArrayList<>();
    private DynamoItem item;

    public List<DynamoAttribute> getEqualityAttributes() {
        return equalityAttributes;
    }

    public List<DynamoAttribute> getKeyAttributes() {
        return keyAttributes;
    }


    void setItem(DynamoItem item) {
        this.item = item;
    }

    public DynamoItem getItem() {
        return item;
    }
}
