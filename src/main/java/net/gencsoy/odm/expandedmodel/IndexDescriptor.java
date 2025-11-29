package net.gencsoy.odm.expandedmodel;

import net.gencsoy.odm.inputmodel.DynamoAttribute;

import java.util.ArrayList;
import java.util.List;

public class IndexDescriptor {
    private List<DynamoAttribute> keyAttributes = new ArrayList<>();
    private List<DynamoAttribute> equalityAttributes = new ArrayList<>();
    private String item;

    public List<DynamoAttribute> getEqualityAttributes() {
        return equalityAttributes;
    }

    public List<DynamoAttribute> getKeyAttributes() {
        return keyAttributes;
    }


    void setItem(String item) {
        this.item = item;
    }

    public String getItem() {
        return item;
    }
}
