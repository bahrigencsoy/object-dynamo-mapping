package net.gencsoy.odm.inputmodel;

import java.util.List;

public class DynamoItem {
    private List<DynamoAttribute> attributes;

    public List<DynamoAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<DynamoAttribute> attributes) {
        this.attributes = attributes;
    }
}
