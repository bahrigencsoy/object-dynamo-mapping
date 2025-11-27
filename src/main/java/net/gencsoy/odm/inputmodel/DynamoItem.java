package net.gencsoy.odm.inputmodel;

import java.util.ArrayList;
import java.util.List;

public class DynamoItem {
    private String name;
    private List<DynamoAttribute> attributes = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public final List<DynamoAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<DynamoAttribute> attributes) {
        this.attributes = attributes;
    }
}
