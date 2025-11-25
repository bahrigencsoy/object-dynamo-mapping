package net.gencsoy.odm.inputmodel;

public class DynamoAttribute {
    private String name;
    private DynamoColumnType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DynamoColumnType getType() {
        return type;
    }

    public void setType(DynamoColumnType type) {
        this.type = type;
    }
}
