package net.gencsoy.odm.expandedmodel;

import net.gencsoy.odm.inputmodel.DynamoAttribute;
import net.gencsoy.odm.inputmodel.DynamoItem;
import net.gencsoy.odm.inputmodel.DynamoTable;

import java.util.ArrayList;
import java.util.List;

public class ExtendedDynamoItem extends DynamoItem {
    private DynamoTable table;

    public DynamoTable getTable() {
        return table;
    }

    public void setTable(DynamoTable table) {
        this.table = table;
    }

    public List<DynamoAttribute> getAllAttributes() {
        List<DynamoAttribute> list = new ArrayList<>();
        list.add(table.getPartitionKey());
        if (table.getSortKey() != null) {
            list.add(table.getSortKey());
        }
        list.addAll(getAttributes());
        return list;
    }
}
