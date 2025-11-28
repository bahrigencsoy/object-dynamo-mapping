package net.gencsoy.odm.expandedmodel;

import net.gencsoy.odm.inputmodel.DynamoAttribute;
import net.gencsoy.odm.inputmodel.DynamoTable;

import java.util.ArrayList;
import java.util.List;

public class ExtendedDynamoTable extends DynamoTable {

    public List<DynamoAttribute> getKeyAttributes() {
        List<DynamoAttribute> list = new ArrayList<>();
        list.add(getPartitionKey());
        if (getSortKey() != null) {
            list.add(getSortKey());
        }
        return list;
    }

    public boolean hasSortKey() {
        return getSortKey() != null;
    }

    public List<DynamoAttribute> getSortAttributes() {
        List<DynamoAttribute> list = getKeyAttributes();
        list.addAll(getLocalSecondaryIndexes().values());
        return list;
    }
}
