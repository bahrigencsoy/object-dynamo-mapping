package net.gencsoy.odm.expandedmodel;

import net.gencsoy.odm.inputmodel.DynamoAttribute;
import net.gencsoy.odm.inputmodel.DynamoTable;

import java.util.ArrayList;
import java.util.List;

public class ExtendedDynamoTable extends DynamoTable {

    public List<DynamoAttribute> getKeyAttributes() {
        List<DynamoAttribute> list = new ArrayList<>();
        /*
        list.add(getPartitionKey());
        if (getSortKey() != null) {
            list.add(getSortKey());
        }
         */
        return list;
    }

    public boolean hasSortKey() {
        return false;
        /*
        return getSortKey() != null;

         */
    }

    public List<DynamoAttribute> getSortAttributes() {
        List<DynamoAttribute> list = getKeyAttributes();
        /*
        list.addAll(getGlobalSecondaryIndexes().values());
        list.addAll(getLocalSecondaryIndexes().values());
         */
        return list;
    }

    public String getPutConditionExpression() {
        StringBuilder sb = new StringBuilder();
        sb.append("attribute_not_exists(" + getPrimaryKey().getPartitionKey() + ")");
        if (getPrimaryKey().getSortKey()!= null) {
            sb.append(" AND attribute_not_exists(" + getPrimaryKey().getSortKey() + ")");
        }
        return sb.toString();
    }

    public List<IndexDescriptor> getIndexDescriptors() {
        // FIXME
        List<IndexDescriptor> list = new ArrayList<>();
        /*
        for (DynamoItem item : getItems()) {
            IndexDescriptor primaryDescriptor = new IndexDescriptor();
            primaryDescriptor.getKeyAttributes().add(getPartitionKey());
            if (hasSortKey()) {
                primaryDescriptor.getKeyAttributes().add(getSortKey());
            }
            primaryDescriptor.getEqualityAttributes().add(getPartitionKey());
            primaryDescriptor.setItem(item);
            list.add(primaryDescriptor);
        }

         */
        return list;
    }

    public List<DynamoAttribute> getAllAttributes() {
        // FIXME
        List<DynamoAttribute> list = new ArrayList<>();
        /*
        list.add(getPartitionKey());
        if (getSortKey() != null) {
            list.add(getSortKey());
        }
        list.addAll(getGlobalSecondaryIndexes().values());
        list.addAll(getLocalSecondaryIndexes().values());
        list.addAll(getAttributes());

         */
        return list;
    }

    public List<DynamoAttribute> getDerivedAttributes() {
        // FIXME
        List<DynamoAttribute> list = new ArrayList<>();
        /*
        list.addAll(getGlobalSecondaryIndexes().values());
        list.addAll(getLocalSecondaryIndexes().values());
        list.addAll(super.getAttributes());

         */
        return list;
    }

    public DynamoAttribute getPartitionKey() {
        return getAttributes().stream()
                .filter(a -> a.getName().equals(getPrimaryKey().getPartitionKey()))
                .findFirst().orElseThrow();
    }
}
