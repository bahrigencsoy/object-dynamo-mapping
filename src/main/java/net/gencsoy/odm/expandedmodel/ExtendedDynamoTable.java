package net.gencsoy.odm.expandedmodel;

import net.gencsoy.odm.inputmodel.DynamoAttribute;
import net.gencsoy.odm.inputmodel.DynamoIndex;
import net.gencsoy.odm.inputmodel.DynamoTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    private void insertToList(String attributeName, List<DynamoAttribute> list) {
        if (attributeName == null) {
            return;
        }
        DynamoAttribute att = findAttributeByName(attributeName);
        if (att == null) {
            throw new IllegalArgumentException("Attribute '" + attributeName + "' not found");
        }
        if (!list.contains(att)) {
            list.add(att);
        }
    }

    public List<DynamoAttribute> getSortAttributes() {
        List<DynamoAttribute> list = new ArrayList<>();
        insertToList(getPrimaryKey().getPartitionKey(), list);
        insertToList(getPrimaryKey().getSortKey(), list);
        for (DynamoIndex index : getIndexes()) {
            insertToList(index.getPartitionKey(), list);
            insertToList(index.getSortKey(), list);
        }
        return list;
    }

    public String getPutConditionExpression() {
        StringBuilder sb = new StringBuilder();
        sb.append("attribute_not_exists(" + getPrimaryKey().getPartitionKey() + ")");
        if (getPrimaryKey().getSortKey() != null) {
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

    private DynamoAttribute findAttributeByName(String name) {
        return getAttributes().stream()
                .filter(at -> Objects.equals(at.getName(), name))
                .findFirst()
                .orElse(null);
    }

    public DynamoAttribute getPartitionKey() {
        return Optional.ofNullable(findAttributeByName(getPrimaryKey().getPartitionKey()))
                .orElseThrow(() -> new IllegalStateException("No partition key"));
    }

    public DynamoAttribute getSortKey() {
        if (getPrimaryKey().getSortKey() == null) {
            return null;
        } else {
            return Optional.ofNullable(findAttributeByName(getPrimaryKey().getSortKey()))
                    .orElseThrow(() -> new IllegalStateException("could not found sort key: '"+getPrimaryKey().getSortKey()+"'"));
        }
    }
}
