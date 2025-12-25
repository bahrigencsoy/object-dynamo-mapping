package net.gencsoy.odm.expandedmodel;

import net.gencsoy.odm.inputmodel.DynamoAttribute;
import net.gencsoy.odm.inputmodel.DynamoIndex;
import net.gencsoy.odm.inputmodel.DynamoTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        if (!list.contains(att)) {
            list.add(att);
        }
    }

    public List<DynamoAttribute> getSortAttributes() {
        List<DynamoAttribute> list = new ArrayList<>();
        insertToList(getPrimaryIndex().getPartitionKey(), list);
        insertToList(getPrimaryIndex().getSortKey(), list);
        for (DynamoIndex index : getIndexes()) {
            insertToList(index.getPartitionKey(), list);
            insertToList(index.getSortKey(), list);
        }
        return list;
    }

    public String getPutConditionExpression() {
        StringBuilder sb = new StringBuilder();
        sb.append("attribute_not_exists(" + getPrimaryIndex().getPartitionKey() + ")");
        if (getPrimaryIndex().getSortKey() != null) {
            sb.append(" AND attribute_not_exists(" + getPrimaryIndex().getSortKey() + ")");
        }
        return sb.toString();
    }

    public List<IndexDescriptor> getIndexDescriptors() {
        List<IndexDescriptor> list = new ArrayList<>();
        IndexDescriptor primary = new IndexDescriptor(getAttributes());
        primary.setKeyAttribute(getPartitionKey());
        primary.setKeyAttributes(getKeyAttributes());
        primary.setQueryAttribute(getPartitionKey());
        list.add(primary);
        for (DynamoIndex index : getIndexes()) {
            if (index.getPartitionKey() == null && index.getSortKey() == null) {
                throw new IllegalStateException("Index '" + index.getName() + "' is invalid");
            }
            IndexDescriptor desc = new IndexDescriptor(getAttributes());
            if (index.getPartitionKey() != null) {
                // Global secondary index - GSI
                desc.setKeyAttribute(findAttributeByName(index.getPartitionKey()));
                List<DynamoAttribute> keyAttributes = new ArrayList<>();
                keyAttributes.add(findAttributeByName(index.getPartitionKey()));
                if (index.getSortKey() != null) {
                    keyAttributes.add(findAttributeByName(index.getSortKey()));
                }
                desc.setKeyAttributes(keyAttributes);
                desc.setQueryAttribute(findAttributeByName(index.getPartitionKey()));
                desc.setIndexName(index.getName());
                list.add(desc);
            } else {
                // Local secondary index
                desc.setKeyAttribute(getPartitionKey());
                List<DynamoAttribute> keyAttributes = new ArrayList<>();
                keyAttributes.add(getPartitionKey());
                keyAttributes.add(findAttributeByName(index.getSortKey()));
                desc.setKeyAttributes(keyAttributes);
                desc.setQueryAttribute(findAttributeByName(index.getSortKey()));
                desc.setIndexName(index.getName());
                list.add(desc);
            }
        }
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

    private boolean attributeExists(String name) {
        return getAttributes().stream()
                .anyMatch(at -> Objects.equals(at.getName(), name));
    }

    private DynamoAttribute findAttributeByName(String name) {
        return getAttributes().stream()
                .filter(at -> Objects.equals(at.getName(), name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no attribute named '" + name + "'"));
    }

    public DynamoAttribute getPartitionKey() {
        return findAttributeByName(getPrimaryIndex().getPartitionKey());
    }

    public DynamoAttribute getSortKey() {
        if (getPrimaryIndex().getSortKey() == null) {
            return null;
        } else {
            return findAttributeByName(getPrimaryIndex().getSortKey());
        }
    }

    public String getTableVar() {
        return "table_" + getJavaClass();
    }

    public long getSerialUID() {
        long id[] = new long[]{0xC0FFEEAA0103B9CDL};
        getAttributes().forEach(att -> {
            id[0] <<= 1;
            id[0] ^= att.getName().hashCode();
            id[0] ^= ((long) att.getJavaType().hashCode()) << 16;
        });
        return id[0];
    }
}
