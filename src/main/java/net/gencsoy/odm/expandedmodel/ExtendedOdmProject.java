package net.gencsoy.odm.expandedmodel;

import net.gencsoy.odm.inputmodel.DynamoTable;
import net.gencsoy.odm.inputmodel.OdmProject;

import java.util.ArrayList;
import java.util.List;

public class ExtendedOdmProject extends OdmProject {

    public List<String> getAllItems() {
        return List.of("FIXME");
        /*
        List<ExtendedDynamoItem> items = new ArrayList<>();
        for (DynamoTable table : getTables()) {
            for (DynamoItem item : table.getItems()) {
                items.add((ExtendedDynamoItem)item);
            }
        }
        return items;

         */
    }
}
