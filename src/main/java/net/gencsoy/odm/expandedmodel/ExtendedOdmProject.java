package net.gencsoy.odm.expandedmodel;

import net.gencsoy.odm.inputmodel.DynamoTable;
import net.gencsoy.odm.inputmodel.OdmProject;

import java.util.List;

public class ExtendedOdmProject extends OdmProject {

    public List<DynamoTable> getUnnamedTables() {
        return getTables().stream().filter(t -> t.getName() == null).toList();
    }

    public List<DynamoTable> getNamedTables() {
        return getTables().stream().filter(t -> t.getName() != null).toList();
    }
}
