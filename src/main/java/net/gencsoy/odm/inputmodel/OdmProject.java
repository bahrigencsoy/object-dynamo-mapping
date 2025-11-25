package net.gencsoy.odm.inputmodel;

import java.util.ArrayList;
import java.util.List;

public class OdmProject {
    private String version;
    private String packageName;
    private String factoryClass;
    private List<DynamoTable> tables = new ArrayList<>();

    public List<DynamoTable> getTables() {
        return tables;
    }

    public void setTables(List<DynamoTable> tables) {
        this.tables = tables;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getFactoryClass() {
        return factoryClass;
    }

    public void setFactoryClass(String factoryClass) {
        this.factoryClass = factoryClass;
    }
}
