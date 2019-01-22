package org.apache.cayenne.testdo.quotemap.auto;

import java.util.Date;

import org.apache.cayenne.CayenneDataObject;

/**
 * Class _Quote_Person was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Quote_Person extends CayenneDataObject {

    public static final String D_ATE_PROPERTY = "dAte";
    public static final String F_ULL_NAME_PROPERTY = "fULL_name";
    public static final String NAME_PROPERTY = "name";
    public static final String SALARY_PROPERTY = "salary";

    public static final String ID_PK_COLUMN = "id";

    public void setDAte(Date dAte) {
        writeProperty("dAte", dAte);
    }
    public Date getDAte() {
        return (Date)readProperty("dAte");
    }

    public void setFULL_name(String fULL_name) {
        writeProperty("fULL_name", fULL_name);
    }
    public String getFULL_name() {
        return (String)readProperty("fULL_name");
    }

    public void setName(String name) {
        writeProperty("name", name);
    }
    public String getName() {
        return (String)readProperty("name");
    }

    public void setSalary(Integer salary) {
        writeProperty("salary", salary);
    }
    public Integer getSalary() {
        return (Integer)readProperty("salary");
    }

}
