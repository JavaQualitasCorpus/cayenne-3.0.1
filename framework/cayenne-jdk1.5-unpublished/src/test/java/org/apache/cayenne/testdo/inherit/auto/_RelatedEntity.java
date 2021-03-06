package org.apache.cayenne.testdo.inherit.auto;

import java.util.List;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.testdo.inherit.BaseEntity;
import org.apache.cayenne.testdo.inherit.SubEntity;

/**
 * Class _RelatedEntity was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _RelatedEntity extends CayenneDataObject {

    public static final String BASE_ENTITIES_PROPERTY = "baseEntities";
    public static final String SUB_ENTITIES_PROPERTY = "subEntities";

    public static final String RELATED_ENTITY_ID_PK_COLUMN = "RELATED_ENTITY_ID";

    public void addToBaseEntities(BaseEntity obj) {
        addToManyTarget("baseEntities", obj, true);
    }
    public void removeFromBaseEntities(BaseEntity obj) {
        removeToManyTarget("baseEntities", obj, true);
    }
    @SuppressWarnings("unchecked")
    public List<BaseEntity> getBaseEntities() {
        return (List<BaseEntity>)readProperty("baseEntities");
    }


    public void addToSubEntities(SubEntity obj) {
        addToManyTarget("subEntities", obj, true);
    }
    public void removeFromSubEntities(SubEntity obj) {
        removeToManyTarget("subEntities", obj, true);
    }
    @SuppressWarnings("unchecked")
    public List<SubEntity> getSubEntities() {
        return (List<SubEntity>)readProperty("subEntities");
    }


}
