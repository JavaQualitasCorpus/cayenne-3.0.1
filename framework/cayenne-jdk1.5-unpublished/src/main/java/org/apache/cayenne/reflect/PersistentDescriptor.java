/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/
package org.apache.cayenne.reflect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.PersistenceState;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.DbEntity;
import org.apache.cayenne.map.EntityInheritanceTree;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.commons.collections.IteratorUtils;

/**
 * A default ClassDescriptor implementation for persistent objects.
 * 
 * @since 3.0
 */
public class PersistentDescriptor implements ClassDescriptor {

    static final Integer TRANSIENT_STATE = Integer.valueOf(PersistenceState.TRANSIENT);
    static final Integer HOLLOW_STATE = Integer.valueOf(PersistenceState.HOLLOW);
    static final Integer COMMITTED_STATE = Integer.valueOf(PersistenceState.COMMITTED);

    protected ClassDescriptor superclassDescriptor;

    // compiled properties ...
    protected Class<?> objectClass;
    protected Map<String, Property> declaredProperties;
    protected Map<String, Property> superProperties;
    protected Map<String, ClassDescriptor> subclassDescriptors;
    protected Accessor persistenceStateAccessor;

    protected ObjEntity entity;
    protected Collection<DbEntity> rootDbEntities;
    protected EntityInheritanceTree entityInheritanceTree;

    // combines declared and super properties
    protected Collection<Property> idProperties;

    // combines declared and super properties
    protected Collection<ArcProperty> mapArcProperties;

    // inheritance information
    protected Collection<ObjAttribute> allDiscriminatorColumns;
    protected Expression entityQualifier;

    /**
     * Creates a PersistentDescriptor.
     */
    public PersistentDescriptor() {
        this.declaredProperties = new HashMap<String, Property>();
        this.superProperties = new HashMap<String, Property>();
        this.subclassDescriptors = new HashMap<String, ClassDescriptor>();

        // must be a set as duplicate addition attempts are expected...
        this.rootDbEntities = new HashSet<DbEntity>(1);
    }

    public void setDiscriminatorColumns(Collection<ObjAttribute> columns) {
        if (columns == null || columns.isEmpty()) {
            allDiscriminatorColumns = null;
        }
        else {
            allDiscriminatorColumns = new ArrayList<ObjAttribute>(columns);
        }
    }

    /**
     * Registers a superclass property.
     */
    public void addSuperProperty(Property property) {
        superProperties.put(property.getName(), property);
        indexAddedProperty(property);
    }

    /**
     * Registers a property. This method is useful to customize default ClassDescriptor
     * generated from ObjEntity by adding new properties or overriding the standard ones.
     */
    public void addDeclaredProperty(Property property) {
        declaredProperties.put(property.getName(), property);
        indexAddedProperty(property);
    }

    /**
     * Adds a root DbEntity to the list of roots, filtering duplicates.
     */
    public void addRootDbEntity(DbEntity dbEntity) {
        this.rootDbEntities.add(dbEntity);
    }

    void indexAddedProperty(Property property) {
        if (property instanceof AttributeProperty) {
            ObjAttribute attribute = ((AttributeProperty) property).getAttribute();
            if (attribute.isPrimaryKey()) {

                if (idProperties == null) {
                    idProperties = new ArrayList<Property>(2);
                }

                idProperties.add(property);
            }
        }
        else if (property instanceof ArcProperty) {
            ObjRelationship relationship = ((ArcProperty) property).getRelationship();
            ObjRelationship reverseRelationship = relationship.getReverseRelationship();
            if (reverseRelationship != null
                    && "java.util.Map".equals(reverseRelationship.getCollectionType())) {

                if (mapArcProperties == null) {
                    mapArcProperties = new ArrayList<ArcProperty>(2);
                }

                mapArcProperties.add((ArcProperty) property);
            }
        }
    }

    /**
     * Removes declared property. This method can be used to customize default
     * ClassDescriptor generated from ObjEntity.
     */
    public void removeDeclaredProperty(String propertyName) {
        Object removed = declaredProperties.remove(propertyName);

        if (removed != null) {
            if (idProperties != null) {
                idProperties.remove(removed);
            }

            if (mapArcProperties != null) {
                mapArcProperties.remove(removed);
            }
        }
    }

    /**
     * Adds a subclass descriptor that maps to a given class name.
     */
    public void addSubclassDescriptor(String className, ClassDescriptor subclassDescriptor) {
        // note that 'className' should be used instead of
        // "subclassDescriptor.getEntity().getClassName()", as this method is called in
        // the early phases of descriptor initialization and we do not want to trigger
        // subclassDescriptor resolution just yet to prevent stack overflow.
        subclassDescriptors.put(className, subclassDescriptor);
    }

    public ObjEntity getEntity() {
        return entity;
    }

    public Collection<DbEntity> getRootDbEntities() {
        return rootDbEntities;
    }

    public boolean isFault(Object object) {
        if (superclassDescriptor != null) {
            return superclassDescriptor.isFault(object);
        }

        if (object == null) {
            return false;
        }

        return HOLLOW_STATE.equals(persistenceStateAccessor.getValue(object));
    }

    public Class<?> getObjectClass() {
        return objectClass;
    }

    void setObjectClass(Class<?> objectClass) {
        this.objectClass = objectClass;
    }

    public ClassDescriptor getSubclassDescriptor(Class<?> objectClass) {
        if (objectClass == null) {
            throw new IllegalArgumentException("Null objectClass");
        }

        if (subclassDescriptors.isEmpty()) {
            return this;
        }

        ClassDescriptor subclassDescriptor = subclassDescriptors.get(objectClass
                .getName());

        // ascend via the class hierarchy (only doing it if there are multiple choices)
        if (subclassDescriptor == null) {
            Class<?> currentClass = objectClass;
            while (subclassDescriptor == null
                    && (currentClass = currentClass.getSuperclass()) != null) {
                subclassDescriptor = subclassDescriptors.get(currentClass.getName());
            }
        }

        return subclassDescriptor != null ? subclassDescriptor : this;
    }

    /**
     * @deprecated since 3.0. Use {@link #visitProperties(PropertyVisitor)} method
     *             instead.
     */
    public Iterator<Property> getProperties() {
        Iterator<Property> declaredIt = IteratorUtils
                .unmodifiableIterator(declaredProperties.values().iterator());

        if (getSuperclassDescriptor() == null) {
            return declaredIt;
        }
        else {
            return IteratorUtils.chainedIterator(
                    superclassDescriptor.getProperties(),
                    declaredIt);
        }
    }

    public Iterator<ObjAttribute> getDiscriminatorColumns() {
        return allDiscriminatorColumns != null
                ? allDiscriminatorColumns.iterator()
                : IteratorUtils.EMPTY_ITERATOR;
    }

    public Iterator<Property> getIdProperties() {

        if (idProperties != null) {
            return idProperties.iterator();
        }

        return IteratorUtils.EMPTY_ITERATOR;
    }

    public Iterator<ArcProperty> getMapArcProperties() {

        if (mapArcProperties != null) {
            return mapArcProperties.iterator();
        }

        return IteratorUtils.EMPTY_ITERATOR;
    }

    /**
     * Recursively looks up property descriptor in this class descriptor and all
     * superclass descriptors.
     */
    public Property getProperty(String propertyName) {
        Property property = getDeclaredProperty(propertyName);

        if (property == null && superclassDescriptor != null) {
            property = superclassDescriptor.getProperty(propertyName);
        }

        return property;
    }

    public Property getDeclaredProperty(String propertyName) {
        return declaredProperties.get(propertyName);
    }

    /**
     * Returns a descriptor of the mapped superclass or null if the descriptor's entity
     * sits at the top of inheritance hierarchy.
     */
    public ClassDescriptor getSuperclassDescriptor() {
        return superclassDescriptor;
    }

    /**
     * Creates a new instance of a class described by this object.
     */
    public Object createObject() {
        if (objectClass == null) {
            throw new NullPointerException(
                    "Null objectClass. Descriptor wasn't initialized properly.");
        }

        try {
            return objectClass.newInstance();
        }
        catch (Throwable e) {
            throw new CayenneRuntimeException("Error creating object of class '"
                    + objectClass.getName()
                    + "'", e);
        }
    }

    /**
     * Invokes 'prepareForAccess' of a super descriptor and then invokes
     * 'prepareForAccess' of each declared property.
     */
    public void injectValueHolders(Object object) throws PropertyException {

        // do super first
        if (getSuperclassDescriptor() != null) {
            getSuperclassDescriptor().injectValueHolders(object);
        }

        for (Property property : declaredProperties.values()) {
            property.injectValueHolder(object);
        }
    }

    /**
     * Copies object properties from one object to another. Invokes 'shallowCopy' of a
     * super descriptor and then invokes 'shallowCopy' of each declared property.
     */
    public void shallowMerge(final Object from, final Object to) throws PropertyException {

        visitProperties(new PropertyVisitor() {

            public boolean visitAttribute(AttributeProperty property) {
                property.writePropertyDirectly(
                        to,
                        property.readPropertyDirectly(to),
                        property.readPropertyDirectly(from));
                return true;
            }

            public boolean visitToOne(ToOneProperty property) {
                property.invalidate(to);
                return true;
            }

            public boolean visitToMany(ToManyProperty property) {
                return true;
            }
        });
    }

    /**
     * @since 3.0
     */
    boolean visitSuperProperties(PropertyVisitor visitor) {
        for (Property next : superProperties.values()) {
            if (!next.visit(visitor)) {
                return false;
            }
        }

        return true;
    }

    /**
     * @since 3.0
     */
    public boolean visitDeclaredProperties(PropertyVisitor visitor) {

        for (Property next : declaredProperties.values()) {
            if (!next.visit(visitor)) {
                return false;
            }
        }

        return true;
    }

    /**
     * @since 3.0
     */
    public boolean visitAllProperties(PropertyVisitor visitor) {
        if (!visitProperties(visitor)) {
            return false;
        }

        if (!subclassDescriptors.isEmpty()) {
            for (ClassDescriptor next : subclassDescriptors.values()) {
                if (!next.visitDeclaredProperties(visitor)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean visitProperties(PropertyVisitor visitor) {
        if (!visitSuperProperties(visitor)) {
            return false;
        }

        return visitDeclaredProperties(visitor);
    }

    public void setPersistenceStateAccessor(Accessor persistenceStateAccessor) {
        this.persistenceStateAccessor = persistenceStateAccessor;
    }

    public void setEntity(ObjEntity entity) {
        this.entity = entity;
    }

    public void setSuperclassDescriptor(ClassDescriptor superclassDescriptor) {
        this.superclassDescriptor = superclassDescriptor;
    }

    public Expression getEntityQualifier() {
        return entityQualifier;
    }

    public void setEntityQualifier(Expression entityQualifier) {
        this.entityQualifier = entityQualifier;
    }

    public EntityInheritanceTree getEntityInheritanceTree() {
        return entityInheritanceTree;
    }

    public void setEntityInheritanceTree(EntityInheritanceTree entityInheritanceTree) {
        this.entityInheritanceTree = entityInheritanceTree;
    }
}
