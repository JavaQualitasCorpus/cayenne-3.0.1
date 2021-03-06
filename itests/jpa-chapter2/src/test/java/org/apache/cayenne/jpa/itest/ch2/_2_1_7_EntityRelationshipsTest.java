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
package org.apache.cayenne.jpa.itest.ch2;

import org.apache.cayenne.itest.jpa.EntityManagerCase;
import org.apache.cayenne.jpa.itest.ch2.entity.CollectionFieldPersistenceEntity;

public class _2_1_7_EntityRelationshipsTest extends EntityManagerCase {

    public void testEmptyCollection() throws Exception {
        getDbHelper().deleteAll("HelperEntity1");
        getDbHelper().deleteAll("CollectionFieldPersistenceEntity");

        getDbHelper().insert("CollectionFieldPersistenceEntity", new String[] {
            "id"
        }, new Object[] {
            new Integer(5)
        });

        CollectionFieldPersistenceEntity o1 = getEntityManager().find(
                CollectionFieldPersistenceEntity.class,
                new Integer(5));
        assertNotNull(o1);
        assertNotNull(o1.getCollection());
        assertTrue(o1.getCollection().isEmpty());
    }

    public void testNonEmptyCollection() throws Exception {
        getDbHelper().deleteAll("HelperEntity1");
        getDbHelper().deleteAll("CollectionFieldPersistenceEntity");

        getDbHelper().insert("CollectionFieldPersistenceEntity", new String[] {
            "id"
        }, new Object[] {
            new Integer(5)
        });

        getDbHelper().insert("HelperEntity1", new String[] {
                "id", "entity_id"
        }, new Object[] {
                new Integer(4), new Integer(5)
        });

        CollectionFieldPersistenceEntity o1 = getEntityManager().find(
                CollectionFieldPersistenceEntity.class,
                new Integer(5));
        assertNotNull(o1);
        assertNotNull(o1.getCollection());
        assertFalse(o1.getCollection().isEmpty());
    }
}
