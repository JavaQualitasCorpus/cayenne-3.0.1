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

package org.apache.cayenne.access.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.cayenne.CayenneException;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.access.OperationObserver;
import org.apache.cayenne.access.QueryLogger;
import org.apache.cayenne.dba.DbAdapter;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.Query;
import org.apache.cayenne.query.QueryMetadata;
import org.apache.cayenne.query.SQLAction;

/**
 * A convenience superclass for SQLAction implementations.
 * 
 * @since 1.2
 */
public abstract class BaseSQLAction implements SQLAction {

    protected DbAdapter adapter;
    protected EntityResolver entityResolver;

    public BaseSQLAction(DbAdapter adapter, EntityResolver entityResolver) {
        this.adapter = adapter;
        this.entityResolver = entityResolver;
    }

    public DbAdapter getAdapter() {
        return adapter;
    }

    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    /**
     * Helper method to process a ResultSet.
     */
    protected void readResultSet(
            ResultSet resultSet,
            RowDescriptor descriptor,
            Query query,
            OperationObserver delegate) throws SQLException, Exception {

        long t1 = System.currentTimeMillis();

        QueryMetadata metadata = query.getMetaData(getEntityResolver());

        JDBCResultIterator resultReader = new JDBCResultIterator(
                null,
                null,
                resultSet,
                descriptor,
                metadata);

        LimitResultIterator it = new LimitResultIterator(
                resultReader,
                getInMemoryOffset(metadata.getFetchOffset()),
                metadata.getFetchLimit());

        if (!delegate.isIteratedResult()) {
            List<DataRow> resultRows = (List<DataRow>) it.allRows();
            QueryLogger
                    .logSelectCount(resultRows.size(), System.currentTimeMillis() - t1);

            delegate.nextRows(query, resultRows);
        }
        else {
            try {
                resultReader.setClosingConnection(true);
                delegate.nextRows(query, it);
            }
            catch (Exception ex) {

                try {
                    it.close();
                }
                catch (CayenneException cex) {
                    // ignore...
                }

                throw ex;
            }
        }
    }

    /**
     * Returns a value of the offset that will be used to rewind the ResultSet within the
     * SQL action before reading the result rows. The default implementation returns
     * 'queryOffset' argument. If the adapter supports setting offset at the SQL level,
     * this method must be overridden to return zero to suppress manual offset.
     * 
     * @since 3.0
     */
    protected int getInMemoryOffset(int queryOffset) {
        return queryOffset;
    }
}
