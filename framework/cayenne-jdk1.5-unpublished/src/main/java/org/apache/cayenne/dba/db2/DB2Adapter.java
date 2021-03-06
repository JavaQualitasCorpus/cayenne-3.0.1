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

package org.apache.cayenne.dba.db2;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;

import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.access.trans.QualifierTranslator;
import org.apache.cayenne.access.trans.QueryAssembler;
import org.apache.cayenne.access.types.BooleanType;
import org.apache.cayenne.access.types.ByteArrayType;
import org.apache.cayenne.access.types.CharType;
import org.apache.cayenne.access.types.ExtendedTypeMap;
import org.apache.cayenne.dba.JdbcAdapter;
import org.apache.cayenne.dba.PkGenerator;
import org.apache.cayenne.dba.QuotingStrategy;
import org.apache.cayenne.dba.TypesMapping;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.DbEntity;
import org.apache.cayenne.merge.MergerFactory;

/**
 * DbAdapter implementation for the <a href="http://www.ibm.com/db2/"> DB2 RDBMS </a>.
 * Sample connection settings to use with DB2 are shown below:
 * 
 * <pre>
 * 
 *       test-db2.cayenne.adapter = org.apache.cayenne.dba.db2.DB2Adapter
 *       test-db2.jdbc.username = test
 *       test-db2.jdbc.password = secret
 *       test-db2.jdbc.url = jdbc:db2://servername:50000/databasename
 *       test-db2.jdbc.driver = com.ibm.db2.jcc.DB2Driver
 * 
 * </pre>
 */
public class DB2Adapter extends JdbcAdapter {

    /**
     * Creates a DB2 specific PK Generator.
     */
    protected PkGenerator createPkGenerator() {
        return new DB2PkGenerator(this);
    }

    @Override
    protected void configureExtendedTypes(ExtendedTypeMap map) {
        super.configureExtendedTypes(map);

        // create specially configured CharType handler
        map.registerType(new CharType(true, true));

        // configure boolean type to work with numeric columns
        map.registerType(new DB2BooleanType());

        map.registerType(new ByteArrayType(false, false));
    }

    /**
     * Returns a SQL string that can be used to create database table corresponding to
     * <code>ent</code> parameter.
     */
    @Override
    public String createTable(DbEntity ent) {
        boolean status;
        if (ent.getDataMap() != null && ent.getDataMap().isQuotingSQLIdentifiers()) {
            status = true;
        }
        else {
            status = false;
        }
        QuotingStrategy context = getQuotingStrategy(status);

        StringBuilder buf = new StringBuilder();
        buf.append("CREATE TABLE ");
        buf.append(context.quoteFullyQualifiedName(ent));

        buf.append(" (");

        // columns
        Iterator<DbAttribute> it = ent.getAttributes().iterator();
        boolean first = true;
        while (it.hasNext()) {
            if (first)
                first = false;
            else
                buf.append(", ");

            DbAttribute at = it.next();

            // attribute may not be fully valid, do a simple check
            if (at.getType() == TypesMapping.NOT_DEFINED) {
                throw new CayenneRuntimeException("Undefined type for attribute '"
                        + ent.getFullyQualifiedName()
                        + "."
                        + at.getName()
                        + "'.");
            }

            String[] types = externalTypesForJdbcType(at.getType());
            if (types == null || types.length == 0) {
                throw new CayenneRuntimeException("Undefined type for attribute '"
                        + ent.getFullyQualifiedName()
                        + "."
                        + at.getName()
                        + "': "
                        + at.getType());
            }

            String type = types[0];
            buf.append(context.quoteString(at.getName())).append(' ').append(type);

            // append size and precision (if applicable)
            if (TypesMapping.supportsLength(at.getType())) {
                int len = at.getMaxLength();
                int scale = TypesMapping.isDecimal(at.getType()) ? at.getScale() : -1;

                // sanity check
                if (scale > len) {
                    scale = -1;
                }

                if (len > 0) {
                    buf.append('(').append(len);

                    if (scale >= 0) {
                        buf.append(", ").append(scale);
                    }

                    buf.append(')');
                }
            }

            if (at.isMandatory()) {
                buf.append(" NOT NULL");
            }
        }

        // primary key clause
        Iterator<DbAttribute> pkit = ent.getPrimaryKeys().iterator();
        if (pkit.hasNext()) {
            if (first)
                first = false;
            else
                buf.append(", ");

            buf.append("PRIMARY KEY (");
            boolean firstPk = true;
            while (pkit.hasNext()) {
                if (firstPk)
                    firstPk = false;
                else
                    buf.append(", ");

                DbAttribute at = pkit.next();
                buf.append(context.quoteString(at.getName()));
            }
            buf.append(')');
        }
        buf.append(')');
        return buf.toString();
    }

    /**
     * Returns a trimming translator.
     */
    @Override
    public QualifierTranslator getQualifierTranslator(QueryAssembler queryAssembler) {
        return new DB2QualifierTranslator(queryAssembler, "RTRIM");
    }

    final class DB2BooleanType extends BooleanType {

        @Override
        public void setJdbcObject(
                PreparedStatement st,
                Object val,
                int pos,
                int type,
                int precision) throws Exception {

            if (val != null) {
                st.setInt(pos, ((Boolean) val).booleanValue() ? 1 : 0);
            }
            else {
                st.setNull(pos, type);
            }
        }
    }

    @Override
    public MergerFactory mergerFactory() {
        return new DB2MergerFactory();
    }

    @Override
    public void bindParameter(
            PreparedStatement statement,
            Object object,
            int pos,
            int sqlType,
            int precision) throws SQLException, Exception {

        if (object == null && (sqlType == 0 || sqlType == Types.BOOLEAN)) {
            statement.setNull(pos, Types.VARCHAR);
        }
        else {
            super.bindParameter(statement, object, pos, sqlType, precision);
        }
    }

}
