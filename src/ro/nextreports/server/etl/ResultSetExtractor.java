/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.nextreports.server.etl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class ResultSetExtractor implements Extractor {

    private static final Logger log = LoggerFactory.getLogger(ResultSetExtractor.class);

    private List<String> columnNames;
    private List<Class<?>> columnTypes;
    private int columnCount;
    private long rowCount;
    private ResultSet resultSet;

    public ResultSetExtractor(ResultSet resultSet) {
        columnNames = new ArrayList<String>();
        columnTypes = new ArrayList<Class<?>>();

        this.resultSet = resultSet;

        try {
            extractMetaData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init() {
        log.debug("Init extractor");
    }

    @Override
    public Iterator<Row> extract() {
        return new RowIterator();
    }

    @Override
    public void destroy() {
        log.debug("Destroy extractor");
        try {
            resultSet.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected Row createRow(long rowNumber, Object[] values) {
//        log.debug("Create row with number {}", rowNumber);
        return new DefaultRow(rowNumber, columnNames, columnTypes, values);
    }

    private void extractMetaData() throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        columnCount = resultSetMetaData.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            columnNames.add(resultSetMetaData.getColumnLabel(i + 1));
            try {
                columnTypes.add(Class.forName(resultSetMetaData.getColumnClassName(i + 1)));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private class RowIterator implements Iterator<Row> {

        @Override
        public boolean hasNext() {
            try {
                return resultSet.next();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Row next() {
            Object[] values = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                try {
                    values[i] = resultSet.getObject(i + 1);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            rowCount++;

            return createRow(rowCount, values);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
