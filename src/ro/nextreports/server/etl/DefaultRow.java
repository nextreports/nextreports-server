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

import java.util.Arrays;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class DefaultRow implements Row {

    private long rowNumber;
    private List<String> fieldNames;
    private List<Class<?>> fieldTypes;
    private Object[] values;
    private Object payload;

    public DefaultRow(long rowNumber, List<String> fieldNames, List<Class<?>> fieldTypes, Object[] values) {
        this.rowNumber = rowNumber;
        this.fieldNames = fieldNames;
        this.fieldTypes = fieldTypes;
        this.values = values;
    }

    @Override
    public long getRowNumber() {
        return rowNumber;
    }

    @Override
    public List<String> getFieldNames() {
        return fieldNames;
    }

    @Override
    public String getFieldName(int fieldIndex) {
        return fieldNames.get(fieldIndex);
    }

    @Override
    public int getFieldIndex(String fieldName) {
        for (int i = 0; i < fieldNames.size(); i++) {
            if (fieldNames.get(i).equals(fieldName)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public Class<?> getFieldType(int fieldIndex) {
        return fieldTypes.get(fieldIndex);
    }

    @Override
    public int getFieldCount() {
        return fieldNames.size();
    }

    @Override
    public Object getFieldValue(int fieldIndex) {
        return values[fieldIndex];
    }

    @Override
    public Object getPayload() {
        return payload;
    }

    @Override
    public void setPayload(Object payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "DefaultRow{" +
                "rowNumber=" + rowNumber +
                ", fieldNames=" + fieldNames +
                ", fieldTypes=" + fieldTypes +
                ", values=" + Arrays.toString(values) +
                ", payload=" + payload +
                '}';
    }

}
