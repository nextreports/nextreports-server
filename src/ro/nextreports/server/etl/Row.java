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

/**
 * @author Decebal Suiu
 */
public interface Row {

    public String getFieldName(int fieldIndex);

    public int getFieldIndex(String fieldName);

    public Class<?> getFieldType(int fieldIndex);

    public int getFieldCount();

    public long getRowNumber();

    public Object getValue(int fieldIndex);

    public Object[] getValues();

    public Object getPayload();

    public void setPayload(Object payload);

}
