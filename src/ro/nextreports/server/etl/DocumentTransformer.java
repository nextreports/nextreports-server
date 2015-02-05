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

import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * @author Decebal Suiu
 */
public class DocumentTransformer implements Transformer {

    @Override
    public void init() {
    }

    @Override
    public Row transform(Row row) {
        ODocument document = (ODocument) row.getPayload();
        if (document == null) {
            document = createDocument(row);
            row.setPayload(document);
        }

        return row;
    }

    @Override
    public void destroy() {
    }

    protected ODocument createDocument(Row row) {
        ODocument document = new ODocument();
        int fieldCount = row.getFieldCount();
        for (int i = 0; i < fieldCount; i++) {
            document.field(row.getFieldName(i), row.getFieldValue(i));
        }

        return document;
    }

}
