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

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.OSchemaException;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Decebal Suiu
 */
public class OrientDbLoader implements Loader {

    private static final Logger log = LoggerFactory.getLogger(OrientDbLoader.class);

    private String dbUrl;
    private String dbUser = "admin";
    private String dbPassword = "admin";
    private boolean dbAutoCreate = true;
    private boolean dbAutoDropIfExists;
    private boolean dbAutoCreateProperties;

    private int batchCommit;
    private long batchCounter;

    private ODatabaseDocumentTx documentDatabase;
    private String className;
    private boolean autoDropClass;

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public boolean isDbAutoCreate() {
        return dbAutoCreate;
    }

    public void setDbAutoCreate(boolean dbAutoCreate) {
        this.dbAutoCreate = dbAutoCreate;
    }

    public boolean isDbAutoDropIfExists() {
        return dbAutoDropIfExists;
    }

    public void setDbAutoDropIfExists(boolean dbAutoDropIfExists) {
        this.dbAutoDropIfExists = dbAutoDropIfExists;
    }

    public boolean isDbAutoCreateProperties() {
        return dbAutoCreateProperties;
    }

    public void setDbAutoCreateProperties(boolean dbAutoCreateProperties) {
        this.dbAutoCreateProperties = dbAutoCreateProperties;
    }

    public int getBatchCommit() {
        return batchCommit;
    }

    public void setBatchCommit(int batchCommit) {
        this.batchCommit = batchCommit;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isAutoDropClass() {
        return autoDropClass;
    }

    public void setAutoDropClass(boolean autoDropClass) {
        this.autoDropClass = autoDropClass;
    }

    @Override
    public void init() {
        log.debug("Init loader");
        documentDatabase = new ODatabaseDocumentTx(dbUrl, false);
        String databaseName = documentDatabase.getName();

        if (documentDatabase.exists() && dbAutoDropIfExists) {
            log.debug("Dropping existent database '{}'", databaseName);
            documentDatabase.open(dbUser, dbPassword).drop();
        }

        if (documentDatabase.exists()) {
            log.debug("Open database '{}'", databaseName);
            documentDatabase.open(dbUser, dbPassword);

            if ((className != null) && autoDropClass) {
                OSchema schema = documentDatabase.getMetadata().getSchema();
                if (schema.existsClass(className)) {
                    log.debug("Dropping class '{}'", className);
                    documentDatabase.command(new OCommandSQL("DELETE FROM " + className)).execute();
                    schema.dropClass(className);
                }
            }
        } else {
            long time = System.currentTimeMillis();
            log.debug("Create database '{}'", databaseName);
            documentDatabase.create();
            time = System.currentTimeMillis() - time;
            log.debug("Created database '{}' in {} ms", databaseName, time);
        }

        documentDatabase.declareIntent(new OIntentMassiveInsert());
    }

    @Override
    public void load(Row row) {
        if (row == null) {
            return;
        }

        ODocument document = (ODocument) row.getPayload();

        if (dbAutoCreateProperties) {
            OClass clazz;
            if (className != null) {
                clazz = getOrCreateClass(className);
            } else {
                clazz = document.getSchemaClass();
            }

            for (String fieldName : document.fieldNames()) {
                String newName = transformFieldName(fieldName);
                String name = newName != null ? newName : fieldName;

                OProperty property = clazz.getProperty(name);
                if (property == null) {
                    Object value = document.field(fieldName);
                    createProperty(clazz, name, value);
                    if (newName != null) {
                        // replace it
                        document.removeField(fieldName);
                        document.field(newName, value);
                    }
                }
            }
        }

        if (className != null) {
            document.setClassName(className);
        }

        if (!documentDatabase.getTransaction().isActive()) {
            // begin the transaction first
            documentDatabase.begin();
        }

//        log.debug("Load document {}", document);
        document.save();

        if (batchCommit > 0) {
            if (batchCounter > batchCommit) {
                documentDatabase.commit();
                documentDatabase.begin();
                batchCounter = 0;
            } else
                batchCounter++;
        }
    }

    @Override
    public void destroy() {
        log.debug("Destroy loader");
        documentDatabase.close();
    }

    protected OClass getOrCreateClass(String className) {
        OClass clazz;

        if (documentDatabase.getMetadata().getSchema().existsClass(className)) {
            clazz = documentDatabase.getMetadata().getSchema().getClass(className);
        } else {
            clazz = documentDatabase.getMetadata().getSchema().createClass(className);
            log.debug("Created class '{}'", className);
        }

        return clazz;
    }

    protected void createProperty(OClass clazz, String name, Object value) {
        if (value != null) {
            OType type = OType.getTypeByClass(value.getClass());

            try {
                clazz.createProperty(name, type);
            } catch (OSchemaException e) {
                log.error(e.getMessage(), e);
            }

            log.debug("Created property '{}' of type '{}'", name, type);
        }
    }

    private String transformFieldName(String fieldName) {
        char first = fieldName.charAt(0);
        if (!Character.isDigit(first)) {
            return null;
        }

        return "field" + Character.toUpperCase(first) + (fieldName.length() > 1 ? fieldName.substring(1) : "");
    }

}
